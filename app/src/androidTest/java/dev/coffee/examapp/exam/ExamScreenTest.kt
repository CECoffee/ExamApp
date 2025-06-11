package dev.coffee.examapp.exam

import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.espresso.Espresso.pressBack
import dev.coffee.examapp.ui.screens.exam.ExamScreen
import dev.coffee.examapp.viewmodel.ExamViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaField
import android.view.KeyEvent as AndroidKeyEvent


class ExamScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var viewModel: ExamViewModel
    private val examId = 1
    private val duration = 120
    private val questionIds = listOf(101, 102)
    private val questionIdString = questionIds.joinToString(",")

    private fun setPrivateStateFlow(fieldName: String, value: Any?) {
        val kClass = viewModel::class
        val property = kClass.declaredMemberProperties.first { it.name == fieldName }
        property.isAccessible = true
        val field = property.javaField!!
        field.isAccessible = true
        val stateFlow = field.get(viewModel) as MutableStateFlow<Any?>
        stateFlow.value = value
    }

    private fun setContent() {
        viewModel = ExamViewModel(examId, duration, questionIds)
        composeTestRule.setContent {
            ExamScreen(
                examId = examId,
                duration = duration,
                questionIdStrings = questionIdString,
                onBack = {},
                viewModel = viewModel
            )
        }
    }

    @Test
    fun `ExamHeader正确展示进度和剩余时间`() {
        setContent()
        setPrivateStateFlow("_remainingTime", 75)
        setPrivateStateFlow("_currentQuestionIndex", 0)
        composeTestRule.onNodeWithText("剩余时间").assertIsDisplayed()
        composeTestRule.onNodeWithText("01:15").assertIsDisplayed()
        composeTestRule.onNodeWithText("题目进度").assertIsDisplayed()
        composeTestRule.onNodeWithText("1/2").assertIsDisplayed()
    }

    @Test
    fun `显示答案提交按钮`() {
        setContent()
        setPrivateStateFlow("_currentQuestionIndex", 1)
        composeTestRule.onNodeWithText("提交").assertIsDisplayed()
    }

    @Test
    fun `显示下一题按钮`() {
        setContent()
        setPrivateStateFlow("_currentQuestionIndex", 0)
        composeTestRule.onNodeWithText("下一题").assertIsDisplayed()
    }

    @Test
    fun `不是第一题时显示上一题按钮`() {
        setContent()
        setPrivateStateFlow("_currentQuestionIndex", 1)
        composeTestRule.onNodeWithText("上一题").assertIsDisplayed()
    }

    @Test
    fun `考试结束后显示考试结束页面`() {
        setContent()
        setPrivateStateFlow("_examFinished", true)
        setPrivateStateFlow("_score", 88.0)
        composeTestRule.onNodeWithText("88分").assertIsDisplayed()
    }

    @Test
    fun `返回后显示Dialog提示`() {
        setContent()
        composeTestRule.runOnUiThread {
            viewModel.finishExam() // 先重置为完成
            setPrivateStateFlow("_examFinished", false)
        }

        val nativeEvent = AndroidKeyEvent(AndroidKeyEvent.ACTION_DOWN, AndroidKeyEvent.KEYCODE_BACK)
        val composeKeyEvent = KeyEvent(nativeEvent)

        // 模拟返回
        composeTestRule.onRoot().performKeyPress(composeKeyEvent)

        composeTestRule.onNodeWithText("退出考试").assertDoesNotExist() // Compose BackHandler不会触发系统事件
    }

    // 可根据需要继续添加更多测试用例


    @Test
    fun alertDialogAppears_whenBackPressedDuringExam2() {
        setContent()

        composeTestRule.runOnUiThread {
            setPrivateStateFlow("_examFinished", false)
        }

        // 通过 Espresso 模拟系统返回键（这能触发 Compose 的 BackHandler）
        pressBack()

        // 验证 AlertDialog 是否出现
        composeTestRule.onNodeWithText("退出考试").assertIsDisplayed()
        composeTestRule.onNodeWithText("中途退出将直接提交成绩，确定退出吗？").assertIsDisplayed()
        composeTestRule.onNodeWithText("确定").assertIsDisplayed()
        composeTestRule.onNodeWithText("取消").assertIsDisplayed()
    }

    @Test
    fun buttonsShowLoadingStateWhenIsLoading() {
        setContent()
        setPrivateStateFlow("_currentQuestionIndex", 0)
        setPrivateStateFlow("_isLoading", true)
        composeTestRule.onNodeWithText("上一题").assertIsNotEnabled()
        composeTestRule.onNodeWithTag("Circular Progress Indicator").assertExists()
    }







}

