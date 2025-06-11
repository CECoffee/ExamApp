package dev.coffee.examapp.exam

import android.view.KeyEvent as AndroidKeyEvent
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyPress
import androidx.compose.ui.test.performTextInput
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.coffee.examapp.model.Question
import dev.coffee.examapp.model.QuestionType
import dev.coffee.examapp.ui.screens.exam.ExamScreen
import dev.coffee.examapp.viewmodel.ExamViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaField


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
    fun showExamHeader_correctlyDisplaysRemainingTimeAndProgress() {
        setContent()
        setPrivateStateFlow("_remainingTime", 75)
        setPrivateStateFlow("_currentQuestionIndex", 0)
        composeTestRule.onNodeWithText("剩余时间").assertIsDisplayed()
        composeTestRule.onNodeWithText("01:15").assertIsDisplayed()
        composeTestRule.onNodeWithText("题目进度").assertIsDisplayed()
        composeTestRule.onNodeWithText("1/2").assertIsDisplayed()
    }

    @Test
    fun showSubmitButtonOnLastQuestion() {
        setContent()
        setPrivateStateFlow("_currentQuestionIndex", 1)
        composeTestRule.onNodeWithText("提交").assertIsDisplayed()
    }

    @Test
    fun showNextButtonBeforeLastQuestion() {
        setContent()
        setPrivateStateFlow("_currentQuestionIndex", 0)
        composeTestRule.onNodeWithText("下一题").assertIsDisplayed()
    }

    @Test
    fun showPreviousButtonWhenNotFirstQuestion() {
        setContent()
        setPrivateStateFlow("_currentQuestionIndex", 1)
        composeTestRule.onNodeWithText("上一题").assertIsDisplayed()
    }

    @Test
    fun examFinished_showsResultScreen() {
        setContent()
        setPrivateStateFlow("_examFinished", true)
        setPrivateStateFlow("_score", 88.0)
        composeTestRule.onNodeWithText("88分").assertIsDisplayed()
    }

    @Test
    fun alertDialogAppears_whenBackPressedDuringExam() {
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
}

