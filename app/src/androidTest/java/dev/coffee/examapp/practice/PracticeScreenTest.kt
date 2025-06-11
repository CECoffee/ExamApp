package dev.coffee.examapp.practice

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dev.coffee.examapp.model.Question
import dev.coffee.examapp.model.QuestionType
import dev.coffee.examapp.ui.screens.exam.ExamScreen
import dev.coffee.examapp.ui.screens.practice.PracticeScreen
import dev.coffee.examapp.viewmodel.ExamViewModel
import dev.coffee.examapp.viewmodel.PracticeViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaField

class PracticeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var viewModel: PracticeViewModel
    private val chapterId = "test"
    private val chapterName = "测试"

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
        viewModel = PracticeViewModel(chapterId, chapterName)
        composeTestRule.setContent {
            PracticeScreen(
                chapterId = chapterId,
                chapterName = chapterName,
                onBack = {},
                viewModel = viewModel
            )
        }
    }

    @Test
    fun `ExamHeader正确展示标题和进度`() {
        setContent()
        setPrivateStateFlow("_currentQuestionIndex", 0)
        composeTestRule.onNodeWithText("测试").assertIsDisplayed()
        composeTestRule.onNodeWithText("当前进度").assertIsDisplayed()
        composeTestRule.onNodeWithText("1/10").assertIsDisplayed()
    }

    @Test
    fun `显示答案提交按钮`() {
        setContent()
        setPrivateStateFlow("_currentQuestionIndex", 1)
        composeTestRule.onNodeWithText("提交答案").assertIsDisplayed()
    }

    @Test
    fun `提交答案后显示解析页面`() {
        setContent()
        setPrivateStateFlow("_currentQuestion",
            Question(
                id = 101,
                difficulty = 1,
                content = "1+1=?",
                questionType = QuestionType.FILL_IN_THE_BLANK,
                correctAnswer = "2",
                explanation = "基础加法"
            )
        )
        setPrivateStateFlow("_userAnswer", "2")
        composeTestRule.onNodeWithText("提交答案").performClick()
        setPrivateStateFlow("_showExplanation", true)

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("解析:").assertIsDisplayed()
    }

    @Test
    fun `解析页面显示下一题按钮`() {
        setContent()
        setPrivateStateFlow("_showExplanation", true)
        composeTestRule.onNodeWithText("下一题").assertIsDisplayed()
    }

    @Test
    fun `解析页面点击下一题后显示下一题`() {
        setContent()
        setPrivateStateFlow("_showExplanation", true)
        setPrivateStateFlow("_nextQuestion",
            Question(
                id = 101,
                difficulty = 1,
                content = "1+1=?",
                questionType = QuestionType.FILL_IN_THE_BLANK,
                correctAnswer = "2",
                explanation = "基础加法"
            )
        )
        composeTestRule.onNodeWithText("下一题").performClick()

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("提交答案").assertIsDisplayed()
    }

    @Test
    fun `结束后显示练习结束页面`() {
        setContent()
        setPrivateStateFlow("_practiceFinished", true)
        setPrivateStateFlow("_correctCount", 10)
        composeTestRule.onNodeWithText("10/10").assertIsDisplayed()
    }

}