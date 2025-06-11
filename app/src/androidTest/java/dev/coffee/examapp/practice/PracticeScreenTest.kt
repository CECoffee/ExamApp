package dev.coffee.examapp.practice

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.printToLog
import dev.coffee.examapp.model.Question
import dev.coffee.examapp.model.QuestionType
import dev.coffee.examapp.ui.screens.practice.PracticeScreen
import dev.coffee.examapp.viewmodel.PracticeViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaField

class PracticeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // 反射设置 PracticeViewModel 内部 MutableStateFlow
    private fun setPrivateStateFlow(viewModel: PracticeViewModel, fieldName: String, value: Any?) {
        val kClass = viewModel::class
        val property = kClass.declaredMemberProperties.firstOrNull { it.name == fieldName }
            ?: error("未找到名为 $fieldName 的属性，实际属性有: ${kClass.declaredMemberProperties.map { it.name }}")
        property.isAccessible = true
        val field = property.javaField!!
        field.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val stateFlow = field.get(viewModel) as MutableStateFlow<Any?>
        stateFlow.value = value
    }

    private fun createFillBlankQuestion() = Question(
        id = 101,
        difficulty = 1,
        content = "地球是第__颗行星（填数字）",
        questionType = QuestionType.FILL_IN_THE_BLANK,
        options = null,
        correctAnswer = "3",
        myAnswer = null,
        explanation = "地球是太阳系的第三颗行星，即距离太阳第三近的行星。",
        isCorrect = null
    )

    @Test
    fun showFillInBlankQuestion_andSubmitButton() {
        val viewModel = PracticeViewModel("chapterId", "科学")

        composeTestRule.setContent {
            MaterialTheme {
                PracticeScreen(
                    chapterId = "chapterId",
                    chapterName = "科学",
                    onBack = { /* no-op */ },
                    viewModel = viewModel
                )
            }
        }

        // 等 UI 挂载后再注入状态（避免被 ViewModel init 逻辑覆盖）
        composeTestRule.waitForIdle()
        composeTestRule.runOnUiThread {
            setPrivateStateFlow(viewModel, "_currentQuestion", createFillBlankQuestion())
            setPrivateStateFlow(viewModel, "_currentQuestionIndex", 0)
            setPrivateStateFlow(viewModel, "_userAnswer", "some answer")
            setPrivateStateFlow(viewModel, "_isLoading", false)
            setPrivateStateFlow(viewModel, "_practiceFinished", false)
            setPrivateStateFlow(viewModel, "_showExplanation", false)
            setPrivateStateFlow(viewModel, "_correctCount", 0)
            setPrivateStateFlow(viewModel, "_showToast", null)
        }

        composeTestRule.waitUntil(timeoutMillis = 100_000) {
            composeTestRule.onAllNodesWithTag("question_content_tag").fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithTag("question_content_tag").assertExists()
        composeTestRule.onNodeWithTag("fill_blank_input").assertExists()
        composeTestRule.onNodeWithTag("submit_answer_btn").assertIsDisplayed()
    }


    @Test
    fun fillBlank_showExplanationAndNextOrFinishButton() {
        val viewModel = PracticeViewModel("chapterId", "科学")
        setPrivateStateFlow(viewModel, "_currentQuestion", createFillBlankQuestion())
        setPrivateStateFlow(viewModel, "_currentQuestionIndex", 0)
        setPrivateStateFlow(viewModel, "_userAnswer", "3")
        setPrivateStateFlow(viewModel, "_isLoading", false)
        setPrivateStateFlow(viewModel, "_practiceFinished", false)
        setPrivateStateFlow(viewModel, "_showExplanation", true)
        setPrivateStateFlow(viewModel, "_correctCount", 1)
        setPrivateStateFlow(viewModel, "_showToast", null)

        composeTestRule.setContent {
            MaterialTheme {
                PracticeScreen(
                    chapterId = "chapterId",
                    chapterName = "科学",
                    onBack = { /* no-op */ },
                    viewModel = viewModel
                )
            }
        }

        // 检查题干存在
        composeTestRule.onNodeWithTag("question_content_tag").assertExists()
        // 检查“下一题”或“完成练习”按钮
        composeTestRule.onNodeWithTag("next_or_finish_btn").assertIsDisplayed()
        // 检查“你的答案”隐藏Text
        composeTestRule.onNodeWithTag("user_answer_tag").assertExists()
        // 检查“正确答案”隐藏Text
        composeTestRule.onNodeWithTag("correct_answer_tag").assertExists()
        // 检查解析内容隐藏Text
        composeTestRule.onNodeWithTag("explanation_content_tag").assertExists()
        // 检查“回答正确”或“回答错误”文字（直接Text）
        composeTestRule.onNodeWithText("回答正确").assertIsDisplayed()
    }

    @Test
    fun practiceFinished_showResultScreen() {
        val viewModel = PracticeViewModel("chapterId", "科学")
        setPrivateStateFlow(viewModel, "_practiceFinished", true)
        setPrivateStateFlow(viewModel, "_correctCount", 1)
        setPrivateStateFlow(viewModel, "_showToast", null)

        composeTestRule.setContent {
            MaterialTheme {
                PracticeScreen(
                    chapterId = "chapterId",
                    chapterName = "科学",
                    onBack = { /* no-op */ },
                    viewModel = viewModel
                )
            }
        }

        // 检查结果页内容
        composeTestRule.onNodeWithText("练习完成").assertIsDisplayed()
        composeTestRule.onNodeWithText("1/10").assertIsDisplayed() // 默认10题
        composeTestRule.onNodeWithText("继续努力！").assertIsDisplayed()
        // 检查“返回”按钮（通过 testTag）
        composeTestRule.onNodeWithTag("result_back_btn").assertIsDisplayed()
    }

    @Test
    fun submitButton_isDisabled_whenUserAnswerIsBlank() {
        val viewModel = PracticeViewModel("chapterId", "科学")

        composeTestRule.setContent {
            MaterialTheme {
                PracticeScreen(
                    chapterId = "chapterId",
                    chapterName = "科学",
                    onBack = {},
                    viewModel = viewModel
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.runOnUiThread {
            setPrivateStateFlow(viewModel, "_currentQuestion", createFillBlankQuestion())
            setPrivateStateFlow(viewModel, "_currentQuestionIndex", 0)
            setPrivateStateFlow(viewModel, "_userAnswer", "") // 空字符串
            setPrivateStateFlow(viewModel, "_isLoading", false)
            setPrivateStateFlow(viewModel, "_practiceFinished", false)
            setPrivateStateFlow(viewModel, "_showExplanation", false)
        }

        composeTestRule.onNodeWithTag("submit_answer_btn").assertIsNotEnabled()
    }

    @Test
    fun header_displaysCurrentProgressAndAccuracy() {
        val viewModel = PracticeViewModel("chapterId", "科学")

        composeTestRule.setContent {
            MaterialTheme {
                PracticeScreen(
                    chapterId = "chapterId",
                    chapterName = "科学",
                    onBack = {},
                    viewModel = viewModel
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.runOnUiThread {
            setPrivateStateFlow(viewModel, "_currentQuestion", createFillBlankQuestion())
            setPrivateStateFlow(viewModel, "_currentQuestionIndex", 0)
            setPrivateStateFlow(viewModel, "_correctCount", 1) // 第一题就答对
            setPrivateStateFlow(viewModel, "_userAnswer", "3")
            setPrivateStateFlow(viewModel, "_isLoading", false)
            setPrivateStateFlow(viewModel, "_practiceFinished", false)
            setPrivateStateFlow(viewModel, "_showExplanation", false)
        }

        composeTestRule.onNodeWithText("1/10").assertIsDisplayed()
        composeTestRule.onNodeWithText("100%").assertIsDisplayed()
    }


    @Test
    fun backButton_invokesOnBackCallback() {
        val viewModel = PracticeViewModel("chapterId", "科学")
        var backCalled = false

        composeTestRule.setContent {
            MaterialTheme {
                PracticeScreen(
                    chapterId = "chapterId",
                    chapterName = "科学",
                    onBack = { backCalled = true },
                    viewModel = viewModel
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.runOnUiThread {
            setPrivateStateFlow(viewModel, "_currentQuestion", createFillBlankQuestion())
            setPrivateStateFlow(viewModel, "_currentQuestionIndex", 0)
            setPrivateStateFlow(viewModel, "_correctCount", 0)
            setPrivateStateFlow(viewModel, "_userAnswer", "")
            setPrivateStateFlow(viewModel, "_isLoading", false)
            setPrivateStateFlow(viewModel, "_practiceFinished", false)
            setPrivateStateFlow(viewModel, "_showExplanation", false)
        }

        // 点击返回 "<"
        composeTestRule.onNodeWithText("<").performClick()
        assert(backCalled) { "onBack 回调未被触发" }
    }


}