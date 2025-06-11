package dev.coffee.examapp.wrongquestion

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import dev.coffee.examapp.model.Question
import dev.coffee.examapp.model.QuestionType
import dev.coffee.examapp.ui.screens.wrongQuestion.SimilarQuestionsScreen
import dev.coffee.examapp.viewmodel.SimilarQuestionsViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaField
@OptIn(ExperimentalMaterial3Api::class)

class SimilarQuestionsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setPrivateStateFlow(viewModel: SimilarQuestionsViewModel, fieldName: String, value: Any?) {
        val kClass = viewModel::class
        val property = kClass.declaredMemberProperties.first { it.name == fieldName }
        property.isAccessible = true
        val field = property.javaField!!
        field.isAccessible = true
        val stateFlow = field.get(viewModel) as MutableStateFlow<Any?>
        stateFlow.value = value
    }

    private fun createFakeViewModel(): SimilarQuestionsViewModel {
        val fake = SimilarQuestionsViewModel(listOf(1, 2, 3))

        setPrivateStateFlow(fake, "_currentQuestionIndex", 0)
        setPrivateStateFlow(fake, "_correctCount", 1)
        setPrivateStateFlow(fake, "_practiceFinished", false)
        setPrivateStateFlow(fake, "_showExplanation", false)
        setPrivateStateFlow(fake, "_userAnswer", "")
        setPrivateStateFlow(fake, "_isLoading", false)

        val fakeQuestion = Question(
            id = 1,
            difficulty = 2,
            content = "请填写下列空白：Kotlin 是一种 _______ 语言。",
            questionType = QuestionType.FILL_IN_THE_BLANK,
            options = null,
            correctAnswer = "现代",
            myAnswer = null,
            explanation = "Kotlin 是一种现代、简洁且安全的编程语言，适用于多平台开发。",
            isCorrect = null
        )

        setPrivateStateFlow(fake, "_currentQuestion", fakeQuestion)

        return fake
    }

    private fun setContentWithInjectedViewModel(viewModel: SimilarQuestionsViewModel) {
        val viewModelStore = ViewModelStore()
        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return viewModel as T
            }
        }
        val owner = object : ViewModelStoreOwner {
            override val viewModelStore: ViewModelStore = viewModelStore
        }

        ViewModelProvider(owner, factory).get(SimilarQuestionsViewModel::class.java) // 预注册

        composeTestRule.setContent {
            CompositionLocalProvider(LocalViewModelStoreOwner provides owner) {
                SimilarQuestionsScreen(
                    questionIdStrings = "1,2,3",
                    onBack = {}
                )
            }
        }
    }

    @Test
    fun similarQuestionsScreen_displaysHeaderInfoCorrectly() {
        val viewModel = createFakeViewModel()
        setContentWithInjectedViewModel(viewModel)

        composeTestRule.onNodeWithText("错题本").assertIsDisplayed()
        composeTestRule.onNodeWithText("当前进度").assertExists()
        composeTestRule.onNodeWithText("1/3").assertExists()
        composeTestRule.onNodeWithText("正确数量").assertExists()
        composeTestRule.onNodeWithText("1").assertExists()
        composeTestRule.onNodeWithText("正确率").assertExists()
        composeTestRule.onNodeWithText("100%").assertExists()
    }

    @Test
    fun similarQuestionsScreen_showsQuestionCardAndSubmitButton() {
        val viewModel = createFakeViewModel()
        setContentWithInjectedViewModel(viewModel)

        composeTestRule.onNodeWithText("请填写下列空白：Kotlin 是一种 _______ 语言。").assertExists()
        composeTestRule.onNodeWithText("提交答案").assertIsDisplayed()
    }

    @Test
    fun similarQuestionsScreen_showsResultScreen_whenFinished() {
        val viewModel = createFakeViewModel()
        setPrivateStateFlow(viewModel, "_practiceFinished", true)
        setPrivateStateFlow(viewModel, "_correctCount", 1)
        setContentWithInjectedViewModel(viewModel)

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithText("练习完成").fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithText("练习完成").assertExists()
        composeTestRule.onNodeWithText("1/3").assertExists()
        composeTestRule.onNodeWithText("33%").assertExists()
        composeTestRule.onNodeWithText("继续努力！").assertExists()
    }


    @Test
    fun similarQuestionsScreen_showsNextButton_whenExplanationShown() {
        val viewModel = createFakeViewModel()
        setPrivateStateFlow(viewModel, "_showExplanation", true)
        setContentWithInjectedViewModel(viewModel)

        composeTestRule.onNodeWithText("下一题").assertExists()
    }

    @Test
    fun similarQuestionsScreen_showsExplanation_afterAnswerSubmitted() {
        val viewModel = createFakeViewModel()
        // 模拟用户作答 + 显示解析
        setPrivateStateFlow(viewModel, "_userAnswer", "现代")
        setPrivateStateFlow(viewModel, "_showExplanation", true)
        setContentWithInjectedViewModel(viewModel)

        // 解析应该展示，提交按钮消失，出现“下一题”
        composeTestRule.onNodeWithText("下一题").assertExists()
        composeTestRule.onNodeWithText("Kotlin 是一种现代、简洁且安全的编程语言，适用于多平台开发。").assertExists()
    }

    @Test
    fun similarQuestionsScreen_showsFinishButton_onLastQuestionWithExplanation() {
        val viewModel = createFakeViewModel()
        // 当前是最后一题 + 显示解析
        setPrivateStateFlow(viewModel, "_currentQuestionIndex", 2) // 第3题（0-based）
        setPrivateStateFlow(viewModel, "_showExplanation", true)
        setContentWithInjectedViewModel(viewModel)

        composeTestRule.onNodeWithText("完成").assertExists()
    }

    @Test
    fun similarQuestionsScreen_showsZeroPercentAccuracyCorrectly() {
        val viewModel = createFakeViewModel()
        setPrivateStateFlow(viewModel, "_correctCount", 0)
        setPrivateStateFlow(viewModel, "_currentQuestionIndex", 0)
        setContentWithInjectedViewModel(viewModel)

        composeTestRule.onNodeWithText("正确率").assertExists()
        composeTestRule.onNodeWithText("0%").assertExists()
    }

    @Test
    fun similarQuestionsScreen_showsLoadingIndicator_onSubmit() {
        val viewModel = createFakeViewModel()
        setPrivateStateFlow(viewModel, "_isLoading", true)
        setPrivateStateFlow(viewModel, "_userAnswer", "现代") // 否则按钮禁用
        setContentWithInjectedViewModel(viewModel)

        // 验证是否展示加载动画（通过进度条）
        composeTestRule.onNodeWithTag("Loading").assertExists()
    }




}
