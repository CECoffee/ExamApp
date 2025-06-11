package dev.coffee.examapp.wrongquestion

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.rememberNavController
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import dev.coffee.examapp.R
import dev.coffee.examapp.model.Question
import dev.coffee.examapp.model.QuestionType
import dev.coffee.examapp.model.WrongQuestion
import dev.coffee.examapp.ui.components.WrongQuestionCard
import dev.coffee.examapp.ui.components.latex.LatexWebview
import dev.coffee.examapp.ui.screens.wrongQuestion.WrongQuestionScreen
import dev.coffee.examapp.viewmodel.WrongQuestionViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaField

@ExperimentalMaterial3Api
class WrongQuestionScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var viewModel: WrongQuestionViewModel

    private fun setPrivateStateFlow(viewModel: WrongQuestionViewModel, fieldName: String, value: Any?) {
        val kClass = viewModel::class
        val property = kClass.declaredMemberProperties.first { it.name == fieldName }
        property.isAccessible = true
        val field = property.javaField!!
        field.isAccessible = true
        val stateFlow = field.get(viewModel) as MutableStateFlow<Any?>
        stateFlow.value = value
    }


    private val fakeWrongQuestion = WrongQuestion(
        questionId = 1,
        content = "\\frac{1}{2} + \\frac{1}{3}", // 示例 LaTeX
        myAnswer = "2/5",
        correctAnswer = "5/6"
    )

    @Test
    fun showsQuestionAndAnswers_correctly() {
        composeTestRule.setContent {
            MaterialTheme {
                WrongQuestionCard(
                    question = fakeWrongQuestion,
                    isLoading = false
                )
            }
        }

        // 标题
        composeTestRule.onNodeWithText("题目：").assertIsDisplayed()

        // LaTeX 渲染框是否存在
        composeTestRule.onNodeWithTag("LatexWebview").assertExists()

        // 用户答案
        composeTestRule.onNodeWithText("你的答案：2/5").assertIsDisplayed()

        // 正确答案
        composeTestRule.onNodeWithText("正确答案：5/6").assertIsDisplayed()

        // 按钮存在并可点击
        composeTestRule.onNodeWithTag("ViewExplanationButton").assertIsDisplayed().assertIsEnabled()
    }


    @Test
    fun showsLoadingIndicator_whenLoading() {
        composeTestRule.setContent {
            MaterialTheme {
                WrongQuestionCard(
                    question = fakeWrongQuestion,
                    isLoading = true
                )
            }
        }

        // 查看解析按钮应该禁用
        composeTestRule.onNodeWithTag("ViewExplanationButton").assertIsNotEnabled()

        // 加载指示器应该显示
        composeTestRule.onNodeWithTag("LoadingIndicator").assertIsDisplayed()
    }

    @Test
    fun clickingViewExplanation_callsCallback() {
        var clicked = false

        composeTestRule.setContent {
            MaterialTheme {
                WrongQuestionCard(
                    question = fakeWrongQuestion,
                    isLoading = false,
                    onViewExplanation = { clicked = true }
                )
            }
        }

        composeTestRule.onNodeWithTag("ViewExplanationButton").performClick()
        assert(clicked)
    }

    @Test
    fun wrongQuestionScreen_showsEmptyState_whenNoWrongQuestions() {
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        val viewModel = WrongQuestionViewModel()
        setPrivateStateFlow(viewModel, "_isLoading", false)
        setPrivateStateFlow(viewModel, "wrongQuestions", emptyList<WrongQuestion>())

        composeTestRule.setContent {
            WrongQuestionScreen(navController = navController, viewModel = viewModel)
        }

        composeTestRule.onNodeWithText("暂无错题记录").assertIsDisplayed()
    }

    @Test
    fun wrongQuestionScreen_showsWrongQuestionCard_whenDataIsLoaded() {
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        val viewModel = WrongQuestionViewModel()
        val fakeQuestion = WrongQuestion(
            questionId = 1,
            content = "错题内容",
            myAnswer = "A",
            correctAnswer = "B"
        )

        setPrivateStateFlow(viewModel, "_isLoading", false)
        setPrivateStateFlow(viewModel, "wrongQuestions", listOf(fakeQuestion))

        composeTestRule.setContent {
            WrongQuestionScreen(navController = navController, viewModel = viewModel)
        }

        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule
                .onAllNodesWithTag("question_content_tag")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        composeTestRule.onNodeWithTag("question_content_tag").assertIsDisplayed()

    }



    @Test
    fun wrongQuestionScreen_showsDialog_whenShowQuestionDialogTrue() {
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        val viewModel = WrongQuestionViewModel()
        val question = Question(
            id = 1,
            difficulty = 2,
            content = "题目详情内容",
            questionType = QuestionType.FILL_IN_THE_BLANK,
            options = listOf("A", "B", "C", "D"),
            correctAnswer = "C",
            myAnswer = "A",
            explanation = "这是一个解释",
            isCorrect = false
        )

        setPrivateStateFlow(viewModel, "questionDetail", question)
        setPrivateStateFlow(viewModel, "_showQuestionDialog", true)
        setPrivateStateFlow(viewModel, "_loadingSimilarQuestions", false)

        composeTestRule.setContent {
            WrongQuestionScreen(navController = navController, viewModel = viewModel)
        }

        composeTestRule.onNodeWithText("题目详情内容").assertIsDisplayed()
        composeTestRule.onNodeWithText("AI同类错题推荐").assertIsDisplayed()
        composeTestRule.onNodeWithText("这是一个解释").assertIsDisplayed()
    }

    @Test
    fun wrongQuestionScreen_showsLoadMoreButton_whenHasMoreAndNotLoading() {
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        val viewModel = WrongQuestionViewModel()

        setPrivateStateFlow(viewModel, "_isLoading", false)
        setPrivateStateFlow(viewModel, "wrongQuestions", listOf(fakeWrongQuestion))
        setPrivateStateFlow(viewModel, "_hasMore", true)

        composeTestRule.setContent {
            WrongQuestionScreen(navController = navController, viewModel = viewModel)
        }

        composeTestRule.onNodeWithText("加载更多").assertIsDisplayed()
    }


    @Test
    fun wrongQuestionScreen_showsLoadingIndicatorAtBottom_whenLoadingAndHasData() {
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        val viewModel = WrongQuestionViewModel()

        setPrivateStateFlow(viewModel, "_isLoading", true)
        setPrivateStateFlow(viewModel, "wrongQuestions", listOf(fakeWrongQuestion))

        composeTestRule.setContent {
            WrongQuestionScreen(navController = navController, viewModel = viewModel)
        }

        composeTestRule.onNodeWithTag("LoadingIndicator")
    }

}
