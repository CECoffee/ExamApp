package dev.coffee.examapp

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import dev.coffee.examapp.model.Exam
import dev.coffee.examapp.model.ExamStatus
import dev.coffee.examapp.ui.screens.exam.ExamListScreen
import dev.coffee.examapp.viewmodel.ExamListViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import java.util.Date
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaField


class ExamListScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setPrivateStateFlow(viewModel: ExamListViewModel, fieldName: String, value: Any?) {
        val kClass = viewModel::class
        val property = kClass.declaredMemberProperties.first { it.name == fieldName }
        property.isAccessible = true
        val field = property.javaField!!
        field.isAccessible = true
        val stateFlow = field.get(viewModel) as MutableStateFlow<Any?>
        stateFlow.value = value
    }

    @Test
    fun examList_showsPendingExams_whenDataIsLoaded() {
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        val viewModel = ExamListViewModel()
        setPrivateStateFlow(
            viewModel,
            "_exams",
            listOf(
                Exam(
                    id = 1,
                    name = "测试考试",
                    startTime = Date(),
                    endTime = Date(),
                    duration = 90,
                    status = ExamStatus.PENDING,
                    questionList = listOf(1, 2, 3)
                )
            )
        )

        composeTestRule.setContent {
            ExamListScreen(navController = navController, viewModel = viewModel)
        }

        composeTestRule.onNodeWithText("测试考试").assertIsDisplayed()
    }

    @Test
    fun examList_showsErrorToast_whenErrorMessageSet() {
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        val viewModel = ExamListViewModel()
        setPrivateStateFlow(viewModel, "_exams", emptyList<Exam>())
        setPrivateStateFlow(viewModel, "_errorMessage", "加载失败")

        composeTestRule.setContent {
            ExamListScreen(navController = navController, viewModel = viewModel)
        }

        // Toast 检查（需配合 ShadowToast 或 Robolectric，略）
    }

   @Test
    fun examList_showsLoadingIndicator_whenLoading() {
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        val viewModel = ExamListViewModel()
        setPrivateStateFlow(viewModel, "_isLoading", true)

        composeTestRule.setContent {
            ExamListScreen(navController = navController, viewModel = viewModel)
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("LoadingIndicator").assertIsDisplayed()
    }

    @Test
    fun displaysTitleAndTabs() {
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        val viewModel = ExamListViewModel()
        setPrivateStateFlow(viewModel, "_exams", emptyList<Exam>())
        setPrivateStateFlow(viewModel, "_errorMessage", "加载失败")

        composeTestRule.setContent {
            ExamListScreen(navController = navController, viewModel = viewModel)
        }

        composeTestRule.onNodeWithText("待考").assertExists()
        composeTestRule.onNodeWithText("已考").assertExists()
        composeTestRule.onNodeWithText("已过期").assertExists()
    }

    @Test
    fun showsPendingExamsInDefaultTab() {
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        val viewModel = ExamListViewModel()
        setPrivateStateFlow(
            viewModel,
            "_exams",
            listOf(
                Exam(
                    id = 1,
                    name = "数学考试",
                    startTime = Date(),
                    endTime = Date(),
                    duration = 90,
                    status = ExamStatus.PENDING,
                    questionList = listOf(1, 2, 3)
                ),
                Exam(
                    id = 2,
                    name = "历史考试",
                    startTime = Date(),
                    endTime = Date(),
                    duration = 90,
                    status = ExamStatus.COMPLETED,
                    questionList = listOf(1, 2, 3)
                )
            )
        )

        composeTestRule.setContent {
            ExamListScreen(navController = navController, viewModel = viewModel)
        }

        composeTestRule.onNodeWithText("数学考试").assertExists()
        composeTestRule.onNodeWithText("历史考试").assertDoesNotExist()
        composeTestRule.onNodeWithText("已考").performClick()
        composeTestRule.onNodeWithText("历史考试").assertExists()
        composeTestRule.onNodeWithText("数学考试").assertDoesNotExist()
    }
}