package dev.coffee.examapp.statistic

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import dev.coffee.examapp.model.Exam
import dev.coffee.examapp.model.ExamStatus
import dev.coffee.examapp.ui.screens.statistic.StatisticScreen
import dev.coffee.examapp.viewmodel.ExamListViewModel
import io.mockk.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.*
import kotlinx.coroutines.flow.asStateFlow
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaField

class StatisticScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var navController: TestNavHostController
    private lateinit var viewModel: ExamListViewModel

    private fun setPrivateStateFlow(viewModel: ExamListViewModel, fieldName: String, value: Any?) {
        val kClass = viewModel::class
        val property = kClass.declaredMemberProperties.first { it.name == fieldName }
        property.isAccessible = true
        val field = property.javaField!!
        field.isAccessible = true
        val stateFlow = field.get(viewModel) as MutableStateFlow<Any?>
        stateFlow.value = value
    }

    private fun createFakeExams(): List<Exam> = listOf(
        Exam(
            id = 1,
            name = "期中考试",
            endTime = Date(),
            score = 89.5,
            status = ExamStatus.COMPLETED,
            startTime = Date(),
            duration = 120,
            questionList = listOf()
        ),
        Exam(
            id = 2,
            name = "期末考试",
            endTime = Date(),
            score = 92.0,
            status = ExamStatus.COMPLETED,
            startTime = Date(),
            duration = 120,
            questionList = listOf()
        )
    )

    private fun setContent(isLoading: Boolean = false, exams: List<Exam> = emptyList()) {
        viewModel = ExamListViewModel()
        setPrivateStateFlow(viewModel, "_isLoading", isLoading)
        setPrivateStateFlow(viewModel, "_exams", exams)

        composeTestRule.setContent {
            MaterialTheme {
                StatisticScreen(
                    navController = navController,
                    viewModel = viewModel
                )
            }
        }
    }

    @Before
    fun setup() {
        navController = TestNavHostController(ApplicationProvider.getApplicationContext())
    }

    @Test
    fun showsLoading_whenIsLoadingTrue() {
        setContent(isLoading = true)
        composeTestRule.onNodeWithTag("CircularProgressIndicator").assertExists()
    }

    @Test
    fun showsEmptyState_whenNoExams() {
        setContent(isLoading = false, exams = emptyList())
        composeTestRule.onNodeWithText("暂无有效的考试成绩数据").assertIsDisplayed()
        composeTestRule.onNodeWithText("完成考试后即可查看成绩趋势").assertIsDisplayed()
    }

    @Test
    fun showsChartAndExamRecords_whenExamsAvailable() {
        setContent(isLoading = false, exams = createFakeExams())
        composeTestRule.onNodeWithText("考试记录 ").assertIsDisplayed()
        composeTestRule.onNodeWithText("期中考试").assertIsDisplayed()
        composeTestRule.onNodeWithText("期末考试").assertIsDisplayed()
        composeTestRule.onNodeWithText("89.5分").assertIsDisplayed()
        composeTestRule.onNodeWithText("92.0分").assertIsDisplayed()
    }

    @Test
    fun loadMoreButtonAppears_whenMoreRecordsExist() {
        val exams = (1..8).map {
            Exam(
                id = it,
                name = "考试$it",
                endTime = Date(),
                score = 89.5,
                status = ExamStatus.COMPLETED,
                startTime = Date(),
                duration = 120,
                questionList = listOf()
            )
        }
        setContent(isLoading = false, exams = exams)
        composeTestRule.onNodeWithText("加载更多...").assertIsDisplayed()
        composeTestRule.onNodeWithText("考试1").assertIsDisplayed()
    }
}
