package dev.coffee.examapp.exam

import android.content.Context
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.NavType
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.composable
import androidx.navigation.createGraph
import androidx.navigation.navArgument
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import dev.coffee.examapp.model.Exam
import dev.coffee.examapp.model.ExamStatus
import dev.coffee.examapp.ui.navigation.Screen
import dev.coffee.examapp.ui.screens.exam.ExamList
import dev.coffee.examapp.ui.screens.exam.ExamListScreen
import dev.coffee.examapp.ui.screens.exam.ExamScreen
import dev.coffee.examapp.viewmodel.ExamListViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.util.*
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaField


class ExamListScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var navController: TestNavHostController

    private fun createFakeExams(): List<Exam> = listOf(
        Exam(
            id = 1,
            name = "测试考试",
            duration = 90,
            questionList = listOf(1, 2, 3),
            status = ExamStatus.PENDING,
            score = null,
            startTime = Date(),
            endTime = Date()
        ),
        Exam(
            id = 2,
            name = "历史考试",
            duration = 60,
            questionList = listOf(4, 5),
            status = ExamStatus.COMPLETED,
            score = 95.0,
            startTime = Date(),
            endTime = Date()
        )
    )

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
    fun `数据加载后显示代考考试`() {
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        val viewModel = ExamListViewModel()
        setPrivateStateFlow(
            viewModel,
            "_exams",
            createFakeExams()
        )

        composeTestRule.setContent {
            ExamListScreen(navController = navController, viewModel = viewModel)
        }

        composeTestRule.onNodeWithText("测试考试").assertIsDisplayed()
    }

    @Test
    fun `设置errorMessage-显示Toast提示`() {
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        val viewModel = ExamListViewModel()
        setPrivateStateFlow(viewModel, "_exams", emptyList<Exam>())
        setPrivateStateFlow(viewModel, "_errorMessage", "加载失败")

        composeTestRule.setContent {
            ExamListScreen(navController = navController, viewModel = viewModel)
        }
    }

    @Test
    fun `显示标题和Tab栏`() {
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
    fun `默认Tab栏显示待考考试`() {
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        val viewModel = ExamListViewModel()
        setPrivateStateFlow(
            viewModel,
            "_exams",
            createFakeExams()
        )

        composeTestRule.setContent {
            ExamListScreen(navController = navController, viewModel = viewModel)
        }

        composeTestRule.onNodeWithText("测试考试").assertExists()
        composeTestRule.onNodeWithText("历史考试").assertDoesNotExist()
        composeTestRule.onNodeWithText("已考").performClick()
        composeTestRule.onNodeWithText("历史考试").assertExists()
        composeTestRule.onNodeWithText("测试考试").assertDoesNotExist()
    }


    @Test
    fun `点击开始考试-导航至考试页面`() {
        val pendingExam = createFakeExams().first { it.status == ExamStatus.PENDING }

        composeTestRule.setContent {
            val context = ApplicationProvider.getApplicationContext<Context>()
            navController = TestNavHostController(context).apply {
                navigatorProvider.addNavigator(ComposeNavigator())
                graph = createGraph(startDestination = Screen.ExamList.route) {
                    composable(Screen.ExamList.route) {  }
                    composable(Screen.Exam.route + "/{examId}/{duration}/{questionIds}",
                        arguments = listOf(
                            navArgument("examId") { type = NavType.IntType },
                            navArgument("duration") { type = NavType.IntType },
                            navArgument("questionIds") { type = NavType.StringType }
                        )
                    ) {
                        backStackEntry ->
                            val examId = backStackEntry.arguments?.getInt("examId") ?: 0
                            val duration = backStackEntry.arguments?.getInt("duration") ?: 0
                            val questionIds = backStackEntry.arguments?.getString("questionIds") ?: ""

                        ExamScreen(
                            examId = examId,
                            duration = duration,
                            questionIdStrings = questionIds,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }

            MaterialTheme {
                // 只渲染ExamList，模拟点击
                ExamList(
                    exams = listOf(pendingExam),
                    status = ExamStatus.PENDING,
                    navController = navController
                )
            }
        }

        // 找到“开始考试”按钮并点击
        composeTestRule.onNodeWithText("开始考试", substring = true, ignoreCase = true)
            .assertIsDisplayed()
            .performClick()

        // 检查跳转
        composeTestRule.waitForIdle()

        val backStackEntry = navController.currentBackStackEntry
        assertEquals(Screen.Exam.route + "/{examId}/{duration}/{questionIds}", backStackEntry?.destination?.route)
        assertEquals(pendingExam.id, backStackEntry?.arguments?.getInt("examId"))
        assertEquals(pendingExam.duration, backStackEntry?.arguments?.getInt("duration"))
        assertEquals(
            pendingExam.questionList.joinToString(","),
            backStackEntry?.arguments?.getString("questionIds")
        )
    }

    @Test
    fun `点击查看成绩-导航至考试成绩页面`() {
        val completedExam = createFakeExams().first { it.status == ExamStatus.COMPLETED }

        composeTestRule.setContent {
            val context = ApplicationProvider.getApplicationContext<Context>()
            navController = TestNavHostController(context).apply {
                navigatorProvider.addNavigator(ComposeNavigator())
                graph = createGraph(startDestination = Screen.ExamList.route) {
                    composable(Screen.ExamList.route) {  }
                    composable(Screen.ExamResult.route + "/{score}") { }
                }
            }

            MaterialTheme {
                ExamList(
                    exams = listOf(completedExam),
                    status = ExamStatus.COMPLETED,
                    navController = navController
                )
            }
        }

        // 找到“查看成绩”按钮并点击
        composeTestRule.onNodeWithText("查看成绩", substring = true, ignoreCase = true)
            .assertIsDisplayed()
            .performClick()

        // 检查跳转
        composeTestRule.waitForIdle()

        val backStackEntry = navController.currentBackStackEntry
        assertEquals(Screen.ExamResult.route + "/{score}", backStackEntry?.destination?.route)
        assertEquals(completedExam.score.toString(), backStackEntry?.arguments?.getString("score"))
    }

    @Test
    fun showsEmptyState_whenNoExams() {
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        val viewModel = ExamListViewModel()
        setPrivateStateFlow(viewModel, "_exams", emptyList<Exam>())

        composeTestRule.setContent {
            ExamListScreen(navController = navController, viewModel = viewModel)
        }

        composeTestRule.onNodeWithText("没有待考考试").assertIsDisplayed()

        composeTestRule.onNodeWithText("已考").performClick()
        composeTestRule.onNodeWithText("没有已完成的考试").assertIsDisplayed()

        composeTestRule.onNodeWithText("已过期").performClick()
        composeTestRule.onNodeWithText("没有已过期的考试").assertIsDisplayed()
    }

    @Test
    fun showsExpiredExamsInTab() {
        val expiredExam = createFakeExams().first().copy(status = ExamStatus.EXPIRED)

        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        val viewModel = ExamListViewModel()
        setPrivateStateFlow(viewModel, "_exams", listOf(expiredExam))

        composeTestRule.setContent {
            ExamListScreen(navController = navController, viewModel = viewModel)
        }

        composeTestRule.onNodeWithText("已过期").performClick()
        composeTestRule.onNodeWithText("测试考试").assertExists()
    }




}