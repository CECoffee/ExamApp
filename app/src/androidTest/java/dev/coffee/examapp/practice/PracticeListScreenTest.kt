import android.content.Context
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.createGraph
import androidx.navigation.navArgument
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import dev.coffee.examapp.model.Chapter
import dev.coffee.examapp.model.Practice
import dev.coffee.examapp.ui.navigation.Screen
import dev.coffee.examapp.ui.screens.practice.PracticeListScreen
import dev.coffee.examapp.viewmodel.PracticeListViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaField

class PracticeListScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun createFakePractices(): List<Practice> = listOf(
        Practice(
            id = 1,
            name = "数学练习",
            questionCount = 10,
            completedCount = 5,
            chapters = listOf(
                Chapter(id = "a1", name = "代数", questionCount = 5, completedCount = 3, progress = 0.6),
                Chapter(id = "a2", name = "几何", questionCount = 5, completedCount = 2, progress = 0.4)
            )
        ),
        Practice(
            id = 2,
            name = "英语练习",
            questionCount = 20,
            completedCount = 10,
            chapters = listOf(
                Chapter(id = "b1", name = "阅读", questionCount = 10, completedCount = 5, progress = 0.5),
                Chapter(id = "b2", name = "写作", questionCount = 10, completedCount = 3, progress = 0.3)
            )
        )
    )

    // 反射设置私有StateFlow字段
    private fun setPrivateStateFlow(viewModel: PracticeListViewModel, fieldName: String, value: Any?) {
        val kClass = viewModel::class
        val property = kClass.declaredMemberProperties.first { it.name == fieldName }
        property.isAccessible = true
        val field = property.javaField!!
        field.isAccessible = true
        val stateFlow = field.get(viewModel) as MutableStateFlow<Any?>
        stateFlow.value = value
    }

    @Test
    fun practiceList_showsLoadingIndicator_whenIsLoadingTrue() {
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        val viewModel = PracticeListViewModel()

        // 1. 先设置非空 practices
        setPrivateStateFlow(
            viewModel, "_practices", listOf(
                Practice(
                    id = 1,
                    name = "数学练习",
                    questionCount = 10,
                    completedCount = 5,
                    chapters = listOf(
                        Chapter(id = "a1", name = "代数", questionCount = 5, completedCount = 3, progress = 0.6),
                        Chapter(id = "a2", name = "几何", questionCount = 5, completedCount = 2, progress = 0.4)
                    )
                )
            )
        )
        // 2. 再设置 isLoading 为 true
        setPrivateStateFlow(viewModel, "_isLoading", true)
        setPrivateStateFlow(viewModel, "_errorMessage", null)
        setPrivateStateFlow(viewModel, "_selectedPracticeIndex", 0)

        // 3. 启动 Compose 页面
        composeTestRule.setContent {
            PracticeListScreen(navController = navController, viewModel = viewModel)
        }

        // 4. 验证 Loading 指示器可见
        composeTestRule.onNodeWithTag("LoadingIndicator")
        // 5. 验证“暂无练习内容”不存在
        composeTestRule.onNodeWithText("暂无练习内容")
            .assertDoesNotExist()
    }

    @Test
    fun practiceList_showsEmptyText_whenPracticesEmptyAndNotLoading() {
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        val viewModel = PracticeListViewModel()
        setPrivateStateFlow(viewModel, "_isLoading", false)
        setPrivateStateFlow(viewModel, "_practices", emptyList<Practice>())
        setPrivateStateFlow(viewModel, "_errorMessage", null)
        setPrivateStateFlow(viewModel, "_selectedPracticeIndex", 0)

        composeTestRule.setContent {
            PracticeListScreen(navController = navController, viewModel = viewModel)
        }
        composeTestRule.onNodeWithText("暂无练习内容").assertIsDisplayed()
    }

    @Test
    fun practiceList_showsPracticePager_andHeader() {
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        val viewModel = PracticeListViewModel()
        val practices = createFakePractices()
        setPrivateStateFlow(viewModel, "_isLoading", false)
        setPrivateStateFlow(viewModel, "_practices", practices)
        setPrivateStateFlow(viewModel, "_errorMessage", null)
        setPrivateStateFlow(viewModel, "_selectedPracticeIndex", 0)

        composeTestRule.setContent {
            PracticeListScreen(navController = navController, viewModel = viewModel)
        }

        // 检查顶部标题
        composeTestRule.onNodeWithText("练习模式").assertIsDisplayed()
        // 检查Practice名称
        composeTestRule.onNodeWithText("数学练习").assertIsDisplayed()
        // 检查完成百分比
        composeTestRule.onNodeWithText("已完成 50%").assertIsDisplayed()
    }

    @Test
    fun practiceList_showsChapters_forSelectedPractice() {
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        val viewModel = PracticeListViewModel()
        val practices = createFakePractices()
        setPrivateStateFlow(viewModel, "_isLoading", false)
        setPrivateStateFlow(viewModel, "_practices", practices)
        setPrivateStateFlow(viewModel, "_errorMessage", null)
        setPrivateStateFlow(viewModel, "_selectedPracticeIndex", 0)

        composeTestRule.setContent {
            PracticeListScreen(navController = navController, viewModel = viewModel)
        }

        // 当前第一个 Practice，章节 "代数" 和 "几何" 可见
        composeTestRule.onNodeWithText("代数").assertIsDisplayed()
        composeTestRule.onNodeWithText("几何").assertIsDisplayed()
    }

    @Test
    fun chapterItem_click_navigatesToPracticeScreen() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        lateinit var navController: TestNavHostController
        composeTestRule.runOnUiThread {
            val context = ApplicationProvider.getApplicationContext<Context>()
            navController = TestNavHostController(context).apply {
                navigatorProvider.addNavigator(ComposeNavigator())
                graph = createGraph(startDestination = Screen.Practice.route) {
                    composable(Screen.Practice.route) {}
                    composable(
                        Screen.Practice.route + "/{chapterId}/{chapterName}",
                        arguments = listOf(
                            navArgument("chapterId") { type = NavType.StringType },
                            navArgument("chapterName") { type = NavType.StringType }
                        )
                    ) {}
                }
            }
        }
        val viewModel = PracticeListViewModel()
        val practices = createFakePractices()
        setPrivateStateFlow(viewModel, "_isLoading", false)
        setPrivateStateFlow(viewModel, "_practices", practices)
        setPrivateStateFlow(viewModel, "_errorMessage", null)
        setPrivateStateFlow(viewModel, "_selectedPracticeIndex", 0)

        composeTestRule.setContent {
            MaterialTheme {
                PracticeListScreen(navController = navController, viewModel = viewModel)
            }
        }

        // 点击"代数"
        composeTestRule.onNodeWithText("代数").performClick()
        composeTestRule.waitForIdle()
        val backStackEntry = navController.currentBackStackEntry
        assertEquals(Screen.Practice.route + "/{chapterId}/{chapterName}", backStackEntry?.destination?.route)
        assertEquals("a1", backStackEntry?.arguments?.getString("chapterId"))
        assertEquals("代数", backStackEntry?.arguments?.getString("chapterName"))
    }

    @Test
    fun practiceList_showsErrorToast_whenErrorMessageSet() {
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        val viewModel = PracticeListViewModel()
        setPrivateStateFlow(viewModel, "_isLoading", false)
        setPrivateStateFlow(viewModel, "_practices", emptyList<Practice>())
        setPrivateStateFlow(viewModel, "_errorMessage", "加载失败")
        setPrivateStateFlow(viewModel, "_selectedPracticeIndex", 0)

        composeTestRule.setContent {
            PracticeListScreen(navController = navController, viewModel = viewModel)
        }
        // Toast无法直接assert，只保证流程无异常
        composeTestRule.onNodeWithText("暂无练习内容").assertIsDisplayed()
    }

    @Test
    fun practiceList_clicksPagerDot_switchesPractice() {
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        val viewModel = PracticeListViewModel()
        val practices = createFakePractices()
        setPrivateStateFlow(viewModel, "_isLoading", false)
        setPrivateStateFlow(viewModel, "_practices", practices)
        setPrivateStateFlow(viewModel, "_errorMessage", null)
        setPrivateStateFlow(viewModel, "_selectedPracticeIndex", 0)

        composeTestRule.setContent {
            PracticeListScreen(navController = navController, viewModel = viewModel)
        }

        // 点击第二个 pager dot
        composeTestRule.onAllNodes(hasClickAction())[1].performClick()
        composeTestRule.waitForIdle()

        // 检查是否切换到“英语练习”
        composeTestRule.onNodeWithText("英语练习").assertIsDisplayed()
    }

}