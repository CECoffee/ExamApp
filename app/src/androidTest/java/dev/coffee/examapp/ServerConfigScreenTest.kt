package dev.coffee.examapp


import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.createGraph
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.coffee.examapp.ui.navigation.Screen
import dev.coffee.examapp.ui.screens.ServerConfigScreen
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ServerConfigScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun serverConfigScreen_displayElements_correctly() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            ServerConfigScreen(navController = navController)
        }

        // 检查 TextField 和 按钮是否显示
        composeTestRule.onNode(hasText("服务器地址")).assertIsDisplayed()
        composeTestRule.onNode(hasContentDescription("连接服务器")).assertIsDisplayed()
    }

    @Test
    fun serverConfigScreen_inputAddress_andClickButton() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            ServerConfigScreen(navController = navController)
        }

        val testAddress = "http://example.com"

        // 输入服务器地址
        composeTestRule.onNode(hasText("服务器地址")).performTextInput(testAddress)

        // 点击连接按钮
        composeTestRule.onNode(hasContentDescription("连接服务器")).performClick()

        // 根据 isLoading 状态检查是否显示了 loading 指示器（需要状态可以观察）
        // composeTestRule.onNode(isInstanceOf(CircularProgressIndicator::class.java)).assertExists()
    }

    @Test
    fun serverConfigScreen_navigatesToExamList_onSuccess() {
        lateinit var navController: TestNavHostController

        composeTestRule.setContent {
            navController = TestNavHostController(ApplicationProvider.getApplicationContext()).apply {
                navigatorProvider.addNavigator(ComposeNavigator())
                graph = createGraph(startDestination = Screen.ServerConfig.route) {
                    composable(Screen.ServerConfig.route) {  }
                    composable(Screen.ExamList.route) {  }
                }
            }
            ServerConfigScreen(
                navController = navController
            )
        }

        val testAddress = "http://47.123.2.211:8080"
        composeTestRule.onNode(hasText("服务器地址")).performTextInput(testAddress)
        composeTestRule.onNode(hasContentDescription("连接服务器")).performClick()

        composeTestRule.waitForIdle()

        assertEquals("exam_list", navController.currentBackStackEntry?.destination?.route)
    }
}
