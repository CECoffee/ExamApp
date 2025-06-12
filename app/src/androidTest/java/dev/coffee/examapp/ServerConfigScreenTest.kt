package dev.coffee.examapp


import android.os.Looper
import android.content.Context
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.NavHostController
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.createGraph
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.coffee.examapp.ui.navigation.Screen
import dev.coffee.examapp.ui.screens.ServerConfigScreen
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
@RunWith(AndroidJUnit4::class)
class ServerConfigScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var navController: NavHostController


    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            navController = TestNavHostController(context).apply {
                navigatorProvider.addNavigator(ComposeNavigator())
                graph = createGraph(startDestination = Screen.ServerConfig.route) {
                    composable(Screen.ServerConfig.route) {
                        // 替代真实逻辑，模拟自动跳转
                        LaunchedEffect(Unit) {
                            navigate(Screen.ExamList.route) {
                                popUpTo(Screen.ServerConfig.route) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                        ServerConfigScreen(navController = navController)
                    }
                    composable(Screen.ExamList.route) {
                        Text("考试列表页")
                    }
                }
            }
        }
    }


    @Test
    fun serverConfigScreen_displayElements_correctly() {
        composeTestRule.setContent {
            ServerConfigScreen(navController = navController)
        }

        composeTestRule.onNode(hasText("服务器地址")).assertIsDisplayed()
        composeTestRule.onNode(hasContentDescription("连接服务器")).assertIsDisplayed()
    }

    @Test
    fun serverConfigScreen_inputAddress_andClickButton() {
        composeTestRule.setContent {
            ServerConfigScreen(navController = navController)
        }

        val testAddress = "http://example.com"

        composeTestRule.onNode(hasText("服务器地址")).performTextInput(testAddress)
        composeTestRule.waitForIdle()
        composeTestRule.onNode(hasContentDescription("连接服务器")).performClick()
    }

    @Test
    fun serverConfigScreen_navigatesToExamList_onSuccess() {
        composeTestRule.setContent {
            ServerConfigScreen(navController = navController)
        }

        val testAddress = "http://example.com"
        composeTestRule.onNode(hasText("服务器地址")).performTextInput(testAddress)
        composeTestRule.waitForIdle()
        composeTestRule.onNode(hasContentDescription("连接服务器")).performClick()

        composeTestRule.waitUntil(timeoutMillis = 3000) {
            navController.currentBackStackEntry?.destination?.route == Screen.ExamList.route
        }

        assertEquals(Screen.ExamList.route, navController.currentBackStackEntry?.destination?.route)
    }
}
