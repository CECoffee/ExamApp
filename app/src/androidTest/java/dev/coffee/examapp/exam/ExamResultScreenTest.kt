package dev.coffee.examapp.exam

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dev.coffee.examapp.ui.screens.exam.ExamResultScreen
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ExamResultScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun examResultScreen_displaysScoreAndMessage_Excellent() {
        val testScore = "95"
        var onBackClicked = false

        composeTestRule.setContent {
            MaterialTheme {
                ExamResultScreen(
                    scoreString = testScore,
                    onBack = { onBackClicked = true }
                )
            }
        }

        // 检查主标题、副标题、分数文本、正确信息
        composeTestRule.onNodeWithText("考试结束").assertIsDisplayed()
        composeTestRule.onNodeWithText("您的最终成绩").assertIsDisplayed()
        composeTestRule.onNodeWithText("95分").assertIsDisplayed()
        composeTestRule.onNodeWithText("优秀！").assertIsDisplayed()
        // 检查返回首页按钮
        composeTestRule.onNodeWithText("返回首页").assertIsDisplayed().performClick()
        assertTrue(onBackClicked)
    }

    @Test
    fun examResultScreen_displaysScoreAndMessage_Good() {
        val testScore = "85"
        composeTestRule.setContent {
            MaterialTheme {
                ExamResultScreen(
                    scoreString = testScore,
                    onBack = {}
                )
            }
        }
        composeTestRule.onNodeWithText("85分").assertIsDisplayed()
        composeTestRule.onNodeWithText("良好！").assertIsDisplayed()
    }

    @Test
    fun examResultScreen_displaysScoreAndMessage_Pass() {
        val testScore = "60"
        composeTestRule.setContent {
            MaterialTheme {
                ExamResultScreen(
                    scoreString = testScore,
                    onBack = {}
                )
            }
        }
        composeTestRule.onNodeWithText("60分").assertIsDisplayed()
        composeTestRule.onNodeWithText("及格！").assertIsDisplayed()
    }

    @Test
    fun examResultScreen_displaysScoreAndMessage_Fail() {
        val testScore = "50"
        composeTestRule.setContent {
            MaterialTheme {
                ExamResultScreen(
                    scoreString = testScore,
                    onBack = {}
                )
            }
        }
        composeTestRule.onNodeWithText("50分").assertIsDisplayed()
        composeTestRule.onNodeWithText("再接再厉！").assertIsDisplayed()
    }
}