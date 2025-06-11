package dev.coffee.examapp

import android.view.View
import android.webkit.WebView
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.espresso.Espresso
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TotalInstrumentedTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun runTotalFlow() {
        serverConfigStep()
        showCompletedExams()
        showExpiredExams()
        showPendingExams()
        doExam()
        showStatistic()
        showPractices()
        slidePractices()
        doPractice()
        showWrongQuestions()
        showWrongQuestionDetail()
        doSimilarQuestions()
        delWrongQuestion()
        loadMoreWrongQuestions()
    }

    private fun serverConfigStep() {
        // TODO IP
        composeTestRule.onNode(hasText("服务器地址")).performTextInput("")
        composeTestRule.onNode(hasContentDescription("连接服务器")).performClick()
    }

    private fun showCompletedExams() {
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("已考").performTouchInput { swipeLeft() }
        composeTestRule.onAllNodesWithText("查看成绩")[0].performClick()
        composeTestRule.onNodeWithText("返回首页").performClick()
    }

    private fun showExpiredExams() {
        composeTestRule.onNodeWithText("已过期").performClick()
    }

    private fun showPendingExams() {
        composeTestRule.onNodeWithText("待考").performClick()
    }

    private fun doExam(){
        composeTestRule.waitForIdle()
        composeTestRule.onAllNodesWithText("开始考试")[0].performClick()

        while (true) {
            // 等待编辑框
            composeTestRule.waitUntil(timeoutMillis = 15000) {
                val webView = composeTestRule.activity.findViewById<WebView>(R.id.math_editor)
                webView?.tag == "math_editor"
            }
            val submitButton = composeTestRule.onAllNodesWithText("下一题")
            if (submitButton.fetchSemanticsNodes().isEmpty()) {
                break
            }
            // 点击富文本编辑框
            composeTestRule.activityRule.scenario.onActivity { activity ->
                val webView = activity.findViewById<WebView>(R.id.math_editor)
                webView.performClick()
            }

            // 等待键盘加载完成
            composeTestRule.waitUntil(timeoutMillis = 15000) {
                try {
                    var tag: Any? = null
                    Espresso.onView(withId(R.id.math_keyboard)).perform(object : ViewAction {
                        override fun getConstraints() = isAssignableFrom(WebView::class.java)
                        override fun getDescription() = "Read WebView tag"
                        override fun perform(uiController: UiController?, view: View?) {
                            tag = view?.tag
                        }
                    })
                    tag == "math_keyboard"
                } catch (e: Exception) {
                    false
                }
            }

            // e^x
            // 点击 ^
            performPointerClickOnXPathInCompose(R.id.math_keyboard, "/html/body/div/div/div/div[1]/div[2]/div[2]/div[10]")
            composeTestRule.waitForIdle()
            // 点击 e
            performPointerClickOnXPathInCompose(R.id.math_keyboard, "/html/body/div/div/div/div[1]/div[2]/div[1]/div[9]")
            composeTestRule.waitForIdle()
            // 点击 >
            performPointerClickOnXPathInCompose(R.id.math_keyboard, "/html/body/div/div/div/div[1]/div[2]/div[4]/div[9]")
            composeTestRule.waitForIdle()
            // 点击 x
            performPointerClickOnXPathInCompose(R.id.math_keyboard, "/html/body/div/div/div/div[1]/div[2]/div[1]/div[1]")
            composeTestRule.waitForIdle()


            Espresso.pressBack()
            composeTestRule.waitForIdle()

            submitButton[0].performClick()
            composeTestRule.waitForIdle() // 等待动画/更新完成
        }

        composeTestRule.onNodeWithText("提交").performClick()
        composeTestRule .waitForIdle()

        composeTestRule.onNodeWithText("返回首页").performClick()
    }

    private fun showStatistic() {
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription("分析", useUnmergedTree = true).performClick()
    }

    private fun showPractices() {
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription("练习", useUnmergedTree = true).performClick()
    }

    private fun slidePractices() {
        composeTestRule.waitForIdle()
        composeTestRule.onAllNodesWithTag("practice_dot")[1].performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("practice_name").performTouchInput { swipeRight() }
    }

    private fun doPractice() {
        composeTestRule.waitForIdle()
        composeTestRule.onAllNodesWithTag("chapter_item")[0].performClick()

        while (true) {
            // 等待编辑框
            composeTestRule.waitUntil(timeoutMillis = 15000) {
                val webView = composeTestRule.activity.findViewById<WebView>(R.id.math_editor)
                webView?.tag == "math_editor"
            }
            // 点击富文本编辑框
            composeTestRule.activityRule.scenario.onActivity { activity ->
                val webView = activity.findViewById<WebView>(R.id.math_editor)
                webView.performClick()
            }

            // 等待键盘加载完成
            composeTestRule.waitUntil(timeoutMillis = 15000) {
                try {
                    var tag: Any? = null
                    Espresso.onView(withId(R.id.math_keyboard)).perform(object : ViewAction {
                        override fun getConstraints() = isAssignableFrom(WebView::class.java)
                        override fun getDescription() = "Read WebView tag"
                        override fun perform(uiController: UiController?, view: View?) {
                            tag = view?.tag
                        }
                    })
                    tag == "math_keyboard"
                } catch (e: Exception) {
                    false
                }
            }

            // e^x
            // 点击 ^
            performPointerClickOnXPathInCompose(R.id.math_keyboard, "/html/body/div/div/div/div[1]/div[2]/div[2]/div[10]")
            composeTestRule.waitForIdle()
            // 点击 e
            performPointerClickOnXPathInCompose(R.id.math_keyboard, "/html/body/div/div/div/div[1]/div[2]/div[1]/div[9]")
            composeTestRule.waitForIdle()
            // 点击 >
            performPointerClickOnXPathInCompose(R.id.math_keyboard, "/html/body/div/div/div/div[1]/div[2]/div[4]/div[9]")
            composeTestRule.waitForIdle()
            // 点击 x
            performPointerClickOnXPathInCompose(R.id.math_keyboard, "/html/body/div/div/div/div[1]/div[2]/div[1]/div[1]")
            composeTestRule.waitForIdle()

            Espresso.pressBack()
            composeTestRule.waitForIdle()

            val submitButton = composeTestRule.onNodeWithText("提交答案")
            submitButton.performClick()
            composeTestRule.waitUntil(timeoutMillis = 15000) {
                composeTestRule.onAllNodesWithText("解析:").fetchSemanticsNodes().isNotEmpty()
            }

            val completeButton = composeTestRule.onAllNodesWithText("完成练习")
            if (completeButton.fetchSemanticsNodes().isNotEmpty()) {
                completeButton[0].performClick()
                break
            }
            composeTestRule.onNodeWithText("下一题").performClick()
        }
        composeTestRule .waitForIdle()
        composeTestRule.onNodeWithText("返回").performClick()
    }

    private fun showWrongQuestions() {
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription("错题", useUnmergedTree = true).performClick()
    }

    private fun showWrongQuestionDetail() {
        val showDetailButtons = composeTestRule.onAllNodesWithText("查看解析")
        composeTestRule.waitUntil(timeoutMillis = 15000) {
            showDetailButtons.fetchSemanticsNodes().isNotEmpty()
        }
        showDetailButtons[0].performClick()
    }

    private fun doSimilarQuestions() {
        composeTestRule.waitUntil(timeoutMillis = 15000) {
            composeTestRule.onAllNodesWithText("解析:").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("AI同类错题推荐").performClick()

        while (true) {
            // 等待编辑框
            composeTestRule.waitUntil(timeoutMillis = 25000) {
                val webView = composeTestRule.activity.findViewById<WebView>(R.id.math_editor)
                webView?.tag == "math_editor"
            }
            // 点击富文本编辑框
            composeTestRule.activityRule.scenario.onActivity { activity ->
                val webView = activity.findViewById<WebView>(R.id.math_editor)
                webView.performClick()
            }

            // 等待键盘加载完成
            composeTestRule.waitUntil(timeoutMillis = 15000) {
                try {
                    var tag: Any? = null
                    Espresso.onView(withId(R.id.math_keyboard)).perform(object : ViewAction {
                        override fun getConstraints() = isAssignableFrom(WebView::class.java)
                        override fun getDescription() = "Read WebView tag"
                        override fun perform(uiController: UiController?, view: View?) {
                            tag = view?.tag
                        }
                    })
                    tag == "math_keyboard"
                } catch (e: Exception) {
                    false
                }
            }

            // e^x
            // 点击 ^
            performPointerClickOnXPathInCompose(R.id.math_keyboard, "/html/body/div/div/div/div[1]/div[2]/div[2]/div[10]")
            composeTestRule.waitForIdle()
            // 点击 e
            performPointerClickOnXPathInCompose(R.id.math_keyboard, "/html/body/div/div/div/div[1]/div[2]/div[1]/div[9]")
            composeTestRule.waitForIdle()
            // 点击 >
            performPointerClickOnXPathInCompose(R.id.math_keyboard, "/html/body/div/div/div/div[1]/div[2]/div[4]/div[9]")
            composeTestRule.waitForIdle()
            // 点击 x
            performPointerClickOnXPathInCompose(R.id.math_keyboard, "/html/body/div/div/div/div[1]/div[2]/div[1]/div[1]")
            composeTestRule.waitForIdle()

            Espresso.pressBack()
            composeTestRule.waitForIdle()

            val submitButton = composeTestRule.onNodeWithText("提交答案")
            submitButton.performClick()
            composeTestRule.waitUntil(timeoutMillis = 15000) {
                composeTestRule.onAllNodesWithText("解析:").fetchSemanticsNodes().isNotEmpty()
            }

            val completeButton = composeTestRule.onAllNodesWithText("完成")
            if (completeButton.fetchSemanticsNodes().isNotEmpty()) {
                completeButton[0].performClick()
                break
            }
            composeTestRule.onNodeWithText("下一题").performClick()
        }
        composeTestRule .waitForIdle()
        composeTestRule.onNodeWithText("返回").performClick()
    }

    private fun delWrongQuestion() {
        val showDetailButtons = composeTestRule.onAllNodesWithText("查看解析")
        composeTestRule.waitUntil(timeoutMillis = 15000) {
            showDetailButtons.fetchSemanticsNodes().isNotEmpty()
        }
        showDetailButtons[0].performTouchInput { swipeLeft() }

        composeTestRule.waitForIdle()
        composeTestRule.onAllNodesWithContentDescription("删除")[0].performClick()
    }

    private fun loadMoreWrongQuestions() {
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().performTouchInput { swipeUp(1000f, 0f) }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("加载更多").performClick()
    }


    private fun performPointerClickOnXPathInCompose(webViewId: Int, xpath: String) {
        Espresso.onView(withId(webViewId)).perform(object : ViewAction {
            override fun getConstraints() = isAssignableFrom(WebView::class.java)

            override fun getDescription() = "Inject JS to perform pointerdown + pointerup on element with XPath"

            override fun perform(uiController: UiController?, view: View?) {
                val js = """
                    (function(){
                        function getElementByXpath(path) {
                            return document.evaluate(path, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;
                        }
                        const el = getElementByXpath("$xpath");
                        if (el) {
                            const downEvt = new PointerEvent('pointerdown', {
                                bubbles: true,
                                cancelable: true
                            });
                            el.dispatchEvent(downEvt);
                            setTimeout(() => {
                                const upEvt = new PointerEvent('pointerup', {
                                    bubbles: true,
                                    cancelable: true
                                });
                                el.dispatchEvent(upEvt);
                            }, 50);
                        }
                    })();
                """.trimIndent()

                (view as? WebView)?.evaluateJavascript(js, null)

                uiController?.loopMainThreadForAtLeast(600)
            }
        })
    }
}