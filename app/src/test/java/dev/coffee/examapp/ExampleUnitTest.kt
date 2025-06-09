package dev.coffee.examapp

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dev.coffee.examapp.network.RetrofitClient
import dev.coffee.examapp.viewmodel.ExamViewModel
import kotlinx.coroutines.launch
import org.junit.Test

import org.junit.Assert.*
import org.junit.Rule

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testExamFinished() {
        composeTestRule.setContent {

            val viewModel: ExamViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return ExamViewModel(2, 10, listOf(2,3,4)) as T
                    }
                }
            )
            val examFinished by viewModel.examFinished.collectAsState()
            viewModel.viewModelScope.launch {
                RetrofitClient.setBaseUrl("http://47.123.2.211:8080/")
                viewModel.finishExam()
                assertEquals(true, examFinished)
            }

        }
    }
}