package dev.coffee.examapp.viewmodel

import dev.coffee.examapp.model.Question
import dev.coffee.examapp.model.QuestionType
import dev.coffee.examapp.network.ApiService
import dev.coffee.examapp.network.RetrofitClient
import dev.coffee.examapp.viewmodel.utils.CoroutineTestRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class SimilarQuestionsViewModelTest {

    @get:Rule
    val coroutineRule = CoroutineTestRule()

    private lateinit var viewModel: SimilarQuestionsViewModel
    private val mockApiService: ApiService = mockk(relaxed = true)
    private val testQuestions = listOf(
        Question(
            id = 101,
            difficulty = 1,
            content = "1+1等于几？",
            questionType = QuestionType.FILL_IN_THE_BLANK,
            correctAnswer = "2",
            explanation = "加法基础"
        ),
        Question(
            id = 102,
            difficulty = 2,
            content = "2+2等于几？",
            questionType = QuestionType.FILL_IN_THE_BLANK,
            correctAnswer = "4",
            explanation = "加法基础"
        )
    )

    @Before
    fun setup() = runBlocking {
        // TODO IP
        val success = RetrofitClient.setBaseUrl("http://47.123.2.211:8080")
        check(success) { "Failed to set base URL!" }

        // 通过反射注入 mockApiService
        viewModel = SimilarQuestionsViewModel(listOf(101, 102), mockApiService)
    }

    @Test
    fun `初始化时应加载第一题`() = runTest {
        coEvery { mockApiService.getQuestion(101) } returns Response.success(testQuestions[0])

        viewModel = SimilarQuestionsViewModel(listOf(101, 102), mockApiService)

        advanceUntilIdle()
        val current = viewModel.currentQuestion.value
        assertEquals(101, current?.id)
        assertEquals("1+1等于几？", current?.content)
        assertEquals(false, viewModel.isLoading.value)
        assertNull(viewModel.errorMessage.value)
    }

    @Test
    fun `加载题目失败时应设置错误信息`() = runTest {
        coEvery { mockApiService.getQuestion(101) } returns Response.error(404, "Not found".toResponseBody(null))
        val viewModel = SimilarQuestionsViewModel(listOf(101), mockApiService)
        advanceUntilIdle()
        assertEquals("加载题目失败: 404", viewModel.errorMessage.value)
        assertEquals(false, viewModel.isLoading.value)
    }

    @Test
    fun `updateAnswer 应更新 userAnswer 及 currentQuestion 的 myAnswer`() = runTest {
        coEvery { mockApiService.getQuestion(101) } returns Response.success(testQuestions[0])
        val viewModel = SimilarQuestionsViewModel(listOf(101), mockApiService)
        advanceUntilIdle()
        viewModel.updateAnswer("2")
        assertEquals("2", viewModel.userAnswer.value)
        assertEquals("2", viewModel.currentQuestion.value?.myAnswer)
    }

    @Test
    fun `submitCurrentAnswer 答案正确应正确计数并显示解析`() = runTest {
        coEvery { mockApiService.getQuestion(101) } returns Response.success(testQuestions[0])
        viewModel = SimilarQuestionsViewModel(listOf(101), mockApiService)
        advanceUntilIdle()
        viewModel.updateAnswer("2")
        viewModel.submitCurrentAnswer()
        assertEquals(1, viewModel.correctCount.value)
        assertEquals(true, viewModel.showExplanation.value)
    }

    @Test
    fun `submitCurrentAnswer 答案错误不计数但显示解析`() = runTest {
        coEvery { mockApiService.getQuestion(101) } returns Response.success(testQuestions[0])
        viewModel = SimilarQuestionsViewModel(listOf(101)).apply {
            val field = SimilarQuestionsViewModel::class.java.getDeclaredField("apiService")
            field.isAccessible = true
            field.set(this, mockApiService)
        }
        advanceUntilIdle()
        viewModel.updateAnswer("1")
        viewModel.submitCurrentAnswer()
        assertEquals(0, viewModel.correctCount.value)
        assertEquals(true, viewModel.showExplanation.value)
    }

    @Test
    fun `loadNextQuestion 可正常切换题目并重置 userAnswer`() = runTest {
        coEvery { mockApiService.getQuestion(101) } returns Response.success(testQuestions[0])
        coEvery { mockApiService.getQuestion(102) } returns Response.success(testQuestions[1])
        viewModel = SimilarQuestionsViewModel(listOf(101, 102)).apply {
            val field = SimilarQuestionsViewModel::class.java.getDeclaredField("apiService")
            field.isAccessible = true
            field.set(this, mockApiService)
        }
        advanceUntilIdle()
        viewModel.updateAnswer("2")
        viewModel.loadNextQuestion()
        advanceUntilIdle()
        assertEquals(102, viewModel.currentQuestion.value?.id)
        assertEquals("", viewModel.userAnswer.value)
        assertEquals(false, viewModel.showExplanation.value)
    }

    @Test
    fun `loadNextQuestion 到最后一题后应完成练习`() = runTest {
        coEvery { mockApiService.getQuestion(101) } returns Response.success(testQuestions[0])
        coEvery { mockApiService.getQuestion(102) } returns Response.success(testQuestions[1])
        viewModel = SimilarQuestionsViewModel(listOf(101, 102)).apply {
            val field = SimilarQuestionsViewModel::class.java.getDeclaredField("apiService")
            field.isAccessible = true
            field.set(this, mockApiService)
        }
        advanceUntilIdle()
        viewModel.loadNextQuestion()
        advanceUntilIdle()
        viewModel.loadNextQuestion()
        advanceUntilIdle()
        assertEquals(true, viewModel.practiceFinished.value)
    }

    @Test
    fun `clearToast 应清除错误信息`() = runTest {
        val field = SimilarQuestionsViewModel::class.java.getDeclaredField("_errorMessage")
        field.isAccessible = true
        (field.get(viewModel) as MutableStateFlow<String?>).value = "错误"
        viewModel.clearToast()
        assertNull(viewModel.errorMessage.value)
    }

    @Test
    fun `finishPractice 应设置练习完成`() = runTest {
        viewModel.finishPractice()
        assertEquals(true, viewModel.practiceFinished.value)
    }
}