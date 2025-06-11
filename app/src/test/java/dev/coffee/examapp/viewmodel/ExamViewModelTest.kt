package dev.coffee.examapp.viewmodel

import io.mockk.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.*
import retrofit2.Response
import android.content.Context
import dev.coffee.examapp.model.Question
import dev.coffee.examapp.model.QuestionType
import dev.coffee.examapp.network.ApiService
import dev.coffee.examapp.network.RetrofitClient
import dev.coffee.examapp.viewmodel.utils.CoroutineTestRule
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ExamViewModelTest {

    @get:Rule
    val coroutineRule = CoroutineTestRule()

    private lateinit var viewModel: ExamViewModel
    private val mockApiService: ApiService = mockk(relaxed = true)
    private val mockContext: Context = mockk(relaxed = true)
    private val questionIds = listOf(101, 102)
    private val examId = 1

    private val question1 = Question(
        id = 101,
        difficulty = 1,
        content = "1+1=?",
        questionType = QuestionType.FILL_IN_THE_BLANK,
        correctAnswer = "2",
        explanation = "基础加法"
    )
    private val question2 = Question(
        id = 102,
        difficulty = 1,
        content = "2+2=?",
        questionType = QuestionType.FILL_IN_THE_BLANK,
        correctAnswer = "4",
        explanation = "基础加法"
    )

    @Before
    fun setup() = runBlocking {
        val success = RetrofitClient.setBaseUrl("http://47.123.2.211:8080/")
        check(success) { "Failed to set base URL!" }
        // 注入mockApiService
        viewModel = ExamViewModel(examId, 60, questionIds, mockApiService)
    }

    @Test
    fun `初始化应加载第一题并启动倒计时`() = runTest {
        coEvery { mockApiService.getQuestion(101) } returns Response.success(question1)
        // 重新初始化触发init
        val viewModel = ExamViewModel(examId, 60, questionIds, mockApiService)
        assertEquals(question1.id, viewModel.currentQuestion.value?.id)
        assertEquals(60, viewModel.remainingTime.value)
        assertEquals(false, viewModel.isLoading.value)
    }

    @Test
    fun `加载题目失败应设置错误信息`() = runTest {
        coEvery { mockApiService.getQuestion(101) } returns Response.error(404, "Not found".toResponseBody(null))
        // 重新初始化触发init
        val viewModel = ExamViewModel(examId, 60, listOf(101), mockApiService)
        advanceUntilIdle()
        assertEquals("加载题目失败: 404", viewModel.errorMessage.value)
        assertEquals(false, viewModel.isLoading.value)
    }

    @Test
    fun `updateAnswer 应更新 userAnswer 和 currentQuestion 的 myAnswer`() = runTest {
        coEvery { mockApiService.getQuestion(101) } returns Response.success(question1)
        val viewModel = ExamViewModel(examId, 60, listOf(101)).apply {
            val field = ExamViewModel::class.java.getDeclaredField("apiService")
            field.isAccessible = true
            field.set(this, mockApiService)
        }
        advanceUntilIdle()
        viewModel.updateAnswer("2")
        assertEquals("2", viewModel.userAnswer.value)
    }

    @Test
    fun `navigateToNext 应提交答案并切换下一题`() = runTest {
        coEvery { mockApiService.getQuestion(101) } returns Response.success(question1)
        coEvery { mockApiService.getQuestion(102) } returns Response.success(question2)
        coEvery { mockApiService.submitExamAnswer(any(), any()) } returns Response.success(Unit)

        val viewModel = ExamViewModel(examId, 60, questionIds).apply {
            val field = ExamViewModel::class.java.getDeclaredField("apiService")
            field.isAccessible = true
            field.set(this, mockApiService)
        }
        advanceUntilIdle()
        viewModel.updateAnswer("2")
        viewModel.navigateToNext()
        advanceUntilIdle()
        assertEquals(1, viewModel.currentQuestionIndex.value)
        assertEquals(102, viewModel.currentQuestion.value?.id)
    }

    @Test
    fun `navigateToNext 到最后一题应结束考试`() = runTest {
        coEvery { mockApiService.getQuestion(101) } returns Response.success(question1)
        coEvery { mockApiService.submitExamAnswer(any(), any()) } returns Response.success(Unit)
        coEvery { mockApiService.submitExam(any(), any()) } returns Response.success(Unit)

        val viewModel = ExamViewModel(examId, 60, listOf(101)).apply {
            val field = ExamViewModel::class.java.getDeclaredField("apiService")
            field.isAccessible = true
            field.set(this, mockApiService)
        }
        advanceUntilIdle()
        viewModel.updateAnswer("2")
        viewModel.navigateToNext()
        advanceUntilIdle()
        assertTrue(viewModel.examFinished.value)
    }

    @Test
    fun `navigateToPrevious 应提交答案并切换回前一题`() = runTest {
        coEvery { mockApiService.getQuestion(101) } returns Response.success(question1)
        coEvery { mockApiService.getQuestion(102) } returns Response.success(question2)
        coEvery { mockApiService.submitExamAnswer(any(), any()) } returns Response.success(Unit)

        val viewModel = ExamViewModel(examId, 60, questionIds).apply {
            val field = ExamViewModel::class.java.getDeclaredField("apiService")
            field.isAccessible = true
            field.set(this, mockApiService)
        }
        advanceUntilIdle()
        // 先到第二题
        viewModel.updateAnswer("2")
        viewModel.navigateToNext()
        advanceUntilIdle()
        // 回到第一题
        viewModel.updateAnswer("4")
        viewModel.navigateToPrevious()
        advanceUntilIdle()
        assertEquals(0, viewModel.currentQuestionIndex.value)
        assertEquals(101, viewModel.currentQuestion.value?.id)
    }

    @Test
    fun `submitCurrentAnswer 答案正确应累计分数`() = runTest {
        coEvery { mockApiService.getQuestion(101) } returns Response.success(question1)
        coEvery { mockApiService.submitExamAnswer(any(), any()) } returns Response.success(Unit)

        val viewModel = ExamViewModel(examId, 60, listOf(101), mockApiService)
        viewModel.updateAnswer("2")
        viewModel.navigateToNext()
        advanceUntilIdle()
        assertEquals(100.0, viewModel.score.value, 0.0)
    }

    @Test
    fun `submitCurrentAnswer 答案错误不得分`() = runTest {
        coEvery { mockApiService.getQuestion(101) } returns Response.success(question1)
        coEvery { mockApiService.submitExamAnswer(any(), any()) } returns Response.success(Unit)

        val viewModel = ExamViewModel(examId, 60, listOf(101)).apply {
            val field = ExamViewModel::class.java.getDeclaredField("apiService")
            field.isAccessible = true
            field.set(this, mockApiService)
        }
        advanceUntilIdle()
        viewModel.updateAnswer("1")
        viewModel.navigateToNext()
        advanceUntilIdle()
        assertEquals(0.0, viewModel.score.value, 0.0)
    }

    @Test
    fun `clearToast 能清除toast信息`() {
        val field = ExamViewModel::class.java.getDeclaredField("_showToast")
        field.isAccessible = true
        (field.get(viewModel) as MutableStateFlow<String?>).value = "错误"
        viewModel.clearToast()
        assertNull(viewModel.showToast.value)
    }

    @Test
    fun `倒计时归零自动交卷`() = runTest {
        coEvery { mockApiService.getQuestion(101) } returns Response.success(question1)
        coEvery { mockApiService.submitExam(any(), any()) } returns Response.success(Unit)

        val viewModel = ExamViewModel(examId, 1, listOf(101)).apply {
            val field = ExamViewModel::class.java.getDeclaredField("apiService")
            field.isAccessible = true
            field.set(this, mockApiService)
        }
        advanceTimeBy(2000)
        advanceUntilIdle()
        assertTrue(viewModel.examFinished.value)
    }

    @Test
    fun `提交考试失败应toast提示`() = runTest {
        coEvery { mockApiService.getQuestion(101) } returns Response.success(question1)
        coEvery { mockApiService.submitExam(any(), any()) } throws RuntimeException("网络中断")

        val viewModel = ExamViewModel(examId, 60, listOf(101)).apply {
            val field = ExamViewModel::class.java.getDeclaredField("apiService")
            field.isAccessible = true
            field.set(this, mockApiService)
        }
        advanceUntilIdle()
        viewModel.finishExam()
        advanceUntilIdle()
        assertEquals("考试提交失败: 网络中断", viewModel.showToast.value)
    }
}