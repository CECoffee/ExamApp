package dev.coffee.examapp.viewmodel

import dev.coffee.examapp.model.Question
import dev.coffee.examapp.model.QuestionType
import dev.coffee.examapp.network.ApiService
import dev.coffee.examapp.network.RetrofitClient
import dev.coffee.examapp.network.SubmitPracticeAnswerRequest
import dev.coffee.examapp.viewmodel.utils.CoroutineTestRule
import io.mockk.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import retrofit2.Response


@OptIn(ExperimentalCoroutinesApi::class)
class PracticeViewModelTest {

    @get:Rule
    val coroutineRule = CoroutineTestRule()

    private lateinit var viewModel: PracticeViewModel
    private val mockApiService: ApiService = mockk(relaxed = true)
    private val chapterId = "ch123"
    private val chapterName = "第一章"
    private val question1 = Question(
        id = 1,
        difficulty = 1,
        content = "1+1=?",
        questionType = QuestionType.SINGLE_CHOICE,
        options = listOf("1", "2", "3"),
        correctAnswer = "2",
        explanation = "基础加法"
    )
    private val question2 = Question(
        id = 2,
        difficulty = 1,
        content = "2+2=?",
        questionType = QuestionType.SINGLE_CHOICE,
        options = listOf("2", "3", "4"),
        correctAnswer = "4",
        explanation = "基础加法"
    )

    @Before
    fun setup() = runBlocking {
        // TODO IP
        val success = RetrofitClient.setBaseUrl("")
        check(success) { "Failed to set base URL!" }

        viewModel = PracticeViewModel(chapterId, chapterName, mockApiService)
    }

    @Test
    fun `initQuestion 成功应设置 nextQuestion`() = runTest {
        coEvery {
            mockApiService.submitPracticeAnswer(
                chapterId,
                SubmitPracticeAnswerRequest(null, null, null)
            )
        } returns Response.success(question1)
        viewModel = PracticeViewModel(chapterId, chapterName, mockApiService)
        // 触发init
        advanceUntilIdle()
        val field = PracticeViewModel::class.java.getDeclaredField("_nextQuestion")
        field.isAccessible = true
        val nextQ = (field.get(viewModel) as MutableStateFlow<Question?>).value
        assertEquals(question1.id, nextQ?.id)
        assertEquals(false, viewModel.isLoading.value)
    }

    @Test
    fun `initQuestion 失败时应toast提示`() = runTest {
        coEvery {
            mockApiService.submitPracticeAnswer(
                chapterId,
                SubmitPracticeAnswerRequest(null, null, null)
            )
        } returns Response.error(500, "error".toResponseBody(null))
        viewModel = PracticeViewModel(chapterId, chapterName, mockApiService)
        advanceUntilIdle()
        assertEquals("获取题目失败: 500", viewModel.showToast.value)
    }

    @Test
    fun `updateUserAnswer 能正确更新 userAnswer`() = runTest {
        viewModel.updateUserAnswer("2")
        assertEquals("2", viewModel.userAnswer.value)
    }

    @Test
    fun `submitAnswer 空答案应toast提示`() = runTest {
        viewModel.updateUserAnswer("")
        viewModel.submitAnswer()
        assertEquals("请输入答案", viewModel.showToast.value)
    }

    @Test
    fun `submitAnswer 正确答案应计数并切换下一题`() = runTest {
        // 设置当前题
        val fieldCq = PracticeViewModel::class.java.getDeclaredField("_currentQuestion")
        fieldCq.isAccessible = true
        (fieldCq.get(viewModel) as MutableStateFlow<Question?>).value = question1

        viewModel.updateUserAnswer("2")
        coEvery {
            mockApiService.submitPracticeAnswer(
                eq(chapterId),
                any()
            )
        } returns Response.success(question2)

        viewModel.submitAnswer()
        advanceUntilIdle()
        assertEquals(1, viewModel.correctCount.value)
        assertTrue(viewModel.showExplanation.value)
        // _nextQuestion 已切换为question2
        val fieldNq = PracticeViewModel::class.java.getDeclaredField("_nextQuestion")
        fieldNq.isAccessible = true
        assertEquals(2, (fieldNq.get(viewModel) as MutableStateFlow<Question?>).value?.id)
    }

    @Test
    fun `submitAnswer 错误答案不得分但切换下一题`() = runTest {
        val fieldCq = PracticeViewModel::class.java.getDeclaredField("_currentQuestion")
        fieldCq.isAccessible = true
        (fieldCq.get(viewModel) as MutableStateFlow<Question?>).value = question1

        viewModel.updateUserAnswer("1")
        coEvery {
            mockApiService.submitPracticeAnswer(
                eq(chapterId),
                any()
            )
        } returns Response.success(question2)

        viewModel.submitAnswer()
        advanceUntilIdle()
        assertEquals(0, viewModel.correctCount.value)
        assertTrue(viewModel.showExplanation.value)
        // _nextQuestion 已切换为question2
        val fieldNq = PracticeViewModel::class.java.getDeclaredField("_nextQuestion")
        fieldNq.isAccessible = true
        assertEquals(2, (fieldNq.get(viewModel) as MutableStateFlow<Question?>).value?.id)
    }

    @Test
    fun `submitAnswer 提交失败应toast提示`() = runTest {
        val fieldCq = PracticeViewModel::class.java.getDeclaredField("_currentQuestion")
        fieldCq.isAccessible = true
        (fieldCq.get(viewModel) as MutableStateFlow<Question?>).value = question1

        viewModel.updateUserAnswer("2")
        coEvery {
            mockApiService.submitPracticeAnswer(
                eq(chapterId),
                any()
            )
        } throws RuntimeException("网络异常")

        viewModel.submitAnswer()
        advanceUntilIdle()
        assertEquals("失败: 网络异常", viewModel.showToast.value)
    }

    @Test
    fun `proceedToNextQuestion 正常推进索引并加载下一题`() = runTest {
        // 模拟_nextQuestion有新题
        val fieldNq = PracticeViewModel::class.java.getDeclaredField("_nextQuestion")
        fieldNq.isAccessible = true
        (fieldNq.get(viewModel) as MutableStateFlow<Question?>).value = question2

        viewModel.proceedToNextQuestion()
        assertEquals(1, viewModel.currentQuestionIndex.value)
        // currentQuestion已被设置为nextQuestion
        assertEquals(question2.id, viewModel.currentQuestion.value?.id)
    }

    @Test
    fun `proceedToNextQuestion 无新题应toast并结束练习`() = runTest {
        // 模拟_nextQuestion.id为0，表示无新题
        val fieldNq = PracticeViewModel::class.java.getDeclaredField("_nextQuestion")
        fieldNq.isAccessible = true
        (fieldNq.get(viewModel) as MutableStateFlow<Question?>).value = question1.copy(id = 0)

        viewModel.proceedToNextQuestion()
        assertEquals("本章节题库已空", viewModel.showToast.value)
        assertTrue(viewModel.practiceFinished.value)
    }

    @Test
    fun `答题达到上限应结束练习`() = runTest {
        val fieldIdx = PracticeViewModel::class.java.getDeclaredField("_currentQuestionIndex")
        fieldIdx.isAccessible = true
        (fieldIdx.get(viewModel) as MutableStateFlow<Int>).value = viewModel.totalQuestions

        viewModel.loadNextQuestion()
        assertTrue(viewModel.practiceFinished.value)
    }

    @Test
    fun `clearToast 能清除toast信息`() = runTest {
        val field = PracticeViewModel::class.java.getDeclaredField("_showToast")
        field.isAccessible = true
        (field.get(viewModel) as MutableStateFlow<String?>).value = "错误"
        viewModel.clearToast()
        assertNull(viewModel.showToast.value)
    }
}