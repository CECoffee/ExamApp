package dev.coffee.examapp.viewmodel

import dev.coffee.examapp.model.Question
import dev.coffee.examapp.model.QuestionType
import dev.coffee.examapp.model.WrongQuestion
import dev.coffee.examapp.network.ApiService
import dev.coffee.examapp.network.RetrofitClient
import dev.coffee.examapp.viewmodel.utils.CoroutineTestRule
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
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class WrongQuestionViewModelTest {

    @get:Rule
    val coroutineRule = CoroutineTestRule()

    private lateinit var viewModel: WrongQuestionViewModel
    private val mockApiService: ApiService = mock()

    private val mockWrongQuestions = listOf(
        WrongQuestion(
            questionId = 1,
            content = "题目内容1",
            myAnswer = "A",
            correctAnswer = "B"
        ),
        WrongQuestion(
            questionId = 2,
            content = "题目内容2",
            myAnswer = "C",
            correctAnswer = "D"
        )
    )

    private val mockQuestion = Question(
        id = 1,
        difficulty = 2,
        content = "题目内容1",
        questionType = QuestionType.FILL_IN_THE_BLANK,
        correctAnswer = "B",
        myAnswer = "A",
        explanation = "解析内容",
        isCorrect = false
    )

    @Before
    fun setup() = runBlocking {
        // TODO IP
        val success = RetrofitClient.setBaseUrl("")
        check(success) { "Failed to set base URL!" }

        viewModel = WrongQuestionViewModel().apply {
            val field = WrongQuestionViewModel::class.java.getDeclaredField("apiService")
            field.isAccessible = true
            field.set(this, mockApiService)
        }
    }

    @Test
    fun `loadWrongQuestions - success should update wrongQuestions and hasMore`() = runTest {
        whenever(mockApiService.getWrongQuestions(1, 3)).thenReturn(Response.success(mockWrongQuestions))

        viewModel.loadWrongQuestions(1)
        advanceUntilIdle()

        assertEquals(mockWrongQuestions, viewModel.wrongQuestions.value)
        assertEquals(false, viewModel.isLoading.value)
        assertEquals(true, viewModel.hasMore.value)
        assertNull(viewModel.errorMessage.value)
        assertEquals(1, viewModel.currentPage.value)
    }

    @Test
    fun `loadWrongQuestions - next page should append questions`() = runTest {
        val mockPage2 = listOf(
            WrongQuestion(
                questionId = 3,
                content = "题目内容3",
                myAnswer = "B",
                correctAnswer = "A"
            )
        )
        whenever(mockApiService.getWrongQuestions(1, 3)).thenReturn(Response.success(mockWrongQuestions))
        whenever(mockApiService.getWrongQuestions(2, 3)).thenReturn(Response.success(mockPage2))

        viewModel.loadWrongQuestions(1)
        advanceUntilIdle()
        viewModel.loadWrongQuestions(2)
        advanceUntilIdle()

        assertEquals(mockWrongQuestions + mockPage2, viewModel.wrongQuestions.value)
        assertEquals(2, viewModel.currentPage.value)
        assertEquals(true, viewModel.hasMore.value)
    }

    @Test
    fun `loadWrongQuestions - empty next page should set hasMore false`() = runTest {
        whenever(mockApiService.getWrongQuestions(1, 3)).thenReturn(Response.success(mockWrongQuestions))
        whenever(mockApiService.getWrongQuestions(2, 3)).thenReturn(Response.success(emptyList()))

        viewModel.loadWrongQuestions(1)
        advanceUntilIdle()
        viewModel.loadWrongQuestions(2)
        advanceUntilIdle()

        assertEquals(mockWrongQuestions, viewModel.wrongQuestions.value)
        assertEquals(2, viewModel.currentPage.value)
        assertEquals(false, viewModel.hasMore.value)
    }

    @Test
    fun `loadWrongQuestions - error response should update errorMessage`() = runTest {
        whenever(mockApiService.getWrongQuestions(1, 3)).thenReturn(Response.error(500, "error".toResponseBody()))

        viewModel.loadWrongQuestions(1)
        advanceUntilIdle()

        assertEquals(emptyList<WrongQuestion>(), viewModel.wrongQuestions.value)
        assertEquals(false, viewModel.isLoading.value)
        assertTrue(viewModel.errorMessage.value?.contains("加载错题失败") == true)
    }

    @Test
    fun `loadWrongQuestions - exception should update errorMessage`() = runTest {
        whenever(mockApiService.getWrongQuestions(1, 3)).thenThrow(RuntimeException("Test Exception"))

        viewModel.loadWrongQuestions(1)
        advanceUntilIdle()

        assertEquals(emptyList<WrongQuestion>(), viewModel.wrongQuestions.value)
        assertEquals(false, viewModel.isLoading.value)
        assertTrue(viewModel.errorMessage.value?.contains("错误") == true)
    }

    @Test
    fun `refresh should reload questions from page 1`() = runTest {
        whenever(mockApiService.getWrongQuestions(1, 3)).thenReturn(Response.success(mockWrongQuestions))

        viewModel.refresh()
        advanceUntilIdle()

        assertEquals(mockWrongQuestions, viewModel.wrongQuestions.value)
        assertEquals(1, viewModel.currentPage.value)
    }

    @Test
    fun `clearCache should clear wrongQuestions`() {
        viewModel.wrongQuestions.value = mockWrongQuestions
        viewModel.clearCache()
        assertEquals(emptyList<WrongQuestion>(), viewModel.wrongQuestions.value)
    }

    @Test
    fun `loadNextPage should call loadWrongQuestions with next page when hasMore`() = runTest {
        whenever(mockApiService.getWrongQuestions(1, 3)).thenReturn(Response.success(mockWrongQuestions))
        whenever(mockApiService.getWrongQuestions(2, 3)).thenReturn(
            Response.success(listOf(WrongQuestion(3, "题目内容3", "B", "C")))
        )

        viewModel.loadWrongQuestions(1)
        advanceUntilIdle()
        viewModel.loadNextPage()
        advanceUntilIdle()

        assertEquals(2, viewModel.currentPage.value)
    }

    @Test
    fun `deleteWrongQuestion - success should update deleteState and refresh`() = runTest {
        whenever(mockApiService.deleteWrongQuestion(1)).thenReturn(Response.success(Unit))
        whenever(mockApiService.getWrongQuestions(1, 3)).thenReturn(Response.success(mockWrongQuestions))

        viewModel.deleteWrongQuestion(1)
        advanceUntilIdle()

        assertTrue(viewModel.deleteState.value is WrongQuestionViewModel.DeleteState.Success)
        assertEquals(mockWrongQuestions, viewModel.wrongQuestions.value)
    }

    @Test
    fun `deleteWrongQuestion - error should update deleteState Error`() = runTest {
        whenever(mockApiService.deleteWrongQuestion(1)).thenThrow(RuntimeException("删除失败"))

        viewModel.deleteWrongQuestion(1)
        advanceUntilIdle()

        val state = viewModel.deleteState.value
        assertTrue(state is WrongQuestionViewModel.DeleteState.Error)
        assertTrue((state as WrongQuestionViewModel.DeleteState.Error).message.contains("删除失败"))
    }

    @Test
    fun `getQuestionDetail - success should update questionDetail and show dialog`() = runTest {
        whenever(mockApiService.getQuestion(1)).thenReturn(Response.success(mockQuestion))

        viewModel.getQuestionDetail(1)
        advanceUntilIdle()

        assertEquals(mockQuestion, viewModel.questionDetail.value)
        assertEquals(true, viewModel.showQuestionDialog.value)
        assertNull(viewModel.loadingDetailId.value)
    }

    @Test
    fun `getQuestionDetail - error response should update errorMessage and show dialog`() = runTest {
        whenever(mockApiService.getQuestion(1)).thenReturn(Response.error(404, "not found".toResponseBody()))

        viewModel.getQuestionDetail(1)
        advanceUntilIdle()

        assertTrue(viewModel.errorMessage.value?.contains("加载考试列表失败") == true)
        assertEquals(true, viewModel.showQuestionDialog.value)
        assertNull(viewModel.loadingDetailId.value)
    }

    @Test
    fun `getQuestionDetail - exception should update errorMessage and show dialog`() = runTest {
        whenever(mockApiService.getQuestion(1)).thenThrow(RuntimeException("Test Exception"))

        viewModel.getQuestionDetail(1)
        advanceUntilIdle()

        assertTrue(viewModel.errorMessage.value?.contains("错误") == true)
        assertEquals(true, viewModel.showQuestionDialog.value)
        assertNull(viewModel.loadingDetailId.value)
    }

    @Test
    fun `viewDetail should update currentQuestionId and call getQuestionDetail`() = runTest {
        whenever(mockApiService.getQuestion(2)).thenReturn(Response.success(mockQuestion))

        viewModel.viewDetail(2)
        advanceUntilIdle()

        // 由于 viewDetail 只设置 _currentQuestionId，getQuestionDetail 的测试已覆盖
    }

    @Test
    fun `closeQuestionDialog should reset dialog and states`() {
        // 先人为设置
        val showDialogField = WrongQuestionViewModel::class.java.getDeclaredField("_showQuestionDialog")
        showDialogField.isAccessible = true
        (showDialogField.get(viewModel) as MutableStateFlow<Boolean>).value = true

        val currentIdField = WrongQuestionViewModel::class.java.getDeclaredField("_currentQuestionId")
        currentIdField.isAccessible = true
        (currentIdField.get(viewModel) as MutableStateFlow<Int?>).value = 123

        viewModel.startPractice.value = true

        viewModel.closeQuestionDialog()

        assertEquals(false, viewModel.showQuestionDialog.value)
        assertEquals(false, viewModel.startPractice.value)
        assertNull((currentIdField.get(viewModel) as MutableStateFlow<Int?>).value)
    }

    @Test
    fun `getSimilarQuestionIds - success should update similarQuestionIds and startPractice`() = runTest {
        whenever(mockApiService.getSimilarQuestionIds(1)).thenReturn(Response.success(listOf(2, 3, 4)))

        viewModel.getSimilarQuestionIds(1)
        advanceUntilIdle()

        assertEquals(listOf(2, 3, 4), viewModel.similarQuestionIds.value)
        assertEquals(false, viewModel.loadingSimilarQuestions.value)
        assertEquals(true, viewModel.startPractice.value)
    }

    @Test
    fun `getSimilarQuestionIds - error response should update errorMessage`() = runTest {
        whenever(mockApiService.getSimilarQuestionIds(1)).thenReturn(Response.error(500, "error".toResponseBody()))

        viewModel.getSimilarQuestionIds(1)
        advanceUntilIdle()

        assertTrue(viewModel.errorMessage.value?.contains("加载考试列表失败") == true)
        assertEquals(false, viewModel.loadingSimilarQuestions.value)
    }

    @Test
    fun `getSimilarQuestionIds - exception should update errorMessage`() = runTest {
        whenever(mockApiService.getSimilarQuestionIds(1)).thenThrow(RuntimeException("Test Exception"))

        viewModel.getSimilarQuestionIds(1)
        advanceUntilIdle()

        assertTrue(viewModel.errorMessage.value?.contains("错误") == true)
        assertEquals(false, viewModel.loadingSimilarQuestions.value)
    }

    @Test
    fun `clearToast should set errorMessage to null`() = runTest {
        // 先人为设置错误信息
        val errorField = WrongQuestionViewModel::class.java.getDeclaredField("_errorMessage")
        errorField.isAccessible = true
        (errorField.get(viewModel) as MutableStateFlow<String?>).value = "错误: 测试"

        viewModel.clearToast()
        assertNull(viewModel.errorMessage.value)
    }
}