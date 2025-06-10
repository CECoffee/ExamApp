package dev.coffee.examapp.viewmodel

import dev.coffee.examapp.viewmodel.utils.CoroutineTestRule
import dev.coffee.examapp.model.Exam
import dev.coffee.examapp.model.ExamStatus
import dev.coffee.examapp.network.ApiService
import dev.coffee.examapp.network.RetrofitClient
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import retrofit2.Response
import java.util.Date


@OptIn(ExperimentalCoroutinesApi::class)
class ExamListViewModelTest {
    @get:Rule
    val coroutineRule = CoroutineTestRule()

    private lateinit var viewModel: ExamListViewModel
    private val mockApiService: ApiService = mock()
    val mockExams = listOf(
        Exam(
            id = 1,
            name = "数学期中考试",
            startTime = Date(2023 - 1900, 9 - 1, 1, 9, 0),
            endTime = Date(2023 - 1900, 9 - 1, 1, 10, 30),
            duration = 5400,
            status = ExamStatus.COMPLETED,
            score = 85.5,
            questionList = listOf(101, 102, 103)
        ),
        Exam(
            id = 1,
            name = "数学期中考试",
            startTime = Date(2023 - 1900, 9 - 1, 1, 9, 0),
            endTime = Date(2023 - 1900, 9 - 1, 1, 10, 30),
            duration = 5400,
            status = ExamStatus.PENDING,
            score = 85.5,
            questionList = listOf(101, 102, 103)
        ),
        Exam(
            id = 1,
            name = "数学期中考试",
            startTime = Date(2023 - 1900, 9 - 1, 1, 9, 0),
            endTime = Date(2023 - 1900, 9 - 1, 1, 10, 30),
            duration = 5400,
            status = ExamStatus.EXPIRED,
            score = 85.5,
            questionList = listOf(101, 102, 103)
        )
    )

    @Before
    fun setup() =runBlocking {
        // TODO IP
        val success = RetrofitClient.setBaseUrl("")
        check(success) { "Failed to set base URL!" }

        // 初始化 ViewModel
        viewModel = ExamListViewModel().apply {
            // 用反射或其他方法注入 mockApiService，如果 PracticeListViewModel 构造器支持注入可直接传参
            val field = ExamListViewModel::class.java.getDeclaredField("apiService")
            field.isAccessible = true
            field.set(this, mockApiService)
        }
    }

    @Test
    fun `加载考试-成功时应更新考试列表`() = runTest {

        whenever(mockApiService.getExams(null)).thenReturn(Response.success(mockExams))

        viewModel.loadExams()

        // 等待协程完成
        advanceUntilIdle()

        // 👇 核心：要收集最新 stateFlow 的值再断言
        val actualExams = viewModel.exams.value
        val actualLoading = viewModel.isLoading.value
        val actualError = viewModel.errorMessage.value
        println("Exams = ${viewModel.exams.value}")
        println("Loading = ${viewModel.isLoading.value}")
        println("Error = ${viewModel.errorMessage.value}")
        assertEquals(mockExams, actualExams)
        assertEquals(false, actualLoading)
        assertNull(actualError)
    }
    @Test
    fun `筛选状态为 PENDING 的考试`() {
        val field = ExamListViewModel::class.java.getDeclaredField("_exams")
        field.isAccessible = true
        (field.get(viewModel) as MutableStateFlow<List<Exam>>).value = mockExams
        val pendingExams = viewModel.filterExamsByStatus(ExamStatus.PENDING)
        assertEquals(1, pendingExams.size)
        assertTrue(pendingExams.all { it.status == ExamStatus.PENDING })
    }

    @Test
    fun `筛选状态为 COMPLETED 的考试`() {
        val field = ExamListViewModel::class.java.getDeclaredField("_exams")
        field.isAccessible = true
        (field.get(viewModel) as MutableStateFlow<List<Exam>>).value = mockExams
        val completedExams = viewModel.filterExamsByStatus(ExamStatus.COMPLETED)
        assertEquals(1, completedExams.size)
        assertTrue(completedExams.all { it.status == ExamStatus.COMPLETED })
    }

    @Test
    fun `筛选状态为 EXPIRED 的考试`() {
        val field = ExamListViewModel::class.java.getDeclaredField("_exams")
        field.isAccessible = true
        (field.get(viewModel) as MutableStateFlow<List<Exam>>).value = mockExams
        val expiredExams = viewModel.filterExamsByStatus(ExamStatus.EXPIRED)
        assertEquals(1, expiredExams.size)
        assertTrue(expiredExams.all { it.status == ExamStatus.EXPIRED })
    }

    @Test
    fun `调用 clearToast 后应清除错误信息`() = runTest {
        val field = ExamListViewModel::class.java.getDeclaredField("_errorMessage")
        field.isAccessible = true
        (field.get(viewModel) as MutableStateFlow<String?>).value = "错误: 无法获取考试"
        viewModel.clearToast()
        assertNull(viewModel.errorMessage.value)
    }

}
