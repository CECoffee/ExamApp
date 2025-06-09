package dev.coffee.examapp.ViewModelTest

import dev.coffee.examapp.ViewModelTest.TestUtils.CoroutineTestRule
import dev.coffee.examapp.model.Exam
import dev.coffee.examapp.model.ExamStatus
import dev.coffee.examapp.network.ApiService
import dev.coffee.examapp.network.RetrofitClient
import dev.coffee.examapp.viewmodel.ExamListViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
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
        val success = RetrofitClient.setBaseUrl("http://http://47.123.2.211:8080/")
        check(!success) { "Failed to set base URL!" }

        // 初始化 ViewModel
        viewModel = ExamListViewModel(mockApiService)
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
        viewModel.setExamsForTest(mockExams)
        val pendingExams = viewModel.filterExamsByStatus(ExamStatus.PENDING)
        assertEquals(1, pendingExams.size)
        assertTrue(pendingExams.all { it.status == ExamStatus.PENDING })
    }

    @Test
    fun `筛选状态为 COMPLETED 的考试`() {
        viewModel.setExamsForTest(mockExams)
        val completedExams = viewModel.filterExamsByStatus(ExamStatus.COMPLETED)
        assertEquals(1, completedExams.size)
        assertTrue(completedExams.all { it.status == ExamStatus.COMPLETED })
    }

    @Test
    fun `筛选状态为 EXPIRED 的考试`() {
        viewModel.setExamsForTest(mockExams)
        val expiredExams = viewModel.filterExamsByStatus(ExamStatus.EXPIRED)
        assertEquals(1, expiredExams.size)
        assertTrue(expiredExams.all { it.status == ExamStatus.EXPIRED })
    }

    @Test
    fun `调用 clearToast 后应清除错误信息`() = runTest {
        viewModel.setErrorMessageForTest("错误: 无法获取考试")
        viewModel.clearToast()
        assertNull(viewModel.errorMessage.value)
    }

}



//class ExamListViewModelTest() {
//    private val viewModel = ExamListViewModel()
//    @Test
//    fun 成功加载考试列表() = runTest{
//        // 测试数据
//        val testExams = listOf(
//        Exam(
//            id = 1,
//            name = "数学期中考试",
//            startTime = Date(2023, 9, 1, 9, 0),
//            endTime = Date(2023, 9, 1, 10, 30),
//            duration = 5400, // 90分钟
//            status = ExamStatus.COMPLETED,
//            score = 85.5,
//            questionList = listOf(101, 102, 103)
//        ),
//        Exam(
//            id = 2,
//            name = "英语期末考试",
//            startTime = Date(2023, 9, 5, 13, 30),
//            endTime = Date(2023, 9, 5, 15, 0),
//            duration = 5400,
//            status = ExamStatus.COMPLETED,
//            score = 92.0,
//            questionList = listOf(201, 202, 203)
//        ),
//        Exam(
//            id = 3,
//            name = "物理单元测试",
//            startTime = Date(2023, 9, 10, 10, 15),
//            endTime = Date(2023, 9, 10, 11, 0),
//            duration = 2700, // 45分钟
//            status = ExamStatus.COMPLETED,
//            score = 78.3,
//            questionList = listOf(301, 302)
//        ),
//        Exam(
//            id = 4,
//            name = "化学实验考试",
//            startTime = Date(2023, 9, 15, 14, 0),
//            endTime = Date(2023, 9, 15, 16, 30),
//            duration = 9000, // 150分钟
//            status = ExamStatus.COMPLETED,
//            score = 88.7,
//            questionList = listOf(401, 402, 403, 404)
//        ),
//        Exam(
//            id = 5,
//            name = "生物期中考试",
//            startTime = Date(2023, 9, 20, 9, 30),
//            endTime = Date(2023, 9, 20, 11, 0),
//            duration = 5400,
//            status = ExamStatus.COMPLETED,
//            score = 76.2,
//            questionList = listOf(501, 502, 503)
//        ),
//        Exam(
//            id = 6,
//            name = "历史期末考试",
//            startTime = Date(2023, 9, 25, 8, 0),
//            endTime = Date(2023, 9, 25, 9, 30),
//            duration = 5400,
//            status = ExamStatus.COMPLETED,
//            score = 95.0,
//            questionList = listOf(601, 602, 603)
//        ),
//        Exam(
//            id = 7,
//            name = "地理单元测试",
//            startTime = Date(2023, 10, 1, 10, 0),
//            endTime = Date(2023, 10, 1, 10, 45),
//            duration = 2700,
//            status = ExamStatus.COMPLETED,
//            score = 82.4,
//            questionList = listOf(701, 702)
//        ),
//        Exam(
//            id = 8,
//            name = "政治模拟考试",
//            startTime = Date(2023, 10, 5, 13, 0),
//            endTime = Date(2023, 10, 5, 15, 30),
//            duration = 9000,
//            status = ExamStatus.COMPLETED,
//            score = 89.1,
//            questionList = listOf(801, 802, 803)
//        ),
//        Exam(
//            id = 9,
//            name = "语文期中考试",
//            startTime = Date(2023, 10, 10, 9, 0),
//            endTime = Date(2023, 10, 10, 11, 30),
//            duration = 9000,
//            status = ExamStatus.COMPLETED,
//            score = 91.5,
//            questionList = listOf(901, 902, 903, 904)
//        ),
//        Exam(
//            id = 10,
//            name = "数学期末考试",
//            startTime = Date(2023, 10, 15, 14, 0),
//            endTime = Date(2023, 10, 15, 16, 0),
//            duration = 7200, // 120分钟
//            status = ExamStatus.COMPLETED,
//            score = 87.8,
//            questionList = listOf(1001, 1002, 1003)
//        )
//    )