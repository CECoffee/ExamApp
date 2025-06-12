package dev.coffee.examapp.viewmodel

import dev.coffee.examapp.model.Chapter
import dev.coffee.examapp.model.Practice
import dev.coffee.examapp.viewmodel.utils.CoroutineTestRule
import dev.coffee.examapp.network.ApiService
import dev.coffee.examapp.network.RetrofitClient
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class PracticeListViewModelTest {
    @get:Rule
    val coroutineRule = CoroutineTestRule()

    private lateinit var viewModel: PracticeListViewModel
    private val mockApiService: ApiService = mock()
    val mockPractices = listOf(
        Practice(
            id = 1,
            name = "基础练习",
            questionCount = 20,
            completedCount = 10,
            chapters = listOf(
                Chapter(
                    id = "c1",
                    name = "第一章：集合与函数",
                    questionCount = 10,
                    completedCount = 5,
                    progress = 0.5
                ),
                Chapter(
                    id = "c2",
                    name = "第二章：数列",
                    questionCount = 10,
                    completedCount = 5,
                    progress = 0.5
                )
            )
        ),
        Practice(
            id = 2,
            name = "提高练习",
            questionCount = 15,
            completedCount = 7,
            chapters = listOf(
                Chapter(
                    id = "c3",
                    name = "第三章：三角函数",
                    questionCount = 8,
                    completedCount = 3,
                    progress = 0.375
                ),
                Chapter(
                    id = "c4",
                    name = "第四章：平面解析几何",
                    questionCount = 7,
                    completedCount = 4,
                    progress = 0.571
                )
            )
        )
    )

    @Before
    fun setup() = runBlocking {
        // TODO 隐藏真实IP
        val success = RetrofitClient.setBaseUrl("http://47.123.2.211:8080/")
        check(success) { "Failed to set base URL!" }

        viewModel = PracticeListViewModel().apply {
            val field = PracticeListViewModel::class.java.getDeclaredField("apiService")
            field.isAccessible = true
            field.set(this, mockApiService)
        }
    }

    @Test
    fun `加载练习-成功时应更新练习列表`() = runTest {
        whenever(mockApiService.getPractices()).thenReturn(Response.success(mockPractices))

        viewModel.loadPractices()

        advanceUntilIdle()

        val actualPractices = viewModel.practices.value
        val actualLoading = viewModel.isLoading.value
        val actualError = viewModel.errorMessage.value

        println("Practices = $actualPractices")
        println("Loading = $actualLoading")
        println("Error = $actualError")

        assertEquals(mockPractices, actualPractices)
        assertEquals(false, actualLoading)
        assertNull(actualError)
    }

    @Test
    fun `加载练习-失败时应更新错误信息`() = runTest {
        whenever(mockApiService.getPractices()).thenReturn(Response.error(500, "error".toResponseBody()))

        viewModel.loadPractices()
        advanceUntilIdle()

        assertEquals(emptyList<Practice>(), viewModel.practices.value)
        assertEquals(false, viewModel.isLoading.value)
        assertTrue(viewModel.errorMessage.value?.contains("加载练习列表失败") == true)
    }

    @Test
    fun `加载练习-异常时应更新错误信息`() = runTest {
        whenever(mockApiService.getPractices()).thenThrow(RuntimeException("Test Exception"))

        viewModel.loadPractices()
        advanceUntilIdle()

        assertEquals(emptyList<Practice>(), viewModel.practices.value)
        assertEquals(false, viewModel.isLoading.value)
        assertTrue(viewModel.errorMessage.value?.contains("错误") == true)
    }

    @Test
    fun `selectPractice 应切换选中索引`() {
        viewModel.selectPractice(1)
        assertEquals(1, viewModel.selectedPracticeIndex.value)
        viewModel.selectPractice(0)
        assertEquals(0, viewModel.selectedPracticeIndex.value)
    }

    @Test
    fun `调用 clearToast 后应清除错误信息`() = runTest {
        // 先人为设置错误信息
        val errorField = PracticeListViewModel::class.java.getDeclaredField("_errorMessage")
        errorField.isAccessible = true
        (errorField.get(viewModel) as MutableStateFlow<String?>).value = "错误: 无法获取练习"

        viewModel.clearToast()
        assertNull(viewModel.errorMessage.value)
    }
}