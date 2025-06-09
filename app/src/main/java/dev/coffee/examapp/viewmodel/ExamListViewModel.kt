package dev.coffee.examapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.coffee.examapp.model.Exam
import dev.coffee.examapp.model.ExamStatus
import dev.coffee.examapp.network.ApiService
import dev.coffee.examapp.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.jetbrains.annotations.VisibleForTesting
import java.util.Date

class ExamListViewModel ( val apiService: ApiService = RetrofitClient.instance): ViewModel() {

    private val _exams = MutableStateFlow<List<Exam>>(emptyList())
    val exams: StateFlow<List<Exam>> = _exams.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun loadExams(status: ExamStatus? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val response = apiService.getExams( status?.name)
                if (response.isSuccessful) {
                    _exams.value = response.body() ?: emptyList()
                } else {
                    _errorMessage.value = "加载考试列表失败: ${response.code()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "错误: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

//    // 测试数据
//    val testExams = listOf(
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
//
//    fun loadExams(status: ExamStatus? = null) {
//        _isLoading.value = true
//        // 模拟网络请求延迟
//        viewModelScope.launch {
//            _exams.value = testExams // 使用测试数据
//            _isLoading.value = false
//        }
//    }

    fun filterExamsByStatus(status: ExamStatus): List<Exam> {
        return _exams.value.filter { it.status == status }
    }

    fun clearToast() {
        _errorMessage.value = null
    }

// ExamListViewModel.kt

    @VisibleForTesting
    fun setExamsForTest(exams: List<Exam>) {
        _exams.value = exams
    }

    @VisibleForTesting
    fun setErrorMessageForTest(message: String?) {
        _errorMessage.value = message
    }

}