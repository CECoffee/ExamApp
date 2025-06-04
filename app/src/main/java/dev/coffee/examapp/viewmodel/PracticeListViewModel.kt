package dev.coffee.examapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.coffee.examapp.model.Chapter
import dev.coffee.examapp.model.Practice
import dev.coffee.examapp.network.ApiService
import dev.coffee.examapp.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
class PracticeListViewModel : ViewModel() {
    private val apiService: ApiService = RetrofitClient.instance

    private val _practices = MutableStateFlow<List<Practice>>(emptyList())
    val practices: StateFlow<List<Practice>> = _practices.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _selectedPracticeIndex = MutableStateFlow(0)
    val selectedPracticeIndex: StateFlow<Int> = _selectedPracticeIndex.asStateFlow()

    fun loadPractices() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                // :
                // TODO: Replace with actual API call
                // val response = apiService.getPractices()
                // if (response.isSuccessful) {
                //     _practices.value = response.body() ?: emptyList()
                // } else {
                //     _errorMessage.value = "加载练习列表失败: ${response.code()}"
                // }

                // Temporary mock data
                _practices.value = mockPractices()
            } catch (e: Exception) {
                _errorMessage.value = "网络错误: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun selectPractice(index: Int) {
        _selectedPracticeIndex.value = index
    }

    private fun mockPractices(): List<Practice> {
        return listOf(
            Practice(
                id = 1,
                name = "高等数学",
                questionCount = 200,
                completedCount = 75,
                progress = 0.375,
                chapters = listOf(
                    Chapter(1, "函数与极限", 30, 10, 0.333),
                    Chapter(2, "导数与微分", 40, 15, 0.375),
                    Chapter(3, "积分学", 50, 20, 0.4),
                    Chapter(4, "多元函数", 40, 20, 0.5),
                    Chapter(5, "级数", 40, 10, 0.25),
                    Chapter(6, "级数", 40, 10, 0.25),
                    Chapter(7, "级数", 40, 10, 0.25)

                )
            ),
            Practice(
                id = 2,
                name = "线性代数",
                questionCount = 150,
                completedCount = 90,
                progress = 0.6,
                chapters = listOf(
                    Chapter(1, "行列式", 20, 15, 0.75),
                    Chapter(2, "矩阵", 30, 20, 0.666),
                    Chapter(3, "向量组", 40, 25, 0.625),
                    Chapter(4, "线性方程组", 30, 20, 0.666),
                    Chapter(5, "特征值与特征向量", 30, 10, 0.333)
                )
            ),
            Practice(
                id = 3,
                name = "概率统计",
                questionCount = 180,
                completedCount = 45,
                progress = 0.25,
                chapters = listOf(
                    Chapter(1, "随机事件", 30, 10, 0.333),
                    Chapter(2, "随机变量", 40, 15, 0.375),
                    Chapter(3, "多维随机变量", 50, 10, 0.2),
                    Chapter(4, "数字特征", 30, 5, 0.166),
                    Chapter(5, "大数定律", 30, 5, 0.166)
                )
            )
        )
    }
}

