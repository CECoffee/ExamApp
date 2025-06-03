package dev.coffee.examapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.coffee.examapp.model.WrongQuestion
import dev.coffee.examapp.network.ApiService
import dev.coffee.examapp.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WrongQuestionViewModel : ViewModel() {
    private val apiService: ApiService = RetrofitClient.instance

    private val _wrongQuestions = MutableStateFlow<List<WrongQuestion>>(emptyList())
    val wrongQuestions: StateFlow<List<WrongQuestion>> = _wrongQuestions.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _currentPage = MutableStateFlow(1)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    private val _hasMore = MutableStateFlow(true)
    val hasMore: StateFlow<Boolean> = _hasMore.asStateFlow()

    fun loadWrongQuestions(token: String, page: Int = 1) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val response = apiService.getWrongQuestions(token, page, 10)
                if (response.isSuccessful) {
                    val newQuestions = response.body() ?: emptyList()

                    if (page == 1) {
                        _wrongQuestions.value = newQuestions
                    } else {
                        _wrongQuestions.value = _wrongQuestions.value + newQuestions
                    }

                    _hasMore.value = newQuestions.isNotEmpty()
                    _currentPage.value = page
                } else {
                    _errorMessage.value = "加载错题失败: ${response.code()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "网络错误: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refresh(token: String) {
        loadWrongQuestions(token, 1)
    }

    fun loadNextPage(token: String) {
        if (!isLoading.value && hasMore.value) {
            loadWrongQuestions(token, currentPage.value + 1)
        }
    }
}