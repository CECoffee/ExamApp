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
                 val response = apiService.getPractices()
                 if (response.isSuccessful) {
                     _practices.value = response.body() ?: emptyList()
                 } else {
                     _errorMessage.value = "加载练习列表失败: ${response.code()}"
                 }
            } catch (e: Exception) {
                _errorMessage.value = "错误: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun selectPractice(index: Int) {
        _selectedPracticeIndex.value = index
    }

    fun clearToast() {
        _errorMessage.value = null
    }
}

