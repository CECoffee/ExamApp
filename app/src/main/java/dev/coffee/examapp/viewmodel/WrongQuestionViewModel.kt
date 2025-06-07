package dev.coffee.examapp.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.coffee.examapp.model.Question
import dev.coffee.examapp.model.WrongQuestion
import dev.coffee.examapp.network.ApiService
import dev.coffee.examapp.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WrongQuestionViewModel : ViewModel() {
    private val apiService: ApiService = RetrofitClient.instance
    val wrongQuestions = MutableStateFlow<List<WrongQuestion>>(emptyList())
    val similarQuestionIds = MutableStateFlow<List<Int>>(emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _loadingDetailId = MutableStateFlow<Int?>(null)
    val loadingDetailId: StateFlow<Int?> = _loadingDetailId.asStateFlow()

    private val _loadingSimilarQuestions = MutableStateFlow<Boolean>(false)
    val loadingSimilarQuestions: StateFlow<Boolean> = _loadingSimilarQuestions.asStateFlow()

    private val _showQuestionDialog = MutableStateFlow(false)
    val showQuestionDialog: StateFlow<Boolean> = _showQuestionDialog

    private val _doSimilarQuestions = MutableStateFlow(false)
    val doSimilarQuestions: StateFlow<Boolean> = _doSimilarQuestions

    private val _currentQuestionId = MutableStateFlow<Int?>(null)

    val questionDetail = MutableStateFlow<Question?>(null)

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _currentPage = MutableStateFlow(1)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    private val _hasMore = MutableStateFlow(true)
    val hasMore: StateFlow<Boolean> = _hasMore.asStateFlow()

    // 删除状态
    sealed class DeleteState {
        object Idle : DeleteState()
        object Loading : DeleteState()
        object Success : DeleteState()
        data class Error(val message: String) : DeleteState()
    }

    val deleteState = mutableStateOf<DeleteState>(DeleteState.Idle)

    fun loadWrongQuestions(page: Int = 1) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val response = apiService.getWrongQuestions(page, 3)
                if (response.isSuccessful) {
                    val newQuestions = response.body() ?: emptyList()

                    if (page == 1) {
                        wrongQuestions.value = newQuestions
                    } else {
                        wrongQuestions.value = wrongQuestions.value + newQuestions
                    }

                    _hasMore.value = newQuestions.isNotEmpty()
                    _currentPage.value = page
                } else {
                    _errorMessage.value = "加载错题失败: ${response.code()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "错误: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refresh() {
        loadWrongQuestions(1)
    }

    fun loadNextPage() {
        if (!isLoading.value && hasMore.value) {
            loadWrongQuestions(currentPage.value + 1)
        }
    }

    suspend fun deleteWrongQuestion(id: Int) {
        try {
            deleteState.value = DeleteState.Loading
            apiService.deleteWrongQuestion(questionId = id)
            deleteState.value = DeleteState.Success
            refresh()
        } catch (e: Exception) {
            deleteState.value = DeleteState.Error(e.message ?: "删除失败")
        }
    }

    fun getQuestionDetail(questionId: Int) {
        viewModelScope.launch{
            try {
                _loadingDetailId.value = questionId
                val response = apiService.getQuestion(questionId)
                if (response.isSuccessful) {
                    questionDetail.value = response.body()
                } else {
                    _errorMessage.value = "加载考试列表失败: ${response.code()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "错误: ${e.message}"
            } finally {
                _loadingDetailId.value = null
                _showQuestionDialog.value = true
            }
        }
    }


    fun viewDetail(questionId: Int) {
        _currentQuestionId.value = questionId
        getQuestionDetail(questionId)
    }

    fun closeQuestionDialog() {
        _showQuestionDialog.value = false
        _currentQuestionId.value = null
    }

    fun getSimilarQuestionIds(questionId: Int) {
        viewModelScope.launch{
            try {
                _loadingSimilarQuestions.value = true
                val response = apiService.getSimilarQuestionIds(questionId)
                if (response.isSuccessful) {
                    similarQuestionIds.value = response.body() ?: emptyList()
                } else {
                    _errorMessage.value = "加载考试列表失败: ${response.code()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "错误: ${e.message}"
            } finally {
                _loadingSimilarQuestions.value = false
                _doSimilarQuestions.value = true
            }
        }
    }

    fun finishPractice() {
        _doSimilarQuestions.value = false
    }

    fun clearToast() {
        _errorMessage.value = null
    }
}