package dev.coffee.examapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.coffee.examapp.model.Question
import dev.coffee.examapp.network.ApiService
import dev.coffee.examapp.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.let

class SimilarQuestionsViewModel(
    private val questionIds: List<Int>,
    private val apiService: ApiService = RetrofitClient.instance
) : ViewModel() {
    private var _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex.asStateFlow()

    private var _correctCount = MutableStateFlow(0)
    val correctCount = _correctCount.asStateFlow()

    private var _currentQuestion = MutableStateFlow<Question?>(null)
    val currentQuestion: StateFlow<Question?> = _currentQuestion.asStateFlow()

    private var _userAnswer: MutableStateFlow<String> = MutableStateFlow("")
    val userAnswer: StateFlow<String> = _userAnswer.asStateFlow()

    private var _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var _practiceFinished = MutableStateFlow(false)
    val practiceFinished: StateFlow<Boolean> = _practiceFinished.asStateFlow()

    private val _showExplanation = MutableStateFlow(false)
    val showExplanation: StateFlow<Boolean> = _showExplanation.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadQuestion(questionIds.first())
    }

    private fun loadQuestion(questionId: Int) {
        _isLoading.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            try {
                val response = apiService.getQuestion(questionId)
                if (response.isSuccessful) {
                    _currentQuestion.value = response.body()
                } else {
                    _errorMessage.value = "加载题目失败: ${response.code()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateAnswer(answer: String) {
        currentQuestion.value?.let { question ->
            _userAnswer.value = answer
            _currentQuestion.value = question.copy(myAnswer = answer)
        }
    }

    fun loadNextQuestion() {
        viewModelScope.launch {
            try {
                val nextIndex = _currentQuestionIndex.value + 1
                if (nextIndex < questionIds.size) {
                    loadQuestion(questionIds[nextIndex])
                    _userAnswer.value = ""
                    _currentQuestionIndex.value = nextIndex
                } else { finishPractice() }
            _showExplanation.value = false
            } catch (e: Exception) { _errorMessage.value = e.message }
        }

    }

    fun submitCurrentAnswer() {
        if (_userAnswer.value == _currentQuestion.value?.correctAnswer) _correctCount.value ++
        _showExplanation.value = true
    }

    fun clearToast() {
        _errorMessage.value = null
    }

    fun finishPractice() {
        _practiceFinished.value = true
    }
}