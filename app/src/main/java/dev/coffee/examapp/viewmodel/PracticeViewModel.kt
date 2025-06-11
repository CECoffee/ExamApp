package dev.coffee.examapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.coffee.examapp.model.Question
import dev.coffee.examapp.network.ApiService
import dev.coffee.examapp.network.RetrofitClient
import dev.coffee.examapp.network.SubmitPracticeAnswerRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PracticeViewModel(
    private val chapterId: String,
    private val chapterName: String,
    private val apiService: ApiService = RetrofitClient.instance
) : ViewModel() {
    private val _currentQuestion = MutableStateFlow<Question?>(null)
    val currentQuestion: StateFlow<Question?> = _currentQuestion.asStateFlow()

    private val _nextQuestion = MutableStateFlow<Question?>(null)

    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex.asStateFlow()

    private var _totalQuestions = 10 // Fixed to 10 questions per practice
    val totalQuestions: Int get() = _totalQuestions

    private val _userAnswer = MutableStateFlow("")
    val userAnswer: StateFlow<String> = _userAnswer.asStateFlow()

    private val _isCorrect = MutableStateFlow(false)

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _showToast = MutableStateFlow<String?>(null)
    val showToast: StateFlow<String?> = _showToast.asStateFlow()

    private val _practiceFinished = MutableStateFlow(false)
    val practiceFinished: StateFlow<Boolean> = _practiceFinished.asStateFlow()

    private val _showExplanation = MutableStateFlow(false)
    val showExplanation: StateFlow<Boolean> = _showExplanation.asStateFlow()

    private val _correctCount = MutableStateFlow(0)
    val correctCount: StateFlow<Int> = _correctCount.asStateFlow()

    init {
        viewModelScope.launch {
            initQuestion()
            loadNextQuestion()
        }
    }

    fun proceedToNextQuestion() {
        _currentQuestionIndex.value ++
        loadNextQuestion()
    }

    fun loadNextQuestion() {
        if (_currentQuestionIndex.value >= _totalQuestions) {
            _practiceFinished.value = true
            return
        }

        _userAnswer.value = ""
        _showExplanation.value = false

        _currentQuestion.value = _nextQuestion.value
    }

    fun updateUserAnswer(answer: String) {
        _userAnswer.value = answer
    }

    fun submitAnswer() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _isCorrect.value = _userAnswer.value == _currentQuestion.value?.correctAnswer

                 val response = apiService.submitPracticeAnswer(
                     chapterId = chapterId,
                     request = SubmitPracticeAnswerRequest(
                        questionId = _currentQuestion.value?.id,
                        answer = _userAnswer.value,
                        isCorrect = _isCorrect.value
                     )
                 )

                 if (response.isSuccessful) {
                     if (_isCorrect.value) {
                         _correctCount.value ++
                     }
                     _currentQuestion.value = _currentQuestion.value?.copy(isCorrect = _isCorrect.value)
                     _showExplanation.value = true

                     _nextQuestion.value = response.body()
                     if (_nextQuestion.value?.id == 0) {
                         _showToast.value = "本章节题库已空"
                         _totalQuestions = _currentQuestionIndex.value + 1
                     }
                 } else {
                     _showToast.value = "提交答案失败: ${response.code()}"
                 }
            } catch (e: Exception) {
                _showToast.value = "失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    suspend fun initQuestion() {
        _isLoading.value = true
        try {
             val response = apiService.submitPracticeAnswer(
                 chapterId = chapterId,
                 request = SubmitPracticeAnswerRequest(null, null, null)
             )

            if (response.isSuccessful) {
                _nextQuestion.value = response.body()
            } else {
                _showToast.value = "获取题目失败: ${response.code()}"
            }
        } catch (e: Exception) {
            _showToast.value = "失败: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    fun clearToast() {
        _showToast.value = null
    }
}