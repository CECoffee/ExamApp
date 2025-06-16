package dev.coffee.examapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.coffee.examapp.model.Question
import dev.coffee.examapp.network.ApiService
import dev.coffee.examapp.network.RetrofitClient
import dev.coffee.examapp.network.ScoreRequest
import dev.coffee.examapp.network.SubmitAnswerRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ExamViewModel(
    private val examId: Int,
    totalTimeSeconds: Int,
    private val questionIds: List<Int>,
    private val apiService: ApiService = RetrofitClient.instance
) : ViewModel() {
    private val _scorePerQuestion = 100 / questionIds.size

    private var _remainingTime = MutableStateFlow(totalTimeSeconds)
    val remainingTime: StateFlow<Int> = _remainingTime.asStateFlow()

    private var _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex.asStateFlow()

    private var _currentQuestion = MutableStateFlow<Question?>(null)
    val currentQuestion: StateFlow<Question?> = _currentQuestion.asStateFlow()

    private var _userAnswer: MutableStateFlow<String> = MutableStateFlow("")
    val userAnswer: StateFlow<String> = _userAnswer.asStateFlow()
    private var _isCorrect = MutableStateFlow(false)
    private var _score = MutableStateFlow(0.0)
    val score: StateFlow<Double> = _score.asStateFlow()

    private var _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var _examFinished = MutableStateFlow(false)
    val examFinished: StateFlow<Boolean> = _examFinished.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _answeredQuestions = MutableStateFlow<Set<Int>>(emptySet())
    val answeredQuestions: StateFlow<Set<Int>> = _answeredQuestions.asStateFlow()


    init {
        startTimer()
        loadQuestion(questionIds.first())
    }

    private fun startTimer() {
        viewModelScope.launch {
            while (_remainingTime.value > 0 && !_examFinished.value) {
                delay(1000)
                _remainingTime.value--
            }
            if (_remainingTime.value <= 0) {
                finishExam()
            }
        }
    }

    private fun loadQuestion(questionId: Int) {
        _isLoading.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            try {
                val response = apiService.getQuestion(questionId)
                if (response.isSuccessful) {
                    val question = response.body()
                    // 保留用户答案（如果有）
                    question?.let { q ->
                        _currentQuestion.value = q.copy(myAnswer = _userAnswer.value)
                    }
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
        _userAnswer.value = answer
        currentQuestion.value?.let { question ->
            _currentQuestion.value = question.copy(myAnswer = answer)
        }
    }

    fun navigateToNext() {
        if (_userAnswer.value.isEmpty()) {
            _errorMessage.value = "请输入答案"
        } else {
            viewModelScope.launch {
                _isLoading.value = true
                try {
                    submitCurrentAnswer()
                    val nextIndex = _currentQuestionIndex.value + 1
                    if (nextIndex < questionIds.size) {
                        _currentQuestionIndex.value = nextIndex
                        loadQuestion(questionIds[nextIndex])
                    } else {
                        finishExam()
                    }
                } catch (e: Exception) {
                    _errorMessage.value = e.message
                } finally {
                    _isLoading.value = false
                }
            }
        }
    }

    fun navigateToPrevious() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                submitCurrentAnswer()
                val prevIndex = _currentQuestionIndex.value - 1
                if (prevIndex >= 0) {
                    _currentQuestionIndex.value = prevIndex
                    loadQuestion(questionIds[prevIndex])
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun submitCurrentAnswer() {
        currentQuestion.value?.let { question ->
            _userAnswer.value.let { answer ->
                try {
                    _isCorrect.value = (answer == _currentQuestion.value?.correctAnswer)
                    calculateScore()
                    apiService.submitExamAnswer(question.id, SubmitAnswerRequest(answer, _isCorrect.value))
                    // 新增：记录已答题号
                    _answeredQuestions.value = _answeredQuestions.value + _currentQuestionIndex.value
                    _userAnswer.value = ""
                } catch (e: Exception) {
                    _errorMessage.value = "答案提交失败: ${e.message}"
                    throw e
                }
            }
        }
    }

    private fun calculateScore() {
        if (_isCorrect.value) _score.value += _scorePerQuestion
    }


    fun clearToast() {
        _errorMessage.value = null
    }

    fun finishExam() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                apiService.submitExam(examId, ScoreRequest(score.value))
                _examFinished.value = true
            } catch (e: Exception) {
                _errorMessage.value = "考试提交失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }


    // 在 navigateToQuestion 方法中，也要处理：如果当前题有答案，记录当前 index
    fun navigateToQuestion(index: Int) {
        if (index in questionIds.indices) {
            viewModelScope.launch {
                _isLoading.value = true
                try {
                    // 提交当前答案（如果有）
                    if (_userAnswer.value.isNotEmpty()) {
                        submitCurrentAnswer()
                    }
                    // 新增：如果当前有答案，记录当前 index
                    if (_userAnswer.value.isNotEmpty()) {
                        _answeredQuestions.value = _answeredQuestions.value + _currentQuestionIndex.value
                    }
                    // 更新当前题目索引
                    _currentQuestionIndex.value = index
                    // 加载新题目
                    loadQuestion(questionIds[index])
                    // 重置当前答案
                    _userAnswer.value = ""
                } catch (e: Exception) {
                    _errorMessage.value = "跳转题目失败: ${e.message}"
                } finally {
                    _isLoading.value = false
                }
            }
        }
    }
}