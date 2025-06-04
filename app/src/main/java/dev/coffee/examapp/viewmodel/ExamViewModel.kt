package dev.coffee.examapp.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.coffee.examapp.model.Question
import dev.coffee.examapp.model.QuestionType
import dev.coffee.examapp.network.ApiService
import dev.coffee.examapp.network.RetrofitClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.let

class ExamViewModel(
    private val examId: Int,
    private val totalTimeSeconds: Int,
    private val questionIds: List<Int>
) : ViewModel() {
    private val apiService: ApiService = RetrofitClient.instance
    private var _remainingTime = MutableStateFlow(totalTimeSeconds)
    val remainingTime: StateFlow<Int> = _remainingTime.asStateFlow()

    private var _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex.asStateFlow()

    private var _currentQuestion = MutableStateFlow<Question?>(null)
    val currentQuestion: StateFlow<Question?> = _currentQuestion.asStateFlow()

    private val _userAnswers = mutableMapOf<Int, String>() // 题目ID到答案的映射
    private var _score = MutableStateFlow(0.0)
    val score: StateFlow<Double> = _score.asStateFlow()

    private var _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var _showToast = MutableStateFlow<String?>(null)
    val showToast: StateFlow<String?> = _showToast.asStateFlow()

    private var _examFinished = MutableStateFlow(false)
    val examFinished: StateFlow<Boolean> = _examFinished.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

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
                    _currentQuestion.value = response.body()
                } else {
                    _errorMessage.value = "加载考试列表失败: ${response.code()}"
                }

                // 模拟获取题目数据
//                val question = Question(
//                    id = questionId,
//                    difficulty = when (questionId % 3) {
//                        0 -> 1
//                        1 -> 2
//                        else -> 3
//                    },
//                    content = "请解释以下概念：${listOf("量子纠缠", "人工智能", "区块链", "大数据", "云计算")[questionId % 5]}",
//                    questionType = QuestionType.SHORT_ANSWER,
//                    correctAnswer = "这是正确答案的示例文本，用于展示在预览中"
//                )

            } catch (e: Exception) {
                // 处理错误
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateAnswer(answer: String) {
        currentQuestion.value?.let { question ->
            _userAnswers[question.id] = answer
            // 更新当前问题状态以显示新答案
            _currentQuestion.value = question.copy(myAnswer = answer)
            calculateScore()
        }
    }

    fun navigateToNext(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                submitCurrentAnswer(context) // 提交当前答案
                // 只有提交成功才继续
                val nextIndex = _currentQuestionIndex.value + 1
                if (nextIndex < questionIds.size) {
                    _currentQuestionIndex.value = nextIndex
                    loadQuestion(questionIds[nextIndex])
                } else {
                    finishExam()
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun navigateToPrevious(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                submitCurrentAnswer(context)
                val prevIndex = _currentQuestionIndex.value - 1
                if (prevIndex >= 0) {
                    _currentQuestionIndex.value = prevIndex
                    loadQuestion(questionIds[prevIndex])
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun submitCurrentAnswer(context: Context) {
        currentQuestion.value?.let { question ->
            _userAnswers[question.id]?.let { answer ->
                try {
                    apiService.submitAnswer(question.id, answer)
                } catch (e: Exception) {
                    _showToast.value = "答案提交失败: ${e.message}"
                    throw e // 重新抛出异常以阻止导航
                }
            }
        }
    }

    private fun calculateScore() {
        val totalQuestions = questionIds.size
        if (totalQuestions == 0) return

        val correctCount = _userAnswers.count()
        _score.value = (correctCount.toDouble() / totalQuestions) * 100
    }

    fun clearToast() {
        _showToast.value = null
    }

    fun finishExam() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                apiService.submitExam(examId, score.value)
                _examFinished.value = true
            } catch (e: Exception) {
                _showToast.value = "考试提交失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}