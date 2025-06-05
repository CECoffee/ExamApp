package dev.coffee.examapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.coffee.examapp.model.Question
import dev.coffee.examapp.model.QuestionType
import dev.coffee.examapp.network.ApiService
import dev.coffee.examapp.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

class PracticeViewModel(
    private val chapterId: Int,
    private val chapterName: String
) : ViewModel() {
    private val apiService: ApiService = RetrofitClient.instance

    // Practice state
    private val _currentQuestion = MutableStateFlow<Question?>(null)
    val currentQuestion: StateFlow<Question?> = _currentQuestion.asStateFlow()

    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex.asStateFlow()

    private val _totalQuestions = 10 // Fixed to 10 questions per practice
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
        loadNextQuestion()
    }

    fun loadNextQuestion() {
        if (_currentQuestionIndex.value >= _totalQuestions) {
            _practiceFinished.value = true
            return
        }

        _isLoading.value = true
        _userAnswer.value = ""
        _showExplanation.value = false

        viewModelScope.launch {
            try {

                 val response = apiService.getPracticeQuestion(chapterId)
                 if (response.isSuccessful) {
                     _currentQuestion.value = response.body()
                 } else {
                     _showToast.value = "加载题目失败: ${response.code()}"
                 }

                // TEST
//                _currentQuestion.value = mockQuestion(
//                    id = _currentQuestionIndex.value + 1,
//                    chapterId = chapterId
//                )
            } catch (e: Exception) {
                _showToast.value = "网络错误: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateUserAnswer(answer: String) {
        _userAnswer.value = answer
    }

    fun submitAnswer() {
        if (_userAnswer.value.isBlank()) {
            _showToast.value = "请输入答案"
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            try {
                _isCorrect.value = _userAnswer.value == _currentQuestion.value?.correctAnswer

                 val response = apiService.submitPracticeAnswer(
                     chapterId = chapterId,
                     answer = _userAnswer.value,
                     isCorrect = _isCorrect.value
                 )

                 if (response.isSuccessful) {
                     if (_isCorrect.value) {
                         _correctCount.value++
                     }
                     _currentQuestion.value = _currentQuestion.value?.copy(isCorrect = _isCorrect.value)
                     _showExplanation.value = true
                 } else {
                     _showToast.value = "提交答案失败: ${response.code()}"
                 }

                // TEST
//                val isCorrect = Random.nextBoolean()
//                if (isCorrect) {
//                    _correctCount.value++
//                }
//                _currentQuestion.value = _currentQuestion.value?.copy(
//                    explanation = mockExplanation(
//                        questionId = _currentQuestion.value?.id ?: 0,
//                        isCorrect = isCorrect
//                    )
//                )
//                _showExplanation.value = true
            } catch (e: Exception) {
                _showToast.value = "提交答案失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun proceedToNextQuestion() {
        _currentQuestionIndex.value++
        loadNextQuestion()
    }

    fun clearToast() {
        _showToast.value = null
    }

    private fun mockQuestion(id: Int, chapterId: Int): Question {
        val chapterTopics = mapOf(
            1 to listOf("函数", "极限", "连续性"),
            2 to listOf("导数", "微分", "中值定理"),
            3 to listOf("不定积分", "定积分", "微积分基本定理")
        )

        val topics = chapterTopics[chapterId] ?: listOf("默认主题")
        val topic = topics.random()

        return Question(
            id = id,
            difficulty = (1..3).random(),
            content = "关于${topic}的问题：${listOf(
                "请解释${topic}的概念",
                "写出${topic}的基本公式",
                "说明${topic}在实际中的应用"
            ).random()}",
            questionType = QuestionType.FILL_IN_THE_BLANK,
            correctAnswer = "这是关于${topic}的正确答案示例",
            explanation = null
        )
    }

    private fun mockExplanation(questionId: Int, isCorrect: Boolean): String {
        return if (isCorrect) {
            "恭喜你答对了！这是关于第${questionId}题的详细解析，你的答案完全正确。"
        } else {
            "正确答案是：${currentQuestion.value?.correctAnswer}\n\n" +
                    "解析：这是关于第${questionId}题的详细解析，你的答案不完全正确，建议复习相关知识点。"
        }
    }
}