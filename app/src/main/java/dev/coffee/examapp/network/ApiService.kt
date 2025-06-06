package dev.coffee.examapp.network

import dev.coffee.examapp.model.Exam
import dev.coffee.examapp.model.Practice
import dev.coffee.examapp.model.Question
import dev.coffee.examapp.model.WrongQuestion
import retrofit2.Response
import retrofit2.http.*
import retrofit2.http.Body
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @GET("question/{id}")
    suspend fun getQuestion(@Path("id") id: Int): Response<Question>

    @POST("question/{id}/submit")
    suspend fun submitExamAnswer(
        @Path("id") id: Int,
        @Body request: SubmitAnswerRequest
    ): Response<Unit>

    @GET("exams")
    suspend fun getExams( @Query("status") status: String? = null ): Response<List<Exam>>

    @POST("exams/{id}/submit")
    suspend fun submitExam(
        @Path("id") examId: Int,
        @Body request: ScoreRequest
    ): Response<Unit>

    @GET("wrong-questions")
    suspend fun getWrongQuestions(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): Response<List<WrongQuestion>>

    @DELETE("wrong-questions/{id}")
    suspend fun deleteWrongQuestion( @Path("id") questionId: Int ): Response<Unit>

    @GET("practices")
    suspend fun getPractices(): Response<List<Practice>>

    @POST("practice-question/{chap_id}")
    suspend fun submitPracticeAnswer(
        @Path("chap_id") chapterId : String,
        @Body request: SubmitPracticeAnswerRequest? = null
    ): Response<Question?>
}


data class SubmitAnswerRequest(val answer: String, val isCorrect: Boolean)

data class SubmitPracticeAnswerRequest(val questionId: Int? = null, val answer: String? = null, val isCorrect: Boolean? = null)

data class ScoreRequest(val score: Double)