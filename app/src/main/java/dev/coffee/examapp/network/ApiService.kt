package dev.coffee.examapp.network

import dev.coffee.examapp.model.Exam
import dev.coffee.examapp.model.WrongQuestion
import retrofit2.Response
import retrofit2.http.*
import retrofit2.http.Header
import retrofit2.http.Body
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("exams")
    suspend fun getExams(
        @Header("Authorization") token: String,
        @Query("status") status: String? = null
    ): Response<List<Exam>>

    @GET("wrong-questions")
    suspend fun getWrongQuestions(
        @Header("Authorization") token: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): Response<List<WrongQuestion>>

    @GET("exams/{id}/start")
    suspend fun startExam(
        @Header("Authorization") token: String,
        @Path("id") examId: String
    ): Response<Unit>

    @POST("exams/{id}/submit")
    suspend fun submitExam(
        @Header("Authorization") token: String,
        @Path("id") examId: String,
        @Body answers: Map<String, String>
    ): Response<ExamResult>

    @DELETE("wrong-question/{id}")
    suspend fun deleteWrongQuestion(
        @Header("Authorization") token: String,
        @Path("id") questionId: Int
    ): Response<Unit>
}

data class ExamResult(
    val score: Int,
    val total: Int,
    val passed: Boolean
)