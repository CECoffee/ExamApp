package dev.coffee.examapp.network

import dev.coffee.examapp.model.Exam
import dev.coffee.examapp.model.Question
import dev.coffee.examapp.model.WrongQuestion
import retrofit2.Response
import retrofit2.http.*
import retrofit2.http.Header
import retrofit2.http.Body
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @GET("/question/{id}")
    suspend fun getQuestion(@Path("id") id: Int): Response<Question>

    @POST("/question/{id}/submit")
    suspend fun submitAnswer(
        @Path("id") id: Int,
        @Body answer: String
    ): Response<Unit>

    @GET("exams")
    suspend fun getExams( @Query("status") status: String? = null ): Response<List<Exam>>

    @GET("exams/{id}/start")
    suspend fun startExam( @Path("id") examId: String ): Response<Unit>

    @POST("exams/{id}/submit")
    suspend fun submitExam(
        @Path("id") examId: Int,
        @Body score: Double
    ): Response<Unit>

    @GET("wrong-questions")
    suspend fun getWrongQuestions(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): Response<List<WrongQuestion>>

    @DELETE("wrong-question/{id}")
    suspend fun deleteWrongQuestion( @Path("id") questionId: Int ): Response<Unit>
}
