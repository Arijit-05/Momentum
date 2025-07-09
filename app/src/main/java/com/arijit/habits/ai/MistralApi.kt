package com.arijit.habits.ai

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Header

interface MistralApi {
    @POST("v1/chat/completions")
    fun getMistralResponse(
        @Header("Authorization") authHeader: String,
        @Body request: MistralRequest
    ): Call<MistralResponse>
}