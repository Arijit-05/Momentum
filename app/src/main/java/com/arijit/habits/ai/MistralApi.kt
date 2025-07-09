package com.arijit.habits.ai

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface MistralApi {
    @Headers(
        "Authorization: Bearer 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",
        "Content-Type: application/json"
    )
    @POST("v1/chat/completions")
    fun getMistralResponse(@Body request: MistralRequest): Call<MistralResponse>
}