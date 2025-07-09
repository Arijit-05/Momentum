package com.arijit.habits.ai

data class MistralRequest(
    val model: String = "mistralai/mistral-small",
    val messages: List<Message>,
    val temperature: Double = 0.7
)

data class Message(
    val role: String,
    val content: String
)

