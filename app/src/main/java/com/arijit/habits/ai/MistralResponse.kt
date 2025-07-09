package com.arijit.habits.ai

data class MistralResponse(
    val choices: List<Choice>
)

data class Choice(
    val message: Message
)
