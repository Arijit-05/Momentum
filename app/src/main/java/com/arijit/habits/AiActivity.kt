package com.arijit.habits

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.arijit.habits.ai.Message
import com.arijit.habits.ai.MistralApi
import com.arijit.habits.ai.MistralRequest
import com.arijit.habits.ai.MistralResponse
import com.arijit.habits.utils.StateManager
import com.arijit.habits.utils.Vibration
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.Call
import retrofit2.Response
import retrofit2.converter.gson.GsonConverterFactory

class AiActivity : AppCompatActivity() {
    private lateinit var questionsCard: CardView
    private lateinit var resetBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_ai)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        questionsCard = findViewById(R.id.questions_card)
        questionsCard.setOnClickListener {
            Vibration.vibrate(this, 50)
            startActivity(Intent(this, QuestionsActivity::class.java))
            finish()
        }

        resetBtn = findViewById(R.id.reset)
        resetBtn.setOnClickListener { questionsCard.visibility = View.VISIBLE }

        if (StateManager.triggerAi == true) {
            questionsCard.visibility = View.GONE

            val result: String = intent.getStringExtra("result") ?: ""
            Log.d("RecResult", result)

            val retrofit = Retrofit.Builder()
                .baseUrl("https://openrouter.ai/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val api = retrofit.create(MistralApi::class.java)
            val userPrompt = result.trimIndent()

            val messages = listOf(
                Message(
                    "system",
                    "You are a helpful assistant that suggests useful, real-life daily habits. \n" +
                            "        Output should be **only a list of 5 habits**, each on a new line, \n" +
                            "        in the format: emoji + concise habit name (e.g. \uD83E\uDDD8\u200D♂\uFE0F Meditation).\n" +
                            "        Do NOT include numbering, titles, or any extra text.\n" +
                            "        These habits will be shown directly in a RecyclerView in an Android app."
                ),
                Message("user", userPrompt)
            )

            val request = MistralRequest(messages = messages)
            api.getMistralResponse(request).enqueue(object: Callback<MistralResponse> {
                override fun onResponse(call: Call<MistralResponse>, response: Response<MistralResponse>) {
                    val reply = response.body()?.choices?.firstOrNull()?.message?.content
                    Log.d("MistralAI", "AI Suggestion: $reply")
                }

                override fun onFailure(call: Call<MistralResponse>, t: Throwable) {
                    Log.e("MistralAI", "Error: ${t.message}")
                }
            })
        }

    }
}