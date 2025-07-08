package com.arijit.habits

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.arijit.habits.utils.Vibration

class AiActivity : AppCompatActivity() {
    private lateinit var questionsCard: CardView

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
    }
}