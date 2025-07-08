package com.arijit.habits

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.google.android.material.textfield.TextInputEditText
import com.arijit.habits.utils.Vibration
import android.widget.LinearLayout
import android.widget.RelativeLayout

class QuestionsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_questions)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Q1: Multi-select, max 3
        val qn1Ids = listOf(R.id.qn1a, R.id.qn1b, R.id.qn1c, R.id.qn1d, R.id.qn1e)
        val qn1Selected = mutableSetOf<Int>()
        qn1Ids.forEach { id ->
            val card = findViewById<CardView>(id)
            val rl = findViewById<RelativeLayout>(resources.getIdentifier("${resources.getResourceEntryName(id)}_rl", "id", packageName))
            val tv = rl.getChildAt(0) as TextView
            card.setOnClickListener {
                Vibration.vibrate(this, 50)
                if (qn1Selected.contains(id)) {
                    qn1Selected.remove(id)
                    rl.setBackgroundColor(getColor(R.color.dark_grey))
                    tv.setTextColor(getColor(R.color.white))
                } else {
                    if (qn1Selected.size < 3) {
                        qn1Selected.add(id)
                        rl.setBackgroundColor(getColor(R.color.white))
                        tv.setTextColor(getColor(R.color.black))
                    }
                }
            }
        }

        // Q2: Single select
        val qn2Ids = listOf(R.id.qn2a, R.id.qn2b, R.id.qn2c, R.id.qn2d)
        var qn2Selected: Int? = null
        qn2Ids.forEach { id ->
            val card = findViewById<CardView>(id)
            val rl = findViewById<RelativeLayout>(resources.getIdentifier("${resources.getResourceEntryName(id)}_rl", "id", packageName))
            val tv = rl.getChildAt(0) as TextView
            card.setOnClickListener {
                Vibration.vibrate(this, 50)
                qn2Ids.forEach { otherId ->
                    val otherRl = findViewById<RelativeLayout>(resources.getIdentifier("${resources.getResourceEntryName(otherId)}_rl", "id", packageName))
                    val otherTv = otherRl.getChildAt(0) as TextView
                    otherRl.setBackgroundColor(getColor(R.color.dark_grey))
                    otherTv.setTextColor(getColor(R.color.white))
                }
                rl.setBackgroundColor(getColor(R.color.white))
                tv.setTextColor(getColor(R.color.black))
                qn2Selected = id
            }
        }

        // Q3: Single select
        val qn3Ids = listOf(R.id.qn3a, R.id.qn3b, R.id.qn3c, R.id.qn3d)
        var qn3Selected: Int? = null
        qn3Ids.forEach { id ->
            val card = findViewById<CardView>(id)
            val rl = findViewById<RelativeLayout>(resources.getIdentifier("${resources.getResourceEntryName(id)}_rl", "id", packageName))
            val tv = rl.getChildAt(0) as TextView
            card.setOnClickListener {
                Vibration.vibrate(this, 50)
                qn3Ids.forEach { otherId ->
                    val otherRl = findViewById<RelativeLayout>(resources.getIdentifier("${resources.getResourceEntryName(otherId)}_rl", "id", packageName))
                    val otherTv = otherRl.getChildAt(0) as TextView
                    otherRl.setBackgroundColor(getColor(R.color.dark_grey))
                    otherTv.setTextColor(getColor(R.color.white))
                }
                rl.setBackgroundColor(getColor(R.color.white))
                tv.setTextColor(getColor(R.color.black))
                qn3Selected = id
            }
        }

        // Q4: Multi-select, no max, but 'None' is exclusive
        val qn4Ids = listOf(R.id.qn4a, R.id.qn4b, R.id.qn4c, R.id.qn4d, R.id.qn4e)
        val qn4Selected = mutableSetOf<Int>()
        val qn4NoneId = R.id.qn4e
        qn4Ids.forEach { id ->
            val card = findViewById<CardView>(id)
            val rl = findViewById<RelativeLayout>(resources.getIdentifier("${resources.getResourceEntryName(id)}_rl", "id", packageName))
            val tv = rl.getChildAt(0) as TextView
            card.setOnClickListener {
                Vibration.vibrate(this, 50)
                if (id == qn4NoneId) {
                    // If 'None' is selected, deselect all others
                    qn4Ids.forEach { otherId ->
                        val otherRl = findViewById<RelativeLayout>(resources.getIdentifier("${resources.getResourceEntryName(otherId)}_rl", "id", packageName))
                        val otherTv = otherRl.getChildAt(0) as TextView
                        if (otherId == qn4NoneId) {
                            otherRl.setBackgroundColor(getColor(R.color.white))
                            otherTv.setTextColor(getColor(R.color.black))
                        } else {
                            otherRl.setBackgroundColor(getColor(R.color.dark_grey))
                            otherTv.setTextColor(getColor(R.color.white))
                        }
                    }
                    qn4Selected.clear()
                    qn4Selected.add(qn4NoneId)
                } else {
                    // If any other is selected, deselect 'None'
                    val noneRl = findViewById<RelativeLayout>(resources.getIdentifier("${resources.getResourceEntryName(qn4NoneId)}_rl", "id", packageName))
                    val noneTv = noneRl.getChildAt(0) as TextView
                    noneRl.setBackgroundColor(getColor(R.color.dark_grey))
                    noneTv.setTextColor(getColor(R.color.white))
                    qn4Selected.remove(qn4NoneId)

                    if (qn4Selected.contains(id)) {
                        qn4Selected.remove(id)
                        rl.setBackgroundColor(getColor(R.color.dark_grey))
                        tv.setTextColor(getColor(R.color.white))
                    } else {
                        qn4Selected.add(id)
                        rl.setBackgroundColor(getColor(R.color.white))
                        tv.setTextColor(getColor(R.color.black))
                    }
                }
            }
        }

        // Q5: Single select
        val qn5Ids = listOf(R.id.qn5a, R.id.qn5b, R.id.qn5c)
        var qn5Selected: Int? = null
        qn5Ids.forEach { id ->
            val card = findViewById<CardView>(id)
            val rl = findViewById<RelativeLayout>(resources.getIdentifier("${resources.getResourceEntryName(id)}_rl", "id", packageName))
            val tv = rl.getChildAt(0) as TextView
            card.setOnClickListener {
                Vibration.vibrate(this, 50)
                qn5Ids.forEach { otherId ->
                    val otherRl = findViewById<RelativeLayout>(resources.getIdentifier("${resources.getResourceEntryName(otherId)}_rl", "id", packageName))
                    val otherTv = otherRl.getChildAt(0) as TextView
                    otherRl.setBackgroundColor(getColor(R.color.dark_grey))
                    otherTv.setTextColor(getColor(R.color.white))
                }
                rl.setBackgroundColor(getColor(R.color.white))
                tv.setTextColor(getColor(R.color.black))
                qn5Selected = id
            }
        }

        val submitCard = (findViewById<LinearLayout>(R.id.questions_layout)).getChildAt(
            (findViewById<LinearLayout>(R.id.questions_layout)).childCount - 2
        ) as CardView
        submitCard.setOnClickListener {
            Vibration.vibrate(this, 50)
            // Q1 selected texts
            val q1Texts = qn1Selected.map { id ->
                val rl = findViewById<RelativeLayout>(resources.getIdentifier("${resources.getResourceEntryName(id)}_rl", "id", packageName))
                val tv = rl.getChildAt(0) as TextView
                tv.text.toString()
            }
            // Q2 selected text
            val q2Text = qn2Selected?.let { id ->
                val rl = findViewById<RelativeLayout>(resources.getIdentifier("${resources.getResourceEntryName(id)}_rl", "id", packageName))
                val tv = rl.getChildAt(0) as TextView
                tv.text.toString()
            } ?: ""
            // Q3 selected text
            val q3Text = qn3Selected?.let { id ->
                val rl = findViewById<RelativeLayout>(resources.getIdentifier("${resources.getResourceEntryName(id)}_rl", "id", packageName))
                val tv = rl.getChildAt(0) as TextView
                tv.text.toString()
            } ?: ""
            // Q4 selected texts
            val q4Texts = qn4Selected.map { id ->
                val rl = findViewById<RelativeLayout>(resources.getIdentifier("${resources.getResourceEntryName(id)}_rl", "id", packageName))
                val tv = rl.getChildAt(0) as TextView
                tv.text.toString()
            }
            // Q5 selected text
            val q5Text = qn5Selected?.let { id ->
                val rl = findViewById<RelativeLayout>(resources.getIdentifier("${resources.getResourceEntryName(id)}_rl", "id", packageName))
                val tv = rl.getChildAt(0) as TextView
                tv.text.toString()
            } ?: ""
            val anythingElse = findViewById<TextInputEditText>(R.id.anything_else).text?.toString() ?: ""
            val result = """
                |I want to ${q1Texts.joinToString(", ")}
                |I can allot $q2Text everyday
                |I prefer doing my habits during $q3Text
                |My bad habits are ${q4Texts.joinToString(", ")}
                |I am currently a $q5Text
                |Additionally, $anythingElse
            """.trimMargin()
            Log.d("Qnresult", result)
        }
    }
}