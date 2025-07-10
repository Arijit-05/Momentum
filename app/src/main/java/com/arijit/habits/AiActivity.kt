package com.arijit.habits

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
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
import com.google.firebase.firestore.FirebaseFirestore
import com.arijit.habits.utils.AiHabit
import com.arijit.habits.utils.AiHabitAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.arijit.habits.models.Habit
import com.google.firebase.auth.FirebaseAuth

class AiActivity : AppCompatActivity() {
    private lateinit var questionsCard: CardView
    private lateinit var aiReplyRv: RecyclerView
    private lateinit var replyLayout: LinearLayout
    private lateinit var addToHabits: CardView
    private lateinit var clearBtn: CardView
    private var lastResult: String? = null
    private var currentAiHabits: List<AiHabit> = emptyList()
    private val addedHabitKeys = mutableSetOf<String>()
    private var aiAdapter: AiHabitAdapter? = null

    private val PREFS_NAME = "ai_habits_prefs"
    private val HABITS_KEY = "ai_habits_list"
    private val ADDED_KEY = "added_ai_habits"
    private val gson = Gson()

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

        // Load saved habits if present
        replyLayout = findViewById(R.id.reply_layout)
        aiReplyRv = findViewById(R.id.ai_reply_rv)
        addToHabits = findViewById(R.id.add_to_habits_card)
        clearBtn = findViewById(R.id.clear_btn)

        // Load added habit keys from prefs
        addedHabitKeys.clear()
        addedHabitKeys.addAll(loadAddedHabitKeysFromPrefs())

        val savedHabits = loadHabitsFromPrefs()
        if (savedHabits.isNotEmpty()) {
            currentAiHabits = savedHabits
            showReplyLayout()
            aiReplyRv.layoutManager = LinearLayoutManager(this@AiActivity)
            aiAdapter = AiHabitAdapter(savedHabits, { aiHabit ->
                val key = aiHabit.emoji + "|" + aiHabit.name
                if (addedHabitKeys.contains(key)) {
                    Toast.makeText(this, "Already added", Toast.LENGTH_SHORT).show()
                } else {
                    showAddHabitDialog(aiHabit)
                }
            }, addedHabitKeys)
            aiReplyRv.adapter = aiAdapter
        } else {
            showQuestionsCard()
        }

        clearBtn.setOnClickListener {
            crossfadeViews(replyLayout, questionsCard)
            clearHabitsFromPrefs()
        }

        addToHabits.setOnClickListener {
            if (currentAiHabits.isNotEmpty()) {
                androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Add all to habits?")
                    .setMessage("Are you sure you want to add all these habits to your list?")
                    .setPositiveButton("Yes") { _, _ ->
                        addAllAiHabitsToFirestore(currentAiHabits)
                    }
                    .setNegativeButton("No", null)
                    .show()
            }
        }

        if (StateManager.triggerAi == true) {
            questionsCard.visibility = View.GONE
            val result: String = intent.getStringExtra("result") ?: ""
            lastResult = result
            Log.d("RecResult", result)
            getAiHabits(result)
        }
    }

    private fun crossfadeViews(hideView: View, showView: View) {
        hideView.animate().alpha(0f).setDuration(200).withEndAction {
            hideView.visibility = View.GONE
            showView.alpha = 0f
            showView.visibility = View.VISIBLE
            showView.animate().alpha(1f).setDuration(200).start()
        }.start()
    }

    private fun showReplyLayout() {
        replyLayout.visibility = View.VISIBLE
        replyLayout.alpha = 1f
        questionsCard.visibility = View.GONE
    }

    private fun showQuestionsCard() {
        questionsCard.visibility = View.VISIBLE
        questionsCard.alpha = 1f
        replyLayout.visibility = View.GONE
    }

    private fun saveHabitsToPrefs(habits: List<AiHabit>) {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val json = gson.toJson(habits)
        prefs.edit().putString(HABITS_KEY, json).apply()
    }

    private fun loadHabitsFromPrefs(): List<AiHabit> {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val json = prefs.getString(HABITS_KEY, null) ?: return emptyList()
        val type = object : TypeToken<List<AiHabit>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    private fun saveAddedHabitKeysToPrefs() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        prefs.edit().putStringSet(ADDED_KEY, addedHabitKeys).apply()
    }

    private fun loadAddedHabitKeysFromPrefs(): Set<String> {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        return prefs.getStringSet(ADDED_KEY, emptySet()) ?: emptySet()
    }

    private fun clearHabitsFromPrefs() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        prefs.edit().remove(HABITS_KEY).apply()
        prefs.edit().remove(ADDED_KEY).apply()
        addedHabitKeys.clear()
    }

    private fun getAiHabits(result: String) {
        FirebaseFirestore.getInstance()
            .collection("config")
            .document("openrouter")
            .get()
            .addOnSuccessListener { document ->
                val apiKey = document.getString("apiKey")
                if (apiKey.isNullOrEmpty()) {
                    Log.e("MistralAI", "API key not found in Firestore")
                    return@addOnSuccessListener
                }
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
                val authHeader = "Bearer $apiKey"
                api.getMistralResponse(authHeader, request)
                    .enqueue(object : Callback<MistralResponse> {
                        override fun onResponse(
                            call: Call<MistralResponse>,
                            response: Response<MistralResponse>
                        ) {
                            val reply = response.body()?.choices?.firstOrNull()?.message?.content
                            Log.d("MistralAI", "AI Suggestion: $reply")

                            questionsCard.visibility = View.GONE
                            replyLayout = findViewById(R.id.reply_layout)
                            replyLayout.visibility = View.VISIBLE
                            aiReplyRv = findViewById(R.id.ai_reply_rv)
                            addToHabits = findViewById(R.id.add_to_habits_card)

                            if (!reply.isNullOrEmpty()) {
                                val habits = reply.lines()
                                    .mapNotNull { line ->
                                        val trimmed = line.trim()
                                        if (trimmed.isEmpty()) return@mapNotNull null
                                        val emoji = trimmed.takeWhile { !it.isWhitespace() }
                                        val name = trimmed.dropWhile { !it.isWhitespace() }.trim()
                                        if (emoji.isNotEmpty() && name.isNotEmpty()) AiHabit(
                                            emoji,
                                            name
                                        ) else null
                                    }
                                currentAiHabits = habits
                                aiReplyRv.layoutManager = LinearLayoutManager(this@AiActivity)
                                aiAdapter = AiHabitAdapter(habits, { aiHabit ->
                                    val key = aiHabit.emoji + "|" + aiHabit.name
                                    if (addedHabitKeys.contains(key)) {
                                        Toast.makeText(baseContext, "Already added", Toast.LENGTH_SHORT).show()
                                    } else {
                                        showAddHabitDialog(aiHabit)
                                    }
                                }, addedHabitKeys)
                                aiReplyRv.adapter = aiAdapter
                                saveHabitsToPrefs(habits)
                                showReplyLayout()
                            }
                        }

                        override fun onFailure(call: Call<MistralResponse>, t: Throwable) {
                            Toast.makeText(
                                baseContext,
                                "Error getting response ${t.message}",
                                Toast.LENGTH_LONG
                            ).show()
                            Log.e("MistralAI", "Error: ${t.message}")
                        }
                    })
            }
            .addOnFailureListener { exception ->
                Log.e("MistralAI", "Failed to fetch API key: ${exception.message}")
            }
    }

    // Add a single AiHabit to Firestore as a Habit
    private fun showAddHabitDialog(aiHabit: AiHabit) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Add to habits?")
            .setMessage("Do you want to add '${aiHabit.emoji} ${aiHabit.name}' to your habits?")
            .setPositiveButton("Yes") { _, _ ->
                addAiHabitToFirestore(aiHabit)
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun addAiHabitToFirestore(aiHabit: AiHabit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()
        // Prevent duplicate by name+emoji
        db.collection("users").document(userId)
            .collection("allHabits")
            .whereEqualTo("name", aiHabit.name)
            .whereEqualTo("emoji", aiHabit.emoji)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    val habit = Habit(
                        name = aiHabit.name,
                        emoji = aiHabit.emoji,
                        isDone = false,
                        hasReminder = false,
                        reminderTime = "",
                        creationDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Calendar.getInstance().time)
                    )
                    val docRef = db.collection("users").document(userId)
                        .collection("allHabits").document()
                    habit.id = docRef.id
                    docRef.set(habit)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Habit added", Toast.LENGTH_SHORT).show()
                            // Mark as added in the set and update adapter
                            val key = aiHabit.emoji + "|" + aiHabit.name
                            addedHabitKeys.add(key)
                            saveAddedHabitKeysToPrefs()
                            aiAdapter?.notifyDataSetChanged()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed to add habit", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "Habit already exists", Toast.LENGTH_SHORT).show()
                    // Mark as added in the set and update adapter
                    val key = aiHabit.emoji + "|" + aiHabit.name
                    addedHabitKeys.add(key)
                    saveAddedHabitKeysToPrefs()
                    aiAdapter?.notifyDataSetChanged()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to check for duplicates", Toast.LENGTH_SHORT).show()
            }
    }

    // Add all AiHabits to Firestore as Habits
    private fun addAllAiHabitsToFirestore(aiHabits: List<AiHabit>) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()
        val dateStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Calendar.getInstance().time)
        aiHabits.forEach { aiHabit ->
            db.collection("users").document(userId)
                .collection("allHabits")
                .whereEqualTo("name", aiHabit.name)
                .whereEqualTo("emoji", aiHabit.emoji)
                .get()
                .addOnSuccessListener { snapshot ->
                    val key = aiHabit.emoji + "|" + aiHabit.name
                    if (snapshot.isEmpty) {
                        val habit = Habit(
                            name = aiHabit.name,
                            emoji = aiHabit.emoji,
                            isDone = false,
                            hasReminder = false,
                            reminderTime = "",
                            creationDate = dateStr
                        )
                        val docRef = db.collection("users").document(userId)
                            .collection("allHabits").document()
                        habit.id = docRef.id
                        docRef.set(habit)
                    }
                    // Mark as added in the set and update adapter
                    addedHabitKeys.add(key)
                    saveAddedHabitKeysToPrefs()
                    aiAdapter?.notifyDataSetChanged()
                }
        }
        Toast.makeText(this, "All habits added (duplicates skipped)", Toast.LENGTH_SHORT).show()
    }
}