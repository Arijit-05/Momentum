package com.arijit.habits

import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arijit.habits.utils.CalendarAdapter
import com.arijit.habits.utils.HabitAdapter
import com.arijit.habits.utils.Vibration
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import android.util.Log
import com.airbnb.lottie.LottieAnimationView
import com.arijit.habits.models.Habit
import java.util.Locale
import com.arijit.habits.models.DateTaskStatus
import com.arijit.habits.utils.ReminderUtils
import android.content.pm.PackageManager
import android.os.Build
import android.widget.LinearLayout
import androidx.core.app.ActivityCompat
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {
    private lateinit var titleTxt: TextView
    private lateinit var wishTxt: TextView
    private lateinit var finishedTasks: TextView
    private lateinit var totalTasks: TextView
    private lateinit var streakDay: TextView
    private lateinit var streakBg: LinearLayout
    private lateinit var lottieFire: LottieAnimationView
    private lateinit var settings: ImageView
    private lateinit var addHabitBtn: CardView
    private lateinit var aiBtn: CardView
    private val allHabits = mutableListOf<Habit>()
    private val filteredallHabits = mutableListOf<Habit>()
    private lateinit var habitAdapter: HabitAdapter
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private var selectedDateString: String? = null
    private lateinit var calendarAdapter: CalendarAdapter
    private val PREFS_NAME = "ai_settings_prefs"
    private val AI_SWITCH_KEY = "ai_switch_enabled"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1001)
            }
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        titleTxt = findViewById(R.id.title_txt)
        wishTxt = findViewById(R.id.wish_txt)
        finishedTasks = findViewById(R.id.finished_tasks)
        totalTasks = findViewById(R.id.total_tasks)
        streakDay = findViewById(R.id.streak_day)
        streakBg = findViewById(R.id.streak_bg)
        aiBtn = findViewById(R.id.ai_btn)
        addHabitBtn = findViewById(R.id.add_habit_btn)
        settings = findViewById(R.id.settings)
        lottieFire = findViewById(R.id.lottie_fire)

        FirebaseDatabase.getInstance().setPersistenceEnabled(true)

        FirebaseFirestore.getInstance().collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                val name = document.getString("name") ?: "User"
                animateTitleSequence(titleTxt, name)
            }

        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        wishTxt.text = when (hour) {
            in 5..11 -> "Good morning ☀️"
            in 12..15 -> "Good afternoon 🌤️"
            in 16..20 -> "Good evening 🌆"
            else -> "Its late, get some rest 🌝"
        }

        aiBtn.setOnClickListener {
            Vibration.vibrate(this, 100)
            startActivity(Intent(this@MainActivity, AiActivity::class.java))
        }

        addHabitBtn.setOnClickListener {
            Vibration.vibrate(this, 100)
            showAddHabitDialog()
        }

        settings.setOnClickListener {
            Vibration.vibrate(this, 50)
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        val recycler = findViewById<RecyclerView>(R.id.calendar_recycler)
        val calendarData = generateCalendarData()
        calendarAdapter = CalendarAdapter(calendarData, { selectedDate ->
            val selectedDateStr = dateFormat.format(selectedDate.date)
            calendarAdapter.updateSelectedDate(selectedDateStr)
            onCalendarDateSelected(selectedDate)
            Vibration.vibrate(this, 50)
        }, selectedDateString)
        recycler.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recycler.adapter = calendarAdapter
        recycler.scrollToPosition(calendarData.size - 1)

        habitAdapter = HabitAdapter(
            filteredallHabits,
            onToggle = { habit ->
                Vibration.vibrate(this, 50)
                val dateStr = selectedDateString ?: dateFormat.format(Calendar.getInstance().time)
                val todayStr = dateFormat.format(Calendar.getInstance().time)

                if (dateStr != todayStr) {
                    Toast.makeText(this, "Can't mark it done for this day", Toast.LENGTH_SHORT).show()
                    return@HabitAdapter
                }

                if (habit.completionDates.contains(dateStr)) {
                    AlertDialog.Builder(this)
                        .setTitle("Undo Habit")
                        .setMessage("Are you sure you want to undo this habit for today?")
                        .setPositiveButton("Yes") { _, _ ->
                            habit.completionDates.remove(dateStr)
                            updateHabit(habit)
                            val date = dateFormat.parse(dateStr)
                            onCalendarDateSelected(DateTaskStatus(date!!, 0, 0))
                        }
                        .setNegativeButton("No", null)
                        .show()
                } else {
                    habit.completionDates.add(dateStr)
                    updateHabit(habit)
                    val date = dateFormat.parse(dateStr)
                    onCalendarDateSelected(DateTaskStatus(date!!, 0, 0))
                }
            },
            onLongPress = { habit -> showEditDeleteDialog(habit) }
        )

        val habitRv = findViewById<RecyclerView>(R.id.habit_recycler)
        habitRv.layoutManager = LinearLayoutManager(this)
        habitRv.adapter = habitAdapter

        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(userId)
            .collection("allHabits")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    Toast.makeText(this, "Failed to fetch allHabits", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                allHabits.clear()

                val fetched = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Habit::class.java)?.apply { id = doc.id }
                }

                val sorted = fetched.sortedBy { it.isDone }
                allHabits.addAll(sorted)
                habitAdapter.notifyDataSetChanged()

                val calendarData = generateCalendarData()
                calendarAdapter.updateData(calendarData)
                selectedDateString?.let {
                    val date = dateFormat.parse(it)
                    calendarAdapter.updateSelectedDate(it)
                    onCalendarDateSelected(DateTaskStatus(date!!, 0, 0))
                }
            }

        val today = Calendar.getInstance().time
        onCalendarDateSelected(DateTaskStatus(today, 0, 0))
    }
    private fun animateTitleSequence(titleTxt: TextView, name: String) {
        titleTxt.animate()
            .alpha(0f)
            .setDuration(500)
            .withEndAction {
                titleTxt.text = "HELLO ${name.uppercase()}"
                titleTxt.animate()
                    .alpha(1f)
                    .setDuration(800)
                    .setStartDelay(200)
                    .withEndAction {
                        titleTxt.animate()
                            .alpha(0f)
                            .setDuration(600)
                            .setStartDelay(400)
                            .withEndAction {
                                titleTxt.text = "MOMENTUM"
                                titleTxt.animate()
                                    .alpha(1f)
                                    .setDuration(800)
                                    .start()
                            }
                            .start()
                    }
                    .start()
            }
            .start()
    }
    private fun generateCalendarData(): List<DateTaskStatus> {
        val calendar = Calendar.getInstance()
        val list = mutableListOf<DateTaskStatus>()
        calendar.add(Calendar.DAY_OF_YEAR, -10)

        for (i in 0..10) {
            val date = calendar.time
            val dateString = dateFormat.format(date)
            val createdHabits = allHabits.filter { habit ->
                if (habit.creationDate.isNullOrEmpty()) return@filter false
                try {
                    val habitCreation = dateFormat.parse(habit.creationDate)
                    habitCreation != null && !habitCreation.after(date)
                } catch (e: Exception) {
                    false
                }
            }

            val totalHabits = createdHabits.size
            val habitsCompleted = createdHabits.count { habit ->
                habit.completionDates.contains(dateString)
            }

            Log.d("CalendarDebug", "date=$dateString, finished=$habitsCompleted, total=$totalHabits")
            list.add(DateTaskStatus(date, habitsCompleted, totalHabits))
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        updateStreak(list)
        return list
    }
    private fun onCalendarDateSelected(selectedDate: DateTaskStatus) {
        val selectedDateStr = dateFormat.format(selectedDate.date)
        selectedDateString = selectedDateStr
        calendarAdapter.updateSelectedDate(selectedDateStr)
        val noTasksText = findViewById<TextView>(R.id.no_tasks_text)

        val filtered = allHabits.filter { habit ->
            if (habit.creationDate.isNullOrEmpty()) return@filter true
            try {
                val created = dateFormat.parse(habit.creationDate)
                created != null && !created.after(selectedDate.date)
            } catch (e: Exception) {
                true
            }
        }.map { habit ->
            habit.copy(isDone = habit.completionDates.contains(selectedDateStr))
        }

        updateAllHabitsForDate(filtered)
        noTasksText.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }
    private fun updateAllHabitsForDate(filtered: List<Habit>) {
        filteredallHabits.clear()
        // Sort: undone habits first, done habits last
        val sorted = filtered.sortedBy { it.isDone }
        filteredallHabits.addAll(sorted)
        habitAdapter.notifyDataSetChanged()

        val finished = filtered.count { it.isDone }
        val total = filtered.size
        finishedTasks.text = finished.toString()
        totalTasks.text = total.toString()
    }
    private fun updateStreak(calendarData: List<DateTaskStatus>) {
        val today = dateFormat.format(Calendar.getInstance().time)
        var streak = 0
        var foundIncomplete = false
        // Find index of today in calendarData
        val todayIndex = calendarData.indexOfLast { dateFormat.format(it.date) == today }
        // Calculate streak up to yesterday
        for (i in todayIndex - 1 downTo 0) {
            val date = calendarData[i]
            val isComplete = date.totalTasks > 0 && date.finishedTasks == date.totalTasks
            if (isComplete) {
                streak++
            } else {
                foundIncomplete = true
                break
            }
        }
        // Check if today is also completed
        if (todayIndex >= 0) {
            val todayData = calendarData[todayIndex]
            val isTodayComplete = todayData.totalTasks > 0 && todayData.finishedTasks == todayData.totalTasks
            if (isTodayComplete) {
                streak++
            }
        }
        streakDay.text = streak.toString()

        if (streakDay.text.toString() == "0") {
            lottieFire.pauseAnimation()
        } else if (streakDay.text.toString() == "1") {
            streakBg.setBackgroundResource(R.color.streak_yellow)
            lottieFire.resumeAnimation()
        } else {
            streakBg.setBackgroundResource(R.color.streak_yellow)
            lottieFire.resumeAnimation()
        }
    }
    private fun showAddHabitDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_habit, null)
        val etEmoji = dialogView.findViewById<EditText>(R.id.et_emoji)
        val etHabitName = dialogView.findViewById<EditText>(R.id.et_habit_name)
        val switchReminder = dialogView.findViewById<MaterialSwitch>(R.id.switch_reminder)
        val timePicker = dialogView.findViewById<CardView>(R.id.reminder_time)
        val timePickerTxt = dialogView.findViewById<TextView>(R.id.time_picker_txt)
        var pickedTime: String? = null

        switchReminder.setOnCheckedChangeListener { _, isChecked ->
            timePicker.visibility = if (isChecked) View.VISIBLE else View.GONE
            timePicker.setOnClickListener {
                Vibration.vibrate(this, 50)
                val cal = Calendar.getInstance()
                val hour = cal.get(Calendar.HOUR_OF_DAY)
                val minute = cal.get(Calendar.MINUTE)
                TimePickerDialog(this, { _, selectedHour, selectedMinute ->
                    pickedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
                    timePickerTxt.text = pickedTime
                }, hour, minute, true).show()
            }
        }

        AlertDialog.Builder(this)
            .setTitle("Add Habit")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val name = etHabitName.text.toString().trim()
                val hasReminder = switchReminder.isChecked
                val emoji = etEmoji.text.toString().trim().takeIf { it.isNotEmpty() } ?: "✅"
                val time = pickedTime ?: ""

                if (name.isEmpty()) {
                    Toast.makeText(this, "Habit name can't be empty", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val creationDateStr = dateFormat.format(Calendar.getInstance().time)
                val habit = Habit(
                    name = name,
                    emoji = emoji,
                    isDone = false,
                    hasReminder = hasReminder,
                    reminderTime = if (hasReminder) time else "",
                    creationDate = creationDateStr
                )

                saveHabitToFirebase(habit)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    private fun saveHabitToFirebase(habit: Habit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("users").document(userId)
            .collection("allHabits").document()
        habit.id = docRef.id

        docRef.set(habit)
            .addOnSuccessListener {
                ReminderUtils.scheduleHabitReminder(this, habit)
                Toast.makeText(this, "Habit added", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to add habit", Toast.LENGTH_SHORT).show()
            }
    }
    private fun updateHabit(habit: Habit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()
        if (habit.completionDates == null) {
            habit.completionDates = mutableListOf()
        }
        Log.d("HabitUpdate", "Updating habit: ${habit.name}, completionDates: ${habit.completionDates}")
        db.collection("users").document(userId)
            .collection("allHabits").document(habit.id)
            .set(habit)
            .addOnSuccessListener {
                ReminderUtils.scheduleHabitReminder(this, habit)
                selectedDateString?.let { dateStr ->
                    val cal = Calendar.getInstance()
                    val date = try { dateFormat.parse(dateStr) } catch (e: Exception) { null }
                    if (date != null) {
                        onCalendarDateSelected(DateTaskStatus(date, 0, 0))
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to update habit", Toast.LENGTH_SHORT).show()
            }
    }
    private fun showEditDeleteDialog(habit: Habit) {
        val options = arrayOf("Edit", "Delete")
        AlertDialog.Builder(this)
            .setTitle("Choose an option")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showEditHabitDialog(habit)
                    1 -> confirmDeleteHabit(habit)
                }
            }
            .show()
    }
    private fun confirmDeleteHabit(habit: Habit) {
        AlertDialog.Builder(this)
            .setTitle("Delete Habit?")
            .setMessage("Are you sure you want to delete this habit? Deleting this habit will delete all records for the past days")
            .setPositiveButton("Yes") { _, _ ->
                deleteHabit(habit)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    private fun deleteHabit(habit: Habit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance()
            .collection("users").document(userId)
            .collection("allHabits").document(habit.id)
            .delete()
            .addOnSuccessListener {
                val index = allHabits.indexOf(habit)
                if (index != -1) {
                    allHabits.removeAt(index)
                    habitAdapter.notifyItemRemoved(index)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to delete habit", Toast.LENGTH_SHORT).show()
            }
    }
    private fun showEditHabitDialog(habit: Habit) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_habit, null)
        val etHabitName = dialogView.findViewById<EditText>(R.id.et_habit_name)
        val switchReminder = dialogView.findViewById<MaterialSwitch>(R.id.switch_reminder)
        val timePicker = dialogView.findViewById<CardView>(R.id.reminder_time)
        val timePickerTxt = dialogView.findViewById<TextView>(R.id.time_picker_txt)
        val etEmoji = dialogView.findViewById<EditText>(R.id.et_emoji)
        etEmoji.setText(habit.emoji)

        etHabitName.setText(habit.name)
        switchReminder.isChecked = habit.hasReminder
        var pickedTime: String? = habit.reminderTime

        if (habit.hasReminder) {
            timePicker.visibility = View.VISIBLE
            timePickerTxt.text = pickedTime
        } else {
            timePicker.visibility = View.GONE
        }

        switchReminder.setOnCheckedChangeListener { _, isChecked ->
            timePicker.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        timePicker.setOnClickListener {
            Vibration.vibrate(this, 50)
            val cal = Calendar.getInstance()
            val hour = cal.get(Calendar.HOUR_OF_DAY)
            val minute = cal.get(Calendar.MINUTE)
            TimePickerDialog(this, { _, selectedHour, selectedMinute ->
                pickedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
                timePickerTxt.text = pickedTime
            }, hour, minute, true).show()
        }

        AlertDialog.Builder(this)
            .setTitle("Edit Habit")
            .setView(dialogView)
            .setPositiveButton("Update") { _, _ ->
                val newName = etHabitName.text.toString().trim()
                val newEmoji = etEmoji.text.toString().trim().takeIf { it.isNotEmpty() } ?: "✅"
                val newReminder = switchReminder.isChecked
                val newTime = pickedTime ?: ""

                if (newName.isEmpty()) {
                    Toast.makeText(this, "Habit name can't be empty", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                habit.name = newName
                habit.emoji = newEmoji
                habit.hasReminder = newReminder
                habit.reminderTime = if (newReminder) newTime else ""

                updateHabit(habit)
                Toast.makeText(this, "Habit updated", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    private fun loadAiSwitchState(): Boolean {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        return prefs.getBoolean(AI_SWITCH_KEY, true)
    }

    override fun onResume() {
        super.onResume()
        if (loadAiSwitchState()) {
            aiBtn.visibility = View.VISIBLE
        } else {
            aiBtn.visibility = View.GONE
        }
    }
}