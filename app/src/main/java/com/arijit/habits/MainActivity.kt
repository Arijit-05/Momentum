package com.arijit.habits

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
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
import com.arijit.habits.utils.ReminderReceiver
import com.arijit.habits.utils.Vibration
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import android.util.Log
import com.airbnb.lottie.LottieAnimationView
import com.arijit.habits.models.CalendarDate
import com.arijit.habits.models.Habit
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var titleTxt: TextView
    private lateinit var wishTxt: TextView
    private lateinit var finishedTasks: TextView
    private lateinit var totalTasks: TextView
    private lateinit var streakDay: TextView
    private lateinit var lottieFire: LottieAnimationView
    private lateinit var settings: ImageView
    private lateinit var addHabitBtn: CardView
    private val allHabits = mutableListOf<Habit>()
    private val filteredallHabits = mutableListOf<Habit>()
    private lateinit var habitAdapter: HabitAdapter
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private var selectedDateString: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
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
        addHabitBtn = findViewById(R.id.add_habit_btn)
        settings = findViewById(R.id.settings)
        lottieFire = findViewById(R.id.lottie_fire)

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
        val calendarAdapter = CalendarAdapter(calendarData) { selectedDate ->
            onCalendarDateSelected(selectedDate)
        }

        recycler.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recycler.adapter = calendarAdapter
        recycler.scrollToPosition(calendarData.size - 1)

        habitAdapter = HabitAdapter(
            filteredallHabits,
            onToggle = { habit ->
                val dateStr = selectedDateString ?: dateFormat.format(Calendar.getInstance().time)
                val todayStr = dateFormat.format(Calendar.getInstance().time)

                if (dateStr != todayStr) {
                    Toast.makeText(this, "Can't mark it done for this day", Toast.LENGTH_SHORT).show()
                    return@HabitAdapter
                }

                if (habit.completionDates.contains(dateStr)) {
                    habit.completionDates.remove(dateStr)
                } else {
                    habit.completionDates.add(dateStr)
                }

                updateHabit(habit)
                val date = dateFormat.parse(dateStr)
                onCalendarDateSelected(CalendarDate(date!!, false))
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
                    onCalendarDateSelected(CalendarDate(date!!, false))
                }
            }

        val today = Calendar.getInstance().time
        onCalendarDateSelected(CalendarDate(today, false))
    }

    private fun fetchallHabitsFromFirebase() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("users").document(userId)
            .collection("allHabits")
            .get()
            .addOnSuccessListener { querySnapshot ->
                allHabits.clear()

                val fetched = querySnapshot.documents.mapNotNull { doc ->
                    doc.toObject(Habit::class.java)?.apply {
                        id = doc.id
                    }
                }

                val sorted = fetched.sortedBy { it.isDone }
                allHabits.addAll(sorted)
                habitAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to fetch allHabits", Toast.LENGTH_SHORT).show()
            }
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
    private fun generateCalendarData(): List<CalendarDate> {
        val calendar = Calendar.getInstance()
        val list = mutableListOf<CalendarDate>()
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

            val isComplete = createdHabits.isNotEmpty() && createdHabits.all { it.completionDates.contains(dateString) }
            list.add(CalendarDate(date, isComplete))
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        updateStreak(list)
        return list
    }
    private fun onCalendarDateSelected(selectedDate: CalendarDate) {
        val selectedDateStr = dateFormat.format(selectedDate.date)
        selectedDateString = selectedDateStr
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

        updateallHabitsForDate(filtered)
        noTasksText.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }
    private fun updateallHabitsForDate(filtered: List<Habit>) {
        filteredallHabits.clear()
        filteredallHabits.addAll(filtered)
        habitAdapter.notifyDataSetChanged()

        val finished = filtered.count { it.isDone }
        val total = filtered.size
        finishedTasks.text = finished.toString()
        totalTasks.text = total.toString()
    }
    private fun updateStreak(calendarData: List<CalendarDate>) {
        var streak = 0
        val today = dateFormat.format(Calendar.getInstance().time)
        for (date in calendarData.reversed()) {
            val dateString = dateFormat.format(date.date)
            if (dateString > today) continue
            if (date.isComplete) streak++ else break
        }
        streakDay.text = streak.toString()

//        if (streakDay.text.toString() == "0") {
//            lottieFire.pauseAnimation()
//        } else {
//            lottieFire.resumeAnimation()
//        }
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

                val habit = Habit(
                    name = name,
                    emoji = emoji,
                    isDone = false,
                    hasReminder = hasReminder,
                    reminderTime = if (hasReminder) time else ""
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
                scheduleHabitReminder(habit)
                Toast.makeText(this, "Habit added", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to add habit", Toast.LENGTH_SHORT).show()
            }
    }
    private fun scheduleHabitReminder(habit: Habit) {
        if (!habit.hasReminder || habit.reminderTime.isEmpty()) return

        val timeParts = habit.reminderTime.split(":")
        val hour = timeParts[0].toInt()
        val minute = timeParts[1].toInt()

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }

        val intent = Intent(this, ReminderReceiver::class.java).apply {
            putExtra("habitName", habit.name)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            habit.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }
    private fun updateHabit(habit: Habit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()
        // Ensure completionDates is always present
        if (habit.completionDates == null) {
            habit.completionDates = mutableListOf()
        }
        Log.d("HabitUpdate", "Updating habit: ${habit.name}, completionDates: ${habit.completionDates}")
        db.collection("users").document(userId)
            .collection("allHabits").document(habit.id)
            .set(habit)
            .addOnSuccessListener {
                // After updating, re-fetch allHabits for the selected date
                selectedDateString?.let { dateStr ->
                    // Find the CalendarDate for the selected string
                    val cal = Calendar.getInstance()
                    val date = try { dateFormat.parse(dateStr) } catch (e: Exception) { null }
                    if (date != null) {
                        onCalendarDateSelected(CalendarDate(date, false))
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
            .setMessage("Are you sure you want to delete this habit?")
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
}