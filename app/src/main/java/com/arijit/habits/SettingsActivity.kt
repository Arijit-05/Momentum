package com.arijit.habits

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.arijit.habits.utils.Vibration
import com.google.firebase.auth.FirebaseAuth
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.net.Uri
import android.widget.EditText
import android.widget.Toast
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.firebase.firestore.FirebaseFirestore

class SettingsActivity : AppCompatActivity() {
    private lateinit var logoutBtn: CardView
    private lateinit var editName: CardView
    private lateinit var github: CardView
    private lateinit var projects: CardView
    private lateinit var aiSwitch: MaterialSwitch
    private val PREFS_NAME = "ai_settings_prefs"
    private val AI_SWITCH_KEY = "ai_switch_enabled"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        logoutBtn = findViewById(R.id.logout_btn)
        logoutBtn.setOnClickListener {
            Vibration.vibrate(this, 100)
            AlertDialog.Builder(this)
                .setTitle("Log out")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Yes") { _, _ ->
                    FirebaseAuth.getInstance().signOut()
                    val intent = Intent(this, AuthenticationActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        editName = findViewById(R.id.edit_name)
        editName.setOnClickListener {
            Vibration.vibrate(this, 50)
            val editText = EditText(this)
            editText.hint = "Enter new name"
            AlertDialog.Builder(this)
                .setTitle("Edit Name")
                .setView(editText)
                .setPositiveButton("Save") { _, _ ->
                    val newName = editText.text.toString().trim()
                    if (newName.isEmpty()) {
                        Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }
                    val userId = FirebaseAuth.getInstance().currentUser?.uid
                    if (userId != null) {
                        FirebaseFirestore.getInstance().collection("users")
                            .document(userId)
                            .update("name", newName)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Name updated", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Failed to update name", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        github = findViewById(R.id.github)
        github.setOnClickListener {
            Vibration.vibrate(this, 50)
            val url = "https://github.com/Arijit-05/Momentum"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            try {
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(this, "No browser found to open link", Toast.LENGTH_SHORT).show()
            }
        }

        projects = findViewById(R.id.projects)
        projects.setOnClickListener {
            Vibration.vibrate(this, 50)
            val url = "https://arijit-05.github.io/website/"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            try {
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(this, "No browser found to open link", Toast.LENGTH_SHORT).show()
            }
        }

        aiSwitch = findViewById(R.id.ai_switch)
        aiSwitch.isChecked = loadAiSwitchState()
        aiSwitch.setOnCheckedChangeListener { _, isChecked ->
            saveAiSwitchState(isChecked)
        }
    }

    private fun saveAiSwitchState(enabled: Boolean) {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        prefs.edit().putBoolean(AI_SWITCH_KEY, enabled).apply()
    }
    private fun loadAiSwitchState(): Boolean {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        return prefs.getBoolean(AI_SWITCH_KEY, true)
    }
}