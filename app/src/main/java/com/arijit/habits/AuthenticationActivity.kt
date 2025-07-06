package com.arijit.habits

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.arijit.habits.fragments.LoginFragment
import com.arijit.habits.fragments.RegisterFragment
import com.arijit.habits.utils.Vibration
import com.google.firebase.auth.FirebaseAuth

class AuthenticationActivity : AppCompatActivity() {
    private lateinit var navigatorTxt: TextView
    private var register: Boolean = true

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_authentication)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.container, RegisterFragment())
            .commit()

        navigatorTxt = findViewById(R.id.navigator_txt)
        navigatorTxt.text = "Already have an account?"
        navigatorTxt.setOnClickListener {
            Vibration.vibrate(this, 50)
            loadFragment()
        }

    }
    private fun loadFragment() {
        val fragment = if (register) LoginFragment() else RegisterFragment()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.setCustomAnimations(
            android.R.anim.fade_in, android.R.anim.fade_out)

        transaction.replace(R.id.container, fragment)
        transaction.commit()

        navigatorTxt.text = if (register) {
            "New here? Create an account"
        } else {
            "Already have an account?"
        }

        register = !register
    }



}