package com.arijit.habits.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.arijit.habits.MainActivity
import com.arijit.habits.R
import com.arijit.habits.utils.Vibration
import com.google.firebase.auth.FirebaseAuth

class LoginFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var loginLayout: LinearLayout
    private lateinit var loadingLayout: LinearLayout
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var loginBtn: CardView
    private lateinit var forgotPassword: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)
        auth = FirebaseAuth.getInstance()
        initializeViews(view)

        loginBtn.setOnClickListener {
            Vibration.vibrate(requireContext(), 100)
            loginUser()
        }

        forgotPassword.setOnClickListener {
            Vibration.vibrate(requireContext(), 100)
            showForgotPasswordDialog()
        }

        return view
    }

    private fun initializeViews(view: View) {
        loginLayout = view.findViewById(R.id.login_layout)
        loadingLayout = view.findViewById(R.id.loading_layout)
        email = view.findViewById(R.id.email)
        password = view.findViewById(R.id.password)
        loginBtn = view.findViewById(R.id.login_btn)
        forgotPassword = view.findViewById(R.id.forgot_password)
    }
    private fun loginUser() {
        val emailTxt = email.text.toString().trim()
        val passwordTxt = password.text.toString().trim()

        if (emailTxt.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(emailTxt).matches()) {
            Toast.makeText(requireContext(), "Enter a valid email address", Toast.LENGTH_SHORT).show()
            return
        }

        if (passwordTxt.isEmpty()) {
            Toast.makeText(requireContext(), "Password cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        val navTxt = (activity as? AppCompatActivity)?.findViewById<TextView>(R.id.navigator_txt)
        navTxt?.animate()?.alpha(0f)?.setDuration(300)?.withEndAction {
            navTxt.visibility = View.GONE
        }?.start()

        loginLayout.animate().alpha(0f).setDuration(300).withEndAction {
            loginLayout.visibility = View.GONE
            loadingLayout.alpha = 0f
            loadingLayout.visibility = View.VISIBLE
            loadingLayout.animate().alpha(1f).setDuration(300).start()
        }.start()

        auth.signInWithEmailAndPassword(emailTxt, passwordTxt)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(requireContext(), "Welcome back!", Toast.LENGTH_SHORT).show()
                    loadingLayout.postDelayed({
                        startActivity(Intent(requireContext(), MainActivity::class.java))
                        requireActivity().finish()
                    }, 800)
                } else {
                    navTxt?.animate()?.alpha(1f)?.setDuration(300)?.withEndAction {
                        navTxt.visibility = View.VISIBLE
                    }?.start()
                    loadingLayout.animate().alpha(0f).setDuration(300).withEndAction {
                        loadingLayout.visibility = View.GONE
                        loginLayout.alpha = 0f
                        loginLayout.visibility = View.VISIBLE
                        loginLayout.animate().alpha(1f).setDuration(300).start()
                    }.start()
                    Toast.makeText(requireContext(), "Error ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
    private fun showForgotPasswordDialog() {
        val input = EditText(requireContext())
        input.hint = "Enter your email"
        input.setPadding(40, 40, 40, 40)

        AlertDialog.Builder(requireContext())
            .setTitle("Reset Password")
            .setMessage("We'll send a password reset link to your email")
            .setView(input)
            .setPositiveButton("Send") { _, _ ->
                val email = input.text.toString().trim()
                if (email.isEmpty()) {
                    Toast.makeText(requireContext(), "Email cannot be empty", Toast.LENGTH_SHORT).show()
                } else {
                    FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(requireContext(), "Reset link sent to $email", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(requireContext(), "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

}