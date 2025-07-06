package com.arijit.habits.fragments

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
import com.google.firebase.firestore.FirebaseFirestore

class RegisterFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var registerLayout: LinearLayout
    private lateinit var loadingLayout: LinearLayout
    private lateinit var name: EditText
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var registerBtn: CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_register, container, false)
        auth = FirebaseAuth.getInstance()
        initializeViews(view)

        registerBtn.setOnClickListener {
            Vibration.vibrate(requireContext(), 100)
            registerNewUser()
        }

        return view
    }

    private fun initializeViews(view: View) {
        registerLayout = view.findViewById(R.id.register_layout)
        loadingLayout = view.findViewById(R.id.loading_layout)
        name = view.findViewById(R.id.name)
        email = view.findViewById(R.id.email)
        password = view.findViewById(R.id.password)
        registerBtn = view.findViewById(R.id.register_btn)
    }
    private fun registerNewUser() {
        val nameTxt = name.text.toString().trim()
        val emailTxt = email.text.toString().trim()
        val passwordTxt = password.text.toString().trim()

        if (nameTxt.isEmpty()) {
            Toast.makeText(requireContext(), "We need a name to call you", Toast.LENGTH_SHORT).show()
            return
        }

        if (emailTxt.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(emailTxt).matches()) {
            Toast.makeText(requireContext(), "Enter a valid email address", Toast.LENGTH_SHORT).show()
            return
        }

        if (passwordTxt.length < 6) {
            Toast.makeText(requireContext(), "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            return
        }

        val navTxt = (activity as? AppCompatActivity)?.findViewById<TextView>(R.id.navigator_txt)
        navTxt?.animate()?.alpha(0f)?.setDuration(300)?.withEndAction {
            navTxt.visibility = View.GONE
        }?.start()

        registerLayout.animate().alpha(0f).setDuration(300).withEndAction {
            registerLayout.visibility = View.GONE
            loadingLayout.alpha = 0f
            loadingLayout.visibility = View.VISIBLE
            loadingLayout.animate().alpha(1f).setDuration(300).start()
        }.start()

        auth.createUserWithEmailAndPassword(emailTxt, passwordTxt)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: return@addOnCompleteListener
                    val userMap = mapOf("name" to nameTxt)
                    FirebaseFirestore.getInstance().collection("users")
                        .document(userId).set(userMap)
                    Toast.makeText(requireContext(), "Registered successfully", Toast.LENGTH_SHORT).show()
                    loadingLayout.postDelayed({
                        startActivity(Intent(requireContext(), MainActivity::class.java))
                        requireActivity().finish()
                    }, 1000)
                } else {
                    navTxt?.animate()?.alpha(1f)?.setDuration(300)?.withEndAction {
                        navTxt.visibility = View.VISIBLE
                    }?.start()
                    loadingLayout.animate().alpha(0f).setDuration(300).withEndAction {
                        loadingLayout.visibility = View.GONE
                        registerLayout.alpha = 0f
                        registerLayout.visibility = View.VISIBLE
                        registerLayout.animate().alpha(1f).setDuration(300).start()
                    }.start()
                    Toast.makeText(requireContext(), "Error ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

}