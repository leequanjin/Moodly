package com.example.moodly

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Firebase
import com.google.firebase.auth.ActionCodeEmailInfo
import com.google.firebase.auth.actionCodeSettings
import com.google.firebase.auth.auth

class Auth : AppCompatActivity() {

    lateinit var edtEmail: EditText
    lateinit var btnVerify: Button
    lateinit var btnSkip: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_auth)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        edtEmail = findViewById(R.id.edtEmail)
        btnVerify = findViewById(R.id.btnVerify)
        btnSkip = findViewById(R.id.btnSkip)

        btnVerify.setOnClickListener{
            sendAuthLink("yiptwinkle@gmail.com")
            verifyAuthLink("yiptwinkle@gmail.com")
        }

        btnSkip.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    fun sendAuthLink(email: String){
        val actionCodeSettings = actionCodeSettings {
            // URL you want to redirect back to. The domain (www.example.com) for this
            // URL must be whitelisted in the Firebase Console.
            url = "https://example.page.link"
            // This must be true
            handleCodeInApp = true
        }

        Firebase.auth.sendSignInLinkToEmail(email, actionCodeSettings)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("success", "Email sent.")

                    Toast.makeText(applicationContext, "Email sent.", Toast.LENGTH_LONG).show()
                }else{
                    Log.d("error", task.exception.toString())

                    Toast.makeText(applicationContext, "${task.exception.toString()}", Toast.LENGTH_LONG).show()
                }
            }
    }

    fun verifyAuthLink(email: String){
        val auth = Firebase.auth
        val intent = intent
        val emailLink = intent.data.toString()

        if (auth.isSignInWithEmailLink(emailLink)) {
            // The client SDK will parse the code from the link for you.o
            auth.signInWithEmailLink(email, emailLink)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(applicationContext, "Successfully signed in with email link!", Toast.LENGTH_LONG).show()

                        val result = task.result

                        //result.user.uid
                        // You can access the new user via result.getUser()
                        // Additional user info profile *not* available via:
                        // result.getAdditionalUserInfo().getProfile() == null
                        // You can check if the user is new or existing:
                        // result.getAdditionalUserInfo().isNewUser()
                    } else {
                        //Log.e("failed", "Error signing in with email link", task.exception)
                        Toast.makeText(applicationContext, "${task.exception.toString()}", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }
}