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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database

class Auth : AppCompatActivity() {

    lateinit var edtEmail: EditText
    lateinit var edtPassword: EditText
    lateinit var btnRegister: Button
    lateinit var btnLogin: Button
    lateinit var btnSkip: Button

    private lateinit var auth: FirebaseAuth
    private lateinit var DB_Reference: DatabaseReference

    lateinit var email: String
    lateinit var password: String

    lateinit var SLD: SaveLoadData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_auth)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //region Initialize Variable

        auth = Firebase.auth
        DB_Reference = Firebase.database.reference

        edtEmail = findViewById(R.id.edtEmail)
        edtPassword = findViewById(R.id.edtPassword)
        btnRegister = findViewById(R.id.btnRegister)
        btnLogin = findViewById(R.id.btnLogin)
        btnSkip = findViewById(R.id.btnSkip)

        //endregion

        //region Load Save Data

        SLD = SaveLoadData()
        SLD.LoadData(this)

        email = SLD.email
        password = SLD.password

        //endregion

        //region Button On Click Listener

        //TODO: Check if input valid

        btnRegister.setOnClickListener{
            email = edtEmail.text.toString()
            password = edtPassword.text.toString()

            register(email, password)
        }

        btnLogin.setOnClickListener{
            email = edtEmail.text.toString()
            password = edtPassword.text.toString()

            login(email, password)
        }

        btnSkip.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        //endregion

        //auto login after app start
        login(email, password)
    }

    fun register(email: String, password: String){
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {

                    val id = auth.currentUser?.uid.toString()

                    SLD.email = email
                    SLD.password = password

                    SLD.SaveData(this)

                    val intent = Intent(this, Welcome::class.java)
                    startActivity(intent)

                    finish()
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    fun login(email: String, password: String){
        if (auth.currentUser == null && email != "" && password != "") {
            // No user is signed in
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success
                    SLD.email = email
                    SLD.password = password

                    val id = auth.currentUser?.uid.toString()
                    DB_Reference.child(id).child("username").get()
                        .addOnSuccessListener {
                            SLD.username = it.value.toString()

                            SLD.SaveData(this)
                        }

                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(
                        baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } else if (auth.currentUser != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)

            finish()
        }
    }
}