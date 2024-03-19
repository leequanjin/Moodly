package com.example.moodly

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database
import java.util.regex.Pattern


class Auth : AppCompatActivity() {

    lateinit var layLoginScreen: ConstraintLayout
    lateinit var edtEmailLogin: EditText
    lateinit var edtPasswordLogin: EditText
    lateinit var btnShowPasswordLogin: ImageButton
    lateinit var btnLogin: Button
    lateinit var btnChangeToRegister: Button

    lateinit var layRegisterScreen: ConstraintLayout
    lateinit var edtEmailRegister: EditText
    lateinit var edtPasswordRegister: EditText
    lateinit var edtPasswordConfirmRegister: EditText
    lateinit var btnShowPasswordRegister: ImageButton
    lateinit var btnShowPasswordConfirmRegister: ImageButton
    lateinit var btnRegister: Button
    lateinit var btnChangeToLogin: Button

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

        layLoginScreen = findViewById(R.id.layLoginScreen)
        edtEmailLogin = findViewById(R.id.edtEmailLogin)
        edtPasswordLogin = findViewById(R.id.edtPasswordLogin)
        btnShowPasswordLogin = findViewById(R.id.btnShowPasswordLogin)
        btnLogin = findViewById(R.id.btnLogin)
        btnChangeToRegister = findViewById(R.id.btnChangeToRegister)

        layRegisterScreen = findViewById(R.id.layRegisterScreen)
        edtEmailRegister = findViewById(R.id.edtEmailRegister)
        edtPasswordRegister = findViewById(R.id.edtPasswordRegister)
        edtPasswordConfirmRegister = findViewById(R.id.edtPasswordConfirmRegister)
        btnShowPasswordRegister = findViewById(R.id.btnShowPasswordRegister)
        btnShowPasswordConfirmRegister = findViewById(R.id.btnShowPasswordConfirmRegister)
        btnRegister = findViewById(R.id.btnRegister)
        btnChangeToLogin = findViewById(R.id.btnChangeToLogin)

        btnSkip = findViewById(R.id.btnSkip)

        //endregion

        //region Load Save Data

        SLD = SaveLoadData()
        SLD.LoadData(this)

        email = SLD.email
        password = SLD.password

        //endregion

        layRegisterScreen.visibility = View.GONE
        layLoginScreen.visibility = View.VISIBLE

        //region Button On Click Listener

        btnLogin.setOnClickListener{
            email = edtEmailLogin.text.toString()
            password = edtPasswordLogin.text.toString()

            if(!isValidEmail(email)){
                Toast.makeText(this, "Email invalid.", Toast.LENGTH_SHORT).show()
            }else if(!isValidPassword(password)){
                Toast.makeText(this, "Password must be longer than 7 character.", Toast.LENGTH_SHORT).show()
            }else{
                login(email, password)
            }
        }

        btnShowPasswordLogin.setOnClickListener{
            showHidePassword(btnShowPasswordLogin, edtPasswordLogin)
        }

        btnShowPasswordRegister.setOnClickListener{
            showHidePassword(btnShowPasswordRegister, edtPasswordRegister)
        }

        btnShowPasswordConfirmRegister.setOnClickListener{
            showHidePassword(btnShowPasswordConfirmRegister, edtPasswordConfirmRegister)
        }

        btnChangeToRegister.setOnClickListener{
            layRegisterScreen.visibility = View.VISIBLE
            layLoginScreen.visibility = View.GONE
        }

        btnRegister.setOnClickListener{
            email = edtEmailRegister.text.toString()
            password = edtPasswordRegister.text.toString()
            val confirmPassword = edtPasswordConfirmRegister.text.toString()

            if(!isValidEmail(email)){
                Toast.makeText(this, "Email invalid.", Toast.LENGTH_SHORT).show()
            }else if(!isValidPassword(password)){
                Toast.makeText(this, "Password must be longer than 7 character.", Toast.LENGTH_SHORT).show()
            }else if(password != confirmPassword){
                Toast.makeText(this, "Passwords don't match.", Toast.LENGTH_SHORT).show()
            }else{
                register(email, password)
            }
        }

        btnChangeToLogin.setOnClickListener(){
            layRegisterScreen.visibility = View.GONE
            layLoginScreen.visibility = View.VISIBLE
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

    fun isValidEmail(email: String): Boolean{
        val EMAIL_ADDRESS_PATTERN = Pattern.compile(
            "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                    "\\@" +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                    "(" +
                    "\\." +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                    ")+"
        )

        return EMAIL_ADDRESS_PATTERN.matcher(email).matches()
    }

    fun isValidPassword(password: String): Boolean {
        return password.length >= 8
    }

    fun showHidePassword(btn: ImageButton, edt: EditText){
        if(btn.tag.equals("hide password")){
            //show password
            btn.setImageResource(R.drawable.eye_close)
            btn.tag = "show password"
            edt.transformationMethod = null

        }else{
            //hide password
            btn.setImageResource(R.drawable.eye_open)
            btn.tag = "hide password"
            edt.transformationMethod = PasswordTransformationMethod()
        }
    }
}
