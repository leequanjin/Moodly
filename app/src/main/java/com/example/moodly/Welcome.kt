package com.example.moodly

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
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

class Welcome : AppCompatActivity() {

    lateinit var edtUsername: EditText
    lateinit var btnSubmitName: Button

    private lateinit var auth: FirebaseAuth
    private lateinit var DB_Reference: DatabaseReference

    lateinit var SLD: SaveLoadData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_welcome)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //region Initialize Variable

        val starAnimSlow = AnimationUtils.loadAnimation(this, R.anim.sparkle_star_slow)
        val starAnimFast = AnimationUtils.loadAnimation(this, R.anim.sparkle_star_fast)
        val cometAnim = AnimationUtils.loadAnimation(this, R.anim.comet)
        val popupAnim = AnimationUtils.loadAnimation(this, R.anim.popup)

        val star1 = findViewById<ImageView>(R.id.star1)
        val star2 = findViewById<ImageView>(R.id.star2)
        val star3 = findViewById<ImageView>(R.id.star3)

        val pnlGetName = findViewById<ConstraintLayout>(R.id.pnlGetName)

        val comet1 = findViewById<ImageView>(R.id.comet1)
        val comet2 = findViewById<ImageView>(R.id.comet2)

        edtUsername = findViewById(R.id.edtUsername)
        btnSubmitName = findViewById(R.id.btnSubmitName)

        auth = Firebase.auth
        DB_Reference = Firebase.database.reference

        //endregion

        //region Animation

        star1.startAnimation(starAnimSlow)
        star2.startAnimation(starAnimFast)
        star3.startAnimation(starAnimSlow)

        comet1.startAnimation(cometAnim)
        comet2.startAnimation(cometAnim)

        pnlGetName.startAnimation(popupAnim)

        btnSubmitName.setOnClickListener{
            val username = edtUsername.text.toString()
            val id = auth.currentUser?.uid.toString()

            if(username.isNotEmpty() && username.length >= 3){
                DB_Reference.child(id).child("username").setValue(username)

                SLD = SaveLoadData()

                SLD.LoadData(this)

                SLD.username = username

                SLD.SaveData(this)

                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)

                finish()
            }else{
                Toast.makeText(this, "Username must be longer than 2 characters", Toast.LENGTH_SHORT).show()
            }
        }

        //endregion
    }
}