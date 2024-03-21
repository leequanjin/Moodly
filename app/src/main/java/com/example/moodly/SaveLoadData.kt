package com.example.moodly

import android.content.Context
import android.content.SharedPreferences

class SaveLoadData {

    val SHARED_PREFS: String = "sharedPrefs"

    var username: String = ""
    var email: String = ""
    var password: String = ""
    var showConfirmChatbot: Boolean = true

    fun LoadData(activity: Context) {
        val sharedPreferences: SharedPreferences =
            activity.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE)

        username = sharedPreferences.getString("username", "").toString()
        email = sharedPreferences.getString("email", "").toString()
        password = sharedPreferences.getString("password", "").toString()
        showConfirmChatbot = sharedPreferences.getBoolean("showConfirmChatbot", true)

        println("Load Data")
        println("username: $username")
        println("email: $email")
        println("password: $password")
        println("showConfirmChatbot: $showConfirmChatbot")
    }

    fun SaveData(activity: Context) {
        val sharedPreferences: SharedPreferences =
            activity.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()

        editor.putString("username", username)
        editor.putString("email", email)
        editor.putString("password", password)
        editor.putBoolean("showConfirmChatbot", showConfirmChatbot)

        editor.apply()

        println("Save Data")
        println("username: $username")
        println("email: $email")
        println("password: $password")
        println("showConfirmChatbot: $showConfirmChatbot")
    }
}