package com.example.moodly

import android.graphics.drawable.Drawable

data class UserDiaryFormat(
    var dateDiary: String,
    var moodDiary: Drawable?,
    var contentDiary: String
)
