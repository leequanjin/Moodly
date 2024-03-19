package com.example.moodly

import android.graphics.drawable.Drawable
import java.time.LocalDate

data class UserDiaryFormat(
    var dateDiary: String,
    var moodDiary: Drawable?,
    var contentDiary: String,
    var completeDate: LocalDate,
    var tagsDiary: ArrayList<String>,
    var mood: String
)
