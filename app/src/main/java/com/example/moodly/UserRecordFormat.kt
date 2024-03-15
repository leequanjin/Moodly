package com.example.moodly

data class UserRecordFormat(
    var rid: Int,
    var months: String,
    var diary: ArrayList<UserDiaryFormat>
)
