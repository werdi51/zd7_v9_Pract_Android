package com.example.belarusuniversities.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "colleges")
data class College(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val region: String,
    val photoUrl: String? = null
)