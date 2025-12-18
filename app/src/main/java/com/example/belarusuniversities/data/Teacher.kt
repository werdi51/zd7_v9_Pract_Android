package com.example.belarusuniversities.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "teachers")
data class Teacher(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val email: String,
    val maxHoursPerYear: Int = 1440,
    val currentHours: Int = 0,
    val hourlyRate: Double = 20.0,
    val bonusRate: Double = 1.5
)