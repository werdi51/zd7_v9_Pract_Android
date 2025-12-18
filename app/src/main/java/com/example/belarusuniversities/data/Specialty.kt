package com.example.belarusuniversities.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "specialties",
    foreignKeys = [ForeignKey(
        entity = College::class,
        parentColumns = ["id"],
        childColumns = ["collegeId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("collegeId")]
)
data class Specialty(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val collegeId: Int,
    val code: String,
    val name: String,
    val description: String = "",
    val budgetPlaces: Int = 50,
    val contractPlaces: Int = 100,
    val studyYears: Int = 4
)