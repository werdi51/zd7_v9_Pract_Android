package com.example.belarusuniversities.data

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "student_specialties",
    primaryKeys = ["studentId", "specialtyId"],
    foreignKeys = [
        ForeignKey(
            entity = Student::class,
            parentColumns = ["id"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Specialty::class,
            parentColumns = ["id"],
            childColumns = ["specialtyId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class StudentSpecialty(
    val studentId: Int,
    val specialtyId: Int,
    val isBudget: Boolean,
    val enrollmentYear: Int,
    val isActive: Boolean = true
)