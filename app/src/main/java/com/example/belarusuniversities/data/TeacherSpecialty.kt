package com.example.belarusuniversities.data

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "teacher_specialties",
    primaryKeys = ["teacherId", "specialtyId"],
    foreignKeys = [
        ForeignKey(
            entity = Teacher::class,
            parentColumns = ["id"],
            childColumns = ["teacherId"],
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
data class TeacherSpecialty(
    val teacherId: Int,
    val specialtyId: Int,
    val hoursPerWeek: Int,
    val semester: Int = 1
) {
    fun getYearlyHours(): Int = hoursPerWeek * 18 * 2
}