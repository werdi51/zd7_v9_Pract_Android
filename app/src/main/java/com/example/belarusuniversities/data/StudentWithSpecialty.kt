
package com.example.belarusuniversities.data


import androidx.room.ColumnInfo
import androidx.room.Embedded

data class StudentWithSpecialty(
    @Embedded
    val student: Student,

    @ColumnInfo(name = "specialtyName")
    val specialtyName: String
)