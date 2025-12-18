package com.example.belarusuniversities.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "students",
    foreignKeys = [
        ForeignKey(
            entity = Specialty::class,
            parentColumns = ["id"],
            childColumns = ["specialtyId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("specialtyId")]
)
data class Student(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "specialtyId")
    val specialtyId: Int,

    @ColumnInfo(name = "fullName")
    val fullName: String,

    @ColumnInfo(name = "birthDate")
    val birthDate: Long,

    @ColumnInfo(name = "certificateScore")
    val certificateScore: Double,

    @ColumnInfo(name = "isBudget")
    val isBudget: Boolean,

    @ColumnInfo(name = "enrollmentDate")
    val enrollmentDate: Long = System.currentTimeMillis()
)