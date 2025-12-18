package com.example.belarusuniversities.data

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Создаем таблицу specialties
        database.execSQL(
            """
            CREATE TABLE specialties (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                collegeId INTEGER NOT NULL,
                code TEXT NOT NULL,
                name TEXT NOT NULL,
                description TEXT NOT NULL DEFAULT '',
                budgetPlaces INTEGER NOT NULL DEFAULT 50,
                contractPlaces INTEGER NOT NULL DEFAULT 100,
                studyYears INTEGER NOT NULL DEFAULT 4,
                FOREIGN KEY(collegeId) REFERENCES colleges(id) ON DELETE CASCADE
            )
            """.trimIndent()
        )
    }
}