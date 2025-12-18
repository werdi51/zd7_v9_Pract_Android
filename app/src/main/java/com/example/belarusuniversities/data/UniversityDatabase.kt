package com.example.belarusuniversities.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        College::class,
        Specialty::class,
        Student::class,
        Teacher::class,
        TeacherSpecialty::class,
        StudentSpecialty::class
    ],
    version = 6,
    exportSchema = false
)
abstract class UniversityDatabase : RoomDatabase() {

    abstract fun universityDao(): UniversityDao

    companion object {
        @Volatile
        private var INSTANCE: UniversityDatabase? = null

        fun getDatabase(context: Context): UniversityDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    UniversityDatabase::class.java,
                    "university_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5,
                        MIGRATION_5_6)
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `specialties` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `collegeId` INTEGER NOT NULL,
                        `code` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `description` TEXT NOT NULL,
                        `budgetPlaces` INTEGER NOT NULL,
                        `contractPlaces` INTEGER NOT NULL,
                        `studyYears` INTEGER NOT NULL DEFAULT 4,
                        FOREIGN KEY(`collegeId`) REFERENCES `colleges`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE 
                    )
                """)
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Создаем таблицу students
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `students` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `specialtyId` INTEGER NOT NULL,
                        `fullName` TEXT NOT NULL,
                        `birthDate` INTEGER NOT NULL,
                        `certificateScore` REAL NOT NULL,
                        `isBudget` INTEGER NOT NULL,
                        `enrollmentDate` INTEGER NOT NULL,
                        FOREIGN KEY(`specialtyId`) REFERENCES `specialties`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE 
                    )
                """)

                database.execSQL("CREATE INDEX IF NOT EXISTS `index_students_specialtyId` ON `students` (`specialtyId`)")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
            }
        }
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
            CREATE TABLE IF NOT EXISTS `teachers` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `name` TEXT NOT NULL,
                `email` TEXT NOT NULL,
                `maxHoursPerYear` INTEGER NOT NULL DEFAULT 1440,
                `currentHours` INTEGER NOT NULL DEFAULT 0,
                `hourlyRate` REAL NOT NULL DEFAULT 20.0,
                `bonusRate` REAL NOT NULL DEFAULT 1.5
            )
        """)

                database.execSQL("""
            CREATE TABLE IF NOT EXISTS `teacher_specialties` (
                `teacherId` INTEGER NOT NULL,
                `specialtyId` INTEGER NOT NULL,
                `hoursPerWeek` INTEGER NOT NULL,
                `semester` INTEGER NOT NULL DEFAULT 1,
                PRIMARY KEY(`teacherId`, `specialtyId`),
                FOREIGN KEY(`teacherId`) REFERENCES `teachers`(`id`) ON DELETE CASCADE,
                FOREIGN KEY(`specialtyId`) REFERENCES `specialties`(`id`) ON DELETE CASCADE
            )
        """)

                database.execSQL("""
            CREATE TABLE IF NOT EXISTS `student_specialties` (
                `studentId` INTEGER NOT NULL,
                `specialtyId` INTEGER NOT NULL,
                `isBudget` INTEGER NOT NULL,
                `enrollmentYear` INTEGER NOT NULL,
                `isActive` INTEGER NOT NULL DEFAULT 1,
                PRIMARY KEY(`studentId`, `specialtyId`),
                FOREIGN KEY(`studentId`) REFERENCES `students`(`id`) ON DELETE CASCADE,
                FOREIGN KEY(`specialtyId`) REFERENCES `specialties`(`id`) ON DELETE CASCADE
            )
        """)

                database.execSQL("CREATE INDEX IF NOT EXISTS `index_teacher_specialties_specialtyId` ON `teacher_specialties` (`specialtyId`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_student_specialties_specialtyId` ON `student_specialties` (`specialtyId`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_student_specialties_studentId` ON `student_specialties` (`studentId`)")

            }
        }
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("DROP TABLE IF EXISTS students")

                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `students` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `specialtyId` INTEGER NOT NULL,
                        `fullName` TEXT NOT NULL,
                        `birthDate` INTEGER NOT NULL,
                        `certificateScore` REAL NOT NULL,
                        `isBudget` INTEGER NOT NULL,
                        `enrollmentDate` INTEGER NOT NULL DEFAULT (strftime('%s','now') * 1000),
                        FOREIGN KEY(`specialtyId`) REFERENCES `specialties`(`id`) ON DELETE CASCADE
                    )
                """)

                database.execSQL("CREATE INDEX IF NOT EXISTS `index_students_specialtyId` ON `students` (`specialtyId`)")

                try {
                    val cursor = database.query("SELECT COUNT(*) FROM student_specialties")
                    cursor.use {
                        if (it.moveToFirst() && it.getInt(0) == 0) {
                            addTestStudents(database)
                        }
                    }
                } catch (e: Exception) {
                }
            }

            private fun addTestStudents(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    INSERT INTO students (specialtyId, fullName, birthDate, certificateScore, isBudget, enrollmentDate)
                    VALUES 
                    (1, 'Иванов Иван Иванович', 1049904000000, 95.5, 1, strftime('%s','now') * 1000),
                    (1, 'Петрова Анна Сергеевна', 1077753600000, 92.0, 1, strftime('%s','now') * 1000),
                    (1, 'Сидоров Алексей Петрович', 1050624000000, 78.5, 0, strftime('%s','now') * 1000)
                """)

                database.execSQL("""
                    INSERT INTO student_specialties (studentId, specialtyId, isBudget, enrollmentYear, isActive)
                    VALUES 
                    (1, 1, 1, 2024, 1),
                    (2, 1, 1, 2024, 1),
                    (3, 1, 0, 2024, 1)
                """)
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                CoroutineScope(Dispatchers.IO).launch {
                }
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
            }
        }
    }
}