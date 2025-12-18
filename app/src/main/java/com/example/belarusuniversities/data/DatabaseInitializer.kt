package com.example.belarusuniversities.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

object DatabaseInitializer {

    private const val TAG = "DatabaseInitializer"

    fun initialize(context: Context) {
        Log.d(TAG, "Инициализация базы данных")
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            try {
                val database = UniversityDatabase.getDatabase(context)
                val dao = database.universityDao()

                val collegesCount = try {
                    var count = 0
                    dao.getAllColleges().collect {
                        count = it.size
                    }
                    count
                } catch (e: Exception) {
                    0
                }

                if (collegesCount == 0) {
                    Log.d(TAG, "Добавляем тестовые данные")
                    addTestData(dao)
                } else {
                    Log.d(TAG, "Данные уже есть ($collegesCount колледжей)")
                }

            } catch (e: Exception) {
                Log.e(TAG, "Ошибка инициализации БД: ${e.message}", e)
            }
        }
    }

    private suspend fun addTestData(dao: UniversityDao) {
        try {
            val collegeId = dao.insertCollege(College(
                name = "БГУИР",
                region = "Минск",
                photoUrl = null
            ))

            val specialtyId = dao.insertSpecialty(Specialty(
                collegeId = collegeId.toInt(),
                code = "1-40 01 01",
                name = "Программная инженерия",
                description = "Разработка программного обеспечения",
                budgetPlaces = 30,
                contractPlaces = 70,
                studyYears = 4
            ))

            val calendar = Calendar.getInstance()

            calendar.set(2003, Calendar.MAY, 15)
            dao.insertStudent(Student(
                specialtyId = specialtyId.toInt(),
                fullName = "Иванов Иван Иванович",
                birthDate = calendar.timeInMillis,
                certificateScore = 95.5,
                isBudget = true
            ))

            calendar.set(2004, Calendar.JULY, 22)
            dao.insertStudent(Student(
                specialtyId = specialtyId.toInt(),
                fullName = "Петрова Анна Сергеевна",
                birthDate = calendar.timeInMillis,
                certificateScore = 92.0,
                isBudget = true
            ))

            calendar.set(2003, Calendar.NOVEMBER, 10)
            dao.insertStudent(Student(
                specialtyId = specialtyId.toInt(),
                fullName = "Сидоров Алексей Петрович",
                birthDate = calendar.timeInMillis,
                certificateScore = 78.5,
                isBudget = false
            ))

            Log.d(TAG, "Тестовые данные успешно добавлены")

        } catch (e: Exception) {
            Log.e(TAG, "Ошибка добавления тестовых данных: ${e.message}", e)
        }
    }
}