package com.example.belarusuniversities.data

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

object TestDataHelper {

    fun addTestData(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = UniversityDatabase.getDatabase(context)
                val dao = database.universityDao()

                var hasData = false
                dao.getAllColleges().collect { colleges ->
                    hasData = colleges.isNotEmpty()
                }

                if (!hasData) {
                    val collegeId = dao.insertCollege(College(
                        name = "БГУИР",
                        region = "Минск"
                    ))

                    val specialtyId = dao.insertSpecialty(Specialty(
                        collegeId = collegeId.toInt(),
                        code = "1-40 01 01",
                        name = "Программная инженерия",
                        description = "Разработка ПО",
                        budgetPlaces = 30,
                        contractPlaces = 70,
                        studyYears = 4
                    ))

                    val calendar = Calendar.getInstance()

                    calendar.set(2003, Calendar.MAY, 15)
                    dao.insertStudent(Student(
                        specialtyId = specialtyId.toInt(),
                        fullName = "Иванов Иван",
                        birthDate = calendar.timeInMillis,
                        certificateScore = 95.5,
                        isBudget = true
                    ))

                    calendar.set(2004, Calendar.JULY, 22)
                    dao.insertStudent(Student(
                        specialtyId = specialtyId.toInt(),
                        fullName = "Петрова Анна",
                        birthDate = calendar.timeInMillis,
                        certificateScore = 92.0,
                        isBudget = true
                    ))

                    calendar.set(2003, Calendar.NOVEMBER, 10)
                    dao.insertStudent(Student(
                        specialtyId = specialtyId.toInt(),
                        fullName = "Сидоров Алексей",
                        birthDate = calendar.timeInMillis,
                        certificateScore = 78.5,
                        isBudget = false
                    ))
                }
            } catch (e: Exception) {
            }
        }
    }
}