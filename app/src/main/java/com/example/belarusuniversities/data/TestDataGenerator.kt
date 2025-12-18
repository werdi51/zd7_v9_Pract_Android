package com.example.belarusuniversities.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.Calendar

object TestDataGenerator {

    fun populateDatabase(dao: UniversityDao, scope: CoroutineScope) {
        scope.launch {
            val college1Id = dao.insertCollege(College(
                name = "Белорусский государственный университет информатики и радиоэлектроники (БГУИР)",
                region = "Минск",
                photoUrl = null
            ))

            val college2Id = dao.insertCollege(College(
                name = "Белорусский государственный университет (БГУ)",
                region = "Минск",
                photoUrl = null
            ))

            val college3Id = dao.insertCollege(College(
                name = "Гродненский государственный университет имени Янки Купалы",
                region = "Гродно",
                photoUrl = null
            ))

            val specialty1Id = dao.insertSpecialty(Specialty(
                collegeId = college1Id.toInt(),
                code = "1-40 01 01",
                name = "Программная инженерия",
                description = "Подготовка специалистов по разработке программного обеспечения",
                budgetPlaces = 30,
                contractPlaces = 70,
                studyYears = 4
            ))

            val specialty2Id = dao.insertSpecialty(Specialty(
                collegeId = college1Id.toInt(),
                code = "1-40 05 01",
                name = "Информационные системы и технологии",
                description = "Разработка и администрирование информационных систем",
                budgetPlaces = 25,
                contractPlaces = 60,
                studyYears = 4
            ))

            val specialty3Id = dao.insertSpecialty(Specialty(
                collegeId = college2Id.toInt(),
                code = "1-31 03 07",
                name = "Прикладная информатика",
                description = "Применение информационных технологий в различных областях",
                budgetPlaces = 20,
                contractPlaces = 50,
                studyYears = 4
            ))

            val calendar = Calendar.getInstance()

            calendar.set(2003, Calendar.MAY, 15)
            dao.insertStudent(Student(
                specialtyId = specialty1Id.toInt(),
                fullName = "Иванов Иван Иванович",
                birthDate = calendar.timeInMillis,
                certificateScore = 95.5,
                isBudget = true
            ))

            calendar.set(2004, Calendar.JULY, 22)
            dao.insertStudent(Student(
                specialtyId = specialty1Id.toInt(),
                fullName = "Петрова Анна Сергеевна",
                birthDate = calendar.timeInMillis,
                certificateScore = 92.0,
                isBudget = true
            ))

            calendar.set(2003, Calendar.NOVEMBER, 10)
            dao.insertStudent(Student(
                specialtyId = specialty1Id.toInt(),
                fullName = "Сидоров Алексей Петрович",
                birthDate = calendar.timeInMillis,
                certificateScore = 78.5,
                isBudget = false
            ))

            calendar.set(2004, Calendar.MARCH, 3)
            dao.insertStudent(Student(
                specialtyId = specialty1Id.toInt(),
                fullName = "Козлова Мария Дмитриевна",
                birthDate = calendar.timeInMillis,
                certificateScore = 85.0,
                isBudget = false
            ))

            calendar.set(2003, Calendar.AUGUST, 14)
            dao.insertStudent(Student(
                specialtyId = specialty2Id.toInt(),
                fullName = "Смирнов Дмитрий Владимирович",
                birthDate = calendar.timeInMillis,
                certificateScore = 88.5,
                isBudget = true
            ))

            calendar.set(2004, Calendar.JANUARY, 25)
            dao.insertStudent(Student(
                specialtyId = specialty2Id.toInt(),
                fullName = "Волкова Екатерина Андреевна",
                birthDate = calendar.timeInMillis,
                certificateScore = 91.0,
                isBudget = false
            ))
        }
    }
}