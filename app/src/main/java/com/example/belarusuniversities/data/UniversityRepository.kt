package com.example.belarusuniversities.data

import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class UniversityRepository(private val dao: UniversityDao) {
    // Колледжи
    suspend fun addCollege(college: College): Long {
        return dao.insertCollege(college)
    }

    fun getAllColleges(): Flow<List<College>> {
        return dao.getAllColleges()
    }

    // Специальности
    suspend fun addSpecialty(specialty: Specialty): Long {
        return dao.insertSpecialty(specialty)
    }

    fun getAllSpecialties(): Flow<List<Specialty>> {
        return dao.getAllSpecialties()
    }

    fun getSpecialtiesByCollegeFlow(collegeId: Int): Flow<List<Specialty>> {
        return dao.getSpecialtiesByCollegeFlow(collegeId)
    }

    suspend fun getSpecialtiesByCollege(collegeId: Int): List<Specialty> {
        return dao.getSpecialtiesByCollege(collegeId)
    }

    // Студенты
    suspend fun addStudent(student: Student): Long {
        return dao.insertStudent(student)
    }

    suspend fun updateStudent(student: Student): Int {
        return dao.updateStudent(student)
    }

    suspend fun deleteStudent(student: Student): Int {
        return dao.deleteStudent(student)
    }

    fun getStudentsBySpecialty(specialtyId: Int): Flow<List<Student>> {
        return dao.getStudentsBySpecialtyOld(specialtyId)
    }

    fun getStudentsWithSpecialtyInfo(specialtyId: Int): Flow<List<StudentWithSpecialty>> {
        return dao.getStudentsWithSpecialtyInfoOld(specialtyId)
    }

    suspend fun getSpecialtyById(specialtyId: Int): Specialty? {
        return dao.getSpecialtyById(specialtyId)
    }
    suspend fun updateSpecialty(specialty: Specialty): Int {
        return dao.updateSpecialty(specialty)
    }

    suspend fun deleteSpecialty(specialty: Specialty): Int {
        return dao.deleteSpecialty(specialty)
    }
    // ПРЕПОДАВАТЕЛИ
    suspend fun addTeacher(teacher: Teacher): Long {
        return dao.insertTeacher(teacher)
    }

    suspend fun updateTeacher(teacher: Teacher): Int {
        return dao.updateTeacher(teacher)
    }

    suspend fun deleteTeacher(teacher: Teacher): Int {
        return dao.deleteTeacher(teacher)
    }

    fun getAllTeachers(): Flow<List<Teacher>> {
        return dao.getAllTeachers()
    }

    suspend fun getTeacherById(teacherId: Int): Teacher? {
        return dao.getTeacherById(teacherId)
    }

    suspend fun assignTeacherToSpecialty(teacherId: Int, specialtyId: Int, hoursPerWeek: Int): Long {
        val yearlyHours = hoursPerWeek * 36
        val currentHours = dao.getTeacherTotalHours(teacherId)
        val totalHours = currentHours + yearlyHours
        val teacher = dao.getTeacherById(teacherId)

        if (teacher != null && totalHours > teacher.maxHoursPerYear) {
            throw Exception("Превышена годовая нагрузка (${teacher.maxHoursPerYear} часов)")
        }

        val result = dao.insertTeacherSpecialty(
            TeacherSpecialty(teacherId, specialtyId, hoursPerWeek)
        )

        dao.addTeacherHours(teacherId, yearlyHours)

        return result
    }



    fun getTeachersBySpecialty(specialtyId: Int): Flow<List<Teacher>> {
        return dao.getTeachersBySpecialty(specialtyId)
    }

    suspend fun enrollStudentInSpecialty(studentId: Int, specialtyId: Int, isBudget: Boolean): Long {
        if (isBudget) {
            val budgetCount = dao.countBudgetSpecialtiesForStudent(studentId)
            if (budgetCount >= 1) {
                throw Exception("Студент уже имеет бюджетную специальность")
            }
        }

        if (isBudget) {
            val budgetStudents = dao.countBudgetStudentsOnSpecialty(specialtyId)
            if (budgetStudents >= 50) {
                throw Exception("На специальности закончились бюджетные места (максимум 50)")
            }
        }

        return dao.insertStudentSpecialty(
            StudentSpecialty(studentId, specialtyId, isBudget, Calendar.getInstance().get(Calendar.YEAR))
        )
    }

    suspend fun unenrollStudentFromSpecialty(studentId: Int, specialtyId: Int): Int {
        return 0
    }

    fun getSpecialtiesByStudent(studentId: Int): Flow<List<Specialty>> {
        return dao.getSpecialtiesByStudent(studentId)
    }

    suspend fun getAllSpecialtiesList(): List<Specialty> {
        return dao.getAllSpecialtiesList()
    }


    suspend fun removeTeacherFromSpecialty(teacherId: Int, specialtyId: Int): Int {
        val hours = dao.getHoursForTeacherSpecialty(teacherId, specialtyId)
        val result = dao.deleteTeacherSpecialty(teacherId, specialtyId)

        if (hours > 0) {
            dao.removeTeacherHours(teacherId, hours)
        }

        return result
    }


}