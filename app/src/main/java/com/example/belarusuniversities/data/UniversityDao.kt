package com.example.belarusuniversities.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UniversityDao {
    // ========== КОЛЛЕДЖИ ==========
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCollege(college: College): Long

    @Query("SELECT * FROM colleges ORDER BY name")
    fun getAllColleges(): Flow<List<College>>

    // ========== СПЕЦИАЛЬНОСТИ ==========
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpecialty(specialty: Specialty): Long

    @Update
    suspend fun updateSpecialty(specialty: Specialty): Int

    @Delete
    suspend fun deleteSpecialty(specialty: Specialty): Int

    @Query("SELECT * FROM specialties ORDER BY name")
    fun getAllSpecialties(): Flow<List<Specialty>>

    @Query("SELECT * FROM specialties WHERE collegeId = :collegeId")
    fun getSpecialtiesByCollegeFlow(collegeId: Int): Flow<List<Specialty>>

    @Query("SELECT * FROM specialties WHERE collegeId = :collegeId")
    suspend fun getSpecialtiesByCollege(collegeId: Int): List<Specialty>

    @Query("SELECT * FROM specialties WHERE id = :specialtyId")
    suspend fun getSpecialtyById(specialtyId: Int): Specialty?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: Student): Long

    @Update
    suspend fun updateStudent(student: Student): Int

    @Delete
    suspend fun deleteStudent(student: Student): Int

    // ВРЕМЕННЫЕ МЕТОДЫ ДЛЯ СТАРОЙ СТРУКТУРЫ
    @Query("SELECT * FROM students WHERE specialtyId = :specialtyId")
    fun getStudentsBySpecialtyOld(specialtyId: Int): Flow<List<Student>>

    @Query("""
        SELECT students.*, specialties.name as specialtyName 
        FROM students 
        INNER JOIN specialties ON students.specialtyId = specialties.id
        WHERE students.specialtyId = :specialtyId
    """)
    fun getStudentsWithSpecialtyInfoOld(specialtyId: Int): Flow<List<StudentWithSpecialty>>
    // ========== ПРЕПОДАВАТЕЛИ ==========
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeacher(teacher: Teacher): Long

    @Update
    suspend fun updateTeacher(teacher: Teacher): Int

    @Delete
    suspend fun deleteTeacher(teacher: Teacher): Int

    @Query("SELECT * FROM teachers ORDER BY name")
    fun getAllTeachers(): Flow<List<Teacher>>

    @Query("SELECT * FROM teachers WHERE id = :teacherId")
    suspend fun getTeacherById(teacherId: Int): Teacher?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeacherSpecialty(teacherSpecialty: TeacherSpecialty): Long

    @Query("DELETE FROM teacher_specialties WHERE teacherId = :teacherId AND specialtyId = :specialtyId")
    suspend fun deleteTeacherSpecialty(teacherId: Int, specialtyId: Int): Int

    @Query("""
        SELECT t.* FROM teachers t
        INNER JOIN teacher_specialties ts ON t.id = ts.teacherId
        WHERE ts.specialtyId = :specialtyId
    """)
    fun getTeachersBySpecialty(specialtyId: Int): Flow<List<Teacher>>

    @Query("""
        SELECT s.* FROM specialties s
        INNER JOIN teacher_specialties ts ON s.id = ts.specialtyId
        WHERE ts.teacherId = :teacherId
    """)
    fun getSpecialtiesByTeacher(teacherId: Int): Flow<List<Specialty>>

    @Query("""
    SELECT COALESCE(SUM(ts.hoursPerWeek * 36), 0) as totalHours 
    FROM teacher_specialties ts 
    WHERE ts.teacherId = :teacherId
""")
    suspend fun getTeacherTotalHours(teacherId: Int): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudentSpecialty(studentSpecialty: StudentSpecialty): Long

    @Query("SELECT COUNT(*) FROM student_specialties WHERE studentId = :studentId AND isBudget = 1")
    suspend fun countBudgetSpecialtiesForStudent(studentId: Int): Int

    @Query("""
        SELECT s.* FROM students s
        INNER JOIN student_specialties ss ON s.id = ss.studentId
        WHERE ss.specialtyId = :specialtyId AND ss.isActive = 1
    """)
    fun getStudentsBySpecialtyNew(specialtyId: Int): Flow<List<Student>>

    @Query("""
        SELECT sp.* FROM specialties sp
        INNER JOIN student_specialties ss ON sp.id = ss.specialtyId
        WHERE ss.studentId = :studentId AND ss.isActive = 1
    """)
    fun getSpecialtiesByStudent(studentId: Int): Flow<List<Specialty>>

    @Query("""
        SELECT COUNT(*) FROM student_specialties ss
        WHERE ss.specialtyId = :specialtyId AND ss.isBudget = 1
    """)
    suspend fun countBudgetStudentsOnSpecialty(specialtyId: Int): Int

    @Query("""
        SELECT s.*, sp.name as specialtyName 
        FROM students s
        INNER JOIN student_specialties ss ON s.id = ss.studentId
        INNER JOIN specialties sp ON ss.specialtyId = sp.id
        WHERE ss.specialtyId = :specialtyId AND ss.isActive = 1
    """)
    fun getStudentsWithSpecialtyInfoNew(specialtyId: Int): Flow<List<StudentWithSpecialty>>
    @Query("SELECT * FROM specialties ORDER BY name")
    suspend fun getAllSpecialtiesList(): List<Specialty>

    @Query("""
    UPDATE teachers 
    SET currentHours = currentHours + :additionalHours 
    WHERE id = :teacherId
""")
    suspend fun addTeacherHours(teacherId: Int, additionalHours: Int): Int

    @Query("""
    UPDATE teachers 
    SET currentHours = (
        SELECT COALESCE(SUM(hoursPerWeek * 36), 0)
        FROM teacher_specialties 
        WHERE teacherId = :teacherId
    )
    WHERE id = :teacherId
""")
    suspend fun recalculateTeacherHours(teacherId: Int): Int

    @Query("""
    SELECT hoursPerWeek * 36 
    FROM teacher_specialties 
    WHERE teacherId = :teacherId AND specialtyId = :specialtyId
""")
    suspend fun getHoursForTeacherSpecialty(teacherId: Int, specialtyId: Int): Int

    @Query("""
    UPDATE teachers 
    SET currentHours = currentHours - :removedHours 
    WHERE id = :teacherId
""")
    suspend fun removeTeacherHours(teacherId: Int, removedHours: Int): Int

}