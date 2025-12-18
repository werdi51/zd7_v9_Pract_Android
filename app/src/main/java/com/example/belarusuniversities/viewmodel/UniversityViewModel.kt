package com.example.belarusuniversities.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.belarusuniversities.data.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UniversityViewModel(private val repository: UniversityRepository) : ViewModel() {

    // Колледжи
    private val _collegesState = MutableStateFlow<List<College>>(emptyList())
    val collegesState: StateFlow<List<College>> = _collegesState.asStateFlow()

    // Специальности
    private val _specialtiesState = MutableStateFlow<List<Specialty>>(emptyList())
    val specialtiesState: StateFlow<List<Specialty>> = _specialtiesState.asStateFlow()

    // API сервис
    private val apiService = UniversityApiService.create()

    init {
        loadAllData()
    }

    private fun loadAllData() {
        viewModelScope.launch {
            repository.getAllColleges().collect { colleges ->
                _collegesState.value = colleges
            }
        }

        viewModelScope.launch {
            repository.getAllSpecialties().collect { specialties ->
                _specialtiesState.value = specialties
            }
        }
    }

    // Колледжи
    suspend fun addCollege(college: College): Long {
        return repository.addCollege(college)
    }

    // Специальности
    suspend fun addSpecialty(specialty: Specialty): Long {
        return repository.addSpecialty(specialty)
    }

    fun getSpecialtiesByCollegeFlow(collegeId: Int) = repository.getSpecialtiesByCollegeFlow(collegeId)

    suspend fun getSpecialtiesByCollege(collegeId: Int) = repository.getSpecialtiesByCollege(collegeId)

    // Студенты
    suspend fun addStudent(student: Student): Long {
        return repository.addStudent(student)
    }

    suspend fun deleteStudent(student: Student): Int {
        return repository.deleteStudent(student)
    }

    fun getStudentsWithSpecialtyInfo(specialtyId: Int) = repository.getStudentsWithSpecialtyInfo(specialtyId)

    suspend fun getSpecialtyById(specialtyId: Int) = repository.getSpecialtyById(specialtyId)

    suspend fun fetchUniversitiesFromApi(country: String = "Belarus"): List<ApiUniversity> {
        return apiService.getUniversitiesByCountry(country)
    }

    suspend fun saveApiUniversities(apiUniversities: List<ApiUniversity>) {
        for (apiUni in apiUniversities) {
            val college = College(
                name = apiUni.name,
                region = getRegionFromName(apiUni.name),
                photoUrl = if (apiUni.webPages.isNotEmpty()) apiUni.webPages[0] else null
            )
            addCollege(college)
        }
    }

    private fun getRegionFromName(name: String): String {
        return when {
            name.contains("Минск", ignoreCase = true) -> "Минск"
            name.contains("Гродно", ignoreCase = true) -> "Гродно"
            name.contains("Витебск", ignoreCase = true) -> "Витебск"
            name.contains("Гомель", ignoreCase = true) -> "Гомель"
            name.contains("Брест", ignoreCase = true) -> "Брест"
            name.contains("Могилев", ignoreCase = true) -> "Могилев"
            name.contains("Mogilev", ignoreCase = true) -> "Могилев"
            name.contains("Vitebsk", ignoreCase = true) -> "Витебск"
            name.contains("Grodno", ignoreCase = true) -> "Гродно"
            name.contains("Gomel", ignoreCase = true) -> "Гомель"
            name.contains("Brest", ignoreCase = true) -> "Брест"
            else -> "Беларусь"
        }
    }
    suspend fun updateStudent(student: Student): Int {
        return repository.updateStudent(student)
    }
    suspend fun updateSpecialty(specialty: Specialty): Int {
        return repository.updateSpecialty(specialty)
    }

    suspend fun deleteSpecialty(specialty: Specialty): Int {
        return repository.deleteSpecialty(specialty)
    }

    fun getAllTeachers(): Flow<List<Teacher>> {
        return repository.getAllTeachers()
    }

    suspend fun addTeacher(teacher: Teacher): Long {
        return repository.addTeacher(teacher)
    }
    fun getSpecialtiesByStudent(studentId: Int) = repository.getSpecialtiesByStudent(studentId)


    fun getTeachersBySpecialty(specialtyId: Int): Flow<List<Teacher>> {
        return repository.getTeachersBySpecialty(specialtyId)
    }

    suspend fun getAllSpecialtiesList(): List<Specialty> {
        return repository.getAllSpecialtiesList()
    }

    suspend fun assignTeacherToSpecialty(teacherId: Int, specialtyId: Int, hoursPerWeek: Int) {
        repository.assignTeacherToSpecialty(teacherId, specialtyId, hoursPerWeek)
    }

}