package com.example.belarusuniversities.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.belarusuniversities.data.UniversityRepository
import com.example.belarusuniversities.data.UniversityDao // ← ДОБАВИТЬ ЭТОТ ИМПОРТ

class UniversityViewModelFactory(
    private val repository: UniversityRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UniversityViewModel::class.java)) {
            return UniversityViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

    companion object {
        fun create(dao: UniversityDao): UniversityViewModelFactory {
            val repository = UniversityRepository(dao)
            return UniversityViewModelFactory(repository)
        }
    }
}