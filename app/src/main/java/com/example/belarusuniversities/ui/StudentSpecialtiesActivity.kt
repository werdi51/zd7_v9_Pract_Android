package com.example.belarusuniversities.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.belarusuniversities.data.UniversityDatabase
import com.example.belarusuniversities.data.UniversityRepository
import com.example.belarusuniversities.databinding.ActivityStudentSpecialtiesBinding
import com.example.belarusuniversities.viewmodel.UniversityViewModel
import com.example.belarusuniversities.viewmodel.UniversityViewModelFactory
import kotlinx.coroutines.launch

class StudentSpecialtiesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudentSpecialtiesBinding
    private lateinit var viewModel: UniversityViewModel
    private var studentId: Int = -1
    private var studentName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentSpecialtiesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        studentId = intent.getIntExtra("STUDENT_ID", -1)
        studentName = intent.getStringExtra("STUDENT_NAME") ?: ""

        initViewModel()
        setupUI()
        loadStudentSpecialties()
    }

    private fun initViewModel() {
        val database = UniversityDatabase.getDatabase(applicationContext)
        val repository = UniversityRepository(database.universityDao())
        val factory = UniversityViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[UniversityViewModel::class.java]
    }

    private fun setupUI() {
        supportActionBar?.title = "Специальности студента: $studentName"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun loadStudentSpecialties() {
        lifecycleScope.launch {
            try {
                binding.tvSpecialtiesList.text = "потом"

                Toast.makeText(
                    this@StudentSpecialtiesActivity,
                    "UI",
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: Exception) {
                binding.tvSpecialtiesList.text = "Ошибка загрузки: ${e.message}"
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}