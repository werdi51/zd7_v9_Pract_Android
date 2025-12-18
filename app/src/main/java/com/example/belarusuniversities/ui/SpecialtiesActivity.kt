package com.example.belarusuniversities.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.belarusuniversities.R
import com.example.belarusuniversities.data.Specialty
import com.example.belarusuniversities.data.UniversityDatabase
import com.example.belarusuniversities.data.UniversityRepository
import com.example.belarusuniversities.databinding.ActivitySpecialtiesBinding
import com.example.belarusuniversities.utils.PreferencesHelper
import com.example.belarusuniversities.viewmodel.UniversityViewModel
import com.example.belarusuniversities.viewmodel.UniversityViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SpecialtiesActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySpecialtiesBinding
    private lateinit var viewModel: UniversityViewModel
    private var currentCollegeId: Int = -1
    private var currentCollegeName: String = ""
    private lateinit var specialtyAdapter: SpecialtyAdapter
    private var selectedSpecialtyId: Int = -1
    private var isMinistry: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySpecialtiesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViewModel()
        currentCollegeId = intent.getIntExtra("COLLEGE_ID", -1)
        currentCollegeName = intent.getStringExtra("COLLEGE_NAME") ?: ""

        val prefs = PreferencesHelper(this)
        val userRole = prefs.getUserRole() ?: "student"
        isMinistry = userRole == "ministry"

        setupUI(isMinistry)
        loadSpecialties()
    }

    private fun initViewModel() {
        val database = UniversityDatabase.getDatabase(applicationContext)
        val repository = UniversityRepository(database.universityDao())
        val factory = UniversityViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[UniversityViewModel::class.java]
    }

    private fun setupUI(isMinistry: Boolean) {
        binding.tvCollegeName.text = "Колледж: $currentCollegeName"

        specialtyAdapter = SpecialtyAdapter(
            onItemClick = { specialty ->
                selectedSpecialtyId = specialty.id
                Toast.makeText(this, "Выбрано: ${specialty.name}", Toast.LENGTH_SHORT).show()
                loadTeachersForSpecialty(specialty.id)
            },
            onEditClick = { specialty ->
                showEditSpecialtyDialog(specialty)
            },
            onDeleteClick = { specialty ->
                showDeleteConfirmationDialog(specialty)
            },
            isEditable = isMinistry
        )

        binding.recyclerViewSpecialties.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewSpecialties.adapter = specialtyAdapter

        if (isMinistry) {
            binding.btnAddSpecialty.visibility = View.VISIBLE
            binding.btnAddSpecialty.setOnClickListener {
                showAddSpecialtyDialog()
            }
        } else {
            binding.btnAddSpecialty.visibility = View.GONE
        }

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnShowStudents.setOnClickListener {
            if (selectedSpecialtyId != -1) {
                val selectedSpecialty = specialtyAdapter.getSpecialtyById(selectedSpecialtyId)
                val intent = Intent(this, StudentsActivity::class.java).apply {
                    putExtra("SPECIALTY_ID", selectedSpecialtyId)
                    putExtra("SPECIALTY_NAME", selectedSpecialty?.name ?: "Специальность")
                }
                startActivity(intent)
            } else {
                Toast.makeText(this, "Выберите специальность", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnSearch.setOnClickListener {
            val searchText = binding.etSearch.text.toString()
            if (searchText.isNotEmpty()) {
                val filtered = specialtyAdapter.getSpecialties().filter {
                    it.name.contains(searchText, ignoreCase = true) ||
                            it.code.contains(searchText, ignoreCase = true)
                }
                specialtyAdapter.updateSpecialties(filtered)
                binding.tvSpecialtyCount.text = "Найдено специальностей: ${filtered.size}"
            } else {
                loadSpecialties()
            }
        }

        binding.tvTeachers.text = "Выберите специальность, чтобы увидеть преподавателей"

        binding.btnViewAllTeachers.visibility = View.VISIBLE
        binding.btnViewAllTeachers.text = "Перейти к списку преподавателей"
        binding.btnViewAllTeachers.setOnClickListener {
            val intent = Intent(this, TeachersActivity::class.java)
            startActivity(intent)
        }

        binding.btnShowStudents.text = "Показать студентов"
    }

    private fun loadSpecialties() {
        lifecycleScope.launch {
            try {
                viewModel.getSpecialtiesByCollegeFlow(currentCollegeId).collectLatest { specialties ->
                    specialtyAdapter.updateSpecialties(specialties)
                    binding.tvSpecialtyCount.text = "Специальностей: ${specialties.size}"

                    if (specialties.isEmpty()) {
                        binding.tvEmptyState.visibility = View.VISIBLE
                    } else {
                        binding.tvEmptyState.visibility = View.GONE
                    }
                }
            } catch (e: Exception) {
                if (e.message?.contains("Job was cancelled") == false) {
                    Toast.makeText(this@SpecialtiesActivity, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun loadTeachersForSpecialty(specialtyId: Int) {
        lifecycleScope.launch {
            try {
                viewModel.getTeachersBySpecialty(specialtyId).collect { teachers ->
                    if (teachers.isNotEmpty()) {
                        val teachersText = teachers.joinToString("\n\n") { teacher ->
                            "${teacher.name}\n" +
                                    "    ${teacher.email}\n" +
                                    "    ${teacher.currentHours}/${teacher.maxHoursPerYear} часов"
                        }
                        binding.tvTeachers.text = " Преподаватели этой специальности:\n\n$teachersText"
                        binding.tvTeachers.visibility = View.VISIBLE
                        binding.btnViewAllTeachers.visibility = View.VISIBLE
                    } else {
                        binding.tvTeachers.text = "На этой специальности пока нет преподавателей"
                        binding.tvTeachers.visibility = View.VISIBLE
                        binding.btnViewAllTeachers.visibility = View.VISIBLE
                    }
                }
            } catch (e: Exception) {
                binding.tvTeachers.text = "Ошибка загрузки преподавателей: ${e.message}"
            }
        }
    }

    private fun showAddSpecialtyDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_specialty, null)
        val etCode = dialogView.findViewById<EditText>(R.id.etSpecialtyCode)
        val etName = dialogView.findViewById<EditText>(R.id.etSpecialtyName)
        val etDescription = dialogView.findViewById<EditText>(R.id.etSpecialtyDescription)
        val etBudget = dialogView.findViewById<EditText>(R.id.etBudgetPlaces)
        val etContract = dialogView.findViewById<EditText>(R.id.etContractPlaces)
        val etYears = dialogView.findViewById<EditText>(R.id.etStudyYears)

        AlertDialog.Builder(this)
            .setTitle("Добавить специальность")
            .setView(dialogView)
            .setPositiveButton("Добавить") { _, _ ->
                val code = etCode.text.toString().trim()
                val name = etName.text.toString().trim()
                val description = etDescription.text.toString().trim()
                val budgetText = etBudget.text.toString()
                val contractText = etContract.text.toString()
                val yearsText = etYears.text.toString()

                if (code.isEmpty() || name.isEmpty()) {
                    Toast.makeText(this, "Заполните код и название", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val budget = budgetText.toIntOrNull() ?: 0
                val contract = contractText.toIntOrNull() ?: 0
                val years = yearsText.toIntOrNull() ?: 4

                lifecycleScope.launch {
                    try {
                        val specialty = Specialty(
                            collegeId = currentCollegeId,
                            code = code,
                            name = name,
                            description = description,
                            budgetPlaces = budget,
                            contractPlaces = contract,
                            studyYears = years
                        )

                        viewModel.addSpecialty(specialty)
                        Toast.makeText(
                            this@SpecialtiesActivity,
                            "Специальность добавлена",
                            Toast.LENGTH_SHORT
                        ).show()
                        loadSpecialties()
                    } catch (e: Exception) {
                        Toast.makeText(
                            this@SpecialtiesActivity,
                            "Ошибка: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun showEditSpecialtyDialog(specialty: Specialty) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_specialty, null)
        val etCode = dialogView.findViewById<EditText>(R.id.etSpecialtyCode)
        val etName = dialogView.findViewById<EditText>(R.id.etSpecialtyName)
        val etDescription = dialogView.findViewById<EditText>(R.id.etSpecialtyDescription)
        val etBudget = dialogView.findViewById<EditText>(R.id.etBudgetPlaces)
        val etContract = dialogView.findViewById<EditText>(R.id.etContractPlaces)
        val etYears = dialogView.findViewById<EditText>(R.id.etStudyYears)

        etCode.setText(specialty.code)
        etName.setText(specialty.name)
        etDescription.setText(specialty.description)
        etBudget.setText(specialty.budgetPlaces.toString())
        etContract.setText(specialty.contractPlaces.toString())
        etYears.setText(specialty.studyYears.toString())

        AlertDialog.Builder(this)
            .setTitle("Редактировать специальность")
            .setView(dialogView)
            .setPositiveButton("Сохранить") { _, _ ->
                val code = etCode.text.toString().trim()
                val name = etName.text.toString().trim()
                val description = etDescription.text.toString().trim()
                val budgetText = etBudget.text.toString()
                val contractText = etContract.text.toString()
                val yearsText = etYears.text.toString()

                if (code.isEmpty() || name.isEmpty()) {
                    Toast.makeText(this, "Заполните код и название", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val budget = budgetText.toIntOrNull() ?: 0
                val contract = contractText.toIntOrNull() ?: 0
                val years = yearsText.toIntOrNull() ?: 4

                lifecycleScope.launch {
                    try {
                        val updatedSpecialty = Specialty(
                            id = specialty.id,
                            collegeId = specialty.collegeId,
                            code = code,
                            name = name,
                            description = description,
                            budgetPlaces = budget,
                            contractPlaces = contract,
                            studyYears = years
                        )

                        val rowsUpdated = viewModel.updateSpecialty(updatedSpecialty)

                        if (rowsUpdated > 0) {
                            Toast.makeText(
                                this@SpecialtiesActivity,
                                "Специальность обновлена",
                                Toast.LENGTH_SHORT
                            ).show()
                            loadSpecialties()
                        } else {
                            Toast.makeText(
                                this@SpecialtiesActivity,
                                "Ошибка обновления",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(
                            this@SpecialtiesActivity,
                            "Ошибка: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                        e.printStackTrace()
                    }
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun showDeleteConfirmationDialog(specialty: Specialty) {
        AlertDialog.Builder(this)
            .setTitle("Удаление специальности")
            .setMessage("Вы уверены, что хотите удалить специальность \"${specialty.name}\"?")
            .setPositiveButton("Удалить") { _, _ ->
                lifecycleScope.launch {
                    try {
                        val rowsDeleted = viewModel.deleteSpecialty(specialty)

                        if (rowsDeleted > 0) {
                            Toast.makeText(
                                this@SpecialtiesActivity,
                                "Специальность удалена",
                                Toast.LENGTH_SHORT
                            ).show()
                            loadSpecialties()
                        } else {
                            Toast.makeText(
                                this@SpecialtiesActivity,
                                "Ошибка удаления",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(
                            this@SpecialtiesActivity,
                            "Ошибка: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                        e.printStackTrace()
                    }
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }
}