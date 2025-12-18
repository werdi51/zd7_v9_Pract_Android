package com.example.belarusuniversities.ui

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.belarusuniversities.R
import com.example.belarusuniversities.data.Student
import com.example.belarusuniversities.data.StudentWithSpecialty
import com.example.belarusuniversities.data.UniversityDatabase
import com.example.belarusuniversities.data.UniversityRepository
import com.example.belarusuniversities.databinding.ActivityStudentsBinding
import com.example.belarusuniversities.utils.PreferencesHelper
import com.example.belarusuniversities.viewmodel.UniversityViewModel
import com.example.belarusuniversities.viewmodel.UniversityViewModelFactory
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class StudentsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudentsBinding
    private lateinit var viewModel: UniversityViewModel
    private var currentSpecialtyId: Int = -1
    private var specialtyName: String = ""
    private lateinit var studentAdapter: StudentAdapter
    private var currentStudents: List<StudentWithSpecialty> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViewModel()

        currentSpecialtyId = intent.getIntExtra("SPECIALTY_ID", -1)
        specialtyName = intent.getStringExtra("SPECIALTY_NAME") ?: ""

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Студенты: $specialtyName"

        val prefs = PreferencesHelper(this)
        val userRole = prefs.getUserRole() ?: "student"
        val isEditable = userRole == "teacher" || userRole == "ministry"

        setupRecyclerView(isEditable)

        setupSearchAndSort()

        if (isEditable) {
            binding.fabAddStudent.visibility = View.VISIBLE
            binding.fabAddStudent.setOnClickListener {
                showAddStudentDialog()
            }
        } else {
            binding.fabAddStudent.visibility = View.GONE
        }

        loadStudents()
    }

    private fun initViewModel() {
        val database = UniversityDatabase.getDatabase(applicationContext)
        val repository = UniversityRepository(database.universityDao())
        val factory = UniversityViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[UniversityViewModel::class.java]
    }

    private fun setupRecyclerView(isEditable: Boolean) {
        studentAdapter = StudentAdapter(
            onEditClick = { studentWithSpecialty ->
                showEditStudentDialog(studentWithSpecialty)
            },
            onDeleteClick = { studentWithSpecialty ->
                showDeleteConfirmationDialog(studentWithSpecialty)
            },
            isEditable = isEditable
        )

        binding.recyclerViewStudents.apply {
            layoutManager = LinearLayoutManager(this@StudentsActivity)
            adapter = studentAdapter
        }
    }

    private fun setupSearchAndSort() {
        binding.searchView.setOnQueryTextListener(object : android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                filterStudents(newText)
                return true
            }
        })

        binding.btnSort.setOnClickListener {
            showSortOptionsDialog()
        }
    }

    private fun showSortOptionsDialog() {
        val sortOptions = arrayOf("По имени (А-Я)", "По имени (Я-А)", "По баллу (↑)", "По баллу (↓)", "Бюджетные", "Контрактные")

        AlertDialog.Builder(this)
            .setTitle("Сортировка")
            .setItems(sortOptions) { _, which ->
                sortStudents(which)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun loadStudents() {
        lifecycleScope.launch {
            viewModel.getStudentsWithSpecialtyInfo(currentSpecialtyId).collect { students ->
                currentStudents = students

                if (students.isEmpty()) {
                    binding.tvEmptyState.visibility = View.VISIBLE
                } else {
                    binding.tvEmptyState.visibility = View.GONE
                    studentAdapter.submitList(students)

                    showStatistics(students)
                }
            }
        }
    }

    private fun showStatistics(students: List<StudentWithSpecialty>) {
        val budgetCount = students.count { it.student.isBudget }
        val contractCount = students.count { !it.student.isBudget }

        if (budgetCount + contractCount > 0) {
            binding.tvEmptyState.text = "Студентов: ${budgetCount + contractCount} (бюджет: $budgetCount, контракт: $contractCount)"
        }
    }

    private fun filterStudents(query: String?) {
        val filtered = if (query.isNullOrEmpty()) {
            currentStudents
        } else {
            currentStudents.filter { student ->
                student.student.fullName.contains(query, ignoreCase = true)
            }
        }
        studentAdapter.submitList(filtered)
    }

    private fun sortStudents(sortType: Int) {
        val sortedList = when (sortType) {
            0 -> currentStudents.sortedBy { it.student.fullName }
            1 -> currentStudents.sortedByDescending { it.student.fullName }
            2 -> currentStudents.sortedBy { it.student.certificateScore }
            3 -> currentStudents.sortedByDescending { it.student.certificateScore }
            4 -> currentStudents.filter { it.student.isBudget }
            5 -> currentStudents.filter { !it.student.isBudget }
            else -> currentStudents
        }
        studentAdapter.submitList(sortedList)
    }

    private suspend fun canAddBudgetStudent(): Boolean {
        val budgetCount = currentStudents.count { it.student.isBudget }
        if (budgetCount >= 50) {
            Toast.makeText(this, "Достигнут лимит: 50 бюджетных мест", Toast.LENGTH_LONG).show()
            return false
        }
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun showAddStudentDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_student, null)
        val etName = dialogView.findViewById<android.widget.EditText>(R.id.etName)
        val etScore = dialogView.findViewById<android.widget.EditText>(R.id.etScore)
        val switchBudget = dialogView.findViewById<android.widget.Switch>(R.id.switchBudget)
        val tvSelectedDate = dialogView.findViewById<android.widget.TextView>(R.id.tvSelectedDate)
        val btnPickDate = dialogView.findViewById<android.widget.Button>(R.id.btnPickDate)

        var selectedBirthDate = Calendar.getInstance().apply {
            set(2000, Calendar.JANUARY, 1) // Дата по умолчанию: 01.01.2000
        }.timeInMillis

        fun updateDateDisplay() {
            val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            tvSelectedDate.text = dateFormat.format(Date(selectedBirthDate))
        }

        updateDateDisplay()

        btnPickDate.setOnClickListener {
            val calendar = Calendar.getInstance().apply {
                timeInMillis = selectedBirthDate
            }

            val datePicker = android.app.DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    val newCalendar = Calendar.getInstance().apply {
                        set(year, month, dayOfMonth)
                    }
                    selectedBirthDate = newCalendar.timeInMillis
                    updateDateDisplay()
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )

            datePicker.datePicker.maxDate = System.currentTimeMillis()
            datePicker.datePicker.minDate = 0

            datePicker.show()
        }

        AlertDialog.Builder(this)
            .setTitle("Добавить студента")
            .setView(dialogView)
            .setPositiveButton("Добавить") { _, _ ->
                val name = etName.text.toString().trim()
                val scoreText = etScore.text.toString()

                if (name.isEmpty() || scoreText.isEmpty()) {
                    Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val score = scoreText.toDoubleOrNull() ?: 0.0

                if (score < 0 || score > 100) {
                    Toast.makeText(this, "Балл должен быть от 0 до 100", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                lifecycleScope.launch {
                    if (switchBudget.isChecked && !canAddBudgetStudent()) {
                        return@launch
                    }

                    val student = Student(
                        specialtyId = currentSpecialtyId,
                        fullName = name,
                        birthDate = selectedBirthDate,
                        certificateScore = score,
                        isBudget = switchBudget.isChecked
                    )

                    viewModel.addStudent(student)
                    Toast.makeText(
                        this@StudentsActivity,
                        "Студент добавлен${if (switchBudget.isChecked) " (бюджет)" else " (контракт)"}",
                        Toast.LENGTH_SHORT
                    ).show()
                    loadStudents()
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun showEditStudentDialog(studentWithSpecialty: StudentWithSpecialty) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_student, null)
        val etName = dialogView.findViewById<android.widget.EditText>(R.id.etName)
        val etScore = dialogView.findViewById<android.widget.EditText>(R.id.etScore)
        val switchBudget = dialogView.findViewById<android.widget.Switch>(R.id.switchBudget)
        val tvSelectedDate = dialogView.findViewById<android.widget.TextView>(R.id.tvSelectedDate)
        val btnPickDate = dialogView.findViewById<android.widget.Button>(R.id.btnPickDate)

        val student = studentWithSpecialty.student
        etName.setText(student.fullName)
        etScore.setText(student.certificateScore.toString())
        switchBudget.isChecked = student.isBudget

        var selectedBirthDate = student.birthDate

        fun updateDateDisplay() {
            val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            tvSelectedDate.text = dateFormat.format(Date(selectedBirthDate))
        }

        updateDateDisplay()

        btnPickDate.setOnClickListener {
            val calendar = Calendar.getInstance().apply {
                timeInMillis = selectedBirthDate
            }

            val datePicker = android.app.DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    val newCalendar = Calendar.getInstance().apply {
                        set(year, month, dayOfMonth)
                    }
                    selectedBirthDate = newCalendar.timeInMillis
                    updateDateDisplay()
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )

            datePicker.datePicker.maxDate = System.currentTimeMillis()
            datePicker.show()
        }

        AlertDialog.Builder(this)
            .setTitle("Редактировать студента")
            .setView(dialogView)
            .setPositiveButton("Сохранить") { dialog, _ ->
                val name = etName.text.toString().trim()
                val scoreText = etScore.text.toString()

                if (name.isEmpty() || scoreText.isEmpty()) {
                    Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val score = scoreText.toDoubleOrNull() ?: 0.0
                if (score < 0 || score > 100) {
                    Toast.makeText(this, "Балл должен быть от 0 до 100", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                lifecycleScope.launch {
                    try {
                        if (switchBudget.isChecked && !student.isBudget) {
                            val budgetCount = currentStudents.count { it.student.isBudget }
                            if (budgetCount >= 50) {
                                Toast.makeText(
                                    this@StudentsActivity,
                                    "Достигнут лимит: 50 бюджетных мест",
                                    Toast.LENGTH_LONG
                                ).show()
                                return@launch
                            }
                        }

                        val updatedStudent = Student(
                            id = student.id, // Тот же ID!
                            specialtyId = student.specialtyId,
                            fullName = name,
                            birthDate = selectedBirthDate,
                            certificateScore = score,
                            isBudget = switchBudget.isChecked,
                            enrollmentDate = student.enrollmentDate
                        )

                        // Обновляем в БД
                        val rowsUpdated = viewModel.updateStudent(updatedStudent)

                        if (rowsUpdated > 0) {
                            Toast.makeText(
                                this@StudentsActivity,
                                "Студент обновлен",
                                Toast.LENGTH_SHORT
                            ).show()
                            loadStudents() // Перезагружаем список
                        } else {
                            Toast.makeText(
                                this@StudentsActivity,
                                "Ошибка обновления",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    } catch (e: Exception) {
                        Toast.makeText(
                            this@StudentsActivity,
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

    private fun showDeleteConfirmationDialog(studentWithSpecialty: StudentWithSpecialty) {
        AlertDialog.Builder(this)
            .setTitle("Удаление студента")
            .setMessage("Вы уверены, что хотите удалить ${studentWithSpecialty.student.fullName}?")
            .setPositiveButton("Удалить") { _, _ ->
                lifecycleScope.launch {
                    viewModel.deleteStudent(studentWithSpecialty.student)
                    Toast.makeText(this@StudentsActivity, "Студент удален", Toast.LENGTH_SHORT).show()
                    loadStudents()
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }
}