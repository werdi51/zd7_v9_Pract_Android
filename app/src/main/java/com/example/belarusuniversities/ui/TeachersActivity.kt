package com.example.belarusuniversities.ui

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.belarusuniversities.R
import com.example.belarusuniversities.data.Specialty
import com.example.belarusuniversities.data.Teacher
import com.example.belarusuniversities.data.UniversityDatabase
import com.example.belarusuniversities.data.UniversityRepository
import com.example.belarusuniversities.databinding.ActivityTeachersBinding
import com.example.belarusuniversities.utils.PreferencesHelper
import com.example.belarusuniversities.viewmodel.UniversityViewModel
import com.example.belarusuniversities.viewmodel.UniversityViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class TeachersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTeachersBinding
    private lateinit var viewModel: UniversityViewModel
    private lateinit var teachersAdapter: TeachersAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTeachersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViewModel()
        setupUI()
        loadTeachers()
    }

    private fun initViewModel() {
        val database = UniversityDatabase.getDatabase(applicationContext)
        val repository = UniversityRepository(database.universityDao())
        val factory = UniversityViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[UniversityViewModel::class.java]
    }

    private fun setupUI() {
        teachersAdapter = TeachersAdapter(
            onItemClick = { teacher ->
                showTeacherDetailsDialog(teacher)
            }
        )

        binding.recyclerViewTeachers.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewTeachers.adapter = teachersAdapter

        val prefs = PreferencesHelper(this)
        val userRole = prefs.getUserRole() ?: "student"
        val isMinistry = userRole == "ministry"

        if (isMinistry) {
            binding.btnAddTeacher.visibility = View.VISIBLE
            binding.btnAddTeacher.setOnClickListener {
                showAddTeacherDialog()
            }
        } else {
            binding.btnAddTeacher.visibility = View.GONE
        }
    }

    private fun loadTeachers() {
        lifecycleScope.launch {
            viewModel.getAllTeachers().collectLatest { teachers ->
                teachersAdapter.submitList(teachers)
                binding.tvTeacherCount.text = "Преподавателей: ${teachers.size}"

                if (teachers.isEmpty()) {
                    binding.tvEmptyState.visibility = View.VISIBLE
                } else {
                    binding.tvEmptyState.visibility = View.GONE
                }
            }
        }
    }

    private fun showAddTeacherDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_teacher, null)
        val etName = dialogView.findViewById<android.widget.EditText>(R.id.etTeacherName)
        val etEmail = dialogView.findViewById<android.widget.EditText>(R.id.etTeacherEmail)
        val etMaxHours = dialogView.findViewById<android.widget.EditText>(R.id.etMaxHours)

        AlertDialog.Builder(this)
            .setTitle("Добавить преподавателя")
            .setView(dialogView)
            .setPositiveButton("Добавить") { _, _ ->
                val name = etName.text.toString().trim()
                val email = etEmail.text.toString().trim()
                val maxHoursText = etMaxHours.text.toString()

                if (name.isEmpty() || email.isEmpty() || maxHoursText.isEmpty()) {
                    Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val maxHours = maxHoursText.toIntOrNull() ?: 1440

                if (maxHours <= 0) {
                    Toast.makeText(this, "Макс. часов должно быть больше 0", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                lifecycleScope.launch {
                    try {
                        val teacher = Teacher(
                            name = name,
                            email = email,
                            maxHoursPerYear = maxHours,
                            currentHours = 0,
                            hourlyRate = 20.0,
                            bonusRate = 1.5
                        )

                        viewModel.addTeacher(teacher)
                        Toast.makeText(
                            this@TeachersActivity,
                            "Преподаватель добавлен",
                            Toast.LENGTH_SHORT
                        ).show()
                        loadTeachers()
                    } catch (e: Exception) {
                        Toast.makeText(
                            this@TeachersActivity,
                            "Ошибка: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun showTeacherDetailsDialog(teacher: Teacher) {
        val regularHours = minOf(teacher.currentHours, teacher.maxHoursPerYear)
        val overtimeHours = maxOf(0, teacher.currentHours - teacher.maxHoursPerYear)

        val regularSalary = regularHours * teacher.hourlyRate
        val overtimeSalary = overtimeHours * teacher.hourlyRate * teacher.bonusRate
        val totalSalary = regularSalary + overtimeSalary

        val percentage = if (teacher.maxHoursPerYear > 0) {
            (teacher.currentHours.toFloat() / teacher.maxHoursPerYear * 100).toInt()
        } else {
            0
        }

        // Сообщение с эмодзи
        val message = """
             ${teacher.name}
             ${teacher.email}
            
            Нагрузка:
            • Текущая: ${teacher.currentHours} часов
            • Максимальная: ${teacher.maxHoursPerYear} часов
            • Загруженность: $percentage%
            
             Зарплата:
            • Обычные часы: $regularHours × $${teacher.hourlyRate} = $${"%.2f".format(regularSalary)}
            • Переработка: $overtimeHours × $${teacher.hourlyRate * teacher.bonusRate} = $${"%.2f".format(overtimeSalary)}
            • ИТОГО: $${"%.2f".format(totalSalary)}
            
            ${if (overtimeHours > 0) " Есть переработка" else " В пределах нормы"}
            """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("Информация о преподавателе")
            .setMessage(message)
            .setPositiveButton("Назначить на специальность") { _, _ ->
                showAssignToSpecialtyDialog(teacher)
            }
            .setNegativeButton("Закрыть", null)
            .show()
    }

    private fun showAssignToSpecialtyDialog(teacher: Teacher) {
        lifecycleScope.launch {
            try {
                val specialties = viewModel.getAllSpecialtiesList()

                if (specialties.isEmpty()) {
                    Toast.makeText(this@TeachersActivity, "Нет доступных специальностей", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val specialtyNames = specialties.map { "${it.code} - ${it.name}" }.toTypedArray()

                AlertDialog.Builder(this@TeachersActivity)
                    .setTitle("Назначить ${teacher.name} на специальность")
                    .setItems(specialtyNames) { _, which ->
                        val selectedSpecialty = specialties[which]
                        showHoursInputDialog(teacher, selectedSpecialty)
                    }
                    .setNegativeButton("Отмена", null)
                    .show()

            } catch (e: Exception) {
                Toast.makeText(this@TeachersActivity, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showHoursInputDialog(teacher: Teacher, specialty: Specialty) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_hours_input, null)
        val etHours = dialogView.findViewById<android.widget.EditText>(R.id.etHoursPerWeek)

        AlertDialog.Builder(this)
            .setTitle("Часы нагрузки")
            .setMessage("Сколько часов в неделю будет преподавать ${teacher.name} на специальности '${specialty.name}'?")
            .setView(dialogView)
            .setPositiveButton("Назначить") { _, _ ->
                val hoursText = etHours.text.toString()

                if (hoursText.isEmpty()) {
                    Toast.makeText(this, "Введите количество часов", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val hoursPerWeek = hoursText.toIntOrNull() ?: 0

                if (hoursPerWeek <= 0) {
                    Toast.makeText(this, "Часы должны быть больше 0", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                lifecycleScope.launch {
                    try {
                        viewModel.assignTeacherToSpecialty(teacher.id, specialty.id, hoursPerWeek)
                        Toast.makeText(
                            this@TeachersActivity,
                            "${teacher.name} назначен на '${specialty.name}' (${hoursPerWeek} часов/неделю)",
                            Toast.LENGTH_LONG
                        ).show()

                        loadTeachers()

                    } catch (e: Exception) {
                        Toast.makeText(
                            this@TeachersActivity,
                            "Ошибка: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    inner class TeachersAdapter(
        private val onItemClick: (Teacher) -> Unit
    ) : RecyclerView.Adapter<TeachersAdapter.TeacherViewHolder>() {

        private var teachers: List<Teacher> = emptyList()

        inner class TeacherViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvName: TextView = itemView.findViewById(R.id.tvTeacherName)
            val tvEmail: TextView = itemView.findViewById(R.id.tvTeacherEmail)
            val tvHours: TextView = itemView.findViewById(R.id.tvTeacherHours)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TeacherViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_teacher_simple, parent, false)
            return TeacherViewHolder(view)
        }

        override fun onBindViewHolder(holder: TeacherViewHolder, position: Int) {
            val teacher = teachers[position]

            holder.tvName.text = teacher.name
            holder.tvEmail.text = teacher.email

            val percentage = if (teacher.maxHoursPerYear > 0) {
                (teacher.currentHours * 100 / teacher.maxHoursPerYear).toInt()
            } else {
                0
            }

            holder.tvHours.text =
                "Нагрузка: ${teacher.currentHours}/${teacher.maxHoursPerYear} часов ($percentage%)"

            when {
                percentage >= 100 -> holder.tvHours.setTextColor(Color.RED)
                percentage >= 90 -> holder.tvHours.setTextColor(Color.parseColor("#FFA500"))
                else -> holder.tvHours.setTextColor(Color.parseColor("#4CAF50"))
            }

            holder.itemView.setOnClickListener {
                onItemClick(teacher)
            }
        }

        override fun getItemCount(): Int = teachers.size

        fun submitList(newTeachers: List<Teacher>) {
            teachers = newTeachers
            notifyDataSetChanged()
        }
    }
}