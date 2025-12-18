package com.example.belarusuniversities.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.belarusuniversities.data.StudentWithSpecialty
import com.example.belarusuniversities.databinding.ItemStudentBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StudentAdapter(
    private val onEditClick: (StudentWithSpecialty) -> Unit,
    private val onDeleteClick: (StudentWithSpecialty) -> Unit,
    private val isEditable: Boolean = false
) : RecyclerView.Adapter<StudentAdapter.StudentViewHolder>() {

    private var students = emptyList<StudentWithSpecialty>()

    inner class StudentViewHolder(
        private val binding: ItemStudentBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(studentWithSpecialty: StudentWithSpecialty) {
            val student = studentWithSpecialty.student

            binding.tvStudentName.text = student.fullName
            binding.tvSpecialtyName.text = studentWithSpecialty.specialtyName

            val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            binding.tvBirthDate.text = "Дата рождения: ${dateFormat.format(Date(student.birthDate))}"

            binding.tvCertificateScore.text = "Балл: ${student.certificateScore}"
            binding.tvStudyType.text = if (student.isBudget) "Бюджет" else "Контракт"

            if (isEditable) {
                binding.btnEdit.visibility = android.view.View.VISIBLE
                binding.btnDelete.visibility = android.view.View.VISIBLE

                binding.btnEdit.setOnClickListener {
                    onEditClick(studentWithSpecialty)
                }

                binding.btnDelete.setOnClickListener {
                    onDeleteClick(studentWithSpecialty)
                }
            } else {
                binding.btnEdit.visibility = android.view.View.GONE
                binding.btnDelete.visibility = android.view.View.GONE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        val binding = ItemStudentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return StudentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        holder.bind(students[position])
    }

    override fun getItemCount(): Int = students.size

    fun submitList(newList: List<StudentWithSpecialty>) {
        students = newList
        notifyDataSetChanged()
    }

    fun getCurrentList(): List<StudentWithSpecialty> {
        return students
    }
}