package com.example.belarusuniversities.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.belarusuniversities.R
import com.example.belarusuniversities.data.Specialty

class SpecialtyAdapter(
    private var specialties: List<Specialty> = emptyList(),
    private val onItemClick: (Specialty) -> Unit,
    private val onEditClick: (Specialty) -> Unit,
    private val onDeleteClick: (Specialty) -> Unit,
    private val isEditable: Boolean = false
) : RecyclerView.Adapter<SpecialtyAdapter.SpecialtyViewHolder>() {

    class SpecialtyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCode: TextView = itemView.findViewById(R.id.tvSpecialtyCode)
        val tvName: TextView = itemView.findViewById(R.id.tvSpecialtyName)
        val tvDescription: TextView = itemView.findViewById(R.id.tvSpecialtyDescription)
        val tvBudget: TextView = itemView.findViewById(R.id.tvBudgetPlaces)
        val tvContract: TextView = itemView.findViewById(R.id.tvContractPlaces)
        val tvYears: TextView = itemView.findViewById(R.id.tvStudyYears)
        val btnEdit: TextView = itemView.findViewById(R.id.btnEditSpecialty)
        val btnDelete: TextView = itemView.findViewById(R.id.btnDeleteSpecialty)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpecialtyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_specialty, parent, false)
        return SpecialtyViewHolder(view)
    }

    override fun onBindViewHolder(holder: SpecialtyViewHolder, position: Int) {
        val specialty = specialties[position]

        holder.tvCode.text = "Код: ${specialty.code}"
        holder.tvName.text = specialty.name
        holder.tvDescription.text = specialty.description
        holder.tvBudget.text = "Бюджет: ${specialty.budgetPlaces}"
        holder.tvContract.text = "Контракт: ${specialty.contractPlaces}"
        holder.tvYears.text = "${specialty.studyYears} года"

        holder.itemView.setBackgroundColor(
            if (specialty.id == selectedSpecialtyId) {
                holder.itemView.context.getColor(android.R.color.holo_blue_light)
            } else {
                holder.itemView.context.getColor(android.R.color.transparent)
            }
        )

        holder.itemView.setOnClickListener {
            selectedSpecialtyId = specialty.id
            notifyDataSetChanged()
            onItemClick(specialty)
        }

        if (isEditable) {
            holder.btnEdit.visibility = View.VISIBLE
            holder.btnDelete.visibility = View.VISIBLE

            holder.btnEdit.setOnClickListener { onEditClick(specialty) }
            holder.btnDelete.setOnClickListener { onDeleteClick(specialty) }
        } else {
            holder.btnEdit.visibility = View.GONE
            holder.btnDelete.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = specialties.size

    fun updateSpecialties(newSpecialties: List<Specialty>) {
        specialties = newSpecialties
        notifyDataSetChanged()
    }

    fun getSpecialtyById(specialtyId: Int): Specialty? {
        return specialties.find { it.id == specialtyId }
    }

    fun getSpecialties(): List<Specialty> {
        return specialties
    }

    private var selectedSpecialtyId: Int = -1
}