package com.example.belarusuniversities.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.belarusuniversities.R
import com.example.belarusuniversities.data.College
import com.squareup.picasso.Picasso
import java.net.URL
import kotlin.math.absoluteValue

class CollegeAdapter(
    private var colleges: List<College> = emptyList(),
    private val onItemClick: (College) -> Unit
) : RecyclerView.Adapter<CollegeAdapter.CollegeViewHolder>() {

    class CollegeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivPhoto: ImageView = itemView.findViewById(R.id.ivCollegePhoto)
        val tvName: TextView = itemView.findViewById(R.id.tvCollegeName)
        val tvRegion: TextView = itemView.findViewById(R.id.tvCollegeRegion)
        val tvId: TextView = itemView.findViewById(R.id.tvCollegeId)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CollegeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_college, parent, false)
        return CollegeViewHolder(view)
    }

    override fun onBindViewHolder(holder: CollegeViewHolder, position: Int) {
        val college = colleges[position]

        holder.tvName.text = college.name
        holder.tvRegion.text = "Регион: ${college.region}"
        holder.tvId.text = "ID: ${college.id}"

        if (!college.photoUrl.isNullOrEmpty()) {
            val faviconUrl = getFaviconUrl(college.photoUrl!!)

            Picasso.get()
                .load(faviconUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_gallery)
                .resize(60, 60)
                .centerCrop()
                .into(holder.ivPhoto)
        } else {
            showInitial(college.name, holder.ivPhoto)
        }

        holder.itemView.setOnClickListener {
            onItemClick(college)
        }
    }

    override fun getItemCount(): Int = colleges.size

    fun updateColleges(newColleges: List<College>) {
        colleges = newColleges
        notifyDataSetChanged()
    }

    private fun getFaviconUrl(websiteUrl: String): String {
        return try {
            val url = URL(websiteUrl)
            val domain = url.host
            "https://www.google.com/s2/favicons?domain=$domain&sz=64"
        } catch (e: Exception) {
            ""
        }
    }

    private fun showInitial(name: String, imageView: ImageView) {
        imageView.setImageDrawable(null)

        if (name.isNotEmpty()) {
            imageView.setBackgroundColor(getRandomColor(name))
        } else {
            imageView.setImageResource(android.R.drawable.ic_menu_gallery)
            imageView.setBackgroundColor(Color.TRANSPARENT)
        }
    }

    private fun getRandomColor(name: String): Int {
        val colors = listOf(
            Color.parseColor("#FF6B6B"),
            Color.parseColor("#4ECDC4"),
            Color.parseColor("#45B7D1"),
            Color.parseColor("#96CEB4"),
            Color.parseColor("#FFEAA7"),
            Color.parseColor("#DDA0DD"),
            Color.parseColor("#F8A5C2")
        )
        val index = name.hashCode().absoluteValue % colors.size
        return colors[index]
    }
}