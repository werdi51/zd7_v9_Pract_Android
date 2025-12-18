package com.example.belarusuniversities.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.belarusuniversities.R
import com.example.belarusuniversities.data.College
import com.example.belarusuniversities.data.UniversityDatabase
import com.example.belarusuniversities.data.UniversityRepository
import com.example.belarusuniversities.databinding.ActivityCarouselBinding
import com.example.belarusuniversities.viewmodel.UniversityViewModel
import com.example.belarusuniversities.viewmodel.UniversityViewModelFactory
import com.squareup.picasso.Picasso
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

class CarouselActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCarouselBinding
    private lateinit var viewModel: UniversityViewModel
    private lateinit var carouselAdapter: CarouselAdapter
    private var selectedCollege: College? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCarouselBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViewModel()
        setupUI()
        loadColleges()
    }

    private fun initViewModel() {
        val database = UniversityDatabase.getDatabase(applicationContext)
        val repository = UniversityRepository(database.universityDao())
        val factory = UniversityViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[UniversityViewModel::class.java]
    }

    private fun setupUI() {
        carouselAdapter = CarouselAdapter { college ->
            selectedCollege = college
            binding.tvSelectedCollege.text = "Выбран: ${college.name}"
            binding.btnGoToSpecialties.visibility = View.VISIBLE
        }

        binding.recyclerViewCarousel.apply {
            layoutManager = LinearLayoutManager(this@CarouselActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = carouselAdapter
        }

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnGoToSpecialties.setOnClickListener {
            selectedCollege?.let { college ->
                val intent = Intent(this, SpecialtiesActivity::class.java).apply {
                    putExtra("COLLEGE_ID", college.id)
                    putExtra("COLLEGE_NAME", college.name)
                }
                startActivity(intent)
            } ?: run {
                Toast.makeText(this, "Сначала выберите колледж", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadColleges() {
        lifecycleScope.launch {
            viewModel.collegesState.collectLatest { colleges ->
                carouselAdapter.submitList(colleges)

                if (colleges.isNotEmpty()) {
                    selectedCollege = colleges.first()
                    binding.tvSelectedCollege.text = "Выбран: ${selectedCollege?.name}"
                    binding.btnGoToSpecialties.visibility = View.VISIBLE
                }
            }
        }
    }

    inner class CarouselAdapter(
        private val onItemClick: (College) -> Unit
    ) : RecyclerView.Adapter<CarouselAdapter.CarouselViewHolder>() {

        private var colleges: List<College> = emptyList()

        inner class CarouselViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val ivPhoto: ImageView = itemView.findViewById(R.id.ivCollegePhoto)
            val tvName: TextView = itemView.findViewById(R.id.tvCollegeName)
            val tvRegion: TextView = itemView.findViewById(R.id.tvCollegeRegion)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarouselViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_college_carousel, parent, false)
            return CarouselViewHolder(view)
        }

        override fun onBindViewHolder(holder: CarouselViewHolder, position: Int) {
            val college = colleges[position]

            holder.tvName.text = college.name
            holder.tvRegion.text = "Регион: ${college.region}"

            if (!college.photoUrl.isNullOrEmpty() && college.photoUrl!!.startsWith("http")) {
                Picasso.get()
                    .load(college.photoUrl)
                    .placeholder(R.drawable.ic_university_placeholder)
                    .error(R.drawable.ic_university_error)
                    .fit()
                    .centerCrop()
                    .into(holder.ivPhoto)
            } else {
                showInitial(college.name, holder.ivPhoto)
            }

            holder.itemView.setOnClickListener {
                onItemClick(college)
            }
        }

        private fun showInitial(name: String, imageView: ImageView) {
            if (name.isNotEmpty()) {
                // Устанавливаем цветной фон
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
                imageView.setBackgroundColor(colors[index])

                val initial = name.first().toString()
            } else {
                imageView.setImageResource(R.drawable.ic_university_placeholder)
                imageView.setBackgroundColor(Color.TRANSPARENT)
            }
        }

        override fun getItemCount(): Int = colleges.size

        fun submitList(newColleges: List<College>) {
            colleges = newColleges
            notifyDataSetChanged()
        }
    }
}