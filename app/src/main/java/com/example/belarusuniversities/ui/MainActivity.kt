package com.example.belarusuniversities.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.belarusuniversities.data.*
import com.example.belarusuniversities.databinding.ActivityMainBinding
import com.example.belarusuniversities.utils.PreferencesHelper
import com.example.belarusuniversities.viewmodel.UniversityViewModel
import com.example.belarusuniversities.viewmodel.UniversityViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: UniversityViewModel
    private lateinit var collegeAdapter: CollegeAdapter
    private var allColleges: List<College> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViewModel()

        if (!PreferencesHelper(this).isLoggedIn()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setupUI()
        loadColleges()
    }

    private fun initViewModel() {
        val database = UniversityDatabase.getDatabase(applicationContext)
        val factory = UniversityViewModelFactory.create(database.universityDao())
        viewModel = ViewModelProvider(this, factory)[UniversityViewModel::class.java]
    }

    private fun setupUI() {
        collegeAdapter = CollegeAdapter(onItemClick = { college ->
            val intent = Intent(this, SpecialtiesActivity::class.java).apply {
                putExtra("COLLEGE_ID", college.id)
                putExtra("COLLEGE_NAME", college.name)
            }
            startActivity(intent)
        })

        binding.recyclerViewColleges.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewColleges.adapter = collegeAdapter

        setupSearch()

        binding.btnTestData.text = "Добавить тестовые данные"
        binding.btnTestData.setOnClickListener {
            addTestData()
        }

        binding.btnExit.setOnClickListener {
            PreferencesHelper(this).clearSession()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        binding.btnLoadFromApi.setOnClickListener {
            loadFromApi()
        }

        binding.btnCarousel.setOnClickListener {
            val intent = Intent(this, CarouselActivity::class.java)
            startActivity(intent)
        }

        binding.btnTeachers.setOnClickListener {
            val intent = Intent(this, TeachersActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupSearch() {
        binding.searchView.setOnQueryTextListener(object : android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { filterColleges(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterColleges(newText)
                return true
            }
        })

        binding.searchView.setOnCloseListener {
            collegeAdapter.updateColleges(allColleges)
            binding.searchView.clearFocus()
            false
        }
    }

    private fun filterColleges(query: String?) {
        val filtered = if (query.isNullOrEmpty()) {
            allColleges
        } else {
            val searchQuery = query.toLowerCase(Locale.getDefault())
            allColleges.filter { college ->
                college.name.toLowerCase(Locale.getDefault()).contains(searchQuery) ||
                        college.region.toLowerCase(Locale.getDefault()).contains(searchQuery)
            }
        }

        collegeAdapter.updateColleges(filtered)

        if (filtered.isEmpty() && !query.isNullOrEmpty()) {
            binding.tvEmptyState.text = "Не найдено колледжей по запросу: \"$query\""
            binding.tvEmptyState.visibility = android.view.View.VISIBLE
        } else if (filtered.isEmpty()) {
            binding.tvEmptyState.text = "Нет колледжей для отображения"
            binding.tvEmptyState.visibility = android.view.View.VISIBLE
        } else {
            binding.tvEmptyState.visibility = android.view.View.GONE
        }
    }

    private fun loadColleges() {
        lifecycleScope.launch {
            try {
                viewModel.collegesState.collectLatest { colleges ->
                    allColleges = colleges

                    val currentQuery = binding.searchView.query?.toString()
                    if (!currentQuery.isNullOrEmpty()) {
                        filterColleges(currentQuery)
                    } else {
                        collegeAdapter.updateColleges(colleges)

                        if (colleges.isNotEmpty()) {
                            binding.tvEmptyState.visibility = android.view.View.GONE
                            Toast.makeText(
                                this@MainActivity,
                                "Загружено колледжей: ${colleges.size}",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            binding.tvEmptyState.visibility = android.view.View.VISIBLE
                        }
                    }
                }
            } catch (e: Exception) {
                if (e.message?.contains("Job was cancelled") == false) {
                    Toast.makeText(this@MainActivity, "Ошибка загрузки: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun addTestData() {
        lifecycleScope.launch {
            try {
                Toast.makeText(this@MainActivity, "Добавляем тестовые данные...", Toast.LENGTH_SHORT).show()

                val collegeId = viewModel.addCollege(
                    College(
                        name = "БГУИР",
                        region = "Минск"
                    )
                )

                if (collegeId > 0) {
                    val specialtyId = viewModel.addSpecialty(
                        Specialty(
                            collegeId = collegeId.toInt(),
                            code = "1-40 01 01",
                            name = "Программная инженерия",
                            description = "Разработка программного обеспечения",
                            budgetPlaces = 30,
                            contractPlaces = 70,
                            studyYears = 4
                        )
                    )

                    if (specialtyId > 0) {
                        val calendar = Calendar.getInstance()

                        calendar.set(2003, Calendar.MAY, 15)
                        viewModel.addStudent(
                            Student(
                                specialtyId = specialtyId.toInt(),
                                fullName = "Иванов Иван Иванович",
                                birthDate = calendar.timeInMillis,
                                certificateScore = 95.5,
                                isBudget = true
                            )
                        )

                        calendar.set(2004, Calendar.JULY, 22)
                        viewModel.addStudent(
                            Student(
                                specialtyId = specialtyId.toInt(),
                                fullName = "Петрова Анна Сергеевна",
                                birthDate = calendar.timeInMillis,
                                certificateScore = 92.0,
                                isBudget = true
                            )
                        )

                        calendar.set(2003, Calendar.NOVEMBER, 10)
                        viewModel.addStudent(
                            Student(
                                specialtyId = specialtyId.toInt(),
                                fullName = "Сидоров Алексей Петрович",
                                birthDate = calendar.timeInMillis,
                                certificateScore = 78.5,
                                isBudget = false
                            )
                        )

                        Toast.makeText(
                            this@MainActivity,
                            "Тестовые данные успешно добавлены!",
                            Toast.LENGTH_LONG
                        ).show()

                        loadColleges()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadFromApi() {
        lifecycleScope.launch {
            try {
                Toast.makeText(this@MainActivity, "Загрузка университетов из API...", Toast.LENGTH_SHORT).show()
                val apiUniversities = viewModel.fetchUniversitiesFromApi("Belarus")
                if (apiUniversities.isNotEmpty()) {
                    viewModel.saveApiUniversities(apiUniversities)
                    Toast.makeText(
                        this@MainActivity,
                        "Загружено ${apiUniversities.size} университетов!",
                        Toast.LENGTH_LONG
                    ).show()
                    loadColleges()
                } else {
                    Toast.makeText(this@MainActivity, "Не удалось загрузить университеты", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}