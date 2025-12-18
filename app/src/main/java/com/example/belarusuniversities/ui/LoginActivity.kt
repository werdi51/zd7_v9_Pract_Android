package com.example.belarusuniversities.ui

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.belarusuniversities.data.TestDataHelper
import com.example.belarusuniversities.databinding.ActivityLoginBinding
import com.example.belarusuniversities.utils.PreferencesHelper

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (PreferencesHelper(this).isLoggedIn()) {
            navigateToMain()
            return
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
    }

    private fun setupUI() {
        val roles = arrayOf("Студент", "Преподаватель", "Минобразования")

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            roles
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.spinnerRole.adapter = adapter
        binding.spinnerRole.setSelection(1)

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val role = binding.spinnerRole.selectedItem as String

            if (email.isEmpty()) {
                binding.etEmail.error = "Введите email (любой текст)"
                return@setOnClickListener
            }

            val roleKey = when (role) {
                "Студент" -> "student"
                "Преподаватель" -> "teacher"
                "Минобразования" -> "ministry"
                else -> "teacher"
            }

            PreferencesHelper(this).saveSession(email, roleKey)
            Toast.makeText(this, "Вход выполнен как: $role", Toast.LENGTH_SHORT).show()

            TestDataHelper.addTestData(this)

            navigateToMain()
        }
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}