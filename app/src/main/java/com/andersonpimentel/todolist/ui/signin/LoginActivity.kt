package com.andersonpimentel.todolist.ui.signin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import com.andersonpimentel.todolist.databinding.ActivityLoginBinding
import com.andersonpimentel.todolist.extensions.text
import com.andersonpimentel.todolist.model.AppRepository
import com.andersonpimentel.todolist.ui.MainActivity
import com.andersonpimentel.todolist.viewmodels.LoginViewModel

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var loginViewModel: LoginViewModel
    private var repository: AppRepository = AppRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loginViewModel = ViewModelProvider(
            this,
            LoginViewModel.LoginViewModelFactory(repository)
        ).get(LoginViewModel::class.java)

        loginViewModel.resultLiveData.observe(this){
            verifyLoginResult(it)
            Log.e("LiveData", it!!)
        }

        btnEnterClickListener()
        dontHaveAccountClickListener()
        textChangedListener()

    }

    private fun textChangedListener() {
        binding.etEmail.addTextChangedListener {
            binding.tilEmail.error = null
        }

        binding.etPassword.addTextChangedListener {
            binding.tilPassword.error = null
        }
    }

    private fun dontHaveAccountClickListener() {
        binding.tvDontHaveAccount.setOnClickListener {
            val intent = Intent(applicationContext, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun btnEnterClickListener() {
        binding.btnEnter.setOnClickListener {
            cleanEditTextsErrors()
            loginUser()
        }
    }

    private fun cleanEditTextsErrors(){
        binding.tilEmail.error = null
        binding.tilPassword.error = null
    }

    private fun verifyEmptyEditText(email: String, password: String): Boolean {
        var result = false
        if (email.isNullOrBlank() || password.isNullOrBlank()) {
            Toast.makeText(
                this,
                "Email e Senha devem ser preenchidos",
                Toast.LENGTH_SHORT
            )
                .show()
            if (email.isNullOrBlank()) binding.tilEmail.error = "Email não pode estar em branco"

            if (password.isNullOrBlank()) binding.tilPassword.error =
                "Senha não pode estar em branco"

            Log.e("Teste", "Email e Senha devem ser preenchidos")

            result = true
        }
        return result
    }

    private fun verifyLoginResult(result: String?) {
        when (result) {
            "success" -> {
                Log.i("Teste", result)
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK + Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }
            "The email address is badly formatted." -> {
                Toast.makeText(
                    this,
                    "Email inválido",
                    Toast.LENGTH_SHORT
                ).show()
                binding.tilEmail.error = "Email inválido"
            }
            "The password is invalid or the user does not have a password." -> {
                Toast.makeText(
                    this,
                    "Senha inválida",
                    Toast.LENGTH_SHORT
                ).show()
                binding.tilPassword.error = "Senha inválida"
            }
            "There is no user record corresponding to this identifier. The user may have been deleted." -> {
                Toast.makeText(
                    this,
                    "Usuário não encontrado",
                    Toast.LENGTH_SHORT
                ).show()
                binding.tilEmail.error = "Usuário não encontrado"
            }
        }

    }

    private fun loginUser(){
        val email = binding.tilEmail.text
        val password = binding.tilPassword.text

        if (!verifyEmptyEditText(email, password)){
            loginViewModel.authUserInFirebase(email, password)
        }
    }
}