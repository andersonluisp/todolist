package com.andersonpimentel.todolist.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telephony.ims.RegistrationManager
import android.util.Log
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import com.andersonpimentel.todolist.R
import com.andersonpimentel.todolist.databinding.ActivityLoginBinding
import com.andersonpimentel.todolist.extensions.text
import com.google.firebase.auth.FirebaseAuth
import java.lang.Exception

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
            val email = binding.tilEmail.text
            val password = binding.tilPassword.text
            Log.e("Teste", "$email / $password")
            authFirebase(email, password)
        }
    }

    private fun authFirebase(email: String, password: String) {
        if (email.isNullOrBlank() || password.isNullOrBlank()) {
            Toast.makeText(
                this,
                "Email e Senha devem ser preenchidos",
                Toast.LENGTH_SHORT
            )
                .show()
            if(email.isNullOrBlank()) binding.tilEmail.error = "Email não pode estar em branco"

            if(password.isNullOrBlank()) binding.tilPassword.error = "Senha não pode estar em branco"

            Log.e("Teste", "Email e Senha devem ser preenchidos")
        } else {
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    Log.i("Teste", it!!.user!!.uid)
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK + Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                }
                .addOnFailureListener { e ->
                    Log.e("Teste", e.message.toString())
                    when (e.message.toString()) {
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
        }
    }
}