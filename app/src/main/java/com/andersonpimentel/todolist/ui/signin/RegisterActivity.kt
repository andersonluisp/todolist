package com.andersonpimentel.todolist.ui.signin

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import com.andersonpimentel.todolist.databinding.ActivityRegisterBinding
import com.andersonpimentel.todolist.extensions.text
import com.andersonpimentel.todolist.model.AppRepository
import com.andersonpimentel.todolist.ui.MainActivity
import com.andersonpimentel.todolist.viewmodels.RegisterViewModel

class RegisterActivity() : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var registerViewModel: RegisterViewModel
    private var mPhotoUri: Uri? = null
    private var repository: AppRepository = AppRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        registerViewModel = ViewModelProvider(
            this,
            RegisterViewModel.RegisterViewModelFactory(repository)
        ).get(RegisterViewModel::class.java)

        clickListeners()
        textChangedListener()

        registerViewModel.resultLiveData.observe(this){
            verifyEmailAndPassword(binding.tilName.text, it)
            Log.e("LiveData", it!!)
        }
    }

    private fun clickListeners() {
        binding.btnCreateAccount.setOnClickListener {
            cleanEditTextErrors()
            createUser()
        }

        binding.btnProfilePhoto.setOnClickListener {
            selectPhoto()
        }

        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun cleanEditTextErrors() {
        binding.tilEmail.error = null
        binding.tilName.error = null
        binding.tilPassword.error = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && data != null){
            mPhotoUri = data.data
            var bitmap: Bitmap? = null
            try {
                bitmap = MediaStore.Images.Media.getBitmap(contentResolver, mPhotoUri)
                binding.cmvPhoto.setImageDrawable(BitmapDrawable(bitmap))
                binding.btnProfilePhoto.alpha = 0F
            }catch (e: Exception){

            }
        }
    }

    private fun textChangedListener() {
        binding.etRegisterEmail.addTextChangedListener {
            binding.tilEmail.error = null
        }
        binding.etRegisterName.addTextChangedListener {
            binding.tilName.error = null
        }
        binding.etRegisterPassword.addTextChangedListener {
            binding.tilPassword.error = null
        }
    }

    private fun selectPhoto() {
        val intent = Intent(Intent.ACTION_PICK);
        intent.type = "image/*"
        startActivityForResult(intent, 0)
    }

    private fun verifyEmptyTexts(name: String, email: String, password: String): Boolean{
        var result = false
        if (email.isNullOrBlank() || password.isNullOrBlank() || name.isNullOrBlank()) {

            Toast.makeText(this, "Nome, Email e Senha devem ser preenchidos", Toast.LENGTH_SHORT)
                .show()

            Log.e("Teste", "Nome, Email e Senha devem ser preenchidos")

            if (email.isNullOrBlank()) binding.tilEmail.error = "Email não pode estar em branco"
            if (password.isNullOrBlank()) binding.tilPassword.error =
                "Senha não pode estar em branco"
            if (name.isNullOrBlank()) binding.tilName.error = "Nome não pode estar em branco"

            result = true
        }
        return result
    }

    private fun verifyEmailAndPassword(username: String, result: String?) {
        when (result) {
            "success" -> {
                Log.e("Success", "Success")
                val intent = Intent(applicationContext, MainActivity::class.java)
                intent.flags =
                    Intent.FLAG_ACTIVITY_CLEAR_TASK + Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }
            "The email address is badly formatted." -> {
                Toast.makeText(
                    applicationContext,
                    "Email inválido",
                    Toast.LENGTH_SHORT
                ).show()
                binding.tilEmail.error = "Email inválido"
            }
            "The given password is invalid. [ Password should be at least 6 characters ]" -> {
                Toast.makeText(
                    applicationContext,
                    "A senha deve ter 6 caracteres",
                    Toast.LENGTH_SHORT
                ).show()
                binding.tilPassword.error = "A senha deve ter 6 caracteres"
            }
            "There is no user record corresponding to this identifier. The user may have been deleted." -> {
                Toast.makeText(
                    applicationContext,
                    "Usuário não encontrado",
                    Toast.LENGTH_SHORT
                ).show()
                binding.tilEmail.error = "Usuário não encontrado"
            }
            "The email address is already in use by another account." -> {
                Toast.makeText(
                    applicationContext,
                    "Email já cadastrado",
                    Toast.LENGTH_SHORT
                ).show()
                binding.tilEmail.error = "Este email possui um cadastro."
            }
        }
    }

    private fun createUser() {
        val name = binding.tilName.text
        val email = binding.tilEmail.text
        val password = binding.tilPassword.text

        if (!verifyEmptyTexts(name, email, password)) {
            registerViewModel.createUserInFirebase(name, email, password, mPhotoUri)
        }
    }
}