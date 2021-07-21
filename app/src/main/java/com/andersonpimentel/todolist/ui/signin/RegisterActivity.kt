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
import com.andersonpimentel.todolist.viewmodels.signin.RegisterViewModel
import com.google.android.material.textfield.TextInputLayout

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

        observers()
        clickListeners()
        textChangedListener()
    }

    private fun observers() {
        registerViewModel.resultRegisterLiveData.observe(this){
            verifyEmailAndPassword(it)
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
            val bitmap: Bitmap?
            try {
                bitmap = MediaStore.Images.Media.getBitmap(contentResolver, mPhotoUri)
                binding.cmvPhoto.setImageDrawable(BitmapDrawable(bitmap))
                binding.btnProfilePhoto.alpha = 0F
            }catch (e: Exception){
                Log.e("PhotoProfile", e.toString())
            }
        }
    }

    private fun textChangedListener() {
        binding.etRegisterEmail.addTextChangedListener {
            cleanEditTextErrors()
        }
        binding.etRegisterName.addTextChangedListener {
            cleanEditTextErrors()
        }
        binding.etRegisterPassword.addTextChangedListener {
            cleanEditTextErrors()
        }
    }

    private fun selectPhoto() {
        val intent = Intent(Intent.ACTION_PICK);
        intent.type = "image/*"
        startActivityForResult(intent, 0)
    }

    private fun verifyEmailAndPassword(result: String?) {
        val name = binding.tilName.text
        val email = binding.tilEmail.text
        val password = binding.tilPassword.text
        val error: String

        when (result) {
            "success" -> {
                startMainActivity()
            }
            "Some data is blank" -> {
                showErrorEmptyData(name, email, password)
            }
            "The email address is badly formatted." -> {
                error = "Email inválido"
                showFieldError(binding.tilEmail, error)
            }
            "The given password is invalid. [ Password should be at least 6 characters ]" -> {
                error = "A senha deve ter 6 caracteres"
                showFieldError(binding.tilPassword, error)
            }
            "There is no user record corresponding to this identifier. The user may have been deleted." -> {
                error = "Usuário não encontrado"
                showFieldError(binding.tilEmail, error)
            }
            "The email address is already in use by another account." -> {
                error = "Este email possui um cadastro."
                showFieldError(binding.tilEmail, error)
            }
        }
    }

    private fun showToast(string: String){
        Toast.makeText(
            applicationContext,
            string,
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun showFieldError(field: TextInputLayout, error: String){
        showToast(error)
        field.error = error
    }

    private fun showErrorEmptyData(name: String, email: String, password: String) {
        val error = "Nome, Email e Senha devem ser preenchidos"

        showToast(error)

        Log.e("EmptyData", error)

        if (email.isBlank()) showFieldError(binding.tilEmail, "Email não pode estar em branco")

        if (password.isBlank()) showFieldError(binding.tilPassword, "Senha não pode estar em branco")

        if (name.isBlank()) showFieldError(binding.tilName, "Nome não pode estar em branco")
    }

    private fun startMainActivity(){
        val intent = Intent(applicationContext, MainActivity::class.java)
        intent.flags =
            Intent.FLAG_ACTIVITY_CLEAR_TASK + Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    private fun createUser() {
        val name = binding.tilName.text
        val email = binding.tilEmail.text
        val password = binding.tilPassword.text
        registerViewModel.createUserInFirebase(name, email, password, mPhotoUri)
    }
}