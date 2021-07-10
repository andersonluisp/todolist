package com.andersonpimentel.todolist.ui

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import com.andersonpimentel.todolist.R
import com.andersonpimentel.todolist.databinding.ActivityRegisterBinding
import com.andersonpimentel.todolist.extensions.text
import com.andersonpimentel.todolist.model.User
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.lang.Exception
import java.util.*

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private var mPhotoUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnCreateAccount.setOnClickListener {
            createUser()
        }

        binding.btnProfilePhoto.setOnClickListener {
            selectPhoto()
        }

        textChangedListener()

        binding.toolbar.setNavigationOnClickListener { finish() }
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

    private fun createUser() {
        val name = binding.tilName.text
        val email = binding.tilEmail.text
        val password = binding.tilPassword.text

        if(email.isNullOrBlank() || password.isNullOrBlank() || name.isNullOrBlank()){
            Toast.makeText(this, "Nome, Email e Senha devem ser preenchidos", Toast.LENGTH_SHORT).show()
            Log.e("Teste", "Nome, Email e Senha devem ser preenchidos")

            if(email.isNullOrBlank()) binding.tilEmail.error = "Email não pode estar em branco"

            if(password.isNullOrBlank()) binding.tilPassword.error = "Senha não pode estar em branco"

            if(name.isNullOrBlank()) binding.tilName.error = "Nome não pode estar em branco"

        } else{

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.i("Teste", it.result!!.user!!.uid)
                    saveUserInFirebase()
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK + Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                }
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
                    "The given password is invalid. [ Password should be at least 6 characters ]" -> {
                        Toast.makeText(
                            this,
                            "A senha deve ter 6 caracteres",
                            Toast.LENGTH_SHORT
                        ).show()
                        binding.tilPassword.error = "A senha deve ter 6 caracteres"
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

    private fun saveUserInFirebase() {
        var filename = UUID.randomUUID().toString()
        var ref = FirebaseStorage.getInstance().getReference("/images/$filename")
        ref.putFile(mPhotoUri!!)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener() { uri ->
                    Log.i("Teste", uri.toString())

                    val uid = FirebaseAuth.getInstance().uid.toString()
                    val username = binding.tilName.text
                    val profileUrl = uri.toString()

                    val user = User(
                        uuid = uid,
                        username = username,
                        profileUrl = profileUrl
                    )

                    FirebaseFirestore.getInstance().collection("users")
                        .add(user)
                        .addOnSuccessListener {
                            Log.i("Teste", it.id)
                        }
                        .addOnFailureListener { e ->
                            Log.e("Teste", e.message.toString())
                        }

                }
            }
            .addOnFailureListener{ e ->
                Log.e("Teste", e.message.toString(), e)
            }
    }
}