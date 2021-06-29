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
            Toast.makeText(this, "Email e Senha devem ser preenchidos", Toast.LENGTH_SHORT).show()
            Log.e("Teste", "Email e Senha devem ser preenchidos")
        } else{

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.i("Teste", it.result!!.user!!.uid)
                    saveUserInFirebase()
                    finish()
                }
            }
            .addOnFailureListener { e ->
                Log.e("Teste", e.message.toString())
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