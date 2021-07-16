package com.andersonpimentel.todolist.model

import android.content.Intent
import android.net.Uri
import android.util.Log
import com.andersonpimentel.todolist.ui.MainActivity
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.*

class AppRepository {
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firebaseStorage = FirebaseStorage.getInstance()
    private val firebaseFirestore = FirebaseFirestore.getInstance()

    suspend fun createUserInFirebase(email: String, password: String): Task<AuthResult> {
        return withContext(CoroutineScope(IO).coroutineContext) {
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    println("success")
                }
                .addOnFailureListener {
                    println(it.message)
                    Log.e("teste fun", it.message.toString())
                }
        }
    }

    suspend fun saveUserInFirebase(mPhotoUri: Uri?, username: String) {
        withContext(IO) {
            val uid = firebaseAuth.uid.toString()
            if (mPhotoUri != null) {
                val filename = UUID.randomUUID().toString()
                val ref = firebaseStorage.getReference("/images/$filename")
                ref.putFile(mPhotoUri)
                    .addOnSuccessListener {
                        ref.downloadUrl.addOnSuccessListener() { uri ->
                            Log.i("Teste", uri.toString())
                            val profileUrl = uri.toString()
                            val user = User(
                                uuid = uid,
                                username = username,
                                profileUrl = profileUrl
                            )

                            firebaseFirestore.collection("users")
                                .document(uid)
                                .set(user)
                                .addOnSuccessListener {
                                }
                                .addOnFailureListener { e ->
                                    Log.e("Teste", e.message.toString())
                                }
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("Teste", e.message.toString(), e)
                    }.await()
            } else {
                val user = User(
                    uuid = uid,
                    username = username,
                    profileUrl = ""
                )
                firebaseFirestore.collection("users")
                    .document(uid)
                    .set(user)
                    .addOnSuccessListener {
                        //Log.e("Teste", it.toString())
                    }
                    .addOnFailureListener { e ->
                        Log.e("Teste", e.message.toString())
                    }.await()
            }
        }
    }

    suspend fun authUserInFirebase(email: String, password: String): Task<AuthResult> {
        return withContext(CoroutineScope(IO).coroutineContext) {
            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    Log.i("Teste", it!!.user!!.uid)
                }
                .addOnFailureListener { e ->
                    Log.e("Teste", e.message.toString())
                }
        }
    }
}