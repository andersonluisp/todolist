package com.andersonpimentel.todolist.model

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.*

class AppRepository {
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firebaseStorage = FirebaseStorage.getInstance()
    private val firebaseFirestore = FirebaseFirestore.getInstance()
    val listTask = MutableLiveData<List<com.andersonpimentel.todolist.model.Task>>()

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
                            Log.i("PhotoUrl", uri.toString())
                            val profileUrl = uri.toString()
                            val user = User(
                                uuid = uid,
                                username = username,
                                profileUrl = profileUrl
                            )
                            CoroutineScope(IO).launch {
                            createUserInFirestore(uid, user)
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("FirebaseStorage", e.message.toString(), e)
                    }.await()
            } else {
                val user = User(
                    uuid = uid,
                    username = username,
                    profileUrl = ""
                )
                createUserInFirestore(uid, user)
            }
        }
    }

    suspend fun createUserInFirestore(uid: String, user: User){
        withContext(IO){
            firebaseFirestore.collection("users")
                .document(uid)
                .set(user)
                .addOnSuccessListener {
                    Log.i("Firestore", "User was created in FireStore")
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", e.message.toString())
                }.await()
        }
    }

    suspend fun authUserInFirebase(email: String, password: String): Task<AuthResult> {
        return withContext(CoroutineScope(IO).coroutineContext) {
            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    Log.i("Login", "Login Success ${it!!.user!!.uid}")
                }
                .addOnFailureListener { e ->
                    Log.e("Login", e.message.toString())
                }
        }
    }

    suspend fun getTaskListFirebase() {
        val listFirebase = arrayListOf<com.andersonpimentel.todolist.model.Task>()
        withContext(IO) {
            var uid: String? = null
            CoroutineScope(IO).async { uid = FirebaseAuth.getInstance().uid }.await()
            FirebaseFirestore.getInstance().collection("/users")
                .document(uid!!)
                .collection("/tasks")
                .addSnapshotListener { value, error ->
                    val documentChanges = value?.documentChanges
                    if (documentChanges != null) {
                        documentChanges.forEach { documentChange ->
                            if (documentChange.type == DocumentChange.Type.ADDED) {
                                val task =
                                    documentChange.document.toObject(com.andersonpimentel.todolist.model.Task::class.java)
                                Log.e("GetTask", task.title)
                                listFirebase.add(task)
                            }
                            if (documentChange.type == DocumentChange.Type.MODIFIED) {
                                val task =
                                    documentChange.document.toObject(com.andersonpimentel.todolist.model.Task::class.java)
                                val taskEditIndex = listFirebase.indexOf(task)
                                listFirebase.removeAt(taskEditIndex)
                                listFirebase.add(taskEditIndex, task)
                            }

                            if (documentChange.type == DocumentChange.Type.REMOVED) {
                                val task =
                                    documentChange.document.toObject(com.andersonpimentel.todolist.model.Task::class.java)
                                val taskRemovedIndex = listFirebase.indexOf(task)
                                listFirebase.removeAt(taskRemovedIndex)
                            }
                        }
                        listFirebase.sortBy { it.id }
                        listTask.postValue(listFirebase)
                    }
                }
        }
    }

    suspend fun insertTaskFirebase(
        task: com.andersonpimentel.todolist.model.Task,
        id: Int
    ) {
        withContext(IO) {
            val uid = FirebaseAuth.getInstance().uid
            val newTask = task.copy(id = id)
            FirebaseFirestore.getInstance().collection("/users")
                .document(uid!!)
                .collection("/tasks")
                .document(newTask.id.toString())
                .set(newTask)
                .addOnSuccessListener {
                    Log.e("InsertTask", "Added")
                }
                .addOnFailureListener { e ->
                    Log.e("InsertTask", e.message.toString(), e)
                }.await()
        }
    }

    suspend fun removeTaskFirebase(
        id: Int
    ) {
        var uid: String? = null
        CoroutineScope(IO).async { uid = FirebaseAuth.getInstance().uid }.await()
        withContext(IO) {
            FirebaseFirestore.getInstance().collection("/users")
                .document(uid!!)
                .collection("/tasks")
                .document(id.toString())
                .delete()
                .addOnSuccessListener {
                    Log.i("RemoveTask", "Deleted task")
                }
                .addOnFailureListener { e ->
                    Log.e("RemoveTAsk", e.message.toString(), e)
                }.await()
        }
    }
}