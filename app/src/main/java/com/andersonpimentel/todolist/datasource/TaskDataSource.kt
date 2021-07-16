package com.andersonpimentel.todolist.datasource

import android.util.Log
import com.andersonpimentel.todolist.model.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main

object TaskDataSource {

    init {

    }

    private val list = arrayListOf<Task>()

    fun getList() = list.toList()

    fun insertTask(task: Task) {
        if (task.id == 0) {
            list.add(task.copy(id = list.size + 1))
        } else {
            list.remove(task)
            list.add(task)
        }
    }

    fun getListFirebase(uid: String?): List<Task>{
        var listFirebase = arrayListOf<Task>()
        FirebaseFirestore.getInstance().collection("/users")
            .document(uid!!)
            .collection("/notes")
            .addSnapshotListener { value, error ->
                var documentChanges = value?.documentChanges
                if(documentChanges != null){
                    documentChanges.forEach {
                        if(it.type == DocumentChange.Type.ADDED){
                            var note = it.document.toObject(Task::class.java)
                            listFirebase.add(note)
                    }
                }
            }
            }
        return listFirebase
    }


    suspend fun insertTaskFirebase(task: Task, uid: String?) {
        var listFirebase = arrayListOf<Task>()
        CoroutineScope(IO).launch {
            listFirebase.addAll(getListFirebase(FirebaseAuth.getInstance().uid))
            withContext(Main) {
                if (task.id == 0) {
                    var newTask = task.copy(id = listFirebase.size + 1)
                    FirebaseFirestore.getInstance().collection("/users")
                        .document(uid!!)
                        .collection("/notes")
                        .document(newTask.id.toString())
                        .set(newTask)
                        .addOnSuccessListener {
                            Log.e("Teste", "Added")
                        }
                        .addOnFailureListener { e ->
                            Log.e("Teste", e.message.toString(), e)
                        }
                } else {

                    FirebaseFirestore.getInstance().collection("/users")
                        .document(uid!!)
                        .collection("/notes")
                        .document(task.id.toString())
                        .set(task)
                        .addOnSuccessListener {
                            Log.e("Teste", "Updated")
                        }
                        .addOnFailureListener { e ->
                            Log.e("Teste", e.message.toString(), e)
                        }
                }
            }

        }




    }

    fun findById(taskId: Int) = list.find { it.id == taskId }

    fun deleteTask(task: Task) {
        list.remove(task)
    }

}