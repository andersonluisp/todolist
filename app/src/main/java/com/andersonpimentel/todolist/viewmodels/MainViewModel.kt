package com.andersonpimentel.todolist.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.andersonpimentel.todolist.model.AppRepository
import com.andersonpimentel.todolist.model.Task
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class MainViewModel(private val repository: AppRepository): ViewModel() {
    var listTask = repository.listTask

    fun getTaskListFirebase(){
        CoroutineScope(IO).launch {
            repository.getTaskListFirebase()
        }
    }

    fun removeTaskFirebase(id: Int){
        CoroutineScope(IO).launch{
            repository.removeTaskFirebase(id)
        }
    }

    fun verifyAuthentication(): Boolean{
        val uid = FirebaseAuth.getInstance().uid
        return uid.isNullOrBlank()
    }

    fun verifyListIsEmpty(listTasks: List<Task>): Boolean{
        return listTasks.isEmpty()
    }

    class MainViewModelFactory(
        private val repository: AppRepository
    ): ViewModelProvider.Factory{
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return MainViewModel(repository) as T
        }
    }
}