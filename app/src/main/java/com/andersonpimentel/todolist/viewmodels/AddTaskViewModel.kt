package com.andersonpimentel.todolist.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.andersonpimentel.todolist.model.AppRepository
import com.andersonpimentel.todolist.model.Task
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class AddTaskViewModel(private val repository: AppRepository) : ViewModel() {

    fun insertTaskFirebase(task: Task, id: Int) {
        CoroutineScope(IO).launch {
            repository.insertTaskFirebase(task, id)
        }
    }

    class AddTaskViewModelFactory(
        private val repository: AppRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return AddTaskViewModel(repository) as T
        }
    }
}