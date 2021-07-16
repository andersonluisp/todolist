package com.andersonpimentel.todolist.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.andersonpimentel.todolist.model.AppRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LoginViewModel(private val repository: AppRepository): ViewModel() {
    var resultLiveData = MutableLiveData<String?>()

    fun authUserInFirebase(
        email: String,
        password: String
    ){
        CoroutineScope(Dispatchers.Main).launch {
            CoroutineScope(Dispatchers.Default).async {
                try {
                    Log.e("Teste", "User Logged")
                    repository.authUserInFirebase(email, password).await()
                    resultLiveData.postValue("success")
                } catch (e: Throwable) {
                    Log.e("Teste", e.message.toString())
                    resultLiveData.postValue(e.message.toString())
                }
            }.await()
        }
    }

    class LoginViewModelFactory (
        private val repository: AppRepository
    ) : ViewModelProvider.Factory{
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return LoginViewModel(repository) as T
        }

    }
}