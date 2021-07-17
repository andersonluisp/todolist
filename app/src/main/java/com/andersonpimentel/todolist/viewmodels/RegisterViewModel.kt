package com.andersonpimentel.todolist.viewmodels

import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.andersonpimentel.todolist.model.AppRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.tasks.await

class RegisterViewModel(private val repository: AppRepository) : ViewModel() {
    var resultLiveData = MutableLiveData<String?>()

    fun createUserInFirebase(
        username: String,
        email: String,
        password: String,
        mPhotoUri: Uri?
    ) {
        CoroutineScope(Main).launch {
            CoroutineScope(Default).async {
                try {
                    repository.createUserInFirebase(email, password).await()
                    resultLiveData.postValue("success")
                    repository.saveUserInFirebase(mPhotoUri, username)
                } catch (e: Throwable) {
                    Log.e("Teste", e.message.toString())
                    resultLiveData.postValue(e.message.toString())
                }
            }.await()
        }
    }

    fun verifyEmptyRegisterData(username: String, email: String, password: String): Boolean {
        return email.isBlank() || password.isBlank() || username.isBlank()
    }

    class RegisterViewModelFactory (
        private val repository: AppRepository
    ) : ViewModelProvider.Factory{
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return RegisterViewModel(repository) as T
        }

    }

}