package com.andersonpimentel.todolist.viewmodels.signin

import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.andersonpimentel.todolist.model.AppRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.tasks.await

class RegisterViewModel(private val repository: AppRepository) : ViewModel() {
    var resultRegisterLiveData = MutableLiveData<String?>()

    fun createUserInFirebase(
        username: String,
        email: String,
        password: String,
        mPhotoUri: Uri?
    ) {
        if (verifyEmptyRegisterData(username, email, password)) {
            val resultRegister = "Some data is blank"
            postValueLiveData(resultRegister, resultRegisterLiveData)
        } else{
        CoroutineScope(Default).launch {
            try {
                repository.createUserInFirebase(email, password).await()
                resultRegisterLiveData.postValue("success")
                repository.saveUserInFirebase(mPhotoUri, username)
                Log.i("Firebase", "User has been successfully registered in Firebase")
            } catch (e: Throwable) {
                Log.e("Firebase", e.message.toString())
                resultRegisterLiveData.postValue(e.message.toString())
            }
        }
        }
    }

    fun postValueLiveData (result: String?,  livedata: MutableLiveData<String?>){
        livedata.postValue(result)
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