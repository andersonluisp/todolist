package com.andersonpimentel.todolist.viewmodels.signin

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.andersonpimentel.todolist.model.AppRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LoginViewModel(private val repository: AppRepository): ViewModel() {
    var resultLoginLiveData = MutableLiveData<String?>()

    fun authUserInFirebase(
        email: String,
        password: String
    ) {
        if (verifyEmptyLoginData(email, password)) {
            resultLoginLiveData.postValue("Email or password is blank")
        } else {
            CoroutineScope(Dispatchers.Default).launch {
                try {
                    repository.authUserInFirebase(email, password).await()
                    resultLoginLiveData.postValue("success")
                } catch (e: Throwable) {
                    val exceptionMessage = e.message.toString()
                    resultLoginLiveData.postValue(exceptionMessage)
                }
            }
        }
    }

    private fun verifyEmptyLoginData(email: String, password: String): Boolean {
        return email.isBlank() || password.isBlank()
    }

    class LoginViewModelFactory (
        private val repository: AppRepository
    ) : ViewModelProvider.Factory{
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return LoginViewModel(repository) as T
        }

    }
}