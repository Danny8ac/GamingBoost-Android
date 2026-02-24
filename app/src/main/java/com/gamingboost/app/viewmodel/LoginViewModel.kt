package com.gamingboost.app.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamingboost.app.network.ApiClient
import com.gamingboost.app.network.LoginRequest
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    var email = mutableStateOf("")
        private set

    var password = mutableStateOf("")
        private set

    var isLoginEnabled = mutableStateOf(false)
        private set

    var loginSuccess = mutableStateOf(false)
        private set

    var loginError = mutableStateOf<String?>(null)
        private set

    // ✅ Token guardado en el ViewModel (no dentro de login())
    var token = mutableStateOf<String?>(null)
        private set

    fun onEmailChange(newEmail: String) {
        email.value = newEmail
        validateForm()
    }

    fun onPasswordChange(newPassword: String) {
        password.value = newPassword
        validateForm()
    }

    private fun validateForm() {
        isLoginEnabled.value = email.value.isNotBlank() && password.value.length >= 6
    }

    fun login() {
        viewModelScope.launch {
            loginError.value = null
            loginSuccess.value = false
            token.value = null

            try {
                val response = ApiClient.api.login(
                    LoginRequest(email.value.trim(), password.value)
                )

                if (response.isSuccessful && response.body() != null) {
                    // ✅ Login OK
                    token.value = response.body()!!.token
                    loginSuccess.value = true
                } else {
                    // ✅ Login falló (credenciales/validación)
                    loginError.value = when (response.code()) {
                        401, 422 -> "Credenciales inválidas"
                        else -> "Error: ${response.code()}"
                    }
                }
            } catch (e: Exception) {
                loginError.value = "Error de conexión: ${e.message}"
            }
        }
    }
}