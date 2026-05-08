package com.example.mobile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobile.data.model.AuthResponse
import com.example.mobile.data.model.FullName
import com.example.mobile.domain.LoginUseCase
import com.example.mobile.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    private val _loginState = MutableStateFlow<Resource<AuthResponse>?>(null)
    val loginState: StateFlow<Resource<AuthResponse>?> = _loginState

    private val _registerState = MutableStateFlow<Resource<AuthResponse>?>(null)
    val registerState: StateFlow<Resource<AuthResponse>?> = _registerState

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _loginState.value = Resource.Loading

            val result = loginUseCase.login(email, password)
            _loginState.value = result
            _isLoading.value = false
        }
    }

    fun register(
        email: String,
        password: String,
        fullName: FullName,
//        role: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _registerState.value = Resource.Loading

            val result = loginUseCase.register(email, password, fullName/*, role*/)
            _registerState.value = result
            _isLoading.value = false
        }
    }

    fun logout() {
        viewModelScope.launch {
            loginUseCase.logout()
            // Здесь можно добавить навигацию на экран входа
        }
    }

    fun clearLoginState() {
        _loginState.value = null
    }

    fun clearRegisterState() {
        _registerState.value = null
    }
}