package com.example.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mobile.presentation.ui.LoginScreen
import com.example.mobile.presentation.ui.RegisterScreen
import com.example.mobile.presentation.ui.theme.MobileTheme
import com.example.mobile.presentation.viewmodel.AuthViewModelFactory
import com.example.mobile.utils.TokenManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val tokenManager = TokenManager(this)
        val viewModelFactory = AuthViewModelFactory(tokenManager)
        val viewModel = ViewModelProvider(this, viewModelFactory)[com.example.mobile.presentation.viewmodel.AuthViewModel::class.java]

        setContent {
            MobileTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = "login") {
                        composable("login") {
                            LoginScreen(
                                onLoginSuccess = {
                                    // TODO: навигация на основе роли пользователя
                                },
                                onNavigateToRegister = { navController.navigate("register") },
                                viewModel = viewModel
                            )
                        }
                        composable("register") {
                            RegisterScreen(
                                onRegisterSuccess = { /* TODO */ },
                                onNavigateToLogin = { navController.navigate("login") },
                                viewModel = viewModel
                            )
                        }
                    }
                }
            }
        }
    }
}