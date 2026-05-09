package com.example.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mobile.data.local.DatabaseProvider
import com.example.mobile.data.repository.EventRepository
import com.example.mobile.data.repository.EventWorkspaceRepository
import com.example.mobile.data.repository.InvitationRepository
import com.example.mobile.domain.CreateEventUseCase
import com.example.mobile.presentation.ui.CreateEventScreen
import com.example.mobile.presentation.ui.EventDetailScreen
import com.example.mobile.presentation.ui.EventListScreen
import com.example.mobile.presentation.ui.InvitationsHubScreen
import com.example.mobile.presentation.ui.LoginScreen
import com.example.mobile.presentation.ui.RegisterScreen
import com.example.mobile.presentation.ui.theme.MobileTheme
import com.example.mobile.presentation.viewmodel.EventListViewModel
import com.example.mobile.presentation.viewmodel.EventListViewModelFactory
import com.example.mobile.presentation.viewmodel.InvitationsHubViewModel
import com.example.mobile.presentation.viewmodel.InvitationsHubViewModelFactory
import com.example.mobile.presentation.viewmodel.AuthViewModelFactory
import com.example.mobile.presentation.viewmodel.CreateEventViewModel
import com.example.mobile.presentation.viewmodel.EventDetailViewModel
import com.example.mobile.presentation.viewmodel.EventDetailViewModelFactory
import com.example.mobile.utils.TokenManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val tokenManager = TokenManager(this)
        val viewModelFactory = AuthViewModelFactory(tokenManager)
        val viewModel = ViewModelProvider(this, viewModelFactory)[com.example.mobile.presentation.viewmodel.AuthViewModel::class.java]
        val eventRepository = EventRepository(
            eventDao = DatabaseProvider.getDatabase(this).eventDao(),
            tokenManager = tokenManager
        )
        val eventListViewModelFactory = EventListViewModelFactory(eventRepository)
        val eventListViewModel = ViewModelProvider(this, eventListViewModelFactory)[EventListViewModel::class.java]
        val invitationRepository = InvitationRepository(tokenManager)
        val createEventUseCase = CreateEventUseCase(eventRepository)
        val createEventViewModel = CreateEventViewModel(createEventUseCase)
        val eventWorkspaceRepository = EventWorkspaceRepository(
            tokenManager,
            DatabaseProvider.getDatabase(this).workspaceCacheDao()
        )

        setContent {
            MobileTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val token by tokenManager.tokenFlow.collectAsState(initial = null)
                    val startDestination = if (token.isNullOrBlank()) "login" else "events"

                    NavHost(navController = navController, startDestination = startDestination) {
                        composable("login") {
                            LoginScreen(
                                onLoginSuccess = {
                                    navController.navigate("events") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                },
                                onNavigateToRegister = { navController.navigate("register") },
                                viewModel = viewModel
                            )
                        }
                        composable("register") {
                            RegisterScreen(
                                onRegisterSuccess = {
                                    navController.navigate("events") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                },
                                onNavigateToLogin = { navController.navigate("login") },
                                viewModel = viewModel
                            )
                        }
                        composable("events") {
                            EventListScreen(
                                viewModel = eventListViewModel,
                                onOpenEvent = { eventId ->
                                    navController.navigate("event/$eventId")
                                },
                                onOpenInvitationsHub = {
                                    navController.navigate("invitations-hub")
                                },
                                onLogout = {
                                    viewModel.logout()
                                    navController.navigate("login") {
                                        popUpTo("events") { inclusive = true }
                                    }
                                },
                                onCreateEvent = {
                                    navController.navigate("create-event")
                                }
                            )
                        }
                        composable("event/{eventId}") { backStackEntry ->
                            val eventId = backStackEntry.arguments?.getString("eventId").orEmpty()
                            val detailFactory = EventDetailViewModelFactory(
                                eventWorkspaceRepository,
                                eventId
                            )
                            val detailViewModel = ViewModelProvider(
                                backStackEntry,
                                detailFactory
                            )[EventDetailViewModel::class.java]
                            EventDetailScreen(
                                viewModel = detailViewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable("invitations-hub") { backStackEntry ->
                            val hubFactory = InvitationsHubViewModelFactory(invitationRepository)
                            val hubViewModel = ViewModelProvider(
                                backStackEntry,
                                hubFactory
                            )[InvitationsHubViewModel::class.java]
                            InvitationsHubScreen(
                                viewModel = hubViewModel,
                                onBack = { navController.popBackStack() },
                                onOpenEvent = { eventId ->
                                    navController.navigate("event/$eventId")
                                }
                            )
                        }

                        composable("create-event") {
                            CreateEventScreen(
                                onEventCreated = { navController.popBackStack() },
                                onBack = { navController.popBackStack() },
                                viewModel = createEventViewModel
                            )
                        }
                    }
                }
            }
        }
    }
}