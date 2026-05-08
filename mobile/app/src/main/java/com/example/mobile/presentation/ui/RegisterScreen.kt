package com.example.mobile.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.mobile.data.model.FullName
import com.example.mobile.presentation.viewmodel.AuthViewModel

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: AuthViewModel
) {
    val registerState by viewModel.registerState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var surname by remember { mutableStateOf("") }
    var patronymic by remember { mutableStateOf("") }
//    var selectedRole by remember { mutableStateOf("ORGANIZER") }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(registerState) {
        when (registerState) {
            is com.example.mobile.utils.Resource.Success -> {
                viewModel.clearRegisterState()
                onRegisterSuccess()
            }
            is com.example.mobile.utils.Resource.Error -> {
                val error = registerState as com.example.mobile.utils.Resource.Error
                errorMessage = error.message
                showErrorDialog = true
                viewModel.clearRegisterState()
            }
            com.example.mobile.utils.Resource.Loading -> {
            }
            null -> {}
        }
    }

    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text("Ошибка регистрации") },
            text = { Text(errorMessage) },
            confirmButton = {
                TextButton(onClick = { showErrorDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

    RegisterContent(
        email = email,
        onEmailChange = { email = it },
        password = password,
        onPasswordChange = { password = it },
        name = name,
        onNameChange = { name = it },
        surname = surname,
        onSurnameChange = { surname = it },
        patronymic = patronymic,
        onPatronymicChange = { patronymic = it },
//        selectedRole = selectedRole,
//        onRoleChange = { selectedRole = it },
        isLoading = isLoading,
        onRegisterClick = {
            val fullName = FullName(name = name, surname = surname, patronymic = patronymic)
            viewModel.register(email, password, fullName/*, selectedRole*/)
        },
        onNavigateToLogin = onNavigateToLogin
    )
}

@Composable
private fun RegisterContent(
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    name: String,
    onNameChange: (String) -> Unit,
    surname: String,
    onSurnameChange: (String) -> Unit,
    patronymic: String,
    onPatronymicChange: (String) -> Unit,
//    selectedRole: String,
//    onRoleChange: (String) -> Unit,
    isLoading: Boolean,
    onRegisterClick: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Регистрация",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text("Пароль") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        )

        OutlinedTextField(
            value = surname,
            onValueChange = onSurnameChange,
            label = { Text("Фамилия") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        )

        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Имя") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        )

        OutlinedTextField(
            value = patronymic,
            onValueChange = onPatronymicChange,
            label = { Text("Отчество") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        )

//        Text(text = "Роль:", style = MaterialTheme.typography.bodyMedium)
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(top = 8.dp),
//            horizontalArrangement = Arrangement.SpaceEvenly
//        ) {
//            RoleButton("ORGANIZER", selectedRole, onRoleChange)
//            RoleButton("COORDINATOR", selectedRole, onRoleChange)
//            RoleButton("PERFORMER", selectedRole, onRoleChange)
//        }

        Button(
            onClick = onRegisterClick,
            enabled = !isLoading &&
                    email.isNotBlank() &&
                    password.isNotBlank() &&
                    name.isNotBlank() &&
                    surname.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Зарегистрироваться")
            }
        }

        Text(
            text = "Уже есть аккаунт? Войти",
            modifier = Modifier
                .padding(top = 16.dp)
                .clickable { onNavigateToLogin() },
            color = MaterialTheme.colorScheme.primary
        )
    }
}

//@Composable
//private fun RoleButton(
//    role: String,
//    selectedRole: String,
//    onRoleSelect: (String) -> Unit
//) {
//    OutlinedButton(
//        onClick = { onRoleSelect(role) },
//        colors = if (role == selectedRole) {
//            ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
//        } else {
//            ButtonDefaults.outlinedButtonColors()
//        },
//        modifier = Modifier
//            .width(100.dp)
//    ) {
//        Text(
//            text = role.lowercase().replaceFirstChar { it.uppercase() },
//            maxLines = 1,
//            softWrap = false
//        )
//    }
//}