package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.OverComerViewModel

enum class AuthScreenState {
    SIGN_IN, SIGN_UP, FORGOT_PASSWORD
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthSyncDialog(
    viewModel: OverComerViewModel,
    onDismissRequest: () -> Unit
) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
    val isFirebaseLive by viewModel.isFirebaseLive.collectAsStateWithLifecycle()
    val currentUserEmail by viewModel.currentUserEmail.collectAsStateWithLifecycle()
    val currentUserName by viewModel.currentUserName.collectAsStateWithLifecycle()
    val currentUserUid by viewModel.currentUserUid.collectAsStateWithLifecycle()
    val logs by viewModel.victoryLogs.collectAsStateWithLifecycle()

    var screenState by remember { mutableStateOf(AuthScreenState.SIGN_IN) }
    
    // Auth inputs
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    // UI Feedback State
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = { if (!isLoading) onDismissRequest() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .padding(vertical = 24.dp)
                .testTag("auth_dialog_surface"),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CloudQueue,
                            contentDescription = "Sync option Icon",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Cloud Sync & Auth",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(
                        onClick = onDismissRequest,
                        enabled = !isLoading,
                        modifier = Modifier.testTag("close_auth_dialog")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Authentication Dialog",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                // Service Status Indicator
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isFirebaseLive) 
                            Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(
                                    color = if (isFirebaseLive) Color(0xFF4CAF50) else Color(0xFFFF9800),
                                    shape = CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isFirebaseLive) "Firebase Auth: Connected" else "Firebase Auth: Sandbox Mode (Offline)",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isFirebaseLive) Color(0xFF1B5E20) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (isFirebaseLive) "Your progress is securely encrypted and backed up directly to firestore." 
                                       else "Enter FIREBASE_API_KEY in .env/Secrets to enable cloud backup.",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isFirebaseLive) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                // Error / Success Banners
                if (errorMessage != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ErrorOutline, "Error Icon", tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(errorMessage!!, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                }

                if (successMessage != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, "Success Icon", tint = Color(0xFF2E7D32))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(successMessage!!, style = MaterialTheme.typography.bodySmall, color = Color(0xFF1B5E20))
                        }
                    }
                }

                AnimatedContent(
                    targetState = isLoggedIn,
                    label = "LoggedInStateTransition"
                ) { loggedIn ->
                    if (loggedIn) {
                        // --- PROFILE VIEW ---
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AccountCircle,
                                contentDescription = "User avatar icon",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(72.dp)
                                    .padding(bottom = 8.dp)
                            )

                            Text(
                                text = currentUserName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Text(
                                text = currentUserEmail,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Sync statistics
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Current Session UID", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                        Text(
                                            text = if (currentUserUid.length > 12) currentUserUid.take(12) + "..." else currentUserUid,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Saved Victory Logs", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                        Text("${logs.size} entries", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Backup Security", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.VerifiedUser, "verified user", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Active", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Button(
                                onClick = {
                                    viewModel.logout()
                                    successMessage = "Signed out successfully."
                                    errorMessage = null
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("auth_logout_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) {
                                Icon(Icons.Default.ExitToApp, contentDescription = "Log out icon")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Log Out from Session")
                            }
                        }
                    } else {
                        // --- AUTH CREDENTIALS FORMS ---
                        Column(modifier = Modifier.fillMaxWidth()) {
                            when (screenState) {
                                AuthScreenState.SIGN_IN -> {
                                    Text(
                                        text = "Walk in Victory!",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                                    )

                                    OutlinedTextField(
                                        value = email,
                                        onValueChange = { email = it; errorMessage = null },
                                        label = { Text("Email Address") },
                                        leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = "Email icon") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                        modifier = Modifier.fillMaxWidth().testTag("auth_email_input"),
                                        singleLine = true
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    OutlinedTextField(
                                        value = password,
                                        onValueChange = { password = it; errorMessage = null },
                                        label = { Text("Session Password") },
                                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password icon") },
                                        trailingIcon = {
                                            IconButton(onClick = { showPassword = !showPassword }) {
                                                Icon(
                                                    imageVector = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                    contentDescription = "Toggle password visibility"
                                                )
                                            }
                                        },
                                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                        modifier = Modifier.fillMaxWidth().testTag("auth_password_input"),
                                        singleLine = true
                                    )

                                    TextButton(
                                        onClick = { screenState = AuthScreenState.FORGOT_PASSWORD; errorMessage = null; successMessage = null },
                                        modifier = Modifier.align(Alignment.End).testTag("forgot_password_link")
                                    ) {
                                        Text("Forgot Password?")
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Button(
                                        onClick = {
                                            isLoading = true
                                            viewModel.signInWithEmailAndPassword(email, password) { success, err ->
                                                isLoading = false
                                                if (success) {
                                                    successMessage = "Successfully logged in!"
                                                    errorMessage = null
                                                } else {
                                                    errorMessage = err
                                                }
                                            }
                                        },
                                        enabled = !isLoading && email.isNotBlank() && password.isNotBlank(),
                                        modifier = Modifier.fillMaxWidth().testTag("auth_signin_btn")
                                    ) {
                                        if (isLoading) {
                                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                                        } else {
                                            Text("Sign In / Sync")
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("New to OverComer?", style = MaterialTheme.typography.bodyMedium)
                                        TextButton(
                                            onClick = { screenState = AuthScreenState.SIGN_UP; errorMessage = null; successMessage = null },
                                            modifier = Modifier.testTag("auth_goto_signup")
                                        ) {
                                            Text("Create Account")
                                        }
                                    }
                                }
                                AuthScreenState.SIGN_UP -> {
                                    Text(
                                        text = "Join as an OverComer",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                                    )

                                    OutlinedTextField(
                                        value = name,
                                        onValueChange = { name = it; errorMessage = null },
                                        label = { Text("Full Name") },
                                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Name icon") },
                                        modifier = Modifier.fillMaxWidth().testTag("auth_signup_name"),
                                        singleLine = true
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    OutlinedTextField(
                                        value = email,
                                        onValueChange = { email = it; errorMessage = null },
                                        label = { Text("Email Address") },
                                        leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = "Email icon") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                        modifier = Modifier.fillMaxWidth().testTag("auth_signup_email"),
                                        singleLine = true
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    OutlinedTextField(
                                        value = password,
                                        onValueChange = { password = it; errorMessage = null },
                                        label = { Text("Define Password") },
                                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password icon") },
                                        trailingIcon = {
                                            IconButton(onClick = { showPassword = !showPassword }) {
                                                Icon(
                                                    imageVector = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                    contentDescription = "Toggle password visibility"
                                                )
                                            }
                                        },
                                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                        modifier = Modifier.fillMaxWidth().testTag("auth_signup_password"),
                                        singleLine = true
                                    )

                                    Spacer(modifier = Modifier.height(20.dp))

                                    Button(
                                        onClick = {
                                            isLoading = true
                                            viewModel.signUpWithEmailAndPassword(email, password, name) { success, err ->
                                                isLoading = false
                                                if (success) {
                                                    successMessage = "Account created successfully!"
                                                    errorMessage = null
                                                } else {
                                                    errorMessage = err
                                                }
                                            }
                                        },
                                        enabled = !isLoading && email.isNotBlank() && password.isNotBlank() && name.isNotBlank(),
                                        modifier = Modifier.fillMaxWidth().testTag("auth_signup_btn")
                                    ) {
                                        if (isLoading) {
                                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                                        } else {
                                            Text("Register & Initialize Sync")
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Already registered?", style = MaterialTheme.typography.bodyMedium)
                                        TextButton(
                                            onClick = { screenState = AuthScreenState.SIGN_IN; errorMessage = null; successMessage = null },
                                            modifier = Modifier.testTag("auth_goto_signin")
                                        ) {
                                            Text("Sign In")
                                        }
                                    }
                                }
                                AuthScreenState.FORGOT_PASSWORD -> {
                                    Text(
                                        text = "Reset Secure Passcode",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                                    )

                                    Text(
                                        text = "Enter your verified session email. We'll generate an encrypted authorization link to reset your account credentials safely.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                                    )

                                    OutlinedTextField(
                                        value = email,
                                        onValueChange = { email = it; errorMessage = null },
                                        label = { Text("Email Address") },
                                        leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = "Email icon") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                        modifier = Modifier.fillMaxWidth().testTag("auth_forgot_email"),
                                        singleLine = true
                                    )

                                    Spacer(modifier = Modifier.height(20.dp))

                                    Button(
                                        onClick = {
                                            isLoading = true
                                            viewModel.sendPasswordResetEmail(email) { success, err ->
                                                isLoading = false
                                                if (success) {
                                                    successMessage = if (isFirebaseLive) "A password recovery email was dispatched safely!" 
                                                                     else "In Sandbox Mode: Verification sequence completed and simulation reset dispatched."
                                                    errorMessage = null
                                                } else {
                                                    errorMessage = err
                                                }
                                            }
                                        },
                                        enabled = !isLoading && email.isNotBlank(),
                                        modifier = Modifier.fillMaxWidth().testTag("auth_reset_btn")
                                    ) {
                                        if (isLoading) {
                                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                                        } else {
                                            Text("Dispatch Reset Link")
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    TextButton(
                                        onClick = { screenState = AuthScreenState.SIGN_IN; errorMessage = null; successMessage = null },
                                        modifier = Modifier.align(Alignment.CenterHorizontally).testTag("auth_reset_back_btn")
                                    ) {
                                        Text("Back to Sign In")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
