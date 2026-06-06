package com.example.network

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object FirebaseAuthManager {
    private const val TAG = "FirebaseAuthManager"

    // State flows to monitor authentication state and mode.
    private val _userState = MutableStateFlow<FirebaseUser?>(null)
    val userState: StateFlow<FirebaseUser?> = _userState.asStateFlow()

    private val _isFirebaseLive = MutableStateFlow(false)
    val isFirebaseLive: StateFlow<Boolean> = _isFirebaseLive.asStateFlow()

    // Offline mock user configuration for Sandbox mode
    private val _mockUser = MutableStateFlow<MockUser?>(null)
    val mockUser: StateFlow<MockUser?> = _mockUser.asStateFlow()

    private var auth: FirebaseAuth? = null

    data class MockUser(
        val uid: String,
        val email: String,
        val displayName: String
    )

    fun initialize(context: Context) {
        try {
            // First, check if default Firebase App is already initialized via google-services.json
            val apps = FirebaseApp.getApps(context)
            if (apps.isNotEmpty()) {
                Log.d(TAG, "Firebase initialized safely via default configuration")
                auth = FirebaseAuth.getInstance()
                _isFirebaseLive.value = true
                setupAuthListener()
                return
            }

            // If not initialized, check if we have programmatic keys in BuildConfig
            val apiKey = try { BuildConfig.FIREBASE_API_KEY } catch (e: Exception) { "" }
            val appId = try { BuildConfig.FIREBASE_APP_ID } catch (e: Exception) { "" }
            val projectId = try { BuildConfig.FIREBASE_PROJECT_ID } catch (e: Exception) { "" }

            if (apiKey.isNotBlank() && apiKey != "MY_FIREBASE_API_KEY" &&
                appId.isNotBlank() && appId != "MY_FIREBASE_APP_ID" &&
                projectId.isNotBlank() && projectId != "MY_FIREBASE_PROJECT_ID") {
                
                val options = FirebaseOptions.Builder()
                    .setApiKey(apiKey)
                    .setApplicationId(appId)
                    .setProjectId(projectId)
                    .build()

                FirebaseApp.initializeApp(context.applicationContext, options)
                auth = FirebaseAuth.getInstance()
                _isFirebaseLive.value = true
                setupAuthListener()
                Log.d(TAG, "Firebase initialized programmatically via system secrets")
            } else {
                Log.w(TAG, "No Firebase configuration found. Launching in secure Offline-Only Sandbox Mode.")
                _isFirebaseLive.value = false
                loadSavedMockUser(context)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing Firebase: ${e.message}. Gracefully falling back to Offline Sandbox Mode.")
            _isFirebaseLive.value = false
            loadSavedMockUser(context)
        }
    }

    private fun setupAuthListener() {
        auth?.addAuthStateListener { firebaseAuth ->
            _userState.value = firebaseAuth.currentUser
        }
    }

    fun getAuthInstance(): FirebaseAuth? = auth

    // --- Mock Authentication Logic (Sandbox fallback for local developers) ---
    private fun loadSavedMockUser(context: Context) {
        val prefs = context.getSharedPreferences("overcomer_mock_auth", Context.MODE_PRIVATE)
        val email = prefs.getString("email", "") ?: ""
        val uid = prefs.getString("uid", "") ?: ""
        val name = prefs.getString("name", "") ?: ""
        if (email.isNotBlank() && uid.isNotBlank()) {
            _mockUser.value = MockUser(uid, email, name)
        }
    }

    fun mockSignUp(context: Context, email: String, name: String, onResult: (Boolean, String?) -> Unit) {
        if (email.isBlank() || name.isBlank()) {
            onResult(false, "Please provide email and name.")
            return
        }
        val uid = "mock_uid_" + email.hashCode().toString()
        val prefs = context.getSharedPreferences("overcomer_mock_auth", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("email", email)
            .putString("uid", uid)
            .putString("name", name)
            .apply()
        _mockUser.value = MockUser(uid, email, name)
        onResult(true, null)
    }

    fun mockSignIn(context: Context, email: String, onResult: (Boolean, String?) -> Unit) {
        if (email.isBlank()) {
            onResult(false, "Please enter your email.")
            return
        }
        val prefs = context.getSharedPreferences("overcomer_mock_auth", Context.MODE_PRIVATE)
        val savedEmail = prefs.getString("email", "") ?: ""
        if (savedEmail.equals(email, ignoreCase = true)) {
            val uid = prefs.getString("uid", "") ?: ""
            val name = prefs.getString("name", "OverComer User") ?: "OverComer User"
            _mockUser.value = MockUser(uid, savedEmail, name)
            onResult(true, null)
        } else {
            // Emulate registering since it's a sandbox/fallback and we want it to be seamless
            mockSignUp(context, email, "OverComer " + email.substringBefore("@"), onResult)
        }
    }

    fun mockSignOut(context: Context) {
        val prefs = context.getSharedPreferences("overcomer_mock_auth", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
        _mockUser.value = null
    }
}
