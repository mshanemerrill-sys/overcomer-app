package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.network.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

class OverComerViewModel(application: Application) : AndroidViewModel(application) {

    // --- Firebase Authentication States ---
    val isFirebaseLive: StateFlow<Boolean> = FirebaseAuthManager.isFirebaseLive
    val firebaseUser: StateFlow<com.google.firebase.auth.FirebaseUser?> = FirebaseAuthManager.userState
    val mockUser: StateFlow<FirebaseAuthManager.MockUser?> = FirebaseAuthManager.mockUser

    val isLoggedIn: StateFlow<Boolean> = combine(firebaseUser, mockUser) { fbUser, mkUser ->
        fbUser != null || mkUser != null
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val currentUserEmail: StateFlow<String> = combine(firebaseUser, mockUser) { fbUser, mkUser ->
        fbUser?.email ?: mkUser?.email ?: ""
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val currentUserName: StateFlow<String> = combine(firebaseUser, mockUser) { fbUser, mkUser ->
        fbUser?.displayName ?: mkUser?.displayName ?: "OverComer"
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "OverComer")

    val currentUserUid: StateFlow<String> = combine(firebaseUser, mockUser) { fbUser, mkUser ->
        fbUser?.uid ?: mkUser?.uid ?: ""
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    private val database = OverComerDatabase.getDatabase(application)
    private val repository = OverComerRepository(database)

    // Reactive streams from the database, filtered by current user's UID to enforce session security
    val victoryLogs: StateFlow<List<VictoryLog>> = repository.victoryLogs
        .combine(currentUserUid) { logs, uid ->
            logs.filter { it.userId == uid }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val dailyCheckIns: StateFlow<List<DailyCheckIn>> = repository.dailyCheckIns
        .combine(currentUserUid) { logs, uid ->
            logs.filter { it.userId == uid }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _isSavingCheckIn = MutableStateFlow(false)
    val isSavingCheckIn: StateFlow<Boolean> = _isSavingCheckIn.asStateFlow()

    private val _latestCheckInReflection = MutableStateFlow<String?>(null)
    val latestCheckInReflection: StateFlow<String?> = _latestCheckInReflection.asStateFlow()

    fun addDailyCheckIn(
        mood: String,
        energyLevel: Int,
        triggers: String,
        journalNotes: String,
        onComplete: (String) -> Unit = {}
    ) {
        _isSavingCheckIn.value = true
        _latestCheckInReflection.value = null
        viewModelScope.launch {
            try {
                // Generate AI Reflection
                val reflection = GeminiClient.generateDailyCheckInReflection(
                    mood = mood,
                    energyLevel = energyLevel,
                    triggers = triggers,
                    journalNotes = journalNotes
                )
                
                // Save to Room Database
                repository.insertCheckIn(
                    DailyCheckIn(
                        mood = mood,
                        energyLevel = energyLevel,
                        triggers = triggers,
                        journalNotes = journalNotes,
                        aiReflection = reflection,
                        userId = currentUserUid.value
                    )
                )
                
                _latestCheckInReflection.value = reflection
                onComplete(reflection)
            } catch (e: Exception) {
                // Fallback
                val fallbackReflection = "Thank you for checking in. God will supply all your needs according to His riches in glory in Christ Jesus (Philippians 4:19)."
                repository.insertCheckIn(
                    DailyCheckIn(
                        mood = mood,
                        energyLevel = energyLevel,
                        triggers = triggers,
                        journalNotes = journalNotes,
                        aiReflection = fallbackReflection,
                        userId = currentUserUid.value
                    )
                )
                _latestCheckInReflection.value = fallbackReflection
                onComplete(fallbackReflection)
            } finally {
                _isSavingCheckIn.value = false
            }
        }
    }

    fun deleteDailyCheckIn(id: Int) {
        viewModelScope.launch {
            repository.deleteCheckInById(id)
        }
    }

    fun clearLatestCheckInReflection() {
        _latestCheckInReflection.value = null
    }

    val freedomGoal: StateFlow<FreedomGoal?> = repository.freedomGoal
        .combine(currentUserUid) { goal, uid ->
            if (goal == null || goal.userId != uid) {
                // Returns a placeholder goal linked to the current ID to seed initial view state
                FreedomGoal(
                    id = if (uid.isEmpty()) 1 else kotlin.math.abs(uid.hashCode()),
                    startDate = System.currentTimeMillis() - (1000 * 60 * 60 * 24 * 3), // Pre-load 3 days of victory
                    struggleType = "Substance Use",
                    customDeclaration = "An OverComer has submitted their life wholly to Christ and no longer fights FOR victory over addiction but rather FROM a position of victory!",
                    userId = uid
                )
            } else {
                goal
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // UI state for the AI Support Chat
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                text = "Welcome to OverComer Support. I am your guide here. I believe that through Christ's grace, you can be set free completely and walk in full victory.\n\n" +
                       "If you are feeling tempted, struggling with a habit, or feeling anxious, talk to me. We can walk through CBT reframing or DBT grounding exercises together, anchored in God's mercy.",
                isUser = false
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    private val _isAnalyzingDistortion = MutableStateFlow(false)
    val isAnalyzingDistortion: StateFlow<Boolean> = _isAnalyzingDistortion.asStateFlow()

    private val _distortionAnalysisResult = MutableStateFlow<com.example.network.DistortionAnalysisResult?>(null)
    val distortionAnalysisResult: StateFlow<com.example.network.DistortionAnalysisResult?> = _distortionAnalysisResult.asStateFlow()

    private val _verseOfTheDay = MutableStateFlow<com.example.network.VerseOfTheDay?>(null)
    val verseOfTheDay: StateFlow<com.example.network.VerseOfTheDay?> = _verseOfTheDay.asStateFlow()

    private val _isLoadingVerse = MutableStateFlow(false)
    val isLoadingVerse: StateFlow<Boolean> = _isLoadingVerse.asStateFlow()

    init {
        // Guarantee a default configuration on initial launch so the UI is immediately functional
        viewModelScope.launch {
            val uid = currentUserUid.value
            val existing = repository.getFreedomGoal()
            if (existing == null) {
                repository.updateFreedomGoal(
                    FreedomGoal(
                        id = if (uid.isEmpty()) 1 else kotlin.math.abs(uid.hashCode()),
                        startDate = System.currentTimeMillis() - (1000 * 60 * 60 * 24 * 3), // Pre-load 3 days of victory for demo purposes
                        struggleType = "Substance Use",
                        customDeclaration = "A OverComer has submitted their life wholly to Christ and no longer fights FOR victory over addiction but rather FROM a position of victory!",
                        userId = uid
                    )
                )
            }
        }
        fetchVerseOfTheDay()
    }

    // --- Database Writers ---

    fun addVictoryLog(
        type: String, // "REFLECT", "TRIGGER", or "CBT"
        notes: String = "",
        triggerContext: String = "",
        automaticThought: String = "",
        identifiedDistortion: String = "",
        reframedTruth: String = "",
        scriptureReference: String = ""
    ) {
        viewModelScope.launch {
            repository.insertLog(
                VictoryLog(
                    type = type,
                    notes = notes,
                    triggerContext = triggerContext,
                    automaticThought = automaticThought,
                    identifiedDistortion = identifiedDistortion,
                    reframedTruth = reframedTruth,
                    scriptureReference = scriptureReference,
                    userId = currentUserUid.value
                )
            )
        }
    }

    fun deleteVictoryLog(id: Int) {
        viewModelScope.launch {
            repository.deleteLogById(id)
        }
    }

    fun updateFreedomGoal(startDateMillis: Long, struggleType: String, customDeclaration: String) {
        viewModelScope.launch {
            val uid = currentUserUid.value
            repository.updateFreedomGoal(
                FreedomGoal(
                    id = if (uid.isEmpty()) 1 else kotlin.math.abs(uid.hashCode()),
                    startDate = startDateMillis,
                    struggleType = struggleType,
                    customDeclaration = customDeclaration.ifBlank { "I can do all things through Christ who strengthens me!" },
                    userId = uid
                )
            )
        }
    }

    // --- Chat Services ---

    fun sendChatMessage(text: String) {
        if (text.isBlank()) return

        // 1. Append user message
        val userMsg = ChatMessage(text = text, isUser = true)
        _chatMessages.update { it + userMsg }

        // 2. Set loading state
        _isChatLoading.value = true

        // 3. Launch async network request in safety-conscious scope
        viewModelScope.launch {
            try {
                // Convert current thread history to standard Gemini contents format
                val conversationHistory = _chatMessages.value.map { msg ->
                    Content(
                        role = if (msg.isUser) "user" else "model",
                        parts = listOf(Part(text = msg.text))
                    )
                }

                // Call client
                val responseText = GeminiClient.generateSupportResponse(conversationHistory)

                // 4. Append AI response
                _chatMessages.update {
                    it + ChatMessage(text = responseText, isUser = false)
                }
            } catch (e: Exception) {
                _chatMessages.update {
                    it + ChatMessage(
                        text = "I failed to connect. Ensure your internet is active and that your API key is correctly configured. Lean on Proverbs 3:5-6, and try again.",
                        isUser = false
                    )
                }
            } finally {
                _isChatLoading.value = false
            }
        }
    }

    fun clearChatHistory() {
        _chatMessages.value = listOf(
            ChatMessage(
                text = "Chat history cleared. I'm here whenever you need a compassionate space to talk. Remember, you do not have to fight for victory, you are fighting FROM victory! What can we address together right now?",
                isUser = false
            )
        )
    }

    fun analyzeJournalDistortion(text: String) {
        if (text.isBlank()) return
        _isAnalyzingDistortion.value = true
        _distortionAnalysisResult.value = null
        viewModelScope.launch {
            try {
                val result = GeminiClient.analyzeCognitiveDistortion(text)
                _distortionAnalysisResult.value = result
            } catch (e: Exception) {
                _distortionAnalysisResult.value = com.example.network.DistortionAnalysisResult(
                    distortions = "Error running analysis",
                    explanation = "Failed to communicate with AI: ${e.message}",
                    reframedTruth = "God's strength is sufficient when I am weak.",
                    scriptureReference = "2 Corinthians 12:9"
                )
            } finally {
                _isAnalyzingDistortion.value = false
            }
        }
    }

    fun clearDistortionAnalysis() {
        _distortionAnalysisResult.value = null
    }

    fun fetchVerseOfTheDay(forceGenerate: Boolean = false) {
        _isLoadingVerse.value = true
        viewModelScope.launch {
            try {
                val verse = GeminiClient.generateVerseOfTheDay()
                _verseOfTheDay.value = verse
            } catch (e: Exception) {
                _verseOfTheDay.value = GeminiClient.getFallbackVerse()
            } finally {
                _isLoadingVerse.value = false
            }
        }
    }

    // --- Firebase Authentication Interaction Methods ---
    fun signUpWithEmailAndPassword(email: String, password: String, displayName: String, onResult: (Boolean, String?) -> Unit) {
        if (email.isBlank() || password.isBlank() || displayName.isBlank()) {
            onResult(false, "All fields are required.")
            return
        }
        if (isFirebaseLive.value) {
            val auth = FirebaseAuthManager.getAuthInstance()
            if (auth != null) {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = task.result?.user
                            if (user != null) {
                                val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                                    .setDisplayName(displayName)
                                    .build()
                                user.updateProfile(profileUpdates)
                                    .addOnCompleteListener { profileTask ->
                                        onResult(true, null)
                                    }
                            } else {
                                onResult(true, null)
                            }
                        } else {
                            onResult(false, task.exception?.localizedMessage ?: "Registration failed.")
                        }
                    }
            } else {
                onResult(false, "Firebase service not ready.")
            }
        } else {
            // Local Sandbox
            FirebaseAuthManager.mockSignUp(getApplication(), email, displayName, onResult)
        }
    }

    fun signInWithEmailAndPassword(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            onResult(false, "Email and password are required.")
            return
        }
        if (isFirebaseLive.value) {
            val auth = FirebaseAuthManager.getAuthInstance()
            if (auth != null) {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            onResult(true, null)
                        } else {
                            onResult(false, task.exception?.localizedMessage ?: "Authentication failed.")
                        }
                    }
            } else {
                onResult(false, "Firebase service not ready.")
            }
        } else {
            // Local Sandbox
            FirebaseAuthManager.mockSignIn(getApplication(), email, onResult)
        }
    }

    fun sendPasswordResetEmail(email: String, onResult: (Boolean, String?) -> Unit) {
        if (email.isBlank()) {
            onResult(false, "Please enter your email.")
            return
        }
        if (isFirebaseLive.value) {
            val auth = FirebaseAuthManager.getAuthInstance()
            if (auth != null) {
                auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            onResult(true, null)
                        } else {
                            onResult(false, task.exception?.localizedMessage ?: "Failed to send reset email.")
                        }
                    }
            } else {
                onResult(false, "Firebase service not ready.")
            }
        } else {
            // Local Sandbox Reset
            onResult(true, "Sent! In Sandbox mode: Reset email simulation successfully triggered.")
        }
    }

    fun logout() {
        if (isFirebaseLive.value) {
            FirebaseAuthManager.getAuthInstance()?.signOut()
        } else {
            FirebaseAuthManager.mockSignOut(getApplication())
        }
    }
}
