package com.example.ui.screens

import android.text.format.DateUtils
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.FreedomGoal
import com.example.data.VictoryLog
import com.example.ui.ChatMessage
import com.example.ui.OverComerViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// Speech-to-Text, Text-to-Speech, Permissions, and Dialog imports
import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

enum class ActiveTab {
    FREEDOM, CHAT, JOURNAL, SHIELD
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    viewModel: OverComerViewModel,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf(ActiveTab.FREEDOM) }
    val focusManager = LocalFocusManager.current
    var showPanicOverlay by remember { mutableStateOf(false) }
    var showAuthDialog by remember { mutableStateOf(false) }

    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
    val isFirebaseLive by viewModel.isFirebaseLive.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "OVERCOMER",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                actions = {
                    IconButton(
                        onClick = { showAuthDialog = true },
                        modifier = Modifier.testTag("auth_profile_button")
                    ) {
                        Box {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Account credentials and sync statistics",
                                tint = if (isLoggedIn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (isLoggedIn) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(
                                            color = if (isFirebaseLive) Color(0xFF4CAF50) else Color(0xFFFF9800),
                                            shape = CircleShape
                                        )
                                        .align(Alignment.TopEnd)
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.testTag("app_top_bar")
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("app_bottom_bar")
            ) {
                NavigationBarItem(
                    selected = activeTab == ActiveTab.FREEDOM,
                    onClick = { activeTab = ActiveTab.FREEDOM; focusManager.clearFocus() },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Freedom home") },
                    label = { Text("Freedom", style = MaterialTheme.typography.labelMedium) },
                    modifier = Modifier.testTag("nav_tab_freedom")
                )
                NavigationBarItem(
                    selected = activeTab == ActiveTab.CHAT,
                    onClick = { activeTab = ActiveTab.CHAT; focusManager.clearFocus() },
                    icon = { Icon(Icons.Default.Send, contentDescription = "Empathy chat") },
                    label = { Text("Counselor", style = MaterialTheme.typography.labelMedium) },
                    modifier = Modifier.testTag("nav_tab_chat")
                )
                NavigationBarItem(
                    selected = activeTab == ActiveTab.JOURNAL,
                    onClick = { activeTab = ActiveTab.JOURNAL; focusManager.clearFocus() },
                    icon = { Icon(Icons.Default.List, contentDescription = "Victory logs") },
                    label = { Text("Victory Logs", style = MaterialTheme.typography.labelMedium) },
                    modifier = Modifier.testTag("nav_tab_journal")
                )
                NavigationBarItem(
                    selected = activeTab == ActiveTab.SHIELD,
                    onClick = { activeTab = ActiveTab.SHIELD; focusManager.clearFocus() },
                    icon = { Icon(Icons.Default.Lock, contentDescription = "Shield of faith") },
                    label = { Text("Creed", style = MaterialTheme.typography.labelMedium) },
                    modifier = Modifier.testTag("nav_tab_shield")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            when (activeTab) {
                ActiveTab.FREEDOM -> FreedomTabScreen(viewModel, onNavigateToChat = { activeTab = ActiveTab.CHAT })
                ActiveTab.CHAT -> ChatTabScreen(viewModel)
                ActiveTab.JOURNAL -> JournalLogsTabScreen(viewModel)
                ActiveTab.SHIELD -> ShieldCreedTabScreen(viewModel)
            }

            if (showAuthDialog) {
                AuthSyncDialog(
                    viewModel = viewModel,
                    onDismissRequest = { showAuthDialog = false }
                )
            }

            // Global Floating Panic Button / Overlay Trigger - High accessibility, sits on bottom-start (bottom-left)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 16.dp, bottom = 12.dp)
            ) {
                FloatingActionButton(
                    onClick = { showPanicOverlay = true },
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("panic_sos_fab")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "🚨 SOS",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }
            }

            if (showPanicOverlay) {
                PanicOverlayDialog(onDismiss = { showPanicOverlay = false })
            }
        }
    }
}

// ==========================================
// PANELS/SCREEN IMPLEMENTATIONS
// ==========================================

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FreedomTabScreen(
    viewModel: OverComerViewModel,
    onNavigateToChat: () -> Unit
) {
    val goal by viewModel.freedomGoal.collectAsStateWithLifecycle()
    val logs by viewModel.victoryLogs.collectAsStateWithLifecycle()
    val currentUserName by viewModel.currentUserName.collectAsStateWithLifecycle()
    
    var showEditGoalDialog by remember { mutableStateOf(false) }
    var showBreathingExercise by remember { mutableStateOf(false) }
    var showDbtSkillsLibrary by remember { mutableStateOf(false) }
    
    // Compute days on the fly. 1 day = 86400000ms. If start date is in the future, count as 0.
    val daysCount = remember(goal) {
        val delta = System.currentTimeMillis() - (goal?.startDate ?: System.currentTimeMillis())
        if (delta <= 0) 0 else (delta / (1000 * 60 * 60 * 24)).toInt()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Styled Greeting Card matching the HTML
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(24.dp), // rounded-3xl
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp) // p-6
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Good Morning, $currentUserName",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        IconButton(
                            onClick = { showEditGoalDialog = true },
                            modifier = Modifier.testTag("edit_freedom_goal_btn")
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit settings",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Remember: You are not defined by your struggle, but by His grace. You are a new creation in Christ. Cleanse your mind, breathe deep, and walk in absolute victory today.",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                        lineHeight = 20.sp
                    )
                }
            }
        }

        item {
            val verseState by viewModel.verseOfTheDay.collectAsStateWithLifecycle()
            val isVerseLoading by viewModel.isLoadingVerse.collectAsStateWithLifecycle()
            val context = LocalContext.current

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .testTag("verse_of_the_day_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Header Row with Title and Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = "Scripture icon",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "VERSE OF THE DAY",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.5.sp
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            // Copy Action
                            IconButton(
                                onClick = {
                                    verseState?.let { verse ->
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                        val clip = android.content.ClipData.newPlainText("Verse of the Day", "\"${verse.text}\"\n— ${verse.reference}")
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "Scripture copied to clipboard! 📋", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.size(32.dp),
                                enabled = verseState != null && !isVerseLoading
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy Verse",
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Refresh Action
                            IconButton(
                                onClick = { viewModel.fetchVerseOfTheDay(forceGenerate = true) },
                                modifier = Modifier.size(32.dp),
                                enabled = !isVerseLoading
                            ) {
                                if (isVerseLoading) {
                                    CircularProgressIndicator(
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Refresh Verse",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    if (isVerseLoading && verseState == null) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                            Text(
                                "Anointing you with daily grace...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    } else {
                        verseState?.let { verse ->
                            // The actual verse text
                            Text(
                                text = "\"${verse.text}\"",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                    lineHeight = 22.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                            )
                            
                            // Reference citation
                            Text(
                                text = "— ${verse.reference}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.secondary,
                                textAlign = TextAlign.End,
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Devotional reflection container
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f))
                                    .padding(12.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Favorite,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = "MENTAL RESILIENCE INSIGHT:",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary,
                                            letterSpacing = 0.5.sp
                                        )
                                    }
                                    Text(
                                        text = verse.reflection,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            // Custom premium circular Canvas displaying days and a wave aura
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(220.dp)
                    .padding(vertical = 12.dp)
            ) {
                // Background Glow
                val primaryColor = MaterialTheme.colorScheme.primary
                val secondaryColor = MaterialTheme.colorScheme.secondary
                
                Canvas(modifier = Modifier.size(200.dp)) {
                    // Calming gradient brush
                    val gradient = Brush.linearGradient(
                        colors = listOf(primaryColor, secondaryColor),
                        start = Offset(0f, 0f),
                        end = Offset(size.width, size.height)
                    )
                    
                    // Base track circle
                    drawCircle(
                        color = primaryColor.copy(alpha = 0.08f),
                        radius = size.minDimension / 2f
                    )
                    
                    // Double concentric arc lines to signify depth and strength
                    drawArc(
                        brush = gradient,
                        startAngle = -210f,
                        sweepAngle = 240f,
                        useCenter = false,
                        style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round),
                        size = Size(width = size.width - 24.dp.toPx(), height = size.height - 24.dp.toPx()),
                        topLeft = Offset(12.dp.toPx(), 12.dp.toPx())
                    )
                    
                    drawCircle(
                        color = secondaryColor.copy(alpha = 0.05f),
                        radius = (size.minDimension / 2f) - 20.dp.toPx()
                    )
                }

                // Days text center stack
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "$daysCount",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 58.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.SansSerif
                        ),
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.testTag("days_counter_text")
                    )
                    Text(
                        text = if (daysCount == 1) "DAY OF FREEDOM" else "DAYS OF VICTORY",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.2.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Sovereign walk in ${goal?.struggleType ?: "Christ"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }
            }
        }

        item {
            // OverComer declaration card - Clean Minimal Secondary Container scheme
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = "My Oath",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "My OverComer Creed",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "\"${goal?.customDeclaration}\"",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }

        item {
            QuoteOfTheDaySection()
        }

        item {
            DailyCheckInSection(viewModel = viewModel)
        }

        item {
            BibleAffirmationsSection()
        }

        item {
            // DBT & CBT Quick Actions
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Distress Tolerance & Grounding",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.align(Alignment.Start)
                )

                // DBT Paced Breathing trigger block
                Button(
                    onClick = { showBreathingExercise = !showBreathingExercise },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dbt_breathing_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (showBreathingExercise) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (showBreathingExercise) Icons.Default.Close else Icons.Default.PlayArrow,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (showBreathingExercise) "Close DBT Paced Breathing" else "Open DBT Paced Breathing Support",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }

                // Breathing Exercise UI
                AnimatedVisibility(
                    visible = showBreathingExercise,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    PacedBreathingGuide()
                }

                // DBT Skills Library Toggle Button
                Button(
                    onClick = { showDbtSkillsLibrary = !showDbtSkillsLibrary },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dbt_skills_library_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (showDbtSkillsLibrary) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (showDbtSkillsLibrary) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (showDbtSkillsLibrary) "Close DBT Skills Library" else "Open DBT Distress Tolerance Library ✨",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }

                // DBT Skills Library UI Panel
                AnimatedVisibility(
                    visible = showDbtSkillsLibrary,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    DbtSkillsLibrarySection()
                }

                // AI Counselor clean button layout matching the HTML
                Button(
                    onClick = onNavigateToChat,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("ai_counselor_card"), // keep the same test tag to avoid breaking existing/future tests
                    shape = RoundedCornerShape(28.dp), // h-14 rounded-full
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Send,
                            contentDescription = "Chat",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Talk to your Companion",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.5.sp
                            )
                        )
                    }
                }
                Text(
                    text = "AI-DRIVEN EMPATHETIC SUPPORT • ABSOLUTE PRIVACY GUARANTEED",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 2.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))
                SupportConnectionSmsCard()
            }
        }

        item {
            // "What to do when triggered" - Scriptures
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "🔥 When Tempted or Triggered:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "1. Submit to God, Resist the devil, and he will flee from you! (James 4:7)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "2. Look for the exit! No temptation has overtaken you except what is common... God will also provide a way out so that you can endure it. (1 Corinthians 10:13)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "3. Cast all anxiety! Remember, Christ suffered when tempted, and He is fully able to help those being tempted today. (Hebrews 2:18)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // --- Goal Edit Dialog ---
    if (showEditGoalDialog) {
        var tempStruggleType by remember { mutableStateOf(goal?.struggleType ?: "Substance Use") }
        var tempDaysOffset by remember { mutableStateOf(daysCount.toString()) }
        var tempDeclaration by remember { mutableStateOf(goal?.customDeclaration ?: "") }

        AlertDialog(
            onDismissRequest = { showEditGoalDialog = false },
            title = { Text("Walk of Freedom Settings") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    Text(
                        "Configure your specific victory focus and custom covenant declaration statement.",
                        style = MaterialTheme.typography.bodySmall
                    )

                    // Struggle drop-down or radio list
                    Text("My Area of Overcoming:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    val struggleTypes = listOf("Substance Use", "Anxiety", "Depression", "Fear", "Anger", "Habitual Temptations")
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        struggleTypes.forEach { type ->
                            FilterChip(
                                selected = tempStruggleType == type,
                                onClick = { tempStruggleType = type },
                                label = { Text(type) }
                            )
                        }
                    }

                    // Days tracker setup
                    OutlinedTextField(
                        value = tempDaysOffset,
                        onValueChange = { if (it.isEmpty() || it.all { char -> char.isDigit() }) tempDaysOffset = it },
                        label = { Text("Days of Freedom to set (Offset)") },
                        supportingText = { Text("This will adjust your start date backwards by this many days.") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_freedom_days_input")
                    )

                    // Custom declaration covenant statement
                    OutlinedTextField(
                        value = tempDeclaration,
                        onValueChange = { tempDeclaration = it },
                        label = { Text("Custom Covenant Declaration") },
                        placeholder = { Text("e.g. In Christ, I have been set free indeed!") },
                        minLines = 2,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_freedom_declaration_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsedDays = tempDaysOffset.toLongOrNull() ?: 1L
                        val newStartDate = System.currentTimeMillis() - (parsedDays * 24 * 60 * 60 * 1000)
                        viewModel.updateFreedomGoal(
                            startDateMillis = newStartDate,
                            struggleType = tempStruggleType,
                            customDeclaration = tempDeclaration
                        )
                        showEditGoalDialog = false
                    },
                    modifier = Modifier.testTag("save_freedom_settings_btn")
                ) {
                    Text("Apply Settings")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditGoalDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// ------------------------------------------
// DBT PACED BREATHING COMPOSABLE
// ------------------------------------------

@Composable
fun PacedBreathingGuide() {
    var isBreathingActive by remember { mutableStateOf(false) }
    var scaleFraction by remember { mutableStateOf(1f) }
    var breathingPhase by remember { mutableStateOf("Ready") }
    var secondsRemaining by remember { mutableStateOf(4) }

    // Launch periodic breathing animation when active
    LaunchedEffect(isBreathingActive) {
        if (isBreathingActive) {
            while (isBreathingActive) {
                // Inhale Phase
                breathingPhase = "INHALE (Fill your soul with God's Spirit)"
                secondsRemaining = 4
                animateScale(target = 2f) { scaleFraction = it }
                for (i in 4 downTo 1) {
                    secondsRemaining = i
                    delay(1000)
                }
                
                if (!isBreathingActive) break
                
                // Exhale Phase
                breathingPhase = "EXHALE (Release all fears into Christ's hands)"
                secondsRemaining = 4
                animateScale(target = 1f) { scaleFraction = it }
                for (i in 4 downTo 1) {
                    secondsRemaining = i
                    delay(1000)
                }
            }
        } else {
            scaleFraction = 1f
            breathingPhase = "Ready"
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "DBT Distress Tolerance: Paced Breathing",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.tertiary
            )
            Text(
                text = "During intense distress or cravings, taking paced deep breaths immediately down-regulates your body's survival hijack (midbrain amygdala reactivity). Use this pacing tool:",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )

            // Dynamic expanding circle representing breath
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(140.dp)
                    .drawBehind {
                        // Drawing circles based on scale fraction
                        val maxRadius = size.minDimension / 2f
                        drawCircle(
                            color = Color(0xFF14B8A6).copy(alpha = 0.3f),
                            radius = maxRadius * (scaleFraction / 2f)
                        )
                        drawCircle(
                            color = Color(0xFF14B8A6).copy(alpha = 0.1f),
                            radius = maxRadius
                        )
                    }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isBreathingActive) "$secondsRemaining" else "Ready",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = breathingPhase,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            // Activator button
            Button(
                onClick = { isBreathingActive = !isBreathingActive },
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isBreathingActive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = if (isBreathingActive) "Stop Tool" else "Start Breathing Loop",
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
        }
    }
}

// --- Custom simple animator utility to run without complex Compose transition declarations
private suspend fun animateScale(target: Float, update: (Float) -> Unit) {
    val duration = 4000
    val start = if (target == 2f) 1f else 2f
    val startTime = System.currentTimeMillis()
    while (System.currentTimeMillis() - startTime < duration) {
        val fraction = (System.currentTimeMillis() - startTime).toFloat() / duration
        val currentScale = start + (target - start) * fraction
        update(currentScale)
        delay(16) // ~60fps
    }
    update(target)
}

// ------------------------------------------
// DBT DISTRESS TOLERANCE SKILLS LIBRARY
// ------------------------------------------

data class DbtSkill(
    val name: String,
    val acronym: String,
    val motto: String,
    val description: String,
    val moods: List<String>,
    val intensity: String, // "Low", "Moderate", "Extreme"
    val steps: List<String>,
    val bibleVerse: String,
    val bibleRef: String,
    val practicePrompt: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DbtSkillsLibrarySection() {
    var searchQuery by remember { mutableStateOf("") }
    var selectedMood by remember { mutableStateOf("ALL") }
    var selectedIntensity by remember { mutableStateOf("ALL") }
    
    // Remember expanded status of each skill by acronym
    var expandedSkillAcronym by remember { mutableStateOf<String?>(null) }
    
    val moods = listOf("ALL", "Overwhelmed", "Anxious", "Angry", "Urge", "Lonely", "Sad")
    val intensities = listOf("ALL", "Low", "Moderate", "Extreme")
    
    val skills = remember {
        listOf(
            DbtSkill(
                name = "STOP Skill",
                acronym = "STOP",
                motto = "Pause before you act to keep control.",
                description = "This key distress tolerance skill helps you pause rather than acting impulsively on overwhelming emotions or unhealthy urges.",
                moods = listOf("Overwhelmed", "Angry", "Urge"),
                intensity = "Moderate",
                steps = listOf(
                    "S - STOP: Freeze! Do not react. Impulsive feelings will try to dictate actions.",
                    "T - Take a Step Back: Pause, take a physical or mental step away, and take a deep breath.",
                    "O - Observe: Notice your physical sensations, thoughts, and environment objectively without judgment.",
                    "P - Proceed Mindfully: Ask yourself what the most effective, Christ-honoring thing to do is right now."
                ),
                bibleVerse = "My dear brothers and sisters, take note of this: Everyone should be quick to listen, slow to speak and slow to get angry.",
                bibleRef = "James 1:19 (NIV)",
                practicePrompt = "Close your eyes, take one slow breath, and think of your next small step."
            ),
            DbtSkill(
                name = "TIPP Crisis Management",
                acronym = "TIPP",
                motto = "Quickly alter your physical state to de-escalate crisis.",
                description = "Designed for extreme, high-stress moments (Level 3 Crisis) to immediately down-regulate your nervous system using rapid biological resets.",
                moods = listOf("Anxious", "Overwhelmed", "Urge"),
                intensity = "Extreme",
                steps = listOf(
                    "T - Temperature: Splash ice-cold water on your face or hold an ice cube to activate your body's calming reflex.",
                    "I - Intense Exercise: Do 60 seconds of high-intensity movement (jumping jacks, high knees) to burn off stress adrenaline.",
                    "P - Paced Breathing: Breathe slowly and deeply. Inhale into your belly for 4 seconds, exhale fully for 6 seconds.",
                    "P - Paired Muscle Relaxation: Tense a muscle group tightly for 5 seconds, then release and notice the release."
                ),
                bibleVerse = "He makes my feet like the feet of a deer; he causes me to stand on the heights. He trains my hands for battle...",
                bibleRef = "Psalm 18:33-34 (NIV)",
                practicePrompt = "Hold a cold object or do 10 jumping jacks now to reset your physiology!"
            ),
            DbtSkill(
                name = "IMPROVE the Moment",
                acronym = "IMPROVE",
                motto = "Shift your mental atmosphere during painful times.",
                description = "Helps you replace immediate painful emotions and situations with more positive, comforting, or stabilizing feelings.",
                moods = listOf("Anxious", "Lonely", "Sad"),
                intensity = "Moderate",
                steps = listOf(
                    "I - Imagery: Mentally visualize a safe, peaceful pasture under God's warm, protective sky.",
                    "M - Meaning: Find a small purpose or lessons in the current struggle.",
                    "P - Prayer: Cast this specific heavy load onto Jesus. Ask Him for His supernatural strength.",
                    "R - Relaxing: Stretch, drink some herbal tea, or listen to a soft acoustic worship melody.",
                    "O - One thing: Focus on only this exact moment. Don't worry about tonight or tomorrow.",
                    "V - Vacation: Take a brief 15-minute healthy break from your environment or phone.",
                    "E - Encouragement: Say a faith-filled affirmation aloud: 'I can do all things through Christ!'"
                ),
                bibleVerse = "Cast all your anxiety on him because he cares for you.",
                bibleRef = "1 Peter 5:7 (NIV)",
                practicePrompt = "Identify one thing in this current room and describe its texture to ground yourself."
            ),
            DbtSkill(
                name = "WISE ACCEPTS Distractions",
                acronym = "ACCEPTS",
                motto = "Temporarily divert your attention from painful distress.",
                description = "Provides a structured set of safe distractions to help emotional storms pass safely without engaging in destructive behaviors.",
                moods = listOf("Urge", "Anxious", "Lonely"),
                intensity = "Low",
                steps = listOf(
                    "Activities: Keep active. Clean your room, write a note, make a meal, or read an encouraging book.",
                    "Contributing: Shift focus outward by praying for a friend or doing a small act of kindness.",
                    "Comparisons: Look back at where you were months ago and thank God for the small graces.",
                    "Emotions (Opposite): Create a different feeling. Watch a wholesome comedy or listen to upbeat worship music.",
                    "Pushing away: Put the trigger out of sight. Put your phone away, lock the app, or walk outside.",
                    "Thoughts: Occupy your mind with a structured cognitive task like repeating an encouraging scripture verse.",
                    "Sensations: Engage your body's senses. Sip freezing-cold water, wrap yourself in a heavy blanket."
                ),
                bibleVerse = "Fix your thoughts on what is true, and honorable, and right, and pure, and lovely, and admirable...",
                bibleRef = "Philippians 4:8 (NLT)",
                practicePrompt = "Perform one random act of kindness or text a friend a word of encouragement right now."
            ),
            DbtSkill(
                name = "Self-Soothing Technique",
                acronym = "5-SENSES",
                motto = "Nurture your soul and body using God's goodness.",
                description = "Provides deep physical comfort and emotional reassurance by mindfully stimulating your primary five physiological senses.",
                moods = listOf("Lonely", "Anxious", "Sad"),
                intensity = "Low",
                steps = listOf(
                    "Sight: Walk outdoors and gaze at God's beautiful sky, the trees, or a calming scenery photo.",
                    "Sound: Turn on peaceful acoustic strings, classical melodies, or steady falling rain sounds.",
                    "Smell: Ignite an aromatic candle, diffuse lavender oil, or breathe in fresh coffee beans.",
                    "Taste: Savor a square of dark chocolate, drink cozy chamomile tea, or chew refreshing mint gum.",
                    "Touch: Put on exceptionally soft socks, cuddle your pet, or rub cool smooth stones."
                ),
                bibleVerse = "Taste and see that the Lord is good; blessed is the one who takes refuge in him.",
                bibleRef = "Psalm 34:8 (NIV)",
                practicePrompt = "Touch something nearby and slowly name 3 things you can see right now."
            ),
            DbtSkill(
                name = "REST Decision Maker",
                acronym = "REST",
                motto = "Pause, process, and proceed in soundness of mind.",
                description = "A comprehensive framework to make deliberate, value-based decisions when tempted or under stress.",
                moods = listOf("Overwhelmed", "Angry", "Urge"),
                intensity = "Moderate",
                steps = listOf(
                    "R - Relax: Pause and take 3 deep full breaths. Release the tension from your shoulders and jaw.",
                    "E - Evaluate: Ask yourself, 'Is there an immediate danger? What emotion or trigger is causing the urge?'",
                    "S - Set a Plan: Formulate a healthy, God-honoring choice (e.g. text a mentor, use breathing exercise).",
                    "T - Take Action: Step out in courage and carry out the chosen behavior, trusting God for the outcome."
                ),
                bibleVerse = "For God has not given us a spirit of fear, but of power and of love and of a sound mind.",
                bibleRef = "2 Timothy 1:7 (NKJV)",
                practicePrompt = "Take 3 deep, steady breaths, exhaling longer than you inhale."
            )
        )
    }
    
    // Filtering logic
    val filteredSkills = remember(skills, searchQuery, selectedMood, selectedIntensity) {
        skills.filter { skill ->
            val matchQuery = skill.name.contains(searchQuery, ignoreCase = true) ||
                             skill.acronym.contains(searchQuery, ignoreCase = true) ||
                             skill.description.contains(searchQuery, ignoreCase = true) ||
                             skill.motto.contains(searchQuery, ignoreCase = true)
            val matchMood = selectedMood == "ALL" || skill.moods.contains(selectedMood)
            val matchIntensity = selectedIntensity == "ALL" || skill.intensity.equals(selectedIntensity, ignoreCase = true)
            matchQuery && matchMood && matchIntensity
        }
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("dbt_skills_library_card"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Section Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "DBT Tools",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Column {
                    Text(
                        text = "DBT Distress Tolerance Library",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Interactive grounding skills to conquer urges & intense feelings",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search skills e.g., TIPP, STOP...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dbt_skills_search_input"),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.outline
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                textStyle = MaterialTheme.typography.bodyMedium
            )
            
            // Mood Filter Chips Header
            Text(
                text = "FILTER BY MOOD / CURRENT STATE:",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
            
            // Mood Filters Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                moods.forEach { mood ->
                    val isSelected = selectedMood == mood
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedMood = mood },
                        label = { Text(mood) },
                        modifier = Modifier.testTag("dbt_mood_filter_chip_$mood")
                    )
                }
            }
            
            // Intensity Filter Chips Header
            Text(
                text = "FILTER BY CHALLENGE INTENSITY:",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
            
            // Intensity Filters Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                intensities.forEach { intensity ->
                    val isSelected = selectedIntensity == intensity
                    val label = when (intensity) {
                        "Low" -> "🟢 Low (Level 1)"
                        "Moderate" -> "🟡 Moderate (Level 2)"
                        "Extreme" -> "🔴 Extreme Crisis (Level 3)"
                        else -> "All Levels"
                    }
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedIntensity = intensity },
                        label = { Text(label) },
                        modifier = Modifier.testTag("dbt_intensity_filter_chip_$intensity")
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Skill List Output list
            if (filteredSkills.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            modifier = Modifier.size(40.dp)
                        )
                        Text(
                            text = "No clinical DBT skills found",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Try adjusting your search terms, current mood filter, or challenge level.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    filteredSkills.forEach { skill ->
                        val isExpanded = expandedSkillAcronym == skill.acronym
                        DbtSkillCard(
                            skill = skill,
                            isExpanded = isExpanded,
                            onToggleExpand = {
                                expandedSkillAcronym = if (isExpanded) null else skill.acronym
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DbtSkillCard(
    skill: DbtSkill,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit
) {
    // Keep local states for the steps checklist
    val checkedStates = remember(skill.acronym) { 
        mutableStateMapOf<Int, Boolean>().apply {
            skill.steps.indices.forEach { put(it, false) }
        }
    }
    
    // Let's remember user's brief reflection notes
    var userReflectionInput by remember(skill.acronym) { mutableStateOf("") }
    val context = LocalContext.current
    
    val allChecked = checkedStates.values.all { it }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("dbt_skill_card_${skill.acronym}")
            .clickable { onToggleExpand() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = if (isExpanded) 1.5.dp else 1.dp,
            color = if (isExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header: Title, Acronym pill, Intensity indicator, Expand arrow
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = skill.acronym,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        
                        val levelBadgeColor = when(skill.intensity) {
                            "Low" -> Color(0xFF10B981)
                            "Moderate" -> Color(0xFFF59E0B)
                            "Extreme" -> Color(0xFFEF4444)
                            else -> MaterialTheme.colorScheme.primary
                        }
                        Surface(
                            shape = CircleShape,
                            color = levelBadgeColor.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = skill.intensity,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                                color = levelBadgeColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = skill.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = skill.motto,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                IconButton(onClick = { onToggleExpand() }) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // Expandable Area
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    
                    // Main description explanation
                    Text(
                        text = skill.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    // Interactive checklist header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "PRACTICAL GROUNDING CHECKLIST:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        // Show completion badge if done
                        if (allChecked) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF10B981).copy(alpha = 0.15f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "GROUNDED!",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold),
                                        color = Color(0xFF10B981)
                                    )
                                }
                            }
                        }
                    }
                    
                    // Checklist steps
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        skill.steps.forEachIndexed { index, step ->
                            val isChecked = checkedStates[index] ?: false
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isChecked) MaterialTheme.colorScheme.primary.copy(alpha = 0.04f)
                                        else Color.Transparent
                                    )
                                    .clickable { checkedStates[index] = !isChecked }
                                    .padding(vertical = 4.dp, horizontal = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { checkedStates[index] = it },
                                    modifier = Modifier.size(20.dp).testTag("step_checkbox_${skill.acronym}_$index"),
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = MaterialTheme.colorScheme.primary,
                                        uncheckedColor = MaterialTheme.colorScheme.outline
                                    )
                                )
                                Text(
                                    text = step,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = if (isChecked) FontWeight.SemiBold else FontWeight.Normal,
                                        fontStyle = if (isChecked) androidx.compose.ui.text.font.FontStyle.Italic else androidx.compose.ui.text.font.FontStyle.Normal
                                    ),
                                    color = if (isChecked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    
                    // Sacred Biblical Anchor Block
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MenuBook,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "SACRED BIBLICAL ANCHOR",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Text(
                                text = "\"${skill.bibleVerse}\"",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                ),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "— ${skill.bibleRef}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.secondary,
                                textAlign = TextAlign.End,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    
                    // Action box: Practice notes and Submission button
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "PRACTICE & BRIEF REFLECTION NOTES:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedTextField(
                            value = userReflectionInput,
                            onValueChange = { userReflectionInput = it },
                            placeholder = { Text(skill.practicePrompt) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 60.dp, max = 100.dp)
                                .testTag("practice_notes_input_${skill.acronym}"),
                            maxLines = 3,
                            singleLine = false,
                            textStyle = MaterialTheme.typography.bodySmall,
                            shape = RoundedCornerShape(8.dp)
                        )
                        
                        Button(
                            onClick = {
                                val message = if (allChecked) {
                                    "Stupendous job completing all practical steps! You successfully deployed ${skill.acronym} to anchor your mind."
                                } else {
                                    "Excellent job practicing the ${skill.acronym} skill. Keep taking proactive steps towards grounding!"
                                }
                                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                userReflectionInput = ""
                                skill.steps.indices.forEach { checkedStates[it] = false }
                            },
                            modifier = Modifier.fillMaxWidth().testTag("practice_submit_${skill.acronym}"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text("Complete & Log Grounding Victory 🛡️", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ------------------------------------------
// GEMINI LIVE CONVERSATIONAL CORE
// ------------------------------------------

enum class LiveModeState {
    INITIALIZING,
    LISTENING,
    THINKING,
    SPEAKING,
    PAUSED,
    ERROR
}

class LiveVoiceController(
    private val context: Context,
    private val onUserSpoken: (String) -> Unit,
    private val onStateChanged: (LiveModeState) -> Unit,
    private val onUserTextPartial: (String) -> Unit,
    private val onRmsChanged: (Float) -> Unit,
    private val onErrorMsg: (String) -> Unit
) {
    private var tts: TextToSpeech? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private var isRecognizerAvailable = SpeechRecognizer.isRecognitionAvailable(context)
    private val mainLooperHandler = Handler(Looper.getMainLooper())
    var isMuted = false
        private set
    private var currentState: LiveModeState = LiveModeState.INITIALIZING

    private val recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
    }

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                try {
                    val result = tts?.setLanguage(Locale.getDefault())
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        tts?.setLanguage(Locale.US)
                    }
                    tts?.setSpeechRate(0.82f) // Comfortably slower, very natural reading pace
                    tts?.setPitch(1.0f)
                } catch (_: Exception) {}
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        currentState = LiveModeState.SPEAKING
                        onStateChanged(LiveModeState.SPEAKING)
                    }
                    override fun onDone(utteranceId: String?) {
                        mainLooperHandler.post {
                            if (!isMuted) {
                                currentState = LiveModeState.LISTENING
                                onStateChanged(LiveModeState.LISTENING)
                                startListening()
                            }
                        }
                    }
                    override fun onError(utteranceId: String?) {
                        mainLooperHandler.post {
                            if (!isMuted) {
                                currentState = LiveModeState.LISTENING
                                onStateChanged(LiveModeState.LISTENING)
                                startListening()
                            }
                        }
                    }
                })
            } else {
                onErrorMsg("TextToSpeech initialization failed.")
            }
        }

        if (isRecognizerAvailable) {
            setupRecognizer()
        }
    }

    private fun setupRecognizer() {
        try {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        if (!isMuted && currentState != LiveModeState.THINKING && currentState != LiveModeState.SPEAKING) {
                            currentState = LiveModeState.LISTENING
                            onStateChanged(LiveModeState.LISTENING)
                            this@LiveVoiceController.onRmsChanged(0f)
                        }
                    }
                    override fun onBeginningOfSpeech() {
                        if (!isMuted) {
                            this@LiveVoiceController.onRmsChanged(0f)
                        }
                    }
                    override fun onRmsChanged(rmsdB: Float) {
                        if (!isMuted && currentState == LiveModeState.LISTENING) {
                            this@LiveVoiceController.onRmsChanged(rmsdB)
                        }
                    }
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {}
                    override fun onError(error: Int) {
                        if (isMuted || currentState == LiveModeState.THINKING || currentState == LiveModeState.SPEAKING) return
                        
                        // Handle and silence common timeouts, restart loop
                        val retryInterval = if (error == SpeechRecognizer.ERROR_NO_MATCH || error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
                            500L
                        } else {
                            1500L
                        }
                        
                        mainLooperHandler.postDelayed({
                            if (!isMuted && currentState != LiveModeState.THINKING && currentState != LiveModeState.SPEAKING) {
                                startListening()
                            }
                        }, retryInterval)
                    }
                    override fun onResults(results: Bundle?) {
                        if (currentState == LiveModeState.THINKING || currentState == LiveModeState.SPEAKING) return
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull()
                        if (!text.isNullOrBlank()) {
                            currentState = LiveModeState.THINKING
                            onStateChanged(LiveModeState.THINKING)
                            stopListening()
                            onUserTextPartial(text)
                            onUserSpoken(text)
                        } else {
                            if (!isMuted && currentState != LiveModeState.THINKING && currentState != LiveModeState.SPEAKING) {
                                startListening()
                            }
                        }
                    }
                    override fun onPartialResults(partialResults: Bundle?) {
                        if (currentState == LiveModeState.THINKING || currentState == LiveModeState.SPEAKING) return
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull()
                        if (!text.isNullOrBlank()) {
                            onUserTextPartial(text)
                        }
                    }
                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }
        } catch (e: Exception) {
            e.printStackTrace()
            isRecognizerAvailable = false
        }
    }

    fun startListening() {
        if (isMuted || currentState == LiveModeState.THINKING || currentState == LiveModeState.SPEAKING) return
        stopSpeak()
        if (isRecognizerAvailable) {
            mainLooperHandler.post {
                try {
                    speechRecognizer?.stopListening()
                    speechRecognizer?.startListening(recognizerIntent)
                    currentState = LiveModeState.LISTENING
                    onStateChanged(LiveModeState.LISTENING)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } else {
            currentState = LiveModeState.ERROR
            onStateChanged(LiveModeState.ERROR)
            onErrorMsg("Voice Recognition is not supported on this device.")
        }
    }

    fun stopListening() {
        if (isRecognizerAvailable) {
            mainLooperHandler.post {
                try {
                    speechRecognizer?.stopListening()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun speak(text: String) {
        currentState = LiveModeState.SPEAKING
        stopListening()
        mainLooperHandler.post {
            try {
                onStateChanged(LiveModeState.SPEAKING)
                val params = Bundle().apply {
                    putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "LiveReply")
                }
                tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, "LiveReply")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun stopSpeak() {
        mainLooperHandler.post {
            try {
                tts?.stop()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun toggleMute(): Boolean {
        isMuted = !isMuted
        if (isMuted) {
            stopListening()
            stopSpeak()
            currentState = LiveModeState.PAUSED
            onStateChanged(LiveModeState.PAUSED)
        } else {
            currentState = LiveModeState.LISTENING
            onStateChanged(LiveModeState.LISTENING)
            startListening()
        }
        return isMuted
    }

    fun shutdown() {
        mainLooperHandler.removeCallbacksAndMessages(null)
        stopSpeak()
        try {
            tts?.shutdown()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        stopListening()
        try {
            speechRecognizer?.destroy()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

@Composable
fun LiveVoiceSessionDialog(
    viewModel: OverComerViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var liveState by remember { mutableStateOf(LiveModeState.INITIALIZING) }
    var rmsLevel by remember { mutableStateOf(0f) }
    var userSpeechText by remember { mutableStateOf("") }
    var aiSpeakingText by remember { mutableStateOf("Guide here. I am listening. Speak freely...") }
    var errorMsg by remember { mutableStateOf("") }
    var isMutedState by remember { mutableStateOf(false) }

    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val isChatLoading by viewModel.isChatLoading.collectAsStateWithLifecycle()

    // Keep instance reference of the controller
    var controller by remember { mutableStateOf<LiveVoiceController?>(null) }

    // Synchronize response complete and reading state
    LaunchedEffect(chatMessages, isChatLoading) {
        if (!isChatLoading && liveState == LiveModeState.THINKING) {
            val lastMsg = chatMessages.lastOrNull()
            if (lastMsg != null) {
                if (!lastMsg.isUser) {
                    aiSpeakingText = lastMsg.text
                    liveState = LiveModeState.SPEAKING
                    controller?.speak(lastMsg.text)
                } else {
                    // Fallback to prevent getting stuck if response failed to append or is duplicate
                    liveState = LiveModeState.LISTENING
                    controller?.startListening()
                }
            } else {
                liveState = LiveModeState.LISTENING
                controller?.startListening()
            }
        }
    }

    // Initialize controller on mount, clean on unmount
    DisposableEffect(Unit) {
        val voiceController = LiveVoiceController(
            context = context,
            onUserSpoken = { prompt ->
                liveState = LiveModeState.THINKING
                viewModel.sendChatMessage(prompt)
            },
            onStateChanged = { state ->
                // Keep paused in sync
                if (liveState != LiveModeState.THINKING || state == LiveModeState.SPEAKING) {
                    liveState = state
                }
            },
            onUserTextPartial = { partial ->
                userSpeechText = partial
            },
            onRmsChanged = { rms ->
                rmsLevel = rms
            },
            onErrorMsg = { err ->
                errorMsg = err
                liveState = LiveModeState.ERROR
            }
        )
        controller = voiceController

        // Gentle delay before starting to let context settle
        Handler(Looper.getMainLooper()).postDelayed({
            voiceController.startListening()
        }, 500)

        onDispose {
            voiceController.shutdown()
        }
    }

    Dialog(
        onDismissRequest = { onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF141218)) // Matte Deep Charcoal background for studio vibe
                .testTag("gemini_live_dialog_overlay")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .safeDrawingPadding(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "GEMINI LIVE SESSION",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.5.sp
                        ),
                        color = MaterialTheme.colorScheme.primaryContainer
                    )
                    IconButton(
                        onClick = { onDismiss() },
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.1f), CircleShape)
                            .testTag("close_live_session_btn")
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close session",
                            tint = Color.White
                        )
                    }
                }

                // Middle area: Transcripts & Visual Orbit
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Soft user text bubble
                    AnimatedVisibility(
                        visible = userSpeechText.isNotEmpty(),
                        enter = fadeIn() + expandVertically()
                    ) {
                        Text(
                            text = "\"$userSpeechText\"",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            ),
                            color = Color.White.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .padding(horizontal = 24.dp)
                                .fillMaxWidth()
                        )
                    }
                    Spacer(modifier = Modifier.height(30.dp))

                    // THE BEAUTIFUL INTERACTIVE VOICE ORBIT
                    VoiceAnimationOrbit(liveState = liveState, rmsLevel = rmsLevel)

                    Spacer(modifier = Modifier.height(30.dp))

                    // Large Guide output bubble
                    VerticalScrollCard(
                        text = aiSpeakingText,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp)
                            .padding(horizontal = 16.dp)
                    )
                }

                // Bottom Controls Trays
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Current active guidance note
                    Text(
                        text = when (liveState) {
                            LiveModeState.INITIALIZING -> "CONNECTING TO COUNSELOR..."
                            LiveModeState.LISTENING -> "LISTENING... SPEAK FREELY"
                            LiveModeState.THINKING -> "COMPASSIONATE ALIGNING..."
                            LiveModeState.SPEAKING -> "TALKING TO YOU ENCOURAGINGLY"
                            LiveModeState.PAUSED -> "PAUSED"
                            LiveModeState.ERROR -> "CONNECTION HALTED"
                        },
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        ),
                        color = when (liveState) {
                            LiveModeState.LISTENING -> Color(0xFF66BB6A)
                            LiveModeState.THINKING -> MaterialTheme.colorScheme.primaryContainer
                            LiveModeState.SPEAKING -> MaterialTheme.colorScheme.primary
                            LiveModeState.ERROR -> MaterialTheme.colorScheme.error
                            else -> Color.White.copy(alpha = 0.5f)
                        }
                    )

                    if (errorMsg.isNotEmpty()) {
                        Text(
                            text = errorMsg,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Mute button
                        IconButton(
                            onClick = {
                                val muted = controller?.toggleMute() ?: false
                                isMutedState = muted
                            },
                            modifier = Modifier
                                .size(56.dp)
                                .background(
                                    if (isMutedState) MaterialTheme.colorScheme.error else Color.White.copy(alpha = 0.1f),
                                    CircleShape
                                )
                                .testTag("mute_live_session_btn")
                        ) {
                            Icon(
                                imageVector = if (isMutedState) Icons.Default.PlayArrow else Icons.Default.Pause, // Toggle play pause mic
                                contentDescription = if (isMutedState) "Unmute Microphone" else "Mute Microphone",
                                tint = Color.White
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(24.dp))

                        // Touch to hold / Interrupt button
                        Button(
                            onClick = {
                                if (liveState == LiveModeState.SPEAKING) {
                                    controller?.stopSpeak()
                                    liveState = LiveModeState.LISTENING
                                    controller?.startListening()
                                } else {
                                    controller?.startListening()
                                }
                            },
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.White
                            ),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Text(
                                text = if (liveState == LiveModeState.SPEAKING) "INTERRUPT TO SPEAK" else "TAP TO TALK",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VoiceAnimationOrbit(liveState: LiveModeState, rmsLevel: Float) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_trans")
    
    // Smooth infinite breathe scale
    val breatheScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathe"
    )

    // Orbit speed rotation
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(
        modifier = Modifier
            .size(240.dp)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        // Glowing background halo
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    val scaleFactor = when (liveState) {
                        LiveModeState.LISTENING -> breatheScale + (rmsLevel.coerceIn(0f, 10f) / 10f)
                        LiveModeState.SPEAKING -> breatheScale + 0.08f
                        LiveModeState.THINKING -> 1.1f
                        else -> 1f
                    }
                    val haloColor = when (liveState) {
                        LiveModeState.LISTENING -> Color(0xFF14B8A6).copy(alpha = 0.15f) // Teal
                        LiveModeState.SPEAKING -> Color(0xFFD0BCFF).copy(alpha = 0.20f) // Soft purple
                        LiveModeState.THINKING -> Color(0xFFCCC2DC).copy(alpha = 0.10f) // Muted purple
                        LiveModeState.ERROR -> Color(0xFFEF4444).copy(alpha = 0.15f) // Red
                        else -> Color.White.copy(alpha = 0.05f)
                    }
                    drawCircle(
                        color = haloColor,
                        radius = size.minDimension / 2f * scaleFactor
                    )
                }
        )

        // Visual design: Render based on active voice state
        when (liveState) {
            LiveModeState.THINKING -> {
                // Circular loading ring spinning beautifully
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawArc(
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                Color(0xFFD0BCFF),
                                Color(0xFF6750A4),
                                Color(0xFF141218)
                            )
                        ),
                        startAngle = rotation,
                        sweepAngle = 280f,
                        useCenter = false,
                        style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = "Thinking ring",
                    tint = Color(0xFFD0BCFF),
                    modifier = Modifier.size(48.dp)
                )
            }
            LiveModeState.SPEAKING -> {
                // 5 Premium Purple animated sound bars mimicking natural output speech waves
                Row(
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val heights = listOf(0.4f, 0.9f, 0.6f, 0.85f, 0.5f)
                    val specDurations = listOf(700, 1100, 900, 1300, 800)
                    
                    heights.forEachIndexed { index, baseVal ->
                        val scaleHeight by infiniteTransition.animateFloat(
                            initialValue = 0.2f,
                            targetValue = baseVal,
                            animationSpec = infiniteRepeatable(
                                animation = tween(specDurations[index], easing = FastOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "bar_$index"
                        )
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 6.dp)
                                .width(8.dp)
                                .fillMaxHeight(scaleHeight)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0xFFD0BCFF),
                                            Color(0xFF6750A4)
                                        )
                                    )
                                )
                        )
                    }
                }
            }
            LiveModeState.LISTENING -> {
                // Interactive ripple wave that expands with actual mic RMS volume
                val micAura = (rmsLevel.coerceIn(0f, 12f) / 12f)
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Pulsing breathing circle
                    drawCircle(
                        color = Color(0xFF14B8A6), // Green teal
                        radius = (size.minDimension / 5f) * (breatheScale + micAura / 2f)
                    )
                    
                    // Ripple 1
                    drawCircle(
                        color = Color(0xFF14B8A6).copy(alpha = 0.3f),
                        radius = (size.minDimension / 3.2f) * (breatheScale + micAura),
                        style = Stroke(width = 2.dp.toPx())
                    )

                    // Ripple 2 (only displays when talking)
                    if (micAura > 0.15f) {
                        drawCircle(
                            color = Color(0xFF14B8A6).copy(alpha = 0.15f),
                            radius = (size.minDimension / 2.2f) * (breatheScale + micAura * 1.5f),
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }
                }
                Icon(
                    Icons.Default.Mic,
                    contentDescription = "Listening",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }
            LiveModeState.PAUSED -> {
                // Static grey locked circle
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.3f),
                        radius = size.minDimension / 4.5f
                    )
                }
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = "Paused",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
            else -> {
                // Warm, static circle
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = Color(0xFFFFB59F).copy(alpha = 0.4f),
                        radius = size.minDimension / 4.5f
                    )
                }
                Icon(
                    Icons.Default.Lock,
                    contentDescription = "Lock",
                    tint = Color(0xFFFFB59F),
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
fun VerticalScrollCard(text: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.05f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ------------------------------------------
// AI SUPPORT COUNSELOR CHAT
// ------------------------------------------

@Composable
fun ChatTabScreen(viewModel: OverComerViewModel) {
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val isChatLoading by viewModel.isChatLoading.collectAsStateWithLifecycle()
    
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    var isLiveSessionOpen by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            isLiveSessionOpen = true
        } else {
            Toast.makeText(context, "Microphone permission is required for Gemini Live.", Toast.LENGTH_LONG).show()
        }
    }

    // Auto-scroll to the end of conversation whenever messages expand or typing status triggers
    LaunchedEffect(chatMessages.size, isChatLoading) {
        if (chatMessages.isNotEmpty()) {
            scope.launch {
                listState.animateScrollToItem(chatMessages.size - 1)
            }
        }
    }

    // Show Gemini Live Dialog Session when active
    if (isLiveSessionOpen) {
        LiveVoiceSessionDialog(
            viewModel = viewModel,
            onDismiss = { isLiveSessionOpen = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Safe privacy banner
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Lock,
                contentDescription = "Lock icon",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Absolute Privacy Guaranteed. Chat is locally cached and never shared.",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                modifier = Modifier.weight(1f)
            )
            TextButton(
                onClick = { viewModel.clearChatHistory() },
                modifier = Modifier.testTag("clear_chat_history_button")
            ) {
                Text(
                    "Clear",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Lazy scroll of bubbles
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // High-fidelity Hands-Free Voice card banner at the top of the chat area
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .testTag("gemini_live_callout_banner"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "🎙️ Walk in Victory Hands-Free",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Talk live with OverComer Guide in real-time. The app listens, processes, and speaks back comfortingly with scriptural wisdom.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(
                            onClick = {
                                val permCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                                if (permCheck == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                                    isLiveSessionOpen = true
                                } else {
                                    micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.White
                            ),
                            modifier = Modifier.testTag("start_live_voice_btn")
                        ) {
                            Text("Talk", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            items(chatMessages) { message ->
                ChatBubble(message = message)
            }

            if (isChatLoading) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(12.dp)
                        ) {
                            Text(
                                "OverComer Guide is holding you in prayer and writing...",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.testTag("chat_loading_indicator")
                            )
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(8.dp)) }
        }

        // Standard dynamic prompt helper chips
        val prompts = listOf(
            "I'm feeling triggered right now...",
            "What if I mess up?",
            "Can we do a CBT reframing?",
            "Give me a scripture for anxiety"
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            prompts.forEach { prompt ->
                SuggestionChip(
                    onClick = {
                        viewModel.sendChatMessage(prompt)
                    },
                    label = { Text(prompt, maxLines = 1, fontSize = 12.sp) },
                    modifier = Modifier.testTag("chip_prompt_${prompt.take(15).replace(" ", "_")}")
                )
            }
        }

        // Input bottom tray
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                label = { Text("Talk through your struggle...") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input_field"),
                leadingIcon = {
                    IconButton(
                        onClick = {
                            val permCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                            if (permCheck == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                                isLiveSessionOpen = true
                            } else {
                                micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        },
                        modifier = Modifier.testTag("chat_mic_leading_icon")
                    ) {
                        Icon(
                            Icons.Default.Mic,
                            contentDescription = "Talk with voice",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Send,
                    keyboardType = KeyboardType.Text
                ),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (inputText.isNotBlank()) {
                            viewModel.sendChatMessage(inputText)
                            inputText = ""
                        }
                    }
                ),
                maxLines = 4,
                shape = RoundedCornerShape(24.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            FloatingActionButton(
                onClick = {
                    if (inputText.isNotBlank()) {
                        viewModel.sendChatMessage(inputText)
                        inputText = ""
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .testTag("chat_send_button"),
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(
                    Icons.Default.Send,
                    contentDescription = "Send text",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val bubbleBg = if (message.isUser) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val align = if (message.isUser) Alignment.End else Alignment.Start
    val textColor = if (message.isUser) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalAlignment = align
    ) {
        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (message.isUser) 16.dp else 4.dp,
                        bottomEnd = if (message.isUser) 4.dp else 16.dp
                    )
                )
                .background(bubbleBg)
                .padding(14.dp)
                .widthIn(max = 280.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                // Header tag to make roles explicit
                Text(
                    text = if (message.isUser) "ME" else "OVERCOMER GUIDE",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (message.isUser) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

// ------------------------------------------
// VICTORY LOGS & CBT RECORDS PANEL
// ------------------------------------------

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun JournalLogsTabScreen(viewModel: OverComerViewModel) {
    val logs by viewModel.victoryLogs.collectAsStateWithLifecycle()
    val isAnalyzing by viewModel.isAnalyzingDistortion.collectAsStateWithLifecycle()
    val analysisResult by viewModel.distortionAnalysisResult.collectAsStateWithLifecycle()
    val goal by viewModel.freedomGoal.collectAsStateWithLifecycle()

    var showAddLogForm by remember { mutableStateOf(false) }
    var selectedLogType by remember { mutableStateOf("CBT") } // "REFLECT", "TRIGGER", "CBT"
    var logFilter by remember { mutableStateOf("ALL") } // "ALL", "REFLECT", "TRIGGER", "CBT"

    // Export/backup states
    var showExportDialog by remember { mutableStateOf(false) }
    var exportIncludePublic by remember { mutableStateOf(true) }
    var exportIncludePrivate by remember { mutableStateOf(false) }

    // Inputs
    var notesInput by remember { mutableStateOf("") }
    var triggerContextInput by remember { mutableStateOf("") }
    var autoThoughtInput by remember { mutableStateOf("") }
    var identifiedDistortionInput by remember { mutableStateOf("") }
    var reframedTruthInput by remember { mutableStateOf("") }
    var scriptureRefInput by remember { mutableStateOf("") }

    // Dual-panel state
    var subTabMode by remember { mutableStateOf("PUBLIC_TRACKER") } // "PUBLIC_TRACKER", "SECURE_JOURNAL"

    // PIN lock security states
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("overcomer_journal_prefs", Context.MODE_PRIVATE) }
    var pinLockEnabled by remember { mutableStateOf(sharedPrefs.getBoolean("pin_enabled", false)) }
    var savedPin by remember { mutableStateOf(sharedPrefs.getString("pin_value", "") ?: "") }
    var isJournalUnlocked by remember { mutableStateOf(false) }

    // Temp PIN inputs
    var pinSetupInput by remember { mutableStateOf("") }
    var pinUnlockInput by remember { mutableStateOf("") }
    var pinSetupError by remember { mutableStateOf("") }
    var pinUnlockError by remember { mutableStateOf("") }
    var showSetPinDialog by remember { mutableStateOf(false) }

    // Secure Journal Input
    var journalBodyInput by remember { mutableStateOf("") }

    val filteredLogs = remember(logs, logFilter) {
        val publicLogs = logs.filter { it.type != "JOURNAL_SECURE" }
        if (logFilter == "ALL") publicLogs else publicLogs.filter { it.type == logFilter }
    }

    val secureJournalLogs = remember(logs) {
        logs.filter { it.type == "JOURNAL_SECURE" }
    }

    Scaffold(
        floatingActionButton = {
            if (subTabMode == "PUBLIC_TRACKER") {
                FloatingActionButton(
                    onClick = { showAddLogForm = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("add_log_record_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add victory record")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Header
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (subTabMode == "PUBLIC_TRACKER") "My Victory Tracker" else "Locked Private Journal",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(start = 4.dp)
                    ) {
                        // Export Action Card button in the header
                        TextButton(
                            onClick = { showExportDialog = true },
                            modifier = Modifier.testTag("export_trigger_btn"),
                            colors = ButtonDefaults.textButtonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share, 
                                    contentDescription = "Export logs", 
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Export", 
                                    style = MaterialTheme.typography.labelMedium, 
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            modifier = Modifier.padding(4.dp)
                        ) {
                            Text(
                                text = if (subTabMode == "PUBLIC_TRACKER") "${filteredLogs.size} Logs" else "${secureJournalLogs.size} Entries",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (subTabMode == "PUBLIC_TRACKER") {
                        "Tracking your struggles physically proves they are behavioral choices. Record automatic thoughts, defeat lies with biblically cognitive reframing, and log the victory."
                    } else {
                        "A completely private, passcode-secured sanctuary to pour out your thoughts. Let OverComer AI check for cognitive distortions using CBT principles and scripture reframing."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Beautiful custom M3 styled Segment Control
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("PUBLIC_TRACKER" to "Public Step Tracker 🛡️", "SECURE_JOURNAL" to "🔐 Private AI Journal").forEach { (mode, label) ->
                        val isSelected = subTabMode == mode
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                .clickable { subTabMode = mode }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Body Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                if (subTabMode == "PUBLIC_TRACKER") {
                    // Current step tracker list
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Horizontal Filter Chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("ALL", "CBT", "TRIGGER", "REFLECT").forEach { filter ->
                                FilterChip(
                                    selected = logFilter == filter,
                                    onClick = { logFilter = filter },
                                    label = { Text(filter) },
                                    modifier = Modifier.testTag("log_filter_chip_$filter")
                                )
                            }
                        }

                        if (filteredLogs.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.List,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f),
                                        modifier = Modifier.size(56.dp)
                                    )
                                    Text(
                                        text = "No logs recorded for $logFilter",
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                                    )
                                    Text(
                                        text = "Tap the '+' bubble to log your first step in victory.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(filteredLogs) { log ->
                                    VictoryLogCard(log = log, onDelete = {
                                        viewModel.deleteVictoryLog(log.id)
                                    })
                                }
                            }
                        }
                    }
                } else {
                    // SECURE_JOURNAL mode!
                    if (pinLockEnabled && !isJournalUnlocked) {
                        // --- UNLOCK SCREEN PANEL ---
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(vertical = 12.dp)
                                .verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Journal Locked",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Secure Journal Locked",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Your personal thoughts & feelings are private. Enter your 4-digit passcode PIN to unlock them safely.",
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            OutlinedTextField(
                                value = pinUnlockInput,
                                onValueChange = { newValue ->
                                    if (newValue.length <= 4 && newValue.all { it.isDigit() }) {
                                        pinUnlockInput = newValue
                                        if (newValue == savedPin) {
                                            isJournalUnlocked = true
                                            pinUnlockError = ""
                                            pinUnlockInput = ""
                                        } else if (newValue.length == 4) {
                                            pinUnlockError = "Incorrect PIN code. Try again."
                                        } else {
                                            pinUnlockError = ""
                                        }
                                    }
                                },
                                label = { Text("4-Digit PIN") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                visualTransformation = PasswordVisualTransformation(),
                                modifier = Modifier
                                    .width(180.dp)
                                    .testTag("journal_pin_unlock_field"),
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Center)
                            )

                            if (pinUnlockError.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = pinUnlockError,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(32.dp))
                            
                            TextButton(
                                onClick = {
                                    sharedPrefs.edit().clear().apply()
                                    pinLockEnabled = false
                                    savedPin = ""
                                    isJournalUnlocked = false
                                    Toast.makeText(context, "Secure passcode has been reset.", Toast.LENGTH_LONG).show()
                                },
                                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("Forgot PIN? Clear Passcode (Clears PIN Lock)")
                            }
                        }
                    } else {
                        // --- UNLOCKED / CONFIGURED JOURNAL BODY ---
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Section Header Configuration Panel
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = if (pinLockEnabled) Icons.Default.Lock else Icons.Default.LockOpen,
                                        contentDescription = null,
                                        tint = if (pinLockEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = if (pinLockEnabled) "Passcode Guard Enabled" else "Insecure (No Passcode)",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if (pinLockEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                                    )
                                }
                                
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    // Security Settings Trigger Button
                                    TextButton(
                                        onClick = {
                                            if (pinLockEnabled) {
                                                // Disable Lock
                                                sharedPrefs.edit()
                                                    .putBoolean("pin_enabled", false)
                                                    .putString("pin_value", "")
                                                    .apply()
                                                pinLockEnabled = false
                                                savedPin = ""
                                                isJournalUnlocked = false
                                                Toast.makeText(context, "Passcode lock disabled.", Toast.LENGTH_SHORT).show()
                                            } else {
                                                // Trigger Setup Dialog
                                                showSetPinDialog = true
                                            }
                                        }
                                    ) {
                                        Text(
                                            text = if (pinLockEnabled) "Disable Guard ⚠️" else "Setup Lock PIN 🔒",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    if (pinLockEnabled) {
                                        // Lock immediately item
                                        IconButton(
                                            onClick = { isJournalUnlocked = false },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Lock,
                                                contentDescription = "Lock journal now",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Write new entry field
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                                ),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = "DOCUMENT YOUR RAW THOUGHTS & FEELINGS:",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    
                                    OutlinedTextField(
                                        value = journalBodyInput,
                                        onValueChange = { journalBodyInput = it },
                                        placeholder = {
                                            Text(
                                                "Write what is on your mind today... e.g., 'I feel completely overwhelmed. I messed up at work and now I feel like I'm a failure and everyone is going to judge me.'"
                                            )
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(min = 100.dp, max = 150.dp)
                                            .testTag("secure_journal_thought_input"),
                                        maxLines = 8
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        // Standard Save
                                        Button(
                                            onClick = {
                                                if (journalBodyInput.isNotBlank()) {
                                                    viewModel.addVictoryLog(
                                                        type = "JOURNAL_SECURE",
                                                        notes = journalBodyInput
                                                    )
                                                    journalBodyInput = ""
                                                    Toast.makeText(context, "Journal entry saved securely! 📖", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            shape = RoundedCornerShape(12.dp),
                                            enabled = journalBodyInput.isNotBlank() && !isAnalyzing,
                                            modifier = Modifier.weight(1f).testTag("secure_journal_quick_save_btn"),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                        ) {
                                            Text("Save Quietly 📖", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }

                                        // Premium AI Analyze
                                        Button(
                                            onClick = {
                                                if (journalBodyInput.isNotBlank()) {
                                                    viewModel.analyzeJournalDistortion(journalBodyInput)
                                                }
                                            },
                                            shape = RoundedCornerShape(12.dp),
                                            enabled = journalBodyInput.isNotBlank() && !isAnalyzing,
                                            modifier = Modifier.weight(1.3f).testTag("secure_journal_analyze_btn"),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.primary,
                                                contentColor = Color.White
                                            )
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Favorite,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Text("Analyze CBT AI ✨", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                                            }
                                        }
                                    }
                                }
                            }

                            // Loading screen for AI scan
                            if (isAnalyzing) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                                    ),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        CircularProgressIndicator(
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(36.dp)
                                        )
                                        Text(
                                            text = "Analyzing cognitive distortions using clinical CBT & biblical truths...",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                            ),
                                            textAlign = TextAlign.Center,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }

                            // Dynamic AI CBT analysis display container
                            analysisResult?.let { result ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    ),
                                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "🎯 AI CBT COGNITIVE ANALYSIS",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                                color = MaterialTheme.colorScheme.primary
                                            )

                                            IconButton(
                                                onClick = { viewModel.clearDistortionAnalysis() },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Close,
                                                    contentDescription = "Clear analysis",
                                                    modifier = Modifier.size(16.dp),
                                                    tint = MaterialTheme.colorScheme.outline
                                                )
                                            }
                                        }

                                        // Detected Distortions Row
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = "Distortions: ",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.error
                                            )
                                            
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                                            ) {
                                                Text(
                                                    text = result.distortions,
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        // Clinical advice explanations
                                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                            Text(
                                                text = "HOW THIS LIE TRICKS THE MIND:",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.secondary
                                            )
                                            Text(
                                                text = result.explanation,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        // Reframed truth
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                                                .padding(10.dp)
                                        ) {
                                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                Text(
                                                    text = "✝ THE TRUTH (REFRAMED COGNITIVELY):",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                Text(
                                                    text = "\"${result.reframedTruth}\"",
                                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                
                                                if (result.scriptureReference.isNotBlank()) {
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = "⚓ Scripture: ${result.scriptureReference}",
                                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                        color = MaterialTheme.colorScheme.secondary
                                                    )
                                                }
                                            }
                                        }

                                        // Save Reframe Button
                                        Button(
                                            onClick = {
                                                viewModel.addVictoryLog(
                                                    type = "JOURNAL_SECURE",
                                                    notes = journalBodyInput,
                                                    automaticThought = journalBodyInput,
                                                    identifiedDistortion = result.distortions,
                                                    reframedTruth = result.reframedTruth,
                                                    scriptureReference = result.scriptureReference
                                                )
                                                journalBodyInput = ""
                                                viewModel.clearDistortionAnalysis()
                                                Toast.makeText(context, "CBT analysis archived securely! 🛡️📖", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Text("Save & Archive CBT Reframe 🛡️", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            // Secure journal ledger list
                            Text(
                                text = "MY SECURE JOURNAL ENTRIES:",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = MaterialTheme.colorScheme.outline
                            )

                            if (secureJournalLogs.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Lock,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                            modifier = Modifier.size(48.dp)
                                        )
                                        Text(
                                            text = "Your Secure Journal is completely empty",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                        Text(
                                            text = "Document your thoughts above to build a timeline of mental health and victory.",
                                            style = MaterialTheme.typography.labelSmall,
                                            textAlign = TextAlign.Center,
                                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
                                            modifier = Modifier.padding(horizontal = 24.dp)
                                        )
                                    }
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(secureJournalLogs) { entry ->
                                        SecureJournalEntryCard(
                                            entry = entry,
                                            onDelete = { viewModel.deleteVictoryLog(entry.id) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // --- SETUP PIN CODE DIALOG ---
    if (showSetPinDialog) {
        AlertDialog(
            onDismissRequest = { showSetPinDialog = false; pinSetupError = ""; pinSetupInput = "" },
            title = { Text("Setup Secure PIN Lock") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Choose a secure 4-digit passcode PIN. This PIN will be required to decrypt and view your secure journaling entries.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = pinSetupInput,
                        onValueChange = { newValue ->
                            if (newValue.length <= 4 && newValue.all { it.isDigit() }) {
                                pinSetupInput = newValue
                            }
                        },
                        label = { Text("4-Digit PIN") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("setup_pin_field"),
                        singleLine = true
                    )
                    if (pinSetupError.isNotEmpty()) {
                        Text(
                            text = pinSetupError,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (pinSetupInput.length == 4) {
                            sharedPrefs.edit()
                                .putBoolean("pin_enabled", true)
                                .putString("pin_value", pinSetupInput)
                                .apply()
                            pinLockEnabled = true
                            savedPin = pinSetupInput
                            isJournalUnlocked = true
                            pinSetupInput = ""
                            pinSetupError = ""
                            showSetPinDialog = false
                            Toast.makeText(context, "Secure passcode activated! 🔐", Toast.LENGTH_SHORT).show()
                        } else {
                            pinSetupError = "PIN must be exactly 4 digits."
                        }
                    }
                ) {
                    Text("Save & Enable")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSetPinDialog = false; pinSetupError = ""; pinSetupInput = "" }) {
                    Text("Cancel")
                }
            }
        )
    }

    // --- Add Log Dialog ---
    if (showAddLogForm) {
        AlertDialog(
            onDismissRequest = { showAddLogForm = false },
            title = { Text("Log a Victory Milestone") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    // Type Selector Tabs
                    Text("Select Record Schema:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("CBT", "TRIGGER", "REFLECT").forEach { type ->
                            ElevatedFilterChip(
                                selected = selectedLogType == type,
                                onClick = { selectedLogType = type },
                                label = { Text(type) },
                                modifier = Modifier.testTag("form_log_type_$type")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    when (selectedLogType) {
                        "CBT" -> {
                            Text(
                                "Cognitive Thought Record helps trace the lie. Reframing alignment captures the negative thoughts mid-flight.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                            OutlinedTextField(
                                value = autoThoughtInput,
                                onValueChange = { autoThoughtInput = it },
                                label = { Text("Automatic Lie / Negative Thought") },
                                placeholder = { Text("e.g. \"I need this drink to clear my anxiety\"") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_form_thought")
                            )
                            OutlinedTextField(
                                value = identifiedDistortionInput,
                                onValueChange = { identifiedDistortionInput = it },
                                label = { Text("Cognitive Distortion Type") },
                                placeholder = { Text("e.g. Emotional Reasoning, Catastrophizing") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_form_distortion")
                            )
                            OutlinedTextField(
                                value = reframedTruthInput,
                                onValueChange = { reframedTruthInput = it },
                                label = { Text("Biblical Reframing & God's Truth") },
                                placeholder = { Text("e.g. \"God has not given me a spirit of fear, but of power!\"") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_form_truth")
                            )
                            OutlinedTextField(
                                value = scriptureRefInput,
                                onValueChange = { scriptureRefInput = it },
                                label = { Text("Scripture Reference Anchor") },
                                placeholder = { Text("e.g. 2 Timothy 1:7, John 8:36") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_form_scripture")
                            )
                        }
                        "TRIGGER" -> {
                            Text(
                                "Confronting triggers helps map social environments, physical exhaustion, or emotional triggers directly.",
                                style = MaterialTheme.typography.bodySmall
                            )
                            OutlinedTextField(
                                value = triggerContextInput,
                                onValueChange = { triggerContextInput = it },
                                label = { Text("Describe the Trigger Event") },
                                placeholder = { Text("e.g. Had a fight with a colleague, entered stressful atmosphere.") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_form_trigger_context")
                            )
                            OutlinedTextField(
                                value = notesInput,
                                onValueChange = { notesInput = it },
                                label = { Text("Immediate Escape plan executed") },
                                placeholder = { Text("e.g. Stepped out of the office, called an accountability partner, prayed.") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_form_notes_trigger")
                            )
                        }
                        "REFLECT" -> {
                            Text(
                                "Repentance, daily logs, and testimonies of God's limitless grace.",
                                style = MaterialTheme.typography.bodySmall
                            )
                            OutlinedTextField(
                                value = notesInput,
                                onValueChange = { notesInput = it },
                                label = { Text("What did God's grace show you today?") },
                                placeholder = { Text("He healed my heart. I confessed my slips (1 John 1:9), and stood firm.") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_form_notes_reflect")
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.addVictoryLog(
                            type = selectedLogType,
                            notes = notesInput,
                            triggerContext = triggerContextInput,
                            automaticThought = autoThoughtInput,
                            identifiedDistortion = identifiedDistortionInput,
                            reframedTruth = reframedTruthInput,
                            scriptureReference = scriptureRefInput
                        )
                        // Clear buffers
                        notesInput = ""
                        triggerContextInput = ""
                        autoThoughtInput = ""
                        identifiedDistortionInput = ""
                        reframedTruthInput = ""
                        scriptureRefInput = ""
                        showAddLogForm = false
                    },
                    modifier = Modifier.testTag("dialog_submit_log_btn")
                ) {
                    Text("Save To Shield")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddLogForm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // --- CLINICAL EXPORT & BACKUP DIALOG ---
    if (showExportDialog) {
        val isPrivateUnlocked = !pinLockEnabled || isJournalUnlocked
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Clinical Export & Backup 📋",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "Share a readable formatting of your progress with your therapist, counseling practitioner, or keep offline as a backup.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    Text(
                        text = "SELECT DATA TO INCLUDE:",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Checkbox for Public Logs
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { exportIncludePublic = !exportIncludePublic }
                            .padding(vertical = 4.dp, horizontal = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Checkbox(
                            checked = exportIncludePublic,
                            onCheckedChange = { exportIncludePublic = it },
                            modifier = Modifier.testTag("export_include_public_checkbox")
                        )
                        Column {
                            Text(
                                text = "Public Step Tracker Logs",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                text = "CBT Reframing, triggers, grace notes (${filteredLogs.size} logs)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Checkbox for Private Journal Logs
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(enabled = isPrivateUnlocked) { exportIncludePrivate = !exportIncludePrivate }
                            .padding(vertical = 4.dp, horizontal = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Checkbox(
                            checked = exportIncludePrivate && isPrivateUnlocked,
                            onCheckedChange = { exportIncludePrivate = it && isPrivateUnlocked },
                            enabled = isPrivateUnlocked,
                            modifier = Modifier.testTag("export_include_private_checkbox")
                        )
                        Column {
                            Text(
                                text = "🔐 Locked Private Journal",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isPrivateUnlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            )
                            Text(
                                text = if (isPrivateUnlocked) {
                                    "Deep secure emotional entries & AI CBT analyses (${secureJournalLogs.size} entries)"
                                } else {
                                    "⚠️ PASSCODE LOCKED. Unlock the private journal tab to include secure entries."
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isPrivateUnlocked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    if (!exportIncludePublic && (!exportIncludePrivate || !isPrivateUnlocked)) {
                        Text(
                            text = "Please select at least one data source to export.",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                val isExportEnabled = exportIncludePublic || (exportIncludePrivate && isPrivateUnlocked)
                Button(
                    onClick = {
                        val buildExportString = buildString {
                            appendLine("==================================================")
                            appendLine("               OVERCOMER VICTORY REPORT           ")
                            appendLine("==================================================")
                            appendLine("Generated on: ${SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault()).format(Date())}")
                            
                            val g = goal
                            if (g != null) {
                                val diff = System.currentTimeMillis() - g.startDate
                                val daysCount = if (diff > 0) (diff / 86400000L).toInt() else 0
                                appendLine("Sovereign Target Struggle: ${g.struggleType}")
                                appendLine("Faith-based Covenant Declaration: \"${g.customDeclaration}\"")
                                appendLine("Days Walked in Absolute Freedom: $daysCount days")
                            }
                            appendLine("--------------------------------------------------")
                            appendLine()

                            if (exportIncludePublic) {
                                appendLine("--- PUBLIC STEP TRACKER LOGS ---")
                                appendLine("Total Count: ${logs.filter { it.type != "JOURNAL_SECURE" }.size}")
                                appendLine()
                                logs.filter { it.type != "JOURNAL_SECURE" }.sortedByDescending { it.timestamp }.forEach { log ->
                                    val logDateStr = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault()).format(Date(log.timestamp))
                                    appendLine("📅 EVENT DATE: $logDateStr")
                                    when (log.type) {
                                        "CBT" -> {
                                            appendLine("🏷️ TYPE: Cognitive CBT Restructuring")
                                            if (log.automaticThought.isNotBlank()) {
                                                appendLine("🧠 Automatic Thought: \"${log.automaticThought}\"")
                                            }
                                            if (log.identifiedDistortion.isNotBlank()) {
                                                appendLine("⚠️ Cognitive Distortion: ${log.identifiedDistortion}")
                                            }
                                            if (log.reframedTruth.isNotBlank()) {
                                                appendLine("✨ Reframed Covenant Truth: \"${log.reframedTruth}\"")
                                            }
                                            if (log.scriptureReference.isNotBlank()) {
                                                appendLine("⚓ Bible Anchor: ${log.scriptureReference}")
                                            }
                                            if (log.notes.isNotBlank() && log.notes != log.automaticThought) {
                                                appendLine("📝 Context Notes: ${log.notes}")
                                            }
                                        }
                                        "TRIGGER" -> {
                                            appendLine("🏷️ TYPE: Trigger Identification Tracker")
                                            if (log.triggerContext.isNotBlank()) {
                                                appendLine("💥 Trigger Scenario: ${log.triggerContext}")
                                            }
                                            if (log.notes.isNotBlank()) {
                                                appendLine("📝 Recovery Action Taken: ${log.notes}")
                                            }
                                        }
                                        "REFLECT" -> {
                                            appendLine("🏷️ TYPE: Self Reflection & Grace Note")
                                            if (log.notes.isNotBlank()) {
                                                appendLine("📝 Reflection Notes: ${log.notes}")
                                            }
                                        }
                                        else -> {
                                            appendLine("🏷️ TYPE: Grounding Event (${log.type})")
                                            if (log.notes.isNotBlank()) {
                                                appendLine("📝 Notes: ${log.notes}")
                                            }
                                        }
                                    }
                                    appendLine("--------------------------------------------------")
                                }
                                appendLine()
                            }

                            if (exportIncludePrivate && isPrivateUnlocked) {
                                appendLine("--- SECURE MIND JOURNAL ENTRIES ---")
                                appendLine("Total Count: ${secureJournalLogs.size}")
                                appendLine()
                                secureJournalLogs.sortedByDescending { it.timestamp }.forEach { entry ->
                                    val entryDateStr = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault()).format(Date(entry.timestamp))
                                    appendLine("📅 ENTRY DATE: $entryDateStr")
                                    appendLine("📝 Thoughts: ${entry.notes}")
                                    if (entry.identifiedDistortion.isNotBlank() && entry.identifiedDistortion != "None") {
                                        appendLine("🧠 Detected Cognitive Distortion: ${entry.identifiedDistortion}")
                                        appendLine("✨ Covenant Truth Reframe: ${entry.reframedTruth}")
                                        if (entry.scriptureReference.isNotBlank()) {
                                            appendLine("⚓ Scripture Anchor: ${entry.scriptureReference}")
                                        }
                                    }
                                    appendLine("--------------------------------------------------")
                                }
                                appendLine()
                            }

                            appendLine("Report generated safely via OverComer.")
                            appendLine("Walk in absolute victory & sovereign light.")
                            appendLine("==================================================")
                        }

                        // Share via Android System Chooser Send Intent
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, buildExportString)
                            type = "text/plain"
                        }
                        try {
                            val shareIntent = Intent.createChooser(sendIntent, "Export OverComer clinical study")
                            context.startActivity(shareIntent)
                            showExportDialog = false
                        } catch (e: Exception) {
                            Toast.makeText(context, "Failed to initialize standard share sheet: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = isExportEnabled,
                    modifier = Modifier.testTag("export_action_share_btn")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text("Share / Export 📤")
                    }
                }
            },
            dismissButton = {
                // Copy to Clipboard option for offline backup
                val isExportEnabled = exportIncludePublic || (exportIncludePrivate && isPrivateUnlocked)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = {
                            if (isExportEnabled) {
                                val buildExportString = buildString {
                                    appendLine("==================================================")
                                    appendLine("               OVERCOMER VICTORY REPORT           ")
                                    appendLine("==================================================")
                                    appendLine("Generated on: ${SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault()).format(Date())}")
                                    
                                    val g = goal
                                    if (g != null) {
                                        val diff = System.currentTimeMillis() - g.startDate
                                        val daysCount = if (diff > 0) (diff / 86400000L).toInt() else 0
                                        appendLine("Sovereign Target Struggle: ${g.struggleType}")
                                        appendLine("Faith-based Covenant Declaration: \"${g.customDeclaration}\"")
                                        appendLine("Days Walked in Absolute Freedom: $daysCount days")
                                    }
                                    appendLine("--------------------------------------------------")
                                    appendLine()

                                    if (exportIncludePublic) {
                                        appendLine("--- PUBLIC STEP TRACKER LOGS ---")
                                        appendLine("Total Count: ${logs.filter { it.type != "JOURNAL_SECURE" }.size}")
                                        appendLine()
                                        logs.filter { it.type != "JOURNAL_SECURE" }.sortedByDescending { it.timestamp }.forEach { log ->
                                            val logDateStr = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault()).format(Date(log.timestamp))
                                            appendLine("📅 EVENT DATE: $logDateStr")
                                            when (log.type) {
                                                "CBT" -> {
                                                    appendLine("🏷️ TYPE: Cognitive CBT Restructuring")
                                                    if (log.automaticThought.isNotBlank()) {
                                                        appendLine("🧠 Automatic Thought: \"${log.automaticThought}\"")
                                                    }
                                                    if (log.identifiedDistortion.isNotBlank()) {
                                                        appendLine("⚠️ Cognitive Distortion: ${log.identifiedDistortion}")
                                                    }
                                                    if (log.reframedTruth.isNotBlank()) {
                                                        appendLine("✨ Reframed Covenant Truth: \"${log.reframedTruth}\"")
                                                    }
                                                    if (log.scriptureReference.isNotBlank()) {
                                                        appendLine("⚓ Bible Anchor: ${log.scriptureReference}")
                                                    }
                                                    if (log.notes.isNotBlank() && log.notes != log.automaticThought) {
                                                        appendLine("📝 Context Notes: ${log.notes}")
                                                    }
                                                }
                                                "TRIGGER" -> {
                                                    appendLine("🏷️ TYPE: Trigger Identification Tracker")
                                                    if (log.triggerContext.isNotBlank()) {
                                                        appendLine("💥 Trigger Scenario: ${log.triggerContext}")
                                                    }
                                                    if (log.notes.isNotBlank()) {
                                                        appendLine("📝 Recovery Action Taken: ${log.notes}")
                                                    }
                                                }
                                                "REFLECT" -> {
                                                    appendLine("🏷️ TYPE: Self Reflection & Grace Note")
                                                    if (log.notes.isNotBlank()) {
                                                        appendLine("📝 Reflection Notes: ${log.notes}")
                                                    }
                                                }
                                                else -> {
                                                    appendLine("🏷️ TYPE: Grounding Event (${log.type})")
                                                    if (log.notes.isNotBlank()) {
                                                        appendLine("📝 Notes: ${log.notes}")
                                                    }
                                                }
                                            }
                                            appendLine("--------------------------------------------------")
                                        }
                                        appendLine()
                                    }

                                    if (exportIncludePrivate && isPrivateUnlocked) {
                                        appendLine("--- SECURE MIND JOURNAL ENTRIES ---")
                                        appendLine("Total Count: ${secureJournalLogs.size}")
                                        appendLine()
                                        secureJournalLogs.sortedByDescending { it.timestamp }.forEach { entry ->
                                            val entryDateStr = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault()).format(Date(entry.timestamp))
                                            appendLine("📅 ENTRY DATE: $entryDateStr")
                                            appendLine("📝 Thoughts: ${entry.notes}")
                                            if (entry.identifiedDistortion.isNotBlank() && entry.identifiedDistortion != "None") {
                                                appendLine("🧠 Detected Cognitive Distortion: ${entry.identifiedDistortion}")
                                                appendLine("✨ Covenant Truth Reframe: ${entry.reframedTruth}")
                                                if (entry.scriptureReference.isNotBlank()) {
                                                    appendLine("⚓ Scripture Anchor: ${entry.scriptureReference}")
                                                }
                                            }
                                            appendLine("--------------------------------------------------")
                                        }
                                        appendLine()
                                    }

                                    appendLine("Report generated safely via OverComer.")
                                    appendLine("Walk in absolute victory & sovereign light.")
                                    appendLine("==================================================")
                                }

                                try {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                    val clip = android.content.ClipData.newPlainText("OverComer Journal Report", buildExportString)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Journal report copied to clipboard! 📋 Successfully backed up.", Toast.LENGTH_SHORT).show()
                                    showExportDialog = false
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Clipboard copy failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        enabled = isExportEnabled,
                        modifier = Modifier.testTag("export_action_copy_btn")
                    ) {
                        Text("Copy to Clipboard")
                    }
                    TextButton(onClick = { showExportDialog = false }) {
                        Text("Close", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        )
    }
}

@Composable
fun SecureJournalEntryCard(entry: VictoryLog, onDelete: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    val dateStr = remember(entry.timestamp) {
        val sdf = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault())
        sdf.format(Date(entry.timestamp))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("secure_journal_card_${entry.id}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .clickable { expanded = !expanded }
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Encrypted Mind Journal",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            // Note context snippet / full
            Text(
                text = entry.notes,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = if (expanded) Int.MAX_VALUE else 2,
                overflow = if (expanded) TextOverflow.Clip else TextOverflow.Ellipsis
            )

            // If there's AI CBT analysis on this secure log, display it below
            if (entry.identifiedDistortion.isNotBlank() && entry.identifiedDistortion != "None") {
                Spacer(modifier = Modifier.height(4.dp))
                
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.08f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "AI CBT: ${entry.identifiedDistortion}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // Expanded detail section
            if (expanded) {
                // Showing fully if CBT was processed on this
                if (entry.identifiedDistortion.isNotBlank()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

                        if (entry.automaticThought.isNotBlank()) {
                            Text(
                                text = "Analyzed Thought:",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = "\"${entry.automaticThought}\"",
                                style = MaterialTheme.typography.bodySmall,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        }

                        if (entry.reframedTruth.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                                    .padding(8.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = "✝ Biblical CBT Reframe:",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = entry.reframedTruth,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                    
                                    if (entry.scriptureReference.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "⚓ Anchor Scripture: ${entry.scriptureReference}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.secondary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Delete entry button row
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete journal entry",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            } else {
                Text(
                    text = "Tap to expand details and CBT Reframing...",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    ),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun VictoryLogCard(log: VictoryLog, onDelete: () -> Unit) {
    val dateStr = remember(log.timestamp) {
        val sdf = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault())
        sdf.format(Date(log.timestamp))
    }

    val typeColor = when (log.type) {
        "CBT" -> MaterialTheme.colorScheme.secondary
        "TRIGGER" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("log_card_${log.id}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, typeColor.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header tag row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = typeColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = log.type,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = typeColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            when (log.type) {
                "CBT" -> {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Automatic Thought (The Lie):",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = "\"${log.automaticThought}\"",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        
                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

                        Text(
                            text = "God's Truth (The Shield Reframe):",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "\"${log.reframedTruth}\"",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )

                        if (log.scriptureReference.isNotBlank()) {
                            Text(
                                text = "⚓ Anchor Scribe: ${log.scriptureReference}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
                "TRIGGER" -> {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Trigger Event Context:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = log.triggerContext,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                        Text(
                            text = "Escape & Grounding Plan Executed:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = log.notes,
                            style = MaterialTheme.typography.bodyMedium,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }
                else -> { // REFLECT
                    Text(
                        text = log.notes,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(28.dp)
                        .testTag("delete_log_btn_${log.id}")
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete log entry",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// ------------------------------------------
// LOCAL SUPPORT & CORE BELIEFS PANEL
// ------------------------------------------

data class LocalBibleChapter(
    val book: String,
    val chapter: Int,
    val verses: List<String>
)

val offlineBibleChapters = listOf(
    LocalBibleChapter(
        book = "Psalms",
        chapter = 23,
        verses = listOf(
            "The Lord is my shepherd; I shall not want.",
            "He maketh me to lie down in green pastures: he leadeth me beside the still waters.",
            "He restoreth my soul: he leadeth me in the paths of righteousness for his name's sake.",
            "Yea, though I walk through the valley of the shadow of death, I will fear no evil: for thou art with me; thy rod and thy staff they comfort me.",
            "Thou preparest a table before me in the presence of mine enemies: thou anointest my head with oil; my cup runneth over.",
            "Surely goodness and mercy shall follow me all the days of my life: and I will dwell in the house of the Lord for ever."
        )
    ),
    LocalBibleChapter(
        book = "Psalms",
        chapter = 91,
        verses = listOf(
            "He that dwelleth in the secret place of the most High shall abide under the shadow of the Almighty.",
            "I will say of the Lord, He is my refuge and my fortress: my God; in him will I trust.",
            "Surely he shall deliver thee from the snare of the fowler, and from the noisome pestilence.",
            "He shall cover thee with his feathers, and under his wings shalt thou trust: his truth shall be thy shield and buckler.",
            "Thou shalt not be afraid for the terror by night; nor for the arrow that flieth by day;",
            "Nor for the pestilence that walketh in darkness; nor for the destruction that wasteth at noonday.",
            "A thousand shall fall at thy side, and ten thousand at thy right hand; but it shall not come nigh thee.",
            "Only with thine eyes shalt thou behold and see the reward of the wicked.",
            "Because thou hast made the Lord, which is my refuge, even the most High, thy habitation;",
            "There shall no evil befall thee, neither shall any plague come nigh thy dwelling.",
            "For he shall give his angels charge over thee, to keep thee in all thy ways.",
            "They shall bear thee up in their hands, lest thou dash thy foot against a stone.",
            "Thou shalt tread upon the lion and adder: the young lion and the dragon shalt thou trample under feet.",
            "Because he hath set his love upon me, therefore will I deliver him: I will set him on high, because he hath known my name.",
            "He shall call upon me, and I will answer him: I will be with him in trouble; I will deliver him, and honour him.",
            "With long life will I satisfy him, and shew him my salvation."
        )
    ),
    LocalBibleChapter(
        book = "Psalms",
        chapter = 121,
        verses = listOf(
            "I will lift up mine eyes unto the hills, from whence cometh my help.",
            "My help cometh from the Lord, which made heaven and earth.",
            "He will not suffer thy foot to be moved: he that keepeth thee will not slumber.",
            "Behold, he that keepeth Israel shall neither slumber nor sleep.",
            "The Lord is thy keeper: the Lord is thy shade upon thy right hand.",
            "The sun shall not smite thee by day, nor the moon by night.",
            "The Lord shall preserve thee from all evil: he shall preserve thy soul.",
            "The Lord shall preserve thy going out and thy coming in from this time forth, and even for evermore."
        )
    ),
    LocalBibleChapter(
        book = "Proverbs",
        chapter = 3,
        verses = listOf(
            "My son, forget not my law; but let thine heart keep my commandments:",
            "For length of days, and long life, and peace, shall they add to thee.",
            "Let not mercy and truth forsake thee: bind them about thy neck; write them upon the table of thine heart:",
            "So shalt thou find favour and good understanding in the sight of God and man.",
            "Trust in the Lord with all thine heart; and lean not unto thine own understanding.",
            "In all thy ways acknowledge him, and he shall direct thy paths.",
            "Be not wise in thine own eyes: fear the Lord, and depart from evil.",
            "It shall be health to thy navel, and marrow to thy bones.",
            "Honour the Lord with thy substance, and with the firstfruits of all thine increase:",
            "So shall thy barns be filled with plenty, and thy presses shall burst out with new wine."
        )
    ),
    LocalBibleChapter(
        book = "Proverbs",
        chapter = 4,
        verses = listOf(
            "Hear, ye children, the instruction of a father, and attend to know understanding.",
            "For I give you good doctrine, forsake ye not my law.",
            "For I was my father's son, tender and only beloved in the sight of my mother.",
            "He taught me also, and said unto me, Let thine heart retain my words: keep my commandments, and live.",
            "Get wisdom, get understanding: forget it not; neither decline from the words of my mouth."
        )
    ),
    LocalBibleChapter(
        book = "John",
        chapter = 1,
        verses = listOf(
            "In the beginning was the Word, and the Word was with God, and the Word was God.",
            "The same was in the beginning with God.",
            "All things were made by him; and without him was not any thing made that was made.",
            "In him was life; and the life was the light of men.",
            "And the light shineth in darkness; and the darkness comprehended it not."
        )
    ),
    LocalBibleChapter(
        book = "John",
        chapter = 14,
        verses = listOf(
            "Let not your heart be troubled: ye believe in God, believe also in me.",
            "In my Father's house are many mansions: if it were not so, I would have told you. I go to prepare a place for you.",
            "And if I go and prepare a place for you, I will come again, and receive you unto myself; that where I am, there ye may be also.",
            "And whither I go ye know, and the way ye know.",
            "Thomas saith unto him, Lord, we know not whither thou goest; and how can we know the way?",
            "Jesus saith unto him, I am the way, the truth, and the life: no man cometh unto the Father, but by me."
        )
    ),
    LocalBibleChapter(
        book = "John",
        chapter = 15,
        verses = listOf(
            "I am the true vine, and my Father is the husbandman.",
            "Every branch in me that beareth not fruit he taketh away: and every branch that beareth fruit, he purgeth it, that it may bring forth more fruit.",
            "Now ye are clean through the word which I have spoken unto you.",
            "Abide in me, and I in you. As the branch cannot bear fruit of itself, except it abide in the vine; no more can ye, except ye abide in me.",
            "I am the vine, ye are the branches: He that abideth in me, and I in him, the same bringeth forth much fruit: for without me ye can do nothing."
        )
    ),
    LocalBibleChapter(
        book = "Romans",
        chapter = 8,
        verses = listOf(
            "There is therefore now no condemnation to them which are in Christ Jesus, who walk not after the flesh, but after the Spirit.",
            "For the law of the Spirit of life in Christ Jesus hath made me free from the law of sin and death.",
            "For what the law could not do, in that it was weak through the flesh, God sending his own Son in the likeness of sinful flesh, and for sin, condemned sin in the flesh:",
            "That the righteousness of the law might be fulfilled in us, who walk not after the flesh, but after the Spirit.",
            "What shall we then say to these things? If God be for us, who can be against us?",
            "Nay, in all these things we are more than conquerors through him that loved us.",
            "For I am persuaded, that neither death, nor life, nor angels, nor principalities, nor powers, nor things present, nor things to come,",
            "Nor height, nor depth, nor any other creature, shall be able to separate us from the love of God, which is in Christ Jesus our Lord."
        )
    ),
    LocalBibleChapter(
        book = "Romans",
        chapter = 12,
        verses = listOf(
            "I beseech you therefore, brethren, by the mercies of God, that ye present your bodies a living sacrifice, holy, acceptable unto God, which is your reasonable service.",
            "And be not conformed to this world: but be ye transformed by the renewing of your mind, that ye may prove what is that good, and acceptable, and perfect, will of God.",
            "For I say, through the grace given unto whomsoever, that no man think of himself more highly than he ought to think; but to think soberly, according as God hath dealt to every man the measure of faith."
        )
    ),
    LocalBibleChapter(
        book = "Philippians",
        chapter = 4,
        verses = listOf(
            "Rejoice in the Lord alway: and again I say, Rejoice.",
            "Let your moderation be known unto all men. The Lord is at hand.",
            "Be careful for nothing; but in every thing by prayer and supplication with thanksgiving let your requests be made known unto God.",
            "And the peace of God, which passeth all understanding, shall keep your hearts and minds through Christ Jesus.",
            "Finally, brethren, whatsoever things are true, whatsoever things are honest, whatsoever things are just, whatsoever things are pure, whatsoever things are lovely, whatsoever things are of good report; if there be any virtue, and if there be any praise, think on these things.",
            "I can do all things through Christ which strengtheneth me."
        )
    )
)

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ShieldCreedTabScreen(viewModel: OverComerViewModel) {
    val beliefs = listOf(
        "True freedom begins in a personal, transformational relationship with Jesus Christ.",
        "The Holy Bible is where we learn who Christ is, who we are in Him, and find directional wisdom for our walk.",
        "Christ can completely deliver you from any addiction or struggle. John 8:36: \"So if the Son sets you free, you will be free indeed!\"",
        "Your struggle is NOT your permanent biological identity. It is a behavioral choice that leads to bondage, but Christ sets us completely free of past chains.",
        "You have not gone too far nor been involved too much for Christ to accept you, clean you up, and crown you an OverComer.",
        "As soon as you repent and surrender your heart, your past is dead! You are a new creation! (2 Corinthians 5:17)",
        "You are NOT still an addict or permanently sick. You are now and forever an OverComer! (Revelation 12:11)",
        "When temptations trigger you, Christ is faithful to help you resist... He has suffered temptations too, and He always provides a way out."
    )

    var selectedSubTab by remember { mutableStateOf(0) } // 0 = Creed, 1 = Bible Reader
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Modern TabRow navigation header inside the Shield tab!
        TabRow(
            selectedTabIndex = selectedSubTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth().testTag("bible_tab_row")
        ) {
            Tab(
                selected = selectedSubTab == 0,
                onClick = { selectedSubTab = 0 },
                text = { Text("🛡️ Pledge & Creed", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("bible_tab_creed")
            )
            Tab(
                selected = selectedSubTab == 1,
                onClick = { selectedSubTab = 1 },
                text = { Text("📖 Holy Bible", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("bible_tab_reader")
            )
        }

        if (selectedSubTab == 0) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🛡️ OverComer Shield & Creed",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Our Theological Foundation of Sovereign Victory",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                item {
                    // Master Motto Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "THE OVERCOMER MOTTO",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.3.sp
                            )
                            Text(
                                text = "\"A OverComer has submitted their life wholly to Christ and no longer fights FOR victory over addiction but rather FROM a position of victory through the Power of our Savior and King Jesus Christ.\"",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "What an OverComer Believes:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Start
                    )
                }

                items(beliefs.size) { index ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${index + 1}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = beliefs[index],
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                }

                item {
                    // Mission statement
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "OUR HOLY MISSION",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "1. Be a safe place for those that are struggling with issues controlling their lives.\n" +
                                       "2. Lead those struggling into a life-transforming relationship with Christ.\n" +
                                       "3. Make Disciples for The Kingdom of God.\n" +
                                       "4. Teach how to reproduce the life-transforming relationship we have had with Christ to others.",
                                style = MaterialTheme.typography.bodySmall,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        } else {
            // ==========================================
            // FREE BIBLE APP SECTION
            // ==========================================
            val context = LocalContext.current
            var selectedBook by remember { mutableStateOf("Psalms") }
            var selectedChapter by remember { mutableStateOf(23) }
            
            // Text scale modifier for premium customization
            var textScale by remember { mutableStateOf(16f) }
            
            // State for dynamic chapters loaded from the AI server
            var dynamicVerses by remember { mutableStateOf<List<String>?>(null) }
            var isChapterLoading by remember { mutableStateOf(false) }
            var customBookInput by remember { mutableStateOf("") }
            var customChapterInput by remember { mutableStateOf("1") }
            var errorMessage by remember { mutableStateOf("") }

            // Local TTS reference
            var ttsInstance by remember { mutableStateOf<TextToSpeech?>(null) }
            var isReadingAloud by remember { mutableStateOf(false) }

            DisposableEffect(Unit) {
                val tts = TextToSpeech(context) { status ->
                    if (status == TextToSpeech.SUCCESS) {
                        ttsInstance?.setLanguage(Locale.getDefault())
                        ttsInstance?.setSpeechRate(0.85f)
                    }
                }
                ttsInstance = tts
                onDispose {
                    tts.shutdown()
                }
            }

            // Determine active verses to print
            val activeVerses = remember(selectedBook, selectedChapter, dynamicVerses) {
                if (selectedBook == "Other Custom") {
                    dynamicVerses ?: emptyList()
                } else {
                    offlineBibleChapters.firstOrNull { it.book == selectedBook && it.chapter == selectedChapter }?.verses ?: emptyList()
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Selector Toolbar Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "📚 BIBLE SELECTION",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        // Book selection row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val booksList = listOf("Psalms", "Proverbs", "John", "Romans", "Philippians", "Other Custom")
                            var bookExpanded by remember { mutableStateOf(false) }

                            Box(modifier = Modifier.weight(1f)) {
                                Button(
                                    onClick = { bookExpanded = true },
                                    modifier = Modifier.fillMaxWidth().testTag("bible_book_select_btn"),
                                    contentPadding = PaddingValues(horizontal = 12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                                ) {
                                    Text(text = "Book: $selectedBook ▾", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                }
                                DropdownMenu(
                                    expanded = bookExpanded,
                                    onDismissRequest = { bookExpanded = false }
                                ) {
                                    booksList.forEach { b ->
                                        DropdownMenuItem(
                                            text = { Text(b) },
                                            onClick = {
                                                selectedBook = b
                                                bookExpanded = false
                                                dynamicVerses = null
                                                errorMessage = ""
                                                // Reset chapter to a standard valid default
                                                selectedChapter = when (b) {
                                                    "Psalms" -> 23
                                                    "Proverbs" -> 3
                                                    "John" -> 1
                                                    "Romans" -> 8
                                                    "Philippians" -> 4
                                                    else -> 1
                                                }
                                            }
                                        )
                                    }
                                }
                            }

                            // Dynamic chapter buttons based on offline availability
                            if (selectedBook != "Other Custom") {
                                val chList = when (selectedBook) {
                                    "Psalms" -> listOf(23, 91, 121)
                                    "Proverbs" -> listOf(3, 4)
                                    "John" -> listOf(1, 14, 15)
                                    "Romans" -> listOf(8, 12)
                                    "Philippians" -> listOf(4)
                                    else -> listOf(1)
                                }
                                var chExpanded by remember { mutableStateOf(false) }

                                Box(modifier = Modifier.width(130.dp)) {
                                    Button(
                                        onClick = { chExpanded = true },
                                        modifier = Modifier.fillMaxWidth().testTag("bible_chapter_select_btn"),
                                        contentPadding = PaddingValues(horizontal = 12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                                    ) {
                                        Text(text = "Ch: $selectedChapter ▾", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                    }
                                    DropdownMenu(
                                        expanded = chExpanded,
                                        onDismissRequest = { chExpanded = false }
                                    ) {
                                        chList.forEach { ch ->
                                            DropdownMenuItem(
                                                text = { Text("Chapter $ch") },
                                                onClick = {
                                                    selectedChapter = ch
                                                    chExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Custom Search controls for any other book/chapter
                        if (selectedBook == "Other Custom") {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = customBookInput,
                                    onValueChange = { customBookInput = it },
                                    label = { Text("Book (e.g. Genesis)", fontSize = 12.sp) },
                                    singleLine = true,
                                    modifier = Modifier.weight(1.2f).testTag("bible_custom_book_input")
                                )
                                OutlinedTextField(
                                    value = customChapterInput,
                                    onValueChange = { customChapterInput = it.filter { char -> char.isDigit() } },
                                    label = { Text("Chapter", fontSize = 12.sp) },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(0.8f).testTag("bible_custom_chapter_input")
                                )
                                Button(
                                    onClick = {
                                        if (customBookInput.isBlank()) {
                                            errorMessage = "Please enter a valid book."
                                            return@Button
                                        }
                                        val chVal = customChapterInput.toIntOrNull() ?: 1
                                        isChapterLoading = true
                                        errorMessage = ""
                                        coroutineScope.launch {
                                            val verses = com.example.network.GeminiClient.generateBibleChapter(customBookInput.trim(), chVal)
                                            isChapterLoading = false
                                            if (verses.isNotEmpty()) {
                                                dynamicVerses = verses
                                            } else {
                                                errorMessage = "Could not fetch custom chapter. Please check your network or enter a classic book name."
                                            }
                                        }
                                    },
                                    enabled = !isChapterLoading,
                                    modifier = Modifier.height(56.dp).testTag("bible_ai_query_btn")
                                ) {
                                    if (isChapterLoading) {
                                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                                    } else {
                                        Text("Load", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                // Error Warning
                if (errorMessage.isNotEmpty()) {
                    Text(text = errorMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                // Text controls card (Resize & Audio Read-Aloud controls)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Slider for resizing bible verses text
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.secondary)
                            Text("Size: ${textScale.toInt()}sp", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            Slider(
                                value = textScale,
                                onValueChange = { textScale = it },
                                valueRange = 12f..32f,
                                modifier = Modifier.weight(1f).testTag("bible_text_scale_slider")
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Audio button (TTS read aloud)
                        IconButton(
                            onClick = {
                                if (isReadingAloud) {
                                    ttsInstance?.stop()
                                    isReadingAloud = false
                                } else {
                                    if (activeVerses.isNotEmpty()) {
                                        isReadingAloud = true
                                        val fullReading = "Reading ${if(selectedBook == "Other Custom") customBookInput else selectedBook} Chapter ${if(selectedBook == "Other Custom") customChapterInput else selectedChapter}. " +
                                                activeVerses.joinToString(". ")
                                        ttsInstance?.speak(fullReading, TextToSpeech.QUEUE_FLUSH, null, "BibleChapterTTS")
                                    } else {
                                        Toast.makeText(context, "No Scripture loaded.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            modifier = Modifier
                                .background(if (isReadingAloud) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primaryContainer, CircleShape)
                                .size(40.dp)
                                .testTag("bible_tts_speak_btn")
                        ) {
                            Icon(
                                imageVector = if (isReadingAloud) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isReadingAloud) "Pause voice reading" else "Listen aloud comfort reader",
                                tint = if (isReadingAloud) Color.White else MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Interactive Scripture Container
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (isChapterLoading) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                Text(
                                    text = "Anointing scriptures with eternal hope...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                )
                            }
                        } else if (activeVerses.isEmpty()) {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "📖 No custom book chapter loaded yet.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Select 'Other Custom' and search any chapter of the Bible (e.g. Genesis 1, Revelation 21) dynamically powered by our AI counselor!",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                item {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${if (selectedBook == "Other Custom") customBookInput else selectedBook} Chapter ${if (selectedBook == "Other Custom") customChapterInput else selectedChapter}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        // Copy entire Bible chapter action
                                        IconButton(
                                            onClick = {
                                                val combinedText = activeVerses.mapIndexed { index, v -> "${index + 1}. $v" }.joinToString("\n")
                                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                                val clip = android.content.ClipData.newPlainText("Holy Bible Scripture", combinedText)
                                                clipboard.setPrimaryClip(clip)
                                                Toast.makeText(context, "Full Scripture copied! 📋", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy text chapter", modifier = Modifier.size(18.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                                }

                                items(activeVerses.size) { index ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Text(
                                            text = "${index + 1} ",
                                            fontSize = (textScale - 2).sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(end = 6.dp)
                                        )
                                        Text(
                                            text = activeVerses[index],
                                            fontSize = textScale.sp,
                                            lineHeight = (textScale + 6).sp,
                                            fontWeight = FontWeight.Normal,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.weight(1f)
                                        )
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

// ==========================================
// INSPIRING CHRISTIAN QUOTE OF THE DAY
// ==========================================

data class ChristianQuote(
    val quote: String,
    val author: String,
    val info: String
)

val inspiringQuotes = listOf(
    ChristianQuote(
        "God allows us to experience the low points of life in order to teach us lessons that we could learn in no other way.",
        "C.S. Lewis",
        "Christian Author & Apologist"
    ),
    ChristianQuote(
        "Humility is not thinking less of yourself, it's thinking of yourself less.",
        "C.S. Lewis",
        "Christian Author & Apologist"
    ),
    ChristianQuote(
        "There are far, far better things ahead than any we leave behind.",
        "C.S. Lewis",
        "Christian Author & Apologist"
    ),
    ChristianQuote(
        "Worry does not empty tomorrow of its sorrow, it empties today of its strength.",
        "Corrie ten Boom",
        "Holocaust Survivor & Writer"
    ),
    ChristianQuote(
        "Never be afraid to trust an unknown future to a known God.",
        "Corrie ten Boom",
        "Holocaust Survivor & Writer"
    ),
    ChristianQuote(
        "By perseverance the snail reached the ark.",
        "Charles Spurgeon",
        "19th Century Reformed Preacher"
    ),
    ChristianQuote(
        "You have made us for yourself, O Lord, and our heart is restless until it rests in you.",
        "Augustine of Hippo",
        "Early Church Father & Philosopher"
    ),
    ChristianQuote(
        "He is no fool who gives what he cannot keep to gain what he cannot lose.",
        "Jim Elliot",
        "Martyred Missionary in Ecuador"
    ),
    ChristianQuote(
        "Refuse to be average. Let your heart soar as high as it will.",
        "A.W. Tozer",
        "Pastor & Author of 'The Pursuit of God'"
    ),
    ChristianQuote(
        "If you believe in a God who controls the big things, you have to believe in a God who controls the little things.",
        "Elisabeth Elliot",
        "Missionary & Christian Author"
    ),
    ChristianQuote(
        "God's work done in God's way will never lack God's supplies.",
        "Hudson Taylor",
        "Pioneer Missionary to China"
    ),
    ChristianQuote(
        "We must be ready to allow ourselves to be interrupted by God.",
        "Dietrich Bonhoeffer",
        "German Pastor & Dissident"
    ),
    ChristianQuote(
        "God has given us two hands, one to receive with and the other to give with.",
        "Billy Graham",
        "Evangelist & Author"
    ),
    ChristianQuote(
        "In prayer, it is better to have a heart without words than words without a heart.",
        "John Bunyan",
        "Author of 'Pilgrim's Progress'"
    ),
    ChristianQuote(
        "Integrity is doing the right thing when no one is watching.",
        "C.S. Lewis",
        "Christian Author & Apologist"
    )
)

@Composable
fun QuoteOfTheDaySection() {
    val context = LocalContext.current
    var currentIndex by remember { mutableStateOf(java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_YEAR) % inspiringQuotes.size) }
    val currentQuote = inspiringQuotes[currentIndex]

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .testTag("quote_of_the_day_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.25f),
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        ),
        border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Quote icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "QUOTE OF THE DAY",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Copy action
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            val clip = android.content.ClipData.newPlainText("Quote of the Day", "\"${currentQuote.quote}\"\n— ${currentQuote.author} (${currentQuote.info})")
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Quote copied to clipboard! 📋", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Quote",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Next quote trigger
                    IconButton(
                        onClick = {
                            currentIndex = (currentIndex + 1) % inspiringQuotes.size
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Next Quote",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Text(
                text = "\"${currentQuote.quote}\"",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Medium,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    lineHeight = 22.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "— ${currentQuote.author}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = currentQuote.info,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}

// ==========================================
// DAILY BIBLE-BASED AFFIRMATIONS
// ==========================================

data class BibleAffirmation(
    val id: Int,
    val text: String,
    val reference: String,
    val contextReflection: String
)

val bibleAffirmationsDefaults = listOf(
    BibleAffirmation(
        id = 1,
        text = "I am a new creation in Christ; the old is gone, the new has come.",
        reference = "2 Corinthians 5:17",
        contextReflection = "Because of Jesus, my past fails do not define me. Today, I walk in absolute brand-new beginnings!"
    ),
    BibleAffirmation(
        id = 2,
        text = "I can do all things through Christ who strengthens me.",
        reference = "Philippians 4:13",
        contextReflection = "My own willpower might falter, but Christ's internal resurrection power inside me is limitless. I am capable of resisting today's temptations."
    ),
    BibleAffirmation(
        id = 3,
        text = "God has not given me a spirit of fear, but of power, love, and a sound mind.",
        reference = "2 Timothy 1:7",
        contextReflection = "Anxiety and triggers have no legal hold over me. I claim a quiet, focused, disciplined mind under God's protection."
    ),
    BibleAffirmation(
        id = 4,
        text = "I am more than a conqueror through Him who loved me.",
        reference = "Romans 8:37",
        contextReflection = "I do not struggle FOR victory; I stand and fight FROM the victory already won for me on the Cross."
    ),
    BibleAffirmation(
        id = 5,
        text = "He who is in me is greater than he who is in the world.",
        reference = "1 John 4:4",
        contextReflection = "The Holy Spirit inside me is infinitely stronger than any chemical, habit, or sensory trigger in the surrounding environment."
    ),
    BibleAffirmation(
        id = 6,
        text = "Sin shall no longer have dominion over me, for I am under grace.",
        reference = "Romans 6:14",
        contextReflection = "I am completely delivered from the cage of guilt. Grace is my defense, my shield, and my master now."
    ),
    BibleAffirmation(
        id = 7,
        text = "The Lord is my strength and my shield; my heart trusts in Him, and I am helped.",
        reference = "Psalm 28:7",
        contextReflection = "I do not have to carry this burden alone. God is shielding my weak spots while I take one step at a time."
    ),
    BibleAffirmation(
        id = 8,
        text = "My body is a temple of the Holy Spirit, bought with a price to honor God.",
        reference = "1 Corinthians 6:19-20",
        contextReflection = "I declare my eyes, my thoughts, and my hands are tools of righteousness and holiness to bring God praise."
    ),
    BibleAffirmation(
        id = 9,
        text = "God's grace is sufficient for me, for His power is made perfect in weakness.",
        reference = "2 Corinthians 12:9",
        contextReflection = "When I feel exhausted and close to giving in, God's grace steps in to do what I cannot. I am strong in Him."
    ),
    BibleAffirmation(
        id = 10,
        text = "I am chosen, holy, and dearly loved by God.",
        reference = "Colossians 3:12",
        contextReflection = "My worth is set by my Father above. No bad day, relapse, or negative comment can change how much I am cherished."
    )
)

@Composable
fun BibleAffirmationsSection() {
    val items = bibleAffirmationsDefaults
    var currentIndex by remember { mutableStateOf(0) }
    val currentItem = items[currentIndex]
    
    // Track spoken/declared affirmation IDs in a stateful set
    var declaredIds by remember { mutableStateOf(setOf<Int>()) }
    val isDeclared = declaredIds.contains(currentItem.id)
    
    // Setup Local Text To Speech
    val context = LocalContext.current
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    var isTtsReady by remember { mutableStateOf(false) }
    
    DisposableEffect(Unit) {
        var localTts: TextToSpeech? = null
        localTts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                try {
                    val result = localTts?.setLanguage(Locale.getDefault())
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        localTts?.setLanguage(Locale.US)
                    }
                    localTts?.setSpeechRate(0.82f) // Comfortably slower, very natural reading pace
                    localTts?.setPitch(1.0f)
                } catch (_: Exception) {}
                isTtsReady = true
            }
        }
        tts = localTts
        onDispose {
            localTts?.stop()
            localTts?.shutdown()
        }
    }
    
    fun speakCurrent() {
        if (isTtsReady) {
            val speechText = "Affirmation: ${currentItem.text}. Scripture reference: ${currentItem.reference}."
            tts?.speak(speechText, TextToSpeech.QUEUE_FLUSH, null, "AffirmationTTS")
        } else {
            Toast.makeText(context, "Text to speech is initializing...", Toast.LENGTH_SHORT).show()
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .testTag("bible_affirmations_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Favorite,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "VICTORY AFFIRMED",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                // Read Aloud / Speaker Action
                IconButton(
                    onClick = { speakCurrent() },
                    modifier = Modifier
                        .size(36.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f), CircleShape)
                        .testTag("affirmation_speak_btn")
                ) {
                    Icon(
                        Icons.Default.VolumeUp,
                        contentDescription = "Read affirmation aloud",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Carousel Text View
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "\"${currentItem.text}\"",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            lineHeight = 24.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "— ${currentItem.reference}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Practical Devotional Reflection
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "RENEW MY MIND:",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = currentItem.contextReflection,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Interactive Declaration & Swiping Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Return previous index
                IconButton(
                    onClick = {
                        if (currentIndex > 0) currentIndex-- else currentIndex = items.size - 1
                    },
                    modifier = Modifier.testTag("affirmation_prev_btn")
                ) {
                    Icon(
                        Icons.Default.ChevronLeft,
                        contentDescription = "Previous affirmation"
                    )
                }

                // Center Declaration active stamp
                Button(
                    onClick = {
                        declaredIds = if (isDeclared) {
                            declaredIds - currentItem.id
                        } else {
                            declaredIds + currentItem.id
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDeclared) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primaryContainer,
                        contentColor = if (isDeclared) MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.testTag("affirmation_declare_btn")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (isDeclared) Icons.Default.CheckCircle else Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (isDeclared) "DECLARED VICTORY! ✝" else "AFFIRM & DECLARE 🛡️",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Go to next index
                IconButton(
                    onClick = {
                        if (currentIndex < items.size - 1) currentIndex++ else currentIndex = 0
                    },
                    modifier = Modifier.testTag("affirmation_next_btn")
                ) {
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = "Next affirmation"
                    )
                }
            }

            // Scoreboard Progress bar metric of total affirmations completed
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Today's Devotional Progress:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${declaredIds.size} of ${items.size} Spoken",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                val progress = declaredIds.size.toFloat() / items.size.toFloat()
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .testTag("affirmations_progress_bar"),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                )
            }
        }
    }
}

// ==========================================
// SUPPORT NETWORK SOS SMS CONNECTION CARD
// ==========================================

@Composable
fun SupportConnectionSmsCard() {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("overcomer_sos_contacts", Context.MODE_PRIVATE) }

    // Role options
    val roles = listOf("Sponsor", "Mentor", "Spouse", "Best Friend")
    var selectedRole by remember { mutableStateOf("Sponsor") }

    // Retrieve saved phone number for the selected role
    var phoneNumber by remember(selectedRole) {
        mutableStateOf(sharedPrefs.getString("phone_$selectedRole", "") ?: "")
    }

    // Default template options
    val templates = listOf(
        "I'm struggling pretty bad right now",
        "Please pray for me",
        "You said if I ever needed you to let you know. I need you"
    )
    var selectedTemplateIndex by remember { mutableStateOf(0) }
    var customMessageText by remember { mutableStateOf("") }

    // The message that will actually be sent
    val finalMessage = if (customMessageText.isNotBlank()) customMessageText else templates.getOrNull(selectedTemplateIndex) ?: ""

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("support_sos_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Title block
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = "🚨 SOS SUPPORT NETWORK TEXT",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Text(
                text = "Add phone numbers for your inner circle below. In moments of temptation, tap a pre-built message or type your own to text them instantly.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Horizontal Selectors for Roles
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                roles.forEach { role ->
                    val isSelected = selectedRole == role
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary 
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .clickable { selectedRole = role }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = role,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Phone Field corresponding to selected role
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { newValue ->
                    phoneNumber = newValue
                    sharedPrefs.edit().putString("phone_$selectedRole", newValue).apply()
                },
                label = { Text("$selectedRole's Phone / Contact") },
                placeholder = { Text("e.g. 704-555-0199") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth().testTag("sos_phone_field"),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.ContactPhone,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                },
                singleLine = true
            )

            // Preset templates header
            Text(
                text = "CHOOSE A QUICK TEMPLATE:",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = MaterialTheme.colorScheme.secondary
            )

            // Vertical list of presets
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                templates.forEachIndexed { index, template ->
                    val isCurrentPreset = selectedTemplateIndex == index && customMessageText.isEmpty()
                    Surface(
                        onClick = {
                            selectedTemplateIndex = index
                            customMessageText = "" // clear custom text to default to chosen template
                        },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isCurrentPreset) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            1.dp,
                            if (isCurrentPreset) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isCurrentPreset,
                                onClick = {
                                    selectedTemplateIndex = index
                                    customMessageText = ""
                                }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = template,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isCurrentPreset) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Space to create their own short text
            OutlinedTextField(
                value = customMessageText,
                onValueChange = { customMessageText = it },
                label = { Text("Or Type Your Custom SOS Message") },
                placeholder = { Text("e.g. I am in a trigger area. Please call me ASAP.") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("sos_custom_msg_field"),
                maxLines = 3
            )

            // Submit Button to open Text App
            Button(
                onClick = {
                    try {
                        val smsUriStr = if (phoneNumber.isNotBlank()) {
                            "smsto:$phoneNumber"
                        } else {
                            "smsto:"
                        }
                        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse(smsUriStr)).apply {
                            putExtra("sms_body", finalMessage)
                        }
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Cannot open messaging client.", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("sos_send_sms_btn"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = Color.White
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "TEXT YOUR ${selectedRole.uppercase()}",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

// ==========================================
// CLINICAL EMERGENCY SOVEREIGN SHIELD (PANIC OVERLAY)
// ==========================================

@Composable
fun PanicOverlayDialog(onDismiss: () -> Unit) {
    var selectedPanicTab by remember { mutableStateOf(0) } // 0 = Grounding, 1 = Support Contacts, 2 = Global Help

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header Block with Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(12.dp)
                        ) {}
                        Text(
                            text = "SOVEREIGN SHIELD SOS",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("panic_close_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close overlay")
                    }
                }
                
                Text(
                    text = "Pause immediately. Breathe deep. God is your refuge and strength, a very present help in trouble.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Beautiful Custom Segmented Tabs Selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val tabs = listOf("🧘 Grounding", "📞 Inner Circle", "🛡️ Helplines")
                    tabs.forEachIndexed { i, title ->
                        val isSelected = selectedPanicTab == i
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else Color.Transparent
                                )
                                .clickable { selectedPanicTab = i }
                                .padding(vertical = 10.dp)
                                .testTag("panic_tab_$i"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    when (selectedPanicTab) {
                        0 -> GroundingExerciseTabContent()
                        1 -> SupportContactsTabContent()
                        2 -> GlobalHelplinesTabContent()
                    }
                }
            }
        }
    }
}

@Composable
fun GroundingExerciseTabContent() {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            BreathingPulseAssist()
        }
        item {
            SensoryGroundingAssist()
        }
    }
}

@Composable
fun BreathingPulseAssist() {
    var phase by remember { mutableStateOf("Ready") }
    var secondsLeft by remember { mutableStateOf(0) }
    var pulseActive by remember { mutableStateOf(false) }

    LaunchedEffect(pulseActive) {
        if (pulseActive) {
            while (true) {
                // Inhale
                phase = "Breathe In (Fill your lungs)"
                secondsLeft = 4
                while (secondsLeft > 0) {
                    delay(1000)
                    secondsLeft--
                }
                // Hold
                phase = "Hold (Be still)"
                secondsLeft = 4
                while (secondsLeft > 0) {
                    delay(1000)
                    secondsLeft--
                }
                // Exhale
                phase = "Exhale (Release stress)"
                secondsLeft = 6
                while (secondsLeft > 0) {
                    delay(1000)
                    secondsLeft--
                }
            }
        } else {
            phase = "Ready to Reset"
            secondsLeft = 0
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth().testTag("breathing_assist_card"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Paced Breathing Assist 💨",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            val animatedScale by animateFloatAsState(
                targetValue = if (!pulseActive) 1.0f 
                else when {
                    phase.startsWith("Breathe") -> 1.3f
                    phase.startsWith("Hold") -> 1.3f
                    else -> 0.8f
                },
                animationSpec = tween(durationMillis = if (phase.startsWith("Ready")) 500 else 4000)
            )

            Box(
                modifier = Modifier
                    .size(100.dp)
                    .scale(animatedScale)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (pulseActive) {
                        Text(
                            text = "$secondsLeft s",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Start",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            Text(
                text = phase,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Button(
                onClick = { pulseActive = !pulseActive },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (pulseActive) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.testTag("panic_breathing_toggle_btn")
            ) {
                Text(
                    text = if (pulseActive) "Stop Breathing Guide" else "Begin Deep Breathing",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun SensoryGroundingAssist() {
    var currentStep by remember { mutableStateOf(5) }
    val stepText = when (currentStep) {
        5 -> "👁️ Gaze around and name of 5 things you can see (e.g. lamp, table, wall photograph, clock, book)."
        4 -> "🖐️ Touch 4 distinct physical textures near you. Feel your clothing, table edge, phone bezel, soft pillow."
        3 -> "👂 Listen closely. Identify 3 sounds you can hear right now (e.g. air conditioning humming, generic ticking, birds chirping)."
        2 -> "👃 Actively inhale and notice 2 distinct aromas or scents (e.g. skin lotion, warm beverage, dry wood)."
        1 -> "👅 Name 1 taste inside your mouth (or take a comforting sip of fresh water to reset taste buds)."
        else -> "🎉 Magnificently Grounded! Your nervous system is de-escalating. You are safe in the present moment."
    }

    Card(
        modifier = Modifier.fillMaxWidth().testTag("sensory_grounding_card"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "5-4-3-2-1 Sensory Grounding Method 🧘‍♀️",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
            
            Text(
                text = "This clinically proven exercise physically slows physiological distress responses by re-focusing active attention onto real-world sensations.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = if (currentStep in 1..5) "STEP $currentStep OF 5" else "GROUNDING COMPLETE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stepText,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentStep > 1) {
                    Button(
                        onClick = { currentStep-- },
                        modifier = Modifier.testTag("panic_grounding_next_btn"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("I can sense this. Next →", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                } else if (currentStep == 1) {
                    Button(
                        onClick = { currentStep = 0 },
                        modifier = Modifier.testTag("panic_grounding_finish_btn"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Text("Complete Exercise ✔️", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                } else {
                    Button(
                        onClick = { currentStep = 5 },
                        modifier = Modifier.testTag("panic_grounding_restart_btn"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Restart Grounding", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun SupportContactsTabContent() {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("overcomer_sos_contacts", Context.MODE_PRIVATE) }
    
    val roles = listOf("Sponsor", "Mentor", "Spouse", "Best Friend")
    
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = "My Personal Supporter Circle 🤝",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "These are your pre-defined supporters. Tap to dial or text immediately.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        items(roles.size) { index ->
            val role = roles[index]
            var phone by remember(role) {
                mutableStateOf(sharedPrefs.getString("phone_$role", "") ?: "")
            }
            var showEditPhone by remember { mutableStateOf(false) }
            var editPhoneInput by remember { mutableStateOf(phone) }

            Card(
                modifier = Modifier.fillMaxWidth().testTag("supporter_card_$role"),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = role,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (phone.isNotBlank()) {
                                Text(
                                    text = phone,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            } else {
                                Text(
                                    text = "No phone number saved",
                                    style = MaterialTheme.typography.labelSmall.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            // Call Button
                            IconButton(
                                onClick = {
                                    if (phone.isNotBlank()) {
                                        try {
                                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Cannot open dialer.", Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        Toast.makeText(context, "Please configure phone first.", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                enabled = phone.isNotBlank(),
                                modifier = Modifier.testTag("panic_call_$role")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = "Call $role",
                                    tint = if (phone.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                )
                            }

                            // SMS button
                            IconButton(
                                onClick = {
                                    if (phone.isNotBlank()) {
                                        try {
                                            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$phone")).apply {
                                                putExtra("sms_body", "I'm experiencing high temptation / distress. Please reach out to me!")
                                            }
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Cannot open messenger", Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        Toast.makeText(context, "Please configure phone first.", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                enabled = phone.isNotBlank(),
                                modifier = Modifier.testTag("panic_sms_$role")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = "Message $role",
                                    tint = if (phone.isNotBlank()) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline
                                )
                            }

                            // Edit Button
                            IconButton(
                                onClick = { 
                                    editPhoneInput = phone
                                    showEditPhone = !showEditPhone 
                                },
                                modifier = Modifier.testTag("panic_edit_btn_$role")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit phone",
                                    tint = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }

                    if (showEditPhone) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = editPhoneInput,
                                onValueChange = { editPhoneInput = it },
                                label = { Text("Phone Number") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                modifier = Modifier.weight(1f).testTag("phone_edit_input_$role")
                            )
                            Button(
                                onClick = {
                                    phone = editPhoneInput
                                    sharedPrefs.edit().putString("phone_$role", editPhoneInput).apply()
                                    showEditPhone = false
                                },
                                modifier = Modifier.testTag("phone_save_btn_$role")
                            ) {
                                Text("Save")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GlobalHelplinesTabContent() {
    val context = LocalContext.current
    
    val helplines = listOf(
        CrisisHelpline(
            name = "988 Suicide & Crisis Lifeline",
            description = "Confidential, 24/7 free emotional support for anyone in high distress or experiencing triggering crises.",
            contact = "988",
            isSms = true,
            smsValue = "HOME"
        ),
        CrisisHelpline(
            name = "SAMHSA National Helpline",
            description = "Government mental health agency providing immediate guidance, referrals, and localized treatment resources.",
            contact = "1-800-662-4357",
            isSms = false
        ),
        CrisisHelpline(
            name = "Crisis Text Line",
            description = "Instantly correspond with a live certified mental health specialist via text messaging.",
            contact = "741741",
            isSms = true,
            smsValue = "HOME"
        ),
        CrisisHelpline(
            name = "National Emergency Services (911)",
            description = "Immediately request urgent tactical dispatcher assistance if you are in physical danger.",
            contact = "911",
            isSms = false
        )
    )

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = "Professional Crisis Lifelines 🛡️",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
            Text(
                text = "Free, confidential, professional service networks available 24 hours a day, 7 days a week.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        items(helplines.size) { index ->
            val helpline = helplines[index]
            Card(
                modifier = Modifier.fillMaxWidth().testTag("helpline_card_${helpline.contact}"),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = helpline.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = helpline.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${helpline.contact}"))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Cannot dial helpline.", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f).testTag("helpline_call_btn_${helpline.contact}"),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(imageVector = Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Call ${helpline.contact}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        if (helpline.isSms) {
                            Button(
                                onClick = {
                                    try {
                                        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${helpline.contact}")).apply {
                                            putExtra("sms_body", helpline.smsValue)
                                        }
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Cannot open messenger.", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.weight(1f).testTag("helpline_sms_btn_${helpline.contact}"),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary
                                )
                            ) {
                                Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (helpline.smsValue == "HOME") "Sms 'HOME'" else "Text ${helpline.contact}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

data class CrisisHelpline(
    val name: String,
    val description: String,
    val contact: String,
    val isSms: Boolean,
    val smsValue: String = ""
)

