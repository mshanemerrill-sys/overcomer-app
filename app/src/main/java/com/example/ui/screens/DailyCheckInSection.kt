package com.example.ui.screens

import android.text.format.DateUtils
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.DailyCheckIn
import com.example.ui.OverComerViewModel
import java.text.SimpleDateFormat
import java.util.*
import android.speech.tts.TextToSpeech

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DailyCheckInSection(
    viewModel: OverComerViewModel,
    modifier: Modifier = Modifier,
    tts: TextToSpeech? = null
) {
    val checkIns by viewModel.dailyCheckIns.collectAsStateWithLifecycle()
    val isSaving by viewModel.isSavingCheckIn.collectAsStateWithLifecycle()
    val latestReflection by viewModel.latestCheckInReflection.collectAsStateWithLifecycle()
    
    // Setup Local Text To Speech if not provided
    val context = LocalContext.current
    var localTtsInstance by remember { mutableStateOf<TextToSpeech?>(null) }
    var isTtsReady by remember { mutableStateOf(false) }
    
    DisposableEffect(Unit) {
        if (tts == null) {
            var initializedTts: TextToSpeech? = null
            initializedTts = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    try {
                        val result = initializedTts?.setLanguage(Locale.getDefault())
                        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                            initializedTts?.setLanguage(Locale.US)
                        }
                        initializedTts?.setSpeechRate(0.82f) // Comfortably slower reading rate as requested
                        initializedTts?.setPitch(1.0f)
                    } catch (_: Exception) {}
                    isTtsReady = true
                }
            }
            localTtsInstance = initializedTts
        }
        onDispose {
            localTtsInstance?.stop()
            localTtsInstance?.shutdown()
        }
    }
    
    val activeTts = tts ?: localTtsInstance

    var isCheckingIn by remember { mutableStateOf(false) }
    var selectedMood by remember { mutableStateOf("Peaceful") }
    var energyLevel by remember { mutableFloatStateOf(4f) }
    
    // Checkbox items for triggers
    val availableTriggers = listOf(
        "Loneliness", "Stress / Work", "Conflict", 
        "Fatigue / Tiredness", "Hunger / Cravings", "Boredom", "Difficult Emotions"
    )
    val selectedTriggers = remember { mutableStateListOf<String>() }
    var customTrigger by remember { mutableStateOf("") }
    var journalNotes by remember { mutableStateOf("") }
    var showHistory by remember { mutableStateOf(false) }

    // Determine if today has been checked in
    val hasCheckedInToday = remember(checkIns) {
        val today = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        checkIns.any { 
            val checkInDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date(it.timestamp))
            checkInDate == today
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .testTag("daily_checkin_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)),
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Daily Check-In",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "DAILY CHECK-IN",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                if (checkIns.isNotEmpty()) {
                    TextButton(
                        onClick = { showHistory = !showHistory },
                        modifier = Modifier.testTag("view_history_btn")
                    ) {
                        Text(
                            text = if (showHistory) "Hide History" else "History (${checkIns.size})",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }

            if (!isCheckingIn && !hasCheckedInToday && latestReflection == null) {
                // Intro and Trigger to start check-in
                Text(
                    text = "Reflecting daily on your emotions, mental batteries, and triggers aligns your soul with the Holy Spirit and helps prevent relapses or anxieties.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Button(
                    onClick = { 
                        selectedTriggers.clear()
                        customTrigger = ""
                        journalNotes = ""
                        energyLevel = 4f
                        selectedMood = "Peaceful"
                        isCheckingIn = true 
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("start_checkin_btn"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "Complete Today's Check-In 📝",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else if (isCheckingIn) {
                // Form Layout
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Divider()

                    // Question 1: Mood
                    Column {
                        Text(
                            text = "1. How is your soul responding to God today?",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        val moods = listOf(
                            "Joyful" to "🌟 Joyful",
                            "Peaceful" to "🕊️ Peaceful",
                            "Anxious" to "🌪️ Anxious",
                            "Discouraged" to "🏔️ Discouraged",
                            "Tempted" to "🔥 Tempted",
                            "Struggling" to "🩹 Struggling"
                        )
                        
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            moods.forEach { (moodKey, moodLabel) ->
                                FilterChip(
                                    selected = selectedMood == moodKey,
                                    onClick = { selectedMood = moodKey },
                                    label = { Text(moodLabel) },
                                    modifier = Modifier.testTag("mood_chip_$moodKey")
                                )
                            }
                        }
                    }

                    // Question 2: Energy level
                    Column {
                        Text(
                            text = "2. What is your mental battery / energy state?",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Very Low (1)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Text(
                                "Excellent (5)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                        Slider(
                            value = energyLevel,
                            onValueChange = { energyLevel = it },
                            valueRange = 1f..5f,
                            steps = 3,
                            modifier = Modifier.testTag("energy_level_slider")
                        )
                    }

                    // Question 3: Triggers
                    Column {
                        Text(
                            text = "3. Any triggers active in your environment?",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            availableTriggers.forEach { trigger ->
                                val isSelected = selectedTriggers.contains(trigger)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { 
                                        if (isSelected) selectedTriggers.remove(trigger) 
                                        else selectedTriggers.add(trigger)
                                    },
                                    label = { Text(trigger) },
                                    modifier = Modifier.testTag("trigger_chip_${trigger.replace(" ", "_").replace("/", "_")}")
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = customTrigger,
                            onValueChange = { customTrigger = it },
                            label = { Text("Describe other triggers or circumstances") },
                            placeholder = { Text("E.g. Bad weather, certain locations, specific text...") },
                            modifier = Modifier.fillMaxWidth().testTag("custom_trigger_field"),
                            maxLines = 2,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    // Question 4: Journal notes
                    Column {
                        Text(
                            text = "4. What is on your mind or heart? (Journal Notes)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = journalNotes,
                            onValueChange = { journalNotes = it },
                            label = { Text("Let it out to God...") },
                            placeholder = { Text("Describe any automatic thoughts, cravings, struggles, or praises.") },
                            modifier = Modifier.fillMaxWidth().height(100.dp).testTag("journal_notes_field"),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    // Submit & Cancel
                    if (isSaving) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                "Anointing reflection from OverComer Guide...",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { isCheckingIn = false },
                                modifier = Modifier.weight(1f).testTag("cancel_checkin_btn"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Cancel", fontWeight = FontWeight.Bold)
                            }
                            
                            Button(
                                onClick = {
                                    val triggersList = selectedTriggers.toMutableList()
                                    if (customTrigger.isNotBlank()) {
                                        triggersList.add(customTrigger)
                                    }
                                    viewModel.addDailyCheckIn(
                                        mood = selectedMood,
                                        energyLevel = energyLevel.toInt(),
                                        triggers = triggersList.joinToString(", "),
                                        journalNotes = journalNotes,
                                        onComplete = {
                                            isCheckingIn = false
                                        }
                                    )
                                },
                                modifier = Modifier.weight(1.5f).testTag("submit_checkin_btn"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Submit & Reflect ✨", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                // Checked In Today! Show encouragement or the latest AI reflection.
                Text(
                    text = "You've successfully checked in today! Standing firm on God's promises in 2 Corinthians 5:17.",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth()
                )

                // Render the latest reflection if generated, or the most recent check-in
                val activeReflection = latestReflection ?: checkIns.firstOrNull()?.aiReflection ?: ""
                val activeCheckIn = checkIns.firstOrNull()
                
                if (activeReflection.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f))
                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                                        imageVector = Icons.Default.Favorite,
                                        contentDescription = "CBT Theological Reflection",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "GUIDE'S CBT REFLECTION",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                        color = MaterialTheme.colorScheme.primary,
                                        letterSpacing = 0.8.sp
                                    )
                                }
                                
                                if (activeTts != null) {
                                    IconButton(
                                        onClick = {
                                            activeTts.speak(activeReflection, TextToSpeech.QUEUE_FLUSH, null, "CheckInRef")
                                        },
                                        modifier = Modifier.size(28.dp).testTag("speak_reflection_btn")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.VolumeUp,
                                            contentDescription = "Speak Reflection Outloud",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                            
                            if (activeCheckIn != null && latestReflection == null) {
                                Text(
                                    text = "Checked in under mood: ${activeCheckIn.mood} | Battery: ${activeCheckIn.energyLevel}/5",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                            
                            Text(
                                text = activeReflection,
                                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
                
                Button(
                    onClick = {
                        selectedTriggers.clear()
                        customTrigger = ""
                        journalNotes = ""
                        energyLevel = 4f
                        selectedMood = "Peaceful"
                        isCheckingIn = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("re_checkin_btn"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Text(
                        text = "Update Daily Check-In 🔄",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // History Expandable Block
            AnimatedVisibility(
                visible = showHistory && checkIns.isNotEmpty(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
                ) {
                    Divider()
                    Text(
                        text = "History of Grace",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.outline
                    )
                    
                    checkIns.forEach { checkIn ->
                        val dateString = DateUtils.getRelativeTimeSpanString(
                            checkIn.timestamp,
                            System.currentTimeMillis(),
                            DateUtils.MINUTE_IN_MILLIS
                        ).toString()

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("checkin_history_item"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${getMoodEmoji(checkIn.mood)} ${checkIn.mood} (Battery: ${checkIn.energyLevel}/5)",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = dateString,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                        IconButton(
                                            onClick = { viewModel.deleteDailyCheckIn(checkIn.id) },
                                            modifier = Modifier.size(24.dp).testTag("delete_checkin_btn_${checkIn.id}")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete Check-in",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }

                                if (checkIn.triggers.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Triggers: ${checkIn.triggers}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }

                                if (checkIn.journalNotes.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "\"${checkIn.journalNotes}\"",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = checkIn.aiReflection,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun getMoodEmoji(mood: String): String {
    return when (mood) {
        "Joyful" -> "🌟"
        "Peaceful" -> "🕊️"
        "Anxious" -> "🌪️"
        "Discouraged" -> "🏔️"
        "Tempted" -> "🔥"
        "Struggling" -> "🩹"
        else -> "📝"
    }
}
