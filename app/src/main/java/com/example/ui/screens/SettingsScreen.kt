package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.UserPreferenceEntity
import com.example.data.localization.AppLanguage
import com.example.data.localization.Localization
import com.example.data.model.EquipmentType
import com.example.data.model.FitnessLevel
import com.example.ui.theme.*

@Composable
fun SettingsScreen(
    currentPrefs: UserPreferenceEntity,
    onUpdatePrefs: (UserPreferenceEntity) -> Unit,
    onResetProgram: () -> Unit
) {
    val scrollState = rememberScrollState()
    val currentLang = remember(currentPrefs.language) {
        try {
            AppLanguage.valueOf(currentPrefs.language)
        } catch (_: Exception) {
            AppLanguage.ENGLISH
        }
    }
    val currentFitness = remember(currentPrefs.fitnessLevel) {
        try {
            FitnessLevel.valueOf(currentPrefs.fitnessLevel)
        } catch (_: Exception) {
            FitnessLevel.BEGINNER
        }
    }
    val currentEquipment = remember(currentPrefs.equipment) {
        try {
            EquipmentType.valueOf(currentPrefs.equipment)
        } catch (_: Exception) {
            EquipmentType.NONE
        }
    }

    var showResetDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GymDarkBackground)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Column {
            Text(
                text = "SETTINGS",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                color = GymTextPrimary
            )
            Text(
                text = "Personalize your workout experience and audio pacing",
                style = MaterialTheme.typography.bodySmall,
                color = GymTextSecondary
            )
        }

        // 1. WORKOUT SECTION
        SettingsSection(title = "WORKOUT") {
            // Fitness Level
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = Localization.get("settings_fitness_level", currentLang),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = GymTextPrimary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FitnessLevel.values().forEach { level ->
                        val selected = currentFitness == level
                        FilterChip(
                            selected = selected,
                            onClick = { onUpdatePrefs(currentPrefs.copy(fitnessLevel = level.name)) },
                            label = {
                                Text(
                                    text = level.name.lowercase().replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GymOrangePrimary,
                                selectedLabelColor = Color.White,
                                containerColor = GymDarkSurface,
                                labelColor = GymTextSecondary
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Divider(color = GymDivider)

            // Target Workout Duration
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = Localization.get("settings_duration", currentLang),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = GymTextPrimary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(10, 20, 30, 45).forEach { mins ->
                        val selected = currentPrefs.preferredDurationMinutes == mins
                        FilterChip(
                            selected = selected,
                            onClick = { onUpdatePrefs(currentPrefs.copy(preferredDurationMinutes = mins)) },
                            label = { Text("${mins}m", style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GymOrangePrimary,
                                selectedLabelColor = Color.White,
                                containerColor = GymDarkSurface,
                                labelColor = GymTextSecondary
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Divider(color = GymDivider)

            // Equipment Type
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = Localization.get("settings_equipment", currentLang),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = GymTextPrimary
                )
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(
                        EquipmentType.NONE to Localization.get("eq_none", currentLang),
                        EquipmentType.DUMBBELLS to Localization.get("eq_dumbbells", currentLang),
                        EquipmentType.RESISTANCE_BAND to Localization.get("eq_resistance_band", currentLang),
                        EquipmentType.FULL to Localization.get("eq_full", currentLang)
                    ).forEach { (eqType, label) ->
                        val selected = currentEquipment == eqType
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (selected) GymOrangePrimary.copy(alpha = 0.15f) else GymDarkSurface,
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = androidx.compose.ui.graphics.SolidColor(if (selected) GymOrangePrimary else GymCardBorder)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onUpdatePrefs(currentPrefs.copy(equipment = eqType.name)) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (selected) GymOrangePrimary else GymTextPrimary,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                                if (selected) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = GymOrangePrimary, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. LANGUAGE SECTION
        SettingsSection(title = "LANGUAGE") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf(
                    AppLanguage.ENGLISH to "English",
                    AppLanguage.HINDI to "हिन्दी (Hindi)"
                ).forEach { (lang, label) ->
                    val selected = currentLang == lang
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (selected) GymOrangePrimary.copy(alpha = 0.2f) else GymDarkSurface,
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(if (selected) GymOrangePrimary else GymCardBorder)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onUpdatePrefs(currentPrefs.copy(language = lang.name)) }
                            .testTag("lang_btn_${lang.name}")
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (selected) GymOrangePrimary else GymTextPrimary
                            )
                            if (selected) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GymOrangePrimary, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }

        // 3. SOUND & AUDIO GUIDANCE SECTION
        SettingsSection(title = "AUDIO & VOICE") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = Localization.get("settings_voice", currentLang),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = GymTextPrimary
                    )
                    Text(
                        text = Localization.get("settings_voice_sub", currentLang),
                        style = MaterialTheme.typography.bodySmall,
                        color = GymTextSecondary
                    )
                }
                Switch(
                    checked = currentPrefs.voiceEnabled,
                    onCheckedChange = { onUpdatePrefs(currentPrefs.copy(voiceEnabled = it)) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = GymOrangePrimary,
                        uncheckedTrackColor = GymDarkSurface
                    )
                )
            }

            Divider(color = GymDivider)

            // Rest Interval duration
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = Localization.get("settings_rest_time", currentLang),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = GymTextPrimary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(30, 45, 60, 90).forEach { secs ->
                        val selected = currentPrefs.restTimeSeconds == secs
                        FilterChip(
                            selected = selected,
                            onClick = { onUpdatePrefs(currentPrefs.copy(restTimeSeconds = secs)) },
                            label = { Text("${secs}s", style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GymOrangePrimary,
                                selectedLabelColor = Color.White,
                                containerColor = GymDarkSurface,
                                labelColor = GymTextSecondary
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 4. CAMERA & AUTO REP COUNTING SECTION
        SettingsSection(title = "CAMERA & POSE DETECTION") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Auto Rep Counting",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = GymTextPrimary
                    )
                    Text(
                        text = "Use front camera for automatic push-up and exercise detection",
                        style = MaterialTheme.typography.bodySmall,
                        color = GymTextSecondary
                    )
                }
                Switch(
                    checked = currentPrefs.autoCountEnabled,
                    onCheckedChange = { onUpdatePrefs(currentPrefs.copy(autoCountEnabled = it)) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = GymOrangePrimary,
                        uncheckedTrackColor = GymDarkSurface
                    )
                )
            }
        }

        // 5. PROGRAM & RESET SECTION
        SettingsSection(title = "DATA & PROGRAM") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = Localization.get("settings_reset_progress", currentLang),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFEF4444)
                    )
                    Text(
                        text = "Restart 30-day program from Day 1",
                        style = MaterialTheme.typography.bodySmall,
                        color = GymTextSecondary
                    )
                }
                OutlinedButton(
                    onClick = { showResetDialog = true },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFEF4444))
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Reset")
                }
            }
        }

        // 6. ABOUT
        SettingsSection(title = "ABOUT") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "App Version", color = GymTextSecondary, style = MaterialTheme.typography.bodySmall)
                Text(text = "v1.0.0 • Premium Edition", color = GymTextPrimary, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Training Method", color = GymTextSecondary, style = MaterialTheme.typography.bodySmall)
                Text(text = "Progressive Bodyweight Overload", color = GymCyanSecondary, style = MaterialTheme.typography.bodySmall)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = {
                Text(
                    text = Localization.get("settings_reset_progress", currentLang),
                    fontWeight = FontWeight.Bold,
                    color = GymTextPrimary
                )
            },
            text = {
                Text(
                    text = Localization.get("settings_reset_confirm", currentLang),
                    color = GymTextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showResetDialog = false
                        onResetProgram()
                    }
                ) {
                    Text(Localization.get("confirm", currentLang), color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(Localization.get("cancel", currentLang), color = GymTextSecondary)
                }
            },
            containerColor = GymCardBackground
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
            color = GymTextSecondary
        )
        Card(
            colors = CardDefaults.cardColors(containerColor = GymCardBackground),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, GymCardBorder, RoundedCornerShape(18.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                content = content
            )
        }
    }
}
