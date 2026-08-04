package app.kamy.saatApp.ui.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.hilt.navigation.compose.hiltViewModel
import app.kamy.saatApp.R
import app.kamy.saatApp.core.locale.AppLanguage
import app.kamy.saatApp.core.locale.AppStrings
import app.kamy.saatApp.design.theme.SaatColors
import app.kamy.saatApp.design.theme.SaatSpacing
import app.kamy.saatApp.domain.model.PrayerType
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.runtime.rememberCoroutineScope

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    vm: OnboardingViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val strings = vm.strings
    var showLocationRationale by remember { androidx.compose.runtime.mutableStateOf(false) }
    val scope = rememberCoroutineScope()

        val locationPermissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { vm.onLocationPermissionResult(it.values.any { granted -> granted }) }

        val notificationPermissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { vm.onNotificationPermissionResult() }

        val stepIndex = when (state.step) {
            OnboardingStep.LANGUAGE -> 1
            OnboardingStep.WELCOME -> 2
            OnboardingStep.LOCATION -> 3
            OnboardingStep.NOTIFICATIONS -> 4
            OnboardingStep.PRAYER_NOTIFICATIONS -> 5
        }

        if (showLocationRationale) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showLocationRationale = false },
                title = {
                    Text(
                        text = strings.getString(R.string.onboarding_location_rationale_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = SaatColors.Slate900
                    )
                },
                text = {
                    Text(
                        text = strings.getString(R.string.onboarding_location_rationale_body),
                        style = MaterialTheme.typography.bodyMedium,
                        color = SaatColors.Slate700
                    )
                },
                confirmButton = {
                    androidx.compose.material3.TextButton(
                        onClick = {
                            showLocationRationale = false
                            locationPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                    ) {
                        Text(
                            text = strings.getString(android.R.string.ok),
                            color = SaatColors.DeepEmerald,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                dismissButton = {
                    androidx.compose.material3.TextButton(
                        onClick = { showLocationRationale = false }
                    ) {
                        Text(
                            text = strings.getString(android.R.string.cancel),
                            color = SaatColors.Slate500
                        )
                    }
                },
                shape = RoundedCornerShape(16.dp),
                containerColor = Color.White
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF085E43),
                            Color(0xFF15AA7C)
                        )
                    )
                )
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp)
        ) {
            Column(modifier = Modifier.padding(bottom = 16.dp)) {
                Text(
                    text = strings.getString(R.string.onboarding_step_progress, stepIndex, 5),
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.75f),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                LinearProgressIndicator(
                    progress = { stepIndex / 5f },
                    modifier = Modifier.fillMaxWidth(),
                    color = SaatColors.GoldBright,
                    trackColor = Color.White.copy(alpha = 0.2f)
                )
            }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (state.step) {
                OnboardingStep.LANGUAGE -> LanguageStep(
                    selected = state.selectedLanguage,
                    onSelect = vm::selectLanguage,
                    strings = strings
                )
                OnboardingStep.WELCOME -> WelcomeStep(strings = strings)
                OnboardingStep.LOCATION -> LocationStep(
                    query = state.locationQuery,
                    saving = state.savingLocation,
                    error = state.locationError,
                    onQueryChange = vm::updateLocationQuery,
                    onUseGps = { showLocationRationale = true },
                    strings = strings
                )
                OnboardingStep.NOTIFICATIONS -> NotificationsStep(strings = strings)
                OnboardingStep.PRAYER_NOTIFICATIONS -> PrayerNotificationsStep(
                    toggles = state.prayerAdzanToggles,
                    onToggle = vm::togglePrayerAdzan,
                    strings = strings
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = {
                    when (state.step) {
                        OnboardingStep.LANGUAGE -> {
                            vm.selectLanguage(state.selectedLanguage)
                            vm.nextStep()
                        }
                        OnboardingStep.WELCOME -> vm.nextStep()
                        OnboardingStep.LOCATION -> {
                            if (state.locationQuery.isNotBlank()) {
                                vm.saveManualLocation()
                            } else {
                                showLocationRationale = true
                            }
                        }
                        OnboardingStep.NOTIFICATIONS -> {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                vm.nextStep()
                            }
                        }
                        OnboardingStep.PRAYER_NOTIFICATIONS -> {
                            scope.launch {
                                vm.completeOnboarding()
                                delay(200)
                                onFinished()
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.savingLocation,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SaatColors.GoldBright,
                    contentColor = SaatColors.DeepEmerald,
                    disabledContainerColor = SaatColors.GoldBright.copy(alpha = 0.4f),
                    disabledContentColor = SaatColors.DeepEmerald.copy(alpha = 0.5f)
                )
            ) {
                Text(
                    when (state.step) {
                        OnboardingStep.LANGUAGE -> strings.getString(R.string.onboarding_continue)
                        OnboardingStep.PRAYER_NOTIFICATIONS -> strings.getString(R.string.onboarding_get_started)
                        OnboardingStep.NOTIFICATIONS -> strings.getString(R.string.onboarding_enable_notifications)
                        else -> strings.getString(R.string.onboarding_continue)
                    }
                )
            }
            if (state.step != OnboardingStep.LANGUAGE) {
                OnboardingSecondaryButton(
                    onClick = {
                        if (state.step == OnboardingStep.NOTIFICATIONS) {
                            vm.skipNotifications()
                        } else if (state.step == OnboardingStep.LOCATION) {
                            vm.skipLocation()
                        } else if (state.step == OnboardingStep.PRAYER_NOTIFICATIONS) {
                            scope.launch {
                                vm.completeOnboarding()
                                delay(200)
                                onFinished()
                            }
                        } else {
                            vm.nextStep()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    labelColor = SaatColors.GoldBright
                ) {
                    Text(
                        when (state.step) {
                            OnboardingStep.LOCATION -> strings.getString(R.string.onboarding_skip_location)
                            OnboardingStep.PRAYER_NOTIFICATIONS -> strings.getString(R.string.onboarding_skip)
                            OnboardingStep.NOTIFICATIONS -> strings.getString(R.string.onboarding_skip_notifications)
                            else -> strings.getString(R.string.onboarding_skip)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun WelcomeStep(strings: AppStrings) {
    Text("☪", style = MaterialTheme.typography.displayMedium, color = SaatColors.GoldBright)
    Spacer(Modifier.height(16.dp))
    Text(
        text = strings.getString(R.string.onboarding_welcome_title),
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = androidx.compose.ui.graphics.Color.White,
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(12.dp))
    Text(
        text = strings.getString(R.string.onboarding_welcome_body),
        style = MaterialTheme.typography.bodyLarge,
        color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.82f),
        textAlign = TextAlign.Center
    )
}

@Composable
private fun LanguageStep(
    selected: AppLanguage,
    onSelect: (AppLanguage) -> Unit,
    strings: AppStrings
) {
    Text(
        text = strings.getString(R.string.onboarding_language_title),
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        color = androidx.compose.ui.graphics.Color.White,
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(12.dp))
    Text(
        text = strings.getString(R.string.onboarding_language_body),
        style = MaterialTheme.typography.bodyMedium,
        color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f),
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(24.dp))
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        AppLanguage.entries.forEach { lang ->
            val isSelected = lang == selected
            val flagRes = when (lang) {
                AppLanguage.INDONESIAN -> R.drawable.ic_flag_id
                AppLanguage.ENGLISH -> R.drawable.ic_flag_en
                AppLanguage.MALAY -> R.drawable.ic_flag_ms
            }
            Surface(
                onClick = { onSelect(lang) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = if (isSelected) {
                    SaatColors.DeepEmerald.copy(alpha = 0.15f)
                } else {
                    SaatColors.LightGrey.copy(alpha = 0.35f)
                },
                border = if (isSelected) {
                    BorderStroke(1.5.dp, SaatColors.GoldBright)
                } else {
                    BorderStroke(1.dp, androidx.compose.ui.graphics.Color.White.copy(alpha = 0.35f))
                }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = androidx.compose.ui.res.painterResource(id = flagRes),
                        contentDescription = null,
                        modifier = Modifier.size(width = 28.dp, height = 20.dp)
                    )
                    Spacer(Modifier.width(14.dp))
                    Text(
                        text = strings.getString(lang.labelRes),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = androidx.compose.ui.graphics.Color.White,
                        modifier = Modifier.weight(1f)
                    )
                    if (isSelected) {
                        Text(
                            text = "✓",
                            color = SaatColors.GoldBright,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LocationStep(
    query: String,
    saving: Boolean,
    error: String?,
    onQueryChange: (String) -> Unit,
    onUseGps: () -> Unit,
    strings: AppStrings
) {
    Text(
        text = strings.getString(R.string.onboarding_location_title),
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        color = androidx.compose.ui.graphics.Color.White,
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(12.dp))
    Text(
        text = strings.getString(R.string.onboarding_location_body),
        style = MaterialTheme.typography.bodyMedium,
        color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f),
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(20.dp))
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(strings.getString(R.string.location_city_hint)) },
        singleLine = true,
        isError = error != null,
        supportingText = error?.let { { Text(it, color = Color.White.copy(alpha = 0.85f)) } },
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedBorderColor = SaatColors.GoldBright,
            unfocusedBorderColor = Color.White.copy(alpha = 0.45f),
            focusedLabelColor = SaatColors.GoldBright,
            unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
            cursorColor = SaatColors.GoldBright,
            errorBorderColor = MaterialTheme.colorScheme.error,
            errorLabelColor = MaterialTheme.colorScheme.error,
            errorSupportingTextColor = Color.White.copy(alpha = 0.9f)
        )
    )
    Spacer(Modifier.height(8.dp))
    OnboardingSecondaryButton(
        onClick = onUseGps,
        modifier = Modifier.fillMaxWidth(),
        enabled = !saving
    ) {
        if (saving) {
            CircularProgressIndicator(
                modifier = Modifier.height(20.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
        } else {
            Text(strings.getString(R.string.location_use_gps))
        }
    }
}

@Composable
private fun OnboardingSecondaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    labelColor: Color = Color.White,
    content: @Composable () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        border = BorderStroke(1.dp, labelColor.copy(alpha = if (enabled) 0.65f else 0.35f)),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = labelColor,
            disabledContentColor = labelColor.copy(alpha = 0.38f)
        )
    ) {
        content()
    }
}

@Composable
private fun NotificationsStep(strings: AppStrings) {
    Text(
        text = strings.getString(R.string.onboarding_notifications_title),
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        color = androidx.compose.ui.graphics.Color.White,
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(12.dp))
    Text(
        text = strings.getString(R.string.onboarding_notifications_body),
        style = MaterialTheme.typography.bodyMedium,
        color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f),
        textAlign = TextAlign.Center
    )
}

@Composable
private fun WidgetStep(strings: AppStrings) {
    Text(
        text = strings.getString(R.string.onboarding_widget_title),
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        color = androidx.compose.ui.graphics.Color.White,
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(12.dp))
    Text(
        text = strings.getString(R.string.onboarding_widget_body),
        style = MaterialTheme.typography.bodyMedium,
        color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f),
        textAlign = TextAlign.Center
    )
}

@Composable
private fun PrayerNotificationsStep(
    toggles: Map<PrayerType, Boolean>,
    onToggle: (PrayerType, Boolean) -> Unit,
    strings: AppStrings
) {
    Text(
        text = strings.getString(R.string.onboarding_prayer_config_title),
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        color = androidx.compose.ui.graphics.Color.White,
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(12.dp))
    Text(
        text = strings.getString(R.string.onboarding_prayer_config_body),
        style = MaterialTheme.typography.bodyMedium,
        color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f),
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(24.dp))
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        PrayerType.ADZAN_NOTIFICATION_PRAYERS.forEach { type ->
            val checked = toggles[type] ?: true
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val prayerName = when (type) {
                    PrayerType.FAJR -> strings.getString(R.string.prayer_fajr)
                    PrayerType.DHUHR -> strings.getString(R.string.prayer_dhuhr)
                    PrayerType.ASR -> strings.getString(R.string.prayer_asr)
                    PrayerType.MAGHRIB -> strings.getString(R.string.prayer_maghrib)
                    PrayerType.ISHA -> strings.getString(R.string.prayer_isha)
                    else -> ""
                }
                Text(prayerName, color = Color.White, style = MaterialTheme.typography.titleMedium)
                androidx.compose.material3.Switch(
                    checked = checked,
                    onCheckedChange = { onToggle(type, it) },
                    colors = androidx.compose.material3.SwitchDefaults.colors(
                        checkedThumbColor = SaatColors.DeepEmerald,
                        checkedTrackColor = SaatColors.GoldBright,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color.White.copy(alpha = 0.3f),
                        uncheckedBorderColor = Color.Transparent
                    )
                )
            }
        }
    }
}


