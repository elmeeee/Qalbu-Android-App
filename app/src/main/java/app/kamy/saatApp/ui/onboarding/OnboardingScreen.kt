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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.kamy.saatApp.R
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
    var showLocationRationale by remember { androidx.compose.runtime.mutableStateOf(false) }
    val scope = rememberCoroutineScope()

        val locationPermissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { vm.onLocationPermissionResult(it.values.any { granted -> granted }) }

        val notificationPermissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { vm.onNotificationPermissionResult() }

        val stepIndex = when (state.step) {
            OnboardingStep.WELCOME -> 1
            OnboardingStep.LOCATION -> 2
            OnboardingStep.NOTIFICATIONS -> 3
            OnboardingStep.PRAYER_NOTIFICATIONS -> 4
        }

        if (showLocationRationale) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showLocationRationale = false },
                title = {
                    Text(
                        text = stringResource(R.string.onboarding_location_rationale_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = SaatColors.Slate900
                    )
                },
                text = {
                    Text(
                        text = stringResource(R.string.onboarding_location_rationale_body),
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
                            text = stringResource(android.R.string.ok),
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
                            text = stringResource(android.R.string.cancel),
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
                    text = stringResource(R.string.onboarding_step_progress, stepIndex, 4),
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.75f),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                LinearProgressIndicator(
                    progress = { stepIndex / 4f },
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
                OnboardingStep.WELCOME -> WelcomeStep()
                OnboardingStep.LOCATION -> LocationStep(
                    query = state.locationQuery,
                    saving = state.savingLocation,
                    error = state.locationError,
                    onQueryChange = vm::updateLocationQuery,
                    onUseGps = { showLocationRationale = true }
                )
                OnboardingStep.NOTIFICATIONS -> NotificationsStep()
                OnboardingStep.PRAYER_NOTIFICATIONS -> PrayerNotificationsStep(
                    toggles = state.prayerAdzanToggles,
                    onToggle = vm::togglePrayerAdzan
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = {
                    when (state.step) {
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
                                delay(200) // Decouple heavy DB read/write to prevent button lag
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
                        OnboardingStep.PRAYER_NOTIFICATIONS -> stringResource(R.string.onboarding_get_started)
                        OnboardingStep.NOTIFICATIONS -> stringResource(R.string.onboarding_enable_notifications)
                        OnboardingStep.LOCATION -> stringResource(R.string.onboarding_continue)
                        OnboardingStep.WELCOME -> stringResource(R.string.onboarding_continue)
                    }
                )
            }
            if (state.step != OnboardingStep.WELCOME) {
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
                            OnboardingStep.LOCATION -> stringResource(R.string.onboarding_skip_location)
                            OnboardingStep.PRAYER_NOTIFICATIONS -> stringResource(R.string.onboarding_skip)
                            OnboardingStep.NOTIFICATIONS -> stringResource(R.string.onboarding_skip_notifications)
                            else -> stringResource(R.string.onboarding_skip)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun WelcomeStep() {
    Text("☪", style = MaterialTheme.typography.displayMedium, color = SaatColors.GoldBright)
    Spacer(Modifier.height(16.dp))
    Text(
        text = stringResource(R.string.onboarding_welcome_title),
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = androidx.compose.ui.graphics.Color.White,
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(12.dp))
    Text(
        text = stringResource(R.string.onboarding_welcome_body),
        style = MaterialTheme.typography.bodyLarge,
        color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.82f),
        textAlign = TextAlign.Center
    )
}

@Composable
private fun LocationStep(
    query: String,
    saving: Boolean,
    error: String?,
    onQueryChange: (String) -> Unit,
    onUseGps: () -> Unit
) {
    Text(
        text = stringResource(R.string.onboarding_location_title),
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        color = androidx.compose.ui.graphics.Color.White,
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(12.dp))
    Text(
        text = stringResource(R.string.onboarding_location_body),
        style = MaterialTheme.typography.bodyMedium,
        color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f),
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(20.dp))
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(stringResource(R.string.location_city_hint)) },
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
            Text(stringResource(R.string.location_use_gps))
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
private fun NotificationsStep() {
    Text(
        text = stringResource(R.string.onboarding_notifications_title),
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        color = androidx.compose.ui.graphics.Color.White,
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(12.dp))
    Text(
        text = stringResource(R.string.onboarding_notifications_body),
        style = MaterialTheme.typography.bodyMedium,
        color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f),
        textAlign = TextAlign.Center
    )
}

@Composable
private fun WidgetStep() {
    Text(
        text = stringResource(R.string.onboarding_widget_title),
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        color = androidx.compose.ui.graphics.Color.White,
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(12.dp))
    Text(
        text = stringResource(R.string.onboarding_widget_body),
        style = MaterialTheme.typography.bodyMedium,
        color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f),
        textAlign = TextAlign.Center
    )
}

@Composable
private fun PrayerNotificationsStep(
    toggles: Map<PrayerType, Boolean>,
    onToggle: (PrayerType, Boolean) -> Unit
) {
    Text(
        text = stringResource(R.string.onboarding_prayer_config_title),
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        color = androidx.compose.ui.graphics.Color.White,
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(12.dp))
    Text(
        text = stringResource(R.string.onboarding_prayer_config_body),
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
                    PrayerType.FAJR -> stringResource(R.string.prayer_fajr)
                    PrayerType.DHUHR -> stringResource(R.string.prayer_dhuhr)
                    PrayerType.ASR -> stringResource(R.string.prayer_asr)
                    PrayerType.MAGHRIB -> stringResource(R.string.prayer_maghrib)
                    PrayerType.ISHA -> stringResource(R.string.prayer_isha)
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


