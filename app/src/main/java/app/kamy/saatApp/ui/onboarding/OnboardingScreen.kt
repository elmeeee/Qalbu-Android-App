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
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.runtime.rememberCoroutineScope

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    vm: OnboardingViewModel = hiltViewModel()
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val languageStore = remember { app.kamy.saatApp.infrastructure.preferences.AppLanguageStore.from(context) }
    val currentLang by languageStore.currentFlow.collectAsState()

    val localizedContext = remember(currentLang) {
        app.kamy.saatApp.core.locale.AppLocale.wrap(context, currentLang)
    }
    val localizedConfiguration = remember(currentLang) {
        android.content.res.Configuration(context.resources.configuration).apply {
            setLocale(java.util.Locale.forLanguageTag(currentLang.tag))
        }
    }

    CompositionLocalProvider(
        androidx.compose.ui.platform.LocalContext provides localizedContext,
        androidx.compose.ui.platform.LocalConfiguration provides localizedConfiguration
    ) {
        val state by vm.state.collectAsState()
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
                        listOf(Color(0xFF085E43), Color(0xFF15AA7C))
                    )
                )
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = SaatSpacing.screenHorizontal, vertical = SaatSpacing.lg),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                stringResource(R.string.onboarding_step_progress, stepIndex, 5),
                style = MaterialTheme.typography.labelMedium,
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
                OnboardingStep.LANGUAGE -> LanguageStep()
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
                        OnboardingStep.LANGUAGE -> vm.nextStep()
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
                        OnboardingStep.LANGUAGE -> stringResource(R.string.onboarding_continue)
                        OnboardingStep.PRAYER_NOTIFICATIONS -> stringResource(R.string.onboarding_get_started)
                        OnboardingStep.NOTIFICATIONS -> stringResource(R.string.onboarding_enable_notifications)
                        OnboardingStep.LOCATION -> stringResource(R.string.onboarding_continue)
                        OnboardingStep.WELCOME -> stringResource(R.string.onboarding_continue)
                    }
                )
            }
            if (state.step != OnboardingStep.WELCOME && state.step != OnboardingStep.LANGUAGE) {
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

@Composable
private fun LanguageStep() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val languageStore = remember { app.kamy.saatApp.infrastructure.preferences.AppLanguageStore.from(context) }
    val currentLang by languageStore.currentFlow.collectAsState()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = stringResource(R.string.onboarding_language_title),
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.onboarding_language_body),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.85f),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(28.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            app.kamy.saatApp.core.locale.AppLanguage.entries.forEach { lang ->
                val isSelected = lang == currentLang
                val flagRes = when (lang) {
                    app.kamy.saatApp.core.locale.AppLanguage.INDONESIAN -> R.drawable.ic_flag_id
                    app.kamy.saatApp.core.locale.AppLanguage.ENGLISH -> R.drawable.ic_flag_en
                    app.kamy.saatApp.core.locale.AppLanguage.MALAY -> R.drawable.ic_flag_ms
                }
                androidx.compose.material3.Surface(
                    onClick = { languageStore.set(lang) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.12f),
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) SaatColors.GoldBright else Color.White.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.material3.Icon(
                            painter = androidx.compose.ui.res.painterResource(flagRes),
                            contentDescription = null,
                            tint = Color.Unspecified,
                            modifier = Modifier.size(width = 30.dp, height = 22.dp)
                        )
                        Spacer(Modifier.width(16.dp))
                        Text(
                            text = stringResource(lang.labelRes),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) SaatColors.DeepEmerald else Color.White,
                            modifier = Modifier.weight(1f)
                        )
                        if (isSelected) {
                            Text(
                                text = "✓",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = SaatColors.DeepEmerald
                            )
                        }
                    }
                }
            }
        }
    }
}
