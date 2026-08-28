package app.kamy.saatApp.ui.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.kamy.saatApp.R
import app.kamy.saatApp.core.locale.AppLanguage
import app.kamy.saatApp.core.locale.AppStrings
import app.kamy.saatApp.domain.model.PrayerType
import app.kamy.saatApp.infrastructure.preferences.AppLanguageStore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val OnboardingDarkGreen = Color(0xFF133E2E)
private val OnboardingCardBg = Color(0xFFFDFBF7).copy(alpha = 0.45f)
private val OnboardingCardBorder = Color(0xFFEAE3D2).copy(alpha = 0.40f)
private val OnboardingSubtext = Color(0xFF64748B)

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    vm: OnboardingViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showLocationRationale by remember { mutableStateOf(false) }

    // Dynamically resolve strings for current language store selection
    val strings = remember(state.selectedLanguage) {
        AppStrings(context.applicationContext, AppLanguageStore.from(context))
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { vm.onLocationPermissionResult(it.values.any { granted -> granted }) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { vm.onNotificationPermissionResult() }

    val currentStepNumber = when (state.step) {
        OnboardingStep.LANGUAGE -> 1
        OnboardingStep.WELCOME -> 2
        OnboardingStep.LOCATION -> 3
        OnboardingStep.NOTIFICATIONS -> 4
        OnboardingStep.PRAYER_NOTIFICATIONS -> 5
    }

    val bgDrawable = when (state.step) {
        OnboardingStep.LANGUAGE -> R.drawable.bg_onboarding_1
        OnboardingStep.WELCOME -> R.drawable.bg_onboarding_2
        OnboardingStep.LOCATION -> R.drawable.bg_onboarding_3
        OnboardingStep.NOTIFICATIONS -> R.drawable.bg_onboarding_4
        OnboardingStep.PRAYER_NOTIFICATIONS -> R.drawable.bg_onboarding_5
    }

    if (showLocationRationale) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showLocationRationale = false },
            title = {
                Text(
                    text = strings.getString(R.string.onboarding_location_rationale_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = OnboardingDarkGreen,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = strings.getString(R.string.onboarding_location_rationale_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnboardingSubtext
                )
            },
            confirmButton = {
                TextButton(
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
                        color = OnboardingDarkGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showLocationRationale = false }) {
                    Text(text = strings.getString(android.R.string.cancel), color = OnboardingSubtext)
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.White
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Full screen device-fitted background illustration (FillBounds prevents cropping)
        Image(
            painter = painterResource(id = bgDrawable),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        // Overlay UI Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // STEP CONTENT (Top / Middle / Card)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.TopCenter
            ) {
                when (state.step) {
                    OnboardingStep.LANGUAGE -> LanguageStep(
                        selected = state.selectedLanguage,
                        onSelect = vm::selectLanguage,
                        strings = strings
                    )
                    OnboardingStep.WELCOME -> WelcomeStep(strings = strings)
                    OnboardingStep.LOCATION -> LocationStep(strings = strings)
                    OnboardingStep.NOTIFICATIONS -> NotificationStep(strings = strings)
                    OnboardingStep.PRAYER_NOTIFICATIONS -> PrayerNotificationsStep(
                        toggles = state.prayerAdzanToggles,
                        onToggle = vm::togglePrayerAdzan,
                        strings = strings
                    )
                }
            }

            // BOTTOM ACTION CONTROLS & PAGE INDICATOR
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // PRIMARY ACTION BUTTON
                Button(
                    onClick = {
                        when (state.step) {
                            OnboardingStep.LANGUAGE -> {
                                vm.selectLanguage(state.selectedLanguage)
                                vm.nextStep()
                            }
                            OnboardingStep.WELCOME -> vm.nextStep()
                            OnboardingStep.LOCATION -> showLocationRationale = true
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OnboardingDarkGreen,
                        contentColor = Color.White
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = when (state.step) {
                                OnboardingStep.LANGUAGE -> strings.getString(R.string.onboarding_continue)
                                OnboardingStep.WELCOME -> strings.getString(R.string.onboarding_welcome_btn)
                                OnboardingStep.LOCATION -> strings.getString(R.string.onboarding_btn_allow_location)
                                OnboardingStep.NOTIFICATIONS -> strings.getString(R.string.onboarding_btn_allow_notif)
                                OnboardingStep.PRAYER_NOTIFICATIONS -> strings.getString(R.string.onboarding_continue)
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        if (state.step == OnboardingStep.LANGUAGE || state.step == OnboardingStep.WELCOME || state.step == OnboardingStep.PRAYER_NOTIFICATIONS) {
                            Spacer(Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // SECONDARY BUTTON / FOOTER TEXT
                when (state.step) {
                    OnboardingStep.LOCATION -> {
                        TextButton(onClick = { vm.skipLocation() }) {
                            Text(
                                text = strings.getString(R.string.onboarding_btn_not_now),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    OnboardingStep.NOTIFICATIONS -> {
                        TextButton(onClick = { vm.skipNotifications() }) {
                            Text(
                                text = strings.getString(R.string.onboarding_btn_not_now),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    OnboardingStep.PRAYER_NOTIFICATIONS -> {
                        Text(
                            text = strings.getString(R.string.onboarding_adhan_footer),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    }
                    else -> Spacer(Modifier.height(10.dp))
                }

                Spacer(Modifier.height(4.dp))

                // 5-DOT PAGE INDICATOR
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    for (i in 1..5) {
                        val isActive = i == currentStepNumber
                        Box(
                            modifier = Modifier
                                .size(if (isActive) 9.dp else 7.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isActive) OnboardingDarkGreen else Color(0xFFCBD5E1)
                                )
                        )
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// STEP 1: LANGUAGE SELECTION (3 Languages Only - Arabic Removed)
// -----------------------------------------------------------------------------
@Composable
private fun LanguageStep(
    selected: AppLanguage,
    onSelect: (AppLanguage) -> Unit,
    strings: AppStrings
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(105.dp))

        Text(
            text = strings.getString(R.string.onboarding_language_title),
            style = MaterialTheme.typography.headlineMedium,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            color = OnboardingDarkGreen,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = strings.getString(R.string.onboarding_language_body),
            style = MaterialTheme.typography.bodyMedium,
            color = OnboardingSubtext,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )

        Spacer(Modifier.weight(1f))

        // Language Selection List Card (3 languages only)
        val languages = listOf(
            Triple(AppLanguage.ENGLISH, "English", "English"),
            Triple(AppLanguage.INDONESIAN, "Bahasa Indonesia", "Indonesian"),
            Triple(AppLanguage.MALAY, "Bahasa Melayu", "Malay")
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            languages.forEach { (lang, title, subtitle) ->
                val isSelected = lang == selected
                Surface(
                    onClick = { onSelect(lang) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = OnboardingCardBg,
                    border = BorderStroke(
                        width = if (isSelected) 1.8.dp else 1.dp,
                        color = if (isSelected) OnboardingDarkGreen else OnboardingCardBorder
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = OnboardingDarkGreen
                            )
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = OnboardingSubtext
                            )
                        }

                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(OnboardingDarkGreen),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.weight(1f))
    }
}

// -----------------------------------------------------------------------------
// STEP 2: WELCOME SCREEN
// -----------------------------------------------------------------------------
@Composable
private fun WelcomeStep(strings: AppStrings) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(105.dp))

        Text(
            text = strings.getString(R.string.onboarding_welcome_subhead),
            style = MaterialTheme.typography.titleLarge,
            fontFamily = FontFamily.Serif,
            color = OnboardingDarkGreen
        )

        Text(
            text = strings.getString(R.string.onboarding_welcome_title),
            style = MaterialTheme.typography.displayLarge.copy(fontSize = 38.sp),
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            color = OnboardingDarkGreen,
            letterSpacing = 3.sp
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = strings.getString(R.string.onboarding_welcome_body),
            style = MaterialTheme.typography.bodyMedium,
            color = OnboardingSubtext,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.weight(1f))
    }
}

// -----------------------------------------------------------------------------
// STEP 3: LOCATION ACCESS
// -----------------------------------------------------------------------------
@Composable
private fun LocationStep(strings: AppStrings) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(105.dp))

        Text(
            text = strings.getString(R.string.onboarding_location_title),
            style = MaterialTheme.typography.headlineMedium,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            color = OnboardingDarkGreen,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = strings.getString(R.string.onboarding_location_body),
            style = MaterialTheme.typography.bodyMedium,
            color = OnboardingSubtext,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )

        Spacer(Modifier.weight(1f))
    }
}

// -----------------------------------------------------------------------------
// STEP 4: NOTIFICATIONS
// -----------------------------------------------------------------------------
@Composable
private fun NotificationStep(strings: AppStrings) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(105.dp))

        Text(
            text = strings.getString(R.string.onboarding_notif_title),
            style = MaterialTheme.typography.headlineMedium,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = strings.getString(R.string.onboarding_notif_body),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.92f),
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )

        Spacer(Modifier.weight(1f))
    }
}

// -----------------------------------------------------------------------------
// STEP 5: ADHAN REMINDERS SELECTION
// -----------------------------------------------------------------------------
@Composable
private fun PrayerNotificationsStep(
    toggles: Map<PrayerType, Boolean>,
    onToggle: (PrayerType, Boolean) -> Unit,
    strings: AppStrings
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(105.dp))

        Text(
            text = strings.getString(R.string.onboarding_adhan_title),
            style = MaterialTheme.typography.headlineMedium,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            color = OnboardingDarkGreen,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = strings.getString(R.string.onboarding_adhan_body),
            style = MaterialTheme.typography.bodyMedium,
            color = OnboardingSubtext,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )

        Spacer(Modifier.weight(1f))

        // Prayer Adhan Reminders Card (with card background like language selection)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = OnboardingCardBg,
            border = BorderStroke(1.dp, OnboardingCardBorder)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                PrayerType.ADZAN_NOTIFICATION_PRAYERS.forEach { type ->
                    val checked = toggles[type] ?: true
                    val prayerName = when (type) {
                        PrayerType.FAJR -> strings.getString(R.string.prayer_fajr)
                        PrayerType.DHUHR -> strings.getString(R.string.prayer_dhuhr)
                        PrayerType.ASR -> strings.getString(R.string.prayer_asr)
                        PrayerType.MAGHRIB -> strings.getString(R.string.prayer_maghrib)
                        PrayerType.ISHA -> strings.getString(R.string.prayer_isha)
                        else -> ""
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = prayerName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = OnboardingDarkGreen
                        )

                        Switch(
                            checked = checked,
                            onCheckedChange = { onToggle(type, it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = OnboardingDarkGreen,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color(0xFFCBD5E1),
                                uncheckedBorderColor = Color.Transparent
                            )
                        )
                    }
                }
            }
        }

        Spacer(Modifier.weight(1f))
    }
}
