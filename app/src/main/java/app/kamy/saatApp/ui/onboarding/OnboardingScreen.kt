package app.kamy.saatApp.ui.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import app.kamy.saatApp.design.theme.SaatColors
import app.kamy.saatApp.domain.model.PrayerType
import app.kamy.saatApp.infrastructure.preferences.AppLanguageStore
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val OnboardingDarkGreen = Color(0xFF1B4332)
private val OnboardingBgWarm = Color(0xFFF9F7F2)
private val OnboardingCardBorder = Color(0xFFEBE5D8)
private val OnboardingSubtext = Color(0xFF64748B)
private val OnboardingTitleGreen = Color(0xFF153828)

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    vm: OnboardingViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isFinalizing by remember { mutableStateOf(false) }

    // Dynamically resolve strings for currently selected language
    val strings = remember(state.selectedLanguage) {
        AppStrings(context.applicationContext, AppLanguageStore.from(context))
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        vm.onLocationPermissionResult(it.values.any { granted -> granted })
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        vm.onNotificationPermissionResult(granted)
    }

    fun finishOnboarding() {
        if (!isFinalizing) {
            isFinalizing = true
            scope.launch {
                vm.completeOnboarding()
                delay(800)
                onFinished()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (state.step) {
            OnboardingStep.LANGUAGE -> {
                LanguageStepScreen(
                    selected = state.selectedLanguage,
                    onSelect = vm::selectLanguage,
                    onStart = {
                        vm.selectLanguage(state.selectedLanguage)
                        vm.nextStep()
                    },
                    onSkip = { finishOnboarding() },
                    strings = strings
                )
            }
            OnboardingStep.WELCOME -> {
                WelcomeStepScreen(
                    language = state.selectedLanguage,
                    onBack = { vm.previousStep() },
                    onContinue = { vm.nextStep() },
                    onSkip = { finishOnboarding() },
                    strings = strings
                )
            }
            OnboardingStep.PERMISSIONS -> {
                PermissionsStepScreen(
                    locationGranted = state.locationGranted,
                    notificationGranted = state.notificationGranted,
                    onRequestLocation = {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    },
                    onRequestNotification = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            vm.onNotificationPermissionResult(true)
                        }
                    },
                    onBack = { vm.previousStep() },
                    onContinue = { vm.nextStep() },
                    onSkip = {
                        vm.skipPermissions()
                    },
                    strings = strings
                )
            }
            OnboardingStep.PRAYER_NOTIFICATIONS -> {
                PrayerNotificationsStepScreen(
                    toggles = state.prayerAdzanToggles,
                    onToggle = vm::togglePrayerAdzan,
                    onBack = { vm.previousStep() },
                    onSave = { finishOnboarding() },
                    onSkip = { finishOnboarding() },
                    strings = strings
                )
            }
        }

        // Fullscreen Loading Overlay when finalizing onboarding
        AnimatedVisibility(
            visible = isFinalizing,
            enter = fadeIn(animationSpec = tween(200)),
            exit = fadeOut(animationSpec = tween(200))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SaatColors.HomeBg),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(SaatColors.PureWhite, CircleShape)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.fillMaxSize(),
                            color = SaatColors.HomeDarkGreen,
                            strokeWidth = 3.dp,
                            trackColor = SaatColors.ArcGold.copy(alpha = 0.25f)
                        )
                        Image(
                            painter = painterResource(R.drawable.ic_tasbih_3d),
                            contentDescription = null,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Text(
                        text = when (state.selectedLanguage) {
                            AppLanguage.INDONESIAN -> "Menyiapkan aplikasi Anda..."
                            AppLanguage.MALAY -> "Menyediakan aplikasi anda..."
                            AppLanguage.ENGLISH -> "Preparing your experience..."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = SaatColors.HomeDarkGreen,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// STEP 1: BRAND INTRO & LANGUAGE SELECTION (Screen 1)
// -----------------------------------------------------------------------------
@Composable
private fun LanguageStepScreen(
    selected: AppLanguage,
    onSelect: (AppLanguage) -> Unit,
    onStart: () -> Unit,
    onSkip: () -> Unit,
    strings: AppStrings
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Full screen background illustration (onboarding1)
        AsyncImage(
            model = R.drawable.bg_onboarding_1,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Section (statusBarsPadding applied here)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Bar: Skip button in White
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onSkip,
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
                    ) {
                        Text(
                            text = strings.getString(R.string.onboarding_skip),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))

                // Title "Sāat" in White
                Text(
                    text = strings.getString(R.string.onboarding_brand_title),
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 42.sp,
                        letterSpacing = 1.5.sp
                    ),
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(4.dp))

                // Subtitle in White
                Text(
                    text = strings.getString(R.string.onboarding_brand_subtitle),
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                    color = Color.White.copy(alpha = 0.92f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 36.dp),
                    lineHeight = 20.sp
                )
            }

            // Flexible middle space so lantern character shines
            Spacer(Modifier.weight(1f))

            // Bottom White Card: Extends fully to bottom screen edge
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                color = Color.White,
                shadowElevation = 12.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(start = 22.dp, end = 22.dp, top = 22.dp, bottom = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Card Header
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = strings.getString(R.string.onboarding_lang_card_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = OnboardingTitleGreen
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = strings.getString(R.string.onboarding_lang_card_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = OnboardingSubtext
                        )
                    }

                    Spacer(Modifier.height(14.dp))

                    // 3 Language Selector Chips (Row) with CIRCULAR FLAGS
                    val languages = listOf(
                        Triple(AppLanguage.INDONESIAN, "Indonesia", R.drawable.ic_flag_id),
                        Triple(AppLanguage.MALAY, "Bahasa Melayu", R.drawable.ic_flag_ms),
                        Triple(AppLanguage.ENGLISH, "English", R.drawable.ic_flag_en)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        languages.forEach { (lang, label, flagRes) ->
                            val isSelected = lang == selected
                            Surface(
                                onClick = { onSelect(lang) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) OnboardingDarkGreen else Color(0xFFFBF9F4),
                                border = if (isSelected) null else BorderStroke(1.dp, Color(0xFFE5DFD3))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    // Circular flag badge
                                    Image(
                                        painter = painterResource(flagRes),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = if (label.length > 10) 11.sp else 12.sp
                                        ),
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else Color(0xFF334155),
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(18.dp))

                    // "Mulai ->" Action Button
                    Button(
                        onClick = onStart,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
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
                                text = strings.getString(R.string.onboarding_start_button),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Step 1 Footer Text
                    Text(
                        text = strings.getString(R.string.onboarding_step1_footer),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = OnboardingSubtext,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// STEP 2: KENALAN DENGAN KĪMI (Screen 2)
// -----------------------------------------------------------------------------
@Composable
private fun WelcomeStepScreen(
    language: AppLanguage,
    onBack: () -> Unit,
    onContinue: () -> Unit,
    onSkip: () -> Unit,
    strings: AppStrings
) {
    val bgRes = when (language) {
        AppLanguage.INDONESIAN -> R.drawable.bg_onboarding_2_id
        AppLanguage.MALAY -> R.drawable.bg_onboarding_2_ms
        AppLanguage.ENGLISH -> R.drawable.bg_onboarding_2_en
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Full screen background illustration with localized speech bubble
        AsyncImage(
            model = bgRes,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Bar: "< Kembali" on left, "Lewati" on right in White
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(
                        onClick = onBack,
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = strings.getString(R.string.onboarding_back),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }

                    TextButton(
                        onClick = onSkip,
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
                    ) {
                        Text(
                            text = strings.getString(R.string.onboarding_skip),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Title: Kenalan dengan Kīmi in White
                Text(
                    text = strings.getString(R.string.onboarding_kimi_title),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    fontFamily = FontFamily.Serif,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(10.dp))

                // Body text in White
                Text(
                    text = strings.getString(R.string.onboarding_kimi_body),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        lineHeight = 21.sp
                    ),
                    color = Color.White.copy(alpha = 0.95f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }

            // Bottom Action Controls
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // "Lanjut ->" Button
                Button(
                    onClick = onContinue,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
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
                            text = strings.getString(R.string.onboarding_continue_btn),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Page Indicator: Dot 2 active
                OnboardingDots(activeStep = 2)
            }
        }
    }
}

// -----------------------------------------------------------------------------
// STEP 3: UNIFIED PERMISSIONS SCREEN (Screen 3)
// -----------------------------------------------------------------------------
@Composable
private fun PermissionsStepScreen(
    locationGranted: Boolean,
    notificationGranted: Boolean,
    onRequestLocation: () -> Unit,
    onRequestNotification: () -> Unit,
    onBack: () -> Unit,
    onContinue: () -> Unit,
    onSkip: () -> Unit,
    strings: AppStrings
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OnboardingBgWarm)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar: "< Kembali" on left, "Lewati" on right
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(
                    onClick = onBack,
                    colors = ButtonDefaults.textButtonColors(contentColor = OnboardingTitleGreen)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = OnboardingTitleGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = strings.getString(R.string.onboarding_back),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = OnboardingTitleGreen
                        )
                    }
                }

                TextButton(
                    onClick = onSkip,
                    colors = ButtonDefaults.textButtonColors(contentColor = OnboardingTitleGreen)
                ) {
                    Text(
                        text = strings.getString(R.string.onboarding_skip),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = OnboardingTitleGreen
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            // Title
            Text(
                text = strings.getString(R.string.onboarding_perms_title),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                ),
                fontFamily = FontFamily.Serif,
                color = OnboardingTitleGreen,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(6.dp))

            // Subtitle
            Text(
                text = strings.getString(R.string.onboarding_perms_subtitle),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                ),
                color = OnboardingSubtext,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(Modifier.height(16.dp))

            // Card 1: Location Access
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = Color.White,
                border = BorderStroke(1.dp, OnboardingCardBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Image(
                            painter = painterResource(R.drawable.ic_onboarding_location),
                            contentDescription = null,
                            modifier = Modifier.size(46.dp)
                        )
                        Spacer(Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = strings.getString(R.string.onboarding_perm_loc_title),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = OnboardingTitleGreen
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = strings.getString(R.string.onboarding_perm_loc_desc),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp
                                ),
                                color = OnboardingSubtext
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    if (locationGranted) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            shape = RoundedCornerShape(50.dp),
                            color = Color(0xFFEDF5EE)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color(0xFF2E7D32),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = strings.getString(R.string.onboarding_perm_loc_granted),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32)
                                )
                            }
                        }
                    } else {
                        Button(
                            onClick = onRequestLocation,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            shape = RoundedCornerShape(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = OnboardingDarkGreen,
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                text = strings.getString(R.string.onboarding_perm_loc_btn),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Card 2: Notification Access
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = Color.White,
                border = BorderStroke(1.dp, OnboardingCardBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Image(
                            painter = painterResource(R.drawable.ic_onboarding_notification),
                            contentDescription = null,
                            modifier = Modifier.size(46.dp)
                        )
                        Spacer(Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = strings.getString(R.string.onboarding_perm_notif_title),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = OnboardingTitleGreen
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = strings.getString(R.string.onboarding_perm_notif_desc),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp
                                ),
                                color = OnboardingSubtext
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    if (notificationGranted) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            shape = RoundedCornerShape(50.dp),
                            color = Color(0xFFEDF5EE)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color(0xFF2E7D32),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = strings.getString(R.string.onboarding_perm_notif_granted),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32)
                                )
                            }
                        }
                    } else {
                        Button(
                            onClick = onRequestNotification,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            shape = RoundedCornerShape(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = OnboardingDarkGreen,
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                text = strings.getString(R.string.onboarding_perm_notif_btn),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Flexible space between cards and bottom section
            Spacer(Modifier.weight(1f))

            // Bottom Section: Button ("Nanti saja" / "Lanjut") positioned right above Privacy Note
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Secondary Action: "Nanti saja" (or "Lanjut" if both permissions granted)
                if (locationGranted && notificationGranted) {
                    Button(
                        onClick = onContinue,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
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
                                text = strings.getString(R.string.onboarding_continue_btn),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                } else {
                    Surface(
                        onClick = onContinue,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(50.dp),
                        color = Color.Transparent,
                        border = BorderStroke(1.dp, Color(0xFFD3CCC0))
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = strings.getString(R.string.onboarding_perm_later_btn),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = OnboardingTitleGreen
                            )
                        }
                    }
                }

                // Privacy Note: directly beneath the button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_onboarding_privacy),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = strings.getString(R.string.onboarding_privacy_text),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        ),
                        color = OnboardingSubtext
                    )
                }

                // Page Indicator: Dot 3 active
                OnboardingDots(activeStep = 3)
            }
        }
    }
}

// -----------------------------------------------------------------------------
// STEP 4: ATUR ADZANMU (Screen 4)
// -----------------------------------------------------------------------------
@Composable
private fun PrayerNotificationsStepScreen(
    toggles: Map<PrayerType, Boolean>,
    onToggle: (PrayerType, Boolean) -> Unit,
    onBack: () -> Unit,
    onSave: () -> Unit,
    onSkip: () -> Unit,
    strings: AppStrings
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OnboardingBgWarm)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Bar: "< Kembali" on left, "Lewati" on right
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(
                        onClick = onBack,
                        colors = ButtonDefaults.textButtonColors(contentColor = OnboardingTitleGreen)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null,
                                tint = OnboardingTitleGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = strings.getString(R.string.onboarding_back),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = OnboardingTitleGreen
                            )
                        }
                    }

                    TextButton(
                        onClick = onSkip,
                        colors = ButtonDefaults.textButtonColors(contentColor = OnboardingTitleGreen)
                    ) {
                        Text(
                            text = strings.getString(R.string.onboarding_skip),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = OnboardingTitleGreen
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))

                // Title: Atur Adzanmu
                Text(
                    text = strings.getString(R.string.onboarding_adhan_new_title),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    fontFamily = FontFamily.Serif,
                    color = OnboardingTitleGreen,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(6.dp))

                // Subtitle
                Text(
                    text = strings.getString(R.string.onboarding_adhan_new_subtitle),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp,
                        lineHeight = 19.sp
                    ),
                    color = OnboardingSubtext,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(Modifier.height(18.dp))

                // Prayers List Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, OnboardingCardBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        val prayers = listOf(
                            Triple(PrayerType.FAJR, R.string.prayer_fajr, R.drawable.ic_onboarding_fajr),
                            Triple(PrayerType.DHUHR, R.string.prayer_dhuhr, R.drawable.ic_onboarding_dhuhr),
                            Triple(PrayerType.ASR, R.string.prayer_asr, R.drawable.ic_onboarding_asr),
                            Triple(PrayerType.MAGHRIB, R.string.prayer_maghrib, R.drawable.ic_onboarding_maghrib),
                            Triple(PrayerType.ISHA, R.string.prayer_isha, R.drawable.ic_onboarding_isha)
                        )

                        prayers.forEachIndexed { index, (type, nameRes, iconRes) ->
                            val checked = toggles[type] ?: true
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Image(
                                        painter = painterResource(iconRes),
                                        contentDescription = null,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(Modifier.width(14.dp))
                                    Text(
                                        text = strings.getString(nameRes),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = OnboardingTitleGreen
                                    )
                                }

                                Switch(
                                    checked = checked,
                                    onCheckedChange = { onToggle(type, it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = Color(0xFF2E7D32),
                                        uncheckedThumbColor = Color.White,
                                        uncheckedTrackColor = Color(0xFFCBD5E1),
                                        uncheckedBorderColor = Color.Transparent
                                    )
                                )
                            }

                            if (index < prayers.lastIndex) {
                                HorizontalDivider(
                                    color = Color(0xFFF3EFE8),
                                    thickness = 1.dp
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Adhan Setting Note Box (Speaker)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFEDF5EE)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = strings.getString(R.string.onboarding_adhan_sound_note),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.sp,
                                lineHeight = 17.sp
                            ),
                            color = OnboardingTitleGreen
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))
            }

            // Bottom Action: "Simpan & Masuk ke Sāat ->" & Page Indicator
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Button(
                    onClick = onSave,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
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
                            text = strings.getString(R.string.onboarding_save_and_enter),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Page Indicator: Dot 4 active
                OnboardingDots(activeStep = 4)
            }
        }
    }
}

// -----------------------------------------------------------------------------
// 4-DOT PAGE INDICATOR
// -----------------------------------------------------------------------------
@Composable
private fun OnboardingDots(activeStep: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 6.dp)
    ) {
        for (i in 1..4) {
            val isActive = i == activeStep
            Box(
                modifier = Modifier
                    .size(if (isActive) 8.dp else 7.dp)
                    .clip(CircleShape)
                    .background(
                        if (isActive) OnboardingDarkGreen else Color(0xFFCBD5E1)
                    )
            )
        }
    }
}
