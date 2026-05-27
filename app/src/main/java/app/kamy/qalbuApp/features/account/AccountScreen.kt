package app.kamy.qalbuApp.features.account

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import app.kamy.qalbuApp.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import app.kamy.qalbuApp.design.components.AlKhatibCard
import app.kamy.qalbuApp.design.components.AlKhatibCardStyle
import app.kamy.qalbuApp.design.components.AlKhatibSettingsGroup
import app.kamy.qalbuApp.design.components.AlKhatibSettingsNavigationRow
import app.kamy.qalbuApp.design.components.AlKhatibSettingsToggleRow
import app.kamy.qalbuApp.design.theme.AlKhatibColors
import app.kamy.qalbuApp.design.theme.AlKhatibSpacing
import app.kamy.qalbuApp.ui.layout.tabContentStatusBarInset
import app.kamy.qalbuApp.domain.adhan.AdhanVoice
import app.kamy.qalbuApp.domain.adhan.AdhanVoiceCatalog
import app.kamy.qalbuApp.domain.model.QFTranslation
import app.kamy.qalbuApp.domain.prayer.PrayerCalculationMethod
import app.kamy.qalbuApp.domain.prayer.PrayerMethodOption
import app.kamy.qalbuApp.infrastructure.auth.OAuthService
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import net.openid.appauth.AuthorizationService

/**
 * Mirrors iOS Features/Settings/Views/ProfileView.swift. Provides sign in/out,
 * profile header, and preference rows (translator, prayer method, notifications).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    oauthService: OAuthService,
    authService: AuthorizationService,
    onBack: (() -> Unit)? = null
) {
    val vm: AccountViewModel = hiltViewModel()
    val state by vm.state.collectAsState()
    val scope = androidx.compose.runtime.rememberCoroutineScope()

    val signInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data ?: return@rememberLauncherForActivityResult
        val (response, ex) = oauthService.parseRedirect(data)
        if (response != null) {
            scope.launch { oauthService.exchangeAuthorizationResponse(authService, response) }
        } else if (ex != null && result.resultCode == Activity.RESULT_OK) {
            // Errors surface via vm.error in future iterations.
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .tabContentStatusBarInset()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = AlKhatibSpacing.screenHorizontal, vertical = AlKhatibSpacing.md),
        verticalArrangement = Arrangement.spacedBy(AlKhatibSpacing.lg)
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        ProfileHeader(
            isSignedIn = state.isSignedIn,
            profile = state.profile,
            isLoading = state.isLoading,
            onSignIn = {
                val intent = oauthService.buildAuthorizationIntent(authService)
                signInLauncher.launch(intent)
            }
        )

        SettingsSectionLabel("General")
        AlKhatibSettingsGroup {
            AlKhatibSettingsNavigationRow(
                icon = Icons.Filled.TextFields,
                title = "Font size",
                subtitle = "Adjust Arabic & translation",
                onClick = { vm.openFontScale() }
            )
        }

        SettingsSectionLabel("Prayer settings")
        AlKhatibSettingsGroup {
            AlKhatibSettingsNavigationRow(
                icon = Icons.Filled.Schedule,
                title = "Prayer calculation method",
                subtitle = state.prayerMethod.organization,
                onClick = { vm.togglePrayerSheet(true) }
            )
            AlKhatibSettingsNavigationRow(
                icon = Icons.AutoMirrored.Filled.VolumeUp,
                title = "Adhan voice",
                subtitle = state.selectedAdhanVoice.displayName,
                onClick = { vm.openAdhanSheet() }
            )
            AlKhatibSettingsToggleRow(
                icon = Icons.AutoMirrored.Filled.MenuBook,
                title = "Show translation",
                checked = state.showTranslation,
                onCheckedChange = vm::setShowTranslation
            )
            AlKhatibSettingsNavigationRow(
                icon = Icons.Filled.Translate,
                title = "Translator",
                subtitle = state.selectedTranslationName.ifBlank { "Tap to choose translation source" },
                onClick = { vm.openTranslator() }
            )
        }

        SettingsSectionLabel("Notifications")
        AlKhatibSettingsGroup {
            AlKhatibSettingsToggleRow(
                icon = Icons.Filled.Notifications,
                title = "Daily verse reminder · Today's surah & translation",
                checked = state.dailyVerseEnabled,
                onCheckedChange = vm::setDailyVerseEnabled
            )
            if (state.dailyVerseEnabled) {
                AlKhatibSettingsNavigationRow(
                    icon = Icons.Filled.Schedule,
                    title = "Morning time",
                    subtitle = state.reminderTimeLabel.ifBlank { "07:00" },
                    onClick = { vm.toggleNotifTimeSheet(true) }
                )
            }
            AlKhatibSettingsToggleRow(
                icon = Icons.Filled.Notifications,
                title = "Prayer times · Fajr, Dhuhr, Asr, Maghrib & Isha",
                checked = state.adzanEnabled,
                onCheckedChange = vm::setAdzanEnabled
            )
            AlKhatibSettingsToggleRow(
                icon = Icons.Filled.Schedule,
                title = "Imsak · Reminder before Fajr while fasting",
                checked = state.imsakEnabled,
                onCheckedChange = vm::setImsakEnabled
            )
            AlKhatibSettingsToggleRow(
                icon = Icons.Filled.Schedule,
                title = "Midnight · Halfway through the night",
                checked = state.midnightEnabled,
                onCheckedChange = vm::setMidnightEnabled
            )
            AlKhatibSettingsToggleRow(
                icon = Icons.Filled.Schedule,
                title = "First third of night · Early night rest",
                checked = state.firstThirdEnabled,
                onCheckedChange = vm::setFirstThirdEnabled
            )
            AlKhatibSettingsToggleRow(
                icon = Icons.Filled.Schedule,
                title = "Last third (Tahajud) · Best time for night prayer",
                checked = state.tahajudEnabled,
                onCheckedChange = vm::setTahajudEnabled
            )
            AlKhatibSettingsToggleRow(
                icon = Icons.AutoMirrored.Filled.MenuBook,
                title = "Surah Yasin · Thursday night before Jumu'ah",
                checked = state.yasinReminderEnabled,
                onCheckedChange = vm::setYasinReminderEnabled
            )
            AlKhatibSettingsToggleRow(
                icon = Icons.AutoMirrored.Filled.MenuBook,
                title = "Surah Al-Kahf · Friday reading reminder",
                checked = state.kahfReminderEnabled,
                onCheckedChange = vm::setKahfReminderEnabled
            )
        }

        if (state.isSignedIn) {
            Button(
                onClick = { vm.signOut() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.authBusy,
                shape = MaterialTheme.shapes.large
            ) {
                Text("Sign out")
            }
        }
        Spacer(Modifier.height(AlKhatibSpacing.xl))
    }

    // Translator sheet
    if (state.showTranslatorSheet) {
        val filteredTranslations = remember(state.translations, state.translatorQuery) {
            val q = state.translatorQuery.trim().lowercase()
            if (q.isEmpty()) {
                state.translations
            } else {
                state.translations.filter {
                    it.name.lowercase().contains(q) ||
                        it.authorName.lowercase().contains(q) ||
                        it.languageName.lowercase().contains(q)
                }
            }
        }
        TranslatorSheet(
            query = state.translatorQuery,
            selectedId = state.selectedTranslationId,
            translations = filteredTranslations,
            isLoading = state.translationsLoading,
            error = state.translationsError,
            onQueryChange = vm::setTranslatorQuery,
            onPick = vm::selectTranslation,
            onDismiss = vm::closeTranslator,
            onRetry = vm::loadTranslations
        )
    }

    if (state.showNotifTimeSheet) {
        ReminderTimeSheet(
            hour = state.reminderHour,
            minute = state.reminderMinute,
            onSave = vm::saveReminderTime,
            onDismiss = { vm.toggleNotifTimeSheet(false) }
        )
    }

    // Font scale sheet
    if (state.showFontScaleSheet) {
        FontScaleSheet(
            scale = state.fontScale,
            onScaleChange = vm::setFontScale,
            onDismiss = vm::closeFontScale
        )
    }

    // Prayer method sheet
    if (state.showPrayerSheet) {
        PrayerMethodSheet(
            selected = state.prayerMethod,
            methods = state.prayerMethods,
            isLoading = state.prayerMethodsLoading,
            onSelect = vm::setPrayerMethod,
            onDismiss = { vm.togglePrayerSheet(false) }
        )
    }

    if (state.showAdhanSheet) {
        AdhanVoiceSheet(
            selected = state.selectedAdhanVoice,
            previewingVoiceId = state.previewingAdhanVoiceId,
            onSelect = vm::selectAdhanVoice,
            onPreview = vm::toggleAdhanPreview,
            onDismiss = vm::closeAdhanSheet
        )
    }
}

@Composable
private fun ProfileHeader(
    isSignedIn: Boolean,
    profile: app.kamy.qalbuApp.domain.model.UserProfilePayload?,
    isLoading: Boolean,
    onSignIn: () -> Unit
) {
    AlKhatibCard(
        modifier = Modifier.fillMaxWidth(),
        style = AlKhatibCardStyle.Filled,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(AlKhatibColors.LightGrey),
            contentAlignment = Alignment.Center
        ) {
            if (profile?.preferredAvatarUrl != null) {
                AsyncImage(
                    model = profile.preferredAvatarUrl,
                    contentDescription = profile.displayTitle,
                    modifier = Modifier.fillMaxSize().clip(CircleShape)
                )
            } else {
                Icon(Icons.Filled.Person, contentDescription = null, tint = AlKhatibColors.Slate500, modifier = Modifier.size(40.dp))
            }
        }
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            if (isSignedIn) {
                Text(
                    text = profile?.displayTitle ?: if (isLoading) "Loading…" else "Signed in",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = AlKhatibColors.DeepEmerald
                )
                profile?.username?.let {
                    Text(
                        text = "@$it",
                        style = MaterialTheme.typography.bodySmall,
                        color = AlKhatibColors.Slate500
                    )
                }
                profile?.country?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelMedium,
                        color = AlKhatibColors.Teal
                    )
                }
            } else {
                Text(
                    text = "Sync Reflections",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = AlKhatibColors.DeepEmerald
                )
                Text(
                    text = "Sign in to back up your reflections and join the community.",
                    style = MaterialTheme.typography.bodySmall,
                    color = AlKhatibColors.Slate500
                )
                Spacer(Modifier.height(8.dp))
                Button(onClick = onSignIn) {
                    Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Sign in")
                }
            }
        }
    }
    }
}

@Composable
private fun SettingsSectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(start = AlKhatibSpacing.xs)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TranslatorSheet(
    query: String,
    selectedId: Int,
    translations: List<QFTranslation>,
    isLoading: Boolean,
    error: String?,
    onQueryChange: (String) -> Unit,
    onPick: (QFTranslation) -> Unit,
    onDismiss: () -> Unit,
    onRetry: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = AlKhatibColors.Slate900,
        unfocusedTextColor = AlKhatibColors.Slate900,
        focusedBorderColor = AlKhatibColors.Teal,
        unfocusedBorderColor = AlKhatibColors.Slate500,
        cursorColor = AlKhatibColors.Teal,
        focusedPlaceholderColor = AlKhatibColors.Slate500,
        unfocusedPlaceholderColor = AlKhatibColors.Slate500
    )
    AlKhatibModalBottomSheet(onDismiss, sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Choose translator",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = AlKhatibColors.DeepEmerald
            )
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text("Search by name, author, or language", color = AlKhatibColors.Slate500) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default,
                singleLine = true,
                colors = fieldColors
            )
            when {
                isLoading -> Text("Loading translators…", color = AlKhatibColors.Slate500)
                error != null -> {
                    Text(error, color = AlKhatibColors.Danger)
                    TextButton(onClick = onRetry) {
                        Text("Try again", color = AlKhatibColors.Teal)
                    }
                }
                translations.isEmpty() -> {
                    Text("No translators found.", color = AlKhatibColors.Slate500)
                }
                else -> {
                    translations.forEach { t ->
                        val isSelected = t.id == selectedId
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSelected) AlKhatibColors.Teal.copy(alpha = 0.12f) else Color.Transparent
                                )
                                .clickable { onPick(t) }
                                .padding(horizontal = 12.dp, vertical = 12.dp)
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    t.authorName.ifBlank { t.name },
                                    color = AlKhatibColors.Slate900,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${t.languageName.replaceFirstChar { c -> c.titlecase() }} · ${t.name}",
                                    color = AlKhatibColors.Slate500,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            if (isSelected) {
                                Text("✓", color = AlKhatibColors.Teal, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderTimeSheet(
    hour: Int,
    minute: Int,
    onSave: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val timeState = rememberTimePickerState(
        initialHour = hour,
        initialMinute = minute,
        is24Hour = false
    )
    AlKhatibModalBottomSheet(onDismiss, sheetState) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Reminder time",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = AlKhatibColors.DeepEmerald
            )
            Text(
                "Choose when you want your daily verse reminder.",
                style = MaterialTheme.typography.bodyMedium,
                color = AlKhatibColors.Slate500
            )
            TimePicker(state = timeState)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = AlKhatibColors.Slate500)
                }
                TextButton(onClick = { onSave(timeState.hour, timeState.minute) }) {
                    Text("Save", color = AlKhatibColors.Teal, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AlKhatibModalBottomSheet(
    onDismiss: () -> Unit,
    sheetState: androidx.compose.material3.SheetState,
    content: @Composable () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        content = { content() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FontScaleSheet(scale: Float, onScaleChange: (Float) -> Unit, onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState()
    AlKhatibModalBottomSheet(onDismiss, sheetState) {
        Column(Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Font size", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = AlKhatibColors.DeepEmerald)
            Text("بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ", fontSize = (26 * scale).sp, color = AlKhatibColors.Slate900)
            Text("In the name of God, the Most Gracious, the Most Merciful.", fontSize = (14 * scale).sp, color = AlKhatibColors.Slate800)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("A", fontSize = 14.sp, color = AlKhatibColors.Slate500)
                Slider(
                    value = scale,
                    onValueChange = onScaleChange,
                    valueRange = 0.85f..1.35f,
                    steps = 9,
                    modifier = Modifier.weight(1f).padding(horizontal = 12.dp)
                )
                Text("A", fontSize = 22.sp, color = AlKhatibColors.Slate500)
            }
            Text(scaleLabel(scale), color = AlKhatibColors.Slate500)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PrayerMethodSheet(
    selected: PrayerCalculationMethod,
    methods: List<PrayerMethodOption>,
    isLoading: Boolean,
    onSelect: (PrayerCalculationMethod) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    AlKhatibModalBottomSheet(onDismiss, sheetState) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Calculation method",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = AlKhatibColors.DeepEmerald
            )
            if (isLoading) {
                Text("Loading methods…", color = AlKhatibColors.Slate500)
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(420.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(methods, key = { "${it.apiKey}-${it.aladhanId}" }) { option ->
                        val isSelected = selected == option.method
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSelected) AlKhatibColors.Teal.copy(alpha = 0.12f) else Color.Transparent
                                )
                                .clickable {
                                    onSelect(option.method)
                                    onDismiss()
                                }
                                .padding(14.dp)
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(option.name, color = AlKhatibColors.Slate900, fontWeight = FontWeight.Medium)
                                Text(
                                    option.organization,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AlKhatibColors.Slate500
                                )
                            }
                            if (isSelected) {
                                Text("✓", color = AlKhatibColors.Teal, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdhanVoiceSheet(
    selected: AdhanVoice,
    previewingVoiceId: String?,
    onSelect: (AdhanVoice) -> Unit,
    onPreview: (AdhanVoice) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    AlKhatibModalBottomSheet(onDismiss, sheetState) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Adhan voice",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = AlKhatibColors.DeepEmerald
            )
            Text(
                stringResource(R.string.adhan_voice_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = AlKhatibColors.Slate500
            )
            AlKhatibCard(
                modifier = Modifier.fillMaxWidth(),
                style = AlKhatibCardStyle.Filled,
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "Subuh (Fajr)",
                        fontWeight = FontWeight.SemiBold,
                        color = AlKhatibColors.Slate900
                    )
                    Text(
                        AdhanVoiceCatalog.fajrDisplayName,
                        style = MaterialTheme.typography.bodySmall,
                        color = AlKhatibColors.Slate500
                    )
                    Text(
                        stringResource(R.string.adhan_fajr_fixed_note),
                        style = MaterialTheme.typography.bodySmall,
                        color = AlKhatibColors.Teal
                    )
                }
            }
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(360.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(AdhanVoice.selectable, key = { it.id }) { voice ->
                    val isSelected = voice == selected
                    val isPreviewing = previewingVoiceId == voice.id
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isSelected) AlKhatibColors.Teal.copy(alpha = 0.12f) else Color.Transparent
                            )
                            .clickable { onSelect(voice) }
                            .padding(horizontal = 8.dp, vertical = 10.dp)
                    ) {
                        Column(Modifier.weight(1f).padding(horizontal = 6.dp)) {
                            Text(voice.displayName, color = AlKhatibColors.Slate900, fontWeight = FontWeight.Medium)
                            if (voice == AdhanVoice.DEFAULT) {
                                Text(
                                    "Default",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AlKhatibColors.Slate500
                                )
                            }
                        }
                        IconButton(onClick = { onPreview(voice) }) {
                            Icon(
                                imageVector = if (isPreviewing) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                                contentDescription = if (isPreviewing) "Stop preview" else "Preview adhan",
                                tint = AlKhatibColors.Teal
                            )
                        }
                        if (isSelected) {
                            Text("✓", color = AlKhatibColors.Teal, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 8.dp))
                        }
                    }
                }
            }
        }
    }
}

private fun scaleLabel(scale: Float): String = when {
    scale < 0.95f -> "Small"
    scale < 1.1f -> "Medium"
    scale < 1.25f -> "Large"
    else -> "Extra large"
}
