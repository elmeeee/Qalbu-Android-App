package app.kamy.qalbuApp.features.account

import android.app.Activity
import androidx.activity.ComponentActivity
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import app.kamy.qalbuApp.core.locale.AppLanguage
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    oauthService: OAuthService,
    authService: AuthorizationService,
    onBack: (() -> Unit)? = null
) {
    val vm: AccountViewModel = hiltViewModel()
    val state by vm.state.collectAsState()
    val context = LocalContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    var showNotificationSettings by rememberSaveable { mutableStateOf(false) }

    val signInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data ?: return@rememberLauncherForActivityResult
        val (response, ex) = oauthService.parseRedirect(data)
        if (response != null) {
            scope.launch {
                oauthService.exchangeAuthorizationResponse(authService, response)
                vm.onSignedIn()
            }
        } else if (ex != null && result.resultCode == Activity.RESULT_OK) {
            // Errors surface via vm.error in future iterations.
        }
    }

    LaunchedEffect(state.isSignedIn) {
        if (state.isSignedIn && state.profile == null && !state.isLoading) {
            vm.fetchProfile()
        }
    }

    if (showNotificationSettings) {
        NotificationSettingsScreen(
            vm = vm,
            onBack = { showNotificationSettings = false }
        )
    } else {
        AccountSettingsContent(
            state = state,
            vm = vm,
            onBack = onBack,
            onOpenNotifications = { showNotificationSettings = true },
            onSignIn = {
                val intent = oauthService.buildAuthorizationIntent(authService)
                signInLauncher.launch(intent)
            }
        )
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

    if (state.showLanguageSheet) {
        LanguageSheet(
            selected = state.appLanguage,
            onSelect = { language ->
                vm.setAppLanguage(language)
                (context as? ComponentActivity)?.recreate()
            },
            onDismiss = vm::closeLanguageSheet
        )
    }
}

@Composable
private fun AccountSettingsContent(
    state: AccountUiState,
    vm: AccountViewModel,
    onBack: (() -> Unit)?,
    onOpenNotifications: () -> Unit,
    onSignIn: () -> Unit
) {
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
                    contentDescription = stringResource(R.string.back),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        ProfileHeader(
            isSignedIn = state.isSignedIn,
            profile = state.profile,
            sessionDisplayName = state.sessionDisplayName,
            sessionUsername = state.sessionUsername,
            sessionAvatarUrl = state.sessionAvatarUrl,
            isLoading = state.isLoading,
            onSignIn = onSignIn
        )

        SettingsSectionLabel(stringResource(R.string.general))
        AlKhatibSettingsGroup {
            AlKhatibSettingsNavigationRow(
                icon = Icons.Filled.Translate,
                title = stringResource(R.string.language_settings_title),
                subtitle = stringResource(state.appLanguage.labelRes),
                onClick = { vm.openLanguageSheet() }
            )
            AlKhatibSettingsNavigationRow(
                icon = Icons.Filled.TextFields,
                title = stringResource(R.string.font_size),
                subtitle = stringResource(R.string.font_size_subtitle),
                onClick = { vm.openFontScale() }
            )
        }

        SettingsSectionLabel(stringResource(R.string.prayer_settings))
        AlKhatibSettingsGroup {
            AlKhatibSettingsNavigationRow(
                icon = Icons.Filled.Schedule,
                title = stringResource(R.string.prayer_calculation_method),
                subtitle = state.prayerMethod.organization,
                onClick = { vm.togglePrayerSheet(true) }
            )
            AlKhatibSettingsNavigationRow(
                icon = Icons.AutoMirrored.Filled.VolumeUp,
                title = stringResource(R.string.adhan_voice),
                subtitle = state.selectedAdhanVoice.displayName,
                onClick = { vm.openAdhanSheet() }
            )
            AlKhatibSettingsToggleRow(
                icon = Icons.AutoMirrored.Filled.MenuBook,
                title = stringResource(R.string.show_translation),
                checked = state.showTranslation,
                onCheckedChange = vm::setShowTranslation
            )
            AlKhatibSettingsNavigationRow(
                icon = Icons.Filled.Translate,
                title = stringResource(R.string.translator),
                subtitle = state.selectedTranslationName.ifBlank { stringResource(R.string.translator_hint) },
                onClick = { vm.openTranslator() }
            )
        }

        SettingsSectionLabel(stringResource(R.string.notifications))
        AlKhatibSettingsGroup {
            AlKhatibSettingsNavigationRow(
                icon = Icons.Filled.Notifications,
                title = stringResource(R.string.reminders),
                subtitle = vm.notificationSummary(state),
                onClick = onOpenNotifications
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
                Text(stringResource(R.string.sign_out))
            }
        }
        Spacer(Modifier.height(AlKhatibSpacing.xl))
    }
}

@Composable
private fun ProfileHeader(
    isSignedIn: Boolean,
    profile: app.kamy.qalbuApp.domain.model.UserProfilePayload?,
    sessionDisplayName: String?,
    sessionUsername: String?,
    sessionAvatarUrl: String?,
    isLoading: Boolean,
    onSignIn: () -> Unit
) {
    val avatarUrl = profile?.preferredAvatarUrl ?: sessionAvatarUrl
    val loadingLabel = stringResource(R.string.loading)
    val displayTitle = profile?.displayTitle
        ?: sessionDisplayName
        ?: if (isLoading) loadingLabel else null
    val username = profile?.username ?: sessionUsername
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
            if (avatarUrl != null) {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = displayTitle ?: stringResource(R.string.profile_photo),
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
                    text = displayTitle ?: stringResource(R.string.signed_in),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = AlKhatibColors.DeepEmerald
                )
                username?.let {
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
                    text = stringResource(R.string.sync_reflections),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = AlKhatibColors.DeepEmerald
                )
                Text(
                    text = stringResource(R.string.sign_in_prompt),
                    style = MaterialTheme.typography.bodySmall,
                    color = AlKhatibColors.Slate500
                )
                Spacer(Modifier.height(8.dp))
                Button(onClick = onSignIn) {
                    Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.sign_in))
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
                stringResource(R.string.choose_translator),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = AlKhatibColors.DeepEmerald
            )
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text(stringResource(R.string.search_translator_placeholder), color = AlKhatibColors.Slate500) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default,
                singleLine = true,
                colors = fieldColors
            )
            when {
                isLoading -> Text(stringResource(R.string.loading_translators), color = AlKhatibColors.Slate500)
                error != null -> {
                    Text(error, color = AlKhatibColors.Danger)
                    TextButton(onClick = onRetry) {
                        Text(stringResource(R.string.try_again), color = AlKhatibColors.Teal)
                    }
                }
                translations.isEmpty() -> {
                    Text(stringResource(R.string.no_translators), color = AlKhatibColors.Slate500)
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
                stringResource(R.string.reminder_time_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = AlKhatibColors.DeepEmerald
            )
            Text(
                stringResource(R.string.reminder_time_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = AlKhatibColors.Slate500
            )
            TimePicker(state = timeState)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.cancel), color = AlKhatibColors.Slate500)
                }
                TextButton(onClick = { onSave(timeState.hour, timeState.minute) }) {
                    Text(stringResource(R.string.save), color = AlKhatibColors.Teal, fontWeight = FontWeight.Bold)
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
            Text(stringResource(R.string.font_size), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = AlKhatibColors.DeepEmerald)
            Text("بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ", fontSize = (26 * scale).sp, color = AlKhatibColors.Slate900)
            Text(stringResource(R.string.font_preview_translation), fontSize = (14 * scale).sp, color = AlKhatibColors.Slate800)
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
                stringResource(R.string.calculation_method),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = AlKhatibColors.DeepEmerald
            )
            if (isLoading) {
                Text(stringResource(R.string.loading_methods), color = AlKhatibColors.Slate500)
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
                stringResource(R.string.adhan_voice_title),
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
                        stringResource(R.string.subuh_fajr),
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
                                    stringResource(R.string.default_label),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AlKhatibColors.Slate500
                                )
                            }
                        }
                        IconButton(onClick = { onPreview(voice) }) {
                            val stopPreviewLabel = stringResource(R.string.stop_preview)
                            val previewAdhanLabel = stringResource(R.string.preview_adhan)
                            Icon(
                                imageVector = if (isPreviewing) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                                contentDescription = if (isPreviewing) stopPreviewLabel else previewAdhanLabel,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LanguageSheet(
    selected: AppLanguage,
    onSelect: (AppLanguage) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AlKhatibSpacing.screenHorizontal)
                .padding(bottom = AlKhatibSpacing.xl),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = stringResource(R.string.language_settings_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = AlKhatibColors.DeepEmerald
            )
            Text(
                text = stringResource(R.string.language_settings_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = AlKhatibColors.Slate500,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            AppLanguage.entries.forEach { language ->
                val isSelected = language == selected
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isSelected) AlKhatibColors.Teal.copy(alpha = 0.12f) else Color.Transparent
                        )
                        .clickable { onSelect(language) }
                        .padding(horizontal = 12.dp, vertical = 14.dp)
                ) {
                    Text(
                        text = stringResource(language.labelRes),
                        modifier = Modifier.weight(1f),
                        color = AlKhatibColors.Slate900,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                    if (isSelected) {
                        Text("✓", color = AlKhatibColors.Teal, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun scaleLabel(scale: Float): String {
    val small = stringResource(R.string.font_scale_small)
    val medium = stringResource(R.string.font_scale_medium)
    val large = stringResource(R.string.font_scale_large)
    val extraLarge = stringResource(R.string.font_scale_extra_large)
    return when {
        scale < 0.95f -> small
        scale < 1.1f -> medium
        scale < 1.25f -> large
        else -> extraLarge
    }
}
