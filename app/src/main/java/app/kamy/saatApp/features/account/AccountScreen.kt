package app.kamy.saatApp.features.account

import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Gavel
import app.kamy.saatApp.infrastructure.preferences.AppThemeColor
import app.kamy.saatApp.domain.prayer.PrayerMadhab
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
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
import app.kamy.saatApp.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import app.kamy.saatApp.core.error.AppError
import app.kamy.saatApp.design.components.AlKhatibCard
import app.kamy.saatApp.design.components.AlKhatibCardStyle
import app.kamy.saatApp.design.components.AlKhatibInlineError
import app.kamy.saatApp.design.components.AlKhatibSettingsGroup
import app.kamy.saatApp.design.components.AlKhatibSettingsNavigationRow
import app.kamy.saatApp.design.components.AlKhatibSettingsToggleRow
import app.kamy.saatApp.design.theme.AlKhatibSpacing
import app.kamy.saatApp.core.locale.AppLanguage
import app.kamy.saatApp.ui.common.rememberErrorDisplay
import app.kamy.saatApp.ui.layout.floatingNavBottomPadding
import app.kamy.saatApp.ui.layout.tabContentStatusBarInset
import app.kamy.saatApp.domain.adhan.AdhanVoice
import app.kamy.saatApp.domain.adhan.AdhanVoiceCatalog
import app.kamy.saatApp.domain.model.QFTranslation
import app.kamy.saatApp.domain.prayer.PrayerCalculationMethod
import app.kamy.saatApp.domain.prayer.PrayerMethodOption
import app.kamy.saatApp.infrastructure.auth.OAuthService
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
    val showNotificationSettings = rememberSaveable { mutableStateOf(false) }
    val showAboutDeveloper = rememberSaveable { mutableStateOf(false) }

    val signInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data ?: return@rememberLauncherForActivityResult
        val (response, ex) = oauthService.parseRedirect(data)
        if (response != null) {
            scope.launch {
                try {
                    oauthService.exchangeAuthorizationResponse(authService, response)
                    vm.onSignedIn()
                } catch (t: Throwable) {
                    vm.onSignInFailed(t.message.orEmpty())
                }
            }
        } else if (ex != null) {
            val message = ex.errorDescription ?: ex.message.orEmpty()
            if (message.isNotBlank()) vm.onSignInFailed(message)
        }
    }

    LaunchedEffect(state.isSignedIn) {
        if (state.isSignedIn && state.profile == null && !state.isLoading) {
            vm.fetchProfile()
        }
    }

    if (showNotificationSettings.value) {
        NotificationSettingsScreen(
            vm = vm,
            onBack = { showNotificationSettings.value = false }
        )
    } else {
        AccountSettingsContent(
            state = state,
            vm = vm,
            onBack = onBack,
            onOpenNotifications = { showNotificationSettings.value = true },
            onSignIn = {
                val intent = oauthService.buildAuthorizationIntent(authService)
                signInLauncher.launch(intent)
            },
            onOpenFollowers = vm::openFollowers,
            onOpenAboutDeveloper = { showAboutDeveloper.value = true }
        )
    }

    if (showAboutDeveloper.value) {
        AboutDeveloperSheet(onDismiss = { showAboutDeveloper.value = false })
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
            onPick = { translation ->
                if (vm.selectTranslation(translation)) {
                    (context as? ComponentActivity)?.recreate()
                }
            },
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

    // Theme selection sheet
    if (state.showThemeSheet) {
        ThemeSelectionSheet(
            selected = state.appTheme,
            onSelect = vm::setAppTheme,
            onDismiss = vm::closeThemeSheet
        )
    }

    // Madhab selection sheet
    if (state.showMadhabSheet) {
        MadhabSelectionSheet(
            selected = state.prayerMadhab,
            onSelect = vm::setPrayerMadhab,
            onDismiss = vm::closeMadhabSheet
        )
    }

    // Prayer method sheet
    if (state.showPrayerSheet) {
        PrayerMethodSheet(
            selected = state.prayerMethod,
            methods = state.prayerMethods,
            isLoading = state.prayerMethodsLoading,
            error = state.prayerMethodsError,
            onSelect = vm::setPrayerMethod,
            onRetry = vm::loadPrayerMethods,
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

    if (state.showFollowersSheet) {
        FollowersSheet(
            followers = state.followers,
            isLoading = state.followersLoading,
            isLoadingMore = state.followersLoadingMore,
            error = state.followersError,
            togglingFollowIds = state.togglingFollowFollowerIds,
            onItemRendered = vm::loadMoreFollowersIfNeeded,
            onToggleFollow = vm::toggleFollowFollower,
            onDismiss = vm::closeFollowers,
            onRetry = { vm.loadFollowers(reset = true) }
        )
    }
}

@Composable
private fun AccountSettingsContent(
    state: AccountUiState,
    vm: AccountViewModel,
    onBack: (() -> Unit)?,
    onOpenNotifications: () -> Unit,
    onSignIn: () -> Unit,
    onOpenFollowers: () -> Unit,
    onOpenAboutDeveloper: () -> Unit
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
            error = state.error,
            onRetry = vm::fetchProfile,
            onSignIn = onSignIn,
            onOpenFollowers = onOpenFollowers
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
            AlKhatibSettingsNavigationRow(
                icon = Icons.Filled.Palette,
                title = stringResource(R.string.theme_settings_title),
                subtitle = stringResource(state.appTheme.displayNameRes),
                onClick = { vm.openThemeSheet() }
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
                icon = Icons.Filled.Gavel,
                title = stringResource(R.string.madhab_settings_title),
                subtitle = stringResource(state.prayerMadhab.displayNameRes),
                onClick = { vm.openMadhabSheet() }
            )
            AlKhatibSettingsNavigationRow(
                icon = Icons.AutoMirrored.Filled.VolumeUp,
                title = stringResource(R.string.adhan_voice),
                subtitle = state.selectedAdhanVoice.displayName,
                onClick = { vm.openAdhanSheet() }
            )
        }

        SettingsSectionLabel(stringResource(R.string.reading_settings))
        AlKhatibSettingsGroup {
            AlKhatibSettingsToggleRow(
                icon = Icons.AutoMirrored.Filled.MenuBook,
                title = stringResource(R.string.show_translation),
                checked = state.showTranslation,
                onCheckedChange = vm::setShowTranslation
            )
            AlKhatibSettingsToggleRow(
                icon = Icons.Filled.TextFields,
                title = stringResource(R.string.show_transliteration),
                checked = state.showTransliteration,
                onCheckedChange = vm::setShowTransliteration
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

        SettingsSectionLabel(stringResource(R.string.about))
        val context = LocalContext.current
        val packageInfo = remember {
            try {
                context.packageManager.getPackageInfo(context.packageName, 0)
            } catch (e: Exception) {
                null
            }
        }
        val appVersion = remember(packageInfo) {
            packageInfo?.versionName ?: "1.0.0"
        }
        AlKhatibSettingsGroup {
            AlKhatibSettingsNavigationRow(
                icon = Icons.Outlined.Info,
                title = stringResource(R.string.about_developer),
                subtitle = "Version $appVersion",
                onClick = onOpenAboutDeveloper
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
        Spacer(Modifier.height(floatingNavBottomPadding()))
    }
}

@Composable
private fun ProfileHeader(
    isSignedIn: Boolean,
    profile: app.kamy.saatApp.domain.model.UserProfilePayload?,
    sessionDisplayName: String?,
    sessionUsername: String?,
    sessionAvatarUrl: String?,
    isLoading: Boolean,
    error: AppError?,
    onRetry: () -> Unit,
    onSignIn: () -> Unit,
    onOpenFollowers: () -> Unit
) {
    val avatarUrl = profile?.preferredAvatarUrl ?: sessionAvatarUrl
    val loadingLabel = stringResource(R.string.loading)
    val profileErrorDisplay = error.rememberErrorDisplay(R.string.profile_load_failed)
    val displayTitle = profile?.displayTitle
        ?: sessionDisplayName
        ?: if (isLoading) loadingLabel else null
    val username = profile?.username ?: sessionUsername
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
                shape = RoundedCornerShape(20.dp)
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                    )
                )
        )
        if (isSignedIn) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    if (avatarUrl != null) {
                        AsyncImage(
                            model = avatarUrl,
                            contentDescription = displayTitle ?: stringResource(R.string.profile_photo),
                            modifier = Modifier.fillMaxSize().clip(CircleShape)
                        )
                    } else {
                        Icon(Icons.Filled.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(40.dp))
                    }
                }
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = displayTitle ?: stringResource(R.string.signed_in),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    username?.let {
                        Text(
                            text = "@$it",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    profile?.country?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    val stats = remember(profile) {
                        listOfNotNull(
                            profile?.postsCount?.let { "posts" to it },
                            profile?.followersCount?.let { "followers" to it },
                            profile?.likesCount?.let { "likes" to it }
                        )
                    }

                    if (stats.isNotEmpty()) {
                        Spacer(Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            stats.forEachIndexed { index, pair ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = if (pair.first == "followers") {
                                        Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .clickable { onOpenFollowers() }
                                            .padding(horizontal = 2.dp, vertical = 2.dp)
                                    } else Modifier
                                ) {
                                    Text(
                                        text = "${pair.second}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                    Spacer(Modifier.width(3.dp))
                                    Text(
                                        text = when (pair.first) {
                                            "posts" -> stringResource(R.string.posts)
                                            "followers" -> stringResource(R.string.followers)
                                            else -> stringResource(R.string.likes)
                                        },
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                                if (index < stats.lastIndex) {
                                    Text(
                                        text = "•",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier.padding(horizontal = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                    profileErrorDisplay?.let { display ->
                        Spacer(Modifier.height(8.dp))
                        AlKhatibInlineError(
                            display = display,
                            onRetry = onRetry
                        )
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.02f)
                            )
                        )
                    )
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = stringResource(R.string.sync_reflections),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = stringResource(R.string.sign_in_prompt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }

                Surface(
                    onClick = onSignIn,
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary,
                    tonalElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Login,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.sign_in),
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                profileErrorDisplay?.let { display ->
                    Spacer(Modifier.height(4.dp))
                    AlKhatibInlineError(display = display)
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionLabel(text: String) {
    Row(
        modifier = Modifier.padding(start = 2.dp, bottom = 8.dp, top = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(16.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.primary)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun PremiumSheetHeader(
    title: String,
    subtitle: String? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f)
                    )
                )
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        subtitle?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TranslatorSheet(
    query: String,
    selectedId: Int,
    translations: List<QFTranslation>,
    isLoading: Boolean,
    error: AppError?,
    onQueryChange: (String) -> Unit,
    onPick: (QFTranslation) -> Unit,
    onDismiss: () -> Unit,
    onRetry: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val errorDisplay = error.rememberErrorDisplay(R.string.failed_load_translators)

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = MaterialTheme.colorScheme.onSurface,
        unfocusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
        cursorColor = MaterialTheme.colorScheme.primary,
        focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
        unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
    )
    AlKhatibModalBottomSheet(onDismiss, sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PremiumSheetHeader(
                title = stringResource(R.string.choose_translator)
            )
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text(stringResource(R.string.search_translator_placeholder), color = MaterialTheme.colorScheme.onSurfaceVariant) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default,
                singleLine = true,
                colors = fieldColors
            )
            when {
                isLoading -> Text(stringResource(R.string.loading_translators), color = MaterialTheme.colorScheme.onSurfaceVariant)
                errorDisplay != null -> {
                    AlKhatibInlineError(
                        display = errorDisplay,
                        onRetry = onRetry
                    )
                }
                translations.isEmpty() -> {
                    Text(stringResource(R.string.no_translators), color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                                    if (isSelected) MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f) else Color.Transparent
                                )
                                .clickable { onPick(t) }
                                .padding(horizontal = 12.dp, vertical = 12.dp)
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    t.authorName.ifBlank { t.name },
                                    color = MaterialTheme.colorScheme.onBackground,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${t.languageName.replaceFirstChar { c -> c.titlecase() }} · ${t.name}",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            if (isSelected) {
                                Text("✓", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
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
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                stringResource(R.string.reminder_time_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TimePicker(state = timeState)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.cancel), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                TextButton(onClick = { onSave(timeState.hour, timeState.minute) }) {
                    Text(stringResource(R.string.save), color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
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

@Suppress("SpellCheckingInspection")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FontScaleSheet(scale: Float, onScaleChange: (Float) -> Unit, onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState()
    AlKhatibModalBottomSheet(onDismiss, sheetState) {
        Column(Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(stringResource(R.string.font_size), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text("بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ", fontSize = (26 * scale).sp, color = MaterialTheme.colorScheme.onBackground)
            Text(stringResource(R.string.font_preview_translation), fontSize = (14 * scale).sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("A", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Slider(
                    value = scale,
                    onValueChange = onScaleChange,
                    valueRange = 0.85f..1.35f,
                    steps = 9,
                    modifier = Modifier.weight(1f).padding(horizontal = 12.dp)
                )
                Text("A", fontSize = 22.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(scaleLabel(scale), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PrayerMethodSheet(
    selected: PrayerCalculationMethod,
    methods: List<PrayerMethodOption>,
    isLoading: Boolean,
    error: AppError?,
    onSelect: (PrayerCalculationMethod) -> Unit,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val errorDisplay = error.rememberErrorDisplay(R.string.error_prayer_fetch_title)
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
                color = MaterialTheme.colorScheme.primary
            )
            if (isLoading) {
                Text(stringResource(R.string.loading_methods), color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else if (errorDisplay != null) {
                AlKhatibInlineError(
                    display = errorDisplay,
                    onRetry = onRetry
                )
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
                                    if (isSelected) MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f) else Color.Transparent
                                )
                                .clickable {
                                    onSelect(option.method)
                                    onDismiss()
                                }
                                .padding(14.dp)
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(option.name, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Medium)
                                Text(
                                    option.organization,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (isSelected) {
                                Text("✓", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
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
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                stringResource(R.string.adhan_voice_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        AdhanVoiceCatalog.fajrDisplayName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        stringResource(R.string.adhan_fajr_fixed_note),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
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
                                if (isSelected) MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f) else Color.Transparent
                            )
                            .clickable { onSelect(voice) }
                            .padding(horizontal = 8.dp, vertical = 10.dp)
                    ) {
                        Column(Modifier.weight(1f).padding(horizontal = 6.dp)) {
                            Text(voice.displayName, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Medium)
                            if (voice == AdhanVoice.DEFAULT) {
                                Text(
                                    stringResource(R.string.default_label),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        IconButton(onClick = { onPreview(voice) }) {
                            val stopPreviewLabel = stringResource(R.string.stop_preview)
                            val previewAdhanLabel = stringResource(R.string.preview_adhan)
                            Icon(
                                imageVector = if (isPreviewing) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                                contentDescription = if (isPreviewing) stopPreviewLabel else previewAdhanLabel,
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                        if (isSelected) {
                            Text("✓", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 8.dp))
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
            PremiumSheetHeader(
                title = stringResource(R.string.language_settings_title),
                subtitle = stringResource(R.string.language_settings_subtitle)
            )
            AppLanguage.entries.forEach { language ->
                val isSelected = language == selected
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f) else Color.Transparent
                        )
                        .clickable { onSelect(language) }
                        .padding(horizontal = 12.dp, vertical = 14.dp)
                ) {
                    Text(
                        text = stringResource(language.labelRes),
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                    if (isSelected) {
                        Text("✓", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FollowersSheet(
    followers: List<app.kamy.saatApp.domain.model.UserProfilePayload>,
    isLoading: Boolean,
    isLoadingMore: Boolean,
    error: AppError?,
    togglingFollowIds: Set<String>,
    onItemRendered: (Int) -> Unit,
    onToggleFollow: (String) -> Unit,
    onDismiss: () -> Unit,
    onRetry: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val errorDisplay = error.rememberErrorDisplay(R.string.followers_load_failed)

    AlKhatibModalBottomSheet(onDismiss, sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            PremiumSheetHeader(
                title = stringResource(R.string.followers_title)
            )
            Spacer(Modifier.height(12.dp))

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
                errorDisplay != null -> {
                    AlKhatibInlineError(
                        display = errorDisplay,
                        onRetry = onRetry
                    )
                    Spacer(Modifier.height(24.dp))
                }
                followers.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.no_followers),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                else -> {
                    val listState = rememberLazyListState()
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 420.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(followers, key = { _, f -> f.id }) { index, follower ->
                            LaunchedEffect(index) {
                                onItemRendered(index)
                            }
                            FollowerRow(
                                follower = follower,
                                isTogglingFollow = follower.id in togglingFollowIds,
                                onToggleFollow = { onToggleFollow(follower.id) }
                            )
                        }
                        if (isLoadingMore) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        color = MaterialTheme.colorScheme.primary,
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun FollowerRow(
    follower: app.kamy.saatApp.domain.model.UserProfilePayload,
    isTogglingFollow: Boolean,
    onToggleFollow: () -> Unit
) {
    val avatarUrl = follower.avatarUrls?.medium ?: follower.avatarUrls?.small ?: follower.avatarUrls?.large
    val displayTitle = follower.firstName?.let { fn ->
        val ln = follower.lastName.orEmpty()
        if (ln.isNotBlank()) "$fn $ln" else fn
    } ?: follower.username ?: ""

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (avatarUrl != null) {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = displayTitle,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = displayTitle,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodyMedium
            )
            follower.username?.let {
                Text(
                    text = "@$it",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        Spacer(Modifier.width(8.dp))
        FollowButton(
            followed = follower.followed == true,
            loading = isTogglingFollow,
            onClick = onToggleFollow
        )
    }
}

@Composable
private fun FollowButton(
    followed: Boolean,
    loading: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(
                if (followed) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            )
            .border(
                width = 1.dp,
                color = if (followed) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                shape = RoundedCornerShape(50)
            )
            .clickable(enabled = !loading, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        if (loading) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 1.5.dp,
                modifier = Modifier.size(14.dp)
            )
        } else {
            Text(
                text = if (followed) stringResource(R.string.following) else stringResource(R.string.follow),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutDeveloperSheet(onDismiss: () -> Unit) {
    val context = LocalContext.current
    
    val openLink = { url: String ->
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    }

    app.kamy.saatApp.design.components.AlKhatibPartialBottomSheet(
        onDismiss = onDismiss,
        maxHeightFraction = 0.85f,
        scrollContent = false
    ) {
        val packageInfo = remember {
            try {
                context.packageManager.getPackageInfo(context.packageName, 0)
            } catch (e: Exception) {
                null
            }
        }
        val appVersion = remember(packageInfo) {
            packageInfo?.versionName ?: "1.0.0"
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App Icon / Initial
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.primaryContainer
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "SĀ",
                        color = MaterialTheme.colorScheme.onPrimary,
            Text(
                text = "SĀAT",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Version $appVersion",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // App Details Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.app_details_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(R.string.about_app_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(24.dp))

            // Developer Section Title
            Text(
                text = stringResource(R.string.developer_details_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Developer Avatar
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.secondary,
                                MaterialTheme.colorScheme.primary
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "SE",
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.developer_name),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = stringResource(R.string.developer_role),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Developer Bio
            Text(
                text = stringResource(R.string.developer_bio),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Achievement Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.developer_achievement_title),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.developer_achievement),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Links / Contact info
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AboutLinkRow(
                    label = stringResource(R.string.contact_linkedin),
                    url = "https://www.linkedin.com/in/elmysf/",
                    onClick = openLink
                )
                AboutLinkRow(
                    label = stringResource(R.string.contact_website),
                    url = "https://elmee.my",
                    onClick = openLink
                )
                AboutLinkRow(
                    label = stringResource(R.string.contact_github),
                    url = "https://github.com/elmeeee",
                    onClick = openLink
                )
                AboutLinkRow(
                    label = stringResource(R.string.contact_email),
                    url = "mailto:hello@elmee.my",
                    onClick = openLink
                )
            }
            
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
private fun AboutLinkRow(
    label: String,
    url: String,
    onClick: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick(url) }
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Filled.Login,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemeSelectionSheet(
    selected: AppThemeColor,
    onSelect: (AppThemeColor) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    AlKhatibModalBottomSheet(onDismiss, sheetState) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.theme_settings_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = stringResource(R.string.theme_settings_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AppThemeColor.values().forEach { theme ->
                    val isSelected = theme == selected
                    val dotColor = when (theme) {
                        AppThemeColor.EMERALD -> MaterialTheme.colorScheme.primary
                        AppThemeColor.INDIGO -> MaterialTheme.colorScheme.secondary
                        AppThemeColor.GOLD -> MaterialTheme.colorScheme.tertiary
                    }
                    Surface(
                        onClick = { onSelect(theme) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                        } else {
                            Color.Transparent
                        },
                        border = if (isSelected) {
                            BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                        } else {
                            null
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Theme Color Indicator Dot
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .background(dotColor, CircleShape)
                            )
                            Spacer(Modifier.width(16.dp))
                            Text(
                                text = stringResource(theme.displayNameRes),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.weight(1f)
                            )
                            if (isSelected) {
                                Text(
                                    text = "✓",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MadhabSelectionSheet(
    selected: PrayerMadhab,
    onSelect: (PrayerMadhab) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    AlKhatibModalBottomSheet(onDismiss, sheetState) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.madhab_settings_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = stringResource(R.string.madhab_settings_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PrayerMadhab.values().forEach { madhab ->
                    val isSelected = madhab == selected
                    Surface(
                        onClick = { onSelect(madhab) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                        } else {
                            Color.Transparent
                        },
                        border = if (isSelected) {
                            BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                        } else {
                            null
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(madhab.displayNameRes),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.weight(1f)
                            )
                            if (isSelected) {
                                Text(
                                    text = "✓",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

