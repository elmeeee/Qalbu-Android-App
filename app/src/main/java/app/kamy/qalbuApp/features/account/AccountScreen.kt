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
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import app.kamy.qalbuApp.design.theme.AlKhatibColors
import app.kamy.qalbuApp.domain.model.QFTranslation
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
    authService: AuthorizationService
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
            .background(AlKhatibColors.ScreenBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ProfileHeader(
            isSignedIn = state.isSignedIn,
            profile = state.profile,
            isLoading = state.isLoading,
            onSignIn = {
                val intent = oauthService.buildAuthorizationIntent(authService)
                signInLauncher.launch(intent)
            }
        )

        // General
        SectionHeader("General")
        SettingsRow(
            icon = Icons.Filled.TextFields,
            title = "Font size",
            subtitle = "Adjust Arabic & translation",
            onClick = { vm.openFontScale() }
        )

        // Prayer settings
        SectionHeader("Prayer settings")
        SettingsRow(
            icon = Icons.Filled.Schedule,
            title = "Prayer calculation method",
            subtitle = methodLabel(state.prayerMethod),
            onClick = { vm.togglePrayerSheet(true) }
        )
        SettingsRowToggle(
            icon = Icons.AutoMirrored.Filled.MenuBook,
            title = "Show translation",
            checked = state.showTranslation,
            onToggle = vm::setShowTranslation
        )
        SettingsRow(
            icon = Icons.Filled.Translate,
            title = "Translator",
            subtitle = "Tap to choose translation source",
            onClick = { vm.openTranslator() }
        )

        // Notifications
        SectionHeader("Notifications")
        SettingsRowToggle(
            icon = Icons.Filled.Notifications,
            title = "Daily verse reminder",
            checked = state.dailyVerseEnabled,
            onToggle = vm::setDailyVerseEnabled
        )
        if (state.dailyVerseEnabled) {
            SettingsRow(
                icon = Icons.Filled.Schedule,
                title = "Reminder time",
                subtitle = "07:00",
                onClick = { vm.toggleNotifTimeSheet(true) }
            )
        }

        if (state.isSignedIn) {
            Button(
                onClick = { vm.signOut() },
                colors = ButtonDefaults.buttonColors(containerColor = AlKhatibColors.Danger),
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.authBusy
            ) {
                Text("Sign out")
            }
        }
        Spacer(Modifier.height(40.dp))
    }

    // Translator sheet
    if (state.showTranslatorSheet) {
        TranslatorSheet(
            query = state.translatorQuery,
            translations = vm.filteredTranslations,
            isLoading = state.translationsLoading,
            onQueryChange = vm::setTranslatorQuery,
            onPick = { vm.closeTranslator() },
            onDismiss = vm::closeTranslator
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
            onSelect = vm::setPrayerMethod,
            onDismiss = { vm.togglePrayerSheet(false) }
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
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(listOf(Color.White, AlKhatibColors.SageMist))
            )
            .padding(20.dp)
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

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = AlKhatibColors.Slate500,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
    )
}

@Composable
private fun SettingsRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        IconBadge(icon = icon)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(text = title, color = AlKhatibColors.Slate900, fontWeight = FontWeight.SemiBold)
            Text(text = subtitle, color = AlKhatibColors.Slate500, style = MaterialTheme.typography.bodySmall)
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
            contentDescription = null,
            tint = AlKhatibColors.Slate500,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun SettingsRowToggle(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    checked: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .padding(14.dp)
    ) {
        IconBadge(icon = icon)
        Spacer(Modifier.width(12.dp))
        Text(text = title, color = AlKhatibColors.Slate900, modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
        Switch(checked = checked, onCheckedChange = onToggle)
    }
}

@Composable
private fun IconBadge(icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(AlKhatibColors.Teal.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = AlKhatibColors.Teal)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TranslatorSheet(
    query: String,
    translations: List<QFTranslation>,
    isLoading: Boolean,
    onQueryChange: (String) -> Unit,
    onPick: (QFTranslation) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Choose translator", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = AlKhatibColors.DeepEmerald)
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text("Search by name, author, or language") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default,
                singleLine = true
            )
            if (isLoading) Text("Loading…", color = AlKhatibColors.Slate500)
            LazyColumn(modifier = Modifier.fillMaxWidth().height(420.dp)) {
                items(translations, key = { it.id }) { t ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPick(t) }
                            .padding(vertical = 10.dp)
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(t.authorName.ifBlank { t.name }, color = AlKhatibColors.Slate900, fontWeight = FontWeight.SemiBold)
                            Text(
                                text = "${t.languageName} · ${t.name}",
                                color = AlKhatibColors.Slate500,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FontScaleSheet(scale: Float, onScaleChange: (Float) -> Unit, onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
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
private fun PrayerMethodSheet(selected: Int, onSelect: (Int) -> Unit, onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val methods = listOf(
        20 to "Muhammadiyah (Indonesia)",
        2 to "ISNA (North America)",
        3 to "Muslim World League",
        4 to "Umm al-Qura (Saudi Arabia)",
        5 to "Egyptian General Authority",
        7 to "Karachi (University of Islamic Sciences)"
    )
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Calculation method", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = AlKhatibColors.DeepEmerald)
            methods.forEach { (id, label) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selected == id) AlKhatibColors.Teal.copy(alpha = 0.12f) else Color.Transparent)
                        .clickable { onSelect(id); onDismiss() }
                        .padding(14.dp)
                ) {
                    Text(label, color = AlKhatibColors.Slate900, modifier = Modifier.weight(1f))
                    if (selected == id) Text("✓", color = AlKhatibColors.Teal, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private fun methodLabel(method: Int): String = when (method) {
    20 -> "Muhammadiyah"
    2 -> "ISNA"
    3 -> "Muslim World League"
    4 -> "Umm al-Qura"
    5 -> "Egyptian General Authority"
    7 -> "Karachi"
    else -> "Method $method"
}

private fun scaleLabel(scale: Float): String = when {
    scale < 0.95f -> "Small"
    scale < 1.1f -> "Medium"
    scale < 1.25f -> "Large"
    else -> "Extra large"
}
