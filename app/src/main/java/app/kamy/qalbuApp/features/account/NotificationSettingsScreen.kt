package app.kamy.qalbuApp.features.account

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import app.kamy.qalbuApp.design.components.AlKhatibSettingsGroup
import app.kamy.qalbuApp.design.components.AlKhatibSettingsNavigationRow
import app.kamy.qalbuApp.design.components.AlKhatibSettingsToggleRow
import app.kamy.qalbuApp.design.theme.AlKhatibSpacing
import app.kamy.qalbuApp.ui.layout.tabContentStatusBarInset

@Composable
fun NotificationSettingsScreen(
    vm: AccountViewModel,
    onBack: () -> Unit
) {
    val state by vm.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .tabContentStatusBarInset()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = AlKhatibSpacing.screenHorizontal, vertical = AlKhatibSpacing.md),
        verticalArrangement = Arrangement.spacedBy(AlKhatibSpacing.lg)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = "Notifications",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Text(
            text = "Choose which reminders you want. Prayer adhan plays when prayer times are on.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = AlKhatibSpacing.xs)
        )

        NotificationSectionLabel("Quran")
        AlKhatibSettingsGroup {
            AlKhatibSettingsToggleRow(
                icon = Icons.Filled.Notifications,
                title = "Daily verse",
                subtitle = "Morning surah and translation",
                checked = state.dailyVerseEnabled,
                onCheckedChange = vm::setDailyVerseEnabled
            )
            if (state.dailyVerseEnabled) {
                AlKhatibSettingsNavigationRow(
                    icon = Icons.Filled.Schedule,
                    title = "Reminder time",
                    subtitle = state.reminderTimeLabel.ifBlank { "7:00 AM" },
                    onClick = { vm.toggleNotifTimeSheet(true) }
                )
            }
        }

        NotificationSectionLabel("Prayer")
        AlKhatibSettingsGroup {
            AlKhatibSettingsToggleRow(
                icon = Icons.Filled.Notifications,
                title = "Prayer times",
                subtitle = "Fajr, Dhuhr, Asr, Maghrib & Isha with adhan",
                checked = state.adzanEnabled,
                onCheckedChange = vm::setAdzanEnabled
            )
            AlKhatibSettingsToggleRow(
                icon = Icons.Filled.WbTwilight,
                title = "Imsak",
                subtitle = "Before Fajr while fasting",
                checked = state.imsakEnabled,
                onCheckedChange = vm::setImsakEnabled
            )
        }

        NotificationSectionLabel("Night")
        AlKhatibSettingsGroup {
            AlKhatibSettingsToggleRow(
                icon = Icons.Filled.Bedtime,
                title = "Midnight",
                subtitle = "Halfway through the night",
                checked = state.midnightEnabled,
                onCheckedChange = vm::setMidnightEnabled
            )
            AlKhatibSettingsToggleRow(
                icon = Icons.Filled.Bedtime,
                title = "First third",
                subtitle = "Early night rest",
                checked = state.firstThirdEnabled,
                onCheckedChange = vm::setFirstThirdEnabled
            )
            AlKhatibSettingsToggleRow(
                icon = Icons.Filled.Bedtime,
                title = "Last third",
                subtitle = "Best time for tahajud",
                checked = state.tahajudEnabled,
                onCheckedChange = vm::setTahajudEnabled
            )
        }

        NotificationSectionLabel("Sunnah reading")
        AlKhatibSettingsGroup {
            AlKhatibSettingsToggleRow(
                icon = Icons.AutoMirrored.Filled.MenuBook,
                title = "Surah Yasin",
                subtitle = "Thursday night before Jumu'ah",
                checked = state.yasinReminderEnabled,
                onCheckedChange = vm::setYasinReminderEnabled
            )
            AlKhatibSettingsToggleRow(
                icon = Icons.AutoMirrored.Filled.MenuBook,
                title = "Surah Al-Kahf",
                subtitle = "Friday reading reminder",
                checked = state.kahfReminderEnabled,
                onCheckedChange = vm::setKahfReminderEnabled
            )
        }

        Spacer(Modifier.height(AlKhatibSpacing.xl))
    }
}

@Composable
private fun NotificationSectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(start = AlKhatibSpacing.xs)
    )
}
