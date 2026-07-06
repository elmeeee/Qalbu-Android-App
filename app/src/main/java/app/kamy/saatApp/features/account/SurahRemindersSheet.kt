package app.kamy.saatApp.features.account

import android.app.TimePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import app.kamy.saatApp.design.theme.AlKhatibColors
import app.kamy.saatApp.core.locale.AppLanguage
import app.kamy.saatApp.infrastructure.preferences.AppLanguageStore
import app.kamy.saatApp.infrastructure.preferences.SurahReminder
import java.util.Calendar
import java.util.UUID

private val SURAH_NAMES = listOf(
    "Al-Fatihah", "Al-Baqarah", "Ali 'Imran", "An-Nisa'", "Al-Ma'idah", "Al-An'am", "Al-A'raf", "Al-Anfal", "At-Tawbah",
    "Yunus", "Hud", "Yusuf", "Ar-Ra'd", "Ibrahim", "Al-Hijr", "An-Nahl", "Al-Isra'", "Al-Kahf", "Maryam",
    "Ta-Ha", "Al-Anbiya'", "Al-Hajj", "Al-Mu'minun", "An-Nur", "Al-Furqan", "Ash-Shu'ara'", "An-Naml", "Al-Qasas",
    "Al-'Ankabut", "Ar-Rum", "Luqman", "As-Sajdah", "Al-Ahzab", "Saba'", "Fatir", "Ya-Sin", "As-Saffat",
    "Sad", "Az-Zumar", "Ghafir", "Fussilat", "Ash-Shura", "Az-Zukhruf", "Ad-Dukhan", "Al-Jathiyah", "Al-Ahqaf",
    "Muhammad", "Al-Fath", "Al-Hujurat", "Qaf", "Adh-Dhariyat", "At-Tur", "An-Najm", "Al-Qamar", "Ar-Rahman",
    "Al-Waqi'ah", "Al-Hadid", "Al-Mujadilah", "Al-Hashr", "Al-Mumtahanah", "As-Saff", "Al-Jumu'ah", "Al-Munafiqun",
    "Al-Taghabun", "At-Talaq", "At-Tahrim", "Al-Mulk", "Al-Qalam", "Al-Haqqah", "Al-Ma'arij", "Nuh", "Al-Jinn",
    "Al-Muzzammil", "Al-Muddaththir", "Al-Qiyamah", "Al-Insan", "Al-Mursalat", "An-Naba'", "An-Nazi'at", "'Abasa",
    "At-Takwir", "Al-Infitar", "Al-Mutaffifin", "Al-Inshiqaq", "Al-Buruj", "At-Tariq", "Al-A'la", "Al-Ghashiyah",
    "Al-Fajr", "Al-Balad", "Ash-Shams", "Al-Layl", "Ad-Duha", "Ash-Sharh", "At-Tin", "Al-'Alaq", "Al-Qadr",
    "Al-Bayyinah", "Al-Zalzalah", "Al-'Adiyat", "Al-Qari'ah", "At-Takathur", "Al-'Asr", "Al-Humazah", "Al-Fil",
    "Quraysh", "Al-Ma'un", "Al-Kauthar", "Al-Kafirun", "An-Nasr", "Al-Masad", "Al-Ikhlas", "Al-Falaq", "An-Nas"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurahRemindersSheet(
    reminders: List<SurahReminder>,
    onToggle: (String, Boolean) -> Unit,
    onAdd: (SurahReminder) -> Unit,
    onUpdate: (SurahReminder) -> Unit,
    onDelete: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    val context = LocalContext.current
    val languageStore = remember { AppLanguageStore.from(context) }
    val isIndoMalay = languageStore.current() == AppLanguage.INDONESIAN || languageStore.current() == AppLanguage.MALAY

    var showAddForm by remember { mutableStateOf(false) }
    var reminderToEdit by remember { mutableStateOf<SurahReminder?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AlKhatibColors.OffWhite,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isIndoMalay) "Pengingat Baca Surah" else "Surah Reminders",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = AlKhatibColors.DeepEmerald
                )
                IconButton(
                    onClick = { showAddForm = true },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = AlKhatibColors.DeepEmerald.copy(alpha = 0.1f)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Reminder",
                        tint = AlKhatibColors.DeepEmerald
                    )
                }
            }

            Text(
                text = if (isIndoMalay) {
                    "Atur pengingat membaca Surah pilihan Anda (seperti Yasin, Al-Kahf, Al-Mulk, dll) secara rutin. Klik kartu untuk mengedit."
                } else {
                    "Configure recurring reminders to read your chosen Surahs (e.g. Yasin, Al-Kahf, Al-Mulk, etc.). Click card to edit."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = AlKhatibColors.Slate700
            )

            if (reminders.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isIndoMalay) "Belum ada pengingat kustom." else "No custom reminders set.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AlKhatibColors.Slate500,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(reminders) { reminder ->
                        SurahReminderItemRow(
                            reminder = reminder,
                            isIndoMalay = isIndoMalay,
                            onToggle = { onToggle(reminder.id, it) },
                            onDelete = { onDelete(reminder.id) },
                            onEdit = { reminderToEdit = reminder }
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }

    if (showAddForm || reminderToEdit != null) {
        AddEditSurahReminderDialog(
            reminder = reminderToEdit,
            isIndoMalay = isIndoMalay,
            onSave = { reminder ->
                if (reminderToEdit != null) {
                    onUpdate(reminder)
                } else {
                    onAdd(reminder)
                }
                showAddForm = false
                reminderToEdit = null
            },
            onDismiss = {
                showAddForm = false
                reminderToEdit = null
            }
        )
    }
}

@Composable
private fun SurahReminderItemRow(
    reminder: SurahReminder,
    isIndoMalay: Boolean,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() },
        shape = RoundedCornerShape(12.dp),
        color = AlKhatibColors.PureWhite,
        border = BorderStroke(1.dp, AlKhatibColors.SoftGrey)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Surah ${reminder.surahName}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = AlKhatibColors.Slate900
                )
                Spacer(Modifier.height(4.dp))
                val timeString = String.format("%02d.%02d", reminder.hour, reminder.minute)
                val dayName = getWeekdayName(reminder.weekday, isIndoMalay)
                Text(
                    text = if (isIndoMalay) "Setiap hari $dayName pukul $timeString" else "Every $dayName at $timeString",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AlKhatibColors.Slate500
                )
            }

            Switch(
                checked = reminder.enabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = AlKhatibColors.DeepEmerald,
                    checkedTrackColor = AlKhatibColors.DeepEmerald.copy(alpha = 0.3f)
                )
            )

            Spacer(Modifier.width(8.dp))

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Reminder",
                    tint = AlKhatibColors.Danger
                )
            }
        }
    }
}

@Composable
private fun AddEditSurahReminderDialog(
    reminder: SurahReminder?,
    isIndoMalay: Boolean,
    onSave: (SurahReminder) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedSurahIndex by remember { mutableStateOf(reminder?.let { it.surahNumber - 1 } ?: 17) }
    var selectedWeekday by remember { mutableStateOf(reminder?.weekday ?: Calendar.FRIDAY) }
    var hour by remember { mutableStateOf(reminder?.hour ?: 10) }
    var minute by remember { mutableStateOf(reminder?.minute ?: 30) }
    var showSurahPicker by remember { mutableStateOf(false) }

    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = AlKhatibColors.PureWhite,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = if (reminder != null) {
                        if (isIndoMalay) "Edit Pengingat" else "Edit Reminder"
                    } else {
                        if (isIndoMalay) "Tambah Pengingat Baru" else "Add New Reminder"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AlKhatibColors.DeepEmerald
                )

                // Surah Selector Trigger Row
                Column {
                    Text(
                        text = if (isIndoMalay) "Pilih Surah:" else "Select Surah:",
                        style = MaterialTheme.typography.bodySmall,
                        color = AlKhatibColors.Slate500
                    )
                    Spacer(Modifier.height(4.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showSurahPicker = true },
                        shape = RoundedCornerShape(8.dp),
                        color = AlKhatibColors.LightGrey,
                        border = BorderStroke(1.dp, AlKhatibColors.SoftGrey)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Surah ${SURAH_NAMES[selectedSurahIndex]}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = AlKhatibColors.Slate900,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = if (isIndoMalay) "Ubah" else "Change",
                                style = MaterialTheme.typography.bodySmall,
                                color = AlKhatibColors.DeepEmerald,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Weekday Selector Row (using 3-letter abbreviations with weighted spacing)
                Column {
                    Text(
                        text = if (isIndoMalay) "Hari:" else "Day:",
                        style = MaterialTheme.typography.bodySmall,
                        color = AlKhatibColors.Slate500
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val weekdays = listOf(
                            Calendar.SUNDAY to (if (isIndoMalay) "Min" else "Sun"),
                            Calendar.MONDAY to (if (isIndoMalay) "Sen" else "Mon"),
                            Calendar.TUESDAY to (if (isIndoMalay) "Sel" else "Tue"),
                            Calendar.WEDNESDAY to (if (isIndoMalay) "Rab" else "Wed"),
                            Calendar.THURSDAY to (if (isIndoMalay) "Kam" else "Thu"),
                            Calendar.FRIDAY to (if (isIndoMalay) "Jum" else "Fri"),
                            Calendar.SATURDAY to (if (isIndoMalay) "Sab" else "Sat")
                        )
                        weekdays.forEach { (day, label) ->
                            val isSelected = selectedWeekday == day
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .background(
                                        color = if (isSelected) AlKhatibColors.DeepEmerald else AlKhatibColors.LightGrey,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { selectedWeekday = day },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) AlKhatibColors.PureWhite else AlKhatibColors.Slate900,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Time Picker Trigger Row
                Column {
                    Text(
                        text = if (isIndoMalay) "Waktu:" else "Time:",
                        style = MaterialTheme.typography.bodySmall,
                        color = AlKhatibColors.Slate500
                    )
                    Spacer(Modifier.height(4.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                TimePickerDialog(
                                    context,
                                    { _, pickedHour, pickedMinute ->
                                        hour = pickedHour
                                        minute = pickedMinute
                                    },
                                    hour,
                                    minute,
                                    true
                                ).show()
                            },
                        shape = RoundedCornerShape(8.dp),
                        color = AlKhatibColors.LightGrey,
                        border = BorderStroke(1.dp, AlKhatibColors.SoftGrey)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AccessTime,
                                    contentDescription = "Time Icon",
                                    tint = AlKhatibColors.Slate500,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = String.format("%02d:%02d", hour, minute),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = AlKhatibColors.Slate900,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Text(
                                text = if (isIndoMalay) "Atur" else "Set",
                                style = MaterialTheme.typography.bodySmall,
                                color = AlKhatibColors.DeepEmerald,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Bottom Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = if (isIndoMalay) "Batal" else "Cancel",
                            color = AlKhatibColors.Slate500
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val finalReminder = SurahReminder(
                                id = reminder?.id ?: UUID.randomUUID().toString(),
                                surahNumber = selectedSurahIndex + 1,
                                surahName = SURAH_NAMES[selectedSurahIndex],
                                weekday = selectedWeekday,
                                hour = hour,
                                minute = minute,
                                enabled = reminder?.enabled ?: true
                            )
                            onSave(finalReminder)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AlKhatibColors.DeepEmerald)
                    ) {
                        Text(text = if (isIndoMalay) "Simpan" else "Save")
                    }
                }
            }
        }
    }

    if (showSurahPicker) {
        Dialog(onDismissRequest = { showSurahPicker = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = AlKhatibColors.PureWhite,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isIndoMalay) "Pilih Surah" else "Select Surah",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AlKhatibColors.DeepEmerald,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(SURAH_NAMES.size) { index ->
                            Text(
                                text = "${index + 1}. ${SURAH_NAMES[index]}",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedSurahIndex = index
                                        showSurahPicker = false
                                    }
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = AlKhatibColors.Slate900
                            )
                            HorizontalDivider(color = AlKhatibColors.SoftGrey.copy(alpha = 0.5f))
                        }
                    }
                }
            }
        }
    }
}

private fun getWeekdayName(weekday: Int, isIndoMalay: Boolean): String = when (weekday) {
    Calendar.SUNDAY -> if (isIndoMalay) "Minggu" else "Sunday"
    Calendar.MONDAY -> if (isIndoMalay) "Senin" else "Monday"
    Calendar.TUESDAY -> if (isIndoMalay) "Selasa" else "Tuesday"
    Calendar.WEDNESDAY -> if (isIndoMalay) "Rabu" else "Wednesday"
    Calendar.THURSDAY -> if (isIndoMalay) "Kamis" else "Thursday"
    Calendar.FRIDAY -> if (isIndoMalay) "Jumat" else "Friday"
    Calendar.SATURDAY -> if (isIndoMalay) "Sabtu" else "Saturday"
    else -> ""
}
