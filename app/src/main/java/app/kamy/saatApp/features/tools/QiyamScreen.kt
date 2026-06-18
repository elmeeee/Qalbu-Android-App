package app.kamy.saatApp.features.tools

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kamy.saatApp.R
import app.kamy.saatApp.design.theme.AlKhatibColors
import app.kamy.saatApp.design.theme.AlKhatibSpacing
import app.kamy.saatApp.features.tools.qiyam.TahajudGuide
import app.kamy.saatApp.features.tools.qiyam.TahajudGuideCategory
import app.kamy.saatApp.features.tools.qiyam.TahajudReading
import app.kamy.saatApp.infrastructure.preferences.QiyamTrackerStore
import app.kamy.saatApp.ui.feedback.rememberConfirmHaptic
import app.kamy.saatApp.ui.layout.floatingNavBottomPadding
import app.kamy.saatApp.ui.layout.tabContentStatusBarInset

@Composable
fun QiyamScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var loggedTonight by remember { mutableStateOf(QiyamTrackerStore.isLogged(context)) }
    val snapshot = remember(loggedTonight) { QiyamTrackerStore.snapshot(context) }
    val weekLog = remember(loggedTonight) { QiyamTrackerStore.last7Days(context) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AlKhatibColors.ScreenBackground)
            .tabContentStatusBarInset()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AlKhatibSpacing.screenHorizontal, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
            }
            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.qiyam_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.qiyam_premium_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = AlKhatibColors.Slate500
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(bottom = floatingNavBottomPadding())
        ) {
            QiyamHeroCard(snapshot = snapshot, weekLog = weekLog)

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = AlKhatibColors.ScreenBackground,
                contentColor = AlKhatibColors.DeepEmerald,
                indicator = { tabPositions ->
                    if (selectedTab < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = AlKhatibColors.DeepEmerald
                        )
                    }
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text(stringResource(R.string.qiyam_tab_tracker)) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text(stringResource(R.string.qiyam_tab_guide)) }
                )
            }

            when (selectedTab) {
                0 -> QiyamTrackerTab(
                    loggedTonight = loggedTonight,
                    onToggle = { loggedTonight = QiyamTrackerStore.toggleTonight(context) }
                )
                1 -> QiyamGuideTab()
            }
        }
    }
}

@Composable
private fun QiyamHeroCard(
    snapshot: app.kamy.saatApp.infrastructure.preferences.QiyamMonthSnapshot,
    weekLog: List<app.kamy.saatApp.infrastructure.preferences.QiyamDayLog>
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 6.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(AlKhatibColors.EmeraldNight, AlKhatibColors.IndigoDeep, AlKhatibColors.ForestDeeper)
                    )
                )
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.NightsStay,
                        contentDescription = null,
                        tint = AlKhatibColors.GoldBright,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(Modifier.size(10.dp))
                    Text(
                        text = stringResource(R.string.qiyam_explain),
                        style = MaterialTheme.typography.bodyMedium,
                        color = AlKhatibColors.PureWhite.copy(alpha = 0.9f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    QiyamStatBlock(
                        value = snapshot.streak.toString(),
                        label = stringResource(R.string.qiyam_streak_label)
                    )
                    QiyamStatBlock(
                        value = snapshot.nightsThisMonth.toString(),
                        label = stringResource(R.string.qiyam_month_label)
                    )
                    QiyamStatBlock(
                        value = snapshot.nightsLast7Days.toString(),
                        label = stringResource(R.string.qiyam_week_label)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    weekLog.forEach { day ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = day.weekdayShort,
                                style = MaterialTheme.typography.labelSmall,
                                color = AlKhatibColors.PureWhite.copy(alpha = 0.6f)
                            )
                            Spacer(Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(if (day.isToday) 14.dp else 10.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            day.logged -> AlKhatibColors.GoldBright
                                            day.isToday -> AlKhatibColors.PureWhite.copy(0.35f)
                                            else -> AlKhatibColors.PureWhite.copy(0.15f)
                                        }
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QiyamStatBlock(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = AlKhatibColors.PureWhite
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = AlKhatibColors.PureWhite.copy(alpha = 0.65f)
        )
    }
}

@Composable
private fun QiyamTrackerTab(
    loggedTonight: Boolean,
    onToggle: () -> Unit
) {
    val confirmHaptic = rememberConfirmHaptic()
    Column(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.qiyam_explain_detail),
            style = MaterialTheme.typography.bodyMedium,
            color = AlKhatibColors.Slate500
        )

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = AlKhatibColors.PrayerMint,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onToggle()
                        if (!loggedTonight) confirmHaptic()
                    }
                    .padding(18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.qiyam_tonight_label),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = AlKhatibColors.DeepEmerald
                    )
                    Text(
                        text = stringResource(R.string.qiyam_tonight_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = AlKhatibColors.Slate500
                    )
                }
                Switch(
                    checked = loggedTonight,
                    onCheckedChange = {
                        onToggle()
                        if (!loggedTonight) confirmHaptic()
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = AlKhatibColors.PureWhite,
                        checkedTrackColor = AlKhatibColors.DeepEmerald
                    )
                )
            }
        }
    }
}

@Composable
private fun QiyamGuideTab() {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.tahajud_guide_intro),
            style = MaterialTheme.typography.bodyMedium,
            color = AlKhatibColors.Slate500
        )

        TahajudGuideCategory.entries.forEach { category ->
            Text(
                text = categoryTitle(category),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = AlKhatibColors.DeepEmerald,
                modifier = Modifier.padding(top = 8.dp)
            )
            TahajudGuide.byCategory(category).forEach { reading ->
                TahajudReadingCard(reading = reading)
            }
        }
    }
}

@Composable
private fun categoryTitle(category: TahajudGuideCategory): String = when (category) {
    TahajudGuideCategory.PREPARATION -> stringResource(R.string.tahajud_cat_preparation)
    TahajudGuideCategory.PRAYER -> stringResource(R.string.tahajud_cat_prayer)
    TahajudGuideCategory.WITR -> stringResource(R.string.tahajud_cat_witr)
    TahajudGuideCategory.CLOSING -> stringResource(R.string.tahajud_cat_closing)
}

@Composable
private fun TahajudReadingCard(reading: TahajudReading) {
    var expanded by remember { mutableStateOf(false) }
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = AlKhatibColors.PureWhite,
        shadowElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(reading.titleRes),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = AlKhatibColors.Slate500
                )
            }
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    reading.arabicRes?.let { arabicRes ->
                        Text(
                            text = stringResource(arabicRes),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 22.sp,
                                lineHeight = 36.sp
                            ),
                            color = AlKhatibColors.DeepEmerald,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                    reading.transliterationRes?.let { translitRes ->
                        Text(
                            text = stringResource(translitRes),
                            style = MaterialTheme.typography.bodyMedium,
                            color = AlKhatibColors.Teal,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                    Text(
                        text = stringResource(reading.bodyRes),
                        style = MaterialTheme.typography.bodyMedium,
                        color = AlKhatibColors.Slate800
                    )
                }
            }
        }
    }
}
