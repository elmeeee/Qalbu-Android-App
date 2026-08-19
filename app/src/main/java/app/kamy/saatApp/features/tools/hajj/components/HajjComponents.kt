package app.kamy.saatApp.features.tools.hajj.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kamy.saatApp.core.locale.AppLanguage
import app.kamy.saatApp.design.theme.SaatColors
import app.kamy.saatApp.design.theme.TajweedFontFamily
import app.kamy.saatApp.features.tools.hajj.model.*

@Composable
fun ManasikStepCard(
    step: ManasikStep,
    appLanguage: AppLanguage,
    onOpenDoa: (String) -> Unit = {}
) {
    var isExpanded by remember { mutableStateOf(false) }
    val arrowRotation by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f, label = "arrow")

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, if (isExpanded) SaatColors.DeepEmerald.copy(alpha = 0.4f) else SaatColors.SoftGrey),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isExpanded) 4.dp else 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded }
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header Row: Step Badge + Rukun/Wajib Pill + Expand Arrow
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(SaatColors.DeepEmerald, SaatColors.Teal)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${step.stepNumber}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (step.isRukun) SaatColors.DeepEmerald.copy(alpha = 0.12f) else SaatColors.GoldDeep.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, if (step.isRukun) SaatColors.DeepEmerald.copy(alpha = 0.3f) else SaatColors.GoldDeep.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = if (step.isRukun) {
                                when (appLanguage) {
                                    AppLanguage.ENGLISH -> "PILLAR (RUKUN)"
                                    AppLanguage.MALAY -> "RUKUN MUTLAK"
                                    else -> "RUKUN WAJIB"
                                }
                            } else {
                                when (appLanguage) {
                                    AppLanguage.ENGLISH -> "MANDATORY (WAJIB)"
                                    AppLanguage.MALAY -> "WAJIB MANASIK"
                                    else -> "WAJIB HAJI"
                                }
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (step.isRukun) SaatColors.DeepEmerald else SaatColors.GoldDeep,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                IconButton(
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Filled.KeyboardArrowDown,
                        contentDescription = "Expand",
                        tint = SaatColors.Slate500,
                        modifier = Modifier.rotate(arrowRotation)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Title
            Text(
                text = step.title.get(appLanguage),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = SaatColors.Slate900
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Subtitle
            Text(
                text = step.subtitle.get(appLanguage),
                style = MaterialTheme.typography.bodySmall,
                color = SaatColors.Slate700,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Location & Time Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SaatColors.LightGrey,
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Place,
                            contentDescription = null,
                            tint = SaatColors.DeepEmerald,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = step.location.get(appLanguage),
                            style = MaterialTheme.typography.labelSmall,
                            color = SaatColors.Slate700,
                            maxLines = 1
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SaatColors.LightGrey,
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Schedule,
                            contentDescription = null,
                            tint = SaatColors.GoldDeep,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = step.timeOrDay.get(appLanguage),
                            style = MaterialTheme.typography.labelSmall,
                            color = SaatColors.Slate700,
                            maxLines = 1
                        )
                    }
                }
            }

            // Expanded Details
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    HorizontalDivider(color = SaatColors.SoftGrey, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(14.dp))

                    // Description
                    Text(
                        text = step.description.get(appLanguage),
                        style = MaterialTheme.typography.bodyMedium,
                        color = SaatColors.Slate800,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Detailed steps list
                    Text(
                        text = when (appLanguage) {
                            AppLanguage.ENGLISH -> "Step-by-Step Instructions:"
                            AppLanguage.MALAY -> "Panduan Langkah demi Langkah:"
                            else -> "Urutan Pelaksanaan:"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = SaatColors.DeepEmerald
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    step.detailedSteps.forEachIndexed { idx, item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = SaatColors.DeepEmerald.copy(alpha = 0.15f),
                                modifier = Modifier.size(20.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "${idx + 1}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = SaatColors.DeepEmerald
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = item.get(appLanguage),
                                style = MaterialTheme.typography.bodySmall,
                                color = SaatColors.Slate700,
                                lineHeight = 19.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Prohibitions Alert (if any)
                    if (step.prohibitions.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFEF2F2),
                            border = BorderStroke(1.dp, Color(0xFFFCA5A5))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Filled.Warning,
                                        contentDescription = null,
                                        tint = Color(0xFFDC2626),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = when (appLanguage) {
                                            AppLanguage.ENGLISH -> "Prohibitions to Watch Out:"
                                            AppLanguage.MALAY -> "Larangan Semasa Tahap Ini:"
                                            else -> "Larangan yang Harus Dihindari:"
                                        },
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF991B1B)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                step.prohibitions.forEach { p ->
                                    Text(
                                        text = "• ${p.get(appLanguage)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF7F1D1D),
                                        lineHeight = 17.sp,
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Practical Tips
                    if (step.practicalTips.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFFFBEB),
                            border = BorderStroke(1.dp, Color(0xFFFDE68A))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Filled.Lightbulb,
                                        contentDescription = null,
                                        tint = Color(0xFFD97706),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = when (appLanguage) {
                                            AppLanguage.ENGLISH -> "Practical Pilgrim Tips:"
                                            AppLanguage.MALAY -> "Tips Praktikal Jemaah:"
                                            else -> "Tips Praktis di Lapangan:"
                                        },
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF92400E)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                step.practicalTips.forEach { tip ->
                                    Text(
                                        text = "💡 ${tip.get(appLanguage)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF78350F),
                                        lineHeight = 17.sp,
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Dalil footer
                    if (step.dalilQuran != null || step.dalilHadits != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = SaatColors.LightGrey,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                if (step.dalilQuran != null) {
                                    Text(
                                        text = "📖 ${step.dalilQuran}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontStyle = FontStyle.Italic,
                                        color = SaatColors.Slate700
                                    )
                                }
                                if (step.dalilHadits != null) {
                                    if (step.dalilQuran != null) Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "📜 ${step.dalilHadits}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontStyle = FontStyle.Italic,
                                        color = SaatColors.Slate700
                                    )
                                }
                            }
                        }
                    }

                    // Quick Action: Buka Doa
                    if (step.doaRefId != null) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = { onOpenDoa(step.doaRefId) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SaatColors.DeepEmerald),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when (appLanguage) {
                                    AppLanguage.ENGLISH -> "View Related Du'a & Dzikir"
                                    AppLanguage.MALAY -> "Lihat Doa & Zikir Berkaitan"
                                    else -> "Buka Doa & Dzikir Terkait"
                                },
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HajjDoaCard(
    doa: HajjDoaItem,
    appLanguage: AppLanguage
) {
    val context = LocalContext.current

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, SaatColors.SoftGrey),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header Row: Category Badge + Share & Copy buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SaatColors.DeepEmerald.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, SaatColors.DeepEmerald.copy(alpha = 0.25f))
                ) {
                    Text(
                        text = doa.category.get(appLanguage),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = SaatColors.DeepEmerald,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Doa Haji", "${doa.arabic}\n\n${doa.latin}\n\n${doa.translation.get(appLanguage)}")
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Doa berhasil disalin", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = "Copy", tint = SaatColors.Slate500, modifier = Modifier.size(18.dp))
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "🤲 ${doa.title.get(appLanguage)}\n\n${doa.arabic}\n\n${doa.latin}\n\nArtinya:\n\"${doa.translation.get(appLanguage)}\"\n\nRef: ${doa.reference}\n\n(Dibagikan via Qalbu App)"
                                )
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Bagikan Doa"))
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = "Share", tint = SaatColors.Slate500, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title
            Text(
                text = doa.title.get(appLanguage),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = SaatColors.Slate900
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Arabic Box with Golden frame
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = SaatColors.ScreenBackground,
                border = BorderStroke(1.dp, SaatColors.Gold.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Text(
                        text = doa.arabic,
                        fontFamily = TajweedFontFamily,
                        fontSize = 24.sp,
                        lineHeight = 44.sp,
                        textAlign = TextAlign.End,
                        color = SaatColors.Slate900,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Latin Transliteration
            Text(
                text = doa.latin,
                style = MaterialTheme.typography.bodyMedium,
                fontStyle = FontStyle.Italic,
                color = SaatColors.DeepEmerald,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Translation
            Text(
                text = doa.translation.get(appLanguage),
                style = MaterialTheme.typography.bodySmall,
                color = SaatColors.Slate700,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = SaatColors.SoftGrey, thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))

            // Context & Reference Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Filled.Info,
                        contentDescription = null,
                        tint = SaatColors.GoldDeep,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = doa.occasions.get(appLanguage),
                        style = MaterialTheme.typography.labelSmall,
                        color = SaatColors.Slate700,
                        maxLines = 1
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = SaatColors.LightGrey
                ) {
                    Text(
                        text = doa.reference,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = SaatColors.Slate700,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun HajjDalilCard(
    dalil: HajjDalilItem,
    appLanguage: AppLanguage
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, SaatColors.SoftGrey),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header Row: Type pill + Reference
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (dalil.isQuran) SaatColors.DeepEmerald.copy(alpha = 0.12f) else SaatColors.Teal.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, if (dalil.isQuran) SaatColors.DeepEmerald.copy(alpha = 0.3f) else SaatColors.Teal.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = dalil.category.get(appLanguage),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (dalil.isQuran) SaatColors.DeepEmerald else SaatColors.Teal,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Text(
                    text = dalil.surahOrNarrator,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = SaatColors.Slate700
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title
            Text(
                text = dalil.title.get(appLanguage),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = SaatColors.Slate900
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Arabic text
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = SaatColors.ScreenBackground,
                border = BorderStroke(1.dp, SaatColors.Gold.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.padding(18.dp), contentAlignment = Alignment.CenterEnd) {
                    Text(
                        text = dalil.arabic,
                        fontFamily = TajweedFontFamily,
                        fontSize = 22.sp,
                        lineHeight = 40.sp,
                        textAlign = TextAlign.End,
                        color = SaatColors.Slate900,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Translation
            Text(
                text = "\"${dalil.translation.get(appLanguage)}\"",
                style = MaterialTheme.typography.bodyMedium,
                fontStyle = FontStyle.Italic,
                color = SaatColors.Slate800,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Tafsir & Context Box
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = SaatColors.DeepEmerald.copy(alpha = 0.05f),
                border = BorderStroke(1.dp, SaatColors.DeepEmerald.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = when (appLanguage) {
                            AppLanguage.ENGLISH -> "Tafsir & Contextual Explanation:"
                            AppLanguage.MALAY -> "Huraian Tafsir & Konteks:"
                            else -> "Penjelasan Tafsir & Konteks:"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = SaatColors.DeepEmerald
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = dalil.tafsirExplanation.get(appLanguage),
                        style = MaterialTheme.typography.bodySmall,
                        color = SaatColors.Slate700,
                        lineHeight = 20.sp
                    )
                }
            }

            // Key Lessons
            if (dalil.keyLessons.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                dalil.keyLessons.forEach { lesson ->
                    Row(
                        modifier = Modifier.padding(vertical = 2.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text("✨", fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = lesson.get(appLanguage),
                            style = MaterialTheme.typography.labelSmall,
                            color = SaatColors.Slate700,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MadhhabRulingCard(
    ruling: MadhhabRuling,
    appLanguage: AppLanguage
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, SaatColors.SoftGrey),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header
            Text(
                text = ruling.topic.get(appLanguage),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = SaatColors.Slate900
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = ruling.generalExplanation.get(appLanguage),
                style = MaterialTheme.typography.bodySmall,
                color = SaatColors.Slate700,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 4-Mazhab Comparison Grid/Rows
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                MadhhabItemTile(name = "Mazhab Syafi'i", text = ruling.syafii.get(appLanguage), accentColor = SaatColors.DeepEmerald)
                MadhhabItemTile(name = "Mazhab Hanafi", text = ruling.hanafi.get(appLanguage), accentColor = SaatColors.Teal)
                MadhhabItemTile(name = "Mazhab Maliki", text = ruling.maliki.get(appLanguage), accentColor = SaatColors.GoldDeep)
                MadhhabItemTile(name = "Mazhab Hanbali", text = ruling.hanbali.get(appLanguage), accentColor = Color(0xFF4F46E5))
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Rajih Conclusion Box
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF0FDF4),
                border = BorderStroke(1.dp, Color(0xFF86EFAC))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Filled.Gavel,
                        contentDescription = null,
                        tint = Color(0xFF16A34A),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = when (appLanguage) {
                                AppLanguage.ENGLISH -> "Fatwa & Practical Conclusion:"
                                AppLanguage.MALAY -> "Kesimpulan Tarjih & Praktikal:"
                                else -> "Kesimpulan Tarjih & Praktis Jamaah:"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF166534)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = ruling.rajihConclusion.get(appLanguage),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF14532D),
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MadhhabItemTile(
    name: String,
    text: String,
    accentColor: Color
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = accentColor.copy(alpha = 0.05f),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.2f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = name,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = SaatColors.Slate800,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun DamRuleCard(
    dam: DamRuleItem,
    appLanguage: AppLanguage
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, SaatColors.SoftGrey),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Category Badge
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFFEF2F2),
                border = BorderStroke(1.dp, Color(0xFFFCA5A5))
            ) {
                Text(
                    text = dam.category.get(appLanguage),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF991B1B),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Violation
            Text(
                text = dam.violation.get(appLanguage),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = SaatColors.Slate900,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Penalty & Alternatives
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = SaatColors.LightGrey,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = when (appLanguage) {
                            AppLanguage.ENGLISH -> "Required Dam / Penalty:"
                            AppLanguage.MALAY -> "Kadar Dam / Denda:"
                            else -> "Bentuk Dam / Denda:"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = SaatColors.DeepEmerald
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = dam.penalty.get(appLanguage),
                        style = MaterialTheme.typography.bodySmall,
                        color = SaatColors.Slate800,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = when (appLanguage) {
                            AppLanguage.ENGLISH -> "Alternative Options (If unable):"
                            AppLanguage.MALAY -> "Pilihan Gantian (Jika tidak mampu):"
                            else -> "Pilihan Alternatif / Gantian:"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = SaatColors.GoldDeep
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = dam.alternatives.get(appLanguage),
                        style = MaterialTheme.typography.bodySmall,
                        color = SaatColors.Slate700,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "📜 Dalil: ${dam.dalil}",
                style = MaterialTheme.typography.labelSmall,
                fontStyle = FontStyle.Italic,
                color = SaatColors.Slate500
            )
        }
    }
}

@Composable
fun MiqatCard(
    miqat: MiqatLocation,
    appLanguage: AppLanguage
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, SaatColors.SoftGrey),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = miqat.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SaatColors.Slate900,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SaatColors.Gold.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = miqat.distanceFromMakkah,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = SaatColors.GoldDeep,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = miqat.arabicName,
                fontFamily = TajweedFontFamily,
                fontSize = 18.sp,
                color = SaatColors.DeepEmerald
            )

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = miqat.description.get(appLanguage),
                style = MaterialTheme.typography.bodySmall,
                color = SaatColors.Slate700,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = SaatColors.LightGrey,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "📍 ${miqat.direction.get(appLanguage)}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = SaatColors.Slate800
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "👥 ${miqat.dedicatedFor.get(appLanguage)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = SaatColors.Slate700,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun HistoricSiteCard(
    site: HistoricZiarahSite,
    appLanguage: AppLanguage
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, SaatColors.SoftGrey),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = site.name.get(appLanguage),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SaatColors.Slate900,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SaatColors.Teal.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = site.city.get(appLanguage),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = SaatColors.Teal,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = site.historicalSignificance.get(appLanguage),
                style = MaterialTheme.typography.bodySmall,
                color = SaatColors.Slate700,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFF0FDF4),
                border = BorderStroke(1.dp, Color(0xFFDCFCE7))
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text("🕌", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = site.adabAndDoa.get(appLanguage),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF166534),
                        lineHeight = 17.sp
                    )
                }
            }
        }
    }
}

@Composable
fun HajjChecklistTile(
    item: HajjChecklistItem,
    isChecked: Boolean,
    onToggle: () -> Unit,
    appLanguage: AppLanguage
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isChecked) SaatColors.DeepEmerald.copy(alpha = 0.06f) else Color.White,
        border = BorderStroke(1.dp, if (isChecked) SaatColors.DeepEmerald.copy(alpha = 0.4f) else SaatColors.SoftGrey),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isChecked,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = SaatColors.DeepEmerald,
                    uncheckedColor = SaatColors.Slate500
                ),
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.label.get(appLanguage),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (item.isCrucial) FontWeight.Bold else FontWeight.Normal,
                    textDecoration = if (isChecked) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (isChecked) SaatColors.Slate500 else SaatColors.Slate900,
                    lineHeight = 20.sp
                )
                if (item.note != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.note.get(appLanguage),
                        style = MaterialTheme.typography.labelSmall,
                        color = SaatColors.Slate500
                    )
                }
            }

            if (item.isCrucial && !isChecked) {
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFFEF2F2)
                ) {
                    Text(
                        text = "WAJIB",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFDC2626),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
