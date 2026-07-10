package app.kamy.saatApp.infrastructure.widget.glance

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import app.kamy.saatApp.infrastructure.widget.PrayerWidgetRenderer
import app.kamy.saatApp.infrastructure.widget.PrayerWidgetSnapshot
import app.kamy.saatApp.infrastructure.widget.PrayerWidgetSlot

class PrayerNextGlanceWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val snapshot = PrayerWidgetRenderer.snapshot(context)

        provideContent {
            GlanceTheme {
                if (snapshot == null) {
                    EmptyWidget()
                } else {
                    NextPrayerWidgetContent(snapshot = snapshot)
                }
            }
        }
    }

    @Composable
    private fun EmptyWidget() {
        Column(
            modifier = GlanceModifier.fillMaxSize().background(GlanceTheme.colors.surface).padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Setup required",
                style = TextStyle(color = GlanceTheme.colors.onSurface)
            )
            Text(
                text = "Please open the app to configure your location.",
                style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 12.sp)
            )
        }
    }

    @Composable
    private fun NextPrayerWidgetContent(snapshot: PrayerWidgetSnapshot) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.surface)
                .padding(16.dp),
        ) {
            // Header Row: City and Date
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = snapshot.cityLabel,
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurfaceVariant,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
                Spacer(modifier = GlanceModifier.defaultWeight())
                val dateStr = snapshot.hijriLabel ?: snapshot.gregorianLabel ?: ""
                Text(
                    text = dateStr,
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                )
            }

            Spacer(modifier = GlanceModifier.defaultWeight())

            // Hero Section: Next Prayer & Time
            Column(
                modifier = GlanceModifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = snapshot.nextPrayerLabel,
                    style = TextStyle(
                        color = GlanceTheme.colors.primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Row(
                    verticalAlignment = Alignment.Bottom,
                    modifier = GlanceModifier.fillMaxWidth().padding(top = 4.dp)
                ) {
                    Text(
                        text = snapshot.nextPrayerName,
                        style = TextStyle(
                            color = GlanceTheme.colors.onSurface,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = GlanceModifier.width(8.dp))
                    Text(
                        text = snapshot.nextPrayerTime,
                        style = TextStyle(
                            color = GlanceTheme.colors.onSurface,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Spacer(modifier = GlanceModifier.defaultWeight())
                    Text(
                        text = snapshot.countdown,
                        style = TextStyle(
                            color = GlanceTheme.colors.primary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = GlanceModifier.defaultWeight())

            // Timeline Row
            Row(
                modifier = GlanceModifier.fillMaxWidth().padding(top = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                snapshot.slots.forEachIndexed { index, slot ->
                    Column(
                        modifier = GlanceModifier.defaultWeight(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = slot.label,
                            style = TextStyle(
                                color = if (slot.isActive) GlanceTheme.colors.primary else GlanceTheme.colors.onSurfaceVariant,
                                fontSize = 10.sp,
                                fontWeight = if (slot.isActive) FontWeight.Bold else FontWeight.Normal
                            )
                        )
                        Spacer(modifier = GlanceModifier.height(4.dp))
                        Text(
                            text = slot.time,
                            style = TextStyle(
                                color = if (slot.isActive) GlanceTheme.colors.onSurface else GlanceTheme.colors.onSurfaceVariant,
                                fontSize = 12.sp,
                                fontWeight = if (slot.isActive) FontWeight.Bold else FontWeight.Medium
                            )
                        )
                    }
                }
            }
        }
    }
}

class PrayerNextGlanceWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = PrayerNextGlanceWidget()
}
