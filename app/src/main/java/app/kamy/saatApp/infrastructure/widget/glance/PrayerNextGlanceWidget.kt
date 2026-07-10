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

class PrayerNextGlanceWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val snapshot = PrayerWidgetRenderer.snapshot(context)

        provideContent {
            GlanceTheme {
                if (snapshot == null) {
                    EmptyWidget()
                } else {
                    NextPrayerWidgetContent(
                        cityLabel = snapshot.cityLabel,
                        nextPrayerName = snapshot.nextPrayerName,
                        nextPrayerTime = snapshot.nextPrayerTime,
                        countdown = snapshot.countdown
                    )
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
        }
    }

    @Composable
    private fun NextPrayerWidgetContent(
        cityLabel: String,
        nextPrayerName: String,
        nextPrayerTime: String,
        countdown: String
    ) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.surface)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = cityLabel,
                style = TextStyle(
                    color = GlanceTheme.colors.onSurfaceVariant,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            )
            Spacer(modifier = GlanceModifier.height(8.dp))
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = nextPrayerName,
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurface,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = GlanceModifier.width(8.dp))
                Text(
                    text = nextPrayerTime,
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurfaceVariant,
                        fontSize = 16.sp
                    )
                )
            }
            Spacer(modifier = GlanceModifier.height(8.dp))
            Text(
                text = countdown,
                style = TextStyle(
                    color = GlanceTheme.colors.primary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}

class PrayerNextGlanceWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = PrayerNextGlanceWidget()
}
