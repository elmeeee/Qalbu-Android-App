package app.kamy.saatApp.ui.adhan

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kamy.saatApp.R
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

@Composable
fun AdhanFullScreenOverlay(
    title: String,
    body: String,
    prayerName: String? = null,
    onStopClick: () -> Unit
) {
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentTime = System.currentTimeMillis()
        }
    }

    val backgroundRes = remember(prayerName, title) {
        resolveAdhanBackgroundRes(prayerName = prayerName, title = title)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = backgroundRes),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Dark overlay for text readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.65f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = timeFormat.format(Date(currentTime)),
                fontSize = 72.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = title,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            
            if (body.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = body,
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
            }
        }

        Button(
            onClick = onStopClick,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 64.dp)
                .fillMaxWidth(0.8f)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = stringResource(id = R.string.adhan_stop),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

fun resolveAdhanBackgroundRes(
    prayerName: String? = null,
    title: String = ""
): Int {
    val query = (prayerName.orEmpty() + " " + title).lowercase(Locale.ROOT)
    return when {
        query.contains("fajr") || query.contains("subuh") -> R.drawable.bg_intent_fajr
        query.contains("dhuhr") || query.contains("dhur") || query.contains("dzuhur") || query.contains("zuhur") || query.contains("zohor") -> R.drawable.bg_intent_dhur
        query.contains("asr") || query.contains("ashar") || query.contains("asar") -> R.drawable.bg_intent_asr
        query.contains("maghrib") || query.contains("magrib") -> R.drawable.bg_intent_maghrib
        query.contains("isha") || query.contains("isya") || query.contains("isyak") -> R.drawable.bg_intent_isha
        else -> {
            val cal = Calendar.getInstance()
            when (cal.get(Calendar.HOUR_OF_DAY)) {
                in 3..5 -> R.drawable.bg_intent_fajr
                in 11..13 -> R.drawable.bg_intent_dhur
                in 14..17 -> R.drawable.bg_intent_asr
                in 18..19 -> R.drawable.bg_intent_maghrib
                in 20..23, in 0..2 -> R.drawable.bg_intent_isha
                else -> R.drawable.bg_intent_fajr
            }
        }
    }
}
