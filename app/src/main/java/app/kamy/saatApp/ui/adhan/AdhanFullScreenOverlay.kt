package app.kamy.saatApp.ui.adhan

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kamy.saatApp.R
import app.kamy.saatApp.infrastructure.preferences.LocationPreferencesStore
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

enum class AdhanPrayerKind(
    val bgRes: Int,
    val isLightBackground: Boolean,
    val subtitleRes: Int,
    val quoteRes: Int,
    val isTahajjudOrAlarm: Boolean
) {
    FAJR(R.drawable.bg_intent_fajr, true, R.string.prayer_body_fajr, R.string.prayer_quote_fajr, false),
    DHUHR(R.drawable.bg_intent_dhur, true, R.string.prayer_body_dhuhr, R.string.prayer_quote_dhuhr, false),
    ASR(R.drawable.bg_intent_asr, true, R.string.prayer_body_asr, R.string.prayer_quote_asr, false),
    MAGHRIB(R.drawable.bg_intent_maghrib, false, R.string.prayer_body_maghrib, R.string.prayer_quote_maghrib, false),
    ISHA(R.drawable.bg_intent_isha, false, R.string.prayer_body_isha, R.string.prayer_quote_isha, false),
    TAHAJUD(R.drawable.bg_intent_tahajud, false, R.string.prayer_body_tahajud, R.string.prayer_quote_tahajud, true)
}

fun resolveAdhanPrayerKind(
    prayerName: String? = null,
    title: String = ""
): AdhanPrayerKind {
    val query = (prayerName.orEmpty() + " " + title).lowercase(Locale.ROOT)
    return when {
        query.contains("tahajud") || query.contains("tahajjud") || query.contains("last_third") -> AdhanPrayerKind.TAHAJUD
        query.contains("fajr") || query.contains("subuh") -> AdhanPrayerKind.FAJR
        query.contains("dhuhr") || query.contains("dhur") || query.contains("dzuhur") || query.contains("zuhur") || query.contains("zohor") -> AdhanPrayerKind.DHUHR
        query.contains("asr") || query.contains("ashar") || query.contains("asar") -> AdhanPrayerKind.ASR
        query.contains("maghrib") || query.contains("magrib") -> AdhanPrayerKind.MAGHRIB
        query.contains("isha") || query.contains("isya") || query.contains("isyak") -> AdhanPrayerKind.ISHA
        else -> {
            val cal = Calendar.getInstance()
            when (cal.get(Calendar.HOUR_OF_DAY)) {
                in 3..5 -> AdhanPrayerKind.FAJR
                in 11..13 -> AdhanPrayerKind.DHUHR
                in 14..17 -> AdhanPrayerKind.ASR
                in 18..19 -> AdhanPrayerKind.MAGHRIB
                in 20..23, in 0..2 -> AdhanPrayerKind.ISHA
                else -> AdhanPrayerKind.FAJR
            }
        }
    }
}

fun resolveAdhanBackgroundRes(
    prayerName: String? = null,
    title: String = ""
): Int = resolveAdhanPrayerKind(prayerName, title).bgRes

@Composable
fun AdhanFullScreenOverlay(
    title: String,
    body: String,
    prayerName: String? = null,
    onStopClick: () -> Unit
) {
    val context = LocalContext.current
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentTime = System.currentTimeMillis()
        }
    }

    val prayerKind = remember(prayerName, title) {
        resolveAdhanPrayerKind(prayerName = prayerName, title = title)
    }

    val fontColor = if (prayerKind.isLightBackground) Color(0xFF124C31) else Color.White

    val locationLabel = remember { LocationPreferencesStore.from(context).displayLabel() }
    val timeStr = timeFormat.format(Date(currentTime))
    val headerTitle = remember(timeStr, locationLabel, title) {
        val rawLoc = if (!locationLabel.isNullOrBlank()) {
            locationLabel
        } else if (title.isNotBlank() && title.contains("•")) {
            title.substringAfter("•").trim()
        } else if (title.isNotBlank()) {
            title
        } else {
            ""
        }
        val cleanLoc = cleanLocationLabel(rawLoc)
        if (cleanLoc.isNotBlank()) "$timeStr - $cleanLoc" else timeStr
    }

    val subtitle = stringResource(id = prayerKind.subtitleRes)
    val rawQuote = stringResource(id = prayerKind.quoteRes)
    val quote = remember(rawQuote) {
        rawQuote.trim().removeSurrounding("“", "”").removeSurrounding("\"", "\"")
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = prayerKind.bgRes),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 225.dp, start = 20.dp, end = 20.dp)
                .align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = headerTitle,
                fontSize = when {
                    headerTitle.length > 20 -> 24.sp
                    headerTitle.length > 15 -> 28.sp
                    else -> 36.sp
                },
                fontWeight = FontWeight.Bold,
                color = fontColor,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = subtitle,
                fontSize = 24.sp,
                fontWeight = FontWeight.Light,
                color = fontColor,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = quote,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 16.sp,
                color = fontColor,
                textAlign = TextAlign.Center,
                maxLines = 2,
                modifier = Modifier.fillMaxWidth(0.75f)
            )
        }

        Button(
            onClick = onStopClick,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp, start = 32.dp, end = 32.dp)
                .navigationBarsPadding()
                .fillMaxWidth(0.85f)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF124C31),
                contentColor = Color.White
            ),
            border = BorderStroke(1.dp, Color.White),
            shape = RoundedCornerShape(32.dp)
        ) {
            Text(
                text = stringResource(id = if (prayerKind.isTahajjudOrAlarm) R.string.alarm_stop else R.string.adhan_stop),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

private fun cleanLocationLabel(raw: String): String {
    if (raw.isBlank()) return ""
    // 1. Ambil nama kota/distrik utama jika formatnya bertingkat "City, Region" / "City, Country"
    var loc = raw.split(",").firstOrNull()?.trim().orEmpty()

    // 2. Bersihkan keterangan dalam kurung seperti "Mecca (Makkah)" -> "Mecca"
    if (loc.contains("(") && loc.contains(")")) {
        loc = loc.substringBefore("(").trim()
    }

    // 3. Bersihkan awalan administratif (Indonesia, Malaysia, UK/US/Internasional)
    val prefixes = listOf(
        // Indonesia / Malaysia / Melayu
        "Kelurahan ", "Kel. ", "Desa ", "Ds. ", "Kampung ", "Kg. ",
        "Kecamatan ", "Kec. ", "Kabupaten ", "Kab. ",
        "Kota Administratif ", "Kota Adm. ", "Kota ",
        "Daerah Khusus Ibukota ", "DKI ", "Daerah ", "Mukim ",
        // Internasional (English / Worldwide)
        "City of ", "Town of ", "Village of ", "Borough of ",
        "Municipality of ", "District of ", "County of ", "Prefecture of ", "State of "
    )
    for (prefix in prefixes) {
        if (loc.startsWith(prefix, ignoreCase = true)) {
            loc = loc.substring(prefix.length).trim()
            break
        }
    }
    return loc.ifBlank { raw.trim() }
}
