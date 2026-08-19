package app.kamy.saatApp.ui.feedback

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import app.kamy.saatApp.R

class TasbihSoundPlayer(context: Context) {
    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(4)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val clickSoundId: Int = soundPool.load(context, R.raw.tasbih_click, 1)
    private val stopSoundId: Int = soundPool.load(context, R.raw.tasbih_stop, 1)

    fun playClick() {
        runCatching {
            soundPool.play(clickSoundId, 1.0f, 1.0f, 1, 0, 1.0f)
        }
    }

    fun playStop() {
        runCatching {
            soundPool.play(stopSoundId, 1.0f, 1.0f, 1, 0, 1.0f)
        }
    }

    fun release() {
        runCatching {
            soundPool.release()
        }
    }
}

@Composable
fun rememberTasbihSoundPlayer(): TasbihSoundPlayer {
    val context = LocalContext.current.applicationContext
    val player = remember(context) { TasbihSoundPlayer(context) }
    DisposableEffect(player) {
        onDispose {
            player.release()
        }
    }
    return player
}
