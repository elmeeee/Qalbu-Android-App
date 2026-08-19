package app.kamy.saatApp.ui.feedback

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import app.kamy.saatApp.R

class TasbihSoundPlayer(private val context: Context) {
    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(4)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    @Volatile
    private var clickLoaded = false

    @Volatile
    private var stopLoaded = false

    private val clickSoundId: Int
    private val stopSoundId: Int

    init {
        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) {
                if (sampleId == clickSoundId) clickLoaded = true
                if (sampleId == stopSoundId) stopLoaded = true
            }
        }
        clickSoundId = soundPool.load(context, R.raw.tasbih_click, 1)
        stopSoundId = soundPool.load(context, R.raw.tasbih_stop, 1)
    }

    fun playClick() {
        runCatching {
            val streamId = if (clickLoaded) {
                soundPool.play(clickSoundId, 1.0f, 1.0f, 1, 0, 1.0f)
            } else 0
            if (streamId == 0) {
                val mp = MediaPlayer.create(context, R.raw.tasbih_click)
                mp?.setOnCompletionListener { player -> player.release() }
                mp?.start()
            }
        }
    }

    fun playStop() {
        runCatching {
            val streamId = if (stopLoaded) {
                soundPool.play(stopSoundId, 1.0f, 1.0f, 1, 0, 1.0f)
            } else 0
            if (streamId == 0) {
                val mp = MediaPlayer.create(context, R.raw.tasbih_stop)
                mp?.setOnCompletionListener { player -> player.release() }
                mp?.start()
            }
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
