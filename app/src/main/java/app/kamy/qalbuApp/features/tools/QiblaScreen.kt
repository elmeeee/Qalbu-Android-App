package app.kamy.qalbuApp.features.tools

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import app.kamy.qalbuApp.ui.feedback.rememberConfirmHaptic
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import app.kamy.qalbuApp.R
import app.kamy.qalbuApp.design.theme.AlKhatibColors
import app.kamy.qalbuApp.design.theme.AlKhatibSpacing
import app.kamy.qalbuApp.domain.tools.QiblaCalculator
import app.kamy.qalbuApp.infrastructure.notifications.PrayerScheduleCache
import app.kamy.qalbuApp.infrastructure.preferences.LocationPreferencesStore
import app.kamy.qalbuApp.ui.layout.tabContentStatusBarInset
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlin.math.roundToInt

private enum class QiblaMode { AR, Compass3D }

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun QiblaScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val location = remember {
        LocationPreferencesStore.from(context).manualLocation()?.let { it.latitude to it.longitude }
            ?: PrayerScheduleCache.loadCoordinates(context)
    }
    val bearing = remember(location) {
        location?.let { (lat, lng) -> QiblaCalculator.bearingToKaaba(lat, lng) } ?: 0f
    }
    var deviceAzimuth by remember { mutableFloatStateOf(0f) }
    var mode by remember { mutableIntStateOf(QiblaMode.AR.ordinal) }
    val cameraPermission = rememberPermissionState(android.Manifest.permission.CAMERA)

    DisposableEffect(context) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val rotation = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
            ?: sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR)
        if (rotation == null) {
            onDispose { }
        } else {
            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    val rotationMatrix = FloatArray(9)
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                    val orientation = FloatArray(3)
                    SensorManager.getOrientation(rotationMatrix, orientation)
                    deviceAzimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
                }
                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
            }
            sensorManager.registerListener(listener, rotation, SensorManager.SENSOR_DELAY_GAME)
            onDispose { sensorManager.unregisterListener(listener) }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .tabContentStatusBarInset()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AlKhatibSpacing.screenHorizontal, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back), tint = Color.White)
            }
            Text(
                text = stringResource(R.string.qibla_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            SegmentedButton(
                selected = mode == QiblaMode.AR.ordinal,
                onClick = {
                    mode = QiblaMode.AR.ordinal
                    if (!cameraPermission.status.isGranted) cameraPermission.launchPermissionRequest()
                },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
            ) { Text(stringResource(R.string.qibla_mode_ar)) }
            SegmentedButton(
                selected = mode == QiblaMode.Compass3D.ordinal,
                onClick = { mode = QiblaMode.Compass3D.ordinal },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
            ) { Text(stringResource(R.string.qibla_mode_3d)) }
        }

        if (location == null) {
            Text(
                text = stringResource(R.string.qibla_location_required),
                color = Color.White.copy(alpha = 0.85f),
                modifier = Modifier.padding(24.dp),
                textAlign = TextAlign.Center
            )
            return@Column
        }

        val performConfirmHaptic = rememberConfirmHaptic()
    val needleRotation = bearing - deviceAzimuth
    val aligned = remember(needleRotation) {
        val normalized = ((needleRotation % 360f) + 360f) % 360f
        normalized <= 8f || normalized >= 352f
    }
    var alignedHapticSent by remember { mutableStateOf(false) }

    LaunchedEffect(aligned) {
        if (aligned && !alignedHapticSent) {
            performConfirmHaptic()
            alignedHapticSent = true
        } else if (!aligned) {
            alignedHapticSent = false
        }
    }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (mode == QiblaMode.AR.ordinal && cameraPermission.status.isGranted) {
                QiblaCameraPreview(modifier = Modifier.fillMaxSize())
            } else {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                listOf(Color(0xFF0F172A), Color(0xFF020617))
                            )
                        )
                )
            }

            QiblaNeedleOverlay(
                needleRotation = needleRotation,
                bearing = bearing,
                modifier = Modifier.fillMaxSize()
            )
        }

        Text(
            text = if (aligned) {
                stringResource(R.string.qibla_aligned)
            } else {
                stringResource(R.string.qibla_bearing, bearing.roundToInt())
            },
            color = if (aligned) AlKhatibColors.GoldBright else AlKhatibColors.GoldBright,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun QiblaCameraPreview(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            PreviewView(ctx).apply {
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            }
        },
        update = { previewView ->
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }
                runCatching {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview
                    )
                }
            }, ContextCompat.getMainExecutor(context))
        }
    )
}

@Composable
private fun QiblaNeedleOverlay(
    needleRotation: Float,
    bearing: Float,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(260.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            drawCircle(
                color = Color.White.copy(alpha = 0.12f),
                radius = size.minDimension / 2f,
                center = center,
                style = Stroke(width = 3f)
            )
            rotate(needleRotation, center) {
                drawLine(
                    color = Color(0xFFD97706),
                    start = center,
                    end = Offset(center.x, center.y - size.height * 0.38f),
                    strokeWidth = 8f
                )
                drawCircle(color = Color(0xFFD97706), radius = 10f, center = center)
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 48.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.15f))
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text(
                text = stringResource(R.string.qibla_bearing_compass, bearing.roundToInt()),
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
