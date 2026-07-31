package app.kamy.saatApp.features.tools

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import app.kamy.saatApp.R
import app.kamy.saatApp.design.theme.SaatColors
import app.kamy.saatApp.domain.tools.QiblaCalculator
import app.kamy.saatApp.infrastructure.notifications.PrayerScheduleCache
import app.kamy.saatApp.infrastructure.preferences.LocationMode
import app.kamy.saatApp.infrastructure.preferences.LocationPreferencesStore
import app.kamy.saatApp.ui.feedback.rememberConfirmHaptic
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun QiblaScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val locationStore = remember(context) { LocationPreferencesStore.from(context) }
    val isManual = remember(locationStore) { locationStore.mode() == LocationMode.MANUAL }

    val location = remember(context, isManual) {
        if (isManual) {
            locationStore.manualLocation()?.let { it.latitude to it.longitude }
                ?: PrayerScheduleCache.loadCoordinates(context)
        } else {
            PrayerScheduleCache.loadCoordinates(context)
        }
    }

    val locationLabel = remember(context, isManual) {
        if (isManual) {
            locationStore.manualLocation()?.label?.takeIf { it.isNotBlank() }
                ?: PrayerScheduleCache.loadMeta(context)?.cityLabel?.takeIf { it.isNotBlank() }
                ?: "Location"
        } else {
            PrayerScheduleCache.loadMeta(context)?.cityLabel?.takeIf { it.isNotBlank() }
                ?: locationStore.displayLabel()?.takeIf { it.isNotBlank() }
                ?: "Location"
        }
    }

    val bearing = remember(location) {
        location?.let { (lat, lng) -> QiblaCalculator.bearingToKaaba(lat, lng) } ?: 0f
    }

    val distanceKm = remember(location) {
        location?.let { (lat, lng) -> QiblaCalculator.distanceToKaabaKm(lat, lng) } ?: 0.0
    }

    val formattedDistance = remember(distanceKm) {
        val numberFormat = NumberFormat.getNumberInstance(Locale.getDefault()).apply {
            maximumFractionDigits = 0
        }
        numberFormat.format(distanceKm)
    }

    var deviceAzimuth by remember { mutableFloatStateOf(0f) }
    val cameraPermission = rememberPermissionState(android.Manifest.permission.CAMERA)

    // Request camera permission on launch
    LaunchedEffect(Unit) {
        if (!cameraPermission.status.isGranted) {
            cameraPermission.launchPermissionRequest()
        }
    }

    // Orientation sensor listener
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
                    var deg = Math.toDegrees(orientation[0].toDouble()).toFloat()
                    if (deg < 0) deg += 360f
                    deviceAzimuth = deg
                }
                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
            }
            sensorManager.registerListener(listener, rotation, SensorManager.SENSOR_DELAY_GAME)
            onDispose { sensorManager.unregisterListener(listener) }
        }
    }

    val needleRotation = (bearing - deviceAzimuth + 360f) % 360f
    val normalizedOffset = remember(needleRotation) {
        if (needleRotation > 180f) needleRotation - 360f else needleRotation
    }
    val aligned = abs(normalizedOffset) <= 8f

    val performConfirmHaptic = rememberConfirmHaptic()
    var alignedHapticSent by remember { mutableStateOf(false) }

    LaunchedEffect(aligned) {
        if (aligned && !alignedHapticSent) {
            performConfirmHaptic()
            alignedHapticSent = true
        } else if (!aligned) {
            alignedHapticSent = false
        }
    }

    val headingDegree = (deviceAzimuth.roundToInt() % 360 + 360) % 360
    val cardinal = remember(headingDegree) { getCardinalDirection(headingDegree.toFloat()) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF070D18))
    ) {
        // Layer 1: Live Camera Preview or Dark Atmospheric Gradient Fallback
        if (cameraPermission.status.isGranted) {
            QiblaCameraPreview(modifier = Modifier.fillMaxSize())
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF0F172A), Color(0xFF070D18))
                        )
                    )
            )
        }

        // Layer 2: Subtle Radial Vignette Gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.55f),
                            Color.Black.copy(alpha = 0.20f),
                            Color.Black.copy(alpha = 0.70f)
                        )
                    )
                )
        )

        // Layer 3: Clean Floating AR Qibla Indicator (No Moving Green Card Base!)
        QiblaARPerspectiveView(
            normalizedOffset = normalizedOffset,
            aligned = aligned,
            modifier = Modifier.fillMaxSize()
        )

        // Layer 4: Top Glassmorphic Navigation Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.45f))
                        .border(0.5.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                        tint = Color.White
                    )
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.qibla_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Location Badge Chip
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.Black.copy(alpha = 0.45f),
                border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.25f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = SaatColors.GoldDeep,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = locationLabel,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Layer 5: Top Guidance Banner
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 68.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.Black.copy(alpha = 0.45f),
                border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.2f))
            ) {
                Text(
                    text = if (aligned) "✦ Ka'bah Terdeteksi!" else stringResource(R.string.qibla_point_phone),
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                    fontWeight = if (aligned) FontWeight.Bold else FontWeight.Medium,
                    color = if (aligned) SaatColors.GoldDeep else Color.White.copy(alpha = 0.95f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)
                )
            }
        }

        // Layer 6: Premium Bottom Glassmorphic HUD Card (Guaranteed Floating Above Android Navbar!)
        val statusText = when {
            aligned -> stringResource(R.string.qibla_already_facing)
            normalizedOffset < 0 -> "${abs(normalizedOffset.roundToInt())}° " + stringResource(R.string.qibla_in_your_left)
            else -> "${abs(normalizedOffset.roundToInt())}° " + stringResource(R.string.qibla_in_your_right)
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            shape = RoundedCornerShape(28.dp),
            color = if (aligned) Color(0xF0085E43) else Color(0xEE0F172A),
            border = BorderStroke(
                width = 1.5.dp,
                color = if (aligned) SaatColors.GoldDeep else Color.White.copy(alpha = 0.2f)
            ),
            shadowElevation = 16.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "$headingDegree° $cardinal",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontSize = 34.sp,
                                fontWeight = FontWeight.Black
                            ),
                            color = Color.White
                        )
                        Text(
                            text = "Arah Qibla: ${bearing.roundToInt()}° • $formattedDistance km ke Makkah",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Alignment Status Badge
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (aligned) SaatColors.GoldDeep else Color.White.copy(alpha = 0.14f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (aligned) Icons.Filled.CheckCircle else Icons.Filled.Navigation,
                                contentDescription = null,
                                tint = if (aligned) Color.Black else Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = statusText,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (aligned) Color.Black else Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QiblaARPerspectiveView(
    normalizedOffset: Float,
    aligned: Boolean,
    modifier: Modifier = Modifier
) {
    // Continuous pulsing animation for directional arrows
    val infiniteTransition = rememberInfiniteTransition(label = "ar_arrow_pulse")
    val arrowOffsetAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -26f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "arrow_slide"
    )
    val arrowAlphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "arrow_alpha"
    )

    // Smooth animated rotation angle toward Kaaba
    val rotationAngle by animateFloatAsState(
        targetValue = normalizedOffset.coerceIn(-60f, 60f),
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "qibla_rotation_anim"
    )

    val shiftPx = (rotationAngle / 45f).coerceIn(-1.2f, 1.2f) * 140f

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Perspective Pivot Container (NO ugly solid green card base!)
        Column(
            modifier = Modifier
                .graphicsLayer {
                    rotationZ = rotationAngle
                    transformOrigin = TransformOrigin(0.5f, 0.85f)
                    translationX = shiftPx
                }
                .padding(bottom = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Floating Kaaba Badge at Apex with Golden Aura Glow
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(
                        if (aligned) {
                            Brush.radialGradient(
                                colors = listOf(
                                    SaatColors.GoldDeep.copy(alpha = 0.55f),
                                    SaatColors.GoldDeep.copy(alpha = 0.15f),
                                    Color.Transparent
                                )
                            )
                        } else {
                            Brush.radialGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.25f),
                                    Color.Transparent
                                )
                            )
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(
                            if (aligned) SaatColors.GoldDeep.copy(alpha = 0.25f)
                            else Color.Black.copy(alpha = 0.45f)
                        )
                        .border(
                            width = if (aligned) 2.dp else 1.dp,
                            color = if (aligned) SaatColors.GoldDeep else Color.White.copy(alpha = 0.3f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.kaba_qibal_icon),
                        contentDescription = null,
                        modifier = Modifier.size(64.dp)
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            // Stack of Animated Directional Arrows (arow_icon) pointing toward Kaaba
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.offset(y = arrowOffsetAnim.dp)
            ) {
                repeat(4) { idx ->
                    val opacity = (arrowAlphaAnim * (1f - idx * 0.18f)).coerceIn(0.2f, 1f)
                    Image(
                        painter = painterResource(R.drawable.arow_icon),
                        contentDescription = null,
                        modifier = Modifier
                            .size(28.dp)
                            .scale(1f - idx * 0.12f),
                        alpha = opacity
                    )
                }
            }
        }
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

private fun getCardinalDirection(azimuth: Float): String {
    val deg = (azimuth % 360 + 360) % 360
    return when {
        deg >= 337.5 || deg < 22.5 -> "N"
        deg >= 22.5 && deg < 67.5 -> "NE"
        deg >= 67.5 && deg < 112.5 -> "E"
        deg >= 112.5 && deg < 157.5 -> "SE"
        deg >= 157.5 && deg < 202.5 -> "S"
        deg >= 202.5 && deg < 247.5 -> "SW"
        deg >= 247.5 && deg < 292.5 -> "W"
        deg >= 292.5 && deg < 337.5 -> "NW"
        else -> "NW"
    }
}
