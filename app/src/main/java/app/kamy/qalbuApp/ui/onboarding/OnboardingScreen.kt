package app.kamy.qalbuApp.ui.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.kamy.qalbuApp.R
import app.kamy.qalbuApp.design.theme.AlKhatibColors
import app.kamy.qalbuApp.design.theme.AlKhatibSpacing

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    vm: OnboardingViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsState()
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { vm.onLocationPermissionResult(it.values.any { granted -> granted }) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { vm.onNotificationPermissionResult() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(AlKhatibColors.DeepEmerald, AlKhatibColors.ForestDeeper)
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = AlKhatibSpacing.screenHorizontal, vertical = AlKhatibSpacing.lg),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (state.step) {
                OnboardingStep.WELCOME -> WelcomeStep()
                OnboardingStep.LOCATION -> LocationStep(
                    query = state.locationQuery,
                    saving = state.savingLocation,
                    error = state.locationError,
                    onQueryChange = vm::updateLocationQuery,
                    onUseGps = {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                )
                OnboardingStep.NOTIFICATIONS -> NotificationsStep()
                OnboardingStep.WIDGET -> WidgetStep()
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = {
                    when (state.step) {
                        OnboardingStep.WELCOME -> vm.nextStep()
                        OnboardingStep.LOCATION -> {
                            if (state.locationQuery.isNotBlank()) {
                                vm.saveManualLocation()
                            } else {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        }
                        OnboardingStep.NOTIFICATIONS -> {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                vm.nextStep()
                            }
                        }
                        OnboardingStep.WIDGET -> {
                            vm.completeOnboarding()
                            onFinished()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.savingLocation
            ) {
                Text(
                    when (state.step) {
                        OnboardingStep.WIDGET -> stringResource(R.string.onboarding_get_started)
                        OnboardingStep.NOTIFICATIONS -> stringResource(R.string.onboarding_enable_notifications)
                        OnboardingStep.LOCATION -> stringResource(R.string.onboarding_continue)
                        OnboardingStep.WELCOME -> stringResource(R.string.onboarding_continue)
                    }
                )
            }
            if (state.step != OnboardingStep.WELCOME) {
                OutlinedButton(
                    onClick = {
                        if (state.step == OnboardingStep.NOTIFICATIONS) {
                            vm.nextStep()
                        } else if (state.step == OnboardingStep.LOCATION) {
                            vm.skipLocation()
                        } else {
                            vm.nextStep()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        when (state.step) {
                            OnboardingStep.LOCATION -> stringResource(R.string.onboarding_skip_location)
                            OnboardingStep.NOTIFICATIONS -> stringResource(R.string.onboarding_skip_notifications)
                            else -> stringResource(R.string.onboarding_skip)
                        },
                        color = AlKhatibColors.GoldBright
                    )
                }
            }
        }
    }
}

@Composable
private fun WelcomeStep() {
    Text("☪", style = MaterialTheme.typography.displayMedium, color = AlKhatibColors.GoldBright)
    Spacer(Modifier.height(16.dp))
    Text(
        text = stringResource(R.string.onboarding_welcome_title),
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = androidx.compose.ui.graphics.Color.White,
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(12.dp))
    Text(
        text = stringResource(R.string.onboarding_welcome_body),
        style = MaterialTheme.typography.bodyLarge,
        color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.82f),
        textAlign = TextAlign.Center
    )
}

@Composable
private fun LocationStep(
    query: String,
    saving: Boolean,
    error: String?,
    onQueryChange: (String) -> Unit,
    onUseGps: () -> Unit
) {
    Text(
        text = stringResource(R.string.onboarding_location_title),
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        color = androidx.compose.ui.graphics.Color.White,
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(12.dp))
    Text(
        text = stringResource(R.string.onboarding_location_body),
        style = MaterialTheme.typography.bodyMedium,
        color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f),
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(20.dp))
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(stringResource(R.string.location_city_hint)) },
        singleLine = true,
        isError = error != null,
        supportingText = error?.let { { Text(it) } }
    )
    Spacer(Modifier.height(8.dp))
    OutlinedButton(onClick = onUseGps, modifier = Modifier.fillMaxWidth(), enabled = !saving) {
        Text(stringResource(R.string.location_use_gps))
    }
}

@Composable
private fun NotificationsStep() {
    Text(
        text = stringResource(R.string.onboarding_notifications_title),
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        color = androidx.compose.ui.graphics.Color.White,
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(12.dp))
    Text(
        text = stringResource(R.string.onboarding_notifications_body),
        style = MaterialTheme.typography.bodyMedium,
        color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f),
        textAlign = TextAlign.Center
    )
}

@Composable
private fun WidgetStep() {
    Text(
        text = stringResource(R.string.onboarding_widget_title),
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        color = androidx.compose.ui.graphics.Color.White,
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(12.dp))
    Text(
        text = stringResource(R.string.onboarding_widget_body),
        style = MaterialTheme.typography.bodyMedium,
        color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f),
        textAlign = TextAlign.Center
    )
}
