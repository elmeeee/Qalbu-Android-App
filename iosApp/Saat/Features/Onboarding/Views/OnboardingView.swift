//
//  OnboardingView.swift
//  Saat
//
//  Created by Elmee on 25/04/2026.
//  Copyright © 2026 Elmee. All rights reserved.
//

import SwiftUI
import CoreLocation
import UserNotifications

class OnboardingLocationDelegate: NSObject, CLLocationManagerDelegate {
    let onStatusChanged: (CLAuthorizationStatus) -> Void
    
    init(onStatusChanged: @escaping (CLAuthorizationStatus) -> Void) {
        self.onStatusChanged = onStatusChanged
    }
    
    func locationManagerDidChangeAuthorization(_ manager: CLLocationManager) {
        onStatusChanged(manager.authorizationStatus)
    }
    
    func locationManager(_ manager: CLLocationManager, didChangeAuthorization status: CLAuthorizationStatus) {
        onStatusChanged(status)
    }
}

struct OnboardingView: View {
    @AppStorage("hasCompletedOnboarding") private var hasCompletedOnboarding = false
    @ObservedObject private var languageManager = AppLanguageManager.shared
    
    @AppStorage("use_manual_location") private var useManualLocation = false
    @AppStorage("manual_latitude") private var manualLatitude = 3.1390
    @AppStorage("manual_longitude") private var manualLongitude = 101.6869
    @AppStorage("manual_city_name") private var manualCityName = "Kuala Lumpur"

    // Individual prayer notification preferences
    @AppStorage("fajrNotificationsEnabled") private var fajrEnabled = true
    @AppStorage("dhuhrNotificationsEnabled") private var dhuhrEnabled = true
    @AppStorage("asrNotificationsEnabled") private var asrEnabled = true
    @AppStorage("maghribNotificationsEnabled") private var maghribEnabled = true
    @AppStorage("ishaNotificationsEnabled") private var ishaEnabled = true
    
    @State private var step: Int = 1
    @State private var locationManager: CLLocationManager?
    @State private var locationDelegate: OnboardingLocationDelegate?
    @State private var isRequestingLocation = false
    @State private var locationQuery: String = ""
    @State private var locationError: String? = nil
    @State private var savingLocation = false
    
    var body: some View {
        ZStack {
            // Dark forest green background matching Android theme
            LinearGradient(
                colors: [Color(hex: "#0F4C3A"), Color(hex: "#062118")],
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
            .ignoresSafeArea()
            
            VStack(spacing: 0) {
                // Top Progress indicator
                progressBar
                    .padding(.horizontal, 24)
                    .padding(.top, 24)
                
                Spacer()
                
                // Screen content
                Group {
                    switch step {
                    case 1:
                        welcomeStep
                    case 2:
                        locationStep
                    case 3:
                        notificationsStep
                    case 4:
                        prayerNotificationsStep
                    default:
                        widgetStep
                    }
                }
                .transition(.opacity.combined(with: .move(edge: .trailing)))
                
                Spacer()
                
                // Bottom actions container
                bottomControls
                    .padding(.horizontal, 24)
                    .padding(.bottom, 36)
            }
        }
        .animation(.spring(response: 0.4, dampingFraction: 0.8), value: step)
    }
    
    private var progressBar: some View {
        VStack(alignment: .leading, spacing: 8) {
            let stepText = String(format: languageManager.localize("onboarding_step"), step)
            Text(stepText)
                .font(.caption.weight(.bold))
                .foregroundColor(.white.opacity(0.75))
            
            HStack(spacing: 8) {
                ForEach(0..<5, id: \.self) { index in
                    let isCompletedOrCurrent = index < step
                    RoundedRectangle(cornerRadius: 3)
                        .fill(isCompletedOrCurrent ? Color(hex: "#D4A017") : Color.white.opacity(0.2))
                        .frame(height: 6)
                }
            }
        }
    }
    
    private var welcomeStep: some View {
        VStack(spacing: 24) {
            Text("☪")
                .font(.system(size: 72))
                .foregroundColor(Color(hex: "#D4A017")) // GoldBright
            
            VStack(spacing: 12) {
                Text(languageManager.localize("onboarding_welcome_title"))
                    .font(.title2.weight(.bold))
                    .foregroundColor(.white)
                    .multilineTextAlignment(.center)
                
                Text(languageManager.localize("onboarding_welcome_subtitle"))
                    .font(.body)
                    .foregroundColor(.white.opacity(0.82))
                    .multilineTextAlignment(.center)
                    .padding(.horizontal, 16)
                    .lineSpacing(4)
            }
            
            VStack(spacing: 12) {
                Text(languageManager.localize("onboarding_select_language"))
                    .font(.subheadline.weight(.semibold))
                    .foregroundColor(Color(hex: "#D4A017"))
                
                HStack(spacing: 12) {
                    ForEach(AppLanguage.allCases) { lang in
                        Button {
                            withAnimation {
                                languageManager.currentLanguage = lang
                            }
                        } label: {
                            Text(lang.displayName)
                                .font(.subheadline.weight(.semibold))
                                .padding(.horizontal, 16)
                                .padding(.vertical, 10)
                                .background(
                                    RoundedRectangle(cornerRadius: 10)
                                        .fill(languageManager.currentLanguage == lang ? Color(hex: "#D4A017") : Color.white.opacity(0.15))
                                )
                                .foregroundColor(languageManager.currentLanguage == lang ? Color(hex: "#0F4C3A") : .white)
                                .overlay(
                                    RoundedRectangle(cornerRadius: 10)
                                        .stroke(Color.white.opacity(0.3), lineWidth: languageManager.currentLanguage == lang ? 0 : 0.5)
                                )
                        }
                        .buttonStyle(.plain)
                    }
                }
            }
            .padding(.top, 16)
        }
        .padding(.horizontal, 24)
    }
    
    private var locationStep: some View {
        VStack(spacing: 20) {
            Text(languageManager.localize("onboarding_location_title"))
                .font(.title2.weight(.bold))
                .foregroundColor(.white)
                .multilineTextAlignment(.center)
            
            Text(languageManager.localize("onboarding_location_subtitle"))
                .font(.body)
                .foregroundColor(.white.opacity(0.8))
                .multilineTextAlignment(.center)
                .padding(.horizontal, 16)
                .lineSpacing(4)
            
            VStack(alignment: .leading, spacing: 8) {
                TextField(
                    "",
                    text: $locationQuery,
                    prompt: Text(languageManager.localize("onboarding_location_city_hint"))
                        .foregroundColor(.white.opacity(0.5))
                )
                .padding(.horizontal, 16)
                .padding(.vertical, 12)
                .background(RoundedRectangle(cornerRadius: 10).fill(Color.white.opacity(0.1)))
                .overlay(
                    RoundedRectangle(cornerRadius: 10)
                        .stroke(locationError != nil ? Color.red : (locationQuery.isEmpty ? Color.white.opacity(0.3) : Color(hex: "#D4A017")), lineWidth: 1)
                )
                .foregroundColor(.white)
                .font(.body)
                .submitLabel(.done)
                .onSubmit {
                    saveManualLocation()
                }
                
                if let error = locationError {
                    Text(error)
                        .font(.caption)
                        .foregroundColor(.red)
                        .padding(.horizontal, 4)
                }
            }
            .padding(.horizontal, 16)
            
            Button {
                let status = CLLocationManager().authorizationStatus
                if status == .notDetermined {
                    isRequestingLocation = true
                    requestLocationPermission()
                } else if status == .denied || status == .restricted {
                    locationError = languageManager.localize("location_failed")
                } else {
                    useManualLocation = false
                    withAnimation {
                        step = 3
                    }
                }
            } label: {
                HStack {
                    if isRequestingLocation || savingLocation {
                        ProgressView()
                            .tint(.white)
                            .padding(.trailing, 8)
                    }
                    Text(languageManager.localize("onboarding_use_gps"))
                        .font(.headline)
                }
                .frame(maxWidth: .infinity)
                .padding(.vertical, 12)
                .background(RoundedRectangle(cornerRadius: 10).stroke(Color.white.opacity(0.6), lineWidth: 1))
                .foregroundColor(.white)
            }
            .buttonStyle(.plain)
            .disabled(isRequestingLocation || savingLocation)
            .padding(.horizontal, 16)
        }
        .padding(.horizontal, 24)
    }
    
    private var notificationsStep: some View {
        VStack(spacing: 24) {
            ZStack {
                Circle()
                    .fill(Color.white.opacity(0.1))
                    .frame(width: 120, height: 120)
                
                Image(systemName: "bell.fill")
                    .font(.system(size: 48, weight: .semibold))
                    .foregroundColor(Color(hex: "#D4A017")) // GoldBright
            }
            
            VStack(spacing: 12) {
                Text(languageManager.localize("onboarding_notifications_title"))
                    .font(.title2.weight(.bold))
                    .foregroundColor(.white)
                    .multilineTextAlignment(.center)
                
                Text(languageManager.localize("onboarding_notifications_subtitle"))
                    .font(.body)
                    .foregroundColor(.white.opacity(0.8))
                    .multilineTextAlignment(.center)
                    .padding(.horizontal, 16)
                    .lineSpacing(4)
            }
        }
        .padding(.horizontal, 24)
    }
    
    private var prayerNotificationsStep: some View {
        VStack(spacing: 20) {
            Text(languageManager.localize("onboarding_prayer_config_title"))
                .font(.title2.weight(.bold))
                .foregroundColor(.white)
                .multilineTextAlignment(.center)
            
            Text(languageManager.localize("onboarding_prayer_config_body"))
                .font(.body)
                .foregroundColor(.white.opacity(0.8))
                .multilineTextAlignment(.center)
                .padding(.horizontal, 16)
                .lineSpacing(4)
            
            VStack(spacing: 12) {
                let prayersList = [
                    (languageManager.localize("prayer_fajr"), $fajrEnabled),
                    (languageManager.localize("prayer_dhuhr"), $dhuhrEnabled),
                    (languageManager.localize("prayer_asr"), $asrEnabled),
                    (languageManager.localize("prayer_maghrib"), $maghribEnabled),
                    (languageManager.localize("prayer_isha"), $ishaEnabled)
                ]
                
                ForEach(0..<prayersList.count, id: \.self) { index in
                    let item = prayersList[index]
                    Toggle(isOn: item.1) {
                        Text(item.0)
                            .font(.headline)
                            .foregroundColor(.white)
                    }
                    .toggleStyle(SwitchToggleStyle(tint: Color(hex: "#D4A017")))
                }
            }
            .padding(.horizontal, 16)
            .padding(.top, 12)
        }
        .padding(.horizontal, 24)
    }
    
    private var widgetStep: some View {
        VStack(spacing: 24) {
            ZStack {
                Circle()
                    .fill(Color.white.opacity(0.1))
                    .frame(width: 120, height: 120)
                
                Image(systemName: "square.text.square.fill")
                    .font(.system(size: 48, weight: .semibold))
                    .foregroundColor(Color(hex: "#D4A017")) // GoldBright
            }
            
            VStack(spacing: 12) {
                Text(languageManager.localize("onboarding_widgets_title"))
                    .font(.title2.weight(.bold))
                    .foregroundColor(.white)
                    .multilineTextAlignment(.center)
                
                Text(languageManager.localize("onboarding_widgets_subtitle"))
                    .font(.body)
                    .foregroundColor(.white.opacity(0.8))
                    .multilineTextAlignment(.center)
                    .padding(.horizontal, 16)
                    .lineSpacing(4)
            }
        }
        .padding(.horizontal, 24)
    }
    
    private var bottomControls: some View {
        HStack(spacing: 12) {
            if step > 1 {
                Button {
                    handleSecondaryAction()
                } label: {
                    Text(secondaryButtonTitle)
                        .font(.headline)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 14)
                        .background(
                            RoundedRectangle(cornerRadius: 12)
                                .stroke(Color(hex: "#D4A017").opacity(0.65), lineWidth: 1)
                        )
                        .foregroundColor(Color(hex: "#D4A017"))
                }
                .buttonStyle(.plain)
                .disabled(isRequestingLocation || savingLocation)
            }
            
            Button {
                handlePrimaryAction()
            } label: {
                Text(primaryButtonTitle)
                    .frame(maxWidth: .infinity)
                    .font(.headline)
                    .padding(.vertical, 14)
                    .background(Color(hex: "#D4A017")) // GoldBright
                    .foregroundColor(Color(hex: "#0F4C3A")) // DeepEmerald
                    .cornerRadius(12)
            }
            .buttonStyle(.plain)
            .disabled(isRequestingLocation || savingLocation)
            .opacity((isRequestingLocation || savingLocation) ? 0.6 : 1.0)
        }
    }
    
    private var primaryButtonTitle: String {
        switch step {
        case 1: return languageManager.localize("onboarding_continue")
        case 2: return languageManager.localize("onboarding_continue")
        case 3: return languageManager.localize("onboarding_enable_notifications")
        case 4: return languageManager.localize("onboarding_continue")
        default: return languageManager.localize("onboarding_get_started")
        }
    }
    
    private var secondaryButtonTitle: String {
        switch step {
        case 2: return languageManager.localize("onboarding_skip_location")
        case 3: return languageManager.localize("onboarding_skip_notifications")
        case 4: return languageManager.localize("onboarding_skip")
        default: return ""
        }
    }
    
    private func handlePrimaryAction() {
        switch step {
        case 1:
            withAnimation {
                step = 2
            }
        case 2:
            if !locationQuery.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
                saveManualLocation()
            } else {
                requestLocationPermission()
            }
        case 3:
            requestNotificationPermission {
                DispatchQueue.main.async {
                    withAnimation {
                        step = 4
                    }
                }
            }
        case 4:
            withAnimation {
                step = 5
            }
        default:
            PrayerNotificationPreferences.notifyDidChange()
            hasCompletedOnboarding = true
        }
    }
    
    private func handleSecondaryAction() {
        if step < 5 {
            withAnimation {
                step += 1
            }
        }
    }
    
    private func saveManualLocation() {
        let query = locationQuery.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !query.isEmpty else { return }
        
        savingLocation = true
        locationError = nil
        
        let geocoder = CLGeocoder()
        geocoder.geocodeAddressString(query) { placemarks, error in
            DispatchQueue.main.async {
                self.savingLocation = false
                if error != nil {
                    self.locationError = self.languageManager.localize("onboarding_location_search_not_found")
                    return
                }
                guard let placemark = placemarks?.first, let location = placemark.location else {
                    self.locationError = self.languageManager.localize("onboarding_location_search_not_found")
                    return
                }
                
                self.manualLatitude = location.coordinate.latitude
                self.manualLongitude = location.coordinate.longitude
                self.manualCityName = placemark.locality ?? placemark.name ?? query
                self.useManualLocation = true
                
                withAnimation {
                    self.step = 3
                }
            }
        }
    }
    
    private func requestLocationPermission() {
        let manager = CLLocationManager()
        isRequestingLocation = true
        let delegate = OnboardingLocationDelegate { status in
            if status != .notDetermined {
                DispatchQueue.main.async {
                    self.isRequestingLocation = false
                    self.useManualLocation = false
                    withAnimation {
                        self.step = 3
                    }
                }
            }
        }
        manager.delegate = delegate
        self.locationDelegate = delegate
        self.locationManager = manager
        manager.requestWhenInUseAuthorization()
    }
    
    private func requestNotificationPermission(completion: @escaping @Sendable () -> Void) {
        UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .sound, .badge]) { _, _ in
            completion()
        }
    }
}
