//
//  OnboardingView.swift
//  Saat
//
//  Created by Sufiandy Elmy on 09/10/2026.
//  Copyright © 2026 Elmee. All rights reserved.
//

import SwiftUI
import CoreLocation
import UserNotifications

private let OnboardingDarkGreen = Color(hex: "#1B4332")
private let OnboardingBgWarm = Color(hex: "#F9F7F2")
private let OnboardingCardBorder = Color(hex: "#EBE5D8")
private let OnboardingSubtext = Color(hex: "#64748B")
private let OnboardingTitleGreen = Color(hex: "#153828")
private let OnboardingSuccessGreen = Color(hex: "#2E7D32")
private let OnboardingSuccessBg = Color(hex: "#EDF5EE")

enum OnboardingStep: Int {
    case language = 1
    case welcome = 2
    case permissions = 3
    case prayerNotifications = 4
}

final class LocationPermissionDelegate: NSObject, CLLocationManagerDelegate {
    var onAuthChanged: ((CLAuthorizationStatus) -> Void)?
    
    func locationManagerDidChangeAuthorization(_ manager: CLLocationManager) {
        onAuthChanged?(manager.authorizationStatus)
    }
}

struct OnboardingView: View {
    @AppStorage("hasCompletedOnboarding") private var hasCompletedOnboarding = false
    @ObservedObject private var languageManager = AppLanguageManager.shared
    
    // Prayer Toggles
    @AppStorage("fajrNotificationsEnabled") private var fajrEnabled = true
    @AppStorage("dhuhrNotificationsEnabled") private var dhuhrEnabled = true
    @AppStorage("asrNotificationsEnabled") private var asrEnabled = true
    @AppStorage("maghribNotificationsEnabled") private var maghribEnabled = true
    @AppStorage("ishaNotificationsEnabled") private var ishaEnabled = true
    
    @State private var step: OnboardingStep = .language
    @State private var locationGranted: Bool = false
    @State private var notificationGranted: Bool = false
    @State private var isFinalizing: Bool = false
    
    @State private var locationManager = CLLocationManager()
    @State private var locationDelegate = LocationPermissionDelegate()
    
    var body: some View {
        ZStack {
            switch step {
            case .language:
                LanguageStepView(
                    selected: languageManager.currentLanguage,
                    onSelect: { lang in
                        languageManager.currentLanguage = lang
                    },
                    onStart: {
                        withAnimation(.easeInOut(duration: 0.25)) {
                            step = .welcome
                        }
                    },
                    onSkip: {
                        finishOnboarding()
                    }
                )
                .transition(.asymmetric(insertion: .move(edge: .trailing).combined(with: .opacity),
                                       removal: .move(edge: .leading).combined(with: .opacity)))
                
            case .welcome:
                WelcomeStepView(
                    language: languageManager.currentLanguage,
                    onBack: {
                        withAnimation(.easeInOut(duration: 0.25)) {
                            step = .language
                        }
                    },
                    onContinue: {
                        withAnimation(.easeInOut(duration: 0.25)) {
                            step = .permissions
                        }
                    },
                    onSkip: {
                        finishOnboarding()
                    }
                )
                .transition(.asymmetric(insertion: .move(edge: .trailing).combined(with: .opacity),
                                       removal: .move(edge: .leading).combined(with: .opacity)))
                
            case .permissions:
                PermissionsStepView(
                    locationGranted: locationGranted,
                    notificationGranted: notificationGranted,
                    onRequestLocation: {
                        requestLocationPermission()
                    },
                    onRequestNotification: {
                        requestNotificationPermission()
                    },
                    onBack: {
                        withAnimation(.easeInOut(duration: 0.25)) {
                            step = .welcome
                        }
                    },
                    onContinue: {
                        withAnimation(.easeInOut(duration: 0.25)) {
                            step = .prayerNotifications
                        }
                    },
                    onSkip: {
                        withAnimation(.easeInOut(duration: 0.25)) {
                            step = .prayerNotifications
                        }
                    }
                )
                .transition(.asymmetric(insertion: .move(edge: .trailing).combined(with: .opacity),
                                       removal: .move(edge: .leading).combined(with: .opacity)))
                
            case .prayerNotifications:
                PrayerNotificationsStepView(
                    fajrEnabled: $fajrEnabled,
                    dhuhrEnabled: $dhuhrEnabled,
                    asrEnabled: $asrEnabled,
                    maghribEnabled: $maghribEnabled,
                    ishaEnabled: $ishaEnabled,
                    onBack: {
                        withAnimation(.easeInOut(duration: 0.25)) {
                            step = .permissions
                        }
                    },
                    onSave: {
                        finishOnboarding()
                    },
                    onSkip: {
                        finishOnboarding()
                    }
                )
                .transition(.asymmetric(insertion: .move(edge: .trailing).combined(with: .opacity),
                                       removal: .move(edge: .leading).combined(with: .opacity)))
            }
            
            // Fullscreen Loading Overlay when finalizing onboarding
            if isFinalizing {
                ZStack {
                    Color(hex: "#F9F7F2")
                        .ignoresSafeArea()
                    
                    VStack(spacing: 20) {
                        ZStack {
                            Circle()
                                .fill(Color.white)
                                .frame(width: 64, height: 64)
                                .shadow(color: Color.black.opacity(0.06), radius: 8, x: 0, y: 3)
                            
                            ProgressView()
                                .progressViewStyle(CircularProgressViewStyle(tint: OnboardingDarkGreen))
                                .scaleEffect(1.3)
                            
                            Image("ic_tasbih_3d")
                                .resizable()
                                .scaledToFit()
                                .frame(width: 32, height: 32)
                        }
                        
                        Text(preparingText)
                            .font(.system(size: 15, weight: .semibold))
                            .foregroundColor(OnboardingDarkGreen)
                            .multilineTextAlignment(.center)
                    }
                }
                .transition(.opacity)
            }
        }
        .onAppear {
            setupLocationDelegate()
            checkExistingPermissions()
        }
    }
    
    private var preparingText: String {
        switch languageManager.currentLanguage {
        case .indonesian:
            return "Menyiapkan aplikasi Anda..."
        case .malay:
            return "Menyediakan aplikasi anda..."
        case .english:
            return "Preparing your experience..."
        }
    }
    
    private func setupLocationDelegate() {
        locationDelegate.onAuthChanged = { status in
            DispatchQueue.main.async {
                self.locationGranted = (status == .authorizedWhenInUse || status == .authorizedAlways)
            }
        }
        locationManager.delegate = locationDelegate
    }
    
    private func checkExistingPermissions() {
        let locStatus = locationManager.authorizationStatus
        locationGranted = (locStatus == .authorizedWhenInUse || locStatus == .authorizedAlways)
        
        Task { @MainActor in
            let settings = await UNUserNotificationCenter.current().notificationSettings()
            self.notificationGranted = (settings.authorizationStatus == .authorized)
        }
    }
    
    private func requestLocationPermission() {
        locationManager.requestWhenInUseAuthorization()
    }
    
    private func requestNotificationPermission() {
        Task { @MainActor in
            do {
                let granted = try await UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .sound, .badge])
                self.notificationGranted = granted
            } catch {
                self.notificationGranted = false
            }
        }
    }
    
    private func finishOnboarding() {
        withAnimation(.easeInOut(duration: 0.2)) {
            isFinalizing = true
        }
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.8) {
            hasCompletedOnboarding = true
        }
    }
}

/// -----------------------------------------------------------------------------
// STEP 1: BRAND INTRO & LANGUAGE SELECTION
// -----------------------------------------------------------------------------
private struct LanguageStepView: View {
    let selected: AppLanguage
    let onSelect: (AppLanguage) -> Void
    let onStart: () -> Void
    let onSkip: () -> Void
    @ObservedObject private var lang = AppLanguageManager.shared
    
    var body: some View {
        GeometryReader { geo in
            ZStack(alignment: .top) {
                // Full Screen Background (Edge to Edge)
                Image("bg_onboarding_1")
                    .resizable()
                    .scaledToFill()
                    .frame(width: geo.size.width, height: geo.size.height + geo.safeAreaInsets.top + geo.safeAreaInsets.bottom)
                    .ignoresSafeArea()
                
                VStack(spacing: 0) {
                    // Top Bar: Skip button sits comfortably below status bar
                    HStack {
                        Spacer()
                        Button(action: onSkip) {
                            Text(lang.localize("onboarding_skip"))
                                .font(.system(size: 13.5, weight: .semibold))
                                .foregroundColor(.white)
                                .padding(.horizontal, 16)
                                .padding(.vertical, 7)
                                .background(Color.black.opacity(0.32))
                                .clipShape(Capsule())
                        }
                    }
                    .padding(.horizontal, 20)
                    .padding(.top, max(geo.safeAreaInsets.top, 20) + 4)
                    
                    Spacer().frame(height: 8)
                    
                    // Translucent Glass Backdrop Card
                    VStack(spacing: 4) {
                        Text(lang.localize("onboarding_brand_title"))
                            .font(.system(size: 32, weight: .bold, design: .serif))
                            .foregroundColor(.white)
                            .multilineTextAlignment(.center)
                            .tracking(1.2)
                        
                        Text(lang.localize("onboarding_brand_subtitle"))
                            .font(.system(size: 12.5))
                            .lineSpacing(2.5)
                            .foregroundColor(.white.opacity(0.95))
                            .multilineTextAlignment(.center)
                    }
                    .padding(.horizontal, 18)
                    .padding(.vertical, 10)
                    .frame(maxWidth: .infinity)
                    .background(
                        RoundedRectangle(cornerRadius: 20, style: .continuous)
                            .fill(Color.black.opacity(0.32))
                            .overlay(
                                RoundedRectangle(cornerRadius: 20, style: .continuous)
                                    .stroke(Color.white.opacity(0.25), lineWidth: 1)
                            )
                    )
                    .padding(.horizontal, 20)
                    
                    // Middle space for character illustration
                    Spacer(minLength: 12)
                    
                    // Bottom White Sheet (Hugs bottom screen edge seamlessly)
                    VStack(alignment: .leading, spacing: 12) {
                        VStack(alignment: .leading, spacing: 2) {
                            Text(lang.localize("onboarding_lang_card_title"))
                                .font(.system(size: 16, weight: .bold))
                                .foregroundColor(OnboardingTitleGreen)
                            
                            Text(lang.localize("onboarding_lang_card_subtitle"))
                                .font(.system(size: 12))
                                .foregroundColor(OnboardingSubtext)
                        }
                        
                        // 3 Language Selector Chips
                        HStack(spacing: 8) {
                            LanguageChip(
                                title: selected == .english ? "Indonesian" : "Indonesia",
                                flagImage: "ic_flag_id",
                                isSelected: selected == .indonesian
                            ) {
                                onSelect(.indonesian)
                            }
                            
                            LanguageChip(
                                title: selected == .english ? "Malay" : "Melayu",
                                flagImage: "ic_flag_ms",
                                isSelected: selected == .malay
                            ) {
                                onSelect(.malay)
                            }
                            
                            LanguageChip(
                                title: selected == .indonesian ? "Inggris" : (selected == .malay ? "Inggeris" : "English"),
                                flagImage: "ic_flag_en",
                                isSelected: selected == .english
                            ) {
                                onSelect(.english)
                            }
                        }
                        
                        // "Mulai ->" Action Button
                        Button(action: onStart) {
                            HStack(spacing: 8) {
                                Text(lang.localize("onboarding_start_button"))
                                    .font(.system(size: 15.5, weight: .bold))
                                    .foregroundColor(.white)
                                
                                Image(systemName: "arrow.right")
                                    .font(.system(size: 13, weight: .bold))
                                    .foregroundColor(.white)
                            }
                            .frame(maxWidth: .infinity)
                            .frame(height: 48)
                            .background(OnboardingDarkGreen)
                            .clipShape(Capsule())
                        }
                        .padding(.top, 2)
                        
                        // Step 1 Footer Text
                        Text(lang.localize("onboarding_step1_footer"))
                            .font(.system(size: 11))
                            .foregroundColor(OnboardingSubtext)
                            .frame(maxWidth: .infinity, alignment: .center)
                            .multilineTextAlignment(.center)
                    }
                    .padding(.horizontal, 20)
                    .padding(.top, 16)
                    .padding(.bottom, max(geo.safeAreaInsets.bottom, 12) + 6)
                    .frame(maxWidth: .infinity)
                    .background(
                        TopRoundedCorner(radius: 26)
                            .fill(Color.white)
                            .shadow(color: Color.black.opacity(0.12), radius: 12, x: 0, y: -4)
                    )
                }
                .frame(width: geo.size.width, height: geo.size.height + geo.safeAreaInsets.top + geo.safeAreaInsets.bottom)
            }
            .ignoresSafeArea()
        }
    }
}

private struct TopRoundedCorner: Shape {
    var radius: CGFloat = 26.0

    func path(in rect: CGRect) -> Path {
        var path = Path()
        path.move(to: CGPoint(x: rect.minX, y: rect.maxY))
        path.addLine(to: CGPoint(x: rect.minX, y: rect.minY + radius))
        path.addArc(center: CGPoint(x: rect.minX + radius, y: rect.minY + radius), radius: radius, startAngle: .degrees(180), endAngle: .degrees(270), clockwise: false)
        path.addLine(to: CGPoint(x: rect.maxX - radius, y: rect.minY))
        path.addArc(center: CGPoint(x: rect.maxX - radius, y: rect.minY + radius), radius: radius, startAngle: .degrees(270), endAngle: .degrees(0), clockwise: false)
        path.addLine(to: CGPoint(x: rect.maxX, y: rect.maxY))
        path.closeSubpath()
        return path
    }
}

private struct LanguageChip: View {
    let title: String
    let flagImage: String
    let isSelected: Bool
    let action: () -> Void
    
    var body: some View {
        Button(action: action) {
            HStack(spacing: 5) {
                Image(flagImage)
                    .resizable()
                    .scaledToFill()
                    .frame(width: 18, height: 18)
                    .clipShape(Circle())
                
                Text(title)
                    .font(.system(size: title.count > 9 ? 11 : 12, weight: isSelected ? .bold : .medium))
                    .foregroundColor(isSelected ? .white : Color(hex: "#334155"))
                    .lineLimit(1)
            }
            .frame(maxWidth: .infinity)
            .frame(height: 44)
            .background(isSelected ? OnboardingDarkGreen : Color(hex: "#FBF9F4"))
            .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
            .overlay(
                RoundedRectangle(cornerRadius: 12, style: .continuous)
                    .stroke(isSelected ? Color.clear : Color(hex: "#E5DFD3"), lineWidth: 1)
            )
        }
        .buttonStyle(.plain)
    }
}

// -----------------------------------------------------------------------------
// STEP 2: KENALAN DENGAN KĪMI
// -----------------------------------------------------------------------------
private struct WelcomeStepView: View {
    let language: AppLanguage
    let onBack: () -> Void
    let onContinue: () -> Void
    let onSkip: () -> Void
    @ObservedObject private var lang = AppLanguageManager.shared
    
    private var bgName: String {
        switch language {
        case .indonesian: return "bg_onboarding_2_id"
        case .malay: return "bg_onboarding_2_ms"
        case .english: return "bg_onboarding_2_en"
        }
    }
    
    var body: some View {
        GeometryReader { geo in
            ZStack(alignment: .top) {
                Image(bgName)
                    .resizable()
                    .scaledToFill()
                    .frame(width: geo.size.width, height: geo.size.height + geo.safeAreaInsets.top + geo.safeAreaInsets.bottom)
                    .ignoresSafeArea()
                
                VStack(spacing: 0) {
                    // Top Bar: Back & Skip with safe area
                    HStack {
                        Button(action: onBack) {
                            HStack(spacing: 4) {
                                Image(systemName: "arrow.left")
                                    .font(.system(size: 13, weight: .bold))
                                Text(lang.localize("onboarding_back"))
                                    .font(.system(size: 13.5, weight: .semibold))
                            }
                            .foregroundColor(.white)
                            .padding(.horizontal, 14)
                            .padding(.vertical, 7)
                            .background(Color.black.opacity(0.32))
                            .clipShape(Capsule())
                        }
                        
                        Spacer()
                        
                        Button(action: onSkip) {
                            Text(lang.localize("onboarding_skip"))
                                .font(.system(size: 13.5, weight: .semibold))
                                .foregroundColor(.white)
                                .padding(.horizontal, 14)
                                .padding(.vertical, 7)
                                .background(Color.black.opacity(0.32))
                                .clipShape(Capsule())
                        }
                    }
                    .padding(.horizontal, 20)
                    .padding(.top, max(geo.safeAreaInsets.top, 20) + 4)
                    
                    Spacer().frame(height: 8)
                    
                    // Translucent Glass Info Card
                    VStack(spacing: 6) {
                        Text(lang.localize("onboarding_kimi_title"))
                            .font(.system(size: 22, weight: .bold, design: .serif))
                            .foregroundColor(.white)
                            .multilineTextAlignment(.center)
                        
                        Text(lang.localize("onboarding_kimi_body"))
                            .font(.system(size: 12.5))
                            .lineSpacing(3)
                            .foregroundColor(.white.opacity(0.95))
                            .multilineTextAlignment(.center)
                    }
                    .padding(.horizontal, 18)
                    .padding(.vertical, 12)
                    .frame(maxWidth: .infinity)
                    .background(
                        RoundedRectangle(cornerRadius: 20, style: .continuous)
                            .fill(Color.black.opacity(0.32))
                            .overlay(
                                RoundedRectangle(cornerRadius: 20, style: .continuous)
                                    .stroke(Color.white.opacity(0.25), lineWidth: 1)
                            )
                    )
                    .padding(.horizontal, 20)
                    
                    Spacer(minLength: 12)
                    
                    // Bottom Action Controls
                    VStack(spacing: 12) {
                        Button(action: onContinue) {
                            HStack(spacing: 8) {
                                Text(lang.localize("onboarding_continue_btn"))
                                    .font(.system(size: 15.5, weight: .bold))
                                    .foregroundColor(.white)
                                
                                Image(systemName: "arrow.right")
                                    .font(.system(size: 13, weight: .bold))
                                    .foregroundColor(.white)
                            }
                            .frame(maxWidth: .infinity)
                            .frame(height: 48)
                            .background(OnboardingDarkGreen)
                            .clipShape(Capsule())
                            .shadow(color: OnboardingDarkGreen.opacity(0.35), radius: 8, x: 0, y: 4)
                        }
                        
                        OnboardingDotsView(activeStep: 2)
                    }
                    .padding(.horizontal, 20)
                    .padding(.bottom, max(geo.safeAreaInsets.bottom, 12) + 8)
                }
                .frame(width: geo.size.width, height: geo.size.height + geo.safeAreaInsets.top + geo.safeAreaInsets.bottom)
            }
            .ignoresSafeArea()
        }
    }
}

// -----------------------------------------------------------------------------
// STEP 3: UNIFIED PERMISSIONS SCREEN
// -----------------------------------------------------------------------------
private struct PermissionsStepView: View {
    let locationGranted: Bool
    let notificationGranted: Bool
    let onRequestLocation: () -> Void
    let onRequestNotification: () -> Void
    let onBack: () -> Void
    let onContinue: () -> Void
    let onSkip: () -> Void
    @ObservedObject private var lang = AppLanguageManager.shared
    
    var body: some View {
        GeometryReader { geo in
            ZStack(alignment: .top) {
                OnboardingBgWarm
                    .ignoresSafeArea()
                
                VStack(spacing: 0) {
                    // Top Bar
                    HStack {
                        Button(action: onBack) {
                            HStack(spacing: 4) {
                                Image(systemName: "arrow.left")
                                    .font(.system(size: 13, weight: .bold))
                                Text(lang.localize("onboarding_back"))
                                    .font(.system(size: 13.5, weight: .semibold))
                            }
                            .foregroundColor(OnboardingTitleGreen)
                            .padding(.horizontal, 14)
                            .padding(.vertical, 7)
                            .background(Color.white)
                            .clipShape(Capsule())
                            .overlay(Capsule().stroke(OnboardingCardBorder, lineWidth: 1))
                        }
                        
                        Spacer()
                        
                        Button(action: onSkip) {
                            Text(lang.localize("onboarding_skip"))
                                .font(.system(size: 13.5, weight: .semibold))
                                .foregroundColor(OnboardingTitleGreen)
                                .padding(.horizontal, 14)
                                .padding(.vertical, 7)
                                .background(Color.white)
                                .clipShape(Capsule())
                                .overlay(Capsule().stroke(OnboardingCardBorder, lineWidth: 1))
                        }
                    }
                    .padding(.horizontal, 20)
                    .padding(.top, max(geo.safeAreaInsets.top, 20) + 4)
                    
                    ScrollView(showsIndicators: false) {
                        VStack(spacing: 12) {
                            Spacer().frame(height: 2)
                            
                            // Header
                            VStack(spacing: 4) {
                                Text(lang.localize("onboarding_perms_title"))
                                    .font(.system(size: 22, weight: .bold, design: .serif))
                                    .foregroundColor(OnboardingTitleGreen)
                                    .multilineTextAlignment(.center)
                                
                                Text(lang.localize("onboarding_perms_subtitle"))
                                    .font(.system(size: 12))
                                    .lineSpacing(2)
                                    .foregroundColor(OnboardingSubtext)
                                    .multilineTextAlignment(.center)
                                    .padding(.horizontal, 8)
                            }
                            
                            // Card 1: Location Access
                            VStack(alignment: .leading, spacing: 10) {
                                HStack(alignment: .top, spacing: 12) {
                                    Image("ic_onboarding_location")
                                        .resizable()
                                        .scaledToFit()
                                        .frame(width: 38, height: 38)
                                    
                                    VStack(alignment: .leading, spacing: 2) {
                                        Text(lang.localize("onboarding_perm_loc_title"))
                                            .font(.system(size: 14, weight: .bold))
                                            .foregroundColor(OnboardingTitleGreen)
                                        
                                        Text(lang.localize("onboarding_perm_loc_desc"))
                                            .font(.system(size: 11.5))
                                            .lineSpacing(2)
                                            .foregroundColor(OnboardingSubtext)
                                    }
                                }
                                
                                if locationGranted {
                                    HStack(spacing: 6) {
                                        Image(systemName: "checkmark.circle.fill")
                                            .font(.system(size: 14, weight: .bold))
                                            .foregroundColor(OnboardingSuccessGreen)
                                        
                                        Text(lang.localize("onboarding_perm_loc_granted"))
                                            .font(.system(size: 13, weight: .bold))
                                            .foregroundColor(OnboardingSuccessGreen)
                                    }
                                    .frame(maxWidth: .infinity)
                                    .frame(height: 40)
                                    .background(OnboardingSuccessBg)
                                    .clipShape(Capsule())
                                } else {
                                    Button(action: onRequestLocation) {
                                        Text(lang.localize("onboarding_perm_loc_btn"))
                                            .font(.system(size: 13, weight: .bold))
                                            .foregroundColor(.white)
                                            .frame(maxWidth: .infinity)
                                            .frame(height: 40)
                                            .background(OnboardingDarkGreen)
                                            .clipShape(Capsule())
                                    }
                                }
                            }
                            .padding(14)
                            .background(
                                RoundedRectangle(cornerRadius: 16, style: .continuous)
                                    .fill(Color.white)
                                    .overlay(
                                        RoundedRectangle(cornerRadius: 16, style: .continuous)
                                            .stroke(OnboardingCardBorder, lineWidth: 1)
                                    )
                                    .shadow(color: Color.black.opacity(0.03), radius: 6, x: 0, y: 2)
                            )
                            
                            // Card 2: Notification Access
                            VStack(alignment: .leading, spacing: 10) {
                                HStack(alignment: .top, spacing: 12) {
                                    Image("ic_onboarding_notification")
                                        .resizable()
                                        .scaledToFit()
                                        .frame(width: 38, height: 38)
                                    
                                    VStack(alignment: .leading, spacing: 2) {
                                        Text(lang.localize("onboarding_perm_notif_title"))
                                            .font(.system(size: 14, weight: .bold))
                                            .foregroundColor(OnboardingTitleGreen)
                                        
                                        Text(lang.localize("onboarding_perm_notif_desc"))
                                            .font(.system(size: 11.5))
                                            .lineSpacing(2)
                                            .foregroundColor(OnboardingSubtext)
                                    }
                                }
                                
                                if notificationGranted {
                                    HStack(spacing: 6) {
                                        Image(systemName: "checkmark.circle.fill")
                                            .font(.system(size: 14, weight: .bold))
                                            .foregroundColor(OnboardingSuccessGreen)
                                        
                                        Text(lang.localize("onboarding_perm_notif_granted"))
                                            .font(.system(size: 13, weight: .bold))
                                            .foregroundColor(OnboardingSuccessGreen)
                                    }
                                    .frame(maxWidth: .infinity)
                                    .frame(height: 40)
                                    .background(OnboardingSuccessBg)
                                    .clipShape(Capsule())
                                } else {
                                    Button(action: onRequestNotification) {
                                        Text(lang.localize("onboarding_perm_notif_btn"))
                                            .font(.system(size: 13, weight: .bold))
                                            .foregroundColor(.white)
                                            .frame(maxWidth: .infinity)
                                            .frame(height: 40)
                                            .background(OnboardingDarkGreen)
                                            .clipShape(Capsule())
                                    }
                                }
                            }
                            .padding(14)
                            .background(
                                RoundedRectangle(cornerRadius: 16, style: .continuous)
                                    .fill(Color.white)
                                    .overlay(
                                        RoundedRectangle(cornerRadius: 16, style: .continuous)
                                            .stroke(OnboardingCardBorder, lineWidth: 1)
                                    )
                                    .shadow(color: Color.black.opacity(0.03), radius: 6, x: 0, y: 2)
                            )
                        }
                        .padding(.horizontal, 20)
                        .padding(.bottom, 8)
                    }
                    
                    // Bottom Actions
                    VStack(spacing: 10) {
                        if locationGranted && notificationGranted {
                            Button(action: onContinue) {
                                HStack(spacing: 6) {
                                    Text(lang.localize("onboarding_continue_btn"))
                                        .font(.system(size: 15, weight: .bold))
                                        .foregroundColor(.white)
                                    Image(systemName: "arrow.right")
                                        .font(.system(size: 13, weight: .bold))
                                        .foregroundColor(.white)
                                }
                                .frame(maxWidth: .infinity)
                                .frame(height: 46)
                                .background(OnboardingDarkGreen)
                                .clipShape(Capsule())
                                .shadow(color: OnboardingDarkGreen.opacity(0.3), radius: 8, x: 0, y: 4)
                            }
                        } else {
                            Button(action: onContinue) {
                                Text(lang.localize("onboarding_perm_later_btn"))
                                    .font(.system(size: 14, weight: .semibold))
                                    .foregroundColor(OnboardingTitleGreen)
                                    .frame(maxWidth: .infinity)
                                    .frame(height: 44)
                                    .background(Color.white)
                                    .clipShape(Capsule())
                                    .overlay(
                                        Capsule()
                                            .stroke(Color(hex: "#D3CCC0"), lineWidth: 1)
                                    )
                            }
                        }
                        
                        // Privacy Note
                        HStack(spacing: 6) {
                            Image("ic_onboarding_privacy")
                                .resizable()
                                .scaledToFit()
                                .frame(width: 18, height: 18)
                            
                            Text(lang.localize("onboarding_privacy_text"))
                                .font(.system(size: 11))
                                .lineSpacing(2)
                                .foregroundColor(OnboardingSubtext)
                        }
                        .padding(.horizontal, 4)
                        
                        OnboardingDotsView(activeStep: 3)
                    }
                    .padding(.horizontal, 20)
                    .padding(.top, 6)
                    .padding(.bottom, max(geo.safeAreaInsets.bottom, 12) + 6)
                }
                .frame(width: geo.size.width, height: geo.size.height + geo.safeAreaInsets.top + geo.safeAreaInsets.bottom)
            }
            .ignoresSafeArea()
        }
    }
}

// -----------------------------------------------------------------------------
// STEP 4: ATUR ADZANMU
// -----------------------------------------------------------------------------
private struct PrayerNotificationsStepView: View {
    @Binding var fajrEnabled: Bool
    @Binding var dhuhrEnabled: Bool
    @Binding var asrEnabled: Bool
    @Binding var maghribEnabled: Bool
    @Binding var ishaEnabled: Bool
    
    let onBack: () -> Void
    let onSave: () -> Void
    let onSkip: () -> Void
    @ObservedObject private var lang = AppLanguageManager.shared
    
    var body: some View {
        GeometryReader { geo in
            ZStack(alignment: .top) {
                OnboardingBgWarm
                    .ignoresSafeArea()
                
                VStack(spacing: 0) {
                    // Top Bar
                    HStack {
                        Button(action: onBack) {
                            HStack(spacing: 4) {
                                Image(systemName: "arrow.left")
                                    .font(.system(size: 13, weight: .bold))
                                Text(lang.localize("onboarding_back"))
                                    .font(.system(size: 13.5, weight: .semibold))
                            }
                            .foregroundColor(OnboardingTitleGreen)
                            .padding(.horizontal, 14)
                            .padding(.vertical, 7)
                            .background(Color.white)
                            .clipShape(Capsule())
                            .overlay(Capsule().stroke(OnboardingCardBorder, lineWidth: 1))
                        }
                        
                        Spacer()
                        
                        Button(action: onSkip) {
                            Text(lang.localize("onboarding_skip"))
                                .font(.system(size: 13.5, weight: .semibold))
                                .foregroundColor(OnboardingTitleGreen)
                                .padding(.horizontal, 14)
                                .padding(.vertical, 7)
                                .background(Color.white)
                                .clipShape(Capsule())
                                .overlay(Capsule().stroke(OnboardingCardBorder, lineWidth: 1))
                        }
                    }
                    .padding(.horizontal, 20)
                    .padding(.top, max(geo.safeAreaInsets.top, 20) + 4)
                    
                    ScrollView(showsIndicators: false) {
                        VStack(spacing: 12) {
                            Spacer().frame(height: 2)
                            
                            // Header
                            VStack(spacing: 4) {
                                Text(lang.localize("onboarding_adhan_new_title"))
                                    .font(.system(size: 22, weight: .bold, design: .serif))
                                    .foregroundColor(OnboardingTitleGreen)
                                    .multilineTextAlignment(.center)
                                
                                Text(lang.localize("onboarding_adhan_new_subtitle"))
                                    .font(.system(size: 12))
                                    .lineSpacing(2)
                                    .foregroundColor(OnboardingSubtext)
                                    .multilineTextAlignment(.center)
                                    .padding(.horizontal, 8)
                            }
                            
                            // Prayer Switches Card
                            VStack(spacing: 0) {
                                PrayerToggleRow(
                                    title: lang.localize("prayer_fajr"),
                                    icon: "ic_onboarding_fajr",
                                    isOn: $fajrEnabled
                                )
                                
                                Divider().background(Color(hex: "#F3EFE8"))
                                
                                PrayerToggleRow(
                                    title: lang.localize("prayer_dhuhr"),
                                    icon: "ic_onboarding_dhuhr",
                                    isOn: $dhuhrEnabled
                                )
                                
                                Divider().background(Color(hex: "#F3EFE8"))
                                
                                PrayerToggleRow(
                                    title: lang.localize("prayer_asr"),
                                    icon: "ic_onboarding_asr",
                                    isOn: $asrEnabled
                                )
                                
                                Divider().background(Color(hex: "#F3EFE8"))
                                
                                PrayerToggleRow(
                                    title: lang.localize("prayer_maghrib"),
                                    icon: "ic_onboarding_maghrib",
                                    isOn: $maghribEnabled
                                )
                                
                                Divider().background(Color(hex: "#F3EFE8"))
                                
                                PrayerToggleRow(
                                    title: lang.localize("prayer_isha"),
                                    icon: "ic_onboarding_isha",
                                    isOn: $ishaEnabled
                                )
                            }
                            .padding(.horizontal, 16)
                            .padding(.vertical, 4)
                            .background(
                                RoundedRectangle(cornerRadius: 16, style: .continuous)
                                    .fill(Color.white)
                                    .overlay(
                                        RoundedRectangle(cornerRadius: 16, style: .continuous)
                                            .stroke(OnboardingCardBorder, lineWidth: 1)
                                    )
                                    .shadow(color: Color.black.opacity(0.03), radius: 6, x: 0, y: 2)
                            )
                            
                            // Adhan Sound Note Card
                            HStack(spacing: 10) {
                                Image(systemName: "speaker.wave.2.fill")
                                    .font(.system(size: 15, weight: .semibold))
                                    .foregroundColor(OnboardingSuccessGreen)
                                
                                Text(lang.localize("onboarding_adhan_sound_note"))
                                    .font(.system(size: 11))
                                    .lineSpacing(2)
                                    .foregroundColor(OnboardingTitleGreen)
                            }
                            .padding(.horizontal, 14)
                            .padding(.vertical, 10)
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .background(
                                RoundedRectangle(cornerRadius: 12, style: .continuous)
                                    .fill(OnboardingSuccessBg)
                            )
                        }
                        .padding(.horizontal, 20)
                        .padding(.bottom, 8)
                    }
                    
                    // Bottom Actions
                    VStack(spacing: 10) {
                        Button(action: onSave) {
                            HStack(spacing: 8) {
                                Text(lang.localize("onboarding_save_and_enter"))
                                    .font(.system(size: 15.5, weight: .bold))
                                    .foregroundColor(.white)
                                
                                Image(systemName: "arrow.right")
                                    .font(.system(size: 13, weight: .bold))
                                    .foregroundColor(.white)
                            }
                            .frame(maxWidth: .infinity)
                            .frame(height: 48)
                            .background(OnboardingDarkGreen)
                            .clipShape(Capsule())
                            .shadow(color: OnboardingDarkGreen.opacity(0.35), radius: 8, x: 0, y: 4)
                        }
                        
                        OnboardingDotsView(activeStep: 4)
                    }
                    .padding(.horizontal, 20)
                    .padding(.top, 6)
                    .padding(.bottom, max(geo.safeAreaInsets.bottom, 12) + 6)
                }
                .frame(width: geo.size.width, height: geo.size.height + geo.safeAreaInsets.top + geo.safeAreaInsets.bottom)
            }
            .ignoresSafeArea()
        }
    }
}

private struct PrayerToggleRow: View {
    let title: String
    let icon: String
    @Binding var isOn: Bool
    
    var body: some View {
        HStack {
            Image(icon)
                .resizable()
                .scaledToFit()
                .frame(width: 30, height: 30)
            
            Text(title)
                .font(.system(size: 14.5, weight: .bold))
                .foregroundColor(OnboardingTitleGreen)
                .padding(.leading, 8)
            
            Spacer()
            
            Toggle("", isOn: $isOn)
                .labelsHidden()
                .tint(OnboardingSuccessGreen)
        }
        .padding(.vertical, 8)
    }
}

// -----------------------------------------------------------------------------
// 4-DOT PAGE INDICATOR
// -----------------------------------------------------------------------------
private struct OnboardingDotsView: View {
    let activeStep: Int
    
    var body: some View {
        HStack(spacing: 8) {
            ForEach(1...4, id: \.self) { i in
                let isActive = (i == activeStep)
                Circle()
                    .fill(isActive ? OnboardingDarkGreen : Color(hex: "#CBD5E1"))
                    .frame(width: isActive ? 8 : 7, height: isActive ? 8 : 7)
            }
        }
        .padding(.bottom, 2)
    }
}
