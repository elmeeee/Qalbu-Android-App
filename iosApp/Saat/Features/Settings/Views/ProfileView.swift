//
//  ProfileView.swift
//  Saat
//
//  Created by Elmee on 25/04/2026.
//  Copyright © 2026 Elmee. All rights reserved.
//

import SwiftUI
internal import UIKit

struct ProfileView: View {
    var preferSystemNavigationTitle: Bool = false
    var verseState: TodayVerseState?

    @Environment(\.appContainer) private var container
    @AppStorage("hasCompletedOnboarding") private var hasCompletedOnboarding = false
    @AppStorage("chapterReaderFontScale") private var fontScale = 1.0
    @AppStorage("chapterReaderShowTranslation") private var showTranslation = true
    @AppStorage(PrayerNotificationPreferences.adzanKey) private var adzanEnabled = true
    @AppStorage(PrayerNotificationPreferences.imsakKey) private var imsakEnabled = true
    @AppStorage(PrayerNotificationPreferences.midnightKey) private var midnightEnabled = true
    @AppStorage(PrayerNotificationPreferences.firstThirdKey) private var firstThirdEnabled = true
    @AppStorage(PrayerNotificationPreferences.tahajudKey) private var tahajudEnabled = true
    @AppStorage(DailyVerseNotificationPreferences.enabledKey) private var dailyVerseEnabled = true
    @AppStorage(DailyVerseNotificationPreferences.hourKey) private var dailyVerseHour =
        DailyVerseNotificationPreferences.defaultHour
    @AppStorage(DailyVerseNotificationPreferences.minuteKey) private var dailyVerseMinute =
        DailyVerseNotificationPreferences.defaultMinute

    @AppStorage(ChapterReaderPreferences.translationIdKey) private var selectedTranslationId =
        ChapterReaderPreferences.defaultTranslationId
    @AppStorage(ChapterReaderPreferences.translationNameKey) private var selectedTranslationName =
        ""
    @AppStorage(PrayerCalculationMethod.storageKey)
    private var prayerMethodRaw = PrayerCalculationMethod.defaultMethod.rawValue
    @AppStorage("selected_adhan_sound") private var selectedAdhanSound = "default"

    @State private var showingFontScaleSheet = false
    @State private var showingTranslatorSheet = false
    @State private var showingAdhanVoiceSheet = false
    @State private var showingDailyVerseTimeSheet = false
    @State private var showingAppLanguageSheet = false
    @State private var showingMadhabSheet = false
    @State private var showingCheckUpdateAlert = false
    @State private var selectedMadhab: String = "Syafi'i, Maliki, Hanbali"

    @ObservedObject var languageManager = AppLanguageManager.shared

    private var appVersion: String {
        Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String ?? "1.0.1"
    }

    var body: some View {
        ZStack {
            SaatTokens.Colors.homeBg
                .ignoresSafeArea()

            ScrollView {
                VStack(alignment: .leading, spacing: 16) {
                    // Header Title & Subtitle
                    VStack(alignment: .leading, spacing: 4) {
                        Text("Pengaturan & Lainnya")
                            .font(.system(size: 24, weight: .bold))
                            .foregroundColor(SaatTokens.Colors.deepEmerald)

                        Text("Kelola preferensi dan informasi aplikasi")
                            .font(.system(size: 13, weight: .regular))
                            .foregroundColor(SaatTokens.Colors.slate500)

                        RoundedRectangle(cornerRadius: 2)
                            .fill(
                                LinearGradient(
                                    colors: [SaatTokens.Colors.gold, SaatTokens.Colors.gold.opacity(0.15)],
                                    startPoint: .leading,
                                    endPoint: .trailing
                                )
                            )
                            .frame(width: 48, height: 2.5)
                            .padding(.top, 4)
                    }
                    .padding(.top, 16)
                    .padding(.horizontal, 4)

                    // 1. Pengaturan Umum
                    sectionHeader("PENGATURAN UMUM")
                    SettingsCardView {
                        SettingsRowItem(
                            iconName: "ic_language_custom",
                            title: "Bahasa Aplikasi",
                            subtitle: languageManager.currentLanguage.displayName,
                            onClick: { showingAppLanguageSheet = true },
                            showDivider: true
                        )

                        SettingsRowItem(
                            iconName: "ic_translator_custom",
                            title: "Penerjemah Al-Qur'an",
                            subtitle: selectedTranslationName.isEmpty ? "Kemenag RI" : selectedTranslationName,
                            onClick: { showingTranslatorSheet = true },
                            showDivider: true
                        )

                        NavigationLink(destination: NotificationSettingsDetailView()) {
                            SettingsRowContent(
                                iconName: "ic_notification_custom",
                                title: "Notifikasi & Adzan",
                                subtitle: "Opsi lanjutan & pengingat",
                                showDivider: false
                            )
                        }
                        .buttonStyle(.plain)
                    }

                    // 2. Metode & Perhitungan Shalat
                    sectionHeader("METODE & PERHITUNGAN SHALAT")
                    SettingsCardView {
                        SettingsRowItem(
                            iconName: "ic_madhab_custom",
                            title: "Mazhab Shalat",
                            subtitle: selectedMadhab,
                            onClick: { showingMadhabSheet = true },
                            showDivider: true
                        )

                        NavigationLink(destination: PrayerCalculationSettingsView()) {
                            SettingsRowContent(
                                iconName: "ic_institution_custom",
                                title: "Metode Perhitungan",
                                subtitle: selectedPrayerMethod.displayName,
                                showDivider: true
                            )
                        }
                        .buttonStyle(.plain)

                        SettingsRowItem(
                            iconName: "ic_adhan_voice_custom",
                            title: "Suara Adzan",
                            subtitle: adhanVoiceDisplayName,
                            onClick: { showingAdhanVoiceSheet = true },
                            showDivider: false
                        )
                    }

                    // 3. Tentang Saat
                    sectionHeader("TENTANG SĀAT")
                    SettingsCardView {
                        SettingsRowItem(
                            iconName: "ic_about_custom",
                            title: "Tentang Aplikasi",
                            subtitle: "Sāat: Waktu Shalat & Al-Qur'an",
                            onClick: {
                                if let url = URL(string: "https://elmee.my/saat") {
                                    UIApplication.shared.open(url)
                                }
                            },
                            showDivider: true
                        )

                        SettingsRowItem(
                            iconName: "ic_privacy_custom",
                            title: "Kebijakan Privasi",
                            subtitle: "Perlindungan data pengguna",
                            onClick: {
                                if let url = URL(string: "https://elmee.my/saat/privacy") {
                                    UIApplication.shared.open(url)
                                }
                            },
                            showDivider: true
                        )

                        SettingsRowItem(
                            iconName: "ic_terms_custom",
                            title: "Syarat & Ketentuan",
                            subtitle: "Ketentuan penggunaan aplikasi",
                            onClick: {
                                if let url = URL(string: "https://elmee.my/saat/terms") {
                                    UIApplication.shared.open(url)
                                }
                            },
                            showDivider: true
                        )

                        SettingsRowItem(
                            iconName: "ic_update_custom",
                            title: "Periksa Pembaruan",
                            subtitle: "Aplikasi sudah versi terbaru",
                            onClick: { showingCheckUpdateAlert = true },
                            showDivider: false
                        )
                    }

                    // App Footer Card
                    AppFooterCardView(appVersion: appVersion)
                        .padding(.top, 8)
                }
                .padding(.horizontal, 16)
                .padding(.bottom, 120)
            }
        }
        .sheet(isPresented: $showingAppLanguageSheet) {
            AppLanguageSelectionSheet(selectedLanguage: $languageManager.currentLanguage)
        }
        .sheet(isPresented: $showingTranslatorSheet) {
            if let container {
                TranslatorSelectionSheetView(
                    selectedTranslationId: $selectedTranslationId,
                    selectedTranslationName: $selectedTranslationName,
                    contentRepository: container.content
                )
            }
        }
        .sheet(isPresented: $showingAdhanVoiceSheet) {
            AdhanVoiceSelectionSheet()
        }
        .confirmationDialog("Pilih Mazhab", isPresented: $showingMadhabSheet, titleVisibility: .visible) {
            Button("Syafi'i, Maliki, Hanbali (Standar)") {
                selectedMadhab = "Syafi'i, Maliki, Hanbali"
            }
            Button("Hanafi (Waktu Ashar lebih lambat)") {
                selectedMadhab = "Hanafi"
            }
            Button("Batal", role: .cancel) {}
        }
        .alert("Aplikasi Sudah Versi Terbaru", isPresented: $showingCheckUpdateAlert) {
            Button("Tutup", role: .cancel) {}
        } message: {
            Text("Kamu sudah menggunakan versi terbaru Sāat (\(appVersion)).")
        }
    }

    private func sectionHeader(_ title: String) -> some View {
        Text(title)
            .font(.system(size: 13, weight: .bold))
            .foregroundColor(Color(hex: 0xFF8E_8E93))
            .padding(.leading, 4)
            .padding(.top, 8)
            .padding(.bottom, 2)
    }

    private var selectedPrayerMethod: PrayerCalculationMethod {
        PrayerCalculationMethod(rawValue: prayerMethodRaw) ?? PrayerCalculationMethod.defaultMethod
    }

    private var adhanVoiceDisplayName: String {
        switch selectedAdhanSound {
        case "default": return "Suara Default Sistem"
        case "adhan_ust_daeng_syawal_indonesia": return "Ust. Daeng Syawal (ID)"
        case "adhan_omar_hisham_al_arabi": return "Omar Hisham Al Arabi"
        case "adhan_sheikh_abdul_karim_malaysia": return "Sheikh Abdul Karim (MY)"
        case "adhan_fajr_mishary_alafasy": return "Mishary Alafasy (Subuh)"
        default: return "Suara Default Sistem"
        }
    }
}

// MARK: - Settings Card Container
private struct SettingsCardView<Content: View>: View {
    let content: Content

    init(@ViewBuilder content: () -> Content) {
        self.content = content()
    }

    var body: some View {
        VStack(spacing: 0) {
            content
        }
        .background(Color.white)
        .cornerRadius(16)
        .overlay(
            RoundedRectangle(cornerRadius: 16, style: .continuous)
                .stroke(Color(hex: 0xFFEE_EEEE), lineWidth: 1)
        )
        .shadow(color: Color.black.opacity(0.02), radius: 4, x: 0, y: 1)
    }
}

// MARK: - Settings Row Item
private struct SettingsRowItem: View {
    let iconName: String
    let title: String
    let subtitle: String?
    let onClick: () -> Void
    let showDivider: Bool

    var body: some View {
        Button(action: onClick) {
            SettingsRowContent(
                iconName: iconName,
                title: title,
                subtitle: subtitle,
                showDivider: showDivider
            )
        }
        .buttonStyle(.plain)
    }
}

private struct SettingsRowContent: View {
    let iconName: String
    let title: String
    let subtitle: String?
    let showDivider: Bool

    var body: some View {
        VStack(spacing: 0) {
            HStack(spacing: 14) {
                Image(iconName)
                    .resizable()
                    .scaledToFit()
                    .frame(width: 24, height: 24)

                VStack(alignment: .leading, spacing: 2) {
                    Text(title)
                        .font(.system(size: 16, weight: .medium))
                        .foregroundColor(Color(hex: 0xFF1C_1C1E))

                    if let subtitle, !subtitle.isEmpty {
                        Text(subtitle)
                            .font(.system(size: 13, weight: .regular))
                            .foregroundColor(Color(hex: 0xFF8E_8E93))
                            .lineLimit(1)
                    }
                }

                Spacer()

                Image(systemName: "chevron.right")
                    .font(.system(size: 14, weight: .semibold))
                    .foregroundColor(Color(hex: 0xFFC7_C7CC))
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 14)

            if showDivider {
                Divider()
                    .padding(.leading, 54)
            }
        }
    }
}

// MARK: - App Footer Card
private struct AppFooterCardView: View {
    let appVersion: String

    var body: some View {
        VStack(spacing: 8) {
            ZStack {
                Circle()
                    .fill(SaatTokens.Colors.sageTint)
                    .frame(width: 50, height: 50)

                Image(systemName: "moon.stars.fill")
                    .font(.system(size: 24))
                    .foregroundColor(SaatTokens.Colors.homeDarkGreen)
            }

            Text("Sāat: Waktu Shalat & Al-Qur'an")
                .font(.system(size: 14, weight: .bold))
                .foregroundColor(SaatTokens.Colors.homeDarkGreen)

            Text("Versi \(appVersion)")
                .font(.system(size: 12, weight: .medium))
                .foregroundColor(SaatTokens.Colors.slate500)

            Text("Dibuat dengan ikhlas untuk umat")
                .font(.system(size: 11, weight: .regular))
                .foregroundColor(SaatTokens.Colors.slate400)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 20)
    }
}

// MARK: - Notification Settings Detail View
struct NotificationSettingsDetailView: View {
    @AppStorage(PrayerNotificationPreferences.adzanKey) private var adzanEnabled = true
    @AppStorage(PrayerNotificationPreferences.imsakKey) private var imsakEnabled = true
    @AppStorage(PrayerNotificationPreferences.midnightKey) private var midnightEnabled = true
    @AppStorage(PrayerNotificationPreferences.firstThirdKey) private var firstThirdEnabled = true
    @AppStorage(PrayerNotificationPreferences.tahajudKey) private var tahajudEnabled = true
    @AppStorage(DailyVerseNotificationPreferences.enabledKey) private var dailyVerseEnabled = true

    var body: some View {
        ZStack {
            SaatTokens.Colors.homeBg
                .ignoresSafeArea()

            ScrollView {
                VStack(alignment: .leading, spacing: 16) {
                    Text("Pengaturan Notifikasi")
                        .font(.system(size: 22, weight: .bold))
                        .foregroundColor(SaatTokens.Colors.deepEmerald)
                        .padding(.top, 16)

                    SettingsCardView {
                        ToggleRow(iconName: "ic_adhan_on_custom", title: "Adzan & Waktu Shalat", subtitle: "Pengingat 5 waktu shalat", isOn: $adzanEnabled, showDivider: true)
                        ToggleRow(iconName: "ic_remainders_custom", title: "Waktu Imsak", subtitle: "10 menit sebelum Subuh", isOn: $imsakEnabled, showDivider: true)
                        ToggleRow(iconName: "ic_remainders_custom", title: "Tahajud & Qiyamul Lail", subtitle: "Sepertiga malam terakhir", isOn: $tahajudEnabled, showDivider: true)
                        ToggleRow(iconName: "ic_daily_verse_custom", title: "Ayat Harian", subtitle: "Kutipan inspirasi setiap pagi", isOn: $dailyVerseEnabled, showDivider: false)
                    }
                }
                .padding(.horizontal, 16)
                .padding(.bottom, 120)
            }
        }
        .navigationTitle("Notifikasi & Adzan")
        .navigationBarTitleDisplayMode(.inline)
    }
}

private struct ToggleRow: View {
    let iconName: String
    let title: String
    let subtitle: String
    @Binding var isOn: Bool
    let showDivider: Bool

    var body: some View {
        VStack(spacing: 0) {
            HStack(spacing: 14) {
                Image(iconName)
                    .resizable()
                    .scaledToFit()
                    .frame(width: 24, height: 24)

                VStack(alignment: .leading, spacing: 2) {
                    Text(title)
                        .font(.system(size: 15, weight: .semibold))
                        .foregroundColor(Color(hex: 0xFF1C_1C1E))

                    Text(subtitle)
                        .font(.system(size: 12, weight: .regular))
                        .foregroundColor(Color(hex: 0xFF8E_8E93))
                }

                Spacer()

                Toggle("", isOn: $isOn)
                    .labelsHidden()
                    .tint(SaatTokens.Colors.deepEmerald)
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 12)

            if showDivider {
                Divider()
                    .padding(.leading, 54)
            }
        }
    }
}

// MARK: - App Language Selection Sheet
struct AppLanguageSelectionSheet: View {
    @Binding var selectedLanguage: AppLanguage
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationStack {
            List {
                ForEach(AppLanguage.allCases) { lang in
                    Button {
                        selectedLanguage = lang
                        dismiss()
                    } label: {
                        HStack {
                            Text(lang.displayName)
                                .foregroundColor(.primary)
                            Spacer()
                            if selectedLanguage == lang {
                                Image(systemName: "checkmark")
                                    .foregroundColor(SaatTokens.Colors.deepEmerald)
                                    .fontWeight(.bold)
                            }
                        }
                    }
                }
            }
            .navigationTitle("Bahasa Aplikasi")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Tutup") {
                        dismiss()
                    }
                }
            }
        }
    }
}
