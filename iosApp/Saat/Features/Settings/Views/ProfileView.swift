//
//  ProfileView.swift
//  Saat
//
//  Created by Sufiandy Elmy on 09/10/2026.
//  Copyright © 2026 Elmee. All rights reserved.
//

import SwiftUI
internal import UIKit

struct ProfileView: View {
    var preferSystemNavigationTitle: Bool = false
    var verseState: TodayVerseState?

    @Environment(\.appContainer) private var container
    @ObservedObject var languageManager = AppLanguageManager.shared

    @AppStorage("chapterReaderFontScale") private var fontScale = 1.0
    @AppStorage("chapterReaderShowTranslation") private var showTranslation = true

    @AppStorage(ChapterReaderPreferences.translationIdKey) private var selectedTranslationId =
        ChapterReaderPreferences.defaultTranslationId
    @AppStorage(ChapterReaderPreferences.translationNameKey) private var selectedTranslationName = ""
    @AppStorage(PrayerCalculationMethod.storageKey)
    private var prayerMethodRaw = PrayerCalculationMethod.defaultMethod.rawValue
    @AppStorage("selected_adhan_sound") private var selectedAdhanSound = "default"
    @AppStorage("prayer_madhab_setting") private var selectedMadhabRaw = "shafi"

    @State private var showingTranslatorSheet = false
    @State private var showingAdhanVoiceSheet = false
    @State private var showingAppLanguageSheet = false
    @State private var showingMadhabSheet = false
    @State private var showingUpToDateSheet = false

    private var appVersion: String {
        Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String ?? "1.0.1"
    }

    var body: some View {
        ZStack {
            Color(hex: "#F9F7F2")
                .ignoresSafeArea()

            ScrollView {
                VStack(alignment: .leading, spacing: 16) {
                    // Clean Header bar matching Android
                    VStack(alignment: .leading, spacing: 4) {
                        Text(languageManager.localize("settings_main_title"))
                            .font(.system(size: 24, weight: .bold))
                            .foregroundColor(Color(hex: "#085E43"))

                        Text(languageManager.localize("settings_main_subtitle"))
                            .font(.system(size: 13, weight: .regular))
                            .foregroundColor(Color(hex: "#64748B"))

                        RoundedRectangle(cornerRadius: 2)
                            .fill(
                                LinearGradient(
                                    colors: [Color(hex: "#D4AF37"), Color(hex: "#D4AF37").opacity(0.15)],
                                    startPoint: .leading,
                                    endPoint: .trailing
                                )
                            )
                            .frame(width: 48, height: 2.5)
                            .padding(.top, 4)
                    }
                    .padding(.top, 16)
                    .padding(.horizontal, 4)

                    // 1. General Settings
                    sectionHeader(languageManager.localize("settings_section_general").uppercased())
                    SettingsCardView {
                        SettingsRowItem(
                            iconName: "ic_language_custom",
                            title: languageManager.localize("settings_item_language"),
                            subtitle: languageManager.currentLanguage.displayName,
                            onClick: { showingAppLanguageSheet = true },
                            showDivider: true
                        )

                        SettingsRowItem(
                            iconName: "ic_translator_custom",
                            title: languageManager.localize("reading_translator"),
                            subtitle: selectedTranslationName.isEmpty ? "Kemenag RI" : selectedTranslationName,
                            onClick: { showingTranslatorSheet = true },
                            showDivider: true
                        )

                        NavigationLink(destination: NotificationSettingsDetailView()) {
                            SettingsRowContent(
                                iconName: "ic_notification_custom",
                                title: languageManager.localize("settings_item_notification"),
                                subtitle: languageManager.localize("settings_item_advance_options"),
                                showDivider: false
                            )
                        }
                        .buttonStyle(.plain)
                    }

                    // 2. Prayer Calculation Settings
                    sectionHeader(languageManager.localize("settings_section_prayer_calc").uppercased())
                    SettingsCardView {
                        SettingsRowItem(
                            iconName: "ic_madhab_custom",
                            title: languageManager.localize("settings_item_madhab"),
                            subtitle: madhabDisplayName,
                            onClick: { showingMadhabSheet = true },
                            showDivider: true
                        )

                        NavigationLink(destination: PrayerCalculationSettingsView()) {
                            SettingsRowContent(
                                iconName: "ic_institution_custom",
                                title: languageManager.localize("settings_item_institution"),
                                subtitle: selectedPrayerMethod.displayName,
                                showDivider: true
                            )
                        }
                        .buttonStyle(.plain)

                        SettingsRowItem(
                            iconName: "ic_adhan_voice_custom",
                            title: languageManager.localize("settings_item_adhan_voice"),
                            subtitle: adhanVoiceDisplayName,
                            onClick: { showingAdhanVoiceSheet = true },
                            showDivider: false
                        )
                    }

                    // 3. About Saat
                    sectionHeader(languageManager.localize("settings_section_about_saat").uppercased())
                    SettingsCardView {
                        SettingsRowItem(
                            iconName: "ic_about_custom",
                            title: languageManager.localize("settings_item_about"),
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
                            title: languageManager.localize("settings_item_privacy"),
                            subtitle: languageManager.localize("privacy_policy_subtitle"),
                            onClick: {
                                if let url = URL(string: "https://elmee.my/saat/privacy") {
                                    UIApplication.shared.open(url)
                                }
                            },
                            showDivider: true
                        )

                        SettingsRowItem(
                            iconName: "ic_terms_custom",
                            title: languageManager.localize("settings_item_terms"),
                            subtitle: languageManager.localize("terms_conditions_subtitle"),
                            onClick: {
                                if let url = URL(string: "https://elmee.my/saat/terms") {
                                    UIApplication.shared.open(url)
                                }
                            },
                            showDivider: true
                        )

                        SettingsRowItem(
                            iconName: "ic_update_custom",
                            title: languageManager.localize("settings_item_check_update"),
                            subtitle: languageManager.localize("settings_item_up_to_date"),
                            onClick: { showingUpToDateSheet = true },
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
        .navigationBarHidden(true)
        .sheet(isPresented: $showingAppLanguageSheet) {
            LanguageSelectionSheet(selectedLanguage: $languageManager.currentLanguage)
                .presentationDetents([.height(380)])
                .presentationDragIndicator(.visible)
        }
        .sheet(isPresented: $showingTranslatorSheet) {
            if let container {
                TranslatorSelectionSheetView(
                    selectedTranslationId: $selectedTranslationId,
                    selectedTranslationName: $selectedTranslationName,
                    contentRepository: container.content
                )
                .presentationDetents([.medium, .large])
                .presentationDragIndicator(.visible)
            }
        }
        .sheet(isPresented: $showingAdhanVoiceSheet) {
            AdhanVoiceSelectionSheet()
        }
        .sheet(isPresented: $showingMadhabSheet) {
            MadhabSelectionSheet(selectedMadhabRaw: $selectedMadhabRaw)
                .presentationDetents([.height(420)])
                .presentationDragIndicator(.visible)
        }
        .sheet(isPresented: $showingUpToDateSheet) {
            UpToDateSheetView(appVersion: appVersion)
        }
    }

    private func sectionHeader(_ title: String) -> some View {
        Text(title)
            .font(.system(size: 13, weight: .bold))
            .foregroundColor(Color(hex: "#8E8E93"))
            .padding(.leading, 4)
            .padding(.top, 8)
            .padding(.bottom, 2)
    }

    private var selectedPrayerMethod: PrayerCalculationMethod {
        PrayerCalculationMethod(rawValue: prayerMethodRaw) ?? PrayerCalculationMethod.defaultMethod
    }

    private var madhabDisplayName: String {
        switch selectedMadhabRaw {
        case "hanafi": return "Hanafi"
        case "maliki": return "Maliki"
        case "hanbali": return "Hanbali"
        default: return "Syafi'i (Standar)"
        }
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
                .stroke(Color(hex: "#EEEEEE"), lineWidth: 1)
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
                        .foregroundColor(Color(hex: "#1C1C1E"))

                    if let subtitle, !subtitle.isEmpty {
                        Text(subtitle)
                            .font(.system(size: 13, weight: .regular))
                            .foregroundColor(Color(hex: "#8E8E93"))
                            .lineLimit(1)
                    }
                }

                Spacer()

                Image(systemName: "chevron.right")
                    .font(.system(size: 14, weight: .semibold))
                    .foregroundColor(Color(hex: "#C7C7CC"))
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
                    .fill(Color(hex: "#E6F3EE"))
                    .frame(width: 50, height: 50)

                Image(systemName: "moon.stars.fill")
                    .font(.system(size: 24))
                    .foregroundColor(Color(hex: "#085E43"))
            }

            Text("Sāat: Waktu Shalat & Al-Qur'an")
                .font(.system(size: 14, weight: .bold))
                .foregroundColor(Color(hex: "#085E43"))

            Text("Versi \(appVersion)")
                .font(.system(size: 12, weight: .medium))
                .foregroundColor(Color(hex: "#8E8E93"))

            Text("Dibuat dengan ikhlas untuk umat")
                .font(.system(size: 11, weight: .regular))
                .foregroundColor(Color(hex: "#A0A0A5"))
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 20)
    }
}

// MARK: - Notification Settings Detail View (100% Android Match)
struct NotificationSettingsDetailView: View {
    @ObservedObject var languageManager = AppLanguageManager.shared
    @Environment(\.dismiss) private var dismiss

    // Prayer 5 waktu
    @AppStorage(PrayerNotificationPreferences.fajrKey) private var fajrEnabled = true
    @AppStorage(PrayerNotificationPreferences.dhuhrKey) private var dhuhrEnabled = true
    @AppStorage(PrayerNotificationPreferences.asrKey) private var asrEnabled = true
    @AppStorage(PrayerNotificationPreferences.maghribKey) private var maghribEnabled = true
    @AppStorage(PrayerNotificationPreferences.ishaKey) private var ishaEnabled = true

    // Live countdown / activity
    @AppStorage("live_prayer_countdown_enabled") private var liveCountdownEnabled = true

    // Reading reminders
    @AppStorage(DailyVerseNotificationPreferences.enabledKey) private var dailyVerseEnabled = true
    @AppStorage("surah_reminder_yasin_enabled") private var yasinReminderEnabled = true
    @AppStorage("quran_last_read_reminder_enabled") private var quranReminderEnabled = true
    @AppStorage("important_days_reminder_enabled") private var importantDaysReminderEnabled = true

    // Sunnah prayers
    @AppStorage(PrayerNotificationPreferences.tahajudKey) private var tahajudEnabled = true
    @AppStorage("dhuha_reminder_enabled") private var dhuhaEnabled = false

    var body: some View {
        ZStack {
            Color(hex: "#F9F7F2")
                .ignoresSafeArea()

            ScrollView {
                VStack(alignment: .leading, spacing: 16) {
                    // Section 1: Notification Adhan 5 Waktu
                    sectionHeader(languageManager.localize("notif_section_adhan").uppercased())
                    SettingsCardView {
                        ToggleRow(
                            iconName: fajrEnabled ? "ic_adhan_on_custom" : "ic_adhan_off_custom",
                            title: languageManager.localize("prayer_fajr"),
                            subtitle: fajrEnabled ? languageManager.localize("state_on") : languageManager.localize("state_off"),
                            isOn: $fajrEnabled,
                            showDivider: true
                        )
                        ToggleRow(
                            iconName: dhuhrEnabled ? "ic_adhan_on_custom" : "ic_adhan_off_custom",
                            title: languageManager.localize("prayer_dhuhr"),
                            subtitle: dhuhrEnabled ? languageManager.localize("state_on") : languageManager.localize("state_off"),
                            isOn: $dhuhrEnabled,
                            showDivider: true
                        )
                        ToggleRow(
                            iconName: asrEnabled ? "ic_adhan_on_custom" : "ic_adhan_off_custom",
                            title: languageManager.localize("prayer_asr"),
                            subtitle: asrEnabled ? languageManager.localize("state_on") : languageManager.localize("state_off"),
                            isOn: $asrEnabled,
                            showDivider: true
                        )
                        ToggleRow(
                            iconName: maghribEnabled ? "ic_adhan_on_custom" : "ic_adhan_off_custom",
                            title: languageManager.localize("prayer_maghrib"),
                            subtitle: maghribEnabled ? languageManager.localize("state_on") : languageManager.localize("state_off"),
                            isOn: $maghribEnabled,
                            showDivider: true
                        )
                        ToggleRow(
                            iconName: ishaEnabled ? "ic_adhan_on_custom" : "ic_adhan_off_custom",
                            title: languageManager.localize("prayer_isha"),
                            subtitle: ishaEnabled ? languageManager.localize("state_on") : languageManager.localize("state_off"),
                            isOn: $ishaEnabled,
                            showDivider: false
                        )
                    }

                    Text(languageManager.localize("notif_adhan_section_info"))
                        .font(.system(size: 12))
                        .foregroundColor(Color(hex: "#8E8E93"))
                        .padding(.horizontal, 4)

                    // Section 2: Live Activity / Countdown
                    sectionHeader(languageManager.localize("channel_live_prayer_countdown").uppercased())
                    SettingsCardView {
                        ToggleRow(
                            iconName: "ic_notification_custom",
                            title: languageManager.localize("setting_live_prayer_countdown"),
                            subtitle: liveCountdownEnabled ? languageManager.localize("state_on") : languageManager.localize("state_off"),
                            isOn: $liveCountdownEnabled,
                            showDivider: false
                        )
                    }

                    Text(languageManager.localize("setting_live_prayer_countdown_desc"))
                        .font(.system(size: 12))
                        .foregroundColor(Color(hex: "#8E8E93"))
                        .padding(.horizontal, 4)

                    // Section 3: Notification Reading
                    sectionHeader(languageManager.localize("notif_section_reading").uppercased())
                    SettingsCardView {
                        ToggleRow(
                            iconName: "ic_daily_verse_custom",
                            title: languageManager.localize("reading_daily_verse"),
                            subtitle: dailyVerseEnabled ? languageManager.localize("state_on") : languageManager.localize("state_off"),
                            isOn: $dailyVerseEnabled,
                            showDivider: true
                        )
                        ToggleRow(
                            iconName: "ic_remainders_custom",
                            title: languageManager.localize("notif_surah_reminders"),
                            subtitle: yasinReminderEnabled ? languageManager.localize("state_on") : languageManager.localize("state_off"),
                            isOn: $yasinReminderEnabled,
                            showDivider: true
                        )
                        ToggleRow(
                            iconName: "ic_remainders_custom",
                            title: languageManager.localize("quran_reminder_setting_title"),
                            subtitle: quranReminderEnabled ? languageManager.localize("state_on") : languageManager.localize("state_off"),
                            isOn: $quranReminderEnabled,
                            showDivider: true
                        )
                        ToggleRow(
                            iconName: "ic_remainders_custom",
                            title: languageManager.localize("notif_fasting_important_days"),
                            subtitle: importantDaysReminderEnabled ? languageManager.localize("state_on") : languageManager.localize("state_off"),
                            isOn: $importantDaysReminderEnabled,
                            showDivider: false
                        )
                    }

                    // Section 4: Other Sunnah Prayers
                    sectionHeader(languageManager.localize("notif_section_other_prayer").uppercased())
                    SettingsCardView {
                        ToggleRow(
                            iconName: "ic_remainders_custom",
                            title: languageManager.localize("notif_tahajud_remainder"),
                            subtitle: tahajudEnabled ? "03:30 • \(languageManager.localize("state_on"))" : languageManager.localize("state_off"),
                            isOn: $tahajudEnabled,
                            showDivider: true
                        )
                        ToggleRow(
                            iconName: "ic_remainders_custom",
                            title: languageManager.localize("notif_dhuha_remainder"),
                            subtitle: dhuhaEnabled ? "08:30 • \(languageManager.localize("state_on"))" : languageManager.localize("state_off"),
                            isOn: $dhuhaEnabled,
                            showDivider: false
                        )
                    }
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 12)
                .padding(.bottom, 60)
            }
        }
        .navigationTitle(languageManager.localize("notif_adhan_title"))
        .navigationBarTitleDisplayMode(.inline)
    }

    private func sectionHeader(_ title: String) -> some View {
        Text(title)
            .font(.system(size: 13, weight: .bold))
            .foregroundColor(Color(hex: "#8E8E93"))
            .padding(.leading, 4)
            .padding(.top, 8)
            .padding(.bottom, 2)
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
                        .foregroundColor(Color(hex: "#1C1C1E"))

                    Text(subtitle)
                        .font(.system(size: 12, weight: .regular))
                        .foregroundColor(Color(hex: "#8E8E93"))
                }

                Spacer()

                Toggle("", isOn: $isOn)
                    .labelsHidden()
                    .tint(Color(hex: "#085E43"))
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

// MARK: - Language Selection Sheet (Matching Android with flags & card style)
struct LanguageSelectionSheet: View {
    @Binding var selectedLanguage: AppLanguage
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationStack {
            VStack(spacing: 12) {
                ForEach(AppLanguage.allCases) { lang in
                    let isSelected = selectedLanguage == lang
                    Button {
                        selectedLanguage = lang
                        dismiss()
                    } label: {
                        HStack(spacing: 14) {
                            Circle()
                                .fill(Color(hex: "#F8F4F7"))
                                .frame(width: 34, height: 34)
                                .overlay(
                                    Image(flagIconName(for: lang))
                                        .resizable()
                                        .scaledToFill()
                                        .frame(width: 34, height: 34)
                                        .clipShape(Circle())
                                )

                            Text(lang.displayName)
                                .font(.system(size: 16, weight: isSelected ? .bold : .medium))
                                .foregroundColor(isSelected ? Color(hex: "#085E43") : Color(hex: "#1C1C1E"))

                            Spacer()

                            if isSelected {
                                ZStack {
                                    Circle()
                                        .fill(Color(hex: "#085E43"))
                                        .frame(width: 22, height: 22)
                                    Image(systemName: "checkmark")
                                        .font(.system(size: 11, weight: .bold))
                                        .foregroundColor(.white)
                                }
                            }
                        }
                        .padding(.horizontal, 16)
                        .padding(.vertical, 14)
                        .background(isSelected ? Color(hex: "#085E43").opacity(0.08) : Color.white)
                        .cornerRadius(14)
                        .overlay(
                            RoundedRectangle(cornerRadius: 14)
                                .stroke(isSelected ? Color(hex: "#085E43") : Color(hex: "#EAE4D6"), lineWidth: isSelected ? 1.5 : 1)
                        )
                    }
                    .buttonStyle(.plain)
                }

                Spacer()
            }
            .padding(.horizontal, 20)
            .padding(.top, 20)
            .background(Color(hex: "#F9F7F2").ignoresSafeArea())
            .navigationTitle(AppLanguageManager.shared.localize("language_settings_title"))
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button(AppLanguageManager.shared.localize("cancel")) {
                        dismiss()
                    }
                    .foregroundColor(Color(hex: "#085E43"))
                }
            }
        }
    }

    private func flagIconName(for lang: AppLanguage) -> String {
        switch lang {
        case .indonesian: return "ic_flag_id"
        case .english: return "ic_flag_en"
        case .malay: return "ic_flag_ms"
        }
    }
}

// MARK: - Madhab Selection Sheet (Matching Android)
struct MadhabSelectionSheet: View {
    @Binding var selectedMadhabRaw: String
    @Environment(\.dismiss) private var dismiss

    private let madhabs = [
        ("shafi", "Shafi'i (Standar Indonesia & Malaysia)"),
        ("hanafi", "Hanafi"),
        ("maliki", "Maliki"),
        ("hanbali", "Hanbali")
    ]

    var body: some View {
        NavigationStack {
            VStack(spacing: 12) {
                ForEach(madhabs, id: \.0) { item in
                    let isSelected = selectedMadhabRaw == item.0
                    Button {
                        selectedMadhabRaw = item.0
                        dismiss()
                    } label: {
                        HStack(spacing: 14) {
                            Image("ic_madhab_custom")
                                .resizable()
                                .scaledToFit()
                                .frame(width: 32, height: 32)

                            Text(item.1)
                                .font(.system(size: 15, weight: isSelected ? .bold : .medium))
                                .foregroundColor(isSelected ? Color(hex: "#085E43") : Color(hex: "#1C1C1E"))

                            Spacer()

                            if isSelected {
                                Image(systemName: "checkmark.circle.fill")
                                    .font(.system(size: 20))
                                    .foregroundColor(Color(hex: "#085E43"))
                            }
                        }
                        .padding(.horizontal, 16)
                        .padding(.vertical, 14)
                        .background(isSelected ? Color(hex: "#085E43").opacity(0.08) : Color.white)
                        .cornerRadius(14)
                        .overlay(
                            RoundedRectangle(cornerRadius: 14)
                                .stroke(isSelected ? Color(hex: "#085E43") : Color(hex: "#EAE4D6"), lineWidth: isSelected ? 1.5 : 1)
                        )
                    }
                    .buttonStyle(.plain)
                }

                Spacer()
            }
            .padding(.horizontal, 20)
            .padding(.top, 20)
            .background(Color(hex: "#F9F7F2").ignoresSafeArea())
            .navigationTitle(AppLanguageManager.shared.localize("madhab_settings_title"))
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button(AppLanguageManager.shared.localize("cancel")) {
                        dismiss()
                    }
                    .foregroundColor(Color(hex: "#085E43"))
                }
            }
        }
    }
}

// MARK: - Up To Date Sheet
struct UpToDateSheetView: View {
    let appVersion: String
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        VStack(spacing: 16) {
            ZStack {
                Circle()
                    .fill(Color(hex: "#E6F3EE"))
                    .frame(width: 64, height: 64)

                Image("ic_update_custom")
                    .resizable()
                    .scaledToFit()
                    .frame(width: 36, height: 36)
            }
            .padding(.top, 24)

            Text(AppLanguageManager.shared.localize("up_to_date_sheet_title"))
                .font(.system(size: 20, weight: .bold))
                .foregroundColor(Color(hex: "#1C1C1E"))

            Text(String(format: AppLanguageManager.shared.localize("up_to_date_sheet_desc"), appVersion))
                .font(.system(size: 14))
                .foregroundColor(Color(hex: "#8E8E93"))
                .multilineTextAlignment(.center)
                .padding(.horizontal, 24)

            Button {
                dismiss()
            } label: {
                Text(AppLanguageManager.shared.localize("got_it"))
                    .font(.system(size: 16, weight: .semibold))
                    .foregroundColor(.white)
                    .frame(maxWidth: .infinity)
                    .frame(height: 48)
                    .background(Color(hex: "#085E43"))
                    .cornerRadius(12)
            }
            .padding(.horizontal, 24)
            .padding(.top, 8)
            .padding(.bottom, 24)
        }
        .presentationDetents([.height(280)])
    }
}
