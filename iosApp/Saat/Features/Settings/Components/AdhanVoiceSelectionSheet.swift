//
//  AdhanVoiceSelectionSheet.swift
//  Saat
//
//  Created by Elmee on 25/06/2026.
//  Copyright © 2026 Elmee. All rights reserved.
//

import AVFoundation
import SwiftUI

struct AdhanVoiceModel: Identifiable, Sendable {
    let id: String
    let displayName: String
    let fileName: String
    let avatarName: String
}

struct AdhanVoiceSelectionSheet: View {
    @Environment(\.dismiss) private var dismiss
    @ObservedObject private var languageManager = AppLanguageManager.shared

    @State private var selectedSoundName =
        UserDefaults.standard.string(forKey: "selected_adhan_sound") ?? "adhan_islam_sobhi"
    @State private var selectedFajrSoundName =
        UserDefaults.standard.string(forKey: "selected_fajr_adhan_sound") ?? "adhan_fajr_mishary_alafasy"
    
    @State private var selectedTab: Int = 0
    @State private var audioPlayer: AVAudioPlayer? = nil
    @State private var playingOptionId: String? = nil

    private let generalVoices: [AdhanVoiceModel] = [
        AdhanVoiceModel(
            id: "islam_sobhi",
            displayName: "Islam Sobhi",
            fileName: "adhan_islam_sobhi",
            avatarName: "adhan_islam_sobhi"
        ),
        AdhanVoiceModel(
            id: "omar_hisham",
            displayName: "Omar Hisham Al Arabi",
            fileName: "adhan_omar_hisham_al_arabi",
            avatarName: "adhan_omar_hisham"
        ),
        AdhanVoiceModel(
            id: "hamza_al_majali",
            displayName: "Hamza Al Majali",
            fileName: "adhan_hamza_al_majale",
            avatarName: "adhan_hamza_majali"
        ),
        AdhanVoiceModel(
            id: "sheikh_abdul_karim",
            displayName: "Sheikh Abdul Karim Umar Al-Makki",
            fileName: "adhan_sheikh_abdul_karim_malaysia",
            avatarName: "adhan_sheikh_abdul_karim"
        ),
        AdhanVoiceModel(
            id: "ust_bilal_attaki",
            displayName: "Ust. Bilal Attaki",
            fileName: "adhan_normal_ust_bilal_attaki",
            avatarName: "adhan_bilal_attaki"
        ),
        AdhanVoiceModel(
            id: "ust_daeng_syawal",
            displayName: "Ust. Daeng Syawal Mubarak",
            fileName: "adhan_ust_daeng_syawal_indonesia",
            avatarName: "adhan_daeng_syawal"
        ),
        AdhanVoiceModel(
            id: "habib_syech",
            displayName: "Habib Syech Bin Abdul Qadir Assegaf",
            fileName: "adhan_habib_syech",
            avatarName: "adhan_habib_syech"
        )
    ]

    private let fajrVoices: [AdhanVoiceModel] = [
        AdhanVoiceModel(
            id: "fajr_mishary",
            displayName: "Mishary Rashid Alafasy",
            fileName: "adhan_fajr_mishary_alafasy",
            avatarName: "reciter_alafasy"
        ),
        AdhanVoiceModel(
            id: "fajr_bilal_attaki",
            displayName: "Ust. Bilal Attaki",
            fileName: "adhan_fajr_ust_bilal_attaki",
            avatarName: "adhan_bilal_attaki"
        ),
        AdhanVoiceModel(
            id: "fajr_muhammad_rohani",
            displayName: "Muhammad Rohani",
            fileName: "adhan_fajr_muhammad_rohani",
            avatarName: "adhan_muhammad_rohani"
        )
    ]

    var body: some View {
        VStack(spacing: 0) {
            // Header
            HStack(spacing: 12) {
                Image("ic_adhan_voice_custom")
                    .resizable()
                    .scaledToFit()
                    .frame(width: 28, height: 28)

                Text(languageManager.localize("adhan_voice"))
                    .font(.system(size: 20, weight: .bold))
                    .foregroundColor(SaatTokens.Colors.deepEmerald)

                Spacer()

                Button(action: { dismiss() }) {
                    Text(languageManager.localize("done"))
                        .font(.system(size: 15, weight: .bold))
                        .foregroundColor(SaatTokens.Colors.deepEmerald)
                }
            }
            .padding(.horizontal, 20)
            .padding(.top, 20)
            .padding(.bottom, 12)

            // Segmented Tab
            HStack(spacing: 4) {
                Button(action: { selectedTab = 0 }) {
                    Text("Adzan Umum")
                        .font(.system(size: 14, weight: selectedTab == 0 ? .bold : .medium))
                        .foregroundColor(selectedTab == 0 ? .white : SaatTokens.Colors.slate700)
                        .frame(maxWidth: .infinity)
                        .frame(height: 36)
                        .background(selectedTab == 0 ? SaatTokens.Colors.deepEmerald : Color.clear)
                        .cornerRadius(8)
                }
                .buttonStyle(.plain)

                Button(action: { selectedTab = 1 }) {
                    Text("Adzan Subuh")
                        .font(.system(size: 14, weight: selectedTab == 1 ? .bold : .medium))
                        .foregroundColor(selectedTab == 1 ? .white : SaatTokens.Colors.slate700)
                        .frame(maxWidth: .infinity)
                        .frame(height: 36)
                        .background(selectedTab == 1 ? SaatTokens.Colors.deepEmerald : Color.clear)
                        .cornerRadius(8)
                }
                .buttonStyle(.plain)
            }
            .padding(4)
            .background(Color(hex: 0xFFF2_F2F7))
            .cornerRadius(10)
            .padding(.horizontal, 20)
            .padding(.bottom, 12)

            Divider()

            ScrollView {
                VStack(spacing: 10) {
                    let list = selectedTab == 0 ? generalVoices : fajrVoices
                    let currentSelected = selectedTab == 0 ? selectedSoundName : selectedFajrSoundName

                    ForEach(list) { voice in
                        let isSelected = currentSelected == voice.fileName
                        let isPlaying = playingOptionId == voice.id

                        Button(action: {
                            if selectedTab == 0 {
                                selectedSoundName = voice.fileName
                                UserDefaults.standard.set(voice.fileName, forKey: "selected_adhan_sound")
                            } else {
                                selectedFajrSoundName = voice.fileName
                                UserDefaults.standard.set(voice.fileName, forKey: "selected_fajr_adhan_sound")
                            }
                            NotificationCenter.default.post(
                                name: PrayerNotificationPreferences.didChangeNotification,
                                object: nil)
                        }) {
                            HStack(spacing: 14) {
                                Image(voice.avatarName)
                                    .resizable()
                                    .scaledToFill()
                                    .frame(width: 44, height: 44)
                                    .clipShape(Circle())
                                    .overlay(
                                        Circle()
                                            .stroke(isSelected ? SaatTokens.Colors.deepEmerald : Color(hex: 0xFFE5_E7EB), lineWidth: isSelected ? 2 : 1)
                                    )

                                Text(voice.displayName)
                                    .font(.system(size: 15, weight: isSelected ? .bold : .semibold))
                                    .foregroundColor(isSelected ? SaatTokens.Colors.deepEmerald : Color(hex: 0xFF1C_1C1E))
                                    .frame(maxWidth: .infinity, alignment: .leading)

                                // Preview audio button
                                Button(action: { togglePreview(voice) }) {
                                    ZStack {
                                        Circle()
                                            .fill(SaatTokens.Colors.deepEmerald.opacity(0.12))
                                            .frame(width: 36, height: 36)

                                        Image(systemName: isPlaying ? "stop.fill" : "play.fill")
                                            .font(.system(size: 14, weight: .bold))
                                            .foregroundColor(SaatTokens.Colors.deepEmerald)
                                    }
                                }
                                .buttonStyle(.plain)

                                if isSelected {
                                    Image(systemName: "checkmark")
                                        .font(.system(size: 14, weight: .bold))
                                        .foregroundColor(SaatTokens.Colors.deepEmerald)
                                        .padding(.leading, 4)
                                }
                            }
                            .padding(.horizontal, 14)
                            .padding(.vertical, 10)
                            .background(
                                RoundedRectangle(cornerRadius: 14)
                                    .fill(isSelected ? SaatTokens.Colors.deepEmerald.opacity(0.08) : Color.white)
                            )
                            .overlay(
                                RoundedRectangle(cornerRadius: 14)
                                    .stroke(isSelected ? SaatTokens.Colors.deepEmerald.opacity(0.3) : Color(hex: 0xFFEA_E4D6), lineWidth: 1)
                            )
                        }
                        .buttonStyle(.plain)
                    }
                }
                .padding(.horizontal, 20)
                .padding(.vertical, 14)
            }
            .background(SaatTokens.Colors.homeBg)
        }
        .onDisappear {
            stopPreview()
        }
    }

    private func togglePreview(_ voice: AdhanVoiceModel) {
        if playingOptionId == voice.id {
            stopPreview()
        } else {
            playPreview(voice)
        }
    }

    private func playPreview(_ voice: AdhanVoiceModel) {
        stopPreview()

        guard
            let url = Bundle.main.url(forResource: voice.fileName, withExtension: "mp3")
                ?? Bundle.main.url(
                    forResource: voice.fileName, withExtension: "mp3", subdirectory: "adhan")
                ?? Bundle.main.url(
                    forResource: voice.fileName, withExtension: "mp3", subdirectory: "Resources/adhan")
        else {
            print("Could not find audio for \(voice.fileName)")
            return
        }

        do {
            let session = AVAudioSession.sharedInstance()
            try session.setCategory(.playback, mode: .default, options: [])
            try session.setActive(true)

            audioPlayer = try AVAudioPlayer(contentsOf: url)
            audioPlayer?.play()
            playingOptionId = voice.id
        } catch {
            print("Failed to play Adhan preview: \(error)")
        }
    }

    private func stopPreview() {
        audioPlayer?.stop()
        audioPlayer = nil
        playingOptionId = nil
    }
}
