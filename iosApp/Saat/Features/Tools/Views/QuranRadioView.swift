//
//  QuranRadioView.swift
//  Saat
//
//  Created by Sufiandy Elmy on 09/10/2026.
//  Copyright © 2026 Elmee. All rights reserved.
//

import SwiftUI
import AVFoundation
import Combine

struct RadioStationItem: Identifiable, Codable, Equatable {
    let id: String
    let name: String
    let countryEn: String
    let countryId: String
    let countryMs: String
    let countryFlag: String
    let descriptionEn: String
    let descriptionId: String
    let descriptionMs: String
    let category: String
    let streamUrl: String

    func description(for lang: AppLanguage) -> String {
        switch lang {
        case .indonesian: return descriptionId
        case .malay: return descriptionMs.isEmpty ? descriptionId : descriptionMs
        case .english: return descriptionEn.isEmpty ? descriptionId : descriptionEn
        }
    }
}

@MainActor
final class QuranRadioPlayerController: ObservableObject {
    @Published var currentStation: RadioStationItem? = nil
    @Published var isPlaying: Bool = false
    @Published var isLoading: Bool = false
    @Published var errorMessage: String? = nil

    private var player: AVPlayer?
    private var timeObserver: Any?

    init() {
        configureAudioSession()
    }

    private func configureAudioSession() {
        do {
            try AVAudioSession.sharedInstance().setCategory(.playback, mode: .default, options: [.duckOthers])
            try AVAudioSession.sharedInstance().setActive(true)
        } catch {
            // Audio session config error
        }
    }

    func play(station: RadioStationItem) {
        if currentStation?.id == station.id && isPlaying {
            pause()
            return
        }

        currentStation = station
        isLoading = true
        errorMessage = nil

        guard let url = URL(string: station.streamUrl) else {
            isLoading = false
            errorMessage = "URL streaming tidak valid"
            return
        }

        player?.pause()
        let playerItem = AVPlayerItem(url: url)
        player = AVPlayer(playerItem: playerItem)
        player?.play()
        isPlaying = true
        isLoading = false
    }

    func togglePlayPause() {
        if isPlaying {
            pause()
        } else if let station = currentStation {
            play(station: station)
        }
    }

    func pause() {
        player?.pause()
        isPlaying = false
    }

    func stop() {
        player?.pause()
        player = nil
        isPlaying = false
        currentStation = nil
    }

    deinit {
        player?.pause()
        player = nil
    }
}

struct QuranRadioView: View {
    @Environment(\.dismiss) private var dismiss
    @ObservedObject private var languageManager = AppLanguageManager.shared
    @StateObject private var radioController = QuranRadioPlayerController()

    @State private var stations: [RadioStationItem] = []
    @State private var selectedCategory: String = "ALL"
    @State private var searchQuery: String = ""

    private var categories: [String] {
        var cats = ["ALL"]
        let distinct = Array(Set(stations.map { $0.category })).sorted()
        cats.append(contentsOf: distinct)
        return cats
    }

    private var filteredStations: [RadioStationItem] {
        stations.filter { st in
            let matchesCat = (selectedCategory == "ALL" || st.category == selectedCategory)
            if searchQuery.trimmingCharacters(in: .whitespaces).isEmpty {
                return matchesCat
            }
            let q = searchQuery.trimmingCharacters(in: .whitespaces).lowercased()
            return matchesCat && (
                st.name.lowercased().contains(q) ||
                st.description(for: languageManager.currentLanguage).lowercased().contains(q)
            )
        }
    }

    var body: some View {
        VStack(spacing: 0) {
            // Header Bar
            HStack(spacing: 12) {
                Button(action: {
                    radioController.stop()
                    dismiss()
                }) {
                    Image(systemName: "arrow.left")
                        .font(.system(size: 18, weight: .semibold))
                        .foregroundColor(Color(hex: "#085E43"))
                        .frame(width: 40, height: 40)
                        .background(Color.white)
                        .clipShape(Circle())
                        .shadow(color: Color.black.opacity(0.04), radius: 3, x: 0, y: 1)
                }

                VStack(alignment: .leading, spacing: 2) {
                    Text(languageManager.localize("tool_radio_title"))
                        .font(.system(size: 18, weight: .bold))
                        .foregroundColor(Color(hex: "#1E293B"))

                    Text("Streaming Al-Qur'an 24 Jam Nonstop")
                        .font(.system(size: 12, weight: .regular))
                        .foregroundColor(Color(hex: "#64748B"))
                }

                Spacer()
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 10)
            .background(Color(hex: "#F9F7F2"))

            // Search Bar
            HStack(spacing: 10) {
                Image(systemName: "magnifyingglass")
                    .foregroundColor(Color(hex: "#085E43").opacity(0.7))

                TextField("Cari stasiun radio…", text: $searchQuery)
                    .font(.system(size: 14))
                    .foregroundColor(Color(hex: "#1E293B"))

                if !searchQuery.isEmpty {
                    Button(action: { searchQuery = "" }) {
                        Image(systemName: "xmark.circle.fill")
                            .foregroundColor(Color(hex: "#94A3B8"))
                    }
                }
            }
            .padding(.horizontal, 14)
            .frame(height: 44)
            .background(Color.white)
            .cornerRadius(12)
            .overlay(
                RoundedRectangle(cornerRadius: 12)
                    .stroke(Color(hex: "#E8E2D2"), lineWidth: 1)
            )
            .padding(.horizontal, 16)
            .padding(.vertical, 8)
            .background(Color(hex: "#F9F7F2"))

            // Station List
            ScrollView {
                VStack(spacing: 12) {
                    ForEach(filteredStations) { station in
                        let isCurrent = radioController.currentStation?.id == station.id
                        RadioStationRowCard(
                            station: station,
                            isCurrent: isCurrent,
                            isPlaying: isCurrent && radioController.isPlaying,
                            lang: languageManager.currentLanguage,
                            onTap: {
                                UIImpactFeedbackGenerator(style: .medium).impactOccurred()
                                radioController.play(station: station)
                            }
                        )
                    }
                }
                .padding(.horizontal, 16)
                .padding(.top, 8)
                .padding(.bottom, radioController.currentStation != nil ? 110 : 32)
            }
            .background(Color(hex: "#F9F7F2"))
        }
        .background(Color(hex: "#F9F7F2").ignoresSafeArea())
        .safeAreaInset(edge: .bottom) {
            if let active = radioController.currentStation {
                RadioBottomMiniPlayer(
                    station: active,
                    isPlaying: radioController.isPlaying,
                    lang: languageManager.currentLanguage,
                    onToggle: { radioController.togglePlayPause() },
                    onClose: { radioController.stop() }
                )
                .transition(.move(edge: .bottom).combined(with: .opacity))
            }
        }
        .navigationBarHidden(true)
        .toolbar(.hidden, for: .navigationBar)
        .toolbar(.hidden, for: .tabBar)
        .onAppear {
            loadStations()
        }
        .onDisappear {
            radioController.stop()
        }
    }

    private func loadStations() {
        guard let url = Bundle.main.url(forResource: "radio_stations", withExtension: "json", subdirectory: "radio") ??
                        Bundle.main.url(forResource: "radio_stations", withExtension: "json") else {
            return
        }
        do {
            let data = try Data(contentsOf: url)
            self.stations = try JSONDecoder().decode([RadioStationItem].self, from: data)
        } catch {
            // Load error
        }
    }
}

// MARK: - Station Card
private struct RadioStationRowCard: View {
    let station: RadioStationItem
    let isCurrent: Bool
    let isPlaying: Bool
    let lang: AppLanguage
    let onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            HStack(spacing: 14) {
                // Radio Avatar / Flag
                ZStack {
                    Circle()
                        .fill(isCurrent ? Color(hex: "#085E43") : Color(hex: "#E6F3EE"))
                        .frame(width: 48, height: 48)

                    if isCurrent && isPlaying {
                        Image(systemName: "waveform")
                            .font(.system(size: 20, weight: .bold))
                            .foregroundColor(.white)
                    } else {
                        Text(station.countryFlag)
                            .font(.system(size: 22))
                    }
                }

                VStack(alignment: .leading, spacing: 3) {
                    Text(station.name)
                        .font(.system(size: 15, weight: isCurrent ? .bold : .semibold))
                        .foregroundColor(isCurrent ? Color(hex: "#085E43") : Color(hex: "#1E293B"))
                        .lineLimit(1)

                    Text(station.description(for: lang))
                        .font(.system(size: 12, weight: .regular))
                        .foregroundColor(Color(hex: "#64748B"))
                        .lineLimit(1)
                }

                Spacer()

                ZStack {
                    Circle()
                        .fill(isCurrent && isPlaying ? Color(hex: "#085E43") : Color(hex: "#F1F5F9"))
                        .frame(width: 36, height: 36)

                    Image(systemName: isCurrent && isPlaying ? "pause.fill" : "play.fill")
                        .font(.system(size: 14, weight: .bold))
                        .foregroundColor(isCurrent && isPlaying ? .white : Color(hex: "#085E43"))
                }
            }
            .padding(14)
            .background(isCurrent ? Color(hex: "#085E43").opacity(0.06) : Color.white)
            .cornerRadius(16)
            .overlay(
                RoundedRectangle(cornerRadius: 16)
                    .stroke(isCurrent ? Color(hex: "#085E43") : Color(hex: "#EAE4D6"), lineWidth: isCurrent ? 1.5 : 1)
            )
            .shadow(color: Color.black.opacity(0.02), radius: 4, x: 0, y: 1)
        }
        .buttonStyle(.plain)
    }
}

// MARK: - Mini Player
private struct RadioBottomMiniPlayer: View {
    let station: RadioStationItem
    let isPlaying: Bool
    let lang: AppLanguage
    let onToggle: () -> Void
    let onClose: () -> Void

    var body: some View {
        HStack(spacing: 12) {
            ZStack {
                Circle()
                    .fill(Color(hex: "#085E43"))
                    .frame(width: 42, height: 42)

                Text(station.countryFlag)
                    .font(.system(size: 20))
            }

            VStack(alignment: .leading, spacing: 2) {
                Text(station.name)
                    .font(.system(size: 14, weight: .bold))
                    .foregroundColor(Color(hex: "#1E293B"))
                    .lineLimit(1)

                Text(isPlaying ? "Sedang Mengudara 🔴" : "Siaran Dijeda")
                    .font(.system(size: 11, weight: .medium))
                    .foregroundColor(isPlaying ? Color(hex: "#085E43") : Color(hex: "#64748B"))
            }

            Spacer()

            Button(action: onToggle) {
                ZStack {
                    Circle()
                        .fill(Color(hex: "#085E43"))
                        .frame(width: 38, height: 38)
                    Image(systemName: isPlaying ? "pause.fill" : "play.fill")
                        .font(.system(size: 15, weight: .bold))
                        .foregroundColor(.white)
                }
            }

            Button(action: onClose) {
                Image(systemName: "xmark")
                    .font(.system(size: 14, weight: .semibold))
                    .foregroundColor(Color(hex: "#94A3B8"))
                    .frame(width: 32, height: 32)
            }
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 10)
        .background(
            Color.white
                .cornerRadius(20)
                .shadow(color: Color.black.opacity(0.12), radius: 12, x: 0, y: 4)
        )
        .padding(.horizontal, 16)
        .padding(.bottom, 12)
    }
}
