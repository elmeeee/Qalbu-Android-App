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

enum RadioCategory: String, CaseIterable, Identifiable {
    case all = "ALL"
    case malaysia = "MALAYSIA"
    case indonesia = "INDONESIA"
    case singapore = "SINGAPORE"
    case brunei = "BRUNEI"
    case murottalGlobal = "MUROTTAL_GLOBAL"

    var id: String { rawValue }

    func label(for lang: AppLanguage) -> String {
        switch self {
        case .all:
            switch lang {
            case .english: return "All"
            case .indonesian, .malay: return "Semua"
            }
        case .malaysia: return "Malaysia"
        case .indonesia: return "Indonesia"
        case .singapore:
            switch lang {
            case .english: return "Singapore"
            case .indonesian, .malay: return "Singapura"
            }
        case .brunei: return "Brunei"
        case .murottalGlobal: return "Murottal 24/7"
        }
    }
}

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

    var iconName: String {
        switch id {
        case "suara_muslim_id": return "radio_suaramuslim"
        case "ikim_my": return "radio_ikimfm"
        case "saudi_quran": return "radio_makkahmadinahlive"
        case "warna_sg": return "radio_warnafm"
        case "nur_islam_bn": return "radio_rtb"
        case "alafasy_radio": return "radio_alafasy"
        case "minshawi_radio": return "radio_minshawi"
        case "maher_radio": return "radio_maher"
        case "abdulbasit_radio": return "radio_abdulbasit"
        case "shuraim_radio": return "radio_shuraim"
        default: return "ic_radio_3d"
        }
    }

    func country(for lang: AppLanguage) -> String {
        switch lang {
        case .indonesian: return countryId
        case .malay: return countryMs.isEmpty ? countryId : countryMs
        case .english: return countryEn.isEmpty ? countryId : countryEn
        }
    }

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

    init() {
        configureAudioSession()
    }

    private func configureAudioSession() {
        Task.detached(priority: .userInitiated) {
            do {
                try AVAudioSession.sharedInstance().setCategory(.playback, mode: .default, options: [.duckOthers])
                try AVAudioSession.sharedInstance().setActive(true)
            } catch {
                // Audio session config error
            }
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
    @State private var selectedCategory: RadioCategory = .all

    private var filteredStations: [RadioStationItem] {
        if selectedCategory == .all {
            return stations
        } else {
            return stations.filter { $0.category == selectedCategory.rawValue }
        }
    }

    var body: some View {
        VStack(spacing: 0) {
            // Top Bar
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

                RadioLiveStatusBadge(isLive: radioController.isPlaying)
            }
            .padding(.horizontal, 20)
            .padding(.vertical, 12)
            .background(SaatTokens.Colors.homeBg)

            ScrollView {
                VStack(spacing: 16) {
                    // Hero / Now Playing Card
                    RadioHeroPlayerCard(
                        station: radioController.currentStation,
                        isPlaying: radioController.isPlaying,
                        lang: languageManager.currentLanguage,
                        onToggle: { radioController.togglePlayPause() },
                        onStop: { radioController.stop() }
                    )

                    // Category Filter Pills
                    RadioCategoryFilterBar(
                        selectedCategory: $selectedCategory,
                        lang: languageManager.currentLanguage
                    )

                    // Section Title
                    HStack {
                        Text("Stasiun Radio")
                            .font(.system(size: 16, weight: .bold))
                            .foregroundColor(Color(hex: "#085E43"))
                        Spacer()
                    }
                    .padding(.top, 4)

                    // Station Cards List
                    VStack(spacing: 12) {
                        ForEach(filteredStations) { station in
                            let isCurrent = radioController.currentStation?.id == station.id
                            RadioStationCard(
                                station: station,
                                isPlaying: isCurrent && radioController.isPlaying,
                                lang: languageManager.currentLanguage,
                                onPlayClick: {
                                    UIImpactFeedbackGenerator(style: .medium).impactOccurred()
                                    if isCurrent {
                                        radioController.togglePlayPause()
                                    } else {
                                        radioController.play(station: station)
                                    }
                                }
                            )
                        }
                    }
                }
                .padding(.horizontal, 20)
                .padding(.top, 8)
                .padding(.bottom, 36)
            }
            .background(SaatTokens.Colors.homeBg)
        }
        .background(SaatTokens.Colors.homeBg.ignoresSafeArea())
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

// MARK: - Live Status Badge
private struct RadioLiveStatusBadge: View {
    let isLive: Bool
    @State private var isPulsing = false

    var body: some View {
        HStack(spacing: 6) {
            Circle()
                .fill(isLive ? Color(hex: "#E53935") : Color.gray)
                .frame(width: 8, height: 8)
                .scaleEffect(isLive && isPulsing ? 1.25 : 0.85)
                .opacity(isLive && isPulsing ? 1.0 : 0.4)
                .animation(
                    isLive ? Animation.easeInOut(duration: 0.7).repeatForever(autoreverses: true) : .default,
                    value: isPulsing
                )

            Text(isLive ? "LIVE" : "OFFLINE")
                .font(.system(size: 11, weight: .bold))
                .foregroundColor(isLive ? Color(hex: "#D32F2F") : Color(hex: "#64748B"))
        }
        .padding(.horizontal, 10)
        .padding(.vertical, 4)
        .background(isLive ? Color(hex: "#E57373").opacity(0.15) : Color.black.opacity(0.04))
        .clipShape(Capsule())
        .overlay(
            Capsule()
                .stroke(isLive ? Color(hex: "#E53935") : Color.gray.opacity(0.3), lineWidth: 1)
        )
        .onAppear {
            isPulsing = true
        }
    }
}

// MARK: - Hero Player Card
private struct RadioHeroPlayerCard: View {
    let station: RadioStationItem?
    let isPlaying: Bool
    let lang: AppLanguage
    let onToggle: () -> Void
    let onStop: () -> Void

    @State private var rotationDegrees: Double = 0

    var body: some View {
        ZStack {
            RoundedRectangle(cornerRadius: 24)
                .fill(
                    LinearGradient(
                        colors: [
                            Color(hex: "#063A2A"),
                            Color(hex: "#0F4435"),
                            Color(hex: "#0A2E24")
                        ],
                        startPoint: .topLeading,
                        endPoint: .bottomTrailing
                    )
                )
                .overlay(
                    RoundedRectangle(cornerRadius: 24)
                        .stroke(
                            LinearGradient(
                                colors: [
                                    Color(hex: "#D4AF37"),
                                    Color(hex: "#D4AF37").opacity(0.3)
                                ],
                                startPoint: .topLeading,
                                endPoint: .bottomTrailing
                            ),
                            lineWidth: 1.5
                        )
                )
                .shadow(color: Color.black.opacity(0.12), radius: 10, x: 0, y: 5)

            VStack(spacing: 16) {
                // Top Header Row
                HStack {
                    HStack(spacing: 8) {
                        ZStack {
                            Circle()
                                .fill(Color(hex: "#D4AF37").opacity(0.2))
                                .frame(width: 32, height: 32)
                            Image(systemName: "radio")
                                .font(.system(size: 16, weight: .bold))
                                .foregroundColor(Color(hex: "#FFD700"))
                        }

                        Text("RADIO QUR'AN")
                            .font(.system(size: 12, weight: .bold))
                            .foregroundColor(Color(hex: "#FFD700"))
                            .tracking(1.2)
                    }

                    Spacer()

                    if station != nil {
                        RadioLiveStatusBadge(isLive: isPlaying)
                    }
                }

                if let current = station {
                    // Active Radio State
                    HStack(spacing: 16) {
                        // Animated Vinyl Disc Art
                        ZStack {
                            Circle()
                                .fill(Color(hex: "#1E1E1E"))
                                .frame(width: 68, height: 68)
                                .overlay(
                                    Circle()
                                        .stroke(Color(hex: "#D4AF37"), lineWidth: 2)
                                )
                                .shadow(color: Color.black.opacity(0.3), radius: 6, x: 0, y: 3)

                            Image(current.iconName)
                                .resizable()
                                .scaledToFill()
                                .frame(width: 68, height: 68)
                                .clipShape(Circle())
                                .rotationEffect(.degrees(rotationDegrees))

                            // Center Spindle Hole
                            Circle()
                                .fill(Color(hex: "#063A2A"))
                                .frame(width: 14, height: 14)
                                .overlay(
                                    Circle()
                                        .stroke(Color(hex: "#D4AF37"), lineWidth: 1.5)
                                )
                        }

                        VStack(alignment: .leading, spacing: 4) {
                            Text(current.name)
                                .font(.system(size: 16, weight: .bold))
                                .foregroundColor(.white)
                                .lineLimit(1)

                            Text(current.country(for: lang) + " • " + current.description(for: lang))
                                .font(.system(size: 12))
                                .foregroundColor(.white.opacity(0.85))
                                .lineLimit(1)
                        }

                        Spacer()

                        RadioSoundEqualizer(isPlaying: isPlaying)
                    }

                    // Controls Row
                    HStack {
                        HStack(spacing: 12) {
                            Button(action: onToggle) {
                                ZStack {
                                    Circle()
                                        .fill(Color(hex: "#D4AF37"))
                                        .frame(width: 50, height: 50)
                                        .shadow(color: Color.black.opacity(0.2), radius: 6, x: 0, y: 3)

                                    Image(systemName: isPlaying ? "pause.fill" : "play.fill")
                                        .font(.system(size: 20, weight: .bold))
                                        .foregroundColor(Color(hex: "#063A2A"))
                                }
                            }

                            Button(action: onStop) {
                                ZStack {
                                    Circle()
                                        .fill(Color.white.opacity(0.15))
                                        .frame(width: 40, height: 40)

                                    Image(systemName: "stop.fill")
                                        .font(.system(size: 16, weight: .bold))
                                        .foregroundColor(.white)
                                }
                            }
                        }

                        Spacer()

                        HStack(spacing: 6) {
                            Text(isPlaying ? "HD Live Stream" : "Paused")
                                .font(.system(size: 11, weight: .semibold))
                                .foregroundColor(isPlaying ? Color(hex: "#FFD700") : Color.white.opacity(0.7))
                        }
                        .padding(.horizontal, 10)
                        .padding(.vertical, 5)
                        .background(Color.white.opacity(0.12))
                        .cornerRadius(14)
                        .overlay(
                            RoundedRectangle(cornerRadius: 14)
                                .stroke(Color.white.opacity(0.2), lineWidth: 1)
                        )
                    }
                } else {
                    // Idle State
                    VStack(alignment: .leading, spacing: 6) {
                        Text("Dengarkan Siaran Qur'an & Dakwah")
                            .font(.system(size: 16, weight: .bold))
                            .foregroundColor(.white)

                        Text("Pilih stasiun radio di bawah untuk memulai siaran langsung murottal 24 jam nonstop.")
                            .font(.system(size: 12))
                            .foregroundColor(.white.opacity(0.8))
                            .lineSpacing(2)
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(.vertical, 4)
                }
            }
            .padding(20)
        }
        .onChange(of: isPlaying) { _, playing in
            if playing {
                withAnimation(.linear(duration: 12).repeatForever(autoreverses: false)) {
                    rotationDegrees = 360
                }
            } else {
                withAnimation(.default) {
                    rotationDegrees = 0
                }
            }
        }
    }
}

// MARK: - Sound Equalizer Animation
private struct RadioSoundEqualizer: View {
    let isPlaying: Bool
    @State private var animate = false

    var body: some View {
        HStack(spacing: 3) {
            bar(heightFactor: animate && isPlaying ? 0.95 : 0.3)
                .animation(isPlaying ? Animation.easeInOut(duration: 0.42).repeatForever(autoreverses: true) : .default, value: animate)

            bar(heightFactor: animate && isPlaying ? 0.35 : 0.85)
                .animation(isPlaying ? Animation.easeInOut(duration: 0.33).repeatForever(autoreverses: true) : .default, value: animate)

            bar(heightFactor: animate && isPlaying ? 1.0 : 0.4)
                .animation(isPlaying ? Animation.easeInOut(duration: 0.52).repeatForever(autoreverses: true) : .default, value: animate)

            bar(heightFactor: animate && isPlaying ? 0.5 : 0.75)
                .animation(isPlaying ? Animation.easeInOut(duration: 0.38).repeatForever(autoreverses: true) : .default, value: animate)
        }
        .frame(height: 20, alignment: .bottom)
        .onAppear {
            animate = true
        }
    }

    private func bar(heightFactor: CGFloat) -> some View {
        RoundedRectangle(cornerRadius: 2)
            .fill(
                LinearGradient(
                    colors: [Color(hex: "#FFD700"), Color(hex: "#D4AF37")],
                    startPoint: .top,
                    endPoint: .bottom
                )
            )
            .frame(width: 3.5, height: 20 * heightFactor)
    }
}

// MARK: - Category Filter Bar
private struct RadioCategoryFilterBar: View {
    @Binding var selectedCategory: RadioCategory
    let lang: AppLanguage

    var body: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 8) {
                ForEach(RadioCategory.allCases) { category in
                    let isSelected = selectedCategory == category
                    Button(action: {
                        UIImpactFeedbackGenerator(style: .light).impactOccurred()
                        selectedCategory = category
                    }) {
                        Text(category.label(for: lang))
                            .font(.system(size: 13, weight: isSelected ? .bold : .medium))
                            .foregroundColor(isSelected ? .white : Color(hex: "#1E293B"))
                            .padding(.horizontal, 16)
                            .padding(.vertical, 8)
                            .background(isSelected ? Color(hex: "#085E43") : Color.white)
                            .clipShape(Capsule())
                            .overlay(
                                Capsule()
                                    .stroke(isSelected ? Color.clear : Color(hex: "#E8E2D2"), lineWidth: 1)
                            )
                            .shadow(color: Color.black.opacity(isSelected ? 0.08 : 0.02), radius: 3, x: 0, y: 1)
                    }
                    .buttonStyle(.plain)
                }
            }
            .padding(.vertical, 2)
        }
    }
}

// MARK: - Station Card
private struct RadioStationCard: View {
    let station: RadioStationItem
    let isPlaying: Bool
    let lang: AppLanguage
    let onPlayClick: () -> Void

    @State private var haloPulsing = false

    var body: some View {
        Button(action: onPlayClick) {
            HStack(spacing: 14) {
                // Station Avatar with Pulsing Halo & Country Badge
                ZStack {
                    if isPlaying {
                        RoundedRectangle(cornerRadius: 18)
                            .fill(Color(hex: "#D4AF37").opacity(haloPulsing ? 0.35 : 0.1))
                            .frame(width: 56, height: 56)
                            .scaleEffect(haloPulsing ? 1.15 : 1.0)
                            .animation(
                                Animation.easeInOut(duration: 1.1).repeatForever(autoreverses: true),
                                value: haloPulsing
                            )
                    }

                    Image(station.iconName)
                        .resizable()
                        .scaledToFill()
                        .frame(width: 50, height: 50)
                        .clipShape(RoundedRectangle(cornerRadius: 16))
                        .overlay(
                            RoundedRectangle(cornerRadius: 16)
                                .stroke(isPlaying ? Color(hex: "#D4AF37") : Color.black.opacity(0.08), lineWidth: isPlaying ? 1.5 : 1)
                        )
                }
                .frame(width: 56, height: 56)

                // Name & Description
                VStack(alignment: .leading, spacing: 4) {
                    HStack(spacing: 6) {
                        Text(station.name)
                            .font(.system(size: 15, weight: .bold))
                            .foregroundColor(isPlaying ? .white : Color(hex: "#1E293B"))
                            .lineLimit(1)

                        if isPlaying {
                            Text("PLAYING")
                                .font(.system(size: 9, weight: .bold))
                                .foregroundColor(Color(hex: "#063A2A"))
                                .padding(.horizontal, 6)
                                .padding(.vertical, 2)
                                .background(Color(hex: "#D4AF37"))
                                .clipShape(RoundedRectangle(cornerRadius: 8))
                        }
                    }

                    Text(station.description(for: lang))
                        .font(.system(size: 12))
                        .foregroundColor(isPlaying ? .white.opacity(0.85) : Color(hex: "#64748B"))
                        .lineLimit(2)
                }

                Spacer()

                // Play / Pause Circle Button
                ZStack {
                    Circle()
                        .fill(isPlaying ? Color(hex: "#D4AF37") : Color(hex: "#085E43").opacity(0.1))
                        .frame(width: 42, height: 42)

                    Image(systemName: isPlaying ? "pause.fill" : "play.fill")
                        .font(.system(size: 16, weight: .bold))
                        .foregroundColor(isPlaying ? Color(hex: "#063A2A") : Color(hex: "#085E43"))
                }
            }
            .padding(14)
            .background(
                isPlaying ? Color(hex: "#063A2A") : Color.white
            )
            .cornerRadius(20)
            .overlay(
                RoundedRectangle(cornerRadius: 20)
                    .stroke(
                        isPlaying ? Color(hex: "#D4AF37").opacity(0.6) : Color(hex: "#EAE4D6"),
                        lineWidth: 1
                    )
            )
            .shadow(color: Color.black.opacity(isPlaying ? 0.08 : 0.03), radius: 6, x: 0, y: 2)
        }
        .buttonStyle(.plain)
        .onAppear {
            haloPulsing = true
        }
    }
}
