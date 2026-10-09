//
//  DhikrTasbihView.swift
//  Saat
//
//  Created by Elmee on 25/06/2026.
//  Copyright © 2026 Elmee. All rights reserved.
//

import SwiftUI

struct DhikrTasbihView: View {
    @Environment(\.dismiss) private var dismiss
    
    @ObservedObject private var languageManager = AppLanguageManager.shared
    
    @State private var selectedIndex = 0
    @State private var count = 0
    @State private var pulseKey = 0

    private var language: String {
        let raw = languageManager.currentLanguage.rawValue
        return (raw == "id" || raw == "ms") ? raw : "en"
    }

    private var currentPreset: DhikrPreset {
        return DhikrStore.presets[selectedIndex]
    }

    var body: some View {
        VStack(spacing: 0) {
            // Custom Top Bar
            HStack(spacing: 8) {
                Button(action: {
                    dismiss()
                }) {
                    Image(systemName: "arrow.left")
                        .font(.system(size: 20, weight: .semibold))
                        .foregroundColor(SaatTokens.Colors.slate900)
                        .frame(width: 44, height: 44)
                }
                
                VStack(alignment: .leading, spacing: 2) {
                    Text(languageManager.localize("dhikr_title"))
                        .font(.title2.bold())
                        .foregroundColor(SaatTokens.Colors.slate900)
                    
                    Text(languageManager.localize("dhikr_subtitle"))
                        .font(.system(size: 12, weight: .regular))
                        .foregroundColor(Color.Token.slate500)
                }
                
                Spacer()
            }
            .padding(.horizontal, SaatTokens.Spacing.screenHorizontal)
            .padding(.vertical, 8)
            
            // Horizontal Presets Selector
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    ForEach(0..<DhikrStore.presets.count, id: \.self) { index in
                        let preset = DhikrStore.presets[index]
                        DhikrPresetChip(
                            title: preset.label(for: language),
                            isSelected: index == selectedIndex
                        ) {
                            selectedIndex = index
                            count = DhikrStore.sessionCount(for: preset.id)
                        }
                    }
                }
                .padding(.horizontal, 20)
                .padding(.vertical, 12)
            }
            
            Spacer().frame(height: 12)
            
            // Main Content Area
            VStack(spacing: 8) {
                // Reading Card
                DhikrReadingCard(preset: currentPreset, language: language)
                
                // Interactive 33-Bead Physical Tasbih
                TasbeehCounterWidget(
                    count: count,
                    pulseKey: pulseKey,
                    target: currentPreset.target,
                    subtitle: language == "id" ? "Putaran \(count > 0 ? ((count - 1) / 33) + 1 : 1)" : "Round \(count > 0 ? ((count - 1) / 33) + 1 : 1)",
                    onTap: {
                        count = DhikrStore.increment(for: currentPreset.id)
                        pulseKey += 1
                        
                        UIImpactFeedbackGenerator(style: .light).impactOccurred()
                        
                        if count > 0 && count % currentPreset.target == 0 {
                            UINotificationFeedbackGenerator().notificationOccurred(.success)
                        }
                    }
                )
                .frame(maxWidth: .infinity, maxHeight: .infinity)
                
                // Stats and Reset Area
                DhikrStatsRow(
                    count: count,
                    target: currentPreset.target,
                    lifetime: DhikrStore.totalCount(for: currentPreset.id),
                    onReset: {
                        DhikrStore.resetSession(for: currentPreset.id)
                        count = 0
                    },
                    language: language
                )
                .background(Color.Token.pureWhite)
                .cornerRadius(24)
                .shadow(color: Color.black.opacity(0.02), radius: 8, x: 0, y: -4)
                .padding(.horizontal, 16)
                .padding(.bottom, 16)
            }
        }
        .background(
            LinearGradient(
                colors: [SaatTokens.Colors.screenBackground, SaatTokens.Colors.sageMist, SaatTokens.Colors.prayerMint],
                startPoint: .top,
                endPoint: .bottom
            )
            .ignoresSafeArea()
        )
        .navigationBarBackButtonHidden(true)
        .navigationBarHidden(true)
        .toolbar(.hidden, for: .tabBar)
        .onAppear {
            count = DhikrStore.sessionCount(for: currentPreset.id)
        }
    }
}

// Preset Chip
struct DhikrPresetChip: View {
    let title: String
    let isSelected: Bool
    let action: () -> Void
    
    var body: some View {
        Button(action: action) {
            Text(title)
                .font(.system(size: 14, weight: .medium))
                .foregroundColor(isSelected ? SaatTokens.Colors.pureWhite : SaatTokens.Colors.slate900)
                .padding(.horizontal, 12)
                .padding(.vertical, 6)
                .background(
                    RoundedRectangle(cornerRadius: 8)
                        .fill(isSelected ? SaatTokens.Colors.deepEmerald : Color.clear)
                )
                .overlay(
                    RoundedRectangle(cornerRadius: 8)
                        .stroke(isSelected ? Color.clear : SaatTokens.Colors.softGrey, lineWidth: 1)
                )
        }
    }
}

// Reading Card
struct DhikrReadingCard: View {
    let preset: DhikrPreset
    let language: String
    
    var body: some View {
        VStack(spacing: 12) {
            Text(preset.arabic)
                .font(.system(size: 26, weight: .regular))
                .multilineTextAlignment(.center)
                .foregroundColor(Color.Token.deepEmerald)
                .padding(.top, 4)
            
            Text(preset.translit(for: language))
                .font(.system(size: 13, weight: .medium, design: .serif))
                .italic()
                .multilineTextAlignment(.center)
                .foregroundColor(Color.Token.teal)
            
            Text(preset.meaning(for: language))
                .font(.system(size: 12, weight: .regular))
                .multilineTextAlignment(.center)
                .foregroundColor(Color.Token.slate500)
                .padding(.bottom, 4)
        }
        .frame(maxWidth: .infinity)
        .padding(18)
        .background(SaatTokens.Colors.pureWhite)
        .cornerRadius(20)
        .shadow(color: Color.black.opacity(0.05), radius: 4, x: 0, y: 2)
        .overlay(
            RoundedRectangle(cornerRadius: 20)
                .stroke(
                    LinearGradient(
                        colors: [SaatTokens.Colors.teal.opacity(0.25), SaatTokens.Colors.gold.opacity(0.2)],
                        startPoint: .topLeading,
                        endPoint: .bottomTrailing
                    ),
                    lineWidth: 1
                )
        )
        .padding(.horizontal, 20)
    }
}

// Stats Row
struct DhikrStatsRow: View {
    let count: Int
    let target: Int
    let lifetime: Int
    let onReset: () -> Void
    let language: String

    private var progressPercent: Int {
        return target > 0 ? min(100, count * 100 / target) : 0
    }

    var body: some View {
        HStack {
            VStack(alignment: .leading, spacing: 4) {
                Text((language == "id" || language == "ms") ? "\(progressPercent)% dari target" : "\(progressPercent)% of target")
                    .font(.system(size: 13, weight: .semibold))
                    .foregroundColor(Color.Token.deepEmerald)
                
                Text((language == "id" || language == "ms") ? "Total: \(lifetime)" : "Lifetime: \(lifetime)")
                    .font(.system(size: 11, weight: .regular))
                    .foregroundColor(Color.Token.slate500)
            }
            
            Spacer()
            
            Button(action: onReset) {
                HStack(spacing: 4) {
                    Image(systemName: "arrow.clockwise")
                        .font(.system(size: 12, weight: .semibold))
                    Text("Reset")
                }
                .font(.system(size: 13, weight: .semibold))
                .foregroundColor(Color.Token.deepEmerald)
                .padding(.horizontal, 16)
                .padding(.vertical, 8)
                .background(
                    Capsule()
                        .stroke(Color.Token.deepEmerald.opacity(0.7), lineWidth: 1)
                )
            }
        }
        .padding(.horizontal, 20)
        .padding(.vertical, 16)
    }
}

// 33-Bead Physical Tasbih Widget (matching Android TasbeehCounterWidget)
struct TasbeehCounterWidget: View {
    let count: Int
    let pulseKey: Int
    let target: Int
    let subtitle: String
    let onTap: () -> Void

    private let totalBeads = 33

    private var activeIndex: Int {
        if count > 0 {
            return (count - 1) % totalBeads
        }
        return 0
    }

    var body: some View {
        GeometryReader { geo in
            let w = geo.size.width
            let h = geo.size.height
            let size = min(w, h)
            let centerX = w / 2.0
            let centerY = h * 0.40
            let radius = size * 0.31
            let beadSize = size * 0.085

            let deltaGap: CGFloat = 0.32 // radians (~18 deg gap at bottom)
            let startAngle = (CGFloat.pi / 2.0) + deltaGap
            let totalSpan = (2.0 * CGFloat.pi) - (2.0 * deltaGap)

            let beadCoords: [CGPoint] = (0..<totalBeads).map { i in
                let fraction = CGFloat(i) / CGFloat(totalBeads - 1)
                let angle = startAngle + (fraction * totalSpan)
                let bx = centerX + cos(angle) * radius
                let by = centerY + sin(angle) * radius
                return CGPoint(x: bx, y: by)
            }

            ZStack {
                // Background Tap Area
                Color.clear
                    .contentShape(Rectangle())
                    .gesture(
                        DragGesture(minimumDistance: 10)
                            .onEnded { _ in
                                onTap()
                            }
                    )
                    .onTapGesture {
                        onTap()
                    }

                // Layer 1: Connecting Rope/Cord
                Path { path in
                    if let first = beadCoords.first {
                        path.move(to: first)
                        for pt in beadCoords.dropFirst() {
                            path.addLine(to: pt)
                        }
                        let bottomCenter = CGPoint(x: centerX, y: centerY + radius)
                        path.addLine(to: bottomCenter)
                        path.addLine(to: first)
                    }
                }
                .stroke(Color(hex: 0x6E4723), style: StrokeStyle(lineWidth: 3.5, lineCap: .round, lineJoin: .round))

                // Layer 2: 33 Beads
                ForEach(0..<totalBeads, id: \.self) { i in
                    let coord = beadCoords[i]
                    let isActive = (i == activeIndex)

                    Image(isActive ? "bead_active" : "bead")
                        .resizable()
                        .scaledToFit()
                        .frame(width: beadSize, height: beadSize)
                        .scaleEffect(isActive ? 1.25 : 1.0)
                        .animation(.spring(response: 0.25, dampingFraction: 0.6), value: pulseKey)
                        .position(x: coord.x, y: coord.y)
                }

                // Layer 3: Connector & Tassel
                let bottomBeadY = centerY + radius
                let ballSize = size * 0.085
                let capWidth = size * 0.080
                let capHeight = capWidth * 0.70
                let tasselCapWidth = size * 0.075
                let tasselCapHeight = tasselCapWidth * 0.65
                let tasselWidth = size * 0.14
                let tasselHeight = tasselWidth * 1.25

                // Gold Connector Ball
                Image("connector_ball")
                    .resizable()
                    .scaledToFit()
                    .frame(width: ballSize, height: ballSize)
                    .position(x: centerX, y: bottomBeadY + (ballSize * 0.10))

                // Gold Connector Cap
                Image("connector_cap")
                    .resizable()
                    .scaledToFit()
                    .frame(width: capWidth, height: capHeight)
                    .position(x: centerX, y: bottomBeadY + (ballSize * 0.30) + (capHeight * 0.50))

                // Tassel Cap
                Image("tassel_cap")
                    .resizable()
                    .scaledToFit()
                    .frame(width: tasselCapWidth, height: tasselCapHeight)
                    .position(x: centerX, y: bottomBeadY + (ballSize * 0.30) + (capHeight * 0.65) + (tasselCapHeight * 0.50))

                // Tassel
                Image("tassel")
                    .resizable()
                    .scaledToFit()
                    .frame(width: tasselWidth, height: tasselHeight)
                    .position(x: centerX, y: bottomBeadY + (ballSize * 0.30) + (capHeight * 0.65) + (tasselCapHeight * 0.50) + (tasselHeight * 0.45))

                // Layer 4: Center Display
                VStack(spacing: 4) {
                    Text("\(count)")
                        .font(.system(size: 44, weight: .bold, design: .rounded))
                        .foregroundColor(Color.Token.deepEmerald)
                        .scaleEffect(pulseKey > 0 ? 1.05 : 1.0)
                        .animation(.spring(response: 0.2, dampingFraction: 0.5), value: pulseKey)

                    Text("/ \(target)")
                        .font(.system(size: 14, weight: .semibold))
                        .foregroundColor(Color.Token.teal)

                    if !subtitle.isEmpty {
                        Text(subtitle)
                            .font(.system(size: 11, weight: .medium))
                            .foregroundColor(Color.Token.slate500)
                            .padding(.top, 2)
                    }
                    
                    Text(AppLanguageManager.shared.currentLanguage == .english ? "Tap anywhere to count" : "Ketuk di mana saja untuk menghitung")
                        .font(.system(size: 10, weight: .medium))
                        .foregroundColor(Color.Token.slate500.opacity(0.7))
                        .padding(.top, 4)
                }
                .position(x: centerX, y: centerY)
            }
        }
    }
}

#Preview {
    NavigationStack {
        DhikrTasbihView()
    }
}

