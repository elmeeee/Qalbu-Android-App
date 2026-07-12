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
            }
            
            Spacer().frame(height: 12)
            
            // Main Content Area
            VStack(spacing: 16) {
                Spacer()
                    .frame(height: 12)
                
                // Reading Card
                DhikrReadingCard(preset: currentPreset, language: language)
                
                // Interactive Tasbih Tap Area
                ZStack {
                    // Transparent full-width click overlay
                    Color.clear
                        .contentShape(Rectangle())
                        .onTapGesture {
                            count = DhikrStore.increment(for: currentPreset.id)
                            pulseKey += 1
                            
                            // Tap vibration
                            UIImpactFeedbackGenerator(style: .light).impactOccurred()
                            
                            // Target milestone feedback
                            if count > 0 && count % currentPreset.target == 0 {
                                UINotificationFeedbackGenerator().notificationOccurred(.success)
                            }
                        }
                    
                    VStack(spacing: 16) {
                        PremiumTasbihCounter(
                            count: count,
                            target: currentPreset.target,
                            pulseKey: pulseKey,
                            subtitle: language == "id" ? "dari \(currentPreset.target)" : "of \(currentPreset.target)",
                            counterSize: 180.0
                        )
                        
                        Text(language == "id" ? "Ketuk di mana saja untuk menghitung" : "Tap anywhere to count")
                            .font(.system(size: 11, weight: .medium))
                            .foregroundColor(Color.Token.slate500.opacity(0.7))
                    }
                }
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
                .padding(.bottom, 20)
            }
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

#Preview {
    NavigationStack {
        DhikrTasbihView()
    }
}
