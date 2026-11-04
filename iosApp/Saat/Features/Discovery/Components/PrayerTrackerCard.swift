//
//  PrayerTrackerCard.swift
//  Saat
//
//  Created by Elmee on 25/06/2026.
//  Copyright © 2026 Elmee. All rights reserved.
//

import SwiftUI

struct PrayerTrackerCard: View {
    @ObservedObject var viewModel: PrayerTrackerViewModel
    let onOpenCalendar: () -> Void
    @ObservedObject private var languageManager = AppLanguageManager.shared

    @State private var isPrayerDone: Bool = false
    @State private var isQuranDone: Bool = false
    @State private var isDhikrDone: Bool = false
    @State private var isSunnahDone: Bool = false

    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            // Header: Today's Journey & Calendar Streak Icon
            HStack(alignment: .center) {
                VStack(alignment: .leading, spacing: 2) {
                    Text(languageManager.localize("todays_journey_title"))
                        .font(.system(size: 18, weight: .bold))
                        .foregroundColor(Color(hex: 0xFF1E_293B))

                    Text(languageManager.localize("todays_journey_subtitle"))
                        .font(.system(size: 13, weight: .regular))
                        .foregroundColor(Color(hex: 0xFF64_748B))
                }

                Spacer()

                Button(action: onOpenCalendar) {
                    Image(systemName: "calendar")
                        .font(.system(size: 20, weight: .medium))
                        .foregroundColor(SaatTokens.Colors.homeDarkGreen)
                        .padding(8)
                }
                .accessibilityLabel(languageManager.localize("prayer_calendar_title"))
            }

            // 4 Circular Journey Badges Row
            HStack(spacing: 0) {
                JourneyBadgeView(
                    label: languageManager.localize("journey_badge_prayer"),
                    isCompleted: isPrayerDone,
                    onClick: {
                        UIImpactFeedbackGenerator(style: .medium).impactOccurred()
                        withAnimation(.spring(response: 0.35, dampingFraction: 0.7)) {
                            isPrayerDone.toggle()
                        }
                    }
                )
                .frame(maxWidth: .infinity)

                JourneyBadgeView(
                    label: languageManager.localize("journey_badge_quran"),
                    isCompleted: isQuranDone,
                    onClick: {
                        UIImpactFeedbackGenerator(style: .medium).impactOccurred()
                        withAnimation(.spring(response: 0.35, dampingFraction: 0.7)) {
                            isQuranDone.toggle()
                        }
                    }
                )
                .frame(maxWidth: .infinity)

                JourneyBadgeView(
                    label: languageManager.localize("journey_badge_dhikr"),
                    isCompleted: isDhikrDone,
                    onClick: {
                        UIImpactFeedbackGenerator(style: .medium).impactOccurred()
                        withAnimation(.spring(response: 0.35, dampingFraction: 0.7)) {
                            isDhikrDone.toggle()
                        }
                    }
                )
                .frame(maxWidth: .infinity)

                JourneyBadgeView(
                    label: languageManager.localize("journey_badge_sunnah"),
                    isCompleted: isSunnahDone,
                    onClick: {
                        UIImpactFeedbackGenerator(style: .medium).impactOccurred()
                        withAnimation(.spring(response: 0.35, dampingFraction: 0.7)) {
                            isSunnahDone.toggle()
                        }
                    }
                )
                .frame(maxWidth: .infinity)
            }

            // Inner Quran Quote Card
            HStack(alignment: .center, spacing: 12) {
                Image("mascot_reading")
                    .resizable()
                    .scaledToFit()
                    .frame(width: 80, height: 80)

                VStack(alignment: .leading, spacing: 6) {
                    Text("\"Dan sembahlah Tuhanmu sampai yakin (ajal) datang kepadamu.\"")
                        .font(.system(size: 13, weight: .semibold))
                        .foregroundColor(Color(hex: 0xFF1E_293B))
                        .lineSpacing(3)

                    Text("Qur'an 15:99")
                        .font(.system(size: 11.5, weight: .medium))
                        .foregroundColor(Color(hex: 0xFF64_748B))
                }
            }
            .padding(14)
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(Color(hex: 0xFFFF_FDF7))
            .cornerRadius(20)
            .overlay(
                RoundedRectangle(cornerRadius: 20)
                    .stroke(Color(hex: 0xFFF3_EDE2), lineWidth: 1)
            )
        }
        .padding(18)
        .background(SaatTokens.Colors.journeyCardBg)
        .cornerRadius(24)
        .overlay(
            RoundedRectangle(cornerRadius: 24, style: .continuous)
                .stroke(Color(hex: 0xFFF0_EBE1).opacity(0.6), lineWidth: 1)
        )
        .onAppear {
            isPrayerDone = viewModel.state.completedPrayers.count >= 5
        }
    }
}

// MARK: - Journey Badge
private struct JourneyBadgeView: View {
    let label: String
    let isCompleted: Bool
    let onClick: () -> Void

    var body: some View {
        Button(action: onClick) {
            VStack(spacing: 6) {
                ZStack {
                    Circle()
                        .fill(isCompleted ? Color(hex: 0xFFE6_F4EA) : Color(hex: 0xFFF7_F4E9))
                        .frame(width: 56, height: 56)
                        .overlay(
                            Circle()
                                .stroke(isCompleted ? Color(hex: 0xFFB8_E0C4) : Color(hex: 0xFFEC_E4D5), lineWidth: 1)
                        )

                    if isCompleted {
                        ZStack {
                            Circle()
                                .fill(SaatTokens.Colors.homeDarkGreen)
                                .frame(width: 24, height: 24)

                            Image(systemName: "checkmark")
                                .font(.system(size: 12, weight: .bold))
                                .foregroundColor(.white)
                        }
                    } else {
                        Circle()
                            .stroke(SaatTokens.Colors.homeDarkGreen.opacity(0.65), lineWidth: 2)
                            .frame(width: 18, height: 18)
                    }
                }

                Text(label)
                    .font(.system(size: 12, weight: isCompleted ? .bold : .semibold))
                    .foregroundColor(isCompleted ? SaatTokens.Colors.homeDarkGreen : Color(hex: 0xFF47_5569))
            }
            .padding(4)
        }
        .buttonStyle(.plain)
    }
}
