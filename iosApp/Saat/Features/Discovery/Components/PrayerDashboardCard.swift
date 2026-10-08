//
//  PrayerDashboardCard.swift
//  Saat
//
//  Created by Elmee on 25/04/2026.
//  Copyright © 2026 Elmee. All rights reserved.
//

import SwiftUI

struct PrayerDashboardCard: View {
    @ObservedObject var viewModel: PrayerDashboardViewModel
    @ObservedObject private var languageManager = AppLanguageManager.shared
    
    init(viewModel: PrayerDashboardViewModel) {
        self.viewModel = viewModel
    }
    
    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            if viewModel.mappedPrayers.isEmpty || viewModel.isLoading {
                PrayerDashboardSkeleton()
            } else {
                activeCardLayout
            }
        }
        .background(
            RoundedRectangle(cornerRadius: 24, style: .continuous)
                .fill(SaatTokens.Colors.primaryGradient)
        )
        .overlay(
            RoundedRectangle(cornerRadius: 24, style: .continuous)
                .stroke(Color.white.opacity(0.15), lineWidth: 1)
        )
        .shadow(color: SaatTokens.Colors.deepEmerald.opacity(0.16), radius: 15, x: 0, y: 8)
        .padding(.horizontal, TodayDiscoveryLayout.horizontalInset)
        .animation(.spring(response: 0.5, dampingFraction: 0.8, blendDuration: 0), value: viewModel.activeTheme)
        .accessibilityElement(children: .ignore)
        .accessibilityLabel(prayerSpokenSummary)
        .accessibilityHint("Prayer schedule for Muslims. Countdown updates automatically.")
    }

    private var prayerSpokenSummary: String {
        guard viewModel.mappedPrayers.isEmpty == false, viewModel.isLoading == false else {
            return "Loading prayer times"
        }
        let city = viewModel.cityName ?? ""
        return "Next prayer \(viewModel.nextPrayerDisplayName) in \(viewModel.countdownString). Location \(city)."
    }
    
    @ViewBuilder
    private var activeCardLayout: some View {
        VStack(alignment: .leading, spacing: 0) {
            HStack(alignment: .center) {
                VStack(alignment: .leading, spacing: 4) {
                    HStack(spacing: 5) {
                        Image(systemName: "clock.fill")
                            .font(.system(size: 11))
                            .foregroundColor(.white.opacity(0.8))
                        Text(languageManager.localize("next_prayer"))
                            .font(.system(size: 12, weight: .medium))
                            .foregroundColor(.white.opacity(0.8))
                    }
                    
                    Text(viewModel.nextPrayerDisplayName)
                        .font(.system(size: 26, weight: .bold))
                        .foregroundColor(.white)
                        .lineLimit(1)
                    
                    Text("\(viewModel.nextPrayerDisplayName) · \(viewModel.nextPrayerTime)")
                        .font(.system(size: 13, weight: .medium))
                        .foregroundColor(.white.opacity(0.85))
                }
                
                Spacer()
                
                VStack(spacing: 2) {
                    Text(viewModel.countdownString)
                        .font(.system(size: 20, weight: .bold, design: .rounded))
                        .foregroundColor(.white)
                        .monospacedDigit()
                        .contentTransition(.numericText())
                }
                .padding(.horizontal, 14)
                .padding(.vertical, 8)
                .background(
                    Capsule()
                        .fill(Color.black.opacity(0.22))
                        .overlay(
                            Capsule().stroke(Color.white.opacity(0.15), lineWidth: 0.5)
                        )
                )
            }
            .padding(.horizontal, 20)
            .padding(.top, 20)
            .padding(.bottom, 16)
            
            Rectangle()
                .fill(Color.white.opacity(0.12))
                .frame(height: 1)
                .padding(.horizontal, 16)
                .padding(.bottom, 14)
            
            HStack(spacing: 0) {
                ForEach(viewModel.mappedPrayers) { item in
                    PrayerTimeColumn(
                        name: item.displayName,
                        time: item.timeString,
                        isActive: item.isActive,
                        theme: viewModel.activeTheme
                    )
                }
            }
            .padding(.horizontal, 6)
            .padding(.vertical, 8)
            .background(
                RoundedRectangle(cornerRadius: 18, style: .continuous)
                    .fill(Color.black.opacity(0.18))
            )
            .padding(.horizontal, 16)
            .padding(.bottom, 16)
        }
    }
    
}

