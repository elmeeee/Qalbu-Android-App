//
//  PrayerCountdownLiveActivity.swift
//  SaatLiveActivity
//
//  Created by Sufiandy Elmy on 09/10/2026.
//  Copyright © 2026 Elmee. All rights reserved.
//

import ActivityKit
import SwiftUI
import WidgetKit

/// Live Activity widget for real-time Live Prayer Countdown.
/// Renders on the Dynamic Island (compact, expanded, minimal) and Lock Screen.
struct PrayerCountdownLiveActivity: Widget {
    var body: some WidgetConfiguration {
        ActivityConfiguration(for: PrayerCountdownAttributes.self) { context in
            // Lock Screen / StandBy banner
            lockScreenBanner(context: context)
        } dynamicIsland: { context in
            DynamicIsland {
                // Expanded presentation
                DynamicIslandExpandedRegion(.leading) {
                    HStack(spacing: 8) {
                        Image(systemName: "moon.stars.fill")
                            .font(.system(size: 16, weight: .bold))
                            .foregroundStyle(Color(hex: 0xFF14_5A43))
                        
                        VStack(alignment: .leading, spacing: 2) {
                            Text(context.state.currentPrayerName)
                                .font(.system(size: 11, weight: .medium))
                                .foregroundStyle(.white.opacity(0.6))
                            
                            Text(context.state.nextPrayerName)
                                .font(.system(size: 15, weight: .heavy))
                                .foregroundStyle(.white)
                        }
                    }
                }

                DynamicIslandExpandedRegion(.center) {
                    VStack(spacing: 2) {
                        Text(timerInterval: Date()...max(Date(), context.state.targetDate), countsDown: true)
                            .font(.system(size: 18, weight: .heavy, design: .rounded))
                            .monospacedDigit()
                            .foregroundStyle(Color(hex: 0xFF34_D399))
                        
                        Text(context.state.nextPrayerTimeFormatted)
                            .font(.system(size: 11, weight: .bold))
                            .foregroundStyle(.white.opacity(0.75))
                    }
                }

                DynamicIslandExpandedRegion(.trailing) {
                    VStack(alignment: .trailing, spacing: 2) {
                        if let loc = context.state.locationName, !loc.isEmpty {
                            Text(loc)
                                .font(.system(size: 10, weight: .semibold))
                                .foregroundStyle(.white.opacity(0.6))
                                .lineLimit(1)
                        }
                        
                        Text("Sāat")
                            .font(.system(size: 12, weight: .bold))
                            .foregroundStyle(Color(hex: 0xFF34_D399))
                    }
                }

                DynamicIslandExpandedRegion(.bottom) {
                    HStack {
                        Text("Menuju waktu shalat")
                            .font(.system(size: 11, weight: .medium))
                            .foregroundStyle(.white.opacity(0.5))
                        Spacer()
                        Text("Pukul \(context.state.nextPrayerTimeFormatted)")
                            .font(.system(size: 11, weight: .semibold))
                            .foregroundStyle(.white.opacity(0.85))
                    }
                    .padding(.top, 4)
                }
            } compactLeading: {
                HStack(spacing: 4) {
                    Image(systemName: "moon.stars.fill")
                        .font(.system(size: 11, weight: .bold))
                        .foregroundStyle(Color(hex: 0xFF34_D399))
                    Text(context.state.nextPrayerName)
                        .font(.system(size: 12, weight: .bold))
                        .foregroundStyle(.white)
                }
            } compactTrailing: {
                Text(timerInterval: Date()...max(Date(), context.state.targetDate), countsDown: true)
                    .font(.system(size: 12, weight: .heavy, design: .rounded))
                    .monospacedDigit()
                    .foregroundStyle(Color(hex: 0xFF34_D399))
                    .frame(width: 48)
            } minimal: {
                Image(systemName: "moon.stars.fill")
                    .font(.system(size: 11, weight: .bold))
                    .foregroundStyle(Color(hex: 0xFF34_D399))
            }
        }
    }

    // MARK: – Lock Screen Banner

    @ViewBuilder
    private func lockScreenBanner(context: ActivityViewContext<PrayerCountdownAttributes>) -> some View {
        HStack(alignment: .center, spacing: 14) {
            // Icon Pill
            ZStack {
                Circle()
                    .fill(Color(hex: 0xFF14_5A43))
                    .frame(width: 46, height: 46)
                
                Image(systemName: "moon.stars.fill")
                    .font(.system(size: 20, weight: .semibold))
                    .foregroundColor(Color(hex: 0xFFF4_EFE2))
            }

            // Center details
            VStack(alignment: .leading, spacing: 3) {
                HStack(spacing: 6) {
                    Text("SĀAT")
                        .font(.system(size: 10, weight: .heavy))
                        .foregroundColor(Color(hex: 0xFF14_5A43))
                    
                    if let loc = context.state.locationName, !loc.isEmpty {
                        Text("•  \(loc)")
                            .font(.system(size: 10, weight: .medium))
                            .foregroundColor(Color(hex: 0xFF64_748B))
                    }
                }

                Text(context.state.nextPrayerName)
                    .font(.system(size: 19, weight: .heavy))
                    .foregroundColor(Color(hex: 0xFF0F_172A))
                    .lineLimit(1)

                Text("Pukul \(context.state.nextPrayerTimeFormatted)")
                    .font(.system(size: 12, weight: .medium))
                    .foregroundColor(Color(hex: 0xFF47_5569))
            }

            Spacer()

            // Countdown timer on right
            VStack(alignment: .trailing, spacing: 2) {
                Text(timerInterval: Date()...max(Date(), context.state.targetDate), countsDown: true)
                    .font(.system(size: 17, weight: .heavy, design: .rounded))
                    .monospacedDigit()
                    .foregroundColor(Color(hex: 0xFF14_5A43))
                
                Text("menuju shalat")
                    .font(.system(size: 10, weight: .medium))
                    .foregroundColor(Color(hex: 0xFF64_748B))
            }
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 12)
        .background(
            RoundedRectangle(cornerRadius: 18, style: .continuous)
                .fill(Color(hex: 0xFFFC_FBF7))
                .overlay(
                    RoundedRectangle(cornerRadius: 18, style: .continuous)
                        .stroke(Color(hex: 0xFFECE7DE), lineWidth: 1)
                )
        )
    }
}
