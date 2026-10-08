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
    let onOpenCalendar: () -> Void

    private var targetPrayerName: String {
        viewModel.nextPrayerDisplayName.isEmpty ? "Dzuhur" : viewModel.nextPrayerDisplayName
    }

    private var targetPrayerIcon: String {
        let name = targetPrayerName.lowercased()
        if name.contains("fajr") || name.contains("subuh") {
            return "ic_onboarding_fajr"
        } else if name.contains("sunrise") || name.contains("terbit") {
            return "ic_onboarding_fajr"
        } else if name.contains("dhuhr") || name.contains("dzuhur") {
            return "ic_onboarding_dhuhr"
        } else if name.contains("asr") || name.contains("ashar") {
            return "ic_onboarding_asr"
        } else if name.contains("maghrib") {
            return "ic_onboarding_maghrib"
        } else {
            return "ic_onboarding_isha"
        }
    }

    private var prayerThemeColor: Color {
        let name = targetPrayerName.lowercased()
        if name.contains("fajr") || name.contains("subuh") {
            return Color(hex: 0xFF3B_82F6)
        } else if name.contains("sunrise") || name.contains("terbit") {
            return Color(hex: 0xFFF5_9E0B)
        } else if name.contains("dhuhr") || name.contains("dzuhur") {
            return Color(hex: 0xFFEA_B308)
        } else if name.contains("asr") || name.contains("ashar") {
            return Color(hex: 0xFFF9_7316)
        } else if name.contains("maghrib") {
            return Color(hex: 0xFFE1_1D48)
        } else {
            return Color(hex: 0xFF63_66F1)
        }
    }

    private var activeIndex: Int {
        let index = viewModel.mappedPrayers.firstIndex(where: { $0.isActive }) ?? 0
        return max(0, min(index, 5))
    }

    private var timelineProgress: CGFloat {
        let base = CGFloat(activeIndex)
        // Add small fractional progress towards next slot
        return min(5.0, base + 0.45)
    }

    var body: some View {
        Button(action: onOpenCalendar) {
            VStack(alignment: .leading, spacing: 16) {
                // Top Row: Next Prayer Name & Circular Arc Countdown
                HStack(alignment: .center) {
                    VStack(alignment: .leading, spacing: 4) {
                        Text("Selanjutnya")
                            .font(.system(size: 14, weight: .medium))
                            .foregroundColor(SaatTokens.Colors.slate500)

                        Text(targetPrayerName)
                            .font(.system(size: 28, weight: .heavy))
                            .foregroundColor(SaatTokens.Colors.slate900)
                            .lineLimit(1)
                    }

                    Spacer()

                    // Circular Arc Countdown Widget
                    PrayerArcCountdownView(
                        countdown: viewModel.countdownString.isEmpty ? "00:00:00" : viewModel.countdownString,
                        targetPrayerName: targetPrayerName,
                        prayerColor: prayerThemeColor,
                        iconName: targetPrayerIcon,
                        progress: 0.55
                    )
                }

                // Timeline & Prayer Slots
                VStack(spacing: 10) {
                    PrayerTimelineTrackView(
                        timelinePosition: timelineProgress,
                        activeIndex: activeIndex,
                        totalSlots: 6
                    )

                    HStack(spacing: 0) {
                        ForEach(Array(viewModel.mappedPrayers.prefix(6).enumerated()), id: \.element.id) { index, item in
                            SchedulePrayerSlotView(
                                name: item.displayName,
                                time: item.timeString,
                                isActive: item.isActive
                            )
                            .frame(maxWidth: .infinity)
                        }
                    }
                }
            }
            .padding(20)
            .background(SaatTokens.Colors.pureWhite)
            .cornerRadius(24)
            .overlay(
                RoundedRectangle(cornerRadius: 24, style: .continuous)
                    .stroke(Color(hex: 0xFFE2_E8F0).opacity(0.6), lineWidth: 1)
            )
            .shadow(color: Color.black.opacity(0.04), radius: 10, x: 0, y: 4)
        }
        .buttonStyle(.plain)
    }
}

// MARK: - Prayer Arc Countdown
private struct PrayerArcCountdownView: View {
    let countdown: String
    let targetPrayerName: String
    let prayerColor: Color
    let iconName: String
    let progress: CGFloat

    var body: some View {
        ZStack(alignment: .center) {
            // Arc canvas
            Canvas { context, size in
                let strokeWidth: CGFloat = 4.0
                let w = size.width
                let diameter = w - strokeWidth
                let radius = diameter / 2.0
                let center = CGPoint(x: w / 2.0, y: strokeWidth / 2.0 + radius)

                // Background Arch (180 deg, top half)
                var bgPath = Path()
                bgPath.addArc(
                    center: center,
                    radius: radius,
                    startAngle: .degrees(180),
                    endAngle: .degrees(360),
                    clockwise: false
                )
                context.stroke(
                    bgPath,
                    with: .color(prayerColor.opacity(0.14)),
                    style: StrokeStyle(lineWidth: strokeWidth, lineCap: .round)
                )

                // Active Arch
                let sweep = max(12.0, min(180.0, 180.0 * progress))
                var activePath = Path()
                activePath.addArc(
                    center: center,
                    radius: radius,
                    startAngle: .degrees(180),
                    endAngle: .degrees(180 + sweep),
                    clockwise: false
                )
                context.stroke(
                    activePath,
                    with: .color(prayerColor),
                    style: StrokeStyle(lineWidth: strokeWidth, lineCap: .round)
                )
            }
            .frame(width: 135, height: 80)

            // Icon overlay at tip of arc
            let sweepRad = Double(180.0 + max(12.0, min(180.0, 180.0 * progress))) * .pi / 180.0
            let radius: CGFloat = (135.0 - 4.0) / 2.0
            let cx: CGFloat = 135.0 / 2.0
            let cy: CGFloat = 4.0 / 2.0 + radius
            let endX = cx + radius * CGFloat(cos(sweepRad))
            let endY = cy + radius * CGFloat(sin(sweepRad))

            Image(iconName)
                .resizable()
                .scaledToFit()
                .frame(width: 22, height: 22)
                .position(x: endX, y: endY)

            // Center countdown text
            VStack(spacing: 2) {
                Text(countdown)
                    .font(.system(size: 20, weight: .bold, design: .rounded))
                    .foregroundColor(SaatTokens.Colors.slate900)
                    .monospacedDigit()

                Text("menuju \(targetPrayerName)")
                    .font(.system(size: 12, weight: .medium))
                    .foregroundColor(SaatTokens.Colors.slate500)
            }
            .padding(.top, 22)
        }
        .frame(width: 135, height: 80)
    }
}

// MARK: - Timeline Progress Track
private struct PrayerTimelineTrackView: View {
    let timelinePosition: CGFloat
    let activeIndex: Int
    let totalSlots: Int

    var body: some View {
        Canvas { context, size in
            let w = size.width
            let centerY = size.height / 2.0
            let slotWidth = w / CGFloat(totalSlots)
            let startX = slotWidth / 2.0
            let endX = w - (slotWidth / 2.0)

            // 1. Unfilled track line
            var trackPath = Path()
            trackPath.move(to: CGPoint(x: startX, y: centerY))
            trackPath.addLine(to: CGPoint(x: endX, y: centerY))
            context.stroke(
                trackPath,
                with: .color(Color(hex: 0xFFD1_D5DB)),
                style: StrokeStyle(lineWidth: 3, lineCap: .round)
            )

            // 2. Active dark green line
            let activeX = min(endX, max(startX, startX + (timelinePosition * slotWidth)))
            if timelinePosition > 0.02 {
                var activePath = Path()
                activePath.move(to: CGPoint(x: startX, y: centerY))
                activePath.addLine(to: CGPoint(x: activeX, y: centerY))
                context.stroke(
                    activePath,
                    with: .color(SaatTokens.Colors.homeDarkGreen),
                    style: StrokeStyle(lineWidth: 3, lineCap: .round)
                )

                // Glowing tip head
                let tipCenter = CGPoint(x: activeX, y: centerY)
                context.fill(
                    Path(ellipseIn: CGRect(x: tipCenter.x - 5, y: tipCenter.y - 5, width: 10, height: 10)),
                    with: .color(SaatTokens.Colors.homeDarkGreen.opacity(0.35))
                )
                context.fill(
                    Path(ellipseIn: CGRect(x: tipCenter.x - 3.5, y: tipCenter.y - 3.5, width: 7, height: 7)),
                    with: .color(SaatTokens.Colors.homeDarkGreen)
                )
                context.fill(
                    Path(ellipseIn: CGRect(x: tipCenter.x - 1.5, y: tipCenter.y - 1.5, width: 3, height: 3)),
                    with: .color(.white)
                )
            }

            // 3. Slot dots
            for i in 0..<totalSlots {
                let dotX = startX + CGFloat(i) * slotWidth
                let isPassed = CGFloat(i) <= timelinePosition + 0.05
                let dotColor = isPassed ? SaatTokens.Colors.homeDarkGreen : Color(hex: 0xFFCB_D5E1)
                let isCurrent = i == activeIndex
                let dotRadius: CGFloat = isCurrent ? 5.0 : 3.5
                let dotCenter = CGPoint(x: dotX, y: centerY)

                context.fill(
                    Path(ellipseIn: CGRect(x: dotCenter.x - dotRadius, y: dotCenter.y - dotRadius, width: dotRadius * 2, height: dotRadius * 2)),
                    with: .color(dotColor)
                )
                if isCurrent {
                    context.fill(
                        Path(ellipseIn: CGRect(x: dotCenter.x - 2, y: dotCenter.y - 2, width: 4, height: 4)),
                        with: .color(.white)
                    )
                }
            }
        }
        .frame(height: 14)
    }
}

// MARK: - Schedule Prayer Slot
private struct SchedulePrayerSlotView: View {
    let name: String
    let time: String
    let isActive: Bool

    var body: some View {
        VStack(spacing: 4) {
            Text(shortPrayerName(name))
                .font(.system(size: 11.5, weight: isActive ? .bold : .medium))
                .foregroundColor(SaatTokens.Colors.slate800)
                .lineLimit(1)

            Text(time)
                .font(.system(size: 11.5, weight: .bold))
                .foregroundColor(SaatTokens.Colors.slate900)
                .lineLimit(1)
        }
        .padding(.vertical, 8)
        .padding(.horizontal, 2)
        .frame(maxWidth: .infinity)
        .background(
            Group {
                if isActive {
                    Color(hex: 0xFFFC_FBF9)
                        .cornerRadius(12)
                }
            }
        )
        .overlay(
            Group {
                if isActive {
                    RoundedRectangle(cornerRadius: 12)
                        .stroke(
                            LinearGradient(
                                colors: [Color(hex: 0xFF14_5A43), Color(hex: 0xFFF4_EFE2)],
                                startPoint: .top,
                                endPoint: .bottom
                            ),
                            lineWidth: 1.5
                        )
                }
            }
        )
    }

    private func shortPrayerName(_ raw: String) -> String {
        let low = raw.lowercased()
        if low.contains("fajr") || low.contains("subuh") { return "Subuh" }
        if low.contains("sunrise") || low.contains("syuruq") || low.contains("terbit") { return "Terbit" }
        if low.contains("dhuhr") || low.contains("dzuhur") { return "Dzuhur" }
        if low.contains("asr") || low.contains("ashar") { return "Ashar" }
        if low.contains("maghrib") { return "Maghrib" }
        if low.contains("isha") || low.contains("isya") { return "Isya" }
        return raw
    }
}
