//
//  TodayDiscoveryHeaderView.swift
//  Saat
//
//  Created by Elmee on 25/04/2026.
//  Copyright © 2026 Elmee. All rights reserved.
//

import SwiftUI
import CoreLocation

struct TodayDiscoveryHeaderView: View {
    let hijriDate: String?
    let gregorianDate: String?
    let cityName: String?
    let locationStatus: String?
    let isScrolled: Bool
    let isDarkBackground: Bool
    let onLocationClick: () -> Void
    let onCalendarClick: () -> Void

    @State private var showHijri = false
    @State private var timer: Timer?
    @ObservedObject private var languageManager = AppLanguageManager.shared

    private var displayLocation: String {
        let text = cityName ?? locationStatus ?? languageManager.localize("discovering_location")
        var raw = text
        if text.contains(",") {
            let parts = text.split(separator: ",")
            if !(parts.count == 2 && Double(parts[0].trimmingCharacters(in: .whitespaces)) != nil) {
                raw = String(parts[0].trimmingCharacters(in: .whitespaces))
            }
        }
        return raw
            .replacingOccurrences(of: "Kecamatan", with: "Kec", options: .caseInsensitive)
            .replacingOccurrences(of: "Kelurahan", with: "Kel", options: .caseInsensitive)
            .replacingOccurrences(of: "Subdistrict", with: "Subdist", options: .caseInsensitive)
    }

    private var localDayName: String {
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: languageManager.currentLanguage.localeIdentifier)
        formatter.dateFormat = "EEEE"
        return formatter.string(from: Date())
    }

    private var formattedHijri: String? {
        guard let hijri = hijriDate, !hijri.trimmingCharacters(in: .whitespaces).isEmpty else { return nil }
        if hijri.hasSuffix(" H") || hijri.hasSuffix(" AH") {
            return hijri
        }
        return "\(hijri) H"
    }

    private var dateDisplayText: String {
        let prefix = localDayName.isEmpty ? "" : "\(localDayName), "
        if showHijri, let formattedHijri {
            return "\(prefix)\(formattedHijri)"
        } else if let gregorian = gregorianDate, !gregorian.isEmpty {
            return "\(prefix)\(gregorian)"
        } else {
            let formatter = DateFormatter()
            formatter.locale = Locale(identifier: languageManager.currentLanguage.localeIdentifier)
            formatter.dateFormat = "d MMM"
            return "\(prefix)\(formatter.string(from: Date()))"
        }
    }

    private var initialTextColor: Color {
        isDarkBackground ? .white : SaatTokens.Colors.homeDarkGreen
    }

    private var textColor: Color {
        isScrolled ? SaatTokens.Colors.homeDarkGreen : initialTextColor
    }

    private var badgeBg: Color {
        if isScrolled {
            return SaatTokens.Colors.homeDarkGreen.opacity(0.12)
        }
        return isDarkBackground ? Color.white.opacity(0.22) : SaatTokens.Colors.homeDarkGreen.opacity(0.10)
    }

    private var badgeBorder: Color {
        if isScrolled {
            return SaatTokens.Colors.homeDarkGreen.opacity(0.25)
        }
        return isDarkBackground ? Color.white.opacity(0.45) : SaatTokens.Colors.homeDarkGreen.opacity(0.25)
    }

    private var badgeContentColor: Color {
        if isScrolled {
            return SaatTokens.Colors.homeDarkGreen
        }
        return isDarkBackground ? .white : SaatTokens.Colors.homeDarkGreen
    }

    var body: some View {
        ZStack {
            if isScrolled {
                SaatTokens.Colors.homeBg
                    .opacity(0.96)
                    .ignoresSafeArea(edges: .top)
            }

            HStack(alignment: .center) {
                // Left: Greeting & Rotating Date
                VStack(alignment: .leading, spacing: 2) {
                    Text("Assalamu'alaikum")
                        .font(.system(size: 12, weight: .semibold))
                        .foregroundColor(textColor)
                        .lineLimit(1)

                    Button(action: onCalendarClick) {
                        Text(dateDisplayText)
                            .font(.system(size: 15, weight: .bold))
                            .foregroundColor(textColor)
                            .lineLimit(1)
                            .contentTransition(.numericText())
                    }
                    .buttonStyle(.plain)
                }

                Spacer()

                // Right: Location Badge Capsule
                Button(action: onLocationClick) {
                    HStack(spacing: 6) {
                        Image(systemName: "mappin.and.ellipse")
                            .font(.system(size: 12, weight: .bold))
                            .foregroundColor(badgeContentColor)

                        Text(displayLocation)
                            .font(.system(size: 13, weight: .bold))
                            .foregroundColor(badgeContentColor)
                            .lineLimit(1)
                            .frame(maxWidth: 140, alignment: .leading)
                    }
                    .padding(.horizontal, 14)
                    .padding(.vertical, 8)
                    .background(
                        Capsule()
                            .fill(badgeBg)
                    )
                    .overlay(
                        Capsule()
                            .stroke(badgeBorder, lineWidth: 1)
                    )
                }
                .buttonStyle(.plain)
            }
            .padding(.horizontal, 20)
            .padding(.vertical, 12)
        }
        .onAppear {
            timer?.invalidate()
            timer = Timer.scheduledTimer(withTimeInterval: 5.0, repeats: true) { _ in
                Task { @MainActor in
                    withAnimation(.easeInOut(duration: 0.35)) {
                        showHijri.toggle()
                    }
                }
            }
        }
        .onDisappear {
            timer?.invalidate()
            timer = nil
        }
    }
}
