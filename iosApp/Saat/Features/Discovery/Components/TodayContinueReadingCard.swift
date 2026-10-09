//
//  TodayContinueReadingCard.swift
//  Saat
//
//  Created by Elmee on 25/04/2026.
//  Copyright © 2026 Elmee. All rights reserved.
//

import SwiftUI

struct TodayContinueReadingCard: View {
    let session: ReadingSession
    let chapterName: String?
    let totalVerses: Int? = 0
    let onTap: () -> Void
    @ObservedObject private var languageManager = AppLanguageManager.shared

    private var percentInt: Int {
        if let total = totalVerses, total > 0 {
            return max(1, min(100, Int((Double(session.verseNumber) / Double(total)) * 100)))
        }
        return 50
    }

    private var fillFraction: CGFloat {
        max(0.05, min(1.0, CGFloat(percentInt) / 100.0))
    }

    var body: some View {
        Button(action: onTap) {
            HStack(spacing: 12) {
                VStack(alignment: .leading, spacing: 4) {
                    Text(languageManager.localize("today_continue_reading_title"))
                        .font(.system(size: 14, weight: .semibold))
                        .foregroundColor(SaatTokens.Colors.homeDarkGreen)
                        .lineLimit(1)

                    let surahTitle = chapterName ?? String(format: languageManager.localize("surah_number"), session.chapterNumber)
                    let verseTitle = String(format: languageManager.localize("verse_number"), session.verseNumber)
                    Text("\(surahTitle) • \(verseTitle)")
                        .font(.system(size: 18, weight: .heavy))
                        .foregroundColor(SaatTokens.Colors.homeDarkGreen)
                        .lineLimit(1)

                    Spacer().frame(height: 4)

                    HStack(spacing: 8) {
                        Text("\(percentInt)%")
                            .font(.system(size: 11, weight: .bold))
                            .foregroundColor(SaatTokens.Colors.homeDarkGreen)

                        GeometryReader { geo in
                            ZStack(alignment: .leading) {
                                Capsule()
                                    .fill(Color(hex: 0xFFB9_CBBE))
                                    .frame(height: 4)

                                Capsule()
                                    .fill(SaatTokens.Colors.homeDarkGreen)
                                    .frame(width: max(8, geo.size.width * fillFraction), height: 4)
                            }
                        }
                        .frame(height: 4)
                        .frame(maxWidth: 160)
                    }
                }

                Spacer()

                Image("last_read")
                    .resizable()
                    .scaledToFit()
                    .frame(width: 90, height: 60)
            }
            .padding(.horizontal, 20)
            .padding(.vertical, 18)
            .background(SaatTokens.Colors.lastReadBg)
            .cornerRadius(24)
            .overlay(
                RoundedRectangle(cornerRadius: 24, style: .continuous)
                    .stroke(Color(hex: 0xFFE2_E8F0).opacity(0.5), lineWidth: 1)
            )
            .shadow(color: Color.black.opacity(0.03), radius: 6, x: 0, y: 2)
        }
        .buttonStyle(.plain)
    }
}
