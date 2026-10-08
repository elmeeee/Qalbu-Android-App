//
//  TodayContinueReadingCard.swift
//  Saat
//

import SwiftUI

struct TodayContinueReadingCard: View {
    let session: ReadingSession
    let chapterName: String?
    let onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            HStack(spacing: 14) {
                ZStack {
                    RoundedRectangle(cornerRadius: 14, style: .continuous)
                        .fill(SaatTokens.Colors.sageTint)
                        .frame(width: 48, height: 48)
                    
                    Image(systemName: "bookmark.fill")
                        .font(.system(size: 20, weight: .semibold))
                        .foregroundColor(SaatTokens.Colors.deepEmerald)
                }
                
                VStack(alignment: .leading, spacing: 3) {
                    Text(String(localized: "continue_reading", defaultValue: "TERAKHIR DIBACA").uppercased())
                        .font(.system(size: 11, weight: .bold))
                        .tracking(0.5)
                        .foregroundColor(SaatTokens.Colors.teal)
                    
                    Text(chapterName ?? "Surah \(session.chapterNumber)")
                        .font(.system(size: 16, weight: .bold))
                        .foregroundColor(SaatTokens.Colors.slate900)
                        .lineLimit(1)
                    
                    Text("Ayat \(session.verseNumber)")
                        .font(.system(size: 13, weight: .medium))
                        .foregroundColor(SaatTokens.Colors.slate500)
                }
                
                Spacer()
                
                Image(systemName: "arrow.right.circle.fill")
                    .font(.system(size: 24))
                    .foregroundColor(SaatTokens.Colors.deepEmerald)
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 14)
            .background(SaatTokens.Colors.pureWhite)
            .cornerRadius(20)
            .overlay(
                RoundedRectangle(cornerRadius: 20, style: .continuous)
                    .stroke(SaatTokens.Colors.softGrey.opacity(0.8), lineWidth: 1)
            )
            .shadow(color: Color.black.opacity(0.04), radius: 8, x: 0, y: 3)
        }
        .buttonStyle(.plain)
    }
}
