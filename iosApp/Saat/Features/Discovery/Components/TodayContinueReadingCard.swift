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
                    RoundedRectangle(cornerRadius: 12)
                        .fill(Color.Token.teal.opacity(0.12))
                        .frame(width: 44, height: 44)
                    
                    Image(systemName: "bookmark.fill")
                        .font(.system(size: 22))
                        .foregroundColor(Color.Token.teal)
                }
                
                VStack(alignment: .leading, spacing: 2) {
                    Text(String(localized: "continue_reading", defaultValue: "CONTINUE READING").uppercased())
                        .font(.system(size: 11, weight: .bold))
                        .tracking(0.5)
                        .foregroundColor(Color.Token.teal)
                    
                    Text(chapterName ?? "Surah \(session.chapterNumber)")
                        .font(.system(size: 16, weight: .bold))
                        .foregroundColor(Color.Token.slate900)
                        .lineLimit(1)
                    
                    Text("Ayah \(session.verseNumber)")
                        .font(.system(size: 14))
                        .foregroundColor(Color.Token.slate500)
                }
                
                Spacer()
                
                Image(systemName: "arrow.right")
                    .font(.system(size: 20))
                    .foregroundColor(Color.Token.teal.opacity(0.7))
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 14)
            .background(
                LinearGradient(
                    colors: [
                        Color.Token.mintWash.opacity(0.5),
                        Color.white
                    ],
                    startPoint: .leading,
                    endPoint: .trailing
                )
            )
            .cornerRadius(18)
            .overlay(
                RoundedRectangle(cornerRadius: 18)
                    .stroke(Color.Token.teal.opacity(0.2), lineWidth: 1)
            )
            .shadow(color: Color.black.opacity(0.05), radius: 2, x: 0, y: 1)
        }
        .buttonStyle(.plain)
    }
}
