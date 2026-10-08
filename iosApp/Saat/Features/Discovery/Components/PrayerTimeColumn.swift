//
//  PrayerTimeColumn.swift
//  Saat
//
//  Created by Elmee on 25/04/2026.
//  Copyright © 2026 Elmee. All rights reserved.
//

import SwiftUI

struct PrayerTimeColumn: View {
    let name: String
    let time: String
    let isActive: Bool
    let theme: PrayerThematicTheme
    
    init(name: String, time: String, isActive: Bool, theme: PrayerThematicTheme) {
        self.name = name
        self.time = time
        self.isActive = isActive
        self.theme = theme
    }
    
    var body: some View {
        VStack(spacing: 4) {
            Text(name)
                .font(.system(size: 11, weight: isActive ? .bold : .medium))
                .foregroundColor(isActive ? SaatTokens.Colors.deepEmerald : .white.opacity(0.75))
                .lineLimit(1)
                .fixedSize(horizontal: true, vertical: false)
            
            Text(time)
                .font(.system(size: 13, weight: isActive ? .bold : .semibold))
                .foregroundColor(isActive ? SaatTokens.Colors.deepEmerald : .white)
                .lineLimit(1)
                .fixedSize(horizontal: true, vertical: false)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 8)
        .padding(.horizontal, 4)
        .background(
            Group {
                if isActive {
                    RoundedRectangle(cornerRadius: 14, style: .continuous)
                        .fill(Color.white)
                        .shadow(color: Color.black.opacity(0.12), radius: 6, y: 3)
                } else {
                    Color.clear
                }
            }
        )
        .scaleEffect(isActive ? 1.02 : 1.0)
        .animation(.spring(response: 0.35, dampingFraction: 0.7, blendDuration: 0), value: isActive)
        .onChange(of: isActive) { oldValue, newValue in
            if newValue {
                let generator = UIImpactFeedbackGenerator(style: .medium)
                generator.prepare()
                generator.impactOccurred()
            }
        }
    }
}
