//
//  Models.swift
//  AlKhatibLiveActivity
//
//  Created by Sufiandy Elmy on 09/10/2026.
//  Copyright © 2026 Elmee. All rights reserved.
//

import ActivityKit
import Foundation
import SwiftUI

public struct PrayerCountdownAttributes: ActivityAttributes {
    public let calculationMethod: String

    public init(calculationMethod: String = "Saat") {
        self.calculationMethod = calculationMethod
    }

    public struct ContentState: Codable, Hashable {
        public let nextPrayerName: String
        public let nextPrayerTimeFormatted: String
        public let targetDate: Date
        public let currentPrayerName: String
        public let isApproaching: Bool
        public let locationName: String?

        public init(
            nextPrayerName: String,
            nextPrayerTimeFormatted: String,
            targetDate: Date,
            currentPrayerName: String,
            isApproaching: Bool = false,
            locationName: String? = nil
        ) {
            self.nextPrayerName = nextPrayerName
            self.nextPrayerTimeFormatted = nextPrayerTimeFormatted
            self.targetDate = targetDate
            self.currentPrayerName = currentPrayerName
            self.isApproaching = isApproaching
            self.locationName = locationName
        }
    }
}

public struct QuranPlaybackAttributes: ActivityAttributes {
    public let surahName: String
    public let reciterName: String

    public init(surahName: String, reciterName: String) {
        self.surahName = surahName
        self.reciterName = reciterName
    }

    public struct ContentState: Codable, Hashable {
        public let verseLabel: String
        public let isPlaying: Bool
        public let progress: Double
        public let currentVerse: Int
        public let totalVerses: Int

        public init(
            verseLabel: String,
            isPlaying: Bool,
            progress: Double,
            currentVerse: Int,
            totalVerses: Int
        ) {
            self.verseLabel = verseLabel
            self.isPlaying = isPlaying
            self.progress = progress
            self.currentVerse = currentVerse
            self.totalVerses = totalVerses
        }
    }
}

extension Color {
    init(hex: UInt, alpha: Double = 1.0) {
        let red = Double((hex >> 16) & 0xFF) / 255.0
        let green = Double((hex >> 8) & 0xFF) / 255.0
        let blue = Double(hex & 0xFF) / 255.0
        self.init(.sRGB, red: red, green: green, blue: blue, opacity: alpha)
    }
}
