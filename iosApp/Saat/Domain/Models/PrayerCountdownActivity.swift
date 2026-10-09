//
//  PrayerCountdownActivity.swift
//  Saat
//
//  Created by Sufiandy Elmy on 09/10/2026.
//  Copyright © 2026 Elmee. All rights reserved.
//

import ActivityKit
import Foundation

/// ActivityAttributes describing Live Prayer Countdown on Lock Screen & Dynamic Island.
/// Shared between the main app and widget extension.
public struct PrayerCountdownAttributes: ActivityAttributes {
    /// Static context that does not change during the activity's lifetime.
    public let calculationMethod: String

    public init(calculationMethod: String = "Saat") {
        self.calculationMethod = calculationMethod
    }

    /// Dynamic state that updates as prayer intervals progress.
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
