//
//  PrayerNotificationPreferences.swift
//  Sāat
//
//  Created by Elmee on 25/04/2026.
//  Copyright © 2026 Elmee. All rights reserved.
//

import Foundation

enum PrayerNotificationPreferences {
    static let adzanKey = "adzanNotificationsEnabled"
    static let imsakKey = "imsakNotificationsEnabled"
    static let midnightKey = "midnightNotificationsEnabled"
    static let firstThirdKey = "firstThirdNotificationsEnabled"
    static let tahajudKey = "tahajudNotificationsEnabled"
    static let fajrKey = "fajrNotificationsEnabled"
    static let dhuhrKey = "dhuhrNotificationsEnabled"
    static let asrKey = "asrNotificationsEnabled"
    static let maghribKey = "maghribNotificationsEnabled"
    static let ishaKey = "ishaNotificationsEnabled"

    static let didChangeNotification = Notification.Name("prayerNotificationPreferencesDidChange")

    struct ScheduleOptions: Sendable {
        var adzanEnabled: Bool
        var imsakEnabled: Bool
        var midnightEnabled: Bool
        var firstThirdEnabled: Bool
        var lastThirdEnabled: Bool
        var fajrEnabled: Bool
        var dhuhrEnabled: Bool
        var asrEnabled: Bool
        var maghribEnabled: Bool
        var ishaEnabled: Bool
    }

    static func scheduleOptions(defaults: UserDefaults = .standard) -> ScheduleOptions {
        ScheduleOptions(
            adzanEnabled: bool(forKey: adzanKey, default: true, defaults: defaults),
            imsakEnabled: bool(forKey: imsakKey, default: true, defaults: defaults),
            midnightEnabled: bool(forKey: midnightKey, default: true, defaults: defaults),
            firstThirdEnabled: bool(forKey: firstThirdKey, default: true, defaults: defaults),
            lastThirdEnabled: bool(forKey: tahajudKey, default: true, defaults: defaults),
            fajrEnabled: bool(forKey: fajrKey, default: true, defaults: defaults),
            dhuhrEnabled: bool(forKey: dhuhrKey, default: true, defaults: defaults),
            asrEnabled: bool(forKey: asrKey, default: true, defaults: defaults),
            maghribEnabled: bool(forKey: maghribKey, default: true, defaults: defaults),
            ishaEnabled: bool(forKey: ishaKey, default: true, defaults: defaults)
        )
    }

    static func notifyDidChange() {
        NotificationCenter.default.post(name: didChangeNotification, object: nil)
    }

    private static func bool(forKey key: String, default defaultValue: Bool, defaults: UserDefaults) -> Bool {
        guard defaults.object(forKey: key) != nil else { return defaultValue }
        return defaults.bool(forKey: key)
    }
}
