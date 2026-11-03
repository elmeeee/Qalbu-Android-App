//
//  PrayerCountdownLiveActivityManager.swift
//  Saat
//
//  Created by Sufiandy Elmy on 09/10/2026.
//  Copyright © 2026 Elmee. All rights reserved.
//

import ActivityKit
import Foundation
import OSLog

/// Manages the lifecycle of the Live Prayer Countdown Activity on iOS.
@MainActor
final class PrayerCountdownLiveActivityManager {
    static let shared = PrayerCountdownLiveActivityManager()
    static let enabledKey = "live_prayer_countdown_enabled"

    private let logger = Logger(subsystem: "co.kamy.Saat", category: "PrayerCountdownLiveActivity")
    private var currentActivity: Activity<PrayerCountdownAttributes>?

    private init() {}

    func setEnabled(_ enabled: Bool) async {
        UserDefaults.standard.set(enabled, forKey: Self.enabledKey)
        if !enabled {
            await endActivity()
        }
    }

    /// Starts or updates the Live Activity for prayer countdown.
    func startOrUpdateActivity(
        nextPrayerName: String,
        nextPrayerTimeFormatted: String,
        targetDate: Date,
        currentPrayerName: String,
        locationName: String? = nil,
        calculationMethod: String = "Saat"
    ) async {
        if UserDefaults.standard.object(forKey: Self.enabledKey) == nil {
            UserDefaults.standard.set(true, forKey: Self.enabledKey)
        }

        guard UserDefaults.standard.bool(forKey: Self.enabledKey) else {
            await endActivity()
            return
        }

        guard ActivityAuthorizationInfo().areActivitiesEnabled else { return }

        let state = PrayerCountdownAttributes.ContentState(
            nextPrayerName: nextPrayerName,
            nextPrayerTimeFormatted: nextPrayerTimeFormatted,
            targetDate: targetDate,
            currentPrayerName: currentPrayerName,
            isApproaching: targetDate.timeIntervalSinceNow < 1800,
            locationName: locationName
        )
        let content = ActivityContent(state: state, staleDate: targetDate.addingTimeInterval(900))

        if let activity = currentActivity ?? Activity<PrayerCountdownAttributes>.activities.first {
            currentActivity = activity
            guard activity.content.state != state else { return }
            let activityID = activity.id
            let staleDate = targetDate.addingTimeInterval(900)
            Task.detached {
                guard let activity = Activity<PrayerCountdownAttributes>.activities.first(where: { $0.id == activityID }) else {
                    return
                }
                await activity.update(ActivityContent(state: state, staleDate: staleDate))
            }
        } else {
            let attributes = PrayerCountdownAttributes(calculationMethod: calculationMethod)
            do {
                currentActivity = try Activity.request(
                    attributes: attributes,
                    content: content,
                    pushType: nil
                )
            } catch {
                currentActivity = nil
                logger.error("Failed to start prayer Live Activity: \(error.localizedDescription, privacy: .public)")
            }
        }
    }

    /// End the Live Activity.
    func endActivity() async {
        let activityIDs = Activity<PrayerCountdownAttributes>.activities.map(\.id)
        currentActivity = nil
        await Task.detached {
            for activityID in activityIDs {
                guard let activity = Activity<PrayerCountdownAttributes>.activities.first(where: { $0.id == activityID }) else {
                    continue
                }
                await activity.end(nil, dismissalPolicy: .immediate)
            }
        }.value
    }
}
