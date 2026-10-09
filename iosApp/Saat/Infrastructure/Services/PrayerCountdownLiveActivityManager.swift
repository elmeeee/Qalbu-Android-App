//
//  PrayerCountdownLiveActivityManager.swift
//  Saat
//
//  Created by Sufiandy Elmy on 09/10/2026.
//  Copyright © 2026 Elmee. All rights reserved.
//

import ActivityKit
import Foundation

/// Manages the lifecycle of the Live Prayer Countdown Activity on iOS.
@MainActor
final class PrayerCountdownLiveActivityManager {
    static let shared = PrayerCountdownLiveActivityManager()

    private var currentActivity: Activity<PrayerCountdownAttributes>?

    private init() {}

    /// Starts or updates the Live Activity for prayer countdown.
    func startOrUpdateActivity(
        nextPrayerName: String,
        nextPrayerTimeFormatted: String,
        targetDate: Date,
        currentPrayerName: String,
        locationName: String? = nil,
        calculationMethod: String = "Saat"
    ) {
        let isEnabled = UserDefaults.standard.bool(forKey: "liveCountdownEnabled")
        // Default to true if not explicitly set
        if UserDefaults.standard.object(forKey: "liveCountdownEnabled") == nil {
            UserDefaults.standard.set(true, forKey: "liveCountdownEnabled")
        }

        guard UserDefaults.standard.bool(forKey: "liveCountdownEnabled") else {
            endActivity()
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

        // If activity already exists, update it
        if let activity = currentActivity ?? Activity<PrayerCountdownAttributes>.activities.first {
            currentActivity = activity
            let activityID = activity.id
            Task.detached {
                if let target = Activity<PrayerCountdownAttributes>.activities.first(where: { $0.id == activityID }) {
                    await target.update(content)
                }
            }
        } else {
            // Request a new Live Activity
            let attributes = PrayerCountdownAttributes(calculationMethod: calculationMethod)
            do {
                currentActivity = try Activity.request(
                    attributes: attributes,
                    content: content,
                    pushType: nil
                )
            } catch {
                // Silently degrade if not supported or disabled in system settings
            }
        }
    }

    /// End the Live Activity.
    func endActivity() {
        let activities = Activity<PrayerCountdownAttributes>.activities
        currentActivity = nil
        Task.detached {
            for act in activities {
                await act.end(nil, dismissalPolicy: .immediate)
            }
        }
    }
}
