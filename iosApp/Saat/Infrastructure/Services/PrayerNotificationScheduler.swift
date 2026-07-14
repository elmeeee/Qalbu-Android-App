//
//  PrayerNotificationScheduler.swift
//  Saat
//
//  Created by Elmee on 25/04/2026.
//  Copyright © 2026 Elmee. All rights reserved.
//

import Foundation
import OSLog
import UserNotifications
import AlarmKit
import SwiftUI
import CryptoKit
import ActivityKit

private let prayerNotifLog = Logger(subsystem: "co.kamy.Saat", category: "PrayerNotifications")

// MARK: - Notification Copy

private enum PrayerNotificationCopy {
    private static let timeFormatter: DateFormatter = {
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: "en_US_POSIX")
        formatter.dateFormat = "HH.mm"
        formatter.timeZone = .current
        return formatter
    }()

    static func title(for prayerName: String, at date: Date) -> String {
        let time = timeFormatter.string(from: date)
        return "It's time for \(prayerName) · \(time)"
    }

    static func alarmTitle(for prayerName: String, at date: Date) -> String {
        let time = timeFormatter.string(from: date)
        return "🕌 \(prayerName) · \(time)"
    }

    static func body(for prayerName: String) -> String {
        switch prayerName {
        case "Fajr":
            return "The world is still asleep. You don't have to be."
        case "Dhuhr":
            return "Pause. Pray. Then carry on."
        case "Asr":
            return "The angels are witnessing. Don't let this one pass."
        case "Maghrib":
            return "The sun just set. This one can't wait."
        case "Isha":
            return "End your day the right way."
        case "Imsak":
            return "Prepare for your fast. The dawn is near."
        default:
            return "It is now time for the \(prayerName) prayer."
        }
    }
}

// MARK: - Night Division

struct NightDivisionEntry: Sendable {
    enum Kind: String, CaseIterable, Sendable {
        case midnight = "Midnight"
        case firstThird = "Firstthird"
        case lastThird = "Lastthird"

        var aladhanKey: String { rawValue }

        var notificationTitle: String {
            switch self {
            case .midnight: return "🌙 Midnight"
            case .firstThird: return "🌃 The Night Begins"
            case .lastThird: return "✨ The Last Third Has Begun"
            }
        }

        var notificationBody: String {
            switch self {
            case .midnight:
                return "The night is halfway through. Pray Witr before you sleep - don't let it slip away."
            case .firstThird:
                return "Rest well. The last third of the night is yours — rise for what the day can't give you."
            case .lastThird:
                return "Allah descends to the lowest heaven. The most powerful hour of the day starts now."
            }
        }
    }

    let kind: Kind
    let date: Date
}

// MARK: - Scheduler

@MainActor
final class PrayerNotificationScheduler {
    private let notificationCenter = UNUserNotificationCenter.current()
    private let prayerPrefix = "Saat.prayer"
    private let nightPrefix = "Saat.night"

    /// Persisted alarm IDs so we can cancel previously scheduled alarms.
    private static let scheduledAlarmIDsKey = "Saat.scheduledAlarmIDs"

    private var lastTask: Task<Void, Never>? = nil
    private var currentTaskID: UUID = UUID()

    // MARK: - Authorization

    /// Requests both standard notification permission (for night divisions)
    /// and AlarmKit permission (for adzan prayer alarms).
    func requestAuthorizationIfNeeded() async -> Bool {
        let notifAuthorized = await requestNotificationAuth()
        let alarmAuthorized = await requestAlarmAuth()
        return notifAuthorized || alarmAuthorized
    }

    private func requestNotificationAuth() async -> Bool {
        let settings = await notificationCenter.notificationSettings()
        switch settings.authorizationStatus {
        case .authorized, .provisional, .ephemeral:
            return true
        case .denied:
            return false
        case .notDetermined:
            do {
                return try await notificationCenter.requestAuthorization(options: [.alert, .sound, .badge])
            } catch {
                prayerNotifLog.error("Failed requesting notification auth: \(error.localizedDescription, privacy: .public)")
                return false
            }
        @unknown default:
            return false
        }
    }

    private nonisolated func requestAlarmAuth() async -> Bool {
        let manager = AlarmManager.shared
        switch manager.authorizationState {
        case .notDetermined:
            do {
                let state = try await manager.requestAuthorization()
                return state == .authorized
            } catch {
                return false
            }
        case .authorized:
            return true
        default:
            return false
        }
    }

    // MARK: - Schedule

    func schedule(
        prayers: [PrayerEntry],
        imsakEntry: PrayerEntry?,
        nightDivisions: [NightDivisionEntry],
        options: PrayerNotificationPreferences.ScheduleOptions
    ) async {
        let myID = UUID()
        currentTaskID = myID
        
        let previousTask = lastTask
        let newTask = Task { @MainActor in
            _ = await previousTask?.result
            
            guard currentTaskID == myID else {
                return
            }
            
            await performSchedule(
                prayers: prayers,
                imsakEntry: imsakEntry,
                nightDivisions: nightDivisions,
                options: options
            )
        }
        lastTask = newTask
        await newTask.value
    }

    private func performSchedule(
        prayers: [PrayerEntry],
        imsakEntry: PrayerEntry?,
        nightDivisions: [NightDivisionEntry],
        options: PrayerNotificationPreferences.ScheduleOptions
    ) async {
        guard await requestAuthorizationIfNeeded() else {
            return
        }

        // --- Cancel previously scheduled alarms & notifications ---
        await cancelPreviousAlarms()
        await cancelPreviousNotifications()

        let now = Date()
        var calendar = Calendar(identifier: .gregorian)
        calendar.timeZone = TimeZone.current
        var scheduledAlarmIDs: [String] = []

        // --- Schedule prayer alarms via AlarmKit ---
        if options.adzanEnabled {
            for prayer in prayers {
                let enabledForThisPrayer: Bool = switch prayer.name {
                case "Fajr": options.fajrEnabled
                case "Dhuhr": options.dhuhrEnabled
                case "Asr": options.asrEnabled
                case "Maghrib": options.maghribEnabled
                case "Isha": options.ishaEnabled
                default: true
                }
                guard enabledForThisPrayer else { continue }

                for fireDate in Self.upcomingOccurrences(of: prayer.date, from: now, calendar: calendar) {
                    let alarmId = "\(prayerPrefix).\(prayer.name).\(Int(fireDate.timeIntervalSince1970))"
                    let success = await scheduleAlarm(
                        id: alarmId,
                        fireDate: fireDate,
                        prayerName: prayer.name
                    )
                    if success {
                        scheduledAlarmIDs.append(alarmId)
                    }
                }
            }
        }

        // --- Imsak alarm via AlarmKit ---
        if options.imsakEnabled, let imsak = imsakEntry {
            for fireDate in Self.upcomingOccurrences(of: imsak.date, from: now, calendar: calendar) {
                let alarmId = "\(prayerPrefix).Imsak.\(Int(fireDate.timeIntervalSince1970))"
                let success = await scheduleAlarm(
                    id: alarmId,
                    fireDate: fireDate,
                    prayerName: "Imsak"
                )
                if success {
                    scheduledAlarmIDs.append(alarmId)
                }
            }
        }

        // Persist scheduled alarm IDs for future cancellation
        UserDefaults.standard.set(scheduledAlarmIDs, forKey: Self.scheduledAlarmIDsKey)

        // --- Night divisions still use UNUserNotifications (less critical) ---
        for division in nightDivisions {
            let enabled: Bool = switch division.kind {
            case .midnight: options.midnightEnabled
            case .firstThird: options.firstThirdEnabled
            case .lastThird: options.lastThirdEnabled
            }
            guard enabled else { continue }

            for fireDate in Self.upcomingOccurrences(of: division.date, from: now, calendar: calendar) {
                let id = "\(nightPrefix).\(division.kind.rawValue).\(Int(fireDate.timeIntervalSince1970))"
                await addLocalNotification(
                    identifier: id,
                    fireDate: fireDate,
                    title: division.kind.notificationTitle,
                    body: division.kind.notificationBody
                )
            }
        }
    }

    // MARK: - AlarmKit Scheduling

    private func scheduleAlarm(
        id: String,
        fireDate: Date,
        prayerName: String
    ) async -> Bool {
        // Build the alarm presentation
        let titleString = PrayerNotificationCopy.alarmTitle(for: prayerName, at: fireDate)
        let alert = AlarmPresentation.Alert(
            title: LocalizedStringResource(stringLiteral: titleString),
            stopButton: AlarmButton(
                text: "Dismiss",
                textColor: .green,
                systemImageName: "checkmark.circle"
            )
        )

        let attributes = AlarmAttributes<EmptyAlarmMetadata>(
            presentation: AlarmPresentation(alert: alert),
            tintColor: .green
        )

        let soundName = UserDefaults.standard.string(forKey: "selected_adhan_sound") ?? "default"
        let alertSound: AlertConfiguration.AlertSound = soundName == "default" ? .default : .named("\(soundName).mp3")

        let configuration = AlarmManager.AlarmConfiguration(
            schedule: .fixed(fireDate),
            attributes: attributes,
            sound: alertSound
        )

        do {
            // Use a deterministic UUID from the string id for stable identity
            let alarmUUID = UUID(uuidString: stableUUID(from: id)) ?? UUID()
            _ = try await AlarmManager.shared.schedule(id: alarmUUID, configuration: configuration)
            prayerNotifLog.info("Scheduled AlarmKit alarm for \(prayerName, privacy: .public) at \(fireDate, privacy: .public)")
            return true
        } catch {
            prayerNotifLog.error("Failed scheduling AlarmKit alarm for \(prayerName, privacy: .public): \(error.localizedDescription, privacy: .public)")
            return false
        }
    }

    // MARK: - Cancellation

    private func cancelPreviousAlarms() async {
        guard let savedIDs = UserDefaults.standard.stringArray(forKey: Self.scheduledAlarmIDsKey) else {
            return
        }
        for idString in savedIDs {
            let uuid = UUID(uuidString: stableUUID(from: idString)) ?? UUID()
            do {
                try AlarmManager.shared.cancel(id: uuid)
            } catch {
                // Alarm may have already fired or been dismissed — not an error
                prayerNotifLog.debug("Could not cancel alarm \(idString, privacy: .public): \(error.localizedDescription, privacy: .public)")
            }
        }
        UserDefaults.standard.removeObject(forKey: Self.scheduledAlarmIDsKey)
    }

    private func cancelPreviousNotifications() async {
        let pending = await notificationCenter.pendingNotificationRequests()
        let toCancel = pending.map(\.identifier).filter {
            $0.hasPrefix(prayerPrefix) || $0.hasPrefix(nightPrefix)
        }
        if toCancel.isEmpty == false {
            notificationCenter.removePendingNotificationRequests(withIdentifiers: toCancel)
        }
    }

    // MARK: - UNNotification (Night Divisions)

    private func addLocalNotification(
        identifier: String,
        fireDate: Date,
        title: String,
        body: String
    ) async {
        let content = UNMutableNotificationContent()
        content.title = title
        content.body = body
        content.sound = .default

        var comps = Calendar.current.dateComponents(
            [.year, .month, .day, .hour, .minute],
            from: fireDate
        )
        comps.calendar = Calendar.current
        comps.timeZone = TimeZone.current
        comps.second = 0

        let trigger = UNCalendarNotificationTrigger(dateMatching: comps, repeats: false)
        let request = UNNotificationRequest(identifier: identifier, content: content, trigger: trigger)

        do {
            try await notificationCenter.add(request)
        } catch {
            prayerNotifLog.error("Failed scheduling \(identifier, privacy: .public): \(error.localizedDescription, privacy: .public)")
        }
    }

    // MARK: - Helpers

    private static func upcomingOccurrences(of date: Date, from now: Date, calendar: Calendar) -> [Date] {
        if date > now {
            return [date]
        }
        guard let tomorrow = calendar.date(byAdding: .day, value: 1, to: date), tomorrow > now else {
            return []
        }
        return [tomorrow]
    }

    /// Produces a deterministic UUID from an arbitrary identifier string.
    /// This ensures the same prayer+timestamp always maps to the same UUID for reliable cancellation.
    private func stableUUID(from string: String) -> String {
        let inputData = Data(string.utf8)
        let hashed = Insecure.MD5.hash(data: inputData)
        let bytes = Array(hashed)
        return NSUUID(uuidBytes: bytes).uuidString
    }
}
