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
import SwiftUI

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

    private var lastTask: Task<Void, Never>? = nil
    private var currentTaskID: UUID = UUID()

    // MARK: - Authorization

    func requestAuthorizationIfNeeded() async -> Bool {
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

        // Cancel previously scheduled notifications
        await cancelPreviousNotifications()

        let now = Date()
        var calendar = Calendar(identifier: .gregorian)
        calendar.timeZone = TimeZone.current

        // Schedule prayer notifications
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

                let soundName: String
                if prayer.name.lowercased() == "fajr" {
                    soundName = UserDefaults.standard.string(forKey: "selected_adhan_fajr_sound") ?? "adhan_fajr_ust_bilal_attaki"
                } else {
                    soundName = UserDefaults.standard.string(forKey: "selected_adhan_sound") ?? "adhan_ust_daeng_syawal_indonesia"
                }

                for fireDate in Self.upcomingOccurrences(of: prayer.date, from: now, calendar: calendar) {
                    let notifId = "\(prayerPrefix).\(prayer.name).\(Int(fireDate.timeIntervalSince1970))"
                    await addLocalNotification(
                        identifier: notifId,
                        fireDate: fireDate,
                        title: PrayerNotificationCopy.title(for: prayer.name, at: fireDate),
                        body: PrayerNotificationCopy.body(for: prayer.name),
                        soundName: soundName
                    )
                }
            }
        }

        // Imsak notification
        if options.imsakEnabled, let imsak = imsakEntry {
            for fireDate in Self.upcomingOccurrences(of: imsak.date, from: now, calendar: calendar) {
                let notifId = "\(prayerPrefix).Imsak.\(Int(fireDate.timeIntervalSince1970))"
                await addLocalNotification(
                    identifier: notifId,
                    fireDate: fireDate,
                    title: PrayerNotificationCopy.title(for: "Imsak", at: fireDate),
                    body: PrayerNotificationCopy.body(for: "Imsak"),
                    soundName: "default"
                )
            }
        }

        // Night divisions
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
                    body: division.kind.notificationBody,
                    soundName: "default"
                )
            }
        }
    }

    // MARK: - Cancellation

    private func cancelPreviousNotifications() async {
        let pending = await notificationCenter.pendingNotificationRequests()
        let toCancel = pending.map(\.identifier).filter {
            $0.hasPrefix(prayerPrefix) || $0.hasPrefix(nightPrefix)
        }
        if toCancel.isEmpty == false {
            notificationCenter.removePendingNotificationRequests(withIdentifiers: toCancel)
        }
    }

    // MARK: - UNNotification

    private func addLocalNotification(
        identifier: String,
        fireDate: Date,
        title: String,
        body: String,
        soundName: String = "default"
    ) async {
        let content = UNMutableNotificationContent()
        content.title = title
        content.body = body
        if soundName == "default" {
            content.sound = .default
        } else {
            content.sound = UNNotificationSound(named: UNNotificationSoundName(rawValue: "\(soundName).mp3"))
        }

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
            prayerNotifLog.info("Scheduled notification for \(identifier, privacy: .public) at \(fireDate, privacy: .public)")
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
}
