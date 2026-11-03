//
//  SaatApp.swift
//  Sāat
//
//  Created by Elmee on 25/04/2026.
//  Copyright © 2026 Elmee. All rights reserved.
//

import SwiftUI
import UserNotifications

@main
struct SaatApp: App {
    private let appContainer: AppContainer

    init() {
        UNUserNotificationCenter.current().delegate = PrayerNotificationCenterDelegate.shared
        self.appContainer = AppContainer()
        _ = SaatTypography.verseArabicHTMLBaseDirectory()
        Task { @MainActor in
            _ = SaatTypography.quranArabicUIFont(size: 24)
            if UserDefaults.standard.bool(forKey: "hasCompletedOnboarding"), DailyVerseNotificationPreferences.isEnabled() {
                _ = await DailyVerseNotificationScheduler().requestAuthorizationIfNeeded()
            }
        }
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
                .environment(\.appContainer, appContainer)
                .onOpenURL { url in
                    Task { await appContainer.oauth.handleIncomingCallback(url) }
                }
        }
    }
}
