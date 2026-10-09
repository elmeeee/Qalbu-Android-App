//
//  RootTabView.swift
//  Sāat
//
//  Created by Sufiandy Elmy on 09/10/2026.
//  Copyright © 2026 Elmee. All rights reserved.
//

import SwiftUI

struct RootTabView: View {
    enum Tab: Int, Hashable {
        case today = 0
        case journey = 1
        case tools = 2
        case account = 3
    }

    @Environment(\.scenePhase) private var scenePhase
    @Environment(\.appContainer) private var container
    @State private var selectedTab: Tab = .today
    @State private var todayNavigationPath: [TodayNavigation] = []
    @State private var vm = RootTabViewModel()
    @ObservedObject private var languageManager = AppLanguageManager.shared
    @StateObject private var prayerController = PrayerTimesController()
    let verseState: TodayVerseState

    enum TodayNavigation: Hashable {
        case account
    }

    init(verseState: TodayVerseState) {
        self.verseState = verseState
    }

    var body: some View {
        TabView(selection: $selectedTab) {
            // Tab 1: Beranda
            NavigationStack(path: $todayNavigationPath) {
                TodayDiscoveryView(verseState: verseState)
            }
            .tabItem {
                Label {
                    Text(languageManager.localize("nav_home"))
                } icon: {
                    Image(selectedTab == .today ? "ic_home_on" : "ic_home_off")
                        .renderingMode(.template)
                }
            }
            .tag(Tab.today)

            // Tab 2: Al-Qur'an
            NavigationStack {
                ChaptersView()
            }
            .tabItem {
                Label {
                    Text(languageManager.localize("nav_quran"))
                } icon: {
                    Image(selectedTab == .journey ? "ic_quran_on" : "ic_quran_off")
                        .renderingMode(.template)
                }
            }
            .tag(Tab.journey)

            // Tab 3: Ibadah
            NavigationStack {
                SpiritualToolsView()
            }
            .tabItem {
                Label {
                    Text(languageManager.localize("nav_spiritual"))
                } icon: {
                    Image(selectedTab == .tools ? "ic_spritual_on" : "ic_spritual_off")
                        .renderingMode(.template)
                }
            }
            .tag(Tab.tools)

            // Tab 4: Lainnya
            NavigationStack {
                ProfileView(preferSystemNavigationTitle: false, verseState: verseState)
                    .environment(\.appContainer, container)
            }
            .tabItem {
                Label {
                    Text(languageManager.localize("nav_setting"))
                } icon: {
                    Image(selectedTab == .account ? "ic_setting_on" : "ic_setting_off")
                        .renderingMode(.template)
                }
            }
            .tag(Tab.account)
        }
        .tint(Color(hex: "#1B4332")) // Dark Emerald Green active tint
        .id(languageManager.currentLanguage)
        .environmentObject(prayerController)
        .onChangeWithFallback(of: verseState.shouldNavigateToAccount) { shouldNavigate in
            if shouldNavigate {
                selectedTab = .account
                verseState.didNavigateToAccount()
            }
        }
        .onChangeWithFallback(of: verseState.shouldSelectTodayTab) { shouldSelect in
            if shouldSelect {
                if selectedTab != .today {
                    withAnimation { selectedTab = .today }
                }
                verseState.didSelectTodayTab()
            }
        }
        .onReceive(
            NotificationCenter.default.publisher(
                for: DailyVerseNotificationPreferences.openTodayTabNotification)
        ) { _ in
            verseState.selectTodayTab()
        }
    }
}
