//
//  RootTabView.swift
//  Sāat
//
//  Created by Elmee on 25/04/2026.
//  Copyright © 2026 Elmee. All rights reserved.
//

import SwiftUI

struct RootTabView: View {
    enum Tab: Hashable {
        case today, journey, tools, account
    }

    enum TodayNavigation: Hashable {
        case account
    }

    @Environment(\.scenePhase) private var scenePhase
    @Environment(\.appContainer) private var container
    @State private var selectedTab: Tab = .today
    @State private var todayNavigationPath: [TodayNavigation] = []
    @State private var vm = RootTabViewModel()
    @ObservedObject private var languageManager = AppLanguageManager.shared
    @StateObject private var prayerController = PrayerTimesController()
    let verseState: TodayVerseState

    init(verseState: TodayVerseState) {
        self.verseState = verseState
    }

    var body: some View {
        ZStack(alignment: .bottom) {
            Group {
                switch selectedTab {
                case .today:
                    NavigationStack(path: $todayNavigationPath) {
                        TodayDiscoveryView(verseState: verseState)
                            .toolbar(.hidden, for: .navigationBar)
                    }
                case .journey:
                    ChaptersView()
                case .tools:
                    NavigationStack {
                        SpiritualToolsView()
                            .toolbar(.hidden, for: .navigationBar)
                    }
                case .account:
                    NavigationStack {
                        ProfileView(preferSystemNavigationTitle: false, verseState: verseState)
                            .environment(\.appContainer, container)
                            .toolbar(.hidden, for: .navigationBar)
                    }
                }
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)

            // Floating Tab Bar
            FloatingTabBar(selectedTab: $selectedTab)
        }
        .ignoresSafeArea(.keyboard, edges: .bottom)
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
        .onReceive(NotificationCenter.default.publisher(for: .qfOAuthWebAuthStateDidChange)) { _ in
            verseState.syncOAuthUIState(container: container)
        }
        .onChangeWithFallback(of: scenePhase) { p in
            if p == .active {
                Task {
                    guard container?.oauth.isWebAuthInProgress == false else { return }
                    await verseState.ensureProfileLoaded(container: container)
                    await vm.runSync(container: container)
                }
            }
        }
        .onReceive(
            NotificationCenter.default.publisher(
                for: DailyVerseNotificationPreferences.openTodayTabNotification)
        ) { _ in
            verseState.selectTodayTab()
        }
        .onReceive(NotificationCenter.default.publisher(for: .qfUserSessionDidChange)) { _ in
            Task { @MainActor in
                guard container?.oauth.isWebAuthInProgress == false else { return }
                guard await vm.shouldResetToDiscover(container: container) else { return }
                if selectedTab != .today {
                    selectedTab = .today
                }
                if todayNavigationPath.isEmpty == false {
                    await Task.yield()
                    todayNavigationPath = []
                }
            }
        }
    }
}
