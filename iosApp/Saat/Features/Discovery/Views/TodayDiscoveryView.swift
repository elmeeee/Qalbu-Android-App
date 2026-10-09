//
//  TodayDiscoveryView.swift
//  Saat
//
//  Created by Elmee on 25/04/2026.
//  Copyright © 2026 Elmee. All rights reserved.
//

import CoreLocation
import SwiftUI

struct TodayDiscoveryView: View {
    @Environment(\.appContainer) private var container
    @Environment(\.scenePhase) private var scenePhase
    @AppStorage("chapterReaderShowTranslation") private var showTranslation = true
    @AppStorage(ChapterReaderPreferences.translationIdKey) private var chapterTranslationId =
        ChapterReaderPreferences.defaultTranslationId

    @EnvironmentObject private var prayer: PrayerTimesController
    @StateObject private var audio = AudioPlayerController()
    @State private var coordinator: TodayDiscoveryCoordinator?
    @State private var actionsViewModel = TodayVerseActionsViewModel()
    @State private var tracker: PrayerTrackerViewModel?
    @State private var showingPrayerCalendar = false
    @State private var showingTrackerCalendar = false
    @State private var isScrolled = false

    let verseState: TodayVerseState

    var body: some View {
        ZStack {
            if let vm = coordinator?.discoveryViewModel {
                discoveryShell(vm)
            }

            if actionsViewModel.isGeneratingShare || actionsViewModel.publishViewModel.isPosting {
                TodayBusyOverlayView(isPosting: actionsViewModel.publishViewModel.isPosting)
            }
        }
        .allowsHitTesting(
            !actionsViewModel.isGeneratingShare && !actionsViewModel.publishViewModel.isPosting
        )
        .overlay(alignment: .top) {
            if actionsViewModel.publishViewModel.showStatus,
                let message = actionsViewModel.publishViewModel.statusMessage
            {
                TodayStatusToastView(
                    message: message, isError: actionsViewModel.publishViewModel.statusIsError
                )
                .transition(.move(edge: .top).combined(with: .opacity))
            }
        }
        .onAppear {
            if coordinator == nil {
                coordinator = TodayDiscoveryCoordinator(prayer: prayer, audio: audio)
            }
            if tracker == nil {
                tracker = PrayerTrackerViewModel(
                    appGroupIdentifier: container?.configuration.appGroupIdentifier,
                    controller: prayer
                )
            } else {
                tracker?.refresh()
            }
            guard let container, let coordinator else { return }
            Task { await coordinator.bootstrap(container: container, verseState: verseState) }
        }
        .onChange(of: chapterTranslationId) { _, _ in
            coordinator?.discoveryViewModel?.reloadForTranslationChange()
        }
        .onReceive(
            NotificationCenter.default.publisher(
                for: ChapterReaderPreferences.translationDidChangeNotification)
        ) { _ in
            coordinator?.discoveryViewModel?.reloadForTranslationChange()
        }
    }

    private var prayerBackgroundName: String {
        guard let target = coordinator?.dashboardViewModel?.nextPrayerDisplayName.lowercased()
        else {
            return "prayer_bg_day"
        }
        if target.contains("fajr") || target.contains("subuh") || target.contains("sunrise")
            || target.contains("terbit") || target.contains("dhuhr") || target.contains("dzuhur")
        {
            return "prayer_bg_day"
        } else if target.contains("asr") || target.contains("ashar") || target.contains("maghrib") {
            return "prayer_bg_sunset"
        } else {
            return "prayer_bg_night"
        }
    }

    @ViewBuilder
    private func discoveryShell(_ vm: TodayDiscoveryViewModel) -> some View {
        ZStack(alignment: .top) {
            SaatTokens.Colors.homeBg
                .ignoresSafeArea()

            // Dynamic Aspect Ratio Prayer Scenic Backdrop Image
            Image(prayerBackgroundName)
                .resizable()
                .scaledToFill()
                .frame(maxWidth: .infinity)
                .frame(height: 238)
                .clipped()
                .overlay(
                    LinearGradient(
                        colors: [
                            Color.clear,
                            Color.clear,
                            SaatTokens.Colors.homeBg.opacity(0.30),
                            SaatTokens.Colors.homeBg,
                        ],
                        startPoint: .top,
                        endPoint: .bottom
                    )
                )
                .ignoresSafeArea(edges: .top)

            ScrollView {
                VStack(spacing: 14) {
                    if let khgt = coordinator?.dashboardViewModel?.khgtToday {
                        TodayImportantDayBanner(info: khgt)
                            .padding(.horizontal, 20)
                    }

                    // 1. Prayer Dashboard Card
                    if let dashboard = coordinator?.dashboardViewModel {
                        PrayerDashboardCard(
                            viewModel: dashboard,
                            onOpenCalendar: {
                                showingPrayerCalendar = true
                            }
                        )
                        .padding(.horizontal, 20)
                    }

                    // 2. Continue Reading Card
                    if let session = vm.continueReading {
                        TodayContinueReadingCard(
                            session: session,
                            chapterName: vm.continueReadingChapterName,
                            onTap: {
                                // Handled via coordinator or reader
                            }
                        )
                        .padding(.horizontal, 20)
                    }

                    // 3. Prayer Tracker Card ("Perjalanan Hari Ini")
                    if let tracker {
                        PrayerTrackerCard(
                            viewModel: tracker,
                            onOpenCalendar: {
                                showingTrackerCalendar = true
                            }
                        )
                        .padding(.horizontal, 20)
                    }
                }
                .padding(.top, 70)
                .padding(.bottom, 100)
            }
            .scrollIndicators(.hidden)
            .refreshable {
                await coordinator?.refreshToday(discovery: vm)
                tracker?.refresh()
            }
            .safeAreaInset(edge: .top, spacing: 0) {
                TodayDiscoveryHeaderView(
                    hijriDate: prayer.hijriDateLabel,
                    gregorianDate: prayer.gregorianDateLabel,
                    cityName: prayer.cityName,
                    locationStatus: locationStatusText,
                    isScrolled: isScrolled,
                    isDarkBackground: prayerBackgroundName != "prayer_bg_day",
                    onLocationClick: {
                        // Open location selection or request
                    },
                    onCalendarClick: {
                        showingPrayerCalendar = true
                    }
                )
            }
        }
        .navigationDestination(isPresented: $showingPrayerCalendar) {
            PrayerCalendarView().environmentObject(prayer)
        }
        .navigationDestination(isPresented: $showingTrackerCalendar) {
            PrayerTrackerCalendarView().environmentObject(prayer)
        }
        .animation(nil, value: audio.currentURL)
        .safeAreaInset(edge: .bottom) {
            if audio.currentURL != nil {
                VerseAudioBar(audio: audio)
            }
        }
        .onChangeWithFallback(of: scenePhase) { phase in
            if phase == .active {
                vm.autoRefreshDailyAyahIfNeeded(forceIfNoData: false)
                prayer.refreshIfNeeded()
                tracker?.refresh()
            }
        }
        .onChangeWithFallback(of: vm.detail?.verseKey) { newKey in
            guard let coordinator else { return }
            coordinator.onVerseKeyChanged(newKey, verseState: verseState, discovery: vm)
        }
        .onDisappear {
            coordinator?.stopAudio()
        }
    }

    private var locationStatusText: String? {
        if prayer.cityName != nil { return nil }
        let authStatus = CLLocationManager().authorizationStatus
        if authStatus == .notDetermined {
            return "Aktifkan Lokasi"
        } else if authStatus == .denied || authStatus == .restricted {
            return "Lokasi Tidak Tersedia"
        } else if prayer.isLoading {
            return "Menemukan lokasi…"
        } else {
            return "Menemukan lokasi…"
        }
    }

}
