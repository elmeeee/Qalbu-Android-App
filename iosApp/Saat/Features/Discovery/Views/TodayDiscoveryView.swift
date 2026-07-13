//
//  TodayDiscoveryView.swift
//  Saat
//
//  Created by Elmee on 25/04/2026.
//  Copyright © 2026 Elmee. All rights reserved.
//

import SwiftUI
import CoreLocation

struct TodayDiscoveryView: View {
    @Environment(\.appContainer) private var container
    @Environment(\.scenePhase) private var scenePhase
    @AppStorage("chapterReaderShowTranslation") private var showTranslation = true
    @AppStorage(ChapterReaderPreferences.translationIdKey) private var chapterTranslationId = ChapterReaderPreferences.defaultTranslationId

    @EnvironmentObject private var prayer: PrayerTimesController
    @StateObject private var audio = AudioPlayerController()
    @State private var coordinator: TodayDiscoveryCoordinator?
    @State private var actionsViewModel = TodayVerseActionsViewModel()
    @State private var tracker: PrayerTrackerViewModel?
    @State private var showingPrayerCalendar = false
    @State private var showingTrackerCalendar = false

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
        .allowsHitTesting(!actionsViewModel.isGeneratingShare && !actionsViewModel.publishViewModel.isPosting)
        .overlay(alignment: .top) {
            if actionsViewModel.publishViewModel.showStatus,
               let message = actionsViewModel.publishViewModel.statusMessage {
                TodayStatusToastView(message: message, isError: actionsViewModel.publishViewModel.statusIsError)
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
        .onReceive(NotificationCenter.default.publisher(for: ChapterReaderPreferences.translationDidChangeNotification)) { _ in
            coordinator?.discoveryViewModel?.reloadForTranslationChange()
        }
    }

    @ViewBuilder
    private func discoveryShell(_ vm: TodayDiscoveryViewModel) -> some View {
        ZStack(alignment: .top) {
            LinearGradient(
                colors: [Color.Token.panelGrey, Color.Token.panelGreyAlt, Color.Token.screenBackground],
                startPoint: .top,
                endPoint: .bottom
            )
            .ignoresSafeArea()

            VStack(spacing: 0) {
                headerView(vm: vm)

                ScrollView {
                    VStack(spacing: 0) {
                        if let khgt = coordinator?.dashboardViewModel?.khgtToday {
                            TodayImportantDayBanner(info: khgt)
                                .padding(.horizontal, TodayDiscoveryLayout.horizontalInset)
                                .padding(.vertical, 4)
                        }

                        prayerCard
                            .padding(.vertical, 4)

                        if let tracker {
                            PrayerTrackerCard(viewModel: tracker, onOpenCalendar: {
                                showingTrackerCalendar = true
                            })
                            .padding(.horizontal, TodayDiscoveryLayout.horizontalInset)
                            .padding(.vertical, 8)
                        }

                        if let session = vm.continueReading {
                            TodayContinueReadingCard(
                                session: session,
                                chapterName: vm.continueReadingChapterName,
                                onTap: {
                                    // TODO: Open chapter reader
                                }
                            )
                            .padding(.horizontal, TodayDiscoveryLayout.horizontalInset)
                            .padding(.vertical, 6)
                        }

                        verseSection(vm: vm)
                    }
                    .padding(.top, 8)
                    .padding(.bottom, 24)
                }
                .scrollIndicators(.hidden)
                .refreshable {
                    await coordinator?.refreshToday(discovery: vm)
                    tracker?.refresh()
                }
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
        .sheet(isPresented: tafsirSheetBinding) {
            if let presenter = coordinator?.tafsirPresenter {
                TafsirReaderSheet(presenter: presenter)
            }
        }
    }

    private func headerView(vm: TodayDiscoveryViewModel) -> some View {
        let locationStatus: String? = {
            if prayer.cityName != nil {
                return nil
            }
            let authStatus = CLLocationManager().authorizationStatus
            if authStatus == .notDetermined {
                return AppLanguageManager.shared.localize("prayer_allow_location")
            } else if authStatus == .denied || authStatus == .restricted {
                return AppLanguageManager.shared.localize("location_failed")
            } else if prayer.isLoading {
                return AppLanguageManager.shared.localize("locating")
            } else {
                return AppLanguageManager.shared.localize("locating")
            }
        }()

        return TodayDiscoveryHeaderView(
            hijriDate: prayer.hijriDateLabel,
            gregorianDate: prayer.gregorianDateLabel,
            cityName: prayer.cityName,
            locationStatus: locationStatus,
            avatarURL: verseState.userAvatarURL,
            isLoggingIn: verseState.isLoggingIn,
            onAccountTap: { verseState.requestAccount() }
        )
        .background(Color.Token.panelGrey.ignoresSafeArea(edges: .top))
    }

    @ViewBuilder
    private func verseSection(vm: TodayDiscoveryViewModel) -> some View {
        VStack(alignment: .leading, spacing: 14) {
            if let detail = vm.detail {
                TodayVerseOfDayCardView(
                    verse: detail,
                    showTranslation: showTranslation,
                    isDetailLoading: vm.isDetailLoading,
                    reciterName: vm.recitations.first(where: { $0.id == vm.selectedRecitationId })?.displayName ?? "",
                    isPlaying: audio.isPlayingURL(detail.audio?.url) && audio.isPlaying,
                    onAudio: { playAudio(for: detail, vm: vm) },
                    onShare: {
                        guard actionsViewModel.isGeneratingShare == false else { return }
                        Task { await actionsViewModel.presentShare(for: detail, shareProvider: vm) }
                    },
                    onReflect: {
                        guard let container else { return }
                        Task {
                            await actionsViewModel.publishReflection(
                                for: detail,
                                shareProvider: vm,
                                verseState: verseState,
                                container: container
                            )
                        }
                    },
                    onTafsir: {
                        actionsViewModel.openTafsir(
                            for: detail,
                            presenter: coordinator?.tafsirPresenter,
                            shareProvider: vm
                        )
                    },
                    audioAccessibilityHint: audioHint(vm: vm)
                )
            } else if vm.isDetailLoading {
                LoadingSkeleton()
            }

            if let error = vm.errorMessage, vm.detail == nil, vm.isDetailLoading == false {
                Text(error)
                    .foregroundStyle(.red)
                    .font(.footnote)
                    .padding(.top)
            }
        }
        .padding(.horizontal, TodayDiscoveryLayout.horizontalInset)
        .padding(.top, 16)
        .padding(.bottom, 100)
    }

    @ViewBuilder
    private var prayerCard: some View {
        Group {
            if let dashboard = coordinator?.dashboardViewModel {
                ZStack(alignment: .topTrailing) {
                    PrayerDashboardCard(viewModel: dashboard)
                        .onTapGesture {
                            showingPrayerCalendar = true
                        }
                }
            } else {
                RoundedRectangle(cornerRadius: 24)
                    .fill(Color.Token.softGrey.opacity(0.4))
                    .frame(height: 220)
                    .padding(.horizontal, TodayDiscoveryLayout.horizontalInset)
                    .padding(.top, 40)
            }
        }
    }

    private var tafsirSheetBinding: Binding<Bool> {
        Binding(
            get: { coordinator?.tafsirPresenter?.isSheetPresented ?? false },
            set: { coordinator?.tafsirPresenter?.isSheetPresented = $0 }
        )
    }

    private func playAudio(for verse: RandomAyahPayload, vm: TodayDiscoveryViewModel) {
        guard let url = verse.audio?.url else { return }
        let reciter = vm.recitations
            .first(where: { $0.id == vm.selectedRecitationId })?.displayName ?? ""
        let label = verse.verseKey.flatMap { ShareVerseCard.humanLabel(for: $0) } ?? ""
        audio.playVerse(url: url, surahTitle: label, ayahLabel: reciter, reciterName: reciter)
    }

    private func audioHint(vm: TodayDiscoveryViewModel) -> String {
        SaatAccessibility.VerseActions.audio(
            hint: vm.recitations.first(where: { $0.id == vm.selectedRecitationId })?.displayName ?? ""
        )
    }
}
