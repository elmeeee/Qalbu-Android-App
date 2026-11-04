//
//  TodayDiscoveryCoordinator.swift
//  Saat
//
//  Created by Elmee on 25/04/2026.
//  Copyright © 2026 Elmee. All rights reserved.
//

import Foundation
import Observation

@MainActor
@Observable
final class TodayDiscoveryCoordinator {
    var discoveryViewModel: TodayDiscoveryViewModel?
    var dashboardViewModel: PrayerDashboardViewModel?
    var tafsirPresenter: TafsirPresenter?

    let prayer: PrayerTimesController
    let audio: AudioPlayerController

    init(prayer: PrayerTimesController, audio: AudioPlayerController) {
        self.prayer = prayer
        self.audio = audio
    }

    func bootstrap(container: AppContainer) async {
        if dashboardViewModel == nil {
            dashboardViewModel = PrayerDashboardViewModel(controller: prayer)
        }

        guard discoveryViewModel == nil else {
            discoveryViewModel?.autoRefreshDailyAyahIfNeeded(forceIfNoData: true)
            prayer.refreshIfNeeded()
            return
        }

        let vm = TodayDiscoveryViewModel(
            content: container.content,
            readingSessions: container.readingSessions
        )
        discoveryViewModel = vm
        if tafsirPresenter == nil {
            tafsirPresenter = TafsirPresenter(content: container.content)
        }
        vm.autoRefreshDailyAyahIfNeeded(forceIfNoData: true)
        await vm.loadContinueReading()
        prayer.refreshIfNeeded()
    }

    func onVerseKeyChanged(_ newKey: String?) {
        guard let newKey else { return }
        Task { await tafsirPresenter?.prefetch(ayahKey: newKey) }
    }

    func refreshToday(discovery: TodayDiscoveryViewModel?) async {
        async let prayerRefresh: Void = prayer.forceRefresh()
        if let discovery {
            await discovery.refreshDailyAyah()
            await discovery.loadContinueReading()
        }
        await prayerRefresh
    }

    func stopAudio() {
        audio.stop()
    }
}
