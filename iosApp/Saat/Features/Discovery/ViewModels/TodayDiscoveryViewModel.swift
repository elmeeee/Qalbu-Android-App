//
//  TodayDiscoveryViewModel.swift
//  Saat
//
//  Created by Elmee on 25/04/2026.
//  Copyright © 2026 Elmee. All rights reserved.
//

import Foundation
import Observation

@MainActor
@Observable
final class TodayDiscoveryViewModel {
    var errorMessage: String?
    var detail: RandomAyahPayload?
    var isDetailLoading = false
    var recitations: [RecitationPayload] = []
    var selectedRecitationId: Int = 6
    var continueReading: ReadingSession?
    var continueReadingChapterName: String?

    private let content: QuranContentRepository
    private let readingSessions: ReadingSessionRepository?
    private let defaults = UserDefaults.standard
    private var loadedTranslationId: Int?
    private var dailyAyahFetchGeneration = 0

    init(
        content: QuranContentRepository,
        readingSessions: ReadingSessionRepository? = nil
    ) {
        self.content = content
        self.readingSessions = readingSessions
    }

    func loadDailyAyahWithHadith() {
        detail = nil
        errorMessage = nil
        isDetailLoading = true
        Task { await performDailyAyahFetch(clearDetailOnStart: true) }
    }

    func autoRefreshDailyAyahIfNeeded(forceIfNoData: Bool = true) {
        guard isDetailLoading == false else { return }
        let selected = ChapterReaderPreferences.selectedTranslationId(defaults: defaults)
        if let loaded = loadedTranslationId, loaded != selected {
            reloadForTranslationChange()
            return
        }
        if shouldRefreshRandomAyah(forceIfNoData: forceIfNoData) {
            loadDailyAyahWithHadith()
        }
    }

    func reloadForTranslationChange() {
        guard isDetailLoading == false else { return }
        loadedTranslationId = nil
        DailyAyahRefreshPolicy.clearLastFetch(defaults: defaults)
        loadDailyAyahWithHadith()
    }

    func refreshDailyAyah() async {
        errorMessage = nil
        isDetailLoading = true
        await performDailyAyahFetch(clearDetailOnStart: false)
    }

    func loadContinueReading() async {
        let repository = readingSessions ?? ReadingSessionRepository()
        guard let session = try? await repository.fetchMostRecent() else {
            continueReading = nil
            continueReadingChapterName = nil
            return
        }
        continueReading = session
        let lang = AppLanguageManager.shared.currentLanguage.rawValue
        if let chapter = try? await content.getChapters(language: lang).first(where: { $0.id == session.chapterNumber }) {
            continueReadingChapterName = chapter.displayComplexName
        }
    }

    private func performDailyAyahFetch(clearDetailOnStart: Bool) async {
        dailyAyahFetchGeneration += 1
        let generation = dailyAyahFetchGeneration

        if clearDetailOnStart {
            detail = nil
        }
        isDetailLoading = true
        errorMessage = nil
        defer {
            if generation == dailyAyahFetchGeneration {
                isDetailLoading = false
            }
        }

        do {
            async let ayahTask = content.getRandomAyah()
            async let recitationsTask: [RecitationPayload]? = recitations.isEmpty
                ? (try? content.getRecitations().recitations)
                : nil

            let response = try await ayahTask
            guard generation == dailyAyahFetchGeneration else { return }
            guard let verse = response.verse else {
                throw NSError(domain: "TodayDiscovery", code: -1, userInfo: [NSLocalizedDescriptionKey: "daily verse payload missing"])
            }

            detail = .init(verse)
            loadedTranslationId = ChapterReaderPreferences.selectedTranslationId(defaults: defaults)
            DailyAyahRefreshPolicy.markFetched(defaults: defaults)

            if let fetched = await recitationsTask, fetched.isEmpty == false {
                recitations = fetched
            }
            
            await loadContinueReading()

            if let detail {
                await DailyVerseNotificationCoordinator.refreshAfterDailyAyahLoaded(detail)
            }
        } catch {
            guard generation == dailyAyahFetchGeneration else { return }
            guard NetworkLoadErrorPolicy.shouldSurface(error) else { return }
            errorMessage = (error as? LocalizedError)?.errorDescription ?? error.localizedDescription
        }
    }

    private func shouldRefreshRandomAyah(forceIfNoData: Bool) -> Bool {
        DailyAyahRefreshPolicy.shouldRefresh(
            lastFetchTimestamp: DailyAyahRefreshPolicy.lastFetchTimestamp(defaults: defaults),
            forceIfNoData: forceIfNoData,
            hasDetail: detail != nil,
            defaults: defaults
        )
    }
}
