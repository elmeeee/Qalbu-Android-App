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
    private let shareComposer: TodayShareTextComposer
    private let readingSessions: ReadingSessionRepository?
    private let defaults = UserDefaults.standard
    private var loadedTranslationId: Int?
    private var dailyAyahFetchGeneration = 0

    init(
        content: QuranContentRepository,
        shareComposer: TodayShareTextComposer? = nil,
        readingSessions: ReadingSessionRepository? = nil
    ) {
        self.content = content
        self.shareComposer = shareComposer ?? TodayShareTextComposer(content: content)
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
        shareComposer.clearCaches()
        loadDailyAyahWithHadith()
    }

    func refreshDailyAyah() async {
        errorMessage = nil
        isDetailLoading = true
        await performDailyAyahFetch(clearDetailOnStart: false)
    }

    func loadContinueReading() async {
        if let readingSessions {
            if let session = try? await readingSessions.fetchReadingSessions(first: 1).data?.first {
                self.continueReading = session
                let lang = AppLanguageManager.shared.currentLanguage.rawValue
                if let chapter = try? await content.getChapters(language: lang).first(where: { $0.id == session.chapterNumber }) {
                    self.continueReadingChapterName = chapter.displayComplexName
                }
                return
            }
        }

        // Fallback to local storage
        let localChapter = UserDefaults.standard.integer(forKey: "last_read_chapter")
        let localVerse = UserDefaults.standard.integer(forKey: "last_read_verse")
        if localChapter >= 1 {
            let session = ReadingSession(
                id: "local_last_read",
                updatedAt: nil,
                chapterNumber: localChapter,
                verseNumber: max(1, localVerse)
            )
            self.continueReading = session
            let lang = AppLanguageManager.shared.currentLanguage.rawValue
            if let chapter = try? await content.getChapters(language: lang).first(where: { $0.id == localChapter }) {
                self.continueReadingChapterName = chapter.displayComplexName
            }
        }
    }

    func prefetchShareTextIfNeeded(for verse: RandomAyahPayload) async {
        await shareComposer.prefetchShareTextIfNeeded(for: verse)
    }

    func cachedShareText(for verse: RandomAyahPayload) -> String? {
        shareComposer.cachedShareText(for: verse)
    }

    func quickReflectionText(for verse: RandomAyahPayload) -> String {
        shareComposer.quickReflectionText(for: verse)
    }

    func prepareShareText(for verse: RandomAyahPayload) async -> String {
        await shareComposer.prepareShareText(for: verse)
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
                throw QFError.parsingError("daily verse payload")
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
