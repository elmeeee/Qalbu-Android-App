//
//  TodayVerseState.swift
//  Sāat
//
//  Created by Elmee on 25/04/2026.
//  Copyright © 2026 Elmee. All rights reserved.
//

import Foundation
import Observation

@MainActor
@Observable
final class TodayVerseState {
    var activeVerseKey: String?
    var activeVerseLabel: String?
    var activeArabicSnippet: String?
    var shouldNavigateToReflect = false
    var feedNeedsRefresh = false
    var shouldNavigateToAccount = false
    var shouldSelectTodayTab = false
    var isLoggedIn = false
    var userAvatarURL: URL?
    var userDisplayName: String?
    var userId: String?
    var isLoggingIn = false
    var isRefreshingProfile = false
    var hasResolvedSession = false

    private var profileRefreshTask: Task<Void, Never>?
    var preparedShareText: String?

    func setVerse(key: String?, label: String?, arabic: String?) {
        activeVerseKey = key
        activeVerseLabel = label
        activeArabicSnippet = arabic
    }

    func requestReflect(shareText: String? = nil) {
        preparedShareText = shareText
        shouldNavigateToReflect = true
    }

    func didNavigateToReflect() {
        shouldNavigateToReflect = false
    }

    func notifyFeedDidUpdate() {
        feedNeedsRefresh = true
    }

    func didRefreshFeed() {
        feedNeedsRefresh = false
    }

    func requestAccount() {
        shouldNavigateToAccount = true
    }

    func didNavigateToAccount() {
        shouldNavigateToAccount = false
    }

    func selectTodayTab() {
        shouldSelectTodayTab = true
    }

    func didSelectTodayTab() {
        shouldSelectTodayTab = false
    }

    func syncOAuthUIState(container: AppContainer?) {
        isLoggingIn = container?.oauth.isWebAuthInProgress ?? false
    }

    func handleOAuthFlowDidChange(container: AppContainer?) async {
        syncOAuthUIState(container: container)
        guard container?.oauth.isWebAuthInProgress != true else { return }
        guard let container, await container.userSession.hasUserAccessToken() else {
            applySignedOutProfile()
            hasResolvedSession = true
            return
        }
        await ensureProfileLoaded(container: container)
        container.warmReflectDataIfSignedIn()
    }

    func handleUserSessionDidChange(container: AppContainer?) async {
        guard container?.oauth.isWebAuthInProgress != true else { return }
        guard let container else { return }
        let hasToken = await container.userSession.hasUserAccessToken()
        if hasToken == false {
            await APICache.clearAll()
            applySignedOutProfile()
            hasResolvedSession = true
            return
        }
        await ensureProfileLoaded(container: container)
        container.warmReflectDataIfSignedIn()
    }

    func applyProfile(_ profile: UserProfilePayload) {
        userAvatarURL = profile.preferredAvatarURL
        userDisplayName = profile.displayTitle
        userId = profile.id
        isLoggedIn = profile.id.isEmpty == false
        hasResolvedSession = true
    }

    func syncFromCachedProfile(container: AppContainer?) async {
        guard let container else { return }
        guard await container.userSession.hasUserAccessToken() else { return }
        if let cached = await APICache.Profile.shared.cached() {
            applyProfile(cached)
        }
    }

    private func applySignedOutProfile() {
        guard isLoggingIn == false else { return }
        profileRefreshTask?.cancel()
        profileRefreshTask = nil
        isLoggingIn = false
        isRefreshingProfile = false
        isLoggedIn = false
        userAvatarURL = nil
        userDisplayName = nil
        userId = nil
        preparedShareText = nil
        shouldNavigateToReflect = false
        shouldNavigateToAccount = false
        feedNeedsRefresh = false
    }

    func ensureProfileLoaded(container: AppContainer?) async {
        await applySessionSnapshot(container: container)
        enqueueProfileRefresh(container: container)
    }

    func ensureProfileLoadedAndAwait(container: AppContainer?) async {
        await applySessionSnapshot(container: container)
        if let profileRefreshTask {
            await profileRefreshTask.value
            return
        }
        await refreshProfile(container: container)
    }

    private func applySessionSnapshot(container: AppContainer?) async {
        guard let container else {
            hasResolvedSession = true
            return
        }
        if isLoggingIn || container.oauth.isWebAuthInProgress {
            return
        }

        let hasToken = await container.userSession.hasUserAccessToken()
        if hasToken {
            if let cached = await APICache.Profile.shared.cached() {
                applyProfile(cached)
            } else {
                isLoggedIn = false
            }
        } else {
            applySignedOutProfile()
        }
        hasResolvedSession = true
    }

    private func enqueueProfileRefresh(container: AppContainer?) {
        guard profileRefreshTask == nil else { return }
        profileRefreshTask = Task { @MainActor in
            await refreshProfile(container: container)
            profileRefreshTask = nil
        }
    }

    func refreshProfile(container: AppContainer?) async {
        guard let container else { return }
        if isLoggingIn || container.oauth.isWebAuthInProgress {
            return
        }
        isRefreshingProfile = true
        defer { isRefreshingProfile = false }

        let hasToken = await container.userSession.hasUserAccessToken()
        guard hasToken else {
            applySignedOutProfile()
            hasResolvedSession = true
            return
        }

        if let cached = await APICache.Profile.shared.cached() {
            applyProfile(cached)
        }

        do {
            let profile = try await container.habits.fetchMyProfile()
            applyProfile(profile)
        } catch {
            if Self.isAuthenticationFailure(error) {
                container.invalidateUserSession()
                applySignedOutProfile()
            } else if userId == nil {
                isLoggedIn = false
            } else {
                isLoggedIn = true
            }
        }
        hasResolvedSession = true
    }

    static func isAuthenticationFailure(_ error: Error) -> Bool {
        let nsError = error as NSError
        return nsError.code == 401 || nsError.code == 403
    }

    func signIn(container: AppContainer?) async {
        guard let container else { return }
        await ensureProfileLoadedAndAwait(container: container)
    }
}
