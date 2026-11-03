//
//  ProfileViewModel.swift
//  Saat
//
//  Created by Elmee on 25/04/2026.
//  Copyright © 2026 Elmee. All rights reserved.
//

import Foundation
import Observation

@MainActor
@Observable
final class ProfileViewModel {
    var isLoading = true
    var errorMessage: String?
    var authBusy = false
    var profile: UserProfilePayload?

    private let container: AppContainer

    init(container: AppContainer) {
        self.container = container
    }

    func hydrateFromCacheIfNeeded() async {
        guard profile == nil, let cached = await APICache.Profile.shared.cached() else { return }
        profile = cached
    }

    func fetchProfile(force: Bool = false) async {
        await hydrateFromCacheIfNeeded()
        if profile == nil {
            profile = try? await container.habits.fetchMyProfile(force: force)
        }
        isLoading = false
    }

    func signIn() async {
        await fetchProfile(force: true)
    }

    func signOut() async {
        authBusy = true
        errorMessage = nil
        defer { authBusy = false }
        await container.signOut()
        profile = nil
    }

    func reloadIfNeeded() async {
        await hydrateFromCacheIfNeeded()
        await fetchProfile()
    }

    func reload(force: Bool = false) async {
        await fetchProfile(force: force)
    }

    func handleOAuthDidChange(isInProgress: Bool) async {}

    func handleSessionDidChange() async {}

    func sync(to verseState: TodayVerseState?) {
        guard let profile else { return }
        verseState?.applyProfile(profile)
    }
}
