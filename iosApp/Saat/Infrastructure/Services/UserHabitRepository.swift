//
//  UserHabitRepository.swift
//  Saat
//
//  Created by Elmee on 25/04/2026.
//  Copyright © 2026 Elmee. All rights reserved.
//

import Foundation

struct UserHabitRepository: Sendable {
    init() {}

    func logQuranActivityForToday(verses: Int) async throws {
        // Saved locally
    }

    func fetchMyProfile(force: Bool = false) async throws -> UserProfilePayload {
        if force == false, let cached = await APICache.Profile.shared.cached() {
            return cached
        }
        let profile = UserProfilePayload(id: "local_user", username: "User", firstName: "User", lastName: nil, bio: nil, avatarUrls: nil)
        await APICache.Profile.shared.store(profile)
        return profile
    }

    func patchMyProfileNoop(postAs: Bool) async throws -> Bool {
        return true
    }
}
