//
//  ReflectRepository.swift
//  Saat
//
//  Created by Elmee on 19/05/2026.
//  Copyright © 2026 Elmee. All rights reserved.
//

import Foundation

struct ReflectRepository: Sendable {
    private let habits: UserHabitRepository

    init(habits: UserHabitRepository) {
        self.habits = habits
    }

    func getCachedFeed(limit: Int) async -> ReflectFeedEnvelope? {
        await APICache.ReflectFeed.shared.cachedFeed(limit: limit, ignoreTTL: true)
    }

    func getCachedMyPosts(limit: Int) async -> ReflectFeedEnvelope? {
        await APICache.ReflectFeed.shared.cachedMyPosts(limit: limit, ignoreTTL: true)
    }

    func fetchFeed(page: Int = 1, limit: Int = 12, force: Bool = false) async throws -> ReflectFeedEnvelope {
        if let cached = await APICache.ReflectFeed.shared.cachedFeed(limit: limit) {
            return cached
        }
        return ReflectFeedEnvelope(data: [], page: page, limit: limit, hasMore: false)
    }

    func fetchMyPosts(page: Int = 1, limit: Int = 12, force: Bool = false) async throws -> ReflectFeedEnvelope {
        if let cached = await APICache.ReflectFeed.shared.cachedMyPosts(limit: limit) {
            return cached
        }
        return ReflectFeedEnvelope(data: [], page: page, limit: limit, hasMore: false)
    }

    func toggleLike(postId: String) async throws -> Bool {
        return false
    }

    @discardableResult
    func createReflectionPost(
        body: String,
        verseKey: String?,
        authorId: String,
        draft: Bool = false,
        idempotencyKey: String? = nil
    ) async throws -> UserPost {
        let trimmed = body.trimmingCharacters(in: .whitespacesAndNewlines)
        guard trimmed.count >= 6 else {
            throw NSError(domain: "ReflectRepository", code: 400, userInfo: [NSLocalizedDescriptionKey: "Reflection must be at least 6 characters."])
        }
        let created = UserPost(
            id: UUID().uuidString,
            body: trimmed,
            createdAt: Self.iso8601Now(),
            likesCount: 0,
            hasLiked: false,
            author: nil,
            references: nil
        )
        await APICache.ReflectFeed.shared.clear()
        return created
    }

    func createPostFromShare(
        body: String,
        verseKey: String?,
        authorId: String,
        languageId: Int? = nil
    ) async throws -> UserPost {
        _ = languageId
        return try await createReflectionPost(
            body: body,
            verseKey: verseKey,
            authorId: authorId
        )
    }

    static func parseVerseKey(_ key: String) -> (sura: Int, ayah: Int)? {
        let parts = key.split(separator: ":")
        guard parts.count == 2,
              let sura = Int(parts[0]),
              let ayah = Int(parts[1]),
              sura >= 1,
              ayah >= 1 else {
            return nil
        }
        return (sura, ayah)
    }

    private static func iso8601Now() -> String {
        ISO8601DateFormatter().string(from: Date())
    }
}
