//
//  ReadingSessionRepository.swift
//  Saat
//
//  Created by Elmee on 25/04/2026.
//  Copyright © 2026 Elmee. All rights reserved.
//

import Foundation

struct ReadingSessionRepository: Sendable {
    init() {}

    func fetchMostRecent() async throws -> ReadingSession? {
        let chapter = UserDefaults.standard.integer(forKey: "last_read_chapter")
        let verse = UserDefaults.standard.integer(forKey: "last_read_verse")
        guard chapter >= 1 else { return nil }
        let timestamp = UserDefaults.standard.double(forKey: "last_read_timestamp")
        let dateString = timestamp > 0 ? ISO8601DateFormatter().string(from: Date(timeIntervalSince1970: timestamp)) : nil
        return ReadingSession(
            id: "local_last_read",
            updatedAt: dateString,
            chapterNumber: chapter,
            verseNumber: max(1, verse)
        )
    }

    func upsertReadingSession(chapterNumber: Int, verseNumber: Int) async throws {
        UserDefaults.standard.set(chapterNumber, forKey: "last_read_chapter")
        UserDefaults.standard.set(verseNumber, forKey: "last_read_verse")
        UserDefaults.standard.set(Date().timeIntervalSince1970, forKey: "last_read_timestamp")
        NotificationCenter.default.post(name: NSNotification.Name("lastReadQuranDidChange"), object: nil)
    }
}
