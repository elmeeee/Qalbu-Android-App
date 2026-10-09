//
//  AppEndpoints.swift
//  Sāat
//
//  Created by Sufiandy Elmy on 09/10/2026.
//  Copyright © 2026 Elmee. All rights reserved.
//

import Foundation

enum AppEndpoints {

    enum External {
        static let versesWebBase: String = "https://verses.quran.com"
        static let alAdhanRoot: String = "https://api.aladhan.com"
    }

    enum Prefix {
        static let contentAPI = "content/api/v4"
        static let quranReflect = "quran-reflect/v1"
        static let authV1 = "auth/v1"
    }

    enum OAuth {
        static let directory = "oauth2"
        static let token = "token"
        static let introspect = "introspect"
    }

    enum Content {
        static let chapters = "chapters"
        static let versesRandom = "verses/random"
        static func versesByChapter(_ chapterNumber: Int) -> String {
            "verses/by_chapter/\(chapterNumber)"
        }
        static let resourcesRecitations = "resources/recitations"
        static let resourcesTranslations = "resources/translations"
        static func verseByKey(_ key: String) -> String { "verses/by_key/\(key)" }
        static func hadithsByAyah(_ ayahKey: String) -> String {
            "hadith-references/by-ayah/\(ayahKey)/hadiths"
        }

        static func tafsirByAyah(resourceId: String, ayahKey: String) -> String {
            "tafsirs/\(resourceId)/by_ayah/\(ayahKey)"
        }
    }

    enum Reflect {
        static let activityDays = "activity_days"
        static let posts = "posts"
        static let postsFeed = "posts/feed"
        static let postsMyPosts = "posts/my-posts"
        static func postToggleLike(_ postId: String) -> String {
            "posts/\(postId)/toggle-like"
        }
        static let userProfile = "users/profile"
    }

    enum AuthV1 {
        static let readingSessions = "reading-sessions"
    }

    enum URLBuilder {
        static func absoluteVerseMediaURLString(from raw: String) -> String {
            if raw.hasPrefix("//") { return "https:\(raw)" }
            if raw.hasPrefix("http") { return raw }
            return "\(External.versesWebBase)/\(raw)"
        }

        static func alAdhanTimings(
            timestamp: Int,
            latitude: Double,
            longitude: Double,
            method: PrayerCalculationMethod = .muhammadiyah
        ) -> URL? {
            var components = URLComponents(string: "\(External.alAdhanRoot)/v1/timings/\(timestamp)")
            var query: [URLQueryItem] = [
                .init(name: "latitude", value: "\(latitude)"),
                .init(name: "longitude", value: "\(longitude)"),
                .init(name: "method", value: "\(method.aladhanMethodID)"),
                .init(name: "school", value: "\(method.aladhanSchool)"),
                .init(name: "tune", value: method.aladhanTune)
            ]
            if let methodSettings = method.aladhanMethodSettings {
                query.append(.init(name: "methodSettings", value: methodSettings))
            }
            components?.queryItems = query
            return components?.url
        }

        static func alAdhanCalendar(
            year: Int,
            month: Int,
            latitude: Double,
            longitude: Double,
            method: PrayerCalculationMethod = .muhammadiyah
        ) -> URL? {
            var components = URLComponents(string: "\(External.alAdhanRoot)/v1/calendar")
            var query: [URLQueryItem] = [
                .init(name: "latitude", value: "\(latitude)"),
                .init(name: "longitude", value: "\(longitude)"),
                .init(name: "year", value: "\(year)"),
                .init(name: "month", value: "\(month)"),
                .init(name: "method", value: "\(method.aladhanMethodID)"),
                .init(name: "school", value: "\(method.aladhanSchool)"),
                .init(name: "tune", value: method.aladhanTune)
            ]
            if let methodSettings = method.aladhanMethodSettings {
                query.append(.init(name: "methodSettings", value: methodSettings))
            }
            components?.queryItems = query
            return components?.url
        }
    }
}
