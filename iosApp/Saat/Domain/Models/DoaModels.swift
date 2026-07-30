//
//  DoaModels.swift
//  Saat
//
//  Created by Elmee on 25/06/2026.
//  Copyright © 2026 Elmee. All rights reserved.
//

import Foundation

internal struct DoaCategory: Codable, Hashable, Sendable {
    internal let id: String?
    internal let name: String?
    internal let slug: String?
    internal let photo: String?
}

internal struct DoaItem: Codable, Hashable, Sendable, Identifiable {
    internal var id: String {
        return _id ?? UUID().uuidString
    }
    private let _id: String?
    internal let title: String?
    internal let arabic: String?
    internal let latin: String?
    internal let translation: String?
    internal let notes: String?
    internal let fawaid: String?
    internal let source: String?
    internal let category: String?
    internal let categories: DoaCategory?

    private enum CodingKeys: String, CodingKey {
        case _id = "id"
        case title, arabic, latin, translation, notes, fawaid, source, category, categories
    }

    internal init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        _id = try container.decodeIfPresent(String.self, forKey: ._id)
        title = try container.decodeIfPresent(String.self, forKey: .title)
        arabic = try container.decodeIfPresent(String.self, forKey: .arabic)
        latin = try container.decodeIfPresent(String.self, forKey: .latin)
        notes = try container.decodeIfPresent(String.self, forKey: .notes)
        fawaid = try container.decodeIfPresent(String.self, forKey: .fawaid)
        source = try container.decodeIfPresent(String.self, forKey: .source)
        category = try container.decodeIfPresent(String.self, forKey: .category)
        categories = try container.decodeIfPresent(DoaCategory.self, forKey: .categories)

        if let str = try? container.decodeIfPresent(String.self, forKey: .translation) {
            translation = str
        } else if let dict = try? container.decodeIfPresent([String: String].self, forKey: .translation) {
            translation = dict["id"] ?? dict["ms"] ?? dict["en"]
        } else {
            translation = nil
        }
    }
}

internal struct DoaListResponse: Codable, Sendable {
    internal let success: Bool?
    internal let message: String?
    internal let data: [DoaItem]?
}

internal struct DhikrContentItem: Codable, Hashable, Sendable {
    internal let id: String?
    internal let arabic: String?
    internal let latin: String?
    internal let translation: String?
    internal let fawaid: String?
    internal let notes: String?
    internal let source: String?
    internal let repeatCount: Int?

    private enum CodingKeys: String, CodingKey {
        case repeatCount = "repeat_count"
        case id, arabic, latin, translation, fawaid, notes, source
    }

    internal init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        id = try container.decodeIfPresent(String.self, forKey: .id)
        arabic = try container.decodeIfPresent(String.self, forKey: .arabic)
        latin = try container.decodeIfPresent(String.self, forKey: .latin)
        fawaid = try container.decodeIfPresent(String.self, forKey: .fawaid)
        notes = try container.decodeIfPresent(String.self, forKey: .notes)
        source = try container.decodeIfPresent(String.self, forKey: .source)
        repeatCount = try container.decodeIfPresent(Int.self, forKey: .repeatCount)

        if let str = try? container.decodeIfPresent(String.self, forKey: .translation) {
            translation = str
        } else if let dict = try? container.decodeIfPresent([String: String].self, forKey: .translation) {
            translation = dict["id"] ?? dict["ms"] ?? dict["en"]
        } else {
            translation = nil
        }
    }
}

internal struct DhikrBundle: Codable, Hashable, Sendable, Identifiable {
    internal var id: String {
        return title ?? UUID().uuidString
    }
    internal let title: String?
    internal let category: String?
    internal let compiledBy: String?
    internal let content: [DhikrContentItem]?

    private enum CodingKeys: String, CodingKey {
        case compiledBy = "compiled_by"
        case title, category, content
    }
}

internal struct DhikrListResponse: Codable, Sendable {
    internal let success: Bool?
    internal let message: String?
    internal let data: [DhikrBundle]?
}

internal struct DoaCategoriesResponse: Codable, Sendable {
    internal let success: Bool?
    internal let message: String?
    internal let data: [DoaCategory]?
}

internal enum DoaCatalogKind: String, Codable, Sendable {
    case doa = "DOA"
    case dhikr = "DHIKR"
}

internal struct DoaCatalogEntry: Codable, Hashable, Sendable, Identifiable {
    internal var id: String { slug }
    internal let slug: String
    internal let title: String
    internal let kind: DoaCatalogKind
}
