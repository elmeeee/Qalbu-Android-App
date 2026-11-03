//
//  ReadingSessionModels.swift
//  Saat
//
//  Created by Elmee on 25/04/2026.
//  Copyright © 2026 Elmee. All rights reserved.
//

import Foundation

struct ReadingSession: Decodable, Sendable, Identifiable, Hashable {
    let id: String
    let updatedAt: String?
    let chapterNumber: Int
    let verseNumber: Int
}
