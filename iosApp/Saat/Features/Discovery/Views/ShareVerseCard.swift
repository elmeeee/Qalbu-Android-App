//
//  ShareVerseCard.swift
//  Saat
//
//  Created by Elmee on 25/04/2026.
//  Copyright © 2026 Elmee. All rights reserved.
//

import Foundation

struct ShareVerseCard {
    @MainActor
    static func humanLabel(for verseKey: String) -> String {
        VerseKeyFormat.humanLabel(for: verseKey)
    }
}
