//
//  ChapterReaderRoute.swift
//  Saat
//
//  Created by Elmee on 25/04/2026.
//  Copyright © 2026 Elmee. All rights reserved.
//

import Foundation

struct ChapterReaderRoute: Hashable, Identifiable {
    var id: String {
        "\(chapter?.id ?? 0)_\(juzNumber ?? 0)_\(initialVerseNumber ?? 0)"
    }
    var chapter: QuranChapter?
    var juzNumber: Int?
    var initialVerseNumber: Int?
}
