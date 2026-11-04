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
        /// Host for recitation audio referenced by relative paths in the local database.
        static let versesWebBase: String = "https://verses.quran.com"
    }

    enum URLBuilder {
        static func absoluteVerseMediaURLString(from raw: String) -> String {
            if raw.hasPrefix("//") { return "https:\(raw)" }
            if raw.hasPrefix("http") { return raw }
            return "\(External.versesWebBase)/\(raw)"
        }
    }
}
