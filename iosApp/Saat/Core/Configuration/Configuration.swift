//
//  Configuration.swift
//  Sāat
//
//  Created by Sufiandy Elmy on 09/10/2026.
//  Copyright © 2026 Elmee. All rights reserved.
//

import Foundation

struct AppConfiguration: Sendable {
    /// Shared app group used by the main app and the Live Activity extension.
    let appGroupIdentifier: String

    init(appGroupIdentifier: String = "group.co.kamy.Saat") {
        self.appGroupIdentifier = appGroupIdentifier
    }
}
