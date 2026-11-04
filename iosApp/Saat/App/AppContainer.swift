//
//  AppContainer.swift
//  Saat
//
//  Created by Elmee on 25/04/2026.
//  Copyright © 2026 Elmee. All rights reserved.
//

import Foundation
import SwiftUI

private struct AppContainerKey: EnvironmentKey {
    nonisolated(unsafe) static var defaultValue: AppContainer? = nil
}

extension EnvironmentValues {
    var appContainer: AppContainer? {
        get { self[AppContainerKey.self] }
        set { self[AppContainerKey.self] = newValue }
    }
}

@MainActor
final class AppContainer {
    let configuration: AppConfiguration
    let content: QuranContentRepository
    let readingSessions: ReadingSessionRepository

    init() {
        self.configuration = AppConfiguration()
        self.content = QuranContentRepository()
        self.readingSessions = ReadingSessionRepository()
    }

    func warmChapterCatalog() {
        Task(priority: .utility) {
            _ = try? await content.getChapters()
        }
    }
}
