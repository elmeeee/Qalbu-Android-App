//
//  AppContainer.swift
//  Sāat
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
    let environment: AppEnvironment
    let configuration: AppConfiguration
    let content: QuranContentRepository
    let habits: UserHabitRepository
    let readingSessions: ReadingSessionRepository
    let reflect: ReflectRepository
    let reflectionStore: ReflectionStore

    init() {
        self.environment = .production
        self.configuration = environment.configuration
        self.content = QuranContentRepository()
        self.habits = UserHabitRepository()
        self.readingSessions = ReadingSessionRepository()
        self.reflect = ReflectRepository(habits: habits)
        self.reflectionStore = ReflectionStore(appGroupIdentifier: configuration.appGroupIdentifier)
    }

    func clearUserSession() async {
        await APICache.clearAll()
        reflectionStore.removeAll()
    }

    func invalidateUserSession() {
        Task { @MainActor in
            await clearUserSession()
        }
    }

    func signOut() async {
        await clearUserSession()
    }

    func warmReflectDataIfSignedIn() {
        // Local offline mode
    }

    func warmUserProfileIfSignedIn() {
        warmReflectDataIfSignedIn()
    }

    func warmChapterCatalog() {
        Task(priority: .utility) {
            _ = try? await content.getChapters()
        }
    }
}
