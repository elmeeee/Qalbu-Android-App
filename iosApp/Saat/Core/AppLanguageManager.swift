//
//  AppLanguageManager.swift
//  Sāat
//
//  Created by Sufiandy Elmy on 09/10/2026.
//  Copyright © 2026 Elmee. All rights reserved.
//

import Foundation
import Combine
import shared

enum AppLanguage: String, CaseIterable, Identifiable {
    case english = "en"
    case indonesian = "id"
    case malay = "ms"
    
    var id: String { rawValue }
    var localeIdentifier: String { rawValue }
    
    var displayName: String {
        switch self {
        case .english: return "English"
        case .indonesian: return "Bahasa Indonesia"
        case .malay: return "Bahasa Melayu"
        }
    }
}

extension Notification.Name {
    static let appLanguageDidChange = Notification.Name("appLanguageDidChange")
}

@MainActor
class AppLanguageManager: ObservableObject {
    static let shared = AppLanguageManager()
    static let storageKey = "selected_app_language"
    
    @Published var currentLanguage: AppLanguage = .indonesian {
        didSet {
            UserDefaults.standard.set(currentLanguage.rawValue, forKey: Self.storageKey)
            NotificationCenter.default.post(name: .appLanguageDidChange, object: nil)
        }
    }
    
    private init() {
        if let raw = UserDefaults.standard.string(forKey: Self.storageKey),
           let lang = AppLanguage(rawValue: raw) {
            currentLanguage = lang
        } else {
            let localeLang = Locale.preferredLanguages.first ?? "id"
            if localeLang.hasPrefix("ms") {
                currentLanguage = .malay
            } else if localeLang.hasPrefix("en") {
                currentLanguage = .english
            } else {
                currentLanguage = .indonesian
            }
        }
    }
    
    func localize(_ key: String) -> String {
        return SharedStrings.shared.get(key: key, langCode: currentLanguage.rawValue)
    }
    
    func localizeFormatted(_ key: String, _ args: CVarArg...) -> String {
        let format = localize(key)
        let convertedArgs: [CVarArg] = args.map { arg in
            if let str = arg as? String {
                return str as NSString
            }
            return arg
        }
        return String(format: format, arguments: convertedArgs)
    }
}
