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
    
    func localizeFormatted(_ key: String, _ args: Any...) -> String {
        let format = localize(key)
        return formatString(format, with: args)
    }
    
    func formatString(_ format: String, with args: [Any]) -> String {
        guard !args.isEmpty else { return format }
        var result = format
        
        // 1. Replace positional specifiers: %1$s, %1$d, %1$f, %1$@, %1$ld, etc.
        for (index, arg) in args.enumerated() {
            let position = index + 1
            let argString = String(describing: arg)
            let specifiers = [
                "%\\(position)$s", "%\\(position)$d", "%\\(position)$f", 
                "%\\(position)$@", "%\\(position)$ld", "%\\(position)$lf"
            ]
            for spec in specifiers {
                result = result.replacingOccurrences(of: spec, with: argString)
            }
        }
        
        // 2. Sequential specifiers (%s, %d, %@, %f, etc.) replaced in order
        let regexPattern = #"%(?:[0-9]+\$)?(@|s|d|f|ld|lf)"#
        if let regex = try? NSRegularExpression(pattern: regexPattern, options: []) {
            var argIndex = 0
            while argIndex < args.count {
                let range = NSRange(result.startIndex..<result.endIndex, in: result)
                guard let match = regex.firstMatch(in: result, options: [], range: range),
                      let matchRange = Range(match.range, in: result) else {
                    break
                }
                let argString = String(describing: args[argIndex])
                result.replaceSubrange(matchRange, with: argString)
                argIndex += 1
            }
        }
        
        return result
    }
}
