#!/usr/bin/env python3
import xml.etree.ElementTree as ET
import os

def parse_xml(path):
    if not os.path.exists(path):
        return {}
    try:
        tree = ET.parse(path)
        root = tree.getroot()
        res = {}
        for item in root.findall('string'):
            name = item.get('name')
            text = ''.join(item.itertext())
            if name and text:
                # clean escaped chars
                text = text.replace("\\'", "'").replace('\\"', '"')
                res[name] = text
        return res
    except Exception as e:
        print(f"Error reading {path}: {e}")
        return {}

def escape_kotlin(s):
    return s.replace('\\', '\\\\').replace('"', '\\"').replace('\n', '\\n').replace('$', '\\$')

def escape_strings_file(s):
    return s.replace('\\', '\\\\').replace('"', '\\"').replace('\n', '\\n')

base_dir = os.path.abspath(os.path.join(os.path.dirname(__file__), '../app/src/main/res'))

# English (default values)
en_strings = parse_xml(os.path.join(base_dir, 'values/strings.xml'))
en_strings.update(parse_xml(os.path.join(base_dir, 'values/share_format_strings.xml')))

# Indonesian (values-in)
id_strings = parse_xml(os.path.join(base_dir, 'values-in/strings.xml'))
id_strings.update(parse_xml(os.path.join(base_dir, 'values-in/strings_features.xml')))
id_strings.update(parse_xml(os.path.join(base_dir, 'values-in/share_format_strings.xml')))

# Malay (values-ms)
ms_strings = parse_xml(os.path.join(base_dir, 'values-ms/strings.xml'))
ms_strings.update(parse_xml(os.path.join(base_dir, 'values-ms/strings_features.xml')))
ms_strings.update(parse_xml(os.path.join(base_dir, 'values-ms/share_format_strings.xml')))

all_keys = sorted(list(set(list(en_strings.keys()) + list(id_strings.keys()) + list(ms_strings.keys()))))
print(f"Total unique keys: {len(all_keys)}")
print(f"EN: {len(en_strings)}, ID: {len(id_strings)}, MS: {len(ms_strings)}")

# 1. KMP Shared Strings
out_kmp_dir = os.path.abspath(os.path.join(os.path.dirname(__file__), '../shared/src/commonMain/kotlin/app/kamy/saatApp/shared/localization'))
os.makedirs(out_kmp_dir, exist_ok=True)

lang_kt = """package app.kamy.saatApp.shared.localization

enum class SharedLanguage(val code: String, val displayName: String) {
    INDONESIAN("id", "Bahasa Indonesia"),
    MALAY("ms", "Bahasa Melayu"),
    ENGLISH("en", "English");

    companion object {
        fun fromCode(code: String): SharedLanguage = when (code.lowercase()) {
            "id", "in", "indonesia", "indonesian" -> INDONESIAN
            "ms", "my", "melayu", "malay" -> MALAY
            else -> ENGLISH
        }
    }
}
"""

with open(os.path.join(out_kmp_dir, 'SharedLanguage.kt'), 'w', encoding='utf-8') as f:
    f.write(lang_kt)

kt_content = """package app.kamy.saatApp.shared.localization

object SharedStrings {

    fun get(key: String, language: SharedLanguage = SharedLanguage.INDONESIAN): String {
        val langMap = when (language) {
            SharedLanguage.INDONESIAN -> idStrings
            SharedLanguage.MALAY -> msStrings
            SharedLanguage.ENGLISH -> enStrings
        }
        return langMap[key] ?: idStrings[key] ?: enStrings[key] ?: key
    }

    fun get(key: String, langCode: String): String {
        return get(key, SharedLanguage.fromCode(langCode))
    }

    private val idStrings: Map<String, String> by lazy {
        val map = HashMap<String, String>(""" + str(len(all_keys)) + """)\n"""

for k in all_keys:
    val = id_strings.get(k, en_strings.get(k, ""))
    if val:
        kt_content += f'        map["{k}"] = "{escape_kotlin(val)}"\n'

kt_content += """        map
    }

    private val msStrings: Map<String, String> by lazy {
        val map = HashMap<String, String>(""" + str(len(all_keys)) + """)\n"""

for k in all_keys:
    val = ms_strings.get(k, id_strings.get(k, en_strings.get(k, "")))
    if val:
        kt_content += f'        map["{k}"] = "{escape_kotlin(val)}"\n'

kt_content += """        map
    }

    private val enStrings: Map<String, String> by lazy {
        val map = HashMap<String, String>(""" + str(len(all_keys)) + """)\n"""

for k in all_keys:
    val = en_strings.get(k, id_strings.get(k, ""))
    if val:
        kt_content += f'        map["{k}"] = "{escape_kotlin(val)}"\n'

kt_content += """        map
    }
}
"""

with open(os.path.join(out_kmp_dir, 'SharedStrings.kt'), 'w', encoding='utf-8') as f:
    f.write(kt_content)

# 2. iOS Localizable.strings
ios_res_dir = os.path.abspath(os.path.join(os.path.dirname(__file__), '../iosApp/Saat/Resources'))
os.makedirs(os.path.join(ios_res_dir, 'en.lproj'), exist_ok=True)
os.makedirs(os.path.join(ios_res_dir, 'id.lproj'), exist_ok=True)
os.makedirs(os.path.join(ios_res_dir, 'ms.lproj'), exist_ok=True)

def write_strings_file(path, data, fallback):
    with open(path, 'w', encoding='utf-8') as f:
        for k in all_keys:
            v = data.get(k, fallback.get(k, ""))
            f.write(f'"{k}" = "{escape_strings_file(v)}";\n')

write_strings_file(os.path.join(ios_res_dir, 'id.lproj/Localizable.strings'), id_strings, en_strings)
write_strings_file(os.path.join(ios_res_dir, 'ms.lproj/Localizable.strings'), ms_strings, id_strings)
write_strings_file(os.path.join(ios_res_dir, 'en.lproj/Localizable.strings'), en_strings, id_strings)

# 3. iOS AppLanguageManager.swift
swift_path = os.path.abspath(os.path.join(os.path.dirname(__file__), '../iosApp/Saat/Core/AppLanguageManager.swift'))
swift_content = """//
//  AppLanguageManager.swift
//  Sāat
//
//  Created by Sufiandy Elmy on 09/10/2026.
//  Copyright © 2026 Elmee. All rights reserved.
//

import Foundation
import Combine

enum AppLanguage: String, CaseIterable, Identifiable {
    case english = "en"
    case indonesian = "id"
    case malay = "ms"
    
    var id: String { rawValue }
    
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
        if let dict = translations[key], let val = dict[currentLanguage], !val.isEmpty {
            return val
        }
        if currentLanguage == .malay, let idVal = translations[key]?[.indonesian], !idVal.isEmpty {
            return idVal
        }
        return translations[key]?[.indonesian] ?? translations[key]?[.english] ?? key
    }
    
    private let translations: [String: [AppLanguage: String]] = [
"""

for k in all_keys:
    en_v = escape_strings_file(en_strings.get(k, id_strings.get(k, "")))
    id_v = escape_strings_file(id_strings.get(k, en_strings.get(k, "")))
    ms_v = escape_strings_file(ms_strings.get(k, id_strings.get(k, en_v)))
    swift_content += f'        "{k}": [.indonesian: "{id_v}", .english: "{en_v}", .malay: "{ms_v}"],\n'

swift_content += """    ]
}
"""

with open(swift_path, 'w', encoding='utf-8') as f:
    f.write(swift_content)

print(f"Successfully synchronized {len(all_keys)} keys across KMP SharedStrings, iOS AppLanguageManager & Localizable.strings!")
