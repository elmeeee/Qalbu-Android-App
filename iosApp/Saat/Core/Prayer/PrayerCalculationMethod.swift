//
//  PrayerCalculationMethod.swift
//  Sāat
//
//  Created by Elmee on 25/04/2026.
//  Copyright © 2026 Elmee. All rights reserved.
//

import Foundation

enum PrayerCalculationMethod: String, CaseIterable, Sendable, Identifiable, Codable {
    // Southeast Asia (featured)
    case muhammadiyah
    case kemenag
    case muis
    case jakim
    case brunei
    case karachi
    case tehran
    case jafari
    case isna
    case mwl
    case ummAlQura
    case egyptian
    case mcw
    case gulf
    case kuwait
    case qatar
    case dubai
    case tunisia
    case algeria
    case morocco
    case jordan
    case france
    case turkey
    case russia
    case lisbon

    static let storageKey = "prayer_calculation_method"
    static let defaultMethod: PrayerCalculationMethod = .muhammadiyah

    var id: String { rawValue }

    enum Section: String, CaseIterable, Identifiable, Sendable {
        case southeastAsia = "Southeast Asia"
        case southAsia = "South Asia"
        case global = "Global standards"
        case middleEast = "Middle East & North Africa"
        case europe = "Europe"

        var id: String { rawValue }

        var methods: [PrayerCalculationMethod] {
            PrayerCalculationMethod.allCases.filter { $0.section == self }
        }
    }

    var section: Section {
        switch self {
        case .muhammadiyah, .kemenag, .muis, .jakim, .brunei:
            return .southeastAsia
        case .karachi, .tehran:
            return .southAsia
        case .jafari, .isna, .mwl, .ummAlQura, .egyptian, .mcw:
            return .global
        case .gulf, .kuwait, .qatar, .dubai, .tunisia, .algeria, .morocco, .jordan:
            return .middleEast
        case .france, .turkey, .russia, .lisbon:
            return .europe
        }
    }

    var displayName: String {
        switch self {
        case .muhammadiyah: "Muhammadiyah"
        case .kemenag: "Ministry of Religious Affairs (Kemenag)"
        case .muis: "Majlis Ugama Islam Singapura (MUIS)"
        case .jakim: "Jabatan Kemajuan Islam Malaysia (JAKIM)"
        case .brunei: "Majlis Ugama Islam Brunei (MUIB)"
        case .karachi: "Karachi"
        case .tehran: "Tehran"
        case .jafari: "Jafari"
        case .isna: "ISNA"
        case .mwl: "Muslim World League"
        case .ummAlQura: "Umm Al-Qura"
        case .egyptian: "Egyptian"
        case .mcw: "Moonsighting Committee"
        case .gulf: "Gulf Region"
        case .kuwait: "Kuwait"
        case .qatar: "Qatar"
        case .dubai: "Dubai"
        case .tunisia: "Tunisia"
        case .algeria: "Algeria"
        case .morocco: "Morocco"
        case .jordan: "Jordan"
        case .france: "UOIF France"
        case .turkey: "Diyanet Turkey"
        case .russia: "Russia"
        case .lisbon: "Lisbon"
        }
    }

    var organization: String {
        switch self {
        case .muhammadiyah: "Persyarikatan Muhammadiyah"
        case .kemenag: "Ministry of Religious Affairs (Kemenag)"
        case .muis: "Majlis Ugama Islam Singapura"
        case .jakim: "Jabatan Kemajuan Islam Malaysia"
        case .brunei: "Majlis Ugama Islam Brunei"
        case .karachi: "University of Islamic Sciences, Karachi"
        case .tehran: "Institute of Geophysics, University of Tehran"
        case .jafari: "Shia Ithna-Ashari (Leva Institute, Qom)"
        case .isna: "Islamic Society of North America"
        case .mwl: "Muslim World League"
        case .ummAlQura: "Umm Al-Qura University, Makkah"
        case .egyptian: "Egyptian General Authority of Survey"
        case .mcw: "Moonsighting Committee Worldwide"
        case .gulf: "Gulf Region"
        case .kuwait: "Kuwait"
        case .qatar: "Qatar"
        case .dubai: "Dubai (experimental)"
        case .tunisia: "Tunisia"
        case .algeria: "Algeria"
        case .morocco: "Morocco"
        case .jordan: "Ministry of Awqaf, Jordan"
        case .france: "Union Organization islamic de France"
        case .turkey: "Diyanet İşleri Başkanlığı"
        case .russia: "Spiritual Administration of Muslims of Russia"
        case .lisbon: "Comunidade Islamica de Lisboa"
        }
    }

    var iconName: String {
        switch self {
        case .muhammadiyah: return "institution_muhammadiyah"
        case .kemenag: return "institution_kemenag"
        case .muis: return "institution_muis"
        case .jakim: return "institution_jakim"
        case .brunei: return "institution_muib"
        case .karachi: return "institution_karachi"
        case .tehran: return "institution_tehran"
        case .jafari: return "institution_jafari"
        case .isna: return "institution_isna"
        case .mwl: return "institution_mwl"
        case .ummAlQura: return "institution_umm_al_qura"
        case .egyptian: return "institution_egyptian"
        case .mcw: return "institution_mcw"
        case .gulf: return "institution_gulf"
        case .kuwait: return "institution_kuwait"
        case .qatar: return "institution_qatar"
        case .dubai: return "institution_dubai"
        case .tunisia: return "institution_tunisia"
        case .algeria: return "institution_algeria"
        case .morocco: return "institution_morocco"
        case .jordan: return "institution_jordan"
        case .france: return "institution_france"
        case .turkey: return "institution_turkey"
        case .russia: return "institution_russia"
        case .lisbon: return "institution_lisbon"
        }
    }

    var countryName: String {
        switch self {
        case .muhammadiyah, .kemenag: return "Indonesia"
        case .muis: return "Singapura"
        case .jakim: return "Malaysia"
        case .brunei: return "Brunei Darussalam"
        case .karachi: return "Pakistan"
        case .tehran, .jafari: return "Iran"
        case .isna: return "Amerika Utara"
        case .mwl, .ummAlQura: return "Arab Saudi"
        case .egyptian: return "Mesir"
        case .mcw: return "Global"
        case .gulf: return "Wilayah Teluk"
        case .kuwait: return "Kuwait"
        case .qatar: return "Qatar"
        case .dubai: return "Uni Emirat Arab"
        case .tunisia: return "Tunisia"
        case .algeria: return "Aljazair"
        case .morocco: return "Maroko"
        case .jordan: return "Yordania"
        case .france: return "Prancis"
        case .turkey: return "Turki"
        case .russia: return "Rusia"
        case .lisbon: return "Portugal"
        }
    }

    var region: String {
        switch section {
        case .southeastAsia: "Southeast Asia"
        case .southAsia: "South Asia"
        case .global: "Worldwide"
        case .middleEast: "MENA & Gulf"
        case .europe: "Europe"
        }
    }

    var subtitle: String {
        "\(displayName) · \(organization)"
    }

    var pickerSubtitle: String {
        organization
    }

    static var hasSavedPreference: Bool {
        UserDefaults.standard.string(forKey: storageKey) != nil
    }

    static func savedOrDefault() -> PrayerCalculationMethod {
        guard let raw = UserDefaults.standard.string(forKey: storageKey),
              let method = PrayerCalculationMethod(rawValue: raw) else {
            return defaultMethod
        }
        return method
    }

    func persist(notify: Bool = true) {
        UserDefaults.standard.set(rawValue, forKey: Self.storageKey)
        if notify {
            NotificationCenter.default.post(name: .prayerCalculationMethodDidChange, object: nil)
        }
    }

    static func forCountryCode(_ code: String) -> PrayerCalculationMethod {
        switch code.uppercased() {
        case "ID": return .muhammadiyah
        case "SG": return .muis
        case "MY": return .jakim
        case "BN": return .brunei
        case "PK", "IN", "BD": return .karachi
        case "SA": return .ummAlQura
        case "EG": return .egyptian
        case "TR": return .turkey
        case "FR": return .france
        case "US", "CA": return .isna
        case "AE": return .dubai
        case "KW": return .kuwait
        case "QA": return .qatar
        case "MA": return .morocco
        case "DZ": return .algeria
        case "TN": return .tunisia
        case "JO": return .jordan
        case "IR": return .tehran
        case "RU": return .russia
        case "PT": return .lisbon
        default: return .mwl
        }
    }

    var aladhanTune: String {
        switch self {
        case .muhammadiyah: "0,2,-1,1,1,3,0,2,0"
        case .kemenag: "0,0,-1,1,1,3,0,2,0"
        case .muis, .jakim, .brunei: "0,0,0,0,0,0,0,0,0"
        default: "0,0,0,0,0,0,0,0,0"
        }
    }
}

extension Notification.Name {
    static let prayerCalculationMethodDidChange = Notification.Name("prayerCalculationMethodDidChange")
}
