//
//  TajweedEngine.swift
//  Saat
//
//  Created by Elmee on 14/07/2026.
//  Copyright © 2026 Elmee. All rights reserved.
//

import SwiftUI

enum TajweedType: String, CaseIterable, Sendable, Identifiable {
    case ghunnah = "ghunnah"
    case idghamWithoutGhunnah = "idgham_wo_ghunnah"
    case idghamWithGhunnah = "idgham_ghunnah"
    case idghamMimi = "idgham_mimi"
    case iqlab = "iqlab"
    case ikhfa = "ikhafa"
    case ikhfaSyafawi = "ikhafa_shafawi"
    case qalqalah = "qalaqah"

    var id: String { rawValue }
}

struct TajweedResult: Sendable {
    let type: TajweedType
    let start: Int
    let end: Int
}

struct ArabicCharUtil {
    static let FATHA = 0x064E
    static let DAMMA = 0x064F
    static let KASRA = 0x0650
    static let FATHATAN = 0x064B
    static let DAMMATAN = 0x064C
    static let KASRATAN = 0x064D
    static let SUKUN = 0x0652
    static let SHADDA = 0x0651
    static let NUN = 0x0646
    static let MIM = 0x0645
    static let BAA = 0x0628
    static let QAAF = 0x0642
    static let THAA = 0x0637 // TAH (ط)
    static let JIMM = 0x062C
    static let DAAL = 0x062F
    static let YAA = 0x064A
    static let WAW = 0x0648
    static let RAA = 0x0631
    static let LAAM = 0x0644
    static let SMALL_MEEM = 0x06ED

    static func getCodePointAt(_ scalars: [Unicode.Scalar], _ index: Int) -> Int {
        guard index >= 0 && index < scalars.count else { return -1 }
        return Int(scalars[index].value)
    }

    static func isTanwin(_ codePoint: Int) -> Bool {
        return codePoint == FATHATAN || codePoint == DAMMATAN || codePoint == KASRATAN
    }

    static func isNunSukunOrTanwin(_ scalars: [Unicode.Scalar], _ index: Int) -> Bool {
        let cp = getCodePointAt(scalars, index)
        if isTanwin(cp) { return true }
        if cp == NUN {
            let nextCp = getCodePointAt(scalars, index + 1)
            if nextCp == SUKUN || isHarf(nextCp) || nextCp == SMALL_MEEM || nextCp == 0x20 || nextCp == SHADDA {
                return true
            }
        }
        return false
    }

    static func isMimSukun(_ scalars: [Unicode.Scalar], _ index: Int) -> Bool {
        if getCodePointAt(scalars, index) == MIM {
            let nextCp = getCodePointAt(scalars, index + 1)
            if nextCp == SUKUN || isHarf(nextCp) || nextCp == 0x20 {
                return true
            }
        }
        return false
    }

    static func isHarf(_ codePoint: Int) -> Bool {
        return (0x0621...0x064A).contains(codePoint)
    }

    static func isCombiningMark(_ codePoint: Int) -> Bool {
        return (0x064B...0x0652).contains(codePoint) ||
            codePoint == 0x0670 ||
            (0x06D6...0x06ED).contains(codePoint) ||
            codePoint == 0x06E3 ||
            codePoint == 0x06E5 ||
            codePoint == 0x06E6 ||
            codePoint == 0x06EA ||
            codePoint == 0x06EB ||
            codePoint == 0x06EC ||
            codePoint == 0x06DF ||
            codePoint == 0x06E0 ||
            codePoint == 0x06E1
    }

    static func getNextHarfIndex(_ scalars: [Unicode.Scalar], _ startIndex: Int) -> Int {
        var index = startIndex
        while index < scalars.count {
            if isHarf(getCodePointAt(scalars, index)) {
                return index
            }
            index += 1
        }
        return -1
    }
}

protocol TajweedRule: Sendable {
    func findTajweed(scalars: [Unicode.Scalar]) -> [TajweedResult]
}

struct IkhfaRule: TajweedRule {
    private let letters: Set<Int> = [0x062A, 0x062B, 0x062C, 0x062F, 0x0630, 0x0632, 0x0633, 0x0634, 0x0635, 0x0636, 0x0637, 0x0638, 0x0641, 0x0642, 0x0643]
    func findTajweed(scalars: [Unicode.Scalar]) -> [TajweedResult] {
        var results: [TajweedResult] = []
        for i in 0..<scalars.count {
            if ArabicCharUtil.isNunSukunOrTanwin(scalars, i) {
                let nextHarfIndex = ArabicCharUtil.getNextHarfIndex(scalars, i + 1)
                if nextHarfIndex != -1 && letters.contains(ArabicCharUtil.getCodePointAt(scalars, nextHarfIndex)) {
                    results.append(TajweedResult(type: .ikhfa, start: i, end: nextHarfIndex + 1))
                }
            }
        }
        return results
    }
}

struct IdghamBighunnahRule: TajweedRule {
    private let letters: Set<Int> = [ArabicCharUtil.YAA, ArabicCharUtil.NUN, ArabicCharUtil.MIM, ArabicCharUtil.WAW]
    func findTajweed(scalars: [Unicode.Scalar]) -> [TajweedResult] {
        var results: [TajweedResult] = []
        for i in 0..<scalars.count {
            if ArabicCharUtil.isNunSukunOrTanwin(scalars, i) {
                let nextHarfIndex = ArabicCharUtil.getNextHarfIndex(scalars, i + 1)
                if nextHarfIndex != -1 && letters.contains(ArabicCharUtil.getCodePointAt(scalars, nextHarfIndex)) {
                    results.append(TajweedResult(type: .idghamWithGhunnah, start: i, end: nextHarfIndex + 1))
                }
            }
        }
        return results
    }
}

struct IdghamBilaghunnahRule: TajweedRule {
    private let letters: Set<Int> = [ArabicCharUtil.LAAM, ArabicCharUtil.RAA]
    func findTajweed(scalars: [Unicode.Scalar]) -> [TajweedResult] {
        var results: [TajweedResult] = []
        for i in 0..<scalars.count {
            if ArabicCharUtil.isNunSukunOrTanwin(scalars, i) {
                let nextHarfIndex = ArabicCharUtil.getNextHarfIndex(scalars, i + 1)
                if nextHarfIndex != -1 && letters.contains(ArabicCharUtil.getCodePointAt(scalars, nextHarfIndex)) {
                    results.append(TajweedResult(type: .idghamWithoutGhunnah, start: i, end: nextHarfIndex + 1))
                }
            }
        }
        return results
    }
}

struct IqlabRule: TajweedRule {
    private let letters: Set<Int> = [ArabicCharUtil.BAA]
    func findTajweed(scalars: [Unicode.Scalar]) -> [TajweedResult] {
        var results: [TajweedResult] = []
        for i in 0..<scalars.count {
            let cp = ArabicCharUtil.getCodePointAt(scalars, i)
            if ArabicCharUtil.isNunSukunOrTanwin(scalars, i) || cp == ArabicCharUtil.SMALL_MEEM {
                let nextHarfIndex = ArabicCharUtil.getNextHarfIndex(scalars, i + 1)
                if nextHarfIndex != -1 && letters.contains(ArabicCharUtil.getCodePointAt(scalars, nextHarfIndex)) {
                    results.append(TajweedResult(type: .iqlab, start: i, end: nextHarfIndex + 1))
                }
            }
        }
        return results
    }
}

struct IkhfaSyafawiRule: TajweedRule {
    private let letters: Set<Int> = [ArabicCharUtil.BAA]
    func findTajweed(scalars: [Unicode.Scalar]) -> [TajweedResult] {
        var results: [TajweedResult] = []
        for i in 0..<scalars.count {
            if ArabicCharUtil.isMimSukun(scalars, i) {
                let nextHarfIndex = ArabicCharUtil.getNextHarfIndex(scalars, i + 1)
                if nextHarfIndex != -1 && letters.contains(ArabicCharUtil.getCodePointAt(scalars, nextHarfIndex)) {
                    results.append(TajweedResult(type: .ikhfaSyafawi, start: i, end: nextHarfIndex + 1))
                }
            }
        }
        return results
    }
}

struct IdghamMimiRule: TajweedRule {
    private let letters: Set<Int> = [ArabicCharUtil.MIM]
    func findTajweed(scalars: [Unicode.Scalar]) -> [TajweedResult] {
        var results: [TajweedResult] = []
        for i in 0..<scalars.count {
            if ArabicCharUtil.isMimSukun(scalars, i) {
                let nextHarfIndex = ArabicCharUtil.getNextHarfIndex(scalars, i + 1)
                if nextHarfIndex != -1 && letters.contains(ArabicCharUtil.getCodePointAt(scalars, nextHarfIndex)) {
                    results.append(TajweedResult(type: .idghamMimi, start: i, end: nextHarfIndex + 1))
                }
            }
        }
        return results
    }
}

struct GhunnahRule: TajweedRule {
    func findTajweed(scalars: [Unicode.Scalar]) -> [TajweedResult] {
        var results: [TajweedResult] = []
        for i in 0..<scalars.count {
            let cp = ArabicCharUtil.getCodePointAt(scalars, i)
            if cp == ArabicCharUtil.NUN || cp == ArabicCharUtil.MIM {
                var nextIndex = i + 1
                var hasShadda = false
                while nextIndex < scalars.count && !ArabicCharUtil.isHarf(ArabicCharUtil.getCodePointAt(scalars, nextIndex)) {
                    if ArabicCharUtil.getCodePointAt(scalars, nextIndex) == ArabicCharUtil.SHADDA {
                        hasShadda = true
                        break
                    }
                    nextIndex += 1
                }
                if hasShadda {
                    results.append(TajweedResult(type: .ghunnah, start: i, end: nextIndex + 1))
                }
            }
        }
        return results
    }
}

struct QalqalahRule: TajweedRule {
    private let letters: Set<Int> = [ArabicCharUtil.QAAF, ArabicCharUtil.THAA, ArabicCharUtil.BAA, ArabicCharUtil.JIMM, ArabicCharUtil.DAAL]
    func findTajweed(scalars: [Unicode.Scalar]) -> [TajweedResult] {
        var results: [TajweedResult] = []
        for i in 0..<scalars.count {
            if letters.contains(ArabicCharUtil.getCodePointAt(scalars, i)) {
                let nextCp = ArabicCharUtil.getCodePointAt(scalars, i + 1)
                if nextCp == ArabicCharUtil.SUKUN || nextCp == -1 || nextCp == 0x20 {
                    results.append(TajweedResult(type: .qalqalah, start: i, end: i + 2))
                }
            }
        }
        return results
    }
}

struct TajweedEngine {
    private static let rules: [TajweedRule] = [
        IkhfaRule(),
        QalqalahRule(),
        GhunnahRule(),
        IdghamBighunnahRule(),
        IdghamBilaghunnahRule(),
        IqlabRule(),
        IdghamMimiRule(),
        IkhfaSyafawiRule()
    ]

    static func applyTajweed(_ text: String) -> [TajweedResult] {
        let scalars = Array(text.unicodeScalars)
        var allResults: [TajweedResult] = []
        for rule in rules {
            allResults.append(contentsOf: rule.findTajweed(scalars: scalars))
        }
        return allResults
    }

    private static func adjustSpanRange(_ text: String, start: Int, end: Int) -> (Int, Int) {
        let scalars = Array(text.unicodeScalars)
        var adjustedStart = start
        while adjustedStart > 0 && ArabicCharUtil.isCombiningMark(ArabicCharUtil.getCodePointAt(scalars, adjustedStart)) {
            adjustedStart -= 1
        }
        var adjustedEnd = end
        while adjustedEnd < scalars.count && ArabicCharUtil.isCombiningMark(ArabicCharUtil.getCodePointAt(scalars, adjustedEnd)) {
            adjustedEnd += 1
        }
        return (adjustedStart, adjustedEnd)
    }

    static func applyTajweedToHTML(_ text: String) -> String {
        let results = applyTajweed(text)
        guard !results.isEmpty else { return text }

        let scalars = Array(text.unicodeScalars)
        var adjustedResults: [TajweedResult] = []
        for res in results {
            let (adjStart, adjEnd) = adjustSpanRange(text, start: res.start, end: res.end)
            if adjStart < adjEnd {
                adjustedResults.append(TajweedResult(type: res.type, start: adjStart, end: adjEnd))
            }
        }

        // Sort descending by start index to prevent offset shifting
        let sorted = adjustedResults.sorted { $0.start > $1.start }
        var resultScalars = scalars

        for res in sorted {
            guard res.start >= 0 && res.end <= resultScalars.count && res.start < res.end else { continue }
            let spanContent = String(String.UnicodeScalarView(resultScalars[res.start..<res.end]))
            let wrapped = "<span class=\"\(res.type.rawValue)\">\(spanContent)</span>"
            
            // Convert wrapped string back to scalars
            let wrappedScalars = Array(wrapped.unicodeScalars)
            resultScalars.replaceSubrange(res.start..<res.end, with: wrappedScalars)
        }

        return String(String.UnicodeScalarView(resultScalars))
    }
}

// MARK: - Tajweed Details

struct TajweedExample: Sendable, Hashable, Identifiable {
    var id: String { arabic + "_" + transliteration }
    let arabic: String
    let transliteration: String
    let explanation: String
}

struct TajweedDetail: Sendable {
    let type: TajweedType
    let title: String
    let description: String
    let letters: String
    let howToRead: String
    let color: Color
    let examples: [TajweedExample]
}

struct TajweedDetailProvider {
    static func getDetail(type: TajweedType, languageCode: String) -> TajweedDetail {
        let code = languageCode.lowercased()
        if code.hasPrefix("id") || code.hasPrefix("in") {
            return getIndonesianDetail(type)
        } else if code.hasPrefix("ms") {
            return getMalayDetail(type)
        } else {
            return getEnglishDetail(type)
        }
    }

    private static func getIndonesianDetail(_ type: TajweedType) -> TajweedDetail {
        switch type {
        case .ghunnah:
            return TajweedDetail(
                type: type,
                title: "Ghunnah",
                description: "Membaca huruf dengan berdengung karena adanya huruf Nun (ن) atau Mim (م) yang memiliki tanda tasydid (ّ).",
                letters: "نّ   مّ",
                howToRead: "Suara ditahan masuk ke rongga hidung selama 2 harakat (ketukan) dengan dengung yang jelas sebelum masuk ke harakat berikutnya.",
                color: Color(red: 1.0, green: 0.49, blue: 0.12),
                examples: [
                    TajweedExample(arabic: "إِنَّ", transliteration: "Inna", explanation: "Terdapat Nun bertasydid, suara berdengung ditahan selama 2 harakat."),
                    TajweedExample(arabic: "عَمَّ", transliteration: "'Amma", explanation: "Terdapat Mim bertasydid, suara berdengung ditahan selama 2 harakat."),
                    TajweedExample(arabic: "فِي الْجِنَّةِ وَالنَّاسِ", transliteration: "Fil jinnati wan-nās", explanation: "Dengung ditahan pada kata Al-Jinnati (نّ) dan An-Nās (نّ).")
                ]
            )
        case .qalqalah:
            return TajweedDetail(
                type: type,
                title: "Qalqalah",
                description: "Memantulkan bunyi huruf apabila huruf tersebut sukun (mati asli) atau diwaqafkan (dihentikan di akhir kalimat).",
                letters: "ق   ط   ب   ج   د",
                howToRead: "Huruf dipantulkan dengan jelas. Jika di tengah kata pantulannya ringan (Sughra). Jika di akhir kalimat atau karena waqaf pantulannya lebih kuat (Kubra).",
                color: Color(red: 0.87, green: 0.0, blue: 0.03),
                examples: [
                    TajweedExample(arabic: "قُلْ هُوَ اللَّهُ أَحَدٌ", transliteration: "Qul huwallāhu aḥad", explanation: "Pantulan kuat (Kubra) pada huruf Dal (د) di akhir ayat karena waqaf."),
                    TajweedExample(arabic: "يَجْعَلُونَ", transliteration: "Yaj'alūna", explanation: "Pantulan ringan (Sughra) pada huruf Jim (جْ) sukun di tengah kata."),
                    TajweedExample(arabic: "فِي صَدْرِكَ", transliteration: "Fī ṣadrika", explanation: "Pantulan ringan (Sughra) pada huruf Dal (دْ) sukun di tengah kata.")
                ]
            )
        case .iqlab:
            return TajweedDetail(
                type: type,
                title: "Iqlab",
                description: "Mengubah bunyi huruf Nun sukun (نْ) atau Tanwin menjadi bunyi Mim (مْ) disertai dengung apabila bertemu huruf Ba (ب).",
                letters: "ب",
                howToRead: "Mengubah bunyi 'N' menjadi 'M' dengan merapatkan kedua bibir secara ringan (tanpa ditekan kuat) dan ditahan berdengung selama 2 harakat.",
                color: Color(red: 0.15, green: 0.75, blue: 0.99),
                examples: [
                    TajweedExample(arabic: "مِنْ بَعْدِ", transliteration: "Mim ba'di", explanation: "Nun sukun (نْ) bertemu Ba (ب), dibaca menjadi 'mim ba'di' dengan dengung."),
                    TajweedExample(arabic: "سَمِيعٌ Bَصِيرٌ", transliteration: "Samī'um baṣīr", explanation: "Tanwin dhummah bertemu Ba (ب), dibaca menjadi 'samī'um baṣīr' dengan dengung.")
                ]
            )
        case .ikhfa:
            return TajweedDetail(
                type: type,
                title: "Ikhfa Haqiqi",
                description: "Menyamarkan bunyi Nun sukun (نْ) atau Tanwin apabila bertemu salah satu dari 15 huruf Ikhfa.",
                letters: "ت   ث   ج   د   ذ   ز   س   ش   ص   ض   ط   ظ   ف   ق   ك",
                howToRead: "Bunyi Nun sukun atau Tanwin dibaca samar-samar menjurus ke makhraj huruf berikutnya, disertai dengan dengung yang ditahan selama 2 harakat.",
                color: Color(red: 0.58, green: 0.0, blue: 0.66),
                examples: [
                    TajweedExample(arabic: "مِنْ قَبْلِ", transliteration: "Ming qabli", explanation: "Nun sukun bertemu Qaf (ق), dibaca samar menjurus ke bunyi 'ng' disertai dengung."),
                    TajweedExample(arabic: "أَنْفُسَكُمْ", transliteration: "Angfusakum", explanation: "Nun sukun bertemu Fa (ف), dibaca samar menjurus ke bunyi 'f' disertai dengung.")
                ]
            )
        case .ikhfaSyafawi:
            return TajweedDetail(
                type: type,
                title: "Ikhfa Syafawi",
                description: "Menyamarkan bunyi Mim sukun (مْ) apabila bertemu dengan huruf Ba (ب).",
                letters: "ب",
                howToRead: "Bunyi Mim sukun disamarkan di bibir disertai dengung yang ditahan selama 2 harakat.",
                color: Color(red: 0.84, green: 0.0, blue: 0.72),
                examples: [
                    TajweedExample(arabic: "تَرْمِيهِمْ بِحِجَارَةٍ", transliteration: "Tarmīhim biḥijārah", explanation: "Mim sukun (مْ) bertemu Ba (ب), dibaca samar-samar dengan merapatkan bibir ringan disertai dengung.")
                ]
            )
        case .idghamWithGhunnah:
            return TajweedDetail(
                type: type,
                title: "Idgham Bighunnah",
                description: "Memasukkan bunyi Nun sukun (نْ) atau Tanwin ke dalam salah satu huruf Idgham Bighunnah disertai dengung.",
                letters: "ي   ن   م   و",
                howToRead: "Meleburkan bunyi Nun sukun atau Tanwin sepenuhnya ke dalam huruf berikutnya sehingga terdengar bertasydid, disertai dengung ditahan 2 harakat.",
                color: Color(red: 0.09, green: 0.57, blue: 0.0),
                examples: [
                    TajweedExample(arabic: "مَنْ يَقُولُ", transliteration: "May yaqūlu", explanation: "Nun sukun melebur ke huruf Ya (ي) dibaca 'mayyaqūlu' disertai dengung."),
                    TajweedExample(arabic: "مِنْ وَالٍ", transliteration: "Miw wālin", explanation: "Nun sukun melebur ke huruf Waw (و) dibaca 'miwwālin' disertai dengung.")
                ]
            )
        case .idghamWithoutGhunnah:
            return TajweedDetail(
                type: type,
                title: "Idgham Bilaghunnah",
                description: "Memasukkan bunyi Nun sukun (نْ) atau Tanwin ke dalam huruf Lam (ل) atau Ra (ر) tanpa disertai dengung.",
                letters: "ل   ر",
                howToRead: "Meleburkan bunyi Nun sukun atau Tanwin sepenuhnya ke dalam huruf Lam atau Ra tanpa dengung (langsung dibaca cepat tanpa ditahan).",
                color: Color(red: 0.63, green: 0.63, blue: 0.63),
                examples: [
                    TajweedExample(arabic: "مِنْ رَبِّهِمْ", transliteration: "Mir rabbihim", explanation: "Nun sukun melebur ke huruf Ra (ر) dibaca 'mir-rabbihim' tanpa ditahan berdengung."),
                    TajweedExample(arabic: "أَنْ لَمْ يَرَهُ", transliteration: "Al lam yarahu", explanation: "Nun sukun melebur ke huruf Lam (ل) dibaca 'al-lam yarahu' tanpa ditahan berdengung.")
                ]
            )
        case .idghamMimi:
            return TajweedDetail(
                type: type,
                title: "Idgham Mimi (Mutamasilain)",
                description: "Memasukkan bunyi Mim sukun (مْ) ke dalam huruf Mim (م) berikutnya disertai dengung.",
                letters: "م",
                howToRead: "Menggabungkan dua huruf Mim menjadi satu huruf Mim bertasydid, dibaca berdengung dan ditahan selama 2 harakat.",
                color: Color(red: 0.09, green: 0.57, blue: 0.0),
                examples: [
                    TajweedExample(arabic: "لَهُمْ مَا يَشَاءُونَ", transliteration: "Lahum mā yasyā'ūn", explanation: "Mim sukun melebur ke huruf Mim berikutnya dibaca berdengung ditahan 2 harakat.")
                ]
            )
        }
    }

    private static func getMalayDetail(_ type: TajweedType) -> TajweedDetail {
        switch type {
        case .ghunnah:
            return TajweedDetail(
                type: type,
                title: "Ghunnah",
                description: "Membaca huruf dengan berdengung kerana terdapat huruf Nun (ن) atau Mim (م) yang mempunyai tanda sabdu/tasydid (ّ).",
                letters: "نّ   مّ",
                howToRead: "Bunyi ditahan di dalam rongga hidung selama 2 harakat (ketukan) dengan dengung yang jelas sebelum menyebut baris seterusnya.",
                color: Color(red: 1.0, green: 0.49, blue: 0.12),
                examples: [
                    TajweedExample(arabic: "إِنَّ", transliteration: "Inna", explanation: "Nun bertasydid, bunyi berdengung ditahan selama 2 harakat."),
                    TajweedExample(arabic: "عَمَّ", transliteration: "'Amma", explanation: "Mim bertasydid, bunyi berdengung ditahan selama 2 harakat.")
                ]
            )
        case .qalqalah:
            return TajweedDetail(
                type: type,
                title: "Qalqalah",
                description: "Memantulkan bunyi huruf apabila huruf tersebut bertanda sukun (mati) atau diwaqafkan (dihentikan bacaan di akhir kalimah).",
                letters: "ق   ط   ب   ج   د",
                howToRead: "Huruf dipantulkan dengan jelas. Jika di tengah perkataan pantulannya adalah kecil/ringan (Sughra). Jika di akhir kalimah atau kerana waqaf pantulannya lebih besar/kuat (Kubra).",
                color: Color(red: 0.87, green: 0.0, blue: 0.03),
                examples: [
                    TajweedExample(arabic: "قُلْ هُوَ اللَّهُ أَحَدٌ", transliteration: "Qul huwallāhu aḥad", explanation: "Pantulan kuat (Kubra) pada huruf Dal (د) di akhir ayat kerana waqaf."),
                    TajweedExample(arabic: "يَجْعَلُونَ", transliteration: "Yaj'alūna", explanation: "Pantulan ringan (Sughra) pada huruf Jim (جْ) sukun di tengah perkataan.")
                ]
            )
        case .iqlab:
            return TajweedDetail(
                type: type,
                title: "Iqlab",
                description: "Menukarkan bunyi huruf Nun sukun (نْ) atau Tanwin menjadi bunyi Mim (مْ) berserta dengung apabila bertemu huruf Ba (ب).",
                letters: "ب",
                howToRead: "Menukarkan bunyi 'N' menjadi 'M' dengan merapatkan kedua-dua bibir secara ringan (tanpa ditekan kuat) dan ditahan berdengung selama 2 harakat.",
                color: Color(red: 0.15, green: 0.75, blue: 0.99),
                examples: [
                    TajweedExample(arabic: "مِنْ بَعْدِ", transliteration: "Mim ba'di", explanation: "Nun sukun (نْ) bertemu Ba (ب), dibaca menjadi 'mim ba'di' secara berdengung.")
                ]
            )
        case .ikhfa:
            return TajweedDetail(
                type: type,
                title: "Ikhfa Haqiqi",
                description: "Menyembunyikan/menyamarkan bunyi Nun sukun (نْ) atau Tanwin apabila bertemu salah satu daripada 15 huruf Ikhfa.",
                letters: "ت   ث   ج   د   ذ   ز   س   ش   ص   ض   ط   ظ   ف   ق   ك",
                howToRead: "Bunyi Nun sukun atau Tanwin dibaca samar-samar menghampiri sebutan huruf berikutnya, berserta dengan dengung yang ditahan selama 2 harakat.",
                color: Color(red: 0.58, green: 0.0, blue: 0.66),
                examples: [
                    TajweedExample(arabic: "مِنْ قَبْلِ", transliteration: "Ming qabli", explanation: "Nun sukun bertemu Qaf (ق), dibaca samar menghampiri bunyi 'ng' berserta dengung.")
                ]
            )
        case .ikhfaSyafawi:
            return TajweedDetail(
                type: type,
                title: "Ikhfa Syafawi",
                description: "Menyembunyikan/menyamarkan bunyi Mim sukun (مْ) apabila bertemu dengan huruf Ba (ب).",
                letters: "ب",
                howToRead: "Bunyi Mim sukun disamarkan di bibir berserta dengung yang ditahan selama 2 harakat.",
                color: Color(red: 0.84, green: 0.0, blue: 0.72),
                examples: [
                    TajweedExample(arabic: "تَرْمِيهِمْ بِحِجَارَةٍ", transliteration: "Tarmīhim biḥijārah", explanation: "Mim sukun (مْ) bertemu Ba (ب), dibaca samar-samar dengan merapatkan bibir secara ringan berserta dengung.")
                ]
            )
        case .idghamWithGhunnah:
            return TajweedDetail(
                type: type,
                title: "Idgham Bighunnah",
                description: "Memasukkan sebutan Nun sukun (نْ) atau Tanwin ke dalam salah satu huruf Idgham Bighunnah berserta dengung.",
                letters: "ي   ن   م   و",
                howToRead: "Meleburkan sebutan Nun sukun atau Tanwin sepenuhnya ke dalam huruf berikutnya sehingga terdengar seperti bertasydid/sabdu, berserta dengung ditahan selama 2 harakat.",
                color: Color(red: 0.09, green: 0.57, blue: 0.0),
                examples: [
                    TajweedExample(arabic: "مَنْ يَقُولُ", transliteration: "May yaqūlu", explanation: "Nun sukun melebur ke huruf Ya (ي) dibaca 'mayyaqūlu' berserta dengung.")
                ]
            )
        case .idghamWithoutGhunnah:
            return TajweedDetail(
                type: type,
                title: "Idgham Bilaghunnah",
                description: "Memasukkan sebutan Nun sukun (نْ) atau Tanwin ke dalam huruf Lam (ل) atau Ra (ر) tanpa berserta dengung.",
                letters: "ل   ر",
                howToRead: "Meleburkan sebutan Nun sukun atau Tanwin sepenuhnya ke dalam huruf Lam atau Ra tanpa dengung (langsung dibaca cepat tanpa ditahan dengungnya).",
                color: Color(red: 0.63, green: 0.63, blue: 0.63),
                examples: [
                    TajweedExample(arabic: "مِنْ رَبِّهِمْ", transliteration: "Mir rabbihim", explanation: "Nun sukun melebur ke huruf Ra (ر) dibaca 'mir-rabbihim' tanpa ditahan dengungnya.")
                ]
            )
        case .idghamMimi:
            return TajweedDetail(
                type: type,
                title: "Idgham Mimi (Mutamasilain)",
                description: "Memasukkan sebutan Mim sukun (مْ) ke dalam huruf Mim (م) berikutnya berserta dengung.",
                letters: "م",
                howToRead: "Menggabungkan dua huruf Mim menjadi satu sebutan Mim yang bersabdu/bertasydid, dibaca berdengung dan ditahan selama 2 harakat.",
                color: Color(red: 0.09, green: 0.57, blue: 0.0),
                examples: [
                    TajweedExample(arabic: "لَهُمْ مَا يَشَاءُونَ", transliteration: "Lahum mā yasyā'ūn", explanation: "Mim sukun melebur ke huruf Mim berikutnya dibaca berdengung ditahan selama 2 harakat.")
                ]
            )
        }
    }

    private static func getEnglishDetail(_ type: TajweedType) -> TajweedDetail {
        switch type {
        case .ghunnah:
            return TajweedDetail(
                type: type,
                title: "Ghunnah",
                description: "Pronouncing with a nasal sound because of a doubled (shaddah) Nun (ن) or Mim (م).",
                letters: "نّ   مّ",
                howToRead: "Hold the nasal sound through the nose for 2 counts (beats) before moving to the next vowel.",
                color: Color(red: 1.0, green: 0.49, blue: 0.12),
                examples: [
                    TajweedExample(arabic: "إِنَّ", transliteration: "Inna", explanation: "Doubled Nun, hold with nasal sound for 2 counts."),
                    TajweedExample(arabic: "عَمَّ", transliteration: "'Amma", explanation: "Doubled Mim, hold with nasal sound for 2 counts.")
                ]
            )
        case .qalqalah:
            return TajweedDetail(
                type: type,
                title: "Qalqalah",
                description: "Echoing or bouncing the sound of the letter when it has a sukoon (silent marker) or is stopped due to pausing (waqf).",
                letters: "ق   ط   ب   ج   د",
                howToRead: "Bounce the sound clearly. Light bounce (Sughra) in the middle of a word, strong bounce (Kubra) at the end of a verse due to stopping.",
                color: Color(red: 0.87, green: 0.0, blue: 0.03),
                examples: [
                    TajweedExample(arabic: "قُلْ هُوَ اللَّهُ أَحَدٌ", transliteration: "Qul huwallāhu aḥad", explanation: "Strong bounce (Kubra) on Dal (د) at the end of the verse."),
                    TajweedExample(arabic: "يَجْعَلُونَ", transliteration: "Yaj'alūna", explanation: "Light bounce (Sughra) on Jim (جْ) in the middle of the word.")
                ]
            )
        case .iqlab:
            return TajweedDetail(
                type: type,
                title: "Iqlab",
                description: "Converting the sound of Nun Sakinah (نْ) or Tanween into a Mim (مْ) with a nasal sound when followed by Ba (ب).",
                letters: "ب",
                howToRead: "Close the lips gently (without pressure), turning the 'N' sound into a hidden 'M' sound, and hold for 2 counts.",
                color: Color(red: 0.15, green: 0.75, blue: 0.99),
                examples: [
                    TajweedExample(arabic: "مِنْ بَعْدِ", transliteration: "Mim ba'di", explanation: "Nun Sakinah followed by Ba is pronounced as a hidden Mim with nasalization.")
                ]
            )
        case .ikhfa:
            return TajweedDetail(
                type: type,
                title: "Ikhfa Haqiqi",
                description: "Hiding the sound of Nun Sakinah (نْ) or Tanween when followed by one of the 15 Ikhfa letters.",
                letters: "ت   ث   ج   د   ذ   ز   س   ش   ص   ض   ط   ظ   ف   ق   ك",
                howToRead: "Partially hide the 'N' sound by preparing your mouth/tongue for the next letter, accompanied by a nasal sound held for 2 counts.",
                color: Color(red: 0.58, green: 0.0, blue: 0.66),
                examples: [
                    TajweedExample(arabic: "مِنْ قَبْلِ", transliteration: "Ming qabli", explanation: "Nun Sakinah followed by Qaf, nasalized and hidden towards Qaf.")
                ]
            )
        case .ikhfaSyafawi:
            return TajweedDetail(
                type: type,
                title: "Ikhfa Syafawi",
                description: "Hiding the Mim Sakinah (مْ) sound at the lips with nasalization when followed by Ba (ب).",
                letters: "ب",
                howToRead: "Pronounce the Mim lightly at the lips with a nasal sound, holding it for 2 counts.",
                color: Color(red: 0.84, green: 0.0, blue: 0.72),
                examples: [
                    TajweedExample(arabic: "تَرْمِيهِمْ بِحِجَARAH", transliteration: "Tarmīhim biḥijārah", explanation: "Mim Sakinah followed by Ba is hidden with a nasal sound.")
                ]
            )
        case .idghamWithGhunnah:
            return TajweedDetail(
                type: type,
                title: "Idgham Bighunnah",
                description: "Merging the sound of Nun Sakinah (نْ) or Tanween into one of the four letters of Idgham Bighunnah with nasalization.",
                letters: "ي   ن   م   و",
                howToRead: "Merge the 'N' sound completely into the next letter so it sounds doubled, and hold the nasal sound for 2 counts.",
                color: Color(red: 0.09, green: 0.57, blue: 0.0),
                examples: [
                    TajweedExample(arabic: "مَنْ يَقُولُ", transliteration: "May yaqūlu", explanation: "Nun Sakinah is merged into Ya, holding with nasal sound.")
                ]
            )
        case .idghamWithoutGhunnah:
            return TajweedDetail(
                type: type,
                title: "Idgham Bilaghunnah",
                description: "Merging the sound of Nun Sakinah (نْ) or Tanween into Lam (ل) or Ra (ر) without nasalization.",
                letters: "ل   ر",
                howToRead: "Merge the 'N' sound completely into Lam or Ra without holding or nasalizing.",
                color: Color(red: 0.63, green: 0.63, blue: 0.63),
                examples: [
                    TajweedExample(arabic: "مِنْ رَبِّهِمْ", transliteration: "Mir rabbihim", explanation: "Nun Sakinah merges into Ra with no nasal sound.")
                ]
            )
        case .idghamMimi:
            return TajweedDetail(
                type: type,
                title: "Idgham Mimi (Mutamasilayn)",
                description: "Merging Mim Sakinah (مْ) into the following Mim (م) with nasalization.",
                letters: "م",
                howToRead: "Combine the two Mim letters into one doubled Mim, holding it with a nasal sound for 2 counts.",
                color: Color(red: 0.09, green: 0.57, blue: 0.0),
                examples: [
                    TajweedExample(arabic: "لَهُمْ مَا يَشَاءُونَ", transliteration: "Lahum mā yasyā'ūn", explanation: "Mim Sakinah merges into the next Mim with a nasal sound.")
                ]
            )
        }
    }
}

// Extension to initialize color from hex
extension Color {
    init(hex: UInt32) {
        let r = Double((hex >> 16) & 0xff) / 255.0
        let g = Double((hex >> 8) & 0xff) / 255.0
        let b = Double(hex & 0xff) / 255.0
        self.init(red: r, green: g, blue: b)
    }
}

struct TajweedInfoSheet: View {
    let rule: TajweedType
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        let langCode = AppLanguageManager.shared.currentLanguage.rawValue
        let detail = TajweedDetailProvider.getDetail(type: rule, languageCode: langCode)
        
        let lettersHeader = AppLanguageManager.shared.currentLanguage == .english ? "Letters" : "Huruf"
        let howToReadHeader = AppLanguageManager.shared.currentLanguage == .english ? "How to Read" : "Cara Membaca"
        let examplesHeader = AppLanguageManager.shared.currentLanguage == .english ? "Examples" : "Contoh"
        
        VStack(spacing: 0) {
            // Drag indicator and close button header
            HStack {
                Spacer()
                Button {
                    dismiss()
                } label: {
                    Image(systemName: "xmark.circle.fill")
                        .font(.system(size: 24))
                        .foregroundColor(Color.Token.slate400)
                        .padding(.trailing, 16)
                        .padding(.top, 16)
                }
            }
            
            ScrollView {
                VStack(alignment: .leading, spacing: 20) {
                    // Header Card
                    HStack(spacing: 16) {
                        Circle()
                            .fill(detail.color)
                            .frame(width: 44, height: 44)
                            .overlay(
                                Image(systemName: "book.closed.fill")
                                    .font(.system(size: 16, weight: .bold))
                                    .foregroundColor(.white)
                            )
                        
                        VStack(alignment: .leading, spacing: 4) {
                            Text(detail.title)
                                .font(.title3)
                                .fontWeight(.bold)
                                .foregroundColor(Color.Token.slate800)
                            
                            Text(detail.description)
                                .font(.subheadline)
                                .foregroundColor(Color.Token.slate600)
                                .fixedSize(horizontal: false, vertical: true)
                        }
                    }
                    .padding(.horizontal, 20)
                    .padding(.top, 8)
                    
                    Divider()
                        .padding(.horizontal, 20)
                    
                    // Letters Section
                    if !detail.letters.isEmpty {
                        VStack(alignment: .leading, spacing: 8) {
                            Text(lettersHeader)
                                .font(.headline)
                                .foregroundColor(Color.Token.slate800)
                            
                            Text(detail.letters)
                                .font(SaatTypography.quranArabic(size: 24))
                                .fontWeight(.bold)
                                .foregroundColor(detail.color)
                                .padding(12)
                                .frame(maxWidth: .infinity, alignment: .center)
                                .background(
                                    RoundedRectangle(cornerRadius: 12)
                                        .fill(detail.color.opacity(0.08))
                                )
                        }
                        .padding(.horizontal, 20)
                    }
                    
                    // How to Read Section
                    VStack(alignment: .leading, spacing: 8) {
                        Text(howToReadHeader)
                            .font(.headline)
                            .foregroundColor(Color.Token.slate800)
                        
                        Text(detail.howToRead)
                            .font(.subheadline)
                            .foregroundColor(Color.Token.slate600)
                            .padding(14)
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .background(
                                RoundedRectangle(cornerRadius: 12)
                                    .fill(Color.Token.lightGrey.opacity(0.35))
                            )
                    }
                    .padding(.horizontal, 20)
                    
                    // Examples Section
                    if !detail.examples.isEmpty {
                        VStack(alignment: .leading, spacing: 12) {
                            Text(examplesHeader)
                                .font(.headline)
                                .foregroundColor(Color.Token.slate800)
                            
                            ForEach(detail.examples) { ex in
                                VStack(alignment: .leading, spacing: 8) {
                                    HStack {
                                        Text(ex.arabic)
                                            .font(SaatTypography.quranArabic(size: 24))
                                            .foregroundColor(Color.Token.slate800)
                                            .environment(\.layoutDirection, .rightToLeft)
                                        
                                        Spacer()
                                        
                                        Text(ex.transliteration)
                                            .font(.subheadline.italic())
                                            .foregroundColor(Color.Token.slate500)
                                    }
                                    
                                    Text(ex.explanation)
                                        .font(.caption)
                                        .foregroundColor(Color.Token.slate600)
                                }
                                .padding(14)
                                .background(
                                    RoundedRectangle(cornerRadius: 12)
                                        .stroke(Color.Token.softGrey, lineWidth: 1)
                                )
                            }
                        }
                        .padding(.horizontal, 20)
                    }
                }
                .padding(.bottom, 32)
            }
        }
    }
}
