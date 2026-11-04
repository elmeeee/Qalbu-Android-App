//
//  SunnahPrayerGuideView.swift
//  Saat
//
//  Created by Sufiandy Elmy on 09/10/2026.
//  Copyright © 2026 Elmee. All rights reserved.
//

import SwiftUI

struct SunnahPrayerItem: Identifiable, Codable, Equatable {
    let id: String
    let category: String
    let titleId: String
    let titleMs: String
    let titleEn: String
    let summaryId: String
    let summaryMs: String
    let summaryEn: String
    let waktuId: String
    let waktuMs: String
    let waktuEn: String
    let rakaatInfoId: String
    let rakaatInfoMs: String
    let rakaatInfoEn: String
    let fadhilahId: String
    let fadhilahMs: String
    let fadhilahEn: String
    let dalilHadithId: String?
    let dalilHadithMs: String?
    let dalilHadithEn: String?
    let hadithReference: String?
    let recommendedSurahsId: String?
    let recommendedSurahsMs: String?
    let recommendedSurahsEn: String?
    let niatList: [SunnahNiatItem]?
    let steps: [SunnahStepItem]?
    let doaArabic: String?
    let doaLatin: String?
    let doaTitleId: String?
    let doaTitleMs: String?
    let doaTitleEn: String?
    let doaTranslationId: String?
    let doaTranslationMs: String?
    let doaTranslationEn: String?

    func title(for lang: AppLanguage) -> String {
        switch lang {
        case .indonesian: return titleId
        case .malay: return titleMs.isEmpty ? titleId : titleMs
        case .english: return titleEn.isEmpty ? titleId : titleEn
        }
    }

    func summary(for lang: AppLanguage) -> String {
        switch lang {
        case .indonesian: return summaryId
        case .malay: return summaryMs.isEmpty ? summaryId : summaryMs
        case .english: return summaryEn.isEmpty ? summaryId : summaryEn
        }
    }

    func waktu(for lang: AppLanguage) -> String {
        switch lang {
        case .indonesian: return waktuId
        case .malay: return waktuMs.isEmpty ? waktuId : waktuMs
        case .english: return waktuEn.isEmpty ? waktuId : waktuEn
        }
    }

    func rakaat(for lang: AppLanguage) -> String {
        switch lang {
        case .indonesian: return rakaatInfoId
        case .malay: return rakaatInfoMs.isEmpty ? rakaatInfoId : rakaatInfoMs
        case .english: return rakaatInfoEn.isEmpty ? rakaatInfoId : rakaatInfoEn
        }
    }

    func fadhilah(for lang: AppLanguage) -> String {
        switch lang {
        case .indonesian: return fadhilahId
        case .malay: return fadhilahMs.isEmpty ? fadhilahId : fadhilahMs
        case .english: return fadhilahEn.isEmpty ? fadhilahId : fadhilahEn
        }
    }

    func dalil(for lang: AppLanguage) -> String? {
        switch lang {
        case .indonesian: return dalilHadithId
        case .malay: return dalilHadithMs ?? dalilHadithId
        case .english: return dalilHadithEn ?? dalilHadithId
        }
    }

    func surah(for lang: AppLanguage) -> String? {
        switch lang {
        case .indonesian: return recommendedSurahsId
        case .malay: return recommendedSurahsMs ?? recommendedSurahsId
        case .english: return recommendedSurahsEn ?? recommendedSurahsId
        }
    }

    func doaTitle(for lang: AppLanguage) -> String? {
        switch lang {
        case .indonesian: return doaTitleId
        case .malay: return doaTitleMs ?? doaTitleId
        case .english: return doaTitleEn ?? doaTitleId
        }
    }

    func doaTranslation(for lang: AppLanguage) -> String? {
        switch lang {
        case .indonesian: return doaTranslationId
        case .malay: return doaTranslationMs ?? doaTranslationId
        case .english: return doaTranslationEn ?? doaTranslationId
        }
    }
}

struct SunnahNiatItem: Identifiable, Codable, Equatable {
    var id: String { titleId }
    let titleId: String
    let titleMs: String
    let titleEn: String
    let arabic: String
    let latin: String
    let translationId: String
    let translationMs: String
    let translationEn: String

    func title(for lang: AppLanguage) -> String {
        switch lang {
        case .indonesian: return titleId
        case .malay: return titleMs.isEmpty ? titleId : titleMs
        case .english: return titleEn.isEmpty ? titleId : titleEn
        }
    }

    func translation(for lang: AppLanguage) -> String {
        switch lang {
        case .indonesian: return translationId
        case .malay: return translationMs.isEmpty ? translationId : translationMs
        case .english: return translationEn.isEmpty ? translationId : translationEn
        }
    }
}

struct SunnahStepItem: Identifiable, Codable, Equatable {
    var id: Int { stepNumber }
    let stepNumber: Int
    let titleId: String
    let titleMs: String
    let titleEn: String
    let descId: String
    let descMs: String
    let descEn: String

    func title(for lang: AppLanguage) -> String {
        switch lang {
        case .indonesian: return titleId
        case .malay: return titleMs.isEmpty ? titleId : titleMs
        case .english: return titleEn.isEmpty ? titleId : titleEn
        }
    }

    func desc(for lang: AppLanguage) -> String {
        switch lang {
        case .indonesian: return descId
        case .malay: return descMs.isEmpty ? descId : descMs
        case .english: return descEn.isEmpty ? descId : descEn
        }
    }
}

struct SunnahPrayerGuideView: View {
    @Environment(\.dismiss) private var dismiss
    @ObservedObject private var languageManager = AppLanguageManager.shared

    @State private var prayers: [SunnahPrayerItem] = []
    @State private var selectedCategory: String = "ALL"
    @State private var searchQuery: String = ""
    @State private var selectedPrayer: SunnahPrayerItem? = nil

    private var categories: [String] {
        ["ALL", "HARIAN", "MALAM", "KEBUTUHAN"]
    }

    private var filteredPrayers: [SunnahPrayerItem] {
        prayers.filter { prayer in
            let matchesCat = (selectedCategory == "ALL" || prayer.category == selectedCategory)
            if searchQuery.trimmingCharacters(in: .whitespaces).isEmpty {
                return matchesCat
            }
            let q = searchQuery.trimmingCharacters(in: .whitespaces).lowercased()
            let title = prayer.title(for: languageManager.currentLanguage).lowercased()
            let summary = prayer.summary(for: languageManager.currentLanguage).lowercased()
            return matchesCat && (title.contains(q) || summary.contains(q))
        }
    }

    var body: some View {
        VStack(spacing: 0) {
            // Header Bar
            HStack(spacing: 12) {
                Button(action: { dismiss() }) {
                    Image(systemName: "arrow.left")
                        .font(.system(size: 18, weight: .semibold))
                        .foregroundColor(Color(hex: "#085E43"))
                        .frame(width: 40, height: 40)
                        .background(Color.white)
                        .clipShape(Circle())
                        .shadow(color: Color.black.opacity(0.04), radius: 3, x: 0, y: 1)
                }

                VStack(alignment: .leading, spacing: 2) {
                    Text(languageManager.localize("tool_sunnah_practices_title"))
                        .font(.system(size: 18, weight: .bold))
                        .foregroundColor(Color(hex: "#1E293B"))

                    Text("Panduan Lengkap, Niat & Tata Cara")
                        .font(.system(size: 12, weight: .regular))
                        .foregroundColor(Color(hex: "#64748B"))
                }

                Spacer()
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 10)
            .background(Color(hex: "#F9F7F2"))

            // Search Bar
            HStack(spacing: 10) {
                Image(systemName: "magnifyingglass")
                    .foregroundColor(Color(hex: "#085E43").opacity(0.7))

                TextField(languageManager.localize("tool_search_hint"), text: $searchQuery)
                    .font(.system(size: 14))
                    .foregroundColor(Color(hex: "#1E293B"))

                if !searchQuery.isEmpty {
                    Button(action: { searchQuery = "" }) {
                        Image(systemName: "xmark.circle.fill")
                            .foregroundColor(Color(hex: "#94A3B8"))
                    }
                }
            }
            .padding(.horizontal, 14)
            .frame(height: 44)
            .background(Color.white)
            .cornerRadius(12)
            .overlay(
                RoundedRectangle(cornerRadius: 12)
                    .stroke(Color(hex: "#E8E2D2"), lineWidth: 1)
            )
            .padding(.horizontal, 16)
            .padding(.vertical, 8)
            .background(Color(hex: "#F9F7F2"))

            // Category Filter Pills
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    ForEach(categories, id: \.self) { cat in
                        let isSelected = selectedCategory == cat
                        Button(action: {
                            withAnimation(.easeInOut(duration: 0.2)) {
                                selectedCategory = cat
                            }
                        }) {
                            Text(categoryDisplayName(cat))
                                .font(.system(size: 13, weight: isSelected ? .bold : .medium))
                                .padding(.horizontal, 16)
                                .padding(.vertical, 7)
                                .background(isSelected ? Color(hex: "#085E43") : Color.white)
                                .foregroundColor(isSelected ? .white : Color(hex: "#334155"))
                                .cornerRadius(20)
                                .overlay(
                                    RoundedRectangle(cornerRadius: 20)
                                        .stroke(isSelected ? Color.clear : Color(hex: "#E8E2D2"), lineWidth: 1)
                                )
                        }
                        .buttonStyle(.plain)
                    }
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 4)
            }
            .background(Color(hex: "#F9F7F2"))

            // Prayer Cards List
            ScrollView {
                VStack(spacing: 12) {
                    ForEach(filteredPrayers) { prayer in
                        SunnahPrayerCard(
                            prayer: prayer,
                            lang: languageManager.currentLanguage,
                            onTap: { selectedPrayer = prayer }
                        )
                    }
                }
                .padding(.horizontal, 16)
                .padding(.top, 10)
                .padding(.bottom, 32)
            }
            .background(Color(hex: "#F9F7F2"))
        }
        .background(Color(hex: "#F9F7F2").ignoresSafeArea())
        .navigationBarHidden(true)
        .toolbar(.hidden, for: .navigationBar)
        .toolbar(.hidden, for: .tabBar)
        .sheet(item: $selectedPrayer) { prayer in
            SunnahPrayerDetailSheet(prayer: prayer, lang: languageManager.currentLanguage)
        }
        .onAppear {
            loadPrayers()
        }
    }

    private func categoryDisplayName(_ cat: String) -> String {
        switch cat {
        case "HARIAN": return "Harian"
        case "MALAM": return "Malam"
        case "KEBUTUHAN": return "Hajat & Kebutuhan"
        default: return "Semua"
        }
    }

    private func loadPrayers() {
        guard let url = Bundle.main.url(forResource: "sunnah_prayers", withExtension: "json", subdirectory: "prayer_guide") ??
                        Bundle.main.url(forResource: "sunnah_prayers", withExtension: "json") else {
            return
        }
        do {
            let data = try Data(contentsOf: url)
            self.prayers = try JSONDecoder().decode([SunnahPrayerItem].self, from: data)
        } catch {
            // Load error
        }
    }
}

// MARK: - Card Component
private struct SunnahPrayerCard: View {
    let prayer: SunnahPrayerItem
    let lang: AppLanguage
    let onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            VStack(alignment: .leading, spacing: 10) {
                HStack(alignment: .top) {
                    VStack(alignment: .leading, spacing: 3) {
                        Text(prayer.title(for: lang))
                            .font(.system(size: 16, weight: .bold))
                            .foregroundColor(Color(hex: "#085E43"))

                        Text(prayer.summary(for: lang))
                            .font(.system(size: 12.5, weight: .regular))
                            .foregroundColor(Color(hex: "#64748B"))
                            .lineLimit(2)
                            .lineSpacing(2)
                    }

                    Spacer()

                    Image(systemName: "chevron.right")
                        .font(.system(size: 14, weight: .semibold))
                        .foregroundColor(Color(hex: "#CBD5E1"))
                        .padding(.top, 4)
                }

                Divider()

                HStack(spacing: 16) {
                    HStack(spacing: 4) {
                        Image(systemName: "clock")
                            .font(.system(size: 11))
                            .foregroundColor(Color(hex: "#085E43"))
                        Text(prayer.waktu(for: lang))
                            .font(.system(size: 11, weight: .medium))
                            .foregroundColor(Color(hex: "#334155"))
                            .lineLimit(1)
                    }

                    Spacer()

                    HStack(spacing: 4) {
                        Image(systemName: "sparkles")
                            .font(.system(size: 11))
                            .foregroundColor(Color(hex: "#D4AF37"))
                        Text(prayer.rakaat(for: lang))
                            .font(.system(size: 11, weight: .medium))
                            .foregroundColor(Color(hex: "#334155"))
                            .lineLimit(1)
                    }
                }
            }
            .padding(16)
            .background(Color.white)
            .cornerRadius(16)
            .overlay(
                RoundedRectangle(cornerRadius: 16)
                    .stroke(Color(hex: "#EAE4D6"), lineWidth: 1)
            )
            .shadow(color: Color.black.opacity(0.02), radius: 4, x: 0, y: 1)
        }
        .buttonStyle(.plain)
    }
}

// MARK: - Detail Sheet
private struct SunnahPrayerDetailSheet: View {
    let prayer: SunnahPrayerItem
    let lang: AppLanguage
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 18) {
                    // Header Card
                    VStack(alignment: .leading, spacing: 8) {
                        Text(prayer.title(for: lang))
                            .font(.system(size: 22, weight: .bold))
                            .foregroundColor(Color(hex: "#085E43"))

                        Text(prayer.summary(for: lang))
                            .font(.system(size: 14))
                            .foregroundColor(Color(hex: "#64748B"))
                            .lineSpacing(3)

                        HStack(spacing: 14) {
                            HStack(spacing: 4) {
                                Image(systemName: "clock.fill")
                                    .foregroundColor(Color(hex: "#085E43"))
                                Text(prayer.waktu(for: lang))
                                    .font(.system(size: 12, weight: .medium))
                            }

                            HStack(spacing: 4) {
                                Image(systemName: "repeat")
                                    .foregroundColor(Color(hex: "#D4AF37"))
                                Text(prayer.rakaat(for: lang))
                                    .font(.system(size: 12, weight: .medium))
                            }
                        }
                        .padding(.top, 4)
                        .foregroundColor(Color(hex: "#334155"))
                    }
                    .padding(18)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .background(Color.white)
                    .cornerRadius(16)
                    .overlay(RoundedRectangle(cornerRadius: 16).stroke(Color(hex: "#EAE4D6"), lineWidth: 1))

                    // Niat Section
                    if let niats = prayer.niatList, !niats.isEmpty {
                        VStack(alignment: .leading, spacing: 10) {
                            Text("Niat Shalat")
                                .font(.system(size: 15, weight: .bold))
                                .foregroundColor(Color(hex: "#085E43"))

                            ForEach(niats) { niat in
                                VStack(alignment: .leading, spacing: 8) {
                                    Text(niat.title(for: lang))
                                        .font(.system(size: 13, weight: .bold))
                                        .foregroundColor(Color(hex: "#1E293B"))

                                    Text(niat.arabic)
                                        .font(.system(size: 22, weight: .semibold))
                                        .foregroundColor(Color(hex: "#085E43"))
                                        .frame(maxWidth: .infinity, alignment: .trailing)
                                        .multilineTextAlignment(.trailing)
                                        .padding(.vertical, 4)

                                    Text(niat.latin)
                                        .font(.system(size: 12.5, weight: .medium, design: .serif))
                                        .foregroundColor(Color(hex: "#D97706"))

                                    Text(niat.translation(for: lang))
                                        .font(.system(size: 12, weight: .regular))
                                        .foregroundColor(Color(hex: "#64748B"))
                                }
                                .padding(14)
                                .frame(maxWidth: .infinity, alignment: .leading)
                                .background(Color.white)
                                .cornerRadius(14)
                                .overlay(RoundedRectangle(cornerRadius: 14).stroke(Color(hex: "#EAE4D6"), lineWidth: 1))
                            }
                        }
                    }

                    // Doa Setelah Shalat
                    if let doaArab = prayer.doaArabic, !doaArab.isEmpty {
                        VStack(alignment: .leading, spacing: 10) {
                            Text(prayer.doaTitle(for: lang) ?? "Doa Setelah Shalat")
                                .font(.system(size: 15, weight: .bold))
                                .foregroundColor(Color(hex: "#085E43"))

                            VStack(alignment: .leading, spacing: 8) {
                                Text(doaArab)
                                    .font(.system(size: 20, weight: .semibold))
                                    .foregroundColor(Color(hex: "#085E43"))
                                    .frame(maxWidth: .infinity, alignment: .trailing)
                                    .multilineTextAlignment(.trailing)
                                    .padding(.vertical, 4)

                                if let latin = prayer.doaLatin {
                                    Text(latin)
                                        .font(.system(size: 12.5, weight: .medium, design: .serif))
                                        .foregroundColor(Color(hex: "#D97706"))
                                }

                                if let trans = prayer.doaTranslation(for: lang) {
                                    Text(trans)
                                        .font(.system(size: 12, weight: .regular))
                                        .foregroundColor(Color(hex: "#64748B"))
                                        .lineSpacing(3)
                                }
                            }
                            .padding(14)
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .background(Color(hex: "#FFFBEB"))
                            .cornerRadius(14)
                            .overlay(RoundedRectangle(cornerRadius: 14).stroke(Color(hex: "#FDE68A"), lineWidth: 1))
                        }
                    }

                    // Keutamaan & Dalil
                    VStack(alignment: .leading, spacing: 10) {
                        Text("Keutamaan & Dalil")
                            .font(.system(size: 15, weight: .bold))
                            .foregroundColor(Color(hex: "#085E43"))

                        VStack(alignment: .leading, spacing: 8) {
                            Text(prayer.fadhilah(for: lang))
                                .font(.system(size: 13, weight: .medium))
                                .foregroundColor(Color(hex: "#1E293B"))

                            if let dalil = prayer.dalil(for: lang) {
                                Text(dalil)
                                    .font(.system(size: 12, weight: .regular))
                                    .foregroundColor(Color(hex: "#64748B"))
                                    .lineSpacing(2)
                            }

                            if let ref = prayer.hadithReference {
                                Text(ref)
                                    .font(.system(size: 11, weight: .semibold))
                                    .foregroundColor(Color(hex: "#085E43"))
                            }
                        }
                        .padding(14)
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .background(Color.white)
                        .cornerRadius(14)
                        .overlay(RoundedRectangle(cornerRadius: 14).stroke(Color(hex: "#EAE4D6"), lineWidth: 1))
                    }
                }
                .padding(20)
            }
            .background(Color(hex: "#F9F7F2").ignoresSafeArea())
            .navigationTitle(prayer.title(for: lang))
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Tutup") { dismiss() }
                        .foregroundColor(Color(hex: "#085E43"))
                }
            }
        }
        .presentationDetents([.large])
    }
}
