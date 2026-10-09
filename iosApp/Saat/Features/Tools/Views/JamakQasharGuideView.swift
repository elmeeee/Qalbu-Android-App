//
//  JamakQasharGuideView.swift
//  Saat
//
//  Created by Sufiandy Elmy on 09/10/2026.
//  Copyright © 2026 Elmee. All rights reserved.
//

import SwiftUI

struct JamakTypeItem: Identifiable, Codable, Equatable {
    var id: String { key }
    let key: String
    let titleId: String
    let titleMs: String
    let titleEn: String
    let subtitleId: String
    let subtitleMs: String
    let subtitleEn: String

    func title(for lang: AppLanguage) -> String {
        switch lang {
        case .indonesian: return titleId
        case .malay: return titleMs.isEmpty ? titleId : titleMs
        case .english: return titleEn.isEmpty ? titleId : titleEn
        }
    }

    func subtitle(for lang: AppLanguage) -> String {
        switch lang {
        case .indonesian: return subtitleId
        case .malay: return subtitleMs.isEmpty ? subtitleId : subtitleMs
        case .english: return subtitleEn.isEmpty ? subtitleId : subtitleEn
        }
    }
}

struct JamakRuleItem: Identifiable, Codable, Equatable {
    let id: String
    let titleId: String
    let titleMs: String
    let titleEn: String
    let descId: String?
    let descMs: String?
    let descEn: String?
    let detailId: String?
    let detailMs: String?
    let detailEn: String?
    let dalilRefId: String?
    let dalilRefMs: String?
    let dalilRefEn: String?

    func title(for lang: AppLanguage) -> String {
        switch lang {
        case .indonesian: return titleId
        case .malay: return titleMs.isEmpty ? titleId : titleMs
        case .english: return titleEn.isEmpty ? titleId : titleEn
        }
    }

    func desc(for lang: AppLanguage) -> String {
        switch lang {
        case .indonesian: return descId ?? ""
        case .malay: return descMs ?? descId ?? ""
        case .english: return descEn ?? descId ?? ""
        }
    }

    func detail(for lang: AppLanguage) -> String {
        switch lang {
        case .indonesian: return detailId ?? ""
        case .malay: return detailMs ?? detailId ?? ""
        case .english: return detailEn ?? detailId ?? ""
        }
    }
}

struct JamakNiatItem: Identifiable, Codable, Equatable {
    let id: String
    let titleId: String
    let titleMs: String
    let titleEn: String
    let arabic: String
    let transliteration: String
    let translationId: String
    let translationMs: String
    let translationEn: String
    let noteId: String?
    let noteMs: String?
    let noteEn: String?
    let hadithRef: String?

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

    func note(for lang: AppLanguage) -> String? {
        switch lang {
        case .indonesian: return noteId
        case .malay: return noteMs ?? noteId
        case .english: return noteEn ?? noteId
        }
    }
}

struct JamakStepItem: Identifiable, Codable, Equatable {
    var id: Int { stepNumber }
    let stepNumber: Int
    let titleId: String
    let titleMs: String
    let titleEn: String
    let descId: String
    let descMs: String
    let descEn: String
    let tipId: String?
    let tipMs: String?
    let tipEn: String?
    let arabic: String?
    let transliteration: String?
    let translationId: String?
    let translationMs: String?
    let translationEn: String?

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

    func tip(for lang: AppLanguage) -> String? {
        switch lang {
        case .indonesian: return tipId
        case .malay: return tipMs ?? tipId
        case .english: return tipEn ?? tipId
        }
    }
}

struct JamakGuideData: Codable {
    let types: [JamakTypeItem]
    let rules: [JamakRuleItem]
    let niatList: [JamakNiatItem]
    let steps: [String: [JamakStepItem]]
}

struct JamakQasharGuideView: View {
    @Environment(\.dismiss) private var dismiss
    @ObservedObject private var languageManager = AppLanguageManager.shared

    @State private var guideData: JamakGuideData? = nil
    @State private var selectedTab: Int = 0 // 0: Tata Cara, 1: Niat, 2: Syarat & Hukum, 3: Cek Jarak Safar
    @State private var selectedType: JamakTypeItem? = nil

    // Distance Calculator
    @State private var travelDistanceKm: String = "85"

    private var isEligibleForJamak: Bool {
        let km = Double(travelDistanceKm) ?? 0
        return km >= 81.0
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
                    Text(languageManager.localize("tool_jamak_guide_title"))
                        .font(.system(size: 18, weight: .bold))
                        .foregroundColor(Color(hex: "#1E293B"))

                    Text("Keringanan Shalat Safar & Perjalanan Jauh")
                        .font(.system(size: 12, weight: .regular))
                        .foregroundColor(Color(hex: "#64748B"))
                }

                Spacer()
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 10)
            .background(Color(hex: "#F9F7F2"))

            // Segmented Tab Control
            HStack(spacing: 0) {
                tabButton("Tata Cara", index: 0)
                tabButton("Niat", index: 1)
                tabButton("Syarat Fiqih", index: 2)
                tabButton("Kalkulator", index: 3)
            }
            .background(Color.white)
            .padding(.vertical, 4)

            Divider()

            ScrollView {
                VStack(spacing: 16) {
                    if selectedTab == 0 {
                        tataCaraTab
                    } else if selectedTab == 1 {
                        niatTab
                    } else if selectedTab == 2 {
                        syaratTab
                    } else {
                        kalkulatorTab
                    }
                }
                .padding(.horizontal, 16)
                .padding(.top, 14)
                .padding(.bottom, 32)
            }
            .background(Color(hex: "#F9F7F2"))
        }
        .background(Color(hex: "#F9F7F2").ignoresSafeArea())
        .navigationBarHidden(true)
        .toolbar(.hidden, for: .navigationBar)
        .toolbar(.hidden, for: .tabBar)
        .sheet(item: $selectedType) { type in
            if let steps = guideData?.steps[type.key] {
                JamakStepDetailSheet(type: type, steps: steps, lang: languageManager.currentLanguage)
            }
        }
        .onAppear {
            loadGuideData()
        }
    }

    private func tabButton(_ title: String, index: Int) -> some View {
        Button(action: {
            withAnimation(.easeInOut(duration: 0.2)) {
                selectedTab = index
            }
        }) {
            VStack(spacing: 6) {
                Text(title)
                    .font(.system(size: 13, weight: selectedTab == index ? .bold : .medium))
                    .foregroundColor(selectedTab == index ? Color(hex: "#085E43") : Color(hex: "#64748B"))

                Rectangle()
                    .fill(selectedTab == index ? Color(hex: "#085E43") : Color.clear)
                    .frame(height: 2.5)
            }
        }
        .frame(maxWidth: .infinity)
    }

    // MARK: - Tab 1: Tata Cara
    @ViewBuilder
    private var tataCaraTab: some View {
        VStack(spacing: 12) {
            ForEach(guideData?.types ?? []) { type in
                Button(action: { selectedType = type }) {
                    HStack(spacing: 14) {
                        ZStack {
                            Circle()
                                .fill(Color(hex: "#E6F3EE"))
                                .frame(width: 44, height: 44)

                            Image(systemName: "figure.walk")
                                .font(.system(size: 18))
                                .foregroundColor(Color(hex: "#085E43"))
                        }

                        VStack(alignment: .leading, spacing: 3) {
                            Text(type.title(for: languageManager.currentLanguage))
                                .font(.system(size: 15, weight: .bold))
                                .foregroundColor(Color(hex: "#1E293B"))

                            Text(type.subtitle(for: languageManager.currentLanguage))
                                .font(.system(size: 12, weight: .regular))
                                .foregroundColor(Color(hex: "#64748B"))
                        }

                        Spacer()

                        Image(systemName: "chevron.right")
                            .font(.system(size: 14, weight: .semibold))
                            .foregroundColor(Color(hex: "#CBD5E1"))
                    }
                    .padding(14)
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
    }

    // MARK: - Tab 2: Niat
    @ViewBuilder
    private var niatTab: some View {
        VStack(spacing: 14) {
            ForEach(guideData?.niatList ?? []) { niat in
                VStack(alignment: .leading, spacing: 8) {
                    Text(niat.title(for: languageManager.currentLanguage))
                        .font(.system(size: 14, weight: .bold))
                        .foregroundColor(Color(hex: "#085E43"))

                    Text(niat.arabic)
                        .font(.system(size: 20, weight: .semibold))
                        .foregroundColor(Color(hex: "#085E43"))
                        .frame(maxWidth: .infinity, alignment: .trailing)
                        .multilineTextAlignment(.trailing)
                        .padding(.vertical, 4)

                    Text(niat.transliteration)
                        .font(.system(size: 12.5, weight: .medium, design: .serif))
                        .foregroundColor(Color(hex: "#D97706"))

                    Text(niat.translation(for: languageManager.currentLanguage))
                        .font(.system(size: 12, weight: .regular))
                        .foregroundColor(Color(hex: "#64748B"))

                    if let note = niat.note(for: languageManager.currentLanguage) {
                        Text(note)
                            .font(.system(size: 11, weight: .medium))
                            .foregroundColor(Color(hex: "#085E43"))
                            .padding(.top, 2)
                    }
                }
                .padding(14)
                .frame(maxWidth: .infinity, alignment: .leading)
                .background(Color.white)
                .cornerRadius(16)
                .overlay(RoundedRectangle(cornerRadius: 16).stroke(Color(hex: "#EAE4D6"), lineWidth: 1))
            }
        }
    }

    // MARK: - Tab 3: Syarat Fiqih
    @ViewBuilder
    private var syaratTab: some View {
        VStack(spacing: 12) {
            ForEach(guideData?.rules ?? []) { rule in
                VStack(alignment: .leading, spacing: 8) {
                    HStack(spacing: 8) {
                        Image(systemName: "checkmark.seal.fill")
                            .foregroundColor(Color(hex: "#085E43"))
                        Text(rule.title(for: languageManager.currentLanguage))
                            .font(.system(size: 15, weight: .bold))
                            .foregroundColor(Color(hex: "#1E293B"))
                    }

                    Text(rule.detail(for: languageManager.currentLanguage))
                        .font(.system(size: 13, weight: .regular))
                        .foregroundColor(Color(hex: "#334155"))
                        .lineSpacing(3)

                    if let dalil = rule.dalilRefId {
                        Text(dalil)
                            .font(.system(size: 11, weight: .medium))
                            .foregroundColor(Color(hex: "#64748B"))
                            .padding(.top, 2)
                    }
                }
                .padding(16)
                .frame(maxWidth: .infinity, alignment: .leading)
                .background(Color.white)
                .cornerRadius(16)
                .overlay(RoundedRectangle(cornerRadius: 16).stroke(Color(hex: "#EAE4D6"), lineWidth: 1))
            }
        }
    }

    // MARK: - Tab 4: Kalkulator Jarak Safar
    @ViewBuilder
    private var kalkulatorTab: some View {
        VStack(spacing: 16) {
            VStack(alignment: .leading, spacing: 10) {
                Text("Kalkulator Jarak Safar")
                    .font(.system(size: 16, weight: .bold))
                    .foregroundColor(Color(hex: "#085E43"))

                Text("Masukkan jarak perjalanan satu arah dari batas kota tempat tinggal Anda ke kota tujuan:")
                    .font(.system(size: 13))
                    .foregroundColor(Color(hex: "#64748B"))

                HStack {
                    TextField("Contoh: 85", text: $travelDistanceKm)
                        .keyboardType(.decimalPad)
                        .font(.system(size: 18, weight: .bold))
                        .foregroundColor(Color(hex: "#1E293B"))

                    Text("KM")
                        .font(.system(size: 14, weight: .bold))
                        .foregroundColor(Color(hex: "#64748B"))
                }
                .padding(14)
                .background(Color(hex: "#F1F5F9"))
                .cornerRadius(12)
            }
            .padding(18)
            .background(Color.white)
            .cornerRadius(16)
            .overlay(RoundedRectangle(cornerRadius: 16).stroke(Color(hex: "#EAE4D6"), lineWidth: 1))

            // Result Banner
            HStack(spacing: 14) {
                ZStack {
                    Circle()
                        .fill(isEligibleForJamak ? Color(hex: "#085E43") : Color(hex: "#DC2626"))
                        .frame(width: 44, height: 44)

                    Image(systemName: isEligibleForJamak ? "checkmark" : "xmark")
                        .font(.system(size: 18, weight: .bold))
                        .foregroundColor(.white)
                }

                VStack(alignment: .leading, spacing: 3) {
                    Text(isEligibleForJamak ? "Boleh Jamak & Qashar" : "Belum Memenuhi Jarak Safar")
                        .font(.system(size: 15, weight: .bold))
                        .foregroundColor(isEligibleForJamak ? Color(hex: "#085E43") : Color(hex: "#DC2626"))

                    Text(isEligibleForJamak ?
                         "Jarak ≥ 81 km memenuhi syarat safar (2 Marhalah) menurut Mazhab Syafi'i, Maliki, & Hanbali." :
                         "Jarak minimal safar yang membolehkan rukhsah jamak & qashar adalah 81–89 km.")
                        .font(.system(size: 12))
                        .foregroundColor(Color(hex: "#64748B"))
                }
            }
            .padding(16)
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(isEligibleForJamak ? Color(hex: "#E6F3EE") : Color(hex: "#FEE2E2"))
            .cornerRadius(16)
        }
    }

    private func loadGuideData() {
        guard let url = Bundle.main.url(forResource: "jamak_qashar_guide", withExtension: "json", subdirectory: "prayer_guide") ??
                        Bundle.main.url(forResource: "jamak_qashar_guide", withExtension: "json") else {
            return
        }
        do {
            let data = try Data(contentsOf: url)
            self.guideData = try JSONDecoder().decode(JamakGuideData.self, from: data)
        } catch {
            // Load error
        }
    }
}

// MARK: - Step Detail Sheet
private struct JamakStepDetailSheet: View {
    let type: JamakTypeItem
    let steps: [JamakStepItem]
    let lang: AppLanguage
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 16) {
                    ForEach(steps) { step in
                        VStack(alignment: .leading, spacing: 8) {
                            HStack(spacing: 8) {
                                ZStack {
                                    Circle()
                                        .fill(Color(hex: "#085E43"))
                                        .frame(width: 26, height: 26)
                                    Text("\(step.stepNumber)")
                                        .font(.system(size: 12, weight: .bold))
                                        .foregroundColor(.white)
                                }

                                Text(step.title(for: lang))
                                    .font(.system(size: 15, weight: .bold))
                                    .foregroundColor(Color(hex: "#1E293B"))
                            }

                            Text(step.desc(for: lang))
                                .font(.system(size: 13))
                                .foregroundColor(Color(hex: "#334155"))
                                .lineSpacing(3)

                            if let arabic = step.arabic {
                                Text(arabic)
                                    .font(.system(size: 18, weight: .semibold))
                                    .foregroundColor(Color(hex: "#085E43"))
                                    .frame(maxWidth: .infinity, alignment: .trailing)
                                    .multilineTextAlignment(.trailing)
                                    .padding(.vertical, 2)
                            }

                            if let trans = step.transliteration {
                                Text(trans)
                                    .font(.system(size: 12, weight: .medium, design: .serif))
                                    .foregroundColor(Color(hex: "#D97706"))
                            }

                            if let tip = step.tip(for: lang) {
                                HStack(spacing: 6) {
                                    Image(systemName: "info.circle.fill")
                                        .foregroundColor(Color(hex: "#085E43"))
                                    Text(tip)
                                        .font(.system(size: 11.5, weight: .medium))
                                        .foregroundColor(Color(hex: "#085E43"))
                                }
                                .padding(8)
                                .frame(maxWidth: .infinity, alignment: .leading)
                                .background(Color(hex: "#E6F3EE"))
                                .cornerRadius(8)
                            }
                        }
                        .padding(16)
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .background(Color.white)
                        .cornerRadius(16)
                        .overlay(RoundedRectangle(cornerRadius: 16).stroke(Color(hex: "#EAE4D6"), lineWidth: 1))
                    }
                }
                .padding(20)
            }
            .background(Color(hex: "#F9F7F2").ignoresSafeArea())
            .navigationTitle(type.title(for: lang))
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
