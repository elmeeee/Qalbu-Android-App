//
//  JanazahGuideView.swift
//  Saat
//
//  Created by Sufiandy Elmy on 09/10/2026.
//  Copyright © 2026 Elmee. All rights reserved.
//

import SwiftUI

struct JanazahGuideData: Codable {
    let principleDescId: String
    let principleDescMs: String
    let principleDescEn: String
    let rewardHadithId: String
    let rewardHadithMs: String
    let rewardHadithEn: String
    let pillarsId: [String]
    let pillarsMs: [String]
    let pillarsEn: [String]
    let conditionsId: [String]
    let conditionsMs: [String]
    let conditionsEn: [String]
    let takbirSteps: [JanazahTakbirStep]
    let niatList: [JanazahNiat]
    let positionGuides: [JanazahPosition]
    let afterDuas: [JanazahAfterDua]
}

struct JanazahTakbirStep: Identifiable, Codable, Equatable {
    var id: Int { takbirNumber }
    let takbirNumber: Int
    let titleId: String
    let titleMs: String
    let titleEn: String
    let descId: String
    let descMs: String
    let descEn: String
    let arabic: String
    let latin: String
    let translationId: String
    let translationMs: String
    let translationEn: String
    let importantNotesId: String?
    let importantNotesMs: String?
    let importantNotesEn: String?

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

    func translation(for lang: AppLanguage) -> String {
        switch lang {
        case .indonesian: return translationId
        case .malay: return translationMs.isEmpty ? translationId : translationMs
        case .english: return translationEn.isEmpty ? translationId : translationEn
        }
    }
}

struct JanazahNiat: Identifiable, Codable, Equatable {
    let id: String
    let category: String
    let titleId: String
    let titleMs: String
    let titleEn: String
    let subtitleId: String?
    let subtitleMs: String?
    let subtitleEn: String?
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

    func subtitle(for lang: AppLanguage) -> String? {
        switch lang {
        case .indonesian: return subtitleId
        case .malay: return subtitleMs ?? subtitleId
        case .english: return subtitleEn ?? subtitleId
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

struct JanazahPosition: Identifiable, Codable, Equatable {
    var id: String { titleId }
    let titleId: String
    let titleMs: String
    let titleEn: String
    let imamPositionId: String
    let imamPositionMs: String
    let imamPositionEn: String
    let descriptionId: String
    let descriptionMs: String
    let descriptionEn: String
    let hadithRef: String?

    func title(for lang: AppLanguage) -> String {
        switch lang {
        case .indonesian: return titleId
        case .malay: return titleMs.isEmpty ? titleId : titleMs
        case .english: return titleEn.isEmpty ? titleId : titleEn
        }
    }

    func imamPosition(for lang: AppLanguage) -> String {
        switch lang {
        case .indonesian: return imamPositionId
        case .malay: return imamPositionMs.isEmpty ? imamPositionId : imamPositionMs
        case .english: return imamPositionEn.isEmpty ? imamPositionId : imamPositionEn
        }
    }

    func description(for lang: AppLanguage) -> String {
        switch lang {
        case .indonesian: return descriptionId
        case .malay: return descriptionMs.isEmpty ? descriptionId : descriptionMs
        case .english: return descriptionEn.isEmpty ? descriptionId : descriptionEn
        }
    }
}

struct JanazahAfterDua: Identifiable, Codable, Equatable {
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

struct JanazahGuideView: View {
    @Environment(\.dismiss) private var dismiss
    @ObservedObject private var languageManager = AppLanguageManager.shared

    @State private var guideData: JanazahGuideData? = nil
    @State private var selectedTab: Int = 0 // 0: 4 Takbir, 1: Niat, 2: Posisi Imam, 3: Rukun & Syarat

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
                    Text(languageManager.localize("tool_janazah_guide_title"))
                        .font(.system(size: 18, weight: .bold))
                        .foregroundColor(Color(hex: "#1E293B"))

                    Text("Panduan 4 Takbir, Niat & Posisi Imam")
                        .font(.system(size: 12, weight: .regular))
                        .foregroundColor(Color(hex: "#64748B"))
                }

                Spacer()
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 10)
            .background(Color(hex: "#F9F7F2"))

            // Tabs
            HStack(spacing: 0) {
                tabButton("4 Takbir", index: 0)
                tabButton("Niat", index: 1)
                tabButton("Posisi Imam", index: 2)
                tabButton("Rukun", index: 3)
            }
            .background(Color.white)
            .padding(.vertical, 4)

            Divider()

            ScrollView {
                VStack(spacing: 16) {
                    if selectedTab == 0 {
                        takbirTab
                    } else if selectedTab == 1 {
                        niatTab
                    } else if selectedTab == 2 {
                        posisiTab
                    } else {
                        rukunTab
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

    // MARK: - 4 Takbir
    @ViewBuilder
    private var takbirTab: some View {
        VStack(spacing: 14) {
            // Principle Note
            HStack(spacing: 10) {
                Image(systemName: "info.circle.fill")
                    .foregroundColor(Color(hex: "#085E43"))
                Text(guideData?.principleDescId ?? "Shalat jenazah dilakukan sepenuhnya dalam keadaan berdiri dengan 4 Takbir.")
                    .font(.system(size: 12.5, weight: .medium))
                    .foregroundColor(Color(hex: "#085E43"))
                    .lineSpacing(2)
            }
            .padding(14)
            .background(Color(hex: "#E6F3EE"))
            .cornerRadius(14)

            ForEach(guideData?.takbirSteps ?? []) { step in
                VStack(alignment: .leading, spacing: 10) {
                    HStack(spacing: 8) {
                        ZStack {
                            Circle()
                                .fill(Color(hex: "#085E43"))
                                .frame(width: 28, height: 28)
                            Text("\(step.takbirNumber)")
                                .font(.system(size: 13, weight: .bold))
                                .foregroundColor(.white)
                        }

                        Text(step.title(for: languageManager.currentLanguage))
                            .font(.system(size: 15, weight: .bold))
                            .foregroundColor(Color(hex: "#1E293B"))
                    }

                    Text(step.desc(for: languageManager.currentLanguage))
                        .font(.system(size: 12.5))
                        .foregroundColor(Color(hex: "#64748B"))
                        .lineSpacing(2)

                    Text(step.arabic)
                        .font(.system(size: 20, weight: .semibold))
                        .foregroundColor(Color(hex: "#085E43"))
                        .frame(maxWidth: .infinity, alignment: .trailing)
                        .multilineTextAlignment(.trailing)
                        .padding(.vertical, 4)

                    Text(step.latin)
                        .font(.system(size: 12.5, weight: .medium, design: .serif))
                        .foregroundColor(Color(hex: "#D97706"))

                    Text(step.translation(for: languageManager.currentLanguage))
                        .font(.system(size: 12, weight: .regular))
                        .foregroundColor(Color(hex: "#334155"))
                        .lineSpacing(3)
                }
                .padding(16)
                .frame(maxWidth: .infinity, alignment: .leading)
                .background(Color.white)
                .cornerRadius(16)
                .overlay(RoundedRectangle(cornerRadius: 16).stroke(Color(hex: "#EAE4D6"), lineWidth: 1))
            }
        }
    }

    // MARK: - Niat
    @ViewBuilder
    private var niatTab: some View {
        VStack(spacing: 14) {
            ForEach(guideData?.niatList ?? []) { niat in
                VStack(alignment: .leading, spacing: 8) {
                    Text(niat.title(for: languageManager.currentLanguage))
                        .font(.system(size: 14, weight: .bold))
                        .foregroundColor(Color(hex: "#085E43"))

                    if let sub = niat.subtitle(for: languageManager.currentLanguage) {
                        Text(sub)
                            .font(.system(size: 11.5))
                            .foregroundColor(Color(hex: "#64748B"))
                    }

                    Text(niat.arabic)
                        .font(.system(size: 20, weight: .semibold))
                        .foregroundColor(Color(hex: "#085E43"))
                        .frame(maxWidth: .infinity, alignment: .trailing)
                        .multilineTextAlignment(.trailing)
                        .padding(.vertical, 4)

                    Text(niat.latin)
                        .font(.system(size: 12.5, weight: .medium, design: .serif))
                        .foregroundColor(Color(hex: "#D97706"))

                    Text(niat.translation(for: languageManager.currentLanguage))
                        .font(.system(size: 12, weight: .regular))
                        .foregroundColor(Color(hex: "#64748B"))
                }
                .padding(16)
                .frame(maxWidth: .infinity, alignment: .leading)
                .background(Color.white)
                .cornerRadius(16)
                .overlay(RoundedRectangle(cornerRadius: 16).stroke(Color(hex: "#EAE4D6"), lineWidth: 1))
            }
        }
    }

    // MARK: - Posisi Imam
    @ViewBuilder
    private var posisiTab: some View {
        VStack(spacing: 14) {
            ForEach(guideData?.positionGuides ?? []) { pos in
                VStack(alignment: .leading, spacing: 8) {
                    Text(pos.title(for: languageManager.currentLanguage))
                        .font(.system(size: 15, weight: .bold))
                        .foregroundColor(Color(hex: "#085E43"))

                    HStack(spacing: 6) {
                        Image(systemName: "figure.stand")
                            .foregroundColor(Color(hex: "#D4AF37"))
                        Text(pos.imamPosition(for: languageManager.currentLanguage))
                            .font(.system(size: 13, weight: .bold))
                            .foregroundColor(Color(hex: "#1E293B"))
                    }

                    Text(pos.description(for: languageManager.currentLanguage))
                        .font(.system(size: 12.5))
                        .foregroundColor(Color(hex: "#64748B"))
                        .lineSpacing(2)

                    if let ref = pos.hadithRef {
                        Text(ref)
                            .font(.system(size: 11, weight: .semibold))
                            .foregroundColor(Color(hex: "#085E43"))
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

    // MARK: - Rukun & Syarat
    @ViewBuilder
    private var rukunTab: some View {
        VStack(spacing: 14) {
            VStack(alignment: .leading, spacing: 10) {
                Text("Rukun Shalat Jenazah (7 Hal)")
                    .font(.system(size: 16, weight: .bold))
                    .foregroundColor(Color(hex: "#085E43"))

                ForEach(guideData?.pillarsId ?? [], id: \.self) { pillar in
                    HStack(spacing: 8) {
                        Image(systemName: "checkmark.circle.fill")
                            .foregroundColor(Color(hex: "#085E43"))
                            .font(.system(size: 14))

                        Text(pillar)
                            .font(.system(size: 13))
                            .foregroundColor(Color(hex: "#334155"))
                    }
                }
            }
            .padding(16)
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(Color.white)
            .cornerRadius(16)
            .overlay(RoundedRectangle(cornerRadius: 16).stroke(Color(hex: "#EAE4D6"), lineWidth: 1))

            VStack(alignment: .leading, spacing: 10) {
                Text("Syarat Sah Shalat Jenazah")
                    .font(.system(size: 16, weight: .bold))
                    .foregroundColor(Color(hex: "#085E43"))

                ForEach(guideData?.conditionsId ?? [], id: \.self) { cond in
                    HStack(spacing: 8) {
                        Image(systemName: "checkmark.seal.fill")
                            .foregroundColor(Color(hex: "#D4AF37"))
                            .font(.system(size: 14))

                        Text(cond)
                            .font(.system(size: 13))
                            .foregroundColor(Color(hex: "#334155"))
                    }
                }
            }
            .padding(16)
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(Color.white)
            .cornerRadius(16)
            .overlay(RoundedRectangle(cornerRadius: 16).stroke(Color(hex: "#EAE4D6"), lineWidth: 1))
        }
    }

    private func loadGuideData() {
        guard let url = Bundle.main.url(forResource: "janazah_guide", withExtension: "json", subdirectory: "prayer_guide") ??
                        Bundle.main.url(forResource: "janazah_guide", withExtension: "json") else {
            return
        }
        do {
            let data = try Data(contentsOf: url)
            self.guideData = try JSONDecoder().decode(JanazahGuideData.self, from: data)
        } catch {
            // Load error
        }
    }
}
