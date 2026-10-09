//
//  HajjUmrahGuideView.swift
//  Saat
//
//  Created by Sufiandy Elmy on 09/10/2026.
//  Copyright © 2026 Elmee. All rights reserved.
//

import SwiftUI

struct HajjUmrahStepItem: Identifiable, Codable, Equatable {
    let id: String
    let stepNumber: Int
    let title: LocalizedStringMap
    let subtitle: LocalizedStringMap
    let location: LocalizedStringMap
    let timeOrDay: LocalizedStringMap
    let isRukun: Bool
    let description: LocalizedStringMap
    let detailedSteps: [LocalizedStringMap]?
    let dalilQuran: LocalizedStringMap?
    let dalilHadits: LocalizedStringMap?
    let practicalTips: [LocalizedStringMap]?
}

struct LocalizedStringMap: Codable, Equatable {
    let id: String?
    let en: String?
    let ms: String?

    func text(for lang: AppLanguage) -> String {
        switch lang {
        case .indonesian: return id ?? ms ?? en ?? ""
        case .malay: return ms ?? id ?? en ?? ""
        case .english: return en ?? id ?? ms ?? ""
        }
    }
}

struct ManasikDuaItem: Identifiable, Codable, Equatable {
    let id: String
    let title: LocalizedStringMap
    let category: LocalizedStringMap
    let arabic: String
    let latin: String
    let translation: LocalizedStringMap
    let contextAndBenefits: LocalizedStringMap?
    let reference: String?
}

struct HajjUmrahGuideData: Codable {
    let umrahSteps: [HajjUmrahStepItem]
    let hajjSteps: [HajjUmrahStepItem]
    let manasikDuas: [ManasikDuaItem]
}

struct HajjUmrahGuideView: View {
    @Environment(\.dismiss) private var dismiss
    @ObservedObject private var languageManager = AppLanguageManager.shared

    @State private var guideData: HajjUmrahGuideData? = nil
    @State private var selectedTab: Int = 0 // 0: Umrah, 1: Haji, 2: Doa Manasik
    @State private var selectedStep: HajjUmrahStepItem? = nil

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
                    Text(languageManager.localize("tool_hajj_umrah_title"))
                        .font(.system(size: 18, weight: .bold))
                        .foregroundColor(Color(hex: "#1E293B"))

                    Text("Panduan Manasik, Rukun & Doa-Doa Pilihan")
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
                tabButton("Manasik Umrah", index: 0)
                tabButton("Manasik Haji", index: 1)
                tabButton("Doa Manasik", index: 2)
            }
            .background(Color.white)
            .padding(.vertical, 4)

            Divider()

            ScrollView {
                VStack(spacing: 14) {
                    if selectedTab == 0 {
                        stepsList(steps: guideData?.umrahSteps ?? [])
                    } else if selectedTab == 1 {
                        stepsList(steps: guideData?.hajjSteps ?? [])
                    } else {
                        duasList(duas: guideData?.manasikDuas ?? [])
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
        .sheet(item: $selectedStep) { step in
            HajjStepDetailSheet(step: step, lang: languageManager.currentLanguage)
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

    @ViewBuilder
    private func stepsList(steps: [HajjUmrahStepItem]) -> some View {
        VStack(spacing: 12) {
            ForEach(steps) { step in
                Button(action: { selectedStep = step }) {
                    HStack(spacing: 14) {
                        ZStack {
                            Circle()
                                .fill(step.isRukun ? Color(hex: "#085E43") : Color(hex: "#E6F3EE"))
                                .frame(width: 44, height: 44)

                            Text("\(step.stepNumber)")
                                .font(.system(size: 16, weight: .bold))
                                .foregroundColor(step.isRukun ? .white : Color(hex: "#085E43"))
                        }

                        VStack(alignment: .leading, spacing: 3) {
                            HStack {
                                Text(step.title.text(for: languageManager.currentLanguage))
                                    .font(.system(size: 15, weight: .bold))
                                    .foregroundColor(Color(hex: "#1E293B"))

                                if step.isRukun {
                                    Text("Rukun")
                                        .font(.system(size: 10, weight: .bold))
                                        .padding(.horizontal, 6)
                                        .padding(.vertical, 2)
                                        .background(Color(hex: "#085E43"))
                                        .foregroundColor(.white)
                                        .cornerRadius(6)
                                }
                            }

                            Text(step.subtitle.text(for: languageManager.currentLanguage))
                                .font(.system(size: 12, weight: .regular))
                                .foregroundColor(Color(hex: "#64748B"))
                                .lineLimit(2)
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

    @ViewBuilder
    private func duasList(duas: [ManasikDuaItem]) -> some View {
        VStack(spacing: 14) {
            ForEach(duas) { dua in
                VStack(alignment: .leading, spacing: 8) {
                    HStack {
                        Text(dua.title.text(for: languageManager.currentLanguage))
                            .font(.system(size: 14, weight: .bold))
                            .foregroundColor(Color(hex: "#085E43"))

                        Spacer()

                        Text(dua.category.text(for: languageManager.currentLanguage))
                            .font(.system(size: 10, weight: .bold))
                            .padding(.horizontal, 6)
                            .padding(.vertical, 2)
                            .background(Color(hex: "#E6F3EE"))
                            .foregroundColor(Color(hex: "#085E43"))
                            .cornerRadius(6)
                    }

                    Text(dua.arabic)
                        .font(.system(size: 20, weight: .semibold))
                        .foregroundColor(Color(hex: "#085E43"))
                        .frame(maxWidth: .infinity, alignment: .trailing)
                        .multilineTextAlignment(.trailing)
                        .padding(.vertical, 4)

                    Text(dua.latin)
                        .font(.system(size: 12.5, weight: .medium, design: .serif))
                        .foregroundColor(Color(hex: "#D97706"))

                    Text(dua.translation.text(for: languageManager.currentLanguage))
                        .font(.system(size: 12, weight: .regular))
                        .foregroundColor(Color(hex: "#334155"))
                        .lineSpacing(2)

                    if let ref = dua.reference {
                        Text(ref)
                            .font(.system(size: 11, weight: .semibold))
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

    private func loadGuideData() {
        guard let url = Bundle.main.url(forResource: "hajj_umrah_guide", withExtension: "json", subdirectory: "hajj") ??
                        Bundle.main.url(forResource: "hajj_umrah_guide", withExtension: "json") else {
            return
        }
        do {
            let data = try Data(contentsOf: url)
            self.guideData = try JSONDecoder().decode(HajjUmrahGuideData.self, from: data)
        } catch {
            // Load error
        }
    }
}

// MARK: - Step Detail Sheet
private struct HajjStepDetailSheet: View {
    let step: HajjUmrahStepItem
    let lang: AppLanguage
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 16) {
                    VStack(alignment: .leading, spacing: 6) {
                        HStack {
                            Text(step.title.text(for: lang))
                                .font(.system(size: 20, weight: .bold))
                                .foregroundColor(Color(hex: "#085E43"))
                            Spacer()
                            if step.isRukun {
                                Text("Rukun Wajib")
                                    .font(.system(size: 11, weight: .bold))
                                    .padding(.horizontal, 8)
                                    .padding(.vertical, 3)
                                    .background(Color(hex: "#085E43"))
                                    .foregroundColor(.white)
                                    .cornerRadius(6)
                            }
                        }

                        Text(step.description.text(for: lang))
                            .font(.system(size: 13.5))
                            .foregroundColor(Color(hex: "#334155"))
                            .lineSpacing(3)
                    }
                    .padding(16)
                    .background(Color.white)
                    .cornerRadius(16)
                    .overlay(RoundedRectangle(cornerRadius: 16).stroke(Color(hex: "#EAE4D6"), lineWidth: 1))

                    if let steps = step.detailedSteps, !steps.isEmpty {
                        VStack(alignment: .leading, spacing: 10) {
                            Text("Rincian Amalan:")
                                .font(.system(size: 14, weight: .bold))
                                .foregroundColor(Color(hex: "#085E43"))

                            ForEach(0..<steps.count, id: \.self) { idx in
                                HStack(alignment: .top, spacing: 10) {
                                    Text("\(idx + 1).")
                                        .font(.system(size: 13, weight: .bold))
                                        .foregroundColor(Color(hex: "#085E43"))
                                    Text(steps[idx].text(for: lang))
                                        .font(.system(size: 13))
                                        .foregroundColor(Color(hex: "#334155"))
                                        .lineSpacing(2)
                                }
                            }
                        }
                        .padding(16)
                        .background(Color.white)
                        .cornerRadius(16)
                        .overlay(RoundedRectangle(cornerRadius: 16).stroke(Color(hex: "#EAE4D6"), lineWidth: 1))
                    }
                }
                .padding(20)
            }
            .background(Color(hex: "#F9F7F2").ignoresSafeArea())
            .navigationTitle(step.title.text(for: lang))
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
