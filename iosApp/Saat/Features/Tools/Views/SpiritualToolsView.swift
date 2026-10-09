//
//  SpiritualToolsView.swift
//  Saat
//
//  Created by Sufiandy Elmy on 09/10/2026.
//  Copyright © 2026 Elmee. All rights reserved.
//

import SwiftUI

enum ToolCategory: String, CaseIterable, Identifiable {
    case all
    case prayer
    case dhikr
    case fiqh

    var id: String { rawValue }

    @MainActor
    func localizedTitle(_ lang: AppLanguageManager) -> String {
        switch self {
        case .all: return lang.localize("tool_category_all")
        case .prayer: return lang.localize("tool_category_prayer")
        case .dhikr: return lang.localize("tool_category_dhikr")
        case .fiqh: return lang.localize("tool_category_fiqh")
        }
    }
}

private struct SpiritualToolItem: Identifiable {
    var id: String { route }
    let iconName: String
    let titleKey: String
    let descKey: String
    let route: String
    let category: ToolCategory
    let destinationBuilder: () -> AnyView
}

struct SpiritualToolsView: View {
    @ObservedObject private var languageManager = AppLanguageManager.shared
    @State private var searchQuery: String = ""
    @State private var selectedCategory: ToolCategory = .all

    private var allTools: [SpiritualToolItem] {
        [
            // 1. Shalat & Waktu (PRAYER)
            SpiritualToolItem(
                iconName: "ic_qibla_3d",
                titleKey: "tool_qibla_title",
                descKey: "tool_qibla_desc",
                route: "qibla",
                category: .prayer,
                destinationBuilder: { AnyView(QiblaFinderView()) }
            ),
            SpiritualToolItem(
                iconName: "ic_sunnah_3d",
                titleKey: "tool_sunnah_practices_title",
                descKey: "tool_sunnah_practices_desc",
                route: "sunnah-prayer",
                category: .prayer,
                destinationBuilder: { AnyView(SunnahPrayerGuideView()) }
            ),
            SpiritualToolItem(
                iconName: "ic_jamak_3d",
                titleKey: "tool_jamak_guide_title",
                descKey: "tool_jamak_guide_desc",
                route: "jamak-qashar",
                category: .prayer,
                destinationBuilder: { AnyView(JamakQasharGuideView()) }
            ),
            SpiritualToolItem(
                iconName: "ic_radio_3d",
                titleKey: "tool_radio_title",
                descKey: "tool_radio_desc",
                route: "radio",
                category: .prayer,
                destinationBuilder: { AnyView(QuranRadioView()) }
            ),

            // 2. Dzikir & Doa (DHIKR)
            SpiritualToolItem(
                iconName: "ic_doazikir_3d",
                titleKey: "tool_dua_dhikr_title",
                descKey: "tool_dua_dhikr_desc",
                route: "doa-zikir",
                category: .dhikr,
                destinationBuilder: { AnyView(DoaZikirView()) }
            ),
            SpiritualToolItem(
                iconName: "ic_tasbih_3d",
                titleKey: "tool_tasbih_title",
                descKey: "tool_tasbih_desc",
                route: "dhikr",
                category: .dhikr,
                destinationBuilder: { AnyView(DhikrTasbihView()) }
            ),
            SpiritualToolItem(
                iconName: "ic_asmaulhusna_3d",
                titleKey: "tool_asmaul_husna_title",
                descKey: "tool_asmaul_husna_desc",
                route: "asmaul-husna",
                category: .dhikr,
                destinationBuilder: { AnyView(AsmaulHusnaView()) }
            ),
            SpiritualToolItem(
                iconName: "ic_manzil_3d",
                titleKey: "tool_manzil_title",
                descKey: "tool_manzil_desc",
                route: "manzil",
                category: .dhikr,
                destinationBuilder: { AnyView(ManzilView()) }
            ),

            // 3. Fiqih & Panduan (FIQH)
            SpiritualToolItem(
                iconName: "ic_zakat_3d",
                titleKey: "tool_zakah_title",
                descKey: "tool_zakah_desc",
                route: "zakat",
                category: .fiqh,
                destinationBuilder: { AnyView(ZakatCalculatorView()) }
            ),
            SpiritualToolItem(
                iconName: "ic_fidyah_3d",
                titleKey: "tool_fidyah_tracker_title",
                descKey: "tool_fidyah_tracker_desc",
                route: "fidyah",
                category: .fiqh,
                destinationBuilder: { AnyView(FidyahCalculatorView()) }
            ),
            SpiritualToolItem(
                iconName: "ic_faraidh_3d",
                titleKey: "tool_faraidh_title",
                descKey: "tool_faraidh_desc",
                route: "faraidh",
                category: .fiqh,
                destinationBuilder: { AnyView(FaraidhCalculatorView()) }
            ),
            SpiritualToolItem(
                iconName: "ic_hajj_umrah_3d",
                titleKey: "tool_hajj_umrah_title",
                descKey: "tool_hajj_umrah_desc",
                route: "hajj-umrah",
                category: .fiqh,
                destinationBuilder: { AnyView(HajjUmrahGuideView()) }
            ),
            SpiritualToolItem(
                iconName: "ic_jenazah_3d",
                titleKey: "tool_janazah_guide_title",
                descKey: "tool_janazah_guide_desc",
                route: "jenazah",
                category: .fiqh,
                destinationBuilder: { AnyView(JanazahGuideView()) }
            ),
            SpiritualToolItem(
                iconName: "ic_encyclopedia_3d",
                titleKey: "tool_encyclopedia_title",
                descKey: "tool_encyclopedia_desc",
                route: "encyclopedia",
                category: .fiqh,
                destinationBuilder: { AnyView(EncyclopediaGuideView()) }
            )
        ]
    }

    private var filteredTools: [SpiritualToolItem] {
        allTools.filter { tool in
            let matchesCategory = (selectedCategory == .all || tool.category == selectedCategory)
            if searchQuery.trimmingCharacters(in: .whitespaces).isEmpty {
                return matchesCategory
            }
            let query = searchQuery.trimmingCharacters(in: .whitespaces).lowercased()
            let title = languageManager.localize(tool.titleKey).lowercased()
            let desc = languageManager.localize(tool.descKey).lowercased()
            return matchesCategory && (title.contains(query) || desc.contains(query))
        }
    }

    private let columns = [
        GridItem(.flexible(), spacing: 14),
        GridItem(.flexible(), spacing: 14)
    ]

    var body: some View {
        ZStack(alignment: .top) {
            SaatTokens.Colors.homeBg
                .ignoresSafeArea()

            // Header Background Image
            Image("bg_worship_header")
                .resizable()
                .scaledToFill()
                .frame(maxWidth: .infinity)
                .frame(height: 280)
                .clipped()
                .overlay(
                    LinearGradient(
                        colors: [
                            SaatTokens.Colors.homeBg.opacity(0.15),
                            SaatTokens.Colors.homeBg.opacity(0.65),
                            SaatTokens.Colors.homeBg
                        ],
                        startPoint: .top,
                        endPoint: .bottom
                    )
                )
                .ignoresSafeArea(edges: .top)

            ScrollView(showsIndicators: false) {
                VStack(alignment: .leading, spacing: 16) {
                    // Header Title & Subtitle with Safe Area top clearance
                    VStack(alignment: .leading, spacing: 4) {
                        Text(languageManager.localize("worship_header_title"))
                            .font(.system(size: 26, weight: .bold))
                            .foregroundColor(Color(hex: "#153828"))

                        Text(languageManager.localize("worship_header_subtitle"))
                            .font(.system(size: 13, weight: .regular))
                            .foregroundColor(Color(hex: "#64748B"))
                    }
                    .padding(.top, 56)
                    .padding(.horizontal, 20)

                    // Modern Search Bar
                    HStack(spacing: 10) {
                        Image(systemName: "magnifyingglass")
                            .font(.system(size: 16, weight: .medium))
                            .foregroundColor(Color(hex: "#1B4332").opacity(0.7))

                        TextField(
                            languageManager.localize("tool_search_hint"),
                            text: $searchQuery
                        )
                        .font(.system(size: 13, weight: .medium))
                        .foregroundColor(Color(hex: "#1E293B"))

                        if !searchQuery.isEmpty {
                            Button(action: { searchQuery = "" }) {
                                Image(systemName: "xmark.circle.fill")
                                    .font(.system(size: 16))
                                    .foregroundColor(Color(hex: "#94A3B8"))
                            }
                            .buttonStyle(.plain)
                        }
                    }
                    .padding(.horizontal, 14)
                    .frame(height: 48)
                    .background(Color.white)
                    .cornerRadius(16)
                    .overlay(
                        RoundedRectangle(cornerRadius: 16)
                            .stroke(Color(hex: "#E8E2D2"), lineWidth: 1)
                    )
                    .shadow(color: Color.black.opacity(0.03), radius: 4, x: 0, y: 1)
                    .padding(.horizontal, 20)

                    // Category Filter Pills
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: 8) {
                            ForEach(ToolCategory.allCases) { cat in
                                let isSelected = (selectedCategory == cat)
                                Button(action: {
                                    UIImpactFeedbackGenerator(style: .light).impactOccurred()
                                    withAnimation(.easeInOut(duration: 0.2)) {
                                        selectedCategory = cat
                                    }
                                }) {
                                    Text(cat.localizedTitle(languageManager))
                                        .font(.system(size: 13, weight: isSelected ? .bold : .medium))
                                        .padding(.horizontal, 16)
                                        .padding(.vertical, 8)
                                        .background(isSelected ? Color(hex: "#085E43") : Color.white)
                                        .foregroundColor(isSelected ? .white : Color(hex: "#334155"))
                                        .cornerRadius(20)
                                        .overlay(
                                            RoundedRectangle(cornerRadius: 20)
                                                .stroke(isSelected ? Color.clear : Color(hex: "#E8E2D2"), lineWidth: 1)
                                        )
                                        .shadow(color: Color.black.opacity(isSelected ? 0.08 : 0.02), radius: 3, x: 0, y: 1)
                                }
                                .buttonStyle(.plain)
                            }
                        }
                        .padding(.horizontal, 20)
                        .padding(.vertical, 2)
                    }

                    // 14 Tools 2-Column Grid (100% Android Match)
                    if filteredTools.isEmpty {
                        VStack(spacing: 8) {
                            Spacer(minLength: 40)
                            Text(languageManager.localize("tool_search_empty"))
                                .font(.system(size: 14, weight: .medium))
                                .foregroundColor(Color(hex: "#64748B"))
                            Spacer(minLength: 40)
                        }
                        .frame(maxWidth: .infinity)
                        .padding(.horizontal, 20)
                    } else {
                        LazyVGrid(columns: columns, spacing: 14) {
                            ForEach(filteredTools) { tool in
                                NavigationLink(destination: tool.destinationBuilder()) {
                                    VStack(alignment: .center, spacing: 4) {
                                        Image(tool.iconName)
                                            .resizable()
                                            .scaledToFit()
                                            .frame(width: 74, height: 74)
                                            .padding(.bottom, 4)

                                        Text(languageManager.localize(tool.titleKey))
                                            .font(.system(size: 13.5, weight: .semibold))
                                            .foregroundColor(Color(hex: "#085E43"))
                                            .lineLimit(1)
                                            .multilineTextAlignment(.center)

                                        Text(languageManager.localize(tool.descKey))
                                            .font(.system(size: 10.5, weight: .regular))
                                            .foregroundColor(Color(hex: "#64748B"))
                                            .lineLimit(2)
                                            .multilineTextAlignment(.center)
                                            .lineSpacing(1.5)
                                    }
                                    .padding(.horizontal, 10)
                                    .padding(.vertical, 14)
                                    .frame(maxWidth: .infinity)
                                    .frame(height: 176)
                                    .background(Color.white)
                                    .cornerRadius(20)
                                    .overlay(
                                        RoundedRectangle(cornerRadius: 20)
                                            .stroke(Color(hex: "#EAE4D6"), lineWidth: 1)
                                    )
                                    .shadow(color: Color.black.opacity(0.04), radius: 6, x: 0, y: 2)
                                }
                                .buttonStyle(.plain)
                            }
                        }
                        .padding(.horizontal, 20)
                        .padding(.top, 4)
                        .padding(.bottom, 110)
                    }
                }
            }
        }
        .navigationBarHidden(true)
    }
}
