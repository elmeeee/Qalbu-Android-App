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

    var body: some View {
        GeometryReader { geometry in
            let contentWidth = min(geometry.size.width, 840)
            let horizontalInset: CGFloat = contentWidth >= 700 ? 28 : 20
            let columnCount = contentWidth >= 700 ? 4 : 2
            let gridColumns = Array(
                repeating: GridItem(.flexible(minimum: 0), spacing: 14, alignment: .top),
                count: columnCount
            )

            ZStack(alignment: .top) {
                SaatTokens.Colors.homeBg
                    .ignoresSafeArea()

                Image("bg_worship_header")
                    .resizable()
                    .scaledToFill()
                    .frame(width: geometry.size.width, height: 300)
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
                    .frame(maxWidth: .infinity, alignment: .top)

                ScrollView(showsIndicators: false) {
                    VStack(alignment: .leading, spacing: 16) {
                        VStack(alignment: .leading, spacing: 4) {
                            Text(languageManager.localize("worship_header_title"))
                                .font(.system(size: 26, weight: .bold))
                                .foregroundColor(Color(hex: "#153828"))

                            Text(languageManager.localize("worship_header_subtitle"))
                                .font(.system(size: 13, weight: .regular))
                                .foregroundColor(Color(hex: "#64748B"))
                        }
                        .padding(.top, 24)
                        .padding(.horizontal, horizontalInset)

                        HStack(spacing: 10) {
                            Image(systemName: "magnifyingglass")
                                .font(.system(size: 16, weight: .medium))
                                .foregroundColor(Color(hex: "#1B4332").opacity(0.7))

                            TextField(
                                languageManager.localize("tool_search_hint"),
                                text: $searchQuery
                            )
                            .font(.system(size: 14, weight: .medium))
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
                        .frame(height: 50)
                        .background(.white, in: RoundedRectangle(cornerRadius: 16, style: .continuous))
                        .overlay {
                            RoundedRectangle(cornerRadius: 16, style: .continuous)
                                .stroke(Color(hex: "#E8E2D2"), lineWidth: 1)
                        }
                        .shadow(color: Color.black.opacity(0.04), radius: 8, x: 0, y: 3)
                        .padding(.horizontal, horizontalInset)

                        ScrollView(.horizontal, showsIndicators: false) {
                            HStack(spacing: 8) {
                                ForEach(ToolCategory.allCases) { category in
                                    let isSelected = selectedCategory == category
                                    Button {
                                        UIImpactFeedbackGenerator(style: .light).impactOccurred()
                                        withAnimation(.easeInOut(duration: 0.2)) {
                                            selectedCategory = category
                                        }
                                    } label: {
                                        Text(category.localizedTitle(languageManager))
                                            .font(.system(size: 13, weight: isSelected ? .bold : .medium))
                                            .padding(.horizontal, 16)
                                            .padding(.vertical, 9)
                                            .background(
                                                isSelected ? Color(hex: "#085E43") : .white,
                                                in: Capsule()
                                            )
                                            .foregroundStyle(
                                                isSelected ? .white : Color(hex: "#334155")
                                            )
                                            .overlay {
                                                Capsule()
                                                    .stroke(
                                                        isSelected ? .clear : Color(hex: "#E8E2D2"),
                                                        lineWidth: 1
                                                    )
                                            }
                                    }
                                    .buttonStyle(.plain)
                                }
                            }
                            .padding(.horizontal, horizontalInset)
                            .padding(.vertical, 2)
                        }

                        if filteredTools.isEmpty {
                            Text(languageManager.localize("tool_search_empty"))
                                .font(.system(size: 14, weight: .medium))
                                .foregroundColor(Color(hex: "#64748B"))
                                .frame(maxWidth: .infinity, minHeight: 160)
                                .padding(.horizontal, horizontalInset)
                        } else {
                            LazyVGrid(columns: gridColumns, spacing: 14) {
                                ForEach(filteredTools) { tool in
                                    NavigationLink(destination: tool.destinationBuilder()) {
                                        VStack(spacing: 6) {
                                            Image(tool.iconName)
                                                .resizable()
                                                .scaledToFit()
                                                .frame(width: 72, height: 72)

                                            Text(languageManager.localize(tool.titleKey))
                                                .font(.system(size: 14, weight: .semibold))
                                                .foregroundColor(Color(hex: "#085E43"))
                                                .lineLimit(2)
                                                .multilineTextAlignment(.center)

                                            Text(languageManager.localize(tool.descKey))
                                                .font(.system(size: 11, weight: .regular))
                                                .foregroundColor(Color(hex: "#64748B"))
                                                .lineLimit(2)
                                                .multilineTextAlignment(.center)
                                                .lineSpacing(1.5)
                                        }
                                        .padding(.horizontal, 10)
                                        .padding(.vertical, 16)
                                        .frame(maxWidth: .infinity)
                                        .frame(minHeight: 186)
                                        .background(.white, in: RoundedRectangle(cornerRadius: 22, style: .continuous))
                                        .overlay {
                                            RoundedRectangle(cornerRadius: 22, style: .continuous)
                                                .stroke(Color(hex: "#EAE4D6"), lineWidth: 1)
                                        }
                                        .shadow(color: Color(hex: "#263B31").opacity(0.06), radius: 10, x: 0, y: 4)
                                    }
                                    .buttonStyle(.plain)
                                }
                            }
                            .padding(.horizontal, horizontalInset)
                            .padding(.top, 2)
                        }
                    }
                    .frame(width: contentWidth)
                    .frame(maxWidth: .infinity, alignment: .center)
                    .padding(.bottom, 110)
                }
                .frame(width: geometry.size.width)
            }
        }
        .navigationBarHidden(true)
    }
}
