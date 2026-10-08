//
//  SpiritualToolsView.swift
//  Saat
//
//  Created by Elmee on 25/06/2026.
//  Copyright © 2026 Elmee. All rights reserved.
//

import SwiftUI

enum ToolCategory: String, CaseIterable, Identifiable {
    case all = "Semua"
    case prayer = "Shalat & Waktu"
    case dhikr = "Dzikir & Doa"
    case fiqh = "Fiqih & Panduan"

    var id: String { rawValue }
}

private struct SpiritualToolItem: Identifiable {
    var id: String { route }
    let iconName: String
    let title: String
    let desc: String
    let route: String
    let category: ToolCategory
    let destination: AnyView
}

struct SpiritualToolsView: View {
    @ObservedObject private var languageManager = AppLanguageManager.shared
    @State private var searchQuery: String = ""
    @State private var selectedCategory: ToolCategory = .all

    private var allTools: [SpiritualToolItem] {
        [
            // Prayer & Time
            SpiritualToolItem(
                iconName: "ic_qibla_3d",
                title: "Arah Kiblat",
                desc: "Kompas presisi tinggi & AR",
                route: "qibla",
                category: .prayer,
                destination: AnyView(QiblaFinderView().toolbar(.hidden, for: .tabBar))
            ),
            SpiritualToolItem(
                iconName: "ic_sunnah_3d",
                title: "Shalat Sunnah",
                desc: "Panduan & niat shalat sunnah",
                route: "sunnah-prayer",
                category: .prayer,
                destination: AnyView(QiyamTrackerView().toolbar(.hidden, for: .tabBar))
            ),
            SpiritualToolItem(
                iconName: "ic_jamak_3d",
                title: "Jamak & Qashar",
                desc: "Syarat & tata cara musafir",
                route: "jamak-qashar",
                category: .prayer,
                destination: AnyView(QiyamTrackerView().toolbar(.hidden, for: .tabBar))
            ),
            SpiritualToolItem(
                iconName: "ic_radio_3d",
                title: "Radio Quran",
                desc: "Siaran murottal 24 jam nonstop",
                route: "radio",
                category: .prayer,
                destination: AnyView(QiyamTrackerView().toolbar(.hidden, for: .tabBar))
            ),

            // Dhikr & Du'a
            SpiritualToolItem(
                iconName: "ic_doazikir_3d",
                title: "Doa & Dzikir",
                desc: "Hisnul Muslim, dzikir pagi petang",
                route: "doa-zikir",
                category: .dhikr,
                destination: AnyView(DoaZikirView().toolbar(.hidden, for: .tabBar))
            ),
            SpiritualToolItem(
                iconName: "ic_tasbih_3d",
                title: "Tasbih Digital",
                desc: "Hitung wirid & dzikir dengan haptic",
                route: "dhikr",
                category: .dhikr,
                destination: AnyView(DhikrTasbihView().toolbar(.hidden, for: .tabBar))
            ),
            SpiritualToolItem(
                iconName: "ic_asmaulhusna_3d",
                title: "Asmaul Husna",
                desc: "99 Nama Allah beserta makna & audio",
                route: "asmaul-husna",
                category: .dhikr,
                destination: AnyView(DoaZikirView().toolbar(.hidden, for: .tabBar))
            ),
            SpiritualToolItem(
                iconName: "ic_manzil_3d",
                title: "Manzil & Ruqyah",
                desc: "Ayat-ayat perlindungan harian",
                route: "manzil",
                category: .dhikr,
                destination: AnyView(ManzilView().toolbar(.hidden, for: .tabBar))
            ),

            // Fiqh & Guides
            SpiritualToolItem(
                iconName: "ic_zakat_3d",
                title: "Kalkulator Zakat",
                desc: "Hitung zakat maal, emas, & fitrah",
                route: "zakat",
                category: .fiqh,
                destination: AnyView(ZakatCalculatorView().toolbar(.hidden, for: .tabBar))
            ),
            SpiritualToolItem(
                iconName: "ic_fidyah_3d",
                title: "Hitung Fidyah",
                desc: "Kalkulasi tanggungan fidyah puasa",
                route: "fidyah",
                category: .fiqh,
                destination: AnyView(ZakatCalculatorView().toolbar(.hidden, for: .tabBar))
            ),
            SpiritualToolItem(
                iconName: "ic_faraidh_3d",
                title: "Kalkulator Waris",
                desc: "Bagi warisan sesuai syariat Islam",
                route: "faraidh",
                category: .fiqh,
                destination: AnyView(FaraidhCalculatorView().toolbar(.hidden, for: .tabBar))
            ),
            SpiritualToolItem(
                iconName: "ic_hajj_umrah_3d",
                title: "Haji & Umrah",
                desc: "Panduan manasik haji & umrah praktis",
                route: "hajj-umrah",
                category: .fiqh,
                destination: AnyView(DoaZikirView().toolbar(.hidden, for: .tabBar))
            ),
            SpiritualToolItem(
                iconName: "ic_jenazah_3d",
                title: "Panduan Jenazah",
                desc: "Tata cara memandikan hingga shalat",
                route: "jenazah",
                category: .fiqh,
                destination: AnyView(DoaZikirView().toolbar(.hidden, for: .tabBar))
            ),
            SpiritualToolItem(
                iconName: "ic_encyclopedia_3d",
                title: "Ensiklopedia Islam",
                desc: "Rangkuman tanya jawab & fatwa fiqih",
                route: "encyclopedia",
                category: .fiqh,
                destination: AnyView(DoaZikirView().toolbar(.hidden, for: .tabBar))
            )
        ]
    }

    private var filteredTools: [SpiritualToolItem] {
        allTools.filter { tool in
            let matchesCategory = selectedCategory == .all || tool.category == selectedCategory
            let matchesSearch = searchQuery.isEmpty || tool.title.localizedCaseInsensitiveContains(searchQuery) || tool.desc.localizedCaseInsensitiveContains(searchQuery)
            return matchesCategory && matchesSearch
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
                .frame(height: 250)
                .clipped()
                .overlay(
                    LinearGradient(
                        colors: [
                            SaatTokens.Colors.homeBg.opacity(0.20),
                            SaatTokens.Colors.homeBg.opacity(0.65),
                            SaatTokens.Colors.homeBg
                        ],
                        startPoint: .top,
                        endPoint: .bottom
                    )
                )
                .ignoresSafeArea(edges: .top)

            ScrollView {
                VStack(alignment: .leading, spacing: 14) {
                    // Header Title & Subtitle
                    VStack(alignment: .leading, spacing: 3) {
                        Text("Ruang Ibadah & Fiqih")
                            .font(.system(size: 24, weight: .bold))
                            .foregroundColor(SaatTokens.Colors.homeDarkGreen)

                        Text("Kumpulan panduan & sarana ibadah harianmu")
                            .font(.system(size: 13, weight: .regular))
                            .foregroundColor(SaatTokens.Colors.slate700)
                    }
                    .padding(.top, 16)

                    // Search Bar
                    HStack(spacing: 10) {
                        Image(systemName: "magnifyingglass")
                            .font(.system(size: 16, weight: .medium))
                            .foregroundColor(SaatTokens.Colors.homeDarkGreen.opacity(0.7))

                        TextField("Cari fitur atau panduan…", text: $searchQuery)
                            .font(.system(size: 13, weight: .medium))
                            .foregroundColor(SaatTokens.Colors.slate900)

                        if !searchQuery.isEmpty {
                            Button(action: { searchQuery = "" }) {
                                Image(systemName: "xmark.circle.fill")
                                    .font(.system(size: 16))
                                    .foregroundColor(SaatTokens.Colors.slate500)
                            }
                            .buttonStyle(.plain)
                        }
                    }
                    .padding(.horizontal, 14)
                    .frame(height: 48)
                    .background(SaatTokens.Colors.pureWhite)
                    .cornerRadius(16)
                    .overlay(
                        RoundedRectangle(cornerRadius: 16)
                            .stroke(Color(hex: 0xFFE8_E2D2), lineWidth: 1)
                    )
                    .shadow(color: Color.black.opacity(0.02), radius: 4, x: 0, y: 1)

                    // Category Filter Pills
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: 8) {
                            ForEach(ToolCategory.allCases) { cat in
                                let isSelected = selectedCategory == cat
                                Button(action: {
                                    UIImpactFeedbackGenerator(style: .light).impactOccurred()
                                    withAnimation(.easeInOut(duration: 0.2)) {
                                        selectedCategory = cat
                                    }
                                }) {
                                    Text(cat.rawValue)
                                        .font(.system(size: 13, weight: isSelected ? .bold : .medium))
                                        .foregroundColor(isSelected ? .white : SaatTokens.Colors.slate700)
                                        .padding(.horizontal, 16)
                                        .padding(.vertical, 8)
                                        .background(isSelected ? SaatTokens.Colors.deepEmerald : Color.white)
                                        .cornerRadius(20)
                                        .overlay(
                                            RoundedRectangle(cornerRadius: 20)
                                                .stroke(isSelected ? SaatTokens.Colors.deepEmerald : Color(hex: 0xFFE8_E2D2), lineWidth: 1)
                                        )
                                }
                                .buttonStyle(.plain)
                            }
                        }
                    }

                    // 2-Column Grid
                    LazyVGrid(columns: columns, spacing: 14) {
                        ForEach(filteredTools) { tool in
                            NavigationLink(destination: tool.destination) {
                                SpiritualToolGridCard(tool: tool)
                            }
                            .buttonStyle(.plain)
                        }
                    }
                    .padding(.top, 4)
                }
                .padding(.horizontal, 18)
                .padding(.bottom, 120)
            }
        }
        .toolbar(.hidden, for: .navigationBar)
    }
}

// MARK: - Spiritual Tool Grid Card
private struct SpiritualToolGridCard: View {
    let tool: SpiritualToolItem

    var body: some View {
        VStack(spacing: 6) {
            Image(tool.iconName)
                .resizable()
                .scaledToFit()
                .frame(width: 76, height: 76)
                .padding(.bottom, 2)

            Text(tool.title)
                .font(.system(size: 13.5, weight: .semibold))
                .foregroundColor(SaatTokens.Colors.homeDarkGreen)
                .multilineTextAlignment(.center)
                .lineLimit(1)

            Text(tool.desc)
                .font(.system(size: 10.5, weight: .regular))
                .foregroundColor(SaatTokens.Colors.slate700)
                .multilineTextAlignment(.center)
                .lineLimit(2)
                .frame(height: 28)
        }
        .padding(.horizontal, 10)
        .padding(.vertical, 14)
        .frame(maxWidth: .infinity)
        .frame(height: 176)
        .background(SaatTokens.Colors.pureWhite)
        .cornerRadius(20)
        .overlay(
            RoundedRectangle(cornerRadius: 20, style: .continuous)
                .stroke(Color(hex: 0xFFEA_E4D6), lineWidth: 1)
        )
        .shadow(color: Color.black.opacity(0.04), radius: 6, x: 0, y: 2)
    }
}
