//
//  SpiritualToolsView.swift
//  Saat
//
//  Created by Elmee on 25/06/2026.
//  Copyright © 2026 Elmee. All rights reserved.
//

import SwiftUI

private struct ToolItemData: Identifiable {
    var id: String { route }
    let iconName: String
    let titleKey: String
    let subtitleKey: String
    let route: String
    let accentStart: Color
    let accentEnd: Color
    let destination: AnyView
}

struct SpiritualToolsView: View {
    @ObservedObject private var languageManager = AppLanguageManager.shared
    
    private var tools: [ToolItemData] {
        [
            ToolItemData(
                iconName: "safari.fill",
                titleKey: "tool_qibla",
                subtitleKey: "tool_qibla_sub",
                route: "qibla",
                accentStart: Color.Token.deepEmerald,
                accentEnd: Color.Token.teal,
                destination: AnyView(QiblaFinderView().toolbar(.hidden, for: .tabBar))
            ),
            ToolItemData(
                iconName: "book.pages.fill",
                titleKey: "tool_doa_zikir",
                subtitleKey: "tool_doa_zikir_sub",
                route: "doa-zikir",
                accentStart: Color.Token.teal,
                accentEnd: Color.Token.deepEmerald,
                destination: AnyView(DoaZikirView().toolbar(.hidden, for: .tabBar))
            ),
            ToolItemData(
                iconName: "heart.fill",
                titleKey: "tool_tasbih",
                subtitleKey: "tool_tasbih_sub",
                route: "dhikr",
                accentStart: Color.Token.goldDeep,
                accentEnd: Color.Token.gold,
                destination: AnyView(DhikrTasbihView().toolbar(.hidden, for: .tabBar))
            ),
            ToolItemData(
                iconName: "percent",
                titleKey: "tool_zakat",
                subtitleKey: "tool_zakat_sub",
                route: "zakat",
                accentStart: Color.Token.indigoAccent,
                accentEnd: Color.Token.teal,
                destination: AnyView(ZakatCalculatorView().toolbar(.hidden, for: .tabBar))
            ),
            ToolItemData(
                iconName: "person.3.fill",
                titleKey: "tool_faraidh",
                subtitleKey: "tool_faraidh_sub",
                route: "faraidh",
                accentStart: Color.Token.goldDeep,
                accentEnd: Color.Token.deepEmerald,
                destination: AnyView(FaraidhCalculatorView().toolbar(.hidden, for: .tabBar))
            ),
            ToolItemData(
                iconName: "moon.stars.fill",
                titleKey: "tool_qiyam",
                subtitleKey: "tool_qiyam_sub",
                route: "qiyam",
                accentStart: Color.Token.deepEmerald,
                accentEnd: Color.Token.indigoAccent,
                destination: AnyView(QiyamTrackerView().toolbar(.hidden, for: .tabBar))
            ),
            ToolItemData(
                iconName: "shield.checkerboard",
                titleKey: "tool_manzil",
                subtitleKey: "tool_manzil_sub",
                route: "manzil",
                accentStart: Color.Token.deepEmerald,
                accentEnd: Color.Token.goldDeep,
                destination: AnyView(ManzilView().toolbar(.hidden, for: .tabBar))
            )
        ]
    }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 0) {
                // Header Box
                VStack(alignment: .leading, spacing: 6) {
                    Text(languageManager.localize("tools_title"))
                        .font(.system(size: 24, weight: .bold))
                        .foregroundColor(Color.Token.deepEmerald)
                    
                    Text(languageManager.localize("tools_desc"))
                        .font(.system(size: 14, weight: .regular))
                        .foregroundColor(Color.Token.slate500)
                }
                .padding(.horizontal, 20)
                .padding(.vertical, 22)
                .frame(maxWidth: .infinity, alignment: .leading)
                .background(
                    LinearGradient(
                        colors: [
                            Color.Token.deepEmerald.opacity(0.14),
                            Color.Token.teal.opacity(0.08),
                            Color.Token.gold.opacity(0.06)
                        ],
                        startPoint: .topLeading,
                        endPoint: .bottomTrailing
                    )
                )
                .cornerRadius(22)
                .overlay(
                    RoundedRectangle(cornerRadius: 22)
                        .stroke(
                            LinearGradient(
                                colors: [
                                    Color.Token.deepEmerald.opacity(0.25),
                                    Color.Token.teal.opacity(0.12)
                                ],
                                startPoint: .topLeading,
                                endPoint: .bottomTrailing
                            ),
                            lineWidth: 1
                        )
                )
                .padding(.horizontal, 16)
                .padding(.top, 16)
                .padding(.bottom, 24)
                
                // Tools List
                LazyVStack(spacing: 12) {
                    ForEach(tools) { tool in
                        NavigationLink(destination: tool.destination) {
                            SpiritualToolCard(
                                iconName: tool.iconName,
                                title: languageManager.localize(tool.titleKey),
                                subtitle: languageManager.localize(tool.subtitleKey),
                                accentStart: tool.accentStart,
                                accentEnd: tool.accentEnd
                            )
                        }
                        .buttonStyle(.plain)
                    }
                }
                .padding(.horizontal, 16)
                .padding(.bottom, 32)
            }
        }
        .background(Color.Token.screenBackground)
        .navigationTitle(languageManager.localize("tools_title"))
        .navigationBarTitleDisplayMode(.inline)
    }
}

struct SpiritualToolCard: View {
    let iconName: String
    let title: String
    let subtitle: String
    let accentStart: Color
    let accentEnd: Color

    var body: some View {
        HStack(alignment: .center, spacing: 14) {
            // Icon Box
            ZStack {
                RoundedRectangle(cornerRadius: 14)
                    .fill(
                        LinearGradient(
                            colors: [accentStart.opacity(0.18), accentEnd.opacity(0.10)],
                            startPoint: .topLeading,
                            endPoint: .bottomTrailing
                        )
                    )
                
                Image(systemName: iconName)
                    .font(.system(size: 24, weight: .regular))
                    .foregroundColor(accentStart)
            }
            .frame(width: 48, height: 48)
            
            // Text Content
            VStack(alignment: .leading, spacing: 3) {
                Text(title)
                    .font(.system(size: 16, weight: .semibold))
                    .foregroundColor(Color.Token.slate800)
                    .lineLimit(1)
                
                Text(subtitle)
                    .font(.system(size: 12, weight: .regular))
                    .foregroundColor(Color.Token.slate500)
                    .lineLimit(2)
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            
            // Right Arrow
            Image(systemName: "chevron.right")
                .font(.system(size: 20, weight: .medium))
                .foregroundColor(Color.Token.teal)
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 14)
        .background(Color.Token.pureWhite)
        .cornerRadius(18)
        .overlay(
            RoundedRectangle(cornerRadius: 18)
                .stroke(Color.Token.softGrey.opacity(0.7), lineWidth: 1)
        )
    }
}
