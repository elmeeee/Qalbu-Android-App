//
//  ManzilView.swift
//  Saat
//
//  Created by Elmee on 25/06/2026.
//  Copyright © 2026 Elmee. All rights reserved.
//

import SwiftUI

struct ManzilSection: Identifiable, Sendable {
    let id: Int
    let title: String
    let description: String
    let surah: Int
    let startAyah: Int
    let endAyah: Int
    
    var key: String {
        "\(surah)_\(startAyah)_\(endAyah)"
    }
}

struct ManzilView: View {
    @Environment(\.appContainer) private var container
    @Environment(\.dismiss) private var dismiss
    
    @ObservedObject private var languageManager = AppLanguageManager.shared
    
    @State private var expandedSections: Set<Int> = []
    @State private var versesMap: [Int: [RandomAyahPayload]] = [:]
    @State private var loadingSections: Set<Int> = []
    @State private var errorSections: [Int: String] = [:]
    
    @State private var isAboutExpanded = false
    
    // Canonical 23 protection sections
    private let sections = [
        ManzilSection(id: 1, title: "Al-Fatihah 1–7", description: "Begin Manzil with Al-Fatihah — the greatest surah, recited in every rakah. A cure and protection.", surah: 1, startAyah: 1, endAyah: 7),
        ManzilSection(id: 2, title: "Al-Baqarah 1–5", description: "Continue with the opening of Al-Baqarah — guidance for the God-conscious and the path to success.", surah: 2, startAyah: 1, endAyah: 5),
        ManzilSection(id: 3, title: "Al-Baqarah 163", description: "Your God is One — there is no deity but Him, the Most Gracious, the Most Merciful.", surah: 2, startAyah: 163, endAyah: 163),
        ManzilSection(id: 4, title: "Ayat Kursi (Al-Baqarah 255)", description: "The greatest verse in the Quran — a fortress of protection. Whoever recites it at night remains under Allah's protection until morning.", surah: 2, startAyah: 255, endAyah: 255),
        ManzilSection(id: 5, title: "Al-Baqarah 256", description: "There is no compulsion in religion — truth has become clear from falsehood.", surah: 2, startAyah: 256, endAyah: 256),
        ManzilSection(id: 6, title: "Al-Baqarah 257", description: "Allah is the Protector of the believers, bringing them from darkness into light.", surah: 2, startAyah: 257, endAyah: 257),
        ManzilSection(id: 7, title: "Al-Baqarah 284", description: "To Allah belongs all that is in the heavens and earth — He will call you to account for what is in your hearts.", surah: 2, startAyah: 284, endAyah: 284),
        ManzilSection(id: 8, title: "Al-Baqarah 285", description: "The Messenger and believers have faith in what was revealed — we hear and we obey, seeking Your forgiveness.", surah: 2, startAyah: 285, endAyah: 285),
        ManzilSection(id: 9, title: "Al-Baqarah 286", description: "Allah does not burden a soul beyond its capacity — the closing supplication of Al-Baqarah for forgiveness and help.", surah: 2, startAyah: 286, endAyah: 286),
        ManzilSection(id: 10, title: "Ali 'Imran 18", description: "Allah testifies that there is no deity but Him — the Almighty, the All-Wise.", surah: 3, startAyah: 18, endAyah: 18),
        ManzilSection(id: 11, title: "Ali 'Imran 26", description: "Say: O Allah, Owner of sovereignty — You give honour and power to whom You will.", surah: 3, startAyah: 26, endAyah: 26),
        ManzilSection(id: 12, title: "Ali 'Imran 27", description: "You cause the night to enter the day and bring the living from the dead — You provide for whom You will without measure.", surah: 3, startAyah: 27, endAyah: 27),
        ManzilSection(id: 13, title: "Al-A'raf 54–56", description: "Your Lord created the heavens and earth in six days — call upon Him humbly and in secret.", surah: 7, startAyah: 54, endAyah: 56),
        ManzilSection(id: 14, title: "Al-Isra' 110–111", description: "Call upon Allah or Ar-Rahman — to Him belong the Most Beautiful Names. Praise be to Him who has no son nor partner.", surah: 17, startAyah: 110, endAyah: 111),
        ManzilSection(id: 15, title: "Al-Mu'minun 115–118", description: "Were you created without purpose? Exalted is Allah, the True King — seek His forgiveness and mercy.", surah: 23, startAyah: 115, endAyah: 118),
        ManzilSection(id: 16, title: "Ash-Shaffat 1–11", description: "By the angels ranged in rows — your God is One, Lord of the heavens and the earth.", surah: 37, startAyah: 1, endAyah: 11),
        ManzilSection(id: 17, title: "Ar-Rahman 33–40", description: "O assembly of jinn and men — none can escape Allah's authority. Which of your Lord's favours will you deny?", surah: 55, startAyah: 33, endAyah: 40),
        ManzilSection(id: 18, title: "Al-Hasyr 21–24", description: "If this Quran were sent upon a mountain, it would crumble — He is Allah, the Creator, the Bestower of Forms, the Most Beautiful Names.", surah: 59, startAyah: 21, endAyah: 24),
        ManzilSection(id: 19, title: "Al-Jinn 1–4", description: "A group of jinn heard the Quran, believed, and declared their faith — our Lord has no spouse or child.", surah: 72, startAyah: 1, endAyah: 4),
        ManzilSection(id: 20, title: "Al-Kafirun 1–6", description: "A declaration of disavowal from disbelief — to you your religion, to me mine.", surah: 109, startAyah: 1, endAyah: 6),
        ManzilSection(id: 21, title: "Al-Ikhlas 1–4", description: "The essence of Tawheed — He is Allah, the One, the Eternal Refuge, who begets not nor is begotten.", surah: 112, startAyah: 1, endAyah: 4),
        ManzilSection(id: 22, title: "Al-Falaq 1–5", description: "Seek refuge with the Lord of daybreak from the evil of creation, darkness, sorcery, and envy.", surah: 113, startAyah: 1, endAyah: 5),
        ManzilSection(id: 23, title: "An-Nas 1–6", description: "Seek refuge with the Lord of mankind, the King of mankind, from the whisperer who withdraws.", surah: 114, startAyah: 1, endAyah: 6)
    ]
    
    var body: some View {
        VStack(spacing: 0) {
            // Header
            HStack {
                Button(action: { dismiss() }) {
                    Image(systemName: "arrow.left")
                        .font(.system(size: 20, weight: .semibold))
                        .foregroundColor(SaatTokens.Colors.deepEmerald)
                }
                .accessibilityLabel("Back")
                
                Spacer()
                
                Text(languageManager.localize("tool_manzil"))
                    .font(.system(size: 18, weight: .bold))
                    .foregroundColor(SaatTokens.Colors.slate800)
                
                Spacer()
                
                // Balance header layout
                Color.clear.frame(width: 20, height: 20)
            }
            .padding()
            .background(SaatTokens.Colors.pureWhite)
            .shadow(color: Color.black.opacity(0.03), radius: 3, x: 0, y: 2)
            
            ScrollView {
                VStack(spacing: 16) {
                    // About Manzil Info Card
                    AboutManzilCard(isExpanded: $isAboutExpanded)
                    
                    // Protection Header
                    ManzilProtectionHeaderView()
                        .padding(.horizontal, 4)
                    
                    // Sections list
                    ForEach(sections) { section in
                        SectionCard(
                            section: section,
                            isExpanded: expandedSections.contains(section.id),
                            isLoading: loadingSections.contains(section.id),
                            errorMessage: errorSections[section.id],
                            verses: versesMap[section.id] ?? [],
                            onTap: { toggleSection(section) }
                        )
                    }
                }
                .padding()
            }
            .background(
                LinearGradient(
                    colors: [SaatTokens.Colors.screenBackground, SaatTokens.Colors.sageMist, SaatTokens.Colors.prayerMint],
                    startPoint: .top,
                    endPoint: .bottom
                )
            )
        }
        .navigationBarHidden(true)
        .toolbar(.hidden, for: .navigationBar)
    }
    
    private func toggleSection(_ section: ManzilSection) {
        if expandedSections.contains(section.id) {
            _ = withAnimation(.easeInOut(duration: 0.25)) {
                expandedSections.remove(section.id)
            }
        } else {
            _ = withAnimation(.easeInOut(duration: 0.25)) {
                expandedSections.insert(section.id)
            }
            if versesMap[section.id] == nil && !loadingSections.contains(section.id) {
                Task {
                    await loadVerses(for: section)
                }
            }
        }
    }
    
    private func loadVerses(for section: ManzilSection) async {
        guard let contentRepo = container?.content else {
            errorSections[section.id] = "Database not loaded."
            return
        }
        
        loadingSections.insert(section.id)
        errorSections.removeValue(forKey: section.id)
        
        do {
            let verses = try await contentRepo.getVersesByRange(
                chapterNumber: section.surah,
                startAyah: section.startAyah,
                endAyah: section.endAyah
            )
            versesMap[section.id] = verses
        } catch {
            errorSections[section.id] = "Failed to load verses."
        }
        loadingSections.remove(section.id)
    }
}

// MARK: - About Manzil Card
struct AboutManzilCard: View {
    @Binding var isExpanded: Bool
    @ObservedObject private var languageManager = AppLanguageManager.shared
    
    private let manzilRows = [
        ("١ — ف (fa')", "Juz 1–6", "Al-Fatihah – An-Nisa'"),
        ("٢ — م (mim)", "Juz 6–11", "Al-Ma'idah – At-Taubah"),
        ("٣ — ي (ya')", "Juz 11–14", "Yunus – An-Nahl"),
        ("٤ — ب (ba')", "Juz 15–19", "Al-Isra' – Al-Furqan"),
        ("٥ — ش (syin)", "Juz 19–23", "Asy-Syu'ara – Yasin"),
        ("٦ — w (wau)", "Juz 23–26", "Ash-Shaffat – Al-Hujurat"),
        ("٧ — q (qaf)", "Juz 26–30", "Qaf – An-Nas")
    ]
    
    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            Button(action: {
                withAnimation(.spring(response: 0.3, dampingFraction: 0.8)) {
                    isExpanded.toggle()
                }
            }) {
                HStack {
                    VStack(alignment: .leading, spacing: 4) {
                        Text(languageManager.localize("manzil_about"))
                            .font(.system(size: 16, weight: .bold))
                            .foregroundColor(SaatTokens.Colors.slate800)
                        
                        Text(languageManager.localize("manzil_about_sub"))
                            .font(.system(size: 12))
                            .foregroundColor(SaatTokens.Colors.slate500)
                    }
                    
                    Spacer()
                    
                    Image(systemName: "chevron.down")
                        .font(.system(size: 14, weight: .bold))
                        .foregroundColor(SaatTokens.Colors.indigoAccent)
                        .rotationEffect(.degrees(isExpanded ? 180 : 0))
                }
                .padding(16)
            }
            .buttonStyle(PlainButtonStyle())
            
            if isExpanded {
                Divider()
                    .padding(.horizontal, 16)
                
                VStack(alignment: .leading, spacing: 16) {
                    // Definition
                    VStack(alignment: .leading, spacing: 6) {
                        Text(languageManager.localize("manzil_what_is"))
                            .font(.system(size: 14, weight: .bold))
                            .foregroundColor(SaatTokens.Colors.indigoDeep)
                        
                        Text(languageManager.localize("manzil_what_is_desc"))
                            .font(.system(size: 13))
                            .foregroundColor(SaatTokens.Colors.slate600)
                            .lineSpacing(4)
                    }
                    
                    // Fami bi Syauqin
                    VStack(alignment: .leading, spacing: 6) {
                        Text(languageManager.localize("manzil_fami"))
                            .font(.system(size: 14, weight: .bold))
                            .foregroundColor(SaatTokens.Colors.indigoDeep)
                        
                        Text(languageManager.localize("manzil_fami_desc"))
                            .font(.system(size: 13))
                            .foregroundColor(SaatTokens.Colors.slate600)
                            .lineSpacing(4)
                    }
                    
                    // 7-Day Division Table
                    VStack(alignment: .leading, spacing: 8) {
                        Text(languageManager.localize("manzil_7day"))
                            .font(.system(size: 14, weight: .bold))
                            .foregroundColor(SaatTokens.Colors.indigoDeep)
                        
                        VStack(spacing: 0) {
                            // Header Row
                            HStack {
                                Text(languageManager.localize("manzil_column"))
                                    .frame(width: 90, alignment: .leading)
                                Text(languageManager.localize("juz"))
                                    .frame(width: 80, alignment: .leading)
                                Text(languageManager.localize("manzil_surah_range"))
                                    .frame(maxWidth: .infinity, alignment: .leading)
                            }
                            .font(.system(size: 11, weight: .bold))
                            .foregroundColor(SaatTokens.Colors.slate500)
                            .padding(.vertical, 6)
                            .padding(.horizontal, 8)
                            .background(SaatTokens.Colors.softGrey.opacity(0.3))
                            
                            // Data Rows
                            ForEach(0..<manzilRows.count, id: \.self) { idx in
                                let row = manzilRows[idx]
                                HStack {
                                    Text(row.0)
                                        .frame(width: 90, alignment: .leading)
                                    Text(row.1.replacingOccurrences(of: "Juz", with: languageManager.localize("juz")))
                                        .frame(width: 80, alignment: .leading)
                                    Text(row.2)
                                        .frame(maxWidth: .infinity, alignment: .leading)
                                }
                                .font(.system(size: 12))
                                .foregroundColor(SaatTokens.Colors.slate700)
                                .padding(.vertical, 8)
                                .padding(.horizontal, 8)
                                .background(idx % 2 == 0 ? Color.clear : SaatTokens.Colors.softGrey.opacity(0.15))
                                
                                if idx < manzilRows.count - 1 {
                                    Divider()
                                }
                            }
                        }
                        .cornerRadius(8)
                        .overlay(
                            RoundedRectangle(cornerRadius: 8)
                                .stroke(SaatTokens.Colors.softGrey.opacity(0.5), lineWidth: 1)
                        )
                    }
                    
                    // How to Practice
                    VStack(alignment: .leading, spacing: 6) {
                        Text(languageManager.localize("manzil_how_to"))
                            .font(.system(size: 14, weight: .bold))
                            .foregroundColor(SaatTokens.Colors.indigoDeep)
                        
                        Text(languageManager.localize("manzil_how_to_desc"))
                            .font(.system(size: 13))
                            .foregroundColor(SaatTokens.Colors.slate600)
                            .lineSpacing(4)
                    }
                    
                    // Virtues & Benefits
                    VStack(alignment: .leading, spacing: 8) {
                        Text(languageManager.localize("manzil_virtues"))
                            .font(.system(size: 14, weight: .bold))
                            .foregroundColor(SaatTokens.Colors.indigoDeep)
                        
                        let benefits = [
                            languageManager.localize("manzil_benefit_1"),
                            languageManager.localize("manzil_benefit_2"),
                            languageManager.localize("manzil_benefit_3"),
                            languageManager.localize("manzil_benefit_4"),
                            languageManager.localize("manzil_benefit_5")
                        ]
                        
                        ForEach(benefits, id: \.self) { benefit in
                            HStack(alignment: .top, spacing: 8) {
                                Image(systemName: "checkmark.circle.fill")
                                    .font(.system(size: 13))
                                    .foregroundColor(SaatTokens.Colors.deepEmerald)
                                    .padding(.top, 2)
                                
                                Text(benefit)
                                    .font(.system(size: 13))
                                    .foregroundColor(SaatTokens.Colors.slate600)
                            }
                        }
                    }
                }
                .padding(16)
                .transition(.opacity)
            }
        }
        .background(SaatTokens.Colors.pureWhite)
        .cornerRadius(18)
        .shadow(color: SaatTokens.Colors.indigoAccent.opacity(isExpanded ? 0.10 : 0.04), radius: isExpanded ? 6 : 3, x: 0, y: 2)
        .overlay(
            RoundedRectangle(cornerRadius: 18)
                .stroke(isExpanded ? SaatTokens.Colors.indigoAccent.opacity(0.3) : SaatTokens.Colors.softGrey.opacity(0.5), lineWidth: 1)
        )
    }
}

// MARK: - Section Card
struct SectionCard: View {
    let section: ManzilSection
    let isExpanded: Bool
    let isLoading: Bool
    let errorMessage: String?
    let verses: [RandomAyahPayload]
    let onTap: () -> Void
    
    @ObservedObject private var languageManager = AppLanguageManager.shared
    
    private var gradientPair: (Color, Color) {
        let pairs = [
            (SaatTokens.Colors.deepEmerald, SaatTokens.Colors.teal),
            (SaatTokens.Colors.teal, SaatTokens.Colors.deepEmerald),
            (SaatTokens.Colors.goldDeep, SaatTokens.Colors.gold),
            (SaatTokens.Colors.teal, SaatTokens.Colors.goldDeep),
            (SaatTokens.Colors.indigoAccent, SaatTokens.Colors.teal),
            (SaatTokens.Colors.deepEmerald, SaatTokens.Colors.indigoAccent)
        ]
        let index = (section.id - 1) % pairs.count
        return pairs[index]
    }
    
    var body: some View {
        let accentStart = gradientPair.0
        let accentEnd = gradientPair.1
        
        VStack(alignment: .leading, spacing: 0) {
            Button(action: onTap) {
                HStack(alignment: .top) {
                    ZStack {
                        Circle()
                            .fill(
                                isExpanded ?
                                AnyShapeStyle(LinearGradient(colors: [accentStart, accentEnd], startPoint: .topLeading, endPoint: .bottomTrailing)) :
                                AnyShapeStyle(LinearGradient(colors: [accentStart.opacity(0.14), accentEnd.opacity(0.08)], startPoint: .topLeading, endPoint: .bottomTrailing))
                            )
                        Circle()
                            .stroke(accentStart.opacity(isExpanded ? 0.6 : 0.22), lineWidth: 1)
                        Text("\(section.id)")
                            .font(.system(size: 14, weight: .bold))
                            .foregroundColor(isExpanded ? .white : accentStart)
                    }
                    .frame(width: 38, height: 38)
                    
                    VStack(alignment: .leading, spacing: 4) {
                        Text(section.title)
                            .font(.system(size: 15, weight: .semibold))
                            .foregroundColor(SaatTokens.Colors.slate800)
                            .lineLimit(2)
                        
                        Text(languageManager.localize("manzil_sec_\(section.id)_desc"))
                            .font(.system(size: 12))
                            .foregroundColor(isExpanded ? accentStart.opacity(0.85) : SaatTokens.Colors.slate500)
                            .lineLimit(isExpanded ? nil : 2)
                            .multilineTextAlignment(.leading)
                    }
                    .padding(.leading, 6)
                    
                    Spacer()
                    
                    ZStack {
                        if isExpanded {
                            Circle()
                                .fill(RadialGradient(colors: [accentStart.opacity(0.18), accentEnd.opacity(0.06)], center: .center, startRadius: 0, endRadius: 17))
                                .frame(width: 34, height: 34)
                            Circle()
                                .stroke(accentStart.opacity(0.28), lineWidth: 1)
                                .frame(width: 34, height: 34)
                        }
                        Image(systemName: "chevron.down")
                            .font(.system(size: 14, weight: .bold))
                            .foregroundColor(isExpanded ? accentStart : SaatTokens.Colors.teal)
                            .rotationEffect(.degrees(isExpanded ? 180 : 0))
                    }
                    .frame(width: 34, height: 34)
                    .padding(.top, 2)
                }
                .padding(16)
            }
            .buttonStyle(PlainButtonStyle())
            
            if isExpanded {
                Divider()
                    .padding(.horizontal, 16)
                
                VStack(spacing: 12) {
                    if isLoading {
                        ProgressView()
                            .padding(.vertical, 20)
                    } else if let error = errorMessage {
                        Text(error)
                            .font(.system(size: 13))
                            .foregroundColor(.red)
                            .padding(.vertical, 20)
                    } else {
                        ForEach(verses, id: \.listIdentity) { verse in
                            VStack(alignment: .leading, spacing: 10) {
                                // Verses metadata/label
                                HStack {
                                    Text("\(languageManager.localize("verses")) \(verse.resolvedVerseNumber ?? 0)")
                                        .font(.system(size: 11, weight: .semibold))
                                        .foregroundColor(SaatTokens.Colors.slate400)
                                        .padding(.horizontal, 8)
                                        .padding(.vertical, 3)
                                        .background(SaatTokens.Colors.softGrey.opacity(0.3))
                                        .cornerRadius(4)
                                    
                                    Spacer()
                                }
                                
                                // Arabic text
                                AyahArabicWebBlock(
                                    payload: verse,
                                    style: .verseCard,
                                    fontScale: 1.0,
                                    includeTranslationInAccessibility: true
                                )
                                .padding(.vertical, 4)
                                
                                // Transliteration (Latin)
                                if let translit = verse.transliteration, !translit.isEmpty {
                                    Text(translit)
                                        .font(.system(size: 13, weight: .medium))
                                        .italic()
                                        .foregroundColor(SaatTokens.Colors.indigoAccent)
                                        .lineSpacing(4)
                                        .frame(maxWidth: .infinity, alignment: .leading)
                                        .padding(.top, 4)
                                }
                                
                                // Translation
                                if let translation = verse.translations?.first?.text {
                                    Text(translation)
                                        .font(.system(size: 14))
                                        .foregroundColor(SaatTokens.Colors.slate600)
                                        .lineSpacing(4)
                                        .frame(maxWidth: .infinity, alignment: .leading)
                                        .padding(.top, 4)
                                }
                                
                                Divider()
                                    .padding(.top, 8)
                            }
                            .padding(.horizontal, 16)
                            .padding(.vertical, 8)
                        }
                    }
                }
                .transition(.opacity)
            }
        }
        .background(SaatTokens.Colors.pureWhite)
        .cornerRadius(16)
        .shadow(color: isExpanded ? accentStart.opacity(0.14) : SaatTokens.Colors.slate800.opacity(0.06), radius: isExpanded ? 8 : 2, x: 0, y: isExpanded ? 4 : 1)
        .overlay(
            RoundedRectangle(cornerRadius: 16)
                .stroke(isExpanded ? accentStart.opacity(0.35) : SaatTokens.Colors.softGrey.opacity(0.7), lineWidth: isExpanded ? 1.5 : 1)
        )
    }
}

// MARK: - Manzil Protection Header Card
struct ManzilProtectionHeaderView: View {
    @ObservedObject private var languageManager = AppLanguageManager.shared
    
    var body: some View {
        ZStack {
            LinearGradient(
                colors: [SaatTokens.Colors.deepEmerald, SaatTokens.Colors.tealDark, SaatTokens.Colors.emeraldRich],
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
            
            GeometryReader { geo in
                Circle()
                    .fill(Color.white.opacity(0.05))
                    .frame(width: 120, height: 120)
                    .position(x: geo.size.width - 20, y: 20)
                
                Circle()
                    .fill(Color.white.opacity(0.04))
                    .frame(width: 80, height: 80)
                    .position(x: geo.size.width - 40, y: 60)
            }
            .clipped()
            
            VStack(spacing: 14) {
                HStack(spacing: 14) {
                    ZStack {
                        Circle()
                            .fill(Color.white.opacity(0.15))
                        Circle()
                            .stroke(Color.white.opacity(0.3), lineWidth: 1.5)
                        Image(systemName: "shield.fill")
                            .foregroundColor(.white)
                            .font(.system(size: 26))
                    }
                    .frame(width: 52, height: 52)
                    
                    VStack(alignment: .leading, spacing: 2) {
                        Text(languageManager.localize("manzil_header_title"))
                            .font(.system(size: 18, weight: .bold))
                            .foregroundColor(.white)
                        
                        Capsule()
                            .fill(SaatTokens.Colors.goldBright.opacity(0.85))
                            .frame(width: 40, height: 2)
                    }
                    Spacer()
                }
                
                Divider()
                    .background(Color.white.opacity(0.15))
                
                Text(languageManager.localize("manzil_header_body"))
                    .font(.system(size: 12))
                    .foregroundColor(.white.opacity(0.85))
                    .lineSpacing(4)
                    .frame(maxWidth: .infinity, alignment: .leading)
            }
            .padding(.horizontal, 20)
            .padding(.vertical, 18)
        }
        .cornerRadius(24)
        .shadow(color: SaatTokens.Colors.deepEmerald.opacity(0.25), radius: 12, x: 0, y: 6)
    }
}
