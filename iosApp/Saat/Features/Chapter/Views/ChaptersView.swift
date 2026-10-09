//
//  ChaptersView.swift
//  Saat
//
//  Created by Elmee on 25/04/2026.
//  Copyright © 2026 Elmee. All rights reserved.
//

import SwiftUI

struct ChaptersView: View {
    @Environment(\.appContainer) private var container
    @State private var vm: QuranChaptersViewModel?
    @State private var navigationPath = NavigationPath()
    @State private var selectedTab: Int = 0
    @State private var isSearchFocused: Bool = false
    @ObservedObject private var languageManager = AppLanguageManager.shared

    var body: some View {
        NavigationStack(path: $navigationPath) {
            ZStack {
                SaatTokens.Colors.screenBackground
                    .ignoresSafeArea()

                if let vm {
                    quranContent(vm)
                } else {
                    LoadingSkeleton()
                }
            }
            .navigationDestination(for: ChapterReaderRoute.self) { route in
                ChapterVersesView(
                    chapter: route.chapter,
                    juzNumber: route.juzNumber,
                    initialVerseNumber: route.initialVerseNumber
                )
                .toolbar(.hidden, for: .tabBar)
                .toolbarBackground(.hidden, for: .navigationBar)
            }
        }
        .id(languageManager.currentLanguage)
        .task {
            guard let c = container, vm == nil else { return }
            let model = QuranChaptersViewModel(
                content: c.content,
                readingSessions: c.readingSessions,
                language: languageManager.currentLanguage.rawValue
            )
            vm = model
            await model.refreshAll()
        }
    }

    @ViewBuilder
    private func quranContent(_ vm: QuranChaptersViewModel) -> some View {
        @Bindable var bindable = vm

        VStack(spacing: 0) {
            // Header with bg_quran_header
            header(bindable)

            // Content List
            if selectedTab == 0 {
                if bindable.isLoading && bindable.chapters.isEmpty {
                    chaptersLoadingBody
                } else if let error = bindable.errorMessage, bindable.chapters.isEmpty {
                    chaptersErrorBody(error) {
                        Task { await bindable.refreshAll(force: true) }
                    }
                } else if bindable.chapters.isEmpty {
                    chaptersEmptyBody
                } else {
                    chaptersList(bindable)
                }
            } else {
                if bindable.isLoadingJuzs && bindable.juzs.isEmpty {
                    chaptersLoadingBody
                } else if let error = bindable.errorMessageJuzs, bindable.juzs.isEmpty {
                    chaptersErrorBody(error) {
                        Task { await bindable.refreshAll(force: true) }
                    }
                } else if bindable.juzs.isEmpty {
                    chaptersEmptyBody
                } else {
                    juzsList(bindable)
                }
            }
        }
        .toolbar(.hidden, for: .navigationBar)
    }

    @ViewBuilder
    private func header(_ vm: QuranChaptersViewModel) -> some View {
        ZStack(alignment: .top) {
            // Header background image
            Image("bg_quran_header")
                .resizable()
                .scaledToFill()
                .frame(maxWidth: .infinity)
                .frame(height: 140)
                .clipped()
                .overlay(
                    LinearGradient(
                        colors: [
                            Color.clear,
                            Color.clear,
                            SaatTokens.Colors.screenBackground.opacity(0.5),
                            SaatTokens.Colors.screenBackground
                        ],
                        startPoint: .top,
                        endPoint: .bottom
                    )
                )
                .ignoresSafeArea(edges: .top)

            VStack(spacing: 12) {
                // Centered Title & Subtitle
                VStack(spacing: 2) {
                    Text(languageManager.localize("quran_title"))
                        .font(.system(size: 20, weight: .semibold))
                        .foregroundColor(Color(hex: 0xFF12_4C31))

                    Text(selectedTab == 0 ? languageManager.localize("quran_subtitle") : languageManager.localize("quran_subtitle_juz"))
                        .font(.system(size: 14, weight: .light))
                        .foregroundColor(Color(hex: 0xFF12_4C31))
                }
                .padding(.top, 8)

                // Search Bar
                QuranSearchBar(
                    text: Binding(
                        get: { vm.searchText },
                        set: { vm.setSearch($0) }
                    ),
                    isFocused: $isSearchFocused,
                    onClear: { vm.clearSearch() }
                )
                .padding(.horizontal, 20)

                // 2-Tab Pill (Surah & Juz)
                if vm.searchText.isEmpty {
                    HStack(spacing: 4) {
                        Button(action: {
                            withAnimation(.easeInOut(duration: 0.2)) {
                                selectedTab = 0
                            }
                        }) {
                            HStack(spacing: 8) {
                                Image("ic_quran_on")
                                    .renderingMode(.template)
                                    .resizable()
                                    .scaledToFit()
                                    .frame(width: 16, height: 16)
                                Text(languageManager.localize("surah"))
                                    .font(.system(size: 14, weight: selectedTab == 0 ? .bold : .medium))
                            }
                            .foregroundColor(selectedTab == 0 ? .white : SaatTokens.Colors.deepEmerald)
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 10)
                            .background(
                                Group {
                                    if selectedTab == 0 {
                                        SaatTokens.Colors.deepEmerald
                                            .cornerRadius(20)
                                    }
                                }
                            )
                        }
                        .buttonStyle(.plain)

                        Button(action: {
                            withAnimation(.easeInOut(duration: 0.2)) {
                                selectedTab = 1
                            }
                        }) {
                            HStack(spacing: 8) {
                                Image("ic_tafsir")
                                    .renderingMode(.template)
                                    .resizable()
                                    .scaledToFit()
                                    .frame(width: 16, height: 16)
                                Text(languageManager.localize("juz"))
                                    .font(.system(size: 14, weight: selectedTab == 1 ? .bold : .medium))
                            }
                            .foregroundColor(selectedTab == 1 ? .white : SaatTokens.Colors.deepEmerald)
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 10)
                            .background(
                                Group {
                                    if selectedTab == 1 {
                                        SaatTokens.Colors.deepEmerald
                                            .cornerRadius(20)
                                    }
                                }
                            )
                        }
                        .buttonStyle(.plain)
                    }
                    .padding(4)
                    .background(Color.white.opacity(0.85))
                    .cornerRadius(24)
                    .overlay(
                        RoundedRectangle(cornerRadius: 24)
                            .stroke(Color.white.opacity(0.9), lineWidth: 1)
                    )
                    .shadow(color: Color.black.opacity(0.04), radius: 6, x: 0, y: 2)
                    .padding(.horizontal, 20)
                }
            }
            .padding(.bottom, 8)
        }
    }

    private var chaptersLoadingBody: some View {
        ScrollView {
            VStack(spacing: 10) {
                ForEach(0..<8, id: \.self) { _ in
                    RoundedRectangle(cornerRadius: 16)
                        .fill(Color.Token.softGrey.opacity(0.3))
                        .frame(height: 76)
                }
            }
            .padding(.horizontal, 20)
            .padding(.bottom, 100)
        }
        .redacted(reason: .placeholder)
    }

    private func chaptersErrorBody(_ message: String, retry: @escaping () -> Void) -> some View {
        VStack(spacing: 16) {
            Spacer()
            Image(systemName: "wifi.exclamationmark")
                .font(.system(size: 40))
                .foregroundColor(SaatTokens.Colors.deepEmerald.opacity(0.5))
            Text(message)
                .font(.subheadline)
                .foregroundColor(.secondary)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 32)
            Button(languageManager.localize("try_again"), action: retry)
                .buttonStyle(.borderedProminent)
                .tint(SaatTokens.Colors.deepEmerald)
            Spacer()
        }
    }

    private var chaptersEmptyBody: some View {
        VStack(spacing: 12) {
            Spacer()
            Image(systemName: "book.closed")
                .font(.system(size: 40))
                .foregroundColor(SaatTokens.Colors.deepEmerald.opacity(0.5))
            Text(languageManager.localize("no_chapters"))
                .font(.subheadline)
                .foregroundColor(.secondary)
            Spacer()
        }
    }

    private func chaptersList(_ vm: QuranChaptersViewModel) -> some View {
        let displayed = vm.filteredChapters
        return ScrollView {
            LazyVStack(spacing: 8) {
                // Continue reading card if not searching
                if vm.searchText.isEmpty, let route = vm.continueReadingRoute(), let ch = route.chapter {
                    NavigationLink(value: route) {
                        TodayContinueReadingCard(
                            session: ReadingSession(id: "cr", updatedAt: nil, chapterNumber: ch.id, verseNumber: route.initialVerseNumber ?? 1),
                            chapterName: ch.displayComplexName,
                            onTap: {}
                        )
                    }
                    .buttonStyle(.plain)
                    .padding(.bottom, 4)
                }

                if displayed.isEmpty && vm.searchText.isEmpty == false {
                    searchEmptyState(query: vm.searchText)
                        .padding(.top, 32)
                } else {
                    ForEach(displayed, id: \.id) { chapter in
                        NavigationLink(value: ChapterReaderRoute(chapter: chapter, juzNumber: nil, initialVerseNumber: nil)) {
                            QuranChapterRow(chapter: chapter)
                        }
                        .buttonStyle(.plain)
                    }
                }
            }
            .padding(.horizontal, 18)
            .padding(.top, 6)
            .padding(.bottom, 120)
        }
        .refreshable {
            await vm.refreshAll(force: true)
        }
    }

    @ViewBuilder
    private func searchEmptyState(query: String) -> some View {
        VStack(spacing: 16) {
            Image(systemName: "magnifyingglass")
                .font(.system(size: 36, weight: .light))
                .foregroundColor(SaatTokens.Colors.deepEmerald.opacity(0.4))
            Text(languageManager.localize("search_no_results"))
                .font(.system(size: 16, weight: .semibold))
                .foregroundColor(SaatTokens.Colors.slate800)
            Text("\"\(query)\"")
                .font(.system(size: 14))
                .foregroundColor(.secondary)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 32)
        }
        .frame(maxWidth: .infinity)
    }

    private func juzsList(_ vm: QuranChaptersViewModel) -> some View {
        ScrollView {
            LazyVStack(spacing: 8) {
                // Target Khatam Header Card
                VStack(alignment: .leading, spacing: 10) {
                    HStack {
                        HStack(spacing: 8) {
                            Image(systemName: "bookmark.fill")
                                .font(.system(size: 14))
                                .foregroundColor(SaatTokens.Colors.deepEmerald)
                            Text("Target Khatam")
                                .font(.system(size: 14, weight: .bold))
                                .foregroundColor(SaatTokens.Colors.slate900)
                        }
                        Spacer()
                        Text("0%")
                            .font(.system(size: 16, weight: .heavy))
                            .foregroundColor(SaatTokens.Colors.deepEmerald)
                    }

                    GeometryReader { geo in
                        ZStack(alignment: .leading) {
                            Capsule()
                                .fill(SaatTokens.Colors.deepEmerald.opacity(0.15))
                                .frame(height: 8)
                            Capsule()
                                .fill(SaatTokens.Colors.deepEmerald)
                                .frame(width: max(8, geo.size.width * 0.05), height: 8)
                        }
                    }
                    .frame(height: 8)

                    HStack {
                        Text("0 dari 30 Juz selesai")
                            .font(.system(size: 12))
                            .foregroundColor(SaatTokens.Colors.slate500)
                        Spacer()
                    }
                }
                .padding(16)
                .background(SaatTokens.Colors.deepEmerald.opacity(0.08))
                .cornerRadius(16)
                .overlay(
                    RoundedRectangle(cornerRadius: 16)
                        .stroke(SaatTokens.Colors.deepEmerald.opacity(0.2), lineWidth: 1.5)
                )
                .padding(.bottom, 4)

                ForEach(vm.juzs, id: \.id) { juz in
                    let start = juz.startChapterAndAyah()
                    let chapter = start.flatMap { chAndAyah in vm.chapters.first(where: { $0.id == chAndAyah.0 }) }

                    NavigationLink(value: ChapterReaderRoute(chapter: nil, juzNumber: juz.juzNumber, initialVerseNumber: start?.1)) {
                        JuzRow(juz: juz, chapter: chapter)
                    }
                    .buttonStyle(.plain)
                }
            }
            .padding(.horizontal, 18)
            .padding(.top, 6)
            .padding(.bottom, 120)
        }
        .refreshable {
            await vm.refreshAll(force: true)
        }
    }
}

// MARK: - Chapter Number Badge (Star Frame)
private struct ChapterNumberBadge: View {
    let number: Int

    var body: some View {
        ZStack {
            Image("frame_number_icon")
                .renderingMode(.template)
                .resizable()
                .scaledToFit()
                .frame(width: 38, height: 38)
                .foregroundColor(SaatTokens.Colors.teal)

            Text("\(number)")
                .font(.system(size: number >= 100 ? 11 : 12, weight: .bold, design: .rounded))
                .foregroundColor(SaatTokens.Colors.slate900)
        }
    }
}

// MARK: - Quran Chapter Row
private struct QuranChapterRow: View {
    let chapter: QuranChapter

    var body: some View {
        HStack(alignment: .center, spacing: 14) {
            // Star Frame Number Badge
            ChapterNumberBadge(number: chapter.id)

            // Info Column
            VStack(alignment: .leading, spacing: 2) {
                Text(chapter.displayComplexName)
                    .font(.system(size: 15, weight: .bold))
                    .foregroundColor(SaatTokens.Colors.slate900)
                    .lineLimit(1)

                if chapter.displayTranslatedName.isEmpty == false {
                    Text(chapter.displayTranslatedName)
                        .font(.system(size: 12, weight: .regular))
                        .foregroundColor(SaatTokens.Colors.slate500)
                        .lineLimit(1)
                }

                HStack(spacing: 8) {
                    Text(chapter.isMeccan ? "Makkiyah" : "Madaniyah")
                        .font(.system(size: 11, weight: .semibold))
                        .foregroundColor(chapter.isMeccan ? Color(hex: 0xFF2E_7D32) : Color(hex: 0xFF15_65C0))
                        .padding(.horizontal, 7)
                        .padding(.vertical, 2)
                        .background(
                            (chapter.isMeccan ? Color(hex: 0xFF2E_7D32) : Color(hex: 0xFF15_65C0)).opacity(0.12)
                        )
                        .cornerRadius(6)

                    if let count = chapter.versesCount {
                        Text("\(count) Ayat")
                            .font(.system(size: 11.5, weight: .medium))
                            .foregroundColor(SaatTokens.Colors.slate500)
                    }
                }
                .padding(.top, 4)
            }
            .frame(maxWidth: .infinity, alignment: .leading)

            // Right: Mecca / Medina 3D illustration badge in 48x48 rounded box
            ZStack {
                RoundedRectangle(cornerRadius: 14, style: .continuous)
                    .fill(Color.white.opacity(0.6))
                    .overlay(
                        RoundedRectangle(cornerRadius: 14, style: .continuous)
                            .stroke(Color.white.opacity(0.8), lineWidth: 1)
                    )
                    .frame(width: 48, height: 48)

                Image(chapter.isMeccan ? "mecca" : "medina")
                    .resizable()
                    .scaledToFit()
                    .frame(width: 42, height: 42)
            }
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 12)
        .background(Color.white)
        .cornerRadius(16)
        .overlay(
            RoundedRectangle(cornerRadius: 16, style: .continuous)
                .stroke(Color(hex: 0xFFEC_E7DE), lineWidth: 1)
        )
        .shadow(color: Color.black.opacity(0.03), radius: 3, x: 0, y: 1)
    }
}

// MARK: - Juz Row
private struct JuzRow: View {
    let juz: QuranJuz
    let chapter: QuranChapter?

    var body: some View {
        HStack(alignment: .center, spacing: 14) {
            ChapterNumberBadge(number: juz.juzNumber)

            VStack(alignment: .leading, spacing: 2) {
                Text("Juz \(juz.juzNumber)")
                    .font(.system(size: 15, weight: .bold))
                    .foregroundColor(SaatTokens.Colors.slate900)

                if let start = juz.startChapterAndAyah() {
                    let surahName = chapter?.displayComplexName ?? "Surah \(start.0)"
                    Text("Mulai dari \(surahName) · Ayat \(start.1)")
                        .font(.system(size: 12, weight: .regular))
                        .foregroundColor(SaatTokens.Colors.slate500)
                        .lineLimit(1)
                }

                if let count = juz.versesCount {
                    Text("\(count) Ayat")
                        .font(.system(size: 11.5, weight: .semibold))
                        .foregroundColor(SaatTokens.Colors.teal)
                        .padding(.top, 4)
                }
            }
            .frame(maxWidth: .infinity, alignment: .leading)

            ZStack {
                Circle()
                    .fill(SaatTokens.Colors.teal.opacity(0.10))
                    .frame(width: 32, height: 32)

                Image(systemName: "arrow.right")
                    .font(.system(size: 12, weight: .bold))
                    .foregroundColor(SaatTokens.Colors.teal)
            }
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 14)
        .background(Color.white)
        .cornerRadius(16)
        .overlay(
            RoundedRectangle(cornerRadius: 16, style: .continuous)
                .stroke(Color(hex: 0xFFEC_E7DE), lineWidth: 1)
        )
        .shadow(color: Color.black.opacity(0.03), radius: 3, x: 0, y: 1)
    }
}
