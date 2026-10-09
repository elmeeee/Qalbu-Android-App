//
//  ChaptersView.swift
//  Saat
//
//  Created by Sufiandy Elmy on 09/10/2026.
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
                Color(hex: "#F9F7F2")
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
                            Color(hex: "#F9F7F2").opacity(0.5),
                            Color(hex: "#F9F7F2")
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
                        .foregroundColor(Color(hex: "#124C31"))

                    Text(selectedTab == 0 ? languageManager.localize("quran_subtitle") : languageManager.localize("quran_subtitle_juz"))
                        .font(.system(size: 14, weight: .light))
                        .foregroundColor(Color(hex: "#124C31"))
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
                                Text(languageManager.localize("quran_tab_surah"))
                                    .font(.system(size: 14, weight: selectedTab == 0 ? .bold : .medium))
                            }
                            .foregroundColor(selectedTab == 0 ? .white : Color(hex: "#085E43"))
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 10)
                            .background(
                                Group {
                                    if selectedTab == 0 {
                                        Color(hex: "#085E43")
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
                                Text(languageManager.localize("quran_tab_juz"))
                                    .font(.system(size: 14, weight: selectedTab == 1 ? .bold : .medium))
                            }
                            .foregroundColor(selectedTab == 1 ? .white : Color(hex: "#085E43"))
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 10)
                            .background(
                                Group {
                                    if selectedTab == 1 {
                                        Color(hex: "#085E43")
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
                        .fill(Color(hex: "#E2E8F0").opacity(0.3))
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
                .foregroundColor(Color(hex: "#085E43").opacity(0.5))
            Text(message)
                .font(.subheadline)
                .foregroundColor(.secondary)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 32)
            Button(languageManager.localize("try_again"), action: retry)
                .buttonStyle(.borderedProminent)
                .tint(Color(hex: "#085E43"))
            Spacer()
        }
    }

    private var chaptersEmptyBody: some View {
        VStack(spacing: 12) {
            Spacer()
            Image(systemName: "book.closed")
                .font(.system(size: 40))
                .foregroundColor(Color(hex: "#085E43").opacity(0.5))
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
                .foregroundColor(Color(hex: "#085E43").opacity(0.4))
            Text(languageManager.localize("search_no_results"))
                .font(.system(size: 16, weight: .semibold))
                .foregroundColor(Color(hex: "#1E293B"))
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
                // Target Khatam Header Card (Matching Android Khatam Header)
                VStack(alignment: .leading, spacing: 10) {
                    HStack {
                        HStack(spacing: 8) {
                            Image(systemName: "bookmark.fill")
                                .font(.system(size: 14))
                                .foregroundColor(Color(hex: "#085E43"))
                            Text(languageManager.localize("khatam_progress_title"))
                                .font(.system(size: 14, weight: .bold))
                                .foregroundColor(Color(hex: "#0F172A"))
                        }
                        Spacer()
                        Text("0%")
                            .font(.system(size: 16, weight: .heavy))
                            .foregroundColor(Color(hex: "#085E43"))
                    }

                    GeometryReader { geo in
                        ZStack(alignment: .leading) {
                            Capsule()
                                .fill(Color(hex: "#085E43").opacity(0.15))
                                .frame(height: 8)
                            Capsule()
                                .fill(Color(hex: "#085E43"))
                                .frame(width: max(8, geo.size.width * 0.05), height: 8)
                        }
                    }
                    .frame(height: 8)

                    HStack {
                        Text(String(format: languageManager.localize("khatam_progress"), 0))
                            .font(.system(size: 12))
                            .foregroundColor(Color(hex: "#64748B"))
                        Spacer()
                    }
                }
                .padding(16)
                .background(Color(hex: "#085E43").opacity(0.08))
                .cornerRadius(16)
                .overlay(
                    RoundedRectangle(cornerRadius: 16)
                        .stroke(Color(hex: "#085E43").opacity(0.2), lineWidth: 1.5)
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

// MARK: - Chapter Number Badge (Star Frame matching Android frame_number_icon)
private struct ChapterNumberBadge: View {
    let number: Int

    var body: some View {
        ZStack {
            Image("frame_number_icon")
                .renderingMode(.template)
                .resizable()
                .scaledToFit()
                .frame(width: 38, height: 38)
                .foregroundColor(Color(hex: "#0E7490"))

            Text("\(number)")
                .font(.system(size: number >= 100 ? 11 : 12, weight: .bold, design: .rounded))
                .foregroundColor(Color(hex: "#0F172A"))
        }
    }
}

// MARK: - Quran Chapter Row (100% Android Match)
private struct QuranChapterRow: View {
    let chapter: QuranChapter
    @ObservedObject private var languageManager = AppLanguageManager.shared

    var body: some View {
        HStack(alignment: .center, spacing: 14) {
            // Star Frame Number Badge
            ChapterNumberBadge(number: chapter.id)

            // Info Column
            VStack(alignment: .leading, spacing: 2) {
                Text(chapter.displayComplexName)
                    .font(.system(size: 15, weight: .bold))
                    .foregroundColor(Color(hex: "#0F172A"))
                    .lineLimit(1)

                if chapter.displayTranslatedName.isEmpty == false {
                    Text(chapter.displayTranslatedName)
                        .font(.system(size: 12, weight: .regular))
                        .foregroundColor(Color(hex: "#64748B"))
                        .lineLimit(1)
                }

                HStack(spacing: 8) {
                    Text(chapter.isMeccan ? languageManager.localize("revelation_place_makkah") : languageManager.localize("revelation_place_madinah"))
                        .font(.system(size: 11, weight: .semibold))
                        .foregroundColor(chapter.isMeccan ? Color(hex: "#2E7D32") : Color(hex: "#1565C0"))
                        .padding(.horizontal, 7)
                        .padding(.vertical, 2)
                        .background(
                            (chapter.isMeccan ? Color(hex: "#2E7D32") : Color(hex: "#1565C0")).opacity(0.12)
                        )
                        .cornerRadius(6)

                    if let count = chapter.versesCount {
                        Text(String(format: languageManager.localize("verse_count_format"), count))
                            .font(.system(size: 11.5, weight: .medium))
                            .foregroundColor(Color(hex: "#64748B"))
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
                .stroke(Color(hex: "#ECE7DE"), lineWidth: 1)
        )
        .shadow(color: Color.black.opacity(0.03), radius: 3, x: 0, y: 1)
    }
}

// MARK: - Juz Row (100% Android Match)
private struct JuzRow: View {
    let juz: QuranJuz
    let chapter: QuranChapter?
    @ObservedObject private var languageManager = AppLanguageManager.shared

    var body: some View {
        HStack(alignment: .center, spacing: 14) {
            ChapterNumberBadge(number: juz.juzNumber)

            VStack(alignment: .leading, spacing: 2) {
                Text(String(format: languageManager.localize("juz_number"), juz.juzNumber))
                    .font(.system(size: 15, weight: .bold))
                    .foregroundColor(Color(hex: "#0F172A"))

                if let start = juz.startChapterAndAyah() {
                    let surahName = chapter?.displayComplexName ?? String(format: languageManager.localize("surah_number"), start.0)
                    let ayahText = String(format: languageManager.localize("verse_number"), start.1)
                    Text(String(format: languageManager.localize("juz_starts_at"), "\(surahName) · \(ayahText)"))
                        .font(.system(size: 12, weight: .regular))
                        .foregroundColor(Color(hex: "#64748B"))
                        .lineLimit(1)
                }

                if let count = juz.versesCount {
                    Text(String(format: languageManager.localize("verse_count_format"), count))
                        .font(.system(size: 11.5, weight: .semibold))
                        .foregroundColor(Color(hex: "#0E7490"))
                        .padding(.top, 4)
                }
            }
            .frame(maxWidth: .infinity, alignment: .leading)

            ZStack {
                Circle()
                    .fill(Color(hex: "#0E7490").opacity(0.10))
                    .frame(width: 32, height: 32)

                Image(systemName: "arrow.right")
                    .font(.system(size: 12, weight: .bold))
                    .foregroundColor(Color(hex: "#0E7490"))
            }
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 14)
        .background(Color.white)
        .cornerRadius(16)
        .overlay(
            RoundedRectangle(cornerRadius: 16, style: .continuous)
                .stroke(Color(hex: "#ECE7DE"), lineWidth: 1)
        )
        .shadow(color: Color.black.opacity(0.03), radius: 3, x: 0, y: 1)
    }
}
