//
//  ChapterVersesView.swift
//  Saat
//
//  Created by Elmee on 25/04/2026.
//  Copyright © 2026 Elmee. All rights reserved.
//

import SwiftUI

struct ChapterVersesView: View {
    @Environment(\.appContainer) private var container
    @Environment(\.dismiss) private var dismiss
    @ObservedObject private var languageManager = AppLanguageManager.shared

    let chapter: QuranChapter?
    var juzNumber: Int? = nil
    var initialVerseNumber: Int? = nil

    @AppStorage("chapterReaderFontScale") private var fontScale = 1.0
    @AppStorage("chapterReaderShowTranslation") private var showTranslation = true
    @AppStorage("chapterReaderShowTransliteration") private var showTransliteration = true
    @AppStorage("chapterReaderMemorizationMode") private var isMemorizationMode = false
    @AppStorage(ChapterReaderPreferences.translationIdKey) private var chapterTranslationId = ChapterReaderPreferences.defaultTranslationId

    @StateObject private var audio = AudioPlayerController()
    @State private var readerCoordinator: ChapterReaderCoordinator?
    @State private var vm: ChapterVersesViewModel?
    @State private var showReadingSettings = false
    @State private var isMenuExpanded = false
    @State private var showAISheet = false
    @State private var showNoteSheet = false
    @State private var toastMessage: String? = nil

    private var showsNowPlaying: Bool { audio.currentURL != nil }

    var body: some View {
        GeometryReader { rootGeo in
            let chromeInsets = ChapterReaderChromeInsets.resolved(
                safeArea: rootGeo.safeAreaInsets,
                showsNowPlaying: showsNowPlaying
            )
            mainContent(chromeInsets: chromeInsets)
        }
        .id(languageManager.currentLanguage)
        .chapterReaderScreenBackground()
        .navigationBarHidden(true)
        .toolbar(.hidden, for: .navigationBar)
        .toolbar(.hidden, for: .tabBar)
        .onAppear {
            if readerCoordinator == nil {
                readerCoordinator = ChapterReaderCoordinator(chapter: chapter, juzNumber: juzNumber, audio: audio)
            }
        }
        .task {
            guard let container, vm == nil, let readerCoordinator else { return }
            let model = readerCoordinator.bootstrap(container: container)
            vm = model
            await model.loadInitial()
            readerCoordinator.applyInitialScrollIfNeeded(vm: model, initialVerseNumber: initialVerseNumber)
            readerCoordinator.lastAppliedTranslationId = chapterTranslationId
        }
        .onChange(of: chapterTranslationId) { _, newId in
            guard let readerCoordinator, readerCoordinator.lastAppliedTranslationId != newId else { return }
            readerCoordinator.lastAppliedTranslationId = newId
            guard let vm else { return }
            audio.stop()
            Task { await vm.applyContentPreferencesChange() }
        }
        .sheet(isPresented: $showReadingSettings) {
            if let vm {
                ChapterReadingSettingsSheetContent(
                    viewModel: vm,
                    fontScale: $fontScale,
                    showTranslation: $showTranslation,
                    onPreferencesChange: {
                        audio.stop()
                        Task { await vm.applyContentPreferencesChange() }
                    }
                )
            }
        }
        .sheet(isPresented: tafsirSheetBinding) {
            if let presenter = readerCoordinator?.tafsirPresenter {
                TafsirReaderSheet(presenter: presenter)
            }
        }
        .sheet(isPresented: hadithSheetBinding) {
            if let presenter = readerCoordinator?.hadithPresenter {
                HadithReaderSheet(presenter: presenter)
            }
        }
        .sheet(isPresented: $showAISheet) {
            if let vm, let verse = readerCoordinator?.currentVerse(in: vm) {
                VerseReflectionSheet(
                    surahName: currentSurahName,
                    verseNumber: verse.resolvedVerseNumber ?? 1,
                    verseText: verse.displayText ?? verse.textUthmani ?? "",
                    translationText: (verse.translations?.first?.text ?? "").strippingHTMLToPlainText(),
                    verseKey: verse.verseKey ?? "",
                    contentRepository: container?.content
                )
            }
        }
        .sheet(isPresented: $showNoteSheet) {
            if let vm, let verse = readerCoordinator?.currentVerse(in: vm), let key = verse.verseKey {
                VerseNoteSheet(
                    surahName: currentSurahName,
                    verseNumber: verse.resolvedVerseNumber ?? 1,
                    verseKey: key,
                    onSave: {
                        showToast(languageManager.currentLanguage == .english ? "Note saved" : "Catatan disimpan")
                    }
                )
            }
        }
        .confirmationDialog(
            languageManager.localize("quran_ayah_options"),
            isPresented: $isMenuExpanded,
            titleVisibility: .visible
        ) {
            Button(languageManager.localize("tafsir")) {
                guard let readerCoordinator, let vm else { return }
                readerCoordinator.openTafsirForCurrentAyah(in: vm)
            }
            Button(languageManager.localize("hadith")) {
                guard let readerCoordinator, let vm else { return }
                readerCoordinator.openHadithForCurrentAyah(in: vm)
            }
            Button(languageManager.localize("ai_reflection")) {
                showAISheet = true
            }
            Button(languageManager.currentLanguage == .english ? "Notes" : "Catatan") {
                showNoteSheet = true
            }
            Button(isCurrentVerseBookmarked ? (languageManager.currentLanguage == .english ? "Remove Bookmark" : "Hapus Bookmark") : (languageManager.currentLanguage == .english ? "Add Bookmark" : "Simpan Bookmark")) {
                toggleCurrentBookmark()
            }
            Button(languageManager.localize("cancel"), role: .cancel) {}
        }
        .onChange(of: audio.activeSequenceIndex) { _, _ in
            guard let vm, let readerCoordinator else { return }
            readerCoordinator.onActiveSequenceIndexChanged(vm: vm)
        }
        .onChange(of: audio.currentURL) { _, url in
            guard let vm, let readerCoordinator else { return }
            readerCoordinator.onAudioURLChanged(url, vm: vm)
        }
        .onDisappear {
            readerCoordinator?.onDisappear()
        }
    }

    @ViewBuilder
    private func mainContent(chromeInsets: ChapterReaderChromeInsets) -> some View {
        ZStack {
            Group {
                if let vm {
                    versePager(vm)
                } else {
                    ChapterReaderBackground()
                }
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .environment(\.chapterReaderChromeInsets, chromeInsets)

            // Top App Bar
            VStack(spacing: 0) {
                topChrome
                Spacer()
            }

            // Bottom Floating Controls
            VStack(spacing: 0) {
                Spacer()
                bottomFloatingControls
            }

            if let toast = toastMessage {
                VStack {
                    Text(toast)
                        .font(.system(size: 14, weight: .bold))
                        .foregroundColor(.white)
                        .padding(.horizontal, 20)
                        .padding(.vertical, 10)
                        .background(Capsule().fill(Color.Token.deepEmerald.opacity(0.95)))
                        .shadow(color: .black.opacity(0.15), radius: 8, y: 4)
                    Spacer()
                }
                .padding(.top, 100)
                .transition(.move(edge: .top).combined(with: .opacity))
                .zIndex(100)
            }

            if vm == nil || (vm?.isLoading == true && vm?.verses.isEmpty == true) {
                ProgressView()
                    .tint(.white)
                    .allowsHitTesting(false)
            }

            if let vm, vm.isLoadingMore {
                ProgressView()
                    .tint(.white)
                    .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .bottom)
                    .padding(.bottom, 80)
                    .allowsHitTesting(false)
            }

            if let vm, vm.isReloadingContent {
                reloadingOverlay
            }
        }
    }

    private var reloadingOverlay: some View {
        Color.black.opacity(0.35)
            .ignoresSafeArea()
            .overlay {
                VStack(spacing: 12) {
                    ProgressView().tint(.white)
                    Text(languageManager.localize("loading"))
                        .font(.subheadline.weight(.medium))
                        .foregroundStyle(.white)
                }
                .padding(20)
                .background(.ultraThinMaterial, in: RoundedRectangle(cornerRadius: 14, style: .continuous))
            }
            .allowsHitTesting(true)
    }

    private var currentSurahName: String {
        if let vm,
           let currentVerse = readerCoordinator?.currentVerse(in: vm),
           let chapterNum = currentVerse.chapterNumber,
           let name = vm.chapterLookup[chapterNum] {
            return name
        }
        return chapter?.displayComplexName ?? vm?.surahDisplayTitle ?? ""
    }

    private var currentPositionLabel: String {
        guard let vm else { return "" }

        if let currentVerse = readerCoordinator?.currentVerse(in: vm) {
            let verseNum = currentVerse.resolvedVerseNumber
            let label = languageManager.currentLanguage == .english ? "Verse" : "Ayat"
            if let verseNum {
                if let juz = currentVerse.juzNumber {
                    return "\(label) \(verseNum) · \(languageManager.localize("juz")) \(juz)"
                }
                return "\(label) \(verseNum)"
            }
        }

        if let firstVerse = vm.verses.first,
           let verseNum = firstVerse.resolvedVerseNumber {
            let label = languageManager.currentLanguage == .english ? "Verse" : "Ayat"
            if let juz = firstVerse.juzNumber {
                return "\(label) \(verseNum) · \(languageManager.localize("juz")) \(juz)"
            }
            return "\(label) \(verseNum)"
        }

        return readerCoordinator?.positionLabel(in: vm) ?? ""
    }

    private var isCurrentVerseBookmarked: Bool {
        guard let vm, let verse = readerCoordinator?.currentVerse(in: vm), let key = verse.verseKey else {
            return false
        }
        let bookmarked = UserDefaults.standard.stringArray(forKey: "bookmarked_verses") ?? []
        return bookmarked.contains(key)
    }

    private func toggleCurrentBookmark() {
        guard let vm, let verse = readerCoordinator?.currentVerse(in: vm), let key = verse.verseKey else { return }
        var bookmarked = UserDefaults.standard.stringArray(forKey: "bookmarked_verses") ?? []
        UIImpactFeedbackGenerator(style: .medium).impactOccurred()
        if bookmarked.contains(key) {
            bookmarked.removeAll(where: { $0 == key })
            showToast(languageManager.currentLanguage == .english ? "Removed bookmark" : "Bookmark dihapus")
        } else {
            bookmarked.append(key)
            showToast(languageManager.currentLanguage == .english ? "Bookmarked successfully" : "Bookmark disimpan")
        }
        UserDefaults.standard.set(bookmarked, forKey: "bookmarked_verses")
    }

    private var topChrome: some View {
        HStack(alignment: .center) {
            Button(action: {
                audio.stop()
                dismiss()
            }) {
                Image(systemName: "arrow.left")
                    .font(.system(size: 20, weight: .semibold))
                    .foregroundColor(Color.Token.slate900)
                    .frame(width: 44, height: 44)
            }

            Spacer()

            VStack(spacing: 2) {
                Text(currentSurahName)
                    .font(.system(size: 17, weight: .bold))
                    .foregroundColor(Color.Token.slate900)
                    .lineLimit(1)

                if !currentPositionLabel.isEmpty {
                    Text(currentPositionLabel)
                        .font(.system(size: 12, weight: .medium))
                        .foregroundColor(Color.Token.slate500)
                        .lineLimit(1)
                }
            }

            Spacer()

            Button(action: toggleCurrentBookmark) {
                Image(systemName: isCurrentVerseBookmarked ? "bookmark.fill" : "bookmark")
                    .font(.system(size: 20, weight: .semibold))
                    .foregroundColor(isCurrentVerseBookmarked ? Color.Token.deepEmerald : Color.Token.slate900)
                    .frame(width: 44, height: 44)
            }
        }
        .padding(.horizontal, 8)
        .padding(.vertical, 6)
        .safeAreaPadding(.top, 4)
        .background(
            LinearGradient(
                colors: [Color.Token.screenBackground.opacity(0.98), Color.Token.screenBackground.opacity(0.85), Color.clear],
                startPoint: .top,
                endPoint: .bottom
            )
        )
    }

    private var bottomFloatingControls: some View {
        HStack(alignment: .center, spacing: 12) {
            // Left: Action Menu (Tafsir & More)
            Button(action: {
                isMenuExpanded = true
            }) {
                ZStack {
                    Circle()
                        .fill(Color.white.opacity(0.85))
                        .frame(width: 48, height: 48)
                        .overlay(Circle().stroke(Color.white.opacity(0.9), lineWidth: 1))
                        .shadow(color: .black.opacity(0.08), radius: 8, y: 3)

                    Image("ic_tafsir")
                        .resizable()
                        .renderingMode(.template)
                        .scaledToFit()
                        .frame(width: 22, height: 22)
                        .foregroundColor(Color.Token.slate800)
                }
            }
            .buttonStyle(.plain)

            Spacer()

            // Center: Audio Player Capsule (Liquid Glass)
            if showsNowPlaying || audio.isPlaying {
                audioCapsule
            }

            Spacer()

            // Right: Reading Settings (Aa)
            Button(action: {
                showReadingSettings = true
            }) {
                ZStack {
                    Circle()
                        .fill(Color.white.opacity(0.85))
                        .frame(width: 48, height: 48)
                        .overlay(Circle().stroke(Color.white.opacity(0.9), lineWidth: 1))
                        .shadow(color: .black.opacity(0.08), radius: 8, y: 3)

                    Text("Aa")
                        .font(.system(size: 18, weight: .bold))
                        .foregroundColor(Color.Token.slate800)
                }
            }
            .buttonStyle(.plain)
        }
        .padding(.horizontal, 20)
        .padding(.bottom, 20)
    }

    private var audioTrackTitle: String {
        guard let vm else { return "" }
        if let currentVerse = readerCoordinator?.currentVerse(in: vm), let num = currentVerse.resolvedVerseNumber {
            return "\(currentSurahName) · \(languageManager.currentLanguage == .english ? "Verse" : "Ayat") \(num)"
        }
        return currentSurahName
    }

    private var audioReciterName: String {
        vm?.reciterDisplayName ?? ""
    }

    private var audioCapsule: some View {
        HStack(spacing: 8) {
            Button(action: {
                if audio.isPlaying {
                    audio.pause()
                } else {
                    audio.toggle()
                }
            }) {
                ZStack {
                    Circle()
                        .fill(Color.Token.deepEmerald)
                        .frame(width: 36, height: 36)

                    Image(systemName: audio.isPlaying ? "pause.fill" : "play.fill")
                        .font(.system(size: 14, weight: .bold))
                        .foregroundColor(.white)
                }
            }

            VStack(alignment: .leading, spacing: 2) {
                Text(audioTrackTitle)
                    .font(.system(size: 12, weight: .bold))
                    .foregroundColor(Color.Token.slate900)
                    .lineLimit(1)

                if !audioReciterName.isEmpty {
                    Text(audioReciterName)
                        .font(.system(size: 10, weight: .regular))
                        .foregroundColor(Color.Token.slate500)
                        .lineLimit(1)
                }
            }

            Button(action: { audio.stop() }) {
                Image(systemName: "xmark")
                    .font(.system(size: 11, weight: .bold))
                    .foregroundColor(Color.Token.slate500)
                    .frame(width: 24, height: 24)
            }
        }
        .padding(.horizontal, 8)
        .padding(.vertical, 6)
        .background(
            Capsule()
                .fill(Color.white.opacity(0.92))
                .overlay(Capsule().stroke(Color.white, lineWidth: 1))
                .shadow(color: .black.opacity(0.12), radius: 8, y: 4)
        )
    }

    private func showToast(_ message: String) {
        toastMessage = message
        Task {
            try? await Task.sleep(nanoseconds: 2_000_000_000)
            withAnimation(.easeOut(duration: 0.35)) {
                if toastMessage == message {
                    toastMessage = nil
                }
            }
        }
    }

    private var tafsirSheetBinding: Binding<Bool> {
        Binding(
            get: { readerCoordinator?.tafsirPresenter?.isSheetPresented ?? false },
            set: { readerCoordinator?.tafsirPresenter?.isSheetPresented = $0 }
        )
    }

    private var hadithSheetBinding: Binding<Bool> {
        Binding(
            get: { readerCoordinator?.hadithPresenter?.isSheetPresented ?? false },
            set: { readerCoordinator?.hadithPresenter?.isSheetPresented = $0 }
        )
    }

    @ViewBuilder
    private func versePager(_ vm: ChapterVersesViewModel) -> some View {
        @Bindable var bindable = vm

        if bindable.isLoading && bindable.verses.isEmpty {
            ChapterReaderBackground()
        } else if let error = bindable.errorMessage, bindable.verses.isEmpty {
            errorOverlay(error) {
                Task { await bindable.loadInitial() }
            }
        } else if bindable.verses.isEmpty {
            Text(languageManager.localize("search_no_results"))
                .foregroundColor(.white)
                .frame(maxWidth: .infinity, maxHeight: .infinity)
        } else if let readerCoordinator {
            @Bindable var readerCoordinator = readerCoordinator

            TabView(selection: $readerCoordinator.scrollPosition) {
                if let ch = chapter {
                    ChapterIntroPage(
                        chapter: ch,
                        isPreparingPlayAll: bindable.isPreparingPlayAll,
                        onPlayAll: { Task { await readerCoordinator.playEntireSurah(vm: bindable) } },
                        onTapScreen: { Task { await readerCoordinator.playEntireSurah(vm: bindable) } }
                    )
                    .tag(ChapterReaderCoordinator.ScrollID.intro as String?)
                }

                ForEach(bindable.verses, id: \.listIdentity) { verse in
                    ChapterAyahPage(
                        verse: verse,
                        showTranslation: showTranslation,
                        showTransliteration: showTransliteration,
                        isMemorizationMode: isMemorizationMode,
                        fontScale: fontScale,
                        isPlaying: audio.isPlayingURL(verse.audio?.url) && audio.isPlaying,
                        onTapScreen: { readerCoordinator.handleTap(for: verse, vm: bindable) }
                    )
                    .tag(verse.listIdentity as String?)
                }
            }
            .tabViewStyle(.page(indexDisplayMode: .never))
            .ignoresSafeArea()
            .onChange(of: readerCoordinator.scrollPosition) { _, newID in
                readerCoordinator.onScrollPositionChanged(newID, vm: bindable)
            }
        }
    }

    private func errorOverlay(_ message: String, retry: @escaping () -> Void) -> some View {
        VStack(spacing: 16) {
            Text(message)
                .font(.subheadline)
                .foregroundColor(.white.opacity(0.85))
                .multilineTextAlignment(.center)
                .padding(.horizontal, 32)
            Button(languageManager.localize("try_again"), action: retry)
                .buttonStyle(.borderedProminent)
                .tint(Color.Token.deepEmerald)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}
