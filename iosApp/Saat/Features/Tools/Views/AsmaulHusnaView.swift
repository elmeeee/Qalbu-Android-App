//
//  AsmaulHusnaView.swift
//  Saat
//
//  Created by Sufiandy Elmy on 09/10/2026.
//  Copyright © 2026 Elmee. All rights reserved.
//

import SwiftUI

struct AsmaulHusnaItem: Identifiable, Codable, Equatable {
    var id: Int { number }
    let number: Int
    let arabic: String
    let latin: String
    let meaningEn: String
    let meaningId: String
    let meaningMs: String
    let dalilEn: String?
    let dalilId: String?
    let dalilMs: String?
    let dalilReference: String?
    let fadhilahEn: String?
    let fadhilahId: String?
    let fadhilahMs: String?
    let recommendedCount: Int?

    func meaning(for lang: AppLanguage) -> String {
        switch lang {
        case .indonesian: return meaningId
        case .malay: return meaningMs.isEmpty ? meaningId : meaningMs
        case .english: return meaningEn.isEmpty ? meaningId : meaningEn
        }
    }

    func dalil(for lang: AppLanguage) -> String? {
        switch lang {
        case .indonesian: return dalilId
        case .malay: return dalilMs ?? dalilId
        case .english: return dalilEn ?? dalilId
        }
    }

    func fadhilah(for lang: AppLanguage) -> String? {
        switch lang {
        case .indonesian: return fadhilahId
        case .malay: return fadhilahMs ?? fadhilahId
        case .english: return fadhilahEn ?? fadhilahId
        }
    }
}

struct AsmaulHusnaView: View {
    @Environment(\.dismiss) private var dismiss
    @ObservedObject private var languageManager = AppLanguageManager.shared

    @State private var items: [AsmaulHusnaItem] = []
    @State private var searchQuery: String = ""
    @State private var selectedItem: AsmaulHusnaItem? = nil
    @State private var bookmarkedNumbers: Set<Int> = []
    @State private var filterOnlyBookmarked: Bool = false

    private var filteredItems: [AsmaulHusnaItem] {
        items.filter { item in
            let matchesBookmark = !filterOnlyBookmarked || bookmarkedNumbers.contains(item.number)
            if searchQuery.trimmingCharacters(in: .whitespaces).isEmpty {
                return matchesBookmark
            }
            let q = searchQuery.trimmingCharacters(in: .whitespaces).lowercased()
            let meaning = item.meaning(for: languageManager.currentLanguage).lowercased()
            return matchesBookmark && (
                item.latin.lowercased().contains(q) ||
                meaning.contains(q) ||
                "\(item.number)".contains(q) ||
                item.arabic.contains(q)
            )
        }
    }

    private let columns = [
        GridItem(.flexible(), spacing: 12),
        GridItem(.flexible(), spacing: 12)
    ]

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
                    Text(languageManager.localize("tool_asmaul_husna_title"))
                        .font(.system(size: 18, weight: .bold))
                        .foregroundColor(Color(hex: "#1E293B"))

                    Text("99 Nama-Nama Agung Allah SWT")
                        .font(.system(size: 12, weight: .regular))
                        .foregroundColor(Color(hex: "#64748B"))
                }

                Spacer()

                Button(action: {
                    withAnimation(.easeInOut(duration: 0.2)) {
                        filterOnlyBookmarked.toggle()
                    }
                }) {
                    Image(systemName: filterOnlyBookmarked ? "bookmark.fill" : "bookmark")
                        .font(.system(size: 16, weight: .semibold))
                        .foregroundColor(filterOnlyBookmarked ? Color(hex: "#D4AF37") : Color(hex: "#64748B"))
                        .frame(width: 40, height: 40)
                        .background(Color.white)
                        .clipShape(Circle())
                        .shadow(color: Color.black.opacity(0.04), radius: 3, x: 0, y: 1)
                }
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 10)
            .background(Color(hex: "#F9F7F2"))

            // Search & Filter
            HStack(spacing: 10) {
                Image(systemName: "magnifyingglass")
                    .foregroundColor(Color(hex: "#085E43").opacity(0.7))

                TextField(languageManager.localize("tool_search_hint"), text: $searchQuery)
                    .font(.system(size: 14))
                    .foregroundColor(Color(hex: "#1E293B"))

                if !searchQuery.isEmpty {
                    Button(action: { searchQuery = "" }) {
                        Image(systemName: "xmark.circle.fill")
                            .foregroundColor(Color(hex: "#94A3B8"))
                    }
                }
            }
            .padding(.horizontal, 14)
            .frame(height: 44)
            .background(Color.white)
            .cornerRadius(12)
            .overlay(
                RoundedRectangle(cornerRadius: 12)
                    .stroke(Color(hex: "#E8E2D2"), lineWidth: 1)
            )
            .padding(.horizontal, 16)
            .padding(.vertical, 8)
            .background(Color(hex: "#F9F7F2"))

            ScrollView {
                LazyVGrid(columns: columns, spacing: 12) {
                    ForEach(filteredItems) { item in
                        AsmaulHusnaCard(
                            item: item,
                            lang: languageManager.currentLanguage,
                            isBookmarked: bookmarkedNumbers.contains(item.number),
                            onTap: { selectedItem = item },
                            onBookmarkToggle: {
                                toggleBookmark(item.number)
                            }
                        )
                    }
                }
                .padding(.horizontal, 16)
                .padding(.top, 8)
                .padding(.bottom, 32)
            }
            .background(Color(hex: "#F9F7F2"))
        }
        .background(Color(hex: "#F9F7F2").ignoresSafeArea())
        .navigationBarHidden(true)
        .toolbar(.hidden, for: .navigationBar)
        .toolbar(.hidden, for: .tabBar)
        .sheet(item: $selectedItem) { item in
            AsmaulHusnaDetailSheet(item: item, lang: languageManager.currentLanguage)
        }
        .onAppear {
            loadCatalog()
            loadBookmarks()
        }
    }

    private func loadCatalog() {
        guard let url = Bundle.main.url(forResource: "asmaul_husna", withExtension: "json", subdirectory: "asmaul_husna") ??
                        Bundle.main.url(forResource: "asmaul_husna", withExtension: "json") else {
            return
        }
        do {
            let data = try Data(contentsOf: url)
            self.items = try JSONDecoder().decode([AsmaulHusnaItem].self, from: data)
        } catch {
            // Error loading
        }
    }

    private func loadBookmarks() {
        let saved = UserDefaults.standard.array(forKey: "asmaul_husna_bookmarks") as? [Int] ?? []
        bookmarkedNumbers = Set(saved)
    }

    private func toggleBookmark(_ num: Int) {
        if bookmarkedNumbers.contains(num) {
            bookmarkedNumbers.remove(num)
        } else {
            bookmarkedNumbers.insert(num)
        }
        UserDefaults.standard.set(Array(bookmarkedNumbers), forKey: "asmaul_husna_bookmarks")
    }
}

// MARK: - Card Component
private struct AsmaulHusnaCard: View {
    let item: AsmaulHusnaItem
    let lang: AppLanguage
    let isBookmarked: Bool
    let onTap: () -> Void
    let onBookmarkToggle: () -> Void

    var body: some View {
        Button(action: onTap) {
            VStack(alignment: .center, spacing: 6) {
                HStack {
                    ZStack {
                        Circle()
                            .fill(Color(hex: "#E6F3EE"))
                            .frame(width: 26, height: 26)
                        Text("\(item.number)")
                            .font(.system(size: 11, weight: .bold))
                            .foregroundColor(Color(hex: "#085E43"))
                    }

                    Spacer()

                    Button(action: onBookmarkToggle) {
                        Image(systemName: isBookmarked ? "bookmark.fill" : "bookmark")
                            .font(.system(size: 13))
                            .foregroundColor(isBookmarked ? Color(hex: "#D4AF37") : Color(hex: "#CBD5E1"))
                    }
                    .buttonStyle(.plain)
                }

                Text(item.arabic)
                    .font(.system(size: 26, weight: .semibold))
                    .foregroundColor(Color(hex: "#085E43"))
                    .multilineTextAlignment(.center)
                    .padding(.vertical, 4)

                Text(item.latin)
                    .font(.system(size: 13, weight: .bold))
                    .foregroundColor(Color(hex: "#1E293B"))
                    .lineLimit(1)

                Text(item.meaning(for: lang))
                    .font(.system(size: 11, weight: .regular))
                    .foregroundColor(Color(hex: "#64748B"))
                    .lineLimit(2)
                    .multilineTextAlignment(.center)
            }
            .padding(12)
            .frame(maxWidth: .infinity)
            .frame(height: 154)
            .background(Color.white)
            .cornerRadius(16)
            .overlay(
                RoundedRectangle(cornerRadius: 16)
                    .stroke(Color(hex: "#EAE4D6"), lineWidth: 1)
            )
            .shadow(color: Color.black.opacity(0.03), radius: 4, x: 0, y: 1)
        }
        .buttonStyle(.plain)
    }
}

// MARK: - Detail Sheet Component
private struct AsmaulHusnaDetailSheet: View {
    let item: AsmaulHusnaItem
    let lang: AppLanguage
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 20) {
                    // Hero Arabic Card
                    VStack(spacing: 8) {
                        ZStack {
                            Circle()
                                .fill(Color(hex: "#E6F3EE"))
                                .frame(width: 44, height: 44)
                            Text("\(item.number)")
                                .font(.system(size: 18, weight: .bold))
                                .foregroundColor(Color(hex: "#085E43"))
                        }

                        Text(item.arabic)
                            .font(.system(size: 44, weight: .bold))
                            .foregroundColor(Color(hex: "#085E43"))
                            .padding(.vertical, 6)

                        Text(item.latin)
                            .font(.system(size: 20, weight: .bold))
                            .foregroundColor(Color(hex: "#1E293B"))

                        Text(item.meaning(for: lang))
                            .font(.system(size: 14, weight: .medium))
                            .foregroundColor(Color(hex: "#64748B"))
                    }
                    .padding(24)
                    .frame(maxWidth: .infinity)
                    .background(Color.white)
                    .cornerRadius(20)
                    .overlay(
                        RoundedRectangle(cornerRadius: 20)
                            .stroke(Color(hex: "#EAE4D6"), lineWidth: 1)
                    )

                    // Dalil Card
                    if let dalil = item.dalil(for: lang), !dalil.isEmpty {
                        VStack(alignment: .leading, spacing: 8) {
                            HStack {
                                Image(systemName: "book.closed.fill")
                                    .foregroundColor(Color(hex: "#085E43"))
                                Text("Dalil Al-Qur'an")
                                    .font(.system(size: 14, weight: .bold))
                                    .foregroundColor(Color(hex: "#085E43"))
                                Spacer()
                                if let ref = item.dalilReference {
                                    Text(ref)
                                        .font(.system(size: 11, weight: .medium))
                                        .foregroundColor(Color(hex: "#64748B"))
                                }
                            }

                            Text(dalil)
                                .font(.system(size: 13, weight: .regular))
                                .foregroundColor(Color(hex: "#334155"))
                                .lineSpacing(4)
                        }
                        .padding(16)
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .background(Color.white)
                        .cornerRadius(16)
                        .overlay(
                            RoundedRectangle(cornerRadius: 16)
                                .stroke(Color(hex: "#EAE4D6"), lineWidth: 1)
                        )
                    }

                    // Fadhilah / Keutamaan Card
                    if let fadhilah = item.fadhilah(for: lang), !fadhilah.isEmpty {
                        VStack(alignment: .leading, spacing: 8) {
                            HStack {
                                Image(systemName: "sparkles")
                                    .foregroundColor(Color(hex: "#D4AF37"))
                                Text("Keutamaan & Khasiat Zikir")
                                    .font(.system(size: 14, weight: .bold))
                                    .foregroundColor(Color(hex: "#1E293B"))
                            }

                            Text(fadhilah)
                                .font(.system(size: 13, weight: .regular))
                                .foregroundColor(Color(hex: "#334155"))
                                .lineSpacing(4)
                        }
                        .padding(16)
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .background(Color(hex: "#FFFBEB"))
                        .cornerRadius(16)
                        .overlay(
                            RoundedRectangle(cornerRadius: 16)
                                .stroke(Color(hex: "#FDE68A"), lineWidth: 1)
                        )
                    }
                }
                .padding(20)
            }
            .background(Color(hex: "#F9F7F2").ignoresSafeArea())
            .navigationTitle(item.latin)
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Tutup") { dismiss() }
                        .foregroundColor(Color(hex: "#085E43"))
                }
            }
        }
        .presentationDetents([.medium, .large])
    }
}
