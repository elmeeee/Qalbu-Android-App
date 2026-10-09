import SwiftUI

struct EncyclopediaTopic: Codable, Identifiable {
    let id: String
    let categoryId: String?
    let title: String
    let titleEn: String?
    let titleMs: String?
    let subtitle: String?
    let subtitleEn: String?
    let subtitleMs: String?
    let icon: String?
    let readTimeMinutes: Int?
    let summary: String?
    let summaryEn: String?
    let summaryMs: String?
    let content: String?
    let contentEn: String?
    let contentMs: String?
    let quranReferences: [EncyclopediaQuranRef]?
    
    func localizedTitle(lang: String) -> String {
        switch lang {
        case "en": return titleEn ?? title
        case "ms": return titleMs ?? title
        default: return title
        }
    }
    
    func localizedSubtitle(lang: String) -> String {
        switch lang {
        case "en": return subtitleEn ?? subtitle ?? ""
        case "ms": return subtitleMs ?? subtitle ?? ""
        default: return subtitle ?? ""
        }
    }
    
    func localizedSummary(lang: String) -> String {
        switch lang {
        case "en": return summaryEn ?? summary ?? ""
        case "ms": return summaryMs ?? summary ?? ""
        default: return summary ?? ""
        }
    }
    
    func localizedContent(lang: String) -> String {
        switch lang {
        case "en": return contentEn ?? content ?? ""
        case "ms": return contentMs ?? content ?? ""
        default: return content ?? ""
        }
    }
}

struct EncyclopediaQuranRef: Codable, Identifiable {
    var id: String { "\(surahNumber ?? 0)_\(ayahRange ?? "")" }
    let surahNumber: Int?
    let surahName: String?
    let surahNameEn: String?
    let surahNameMs: String?
    let ayahRange: String?
    let verseTextAr: String?
    let verseTextTranslation: String?
    let verseTextTranslationEn: String?
    let verseTextTranslationMs: String?
    
    func localizedSurah(lang: String) -> String {
        switch lang {
        case "en": return surahNameEn ?? surahName ?? ""
        case "ms": return surahNameMs ?? surahName ?? ""
        default: return surahName ?? ""
        }
    }
    
    func localizedTranslation(lang: String) -> String {
        switch lang {
        case "en": return verseTextTranslationEn ?? verseTextTranslation ?? ""
        case "ms": return verseTextTranslationMs ?? verseTextTranslation ?? ""
        default: return verseTextTranslation ?? ""
        }
    }
}

struct GlossaryTerm: Codable, Identifiable {
    let id: String
    let term: String
    let termAr: String?
    let definition: String
    let definitionEn: String?
    let definitionMs: String?
    
    func localizedDefinition(lang: String) -> String {
        switch lang {
        case "en": return definitionEn ?? definition
        case "ms": return definitionMs ?? definition
        default: return definition
        }
    }
}

struct EncyclopediaGuideView: View {
    @ObservedObject private var languageManager = AppLanguageManager.shared
    @Environment(\.dismiss) private var dismiss
    
    @State private var topics: [EncyclopediaTopic] = []
    @State private var glossary: [GlossaryTerm] = []
    @State private var selectedTab = 0 // 0: Topics / Prophets, 1: Glossary
    @State private var searchText = ""
    @State private var selectedTopic: EncyclopediaTopic?
    
    var lang: String { languageManager.currentLanguage.rawValue }
    
    var filteredTopics: [EncyclopediaTopic] {
        if searchText.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
            return topics
        }
        let q = searchText.lowercased()
        return topics.filter {
            $0.localizedTitle(lang: lang).lowercased().contains(q) ||
            $0.localizedSubtitle(lang: lang).lowercased().contains(q) ||
            $0.localizedSummary(lang: lang).lowercased().contains(q)
        }
    }
    
    var filteredGlossary: [GlossaryTerm] {
        if searchText.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
            return glossary
        }
        let q = searchText.lowercased()
        return glossary.filter {
            $0.term.lowercased().contains(q) ||
            ($0.termAr?.contains(q) ?? false) ||
            $0.localizedDefinition(lang: lang).lowercased().contains(q)
        }
    }
    
    var body: some View {
        ZStack {
            Color(UIColor.systemGroupedBackground)
                .ignoresSafeArea()
            
            VStack(spacing: 0) {
                // Header Banner
                ZStack(alignment: .bottomLeading) {
                    if let bgImage = UIImage(named: "bg_worship_header") {
                        Image(uiImage: bgImage)
                            .resizable()
                            .aspectRatio(contentMode: .fill)
                            .frame(height: 120)
                            .clipped()
                    } else {
                        LinearGradient(
                            colors: [Color(hex: "1B3B2B"), Color(hex: "2D5A43")],
                            startPoint: .topLeading,
                            endPoint: .bottomTrailing
                        )
                        .frame(height: 120)
                    }
                    
                    LinearGradient(
                        colors: [Color.black.opacity(0.6), Color.clear],
                        startPoint: .bottom,
                        endPoint: .top
                    )
                    .frame(height: 120)
                    
                    VStack(alignment: .leading, spacing: 4) {
                        Text(languageManager.localize("tool_encyclopedia"))
                            .font(.title2.weight(.bold))
                            .foregroundColor(.white)
                        
                        Text(languageManager.localize("tool_encyclopedia_desc"))
                            .font(.caption)
                            .foregroundColor(.white.opacity(0.85))
                    }
                    .padding(.horizontal, 16)
                    .padding(.bottom, 14)
                }
                
                // Segmented Tab Picker
                Picker("", selection: $selectedTab) {
                    Text(languageManager.localize("encyclopedia_topics_tab")).tag(0)
                    Text(languageManager.localize("encyclopedia_glossary_tab")).tag(1)
                }
                .pickerStyle(.segmented)
                .padding(.horizontal, 16)
                .padding(.vertical, 10)
                
                // Search Bar
                HStack {
                    Image(systemName: "magnifyingglass")
                        .foregroundColor(.secondary)
                    TextField(languageManager.localize("search_placeholder"), text: $searchText)
                        .textFieldStyle(.plain)
                    if !searchText.isEmpty {
                        Button {
                            searchText = ""
                        } label: {
                            Image(systemName: "xmark.circle.fill")
                                .foregroundColor(.secondary)
                        }
                    }
                }
                .padding(10)
                .background(Color(UIColor.secondarySystemGroupedBackground))
                .cornerRadius(10)
                .padding(.horizontal, 16)
                .padding(.bottom, 8)
                
                // Content List
                if selectedTab == 0 {
                    ScrollView {
                        LazyVStack(spacing: 12) {
                            ForEach(filteredTopics) { topic in
                                Button {
                                    selectedTopic = topic
                                } label: {
                                    topicRow(topic)
                                }
                                .buttonStyle(.plain)
                            }
                        }
                        .padding(.horizontal, 16)
                        .padding(.vertical, 8)
                    }
                } else {
                    ScrollView {
                        LazyVStack(spacing: 10) {
                            ForEach(filteredGlossary) { term in
                                glossaryRow(term)
                            }
                        }
                        .padding(.horizontal, 16)
                        .padding(.vertical, 8)
                    }
                }
            }
        }
        .navigationTitle(languageManager.localize("tool_encyclopedia"))
        .navigationBarTitleDisplayMode(.inline)
        .toolbar(.hidden, for: .tabBar)
        .sheet(item: $selectedTopic) { topic in
            topicDetailSheet(topic)
        }
        .onAppear {
            loadEncyclopediaData()
        }
    }
    
    // MARK: - Subviews
    private func topicRow(_ topic: EncyclopediaTopic) -> some View {
        HStack(alignment: .top, spacing: 14) {
            ZStack {
                Circle()
                    .fill(Color(hex: "2D5A43").opacity(0.12))
                    .frame(width: 44, height: 44)
                
                Image(systemName: "book.pages.fill")
                    .foregroundColor(Color(hex: "2D5A43"))
                    .font(.system(size: 18))
            }
            
            VStack(alignment: .leading, spacing: 4) {
                HStack {
                    Text(topic.localizedTitle(lang: lang))
                        .font(.headline)
                        .foregroundColor(.primary)
                    Spacer()
                    if let readTime = topic.readTimeMinutes {
                        Text("\(readTime) min")
                            .font(.caption2.weight(.medium))
                            .foregroundColor(.secondary)
                            .padding(.horizontal, 6)
                            .padding(.vertical, 2)
                            .background(Color(UIColor.systemGray5))
                            .cornerRadius(4)
                    }
                }
                
                if !topic.localizedSubtitle(lang: lang).isEmpty {
                    Text(topic.localizedSubtitle(lang: lang))
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
                
                Text(topic.localizedSummary(lang: lang))
                    .font(.caption)
                    .foregroundColor(.secondary)
                    .lineLimit(2)
                    .padding(.top, 2)
            }
        }
        .padding(14)
        .background(Color(UIColor.secondarySystemGroupedBackground))
        .cornerRadius(14)
    }
    
    private func glossaryRow(_ term: GlossaryTerm) -> some View {
        VStack(alignment: .leading, spacing: 6) {
            HStack {
                Text(term.term)
                    .font(.headline)
                    .foregroundColor(Color(hex: "2D5A43"))
                
                Spacer()
                
                if let ar = term.termAr {
                    Text(ar)
                        .font(.custom("Amiri-Bold", size: 18))
                        .foregroundColor(.primary)
                }
            }
            
            Text(term.localizedDefinition(lang: lang))
                .font(.subheadline)
                .foregroundColor(.secondary)
                .fixedSize(horizontal: false, vertical: true)
        }
        .padding(14)
        .background(Color(UIColor.secondarySystemGroupedBackground))
        .cornerRadius(12)
    }
    
    private func topicDetailSheet(_ topic: EncyclopediaTopic) -> some View {
        NavigationView {
            ScrollView {
                VStack(alignment: .leading, spacing: 18) {
                    VStack(alignment: .leading, spacing: 6) {
                        Text(topic.localizedTitle(lang: lang))
                            .font(.title2.weight(.bold))
                            .foregroundColor(.primary)
                        
                        if !topic.localizedSubtitle(lang: lang).isEmpty {
                            Text(topic.localizedSubtitle(lang: lang))
                                .font(.subheadline)
                                .foregroundColor(.secondary)
                        }
                    }
                    
                    Divider()
                    
                    Text(topic.localizedContent(lang: lang))
                        .font(.body)
                        .lineSpacing(6)
                        .foregroundColor(.primary)
                    
                    if let refs = topic.quranReferences, !refs.isEmpty {
                        Divider()
                            .padding(.top, 10)
                        
                        Text(languageManager.localize("quran_references_title"))
                            .font(.headline)
                            .foregroundColor(Color(hex: "2D5A43"))
                        
                        ForEach(refs) { ref in
                            VStack(alignment: .trailing, spacing: 10) {
                                HStack {
                                    Text("\(ref.localizedSurah(lang: lang)) : \(ref.ayahRange ?? "")")
                                        .font(.caption.weight(.bold))
                                        .foregroundColor(Color(hex: "2D5A43"))
                                        .padding(.horizontal, 8)
                                        .padding(.vertical, 4)
                                        .background(Color(hex: "2D5A43").opacity(0.1))
                                        .cornerRadius(6)
                                    Spacer()
                                }
                                
                                if let ar = ref.verseTextAr {
                                    Text(ar)
                                        .font(.custom("Amiri-Bold", size: 20))
                                        .multilineTextAlignment(.trailing)
                                        .foregroundColor(.primary)
                                        .lineSpacing(8)
                                        .frame(maxWidth: .infinity, alignment: .trailing)
                                }
                                
                                Text(ref.localizedTranslation(lang: lang))
                                    .font(.subheadline)
                                    .foregroundColor(.secondary)
                                    .frame(maxWidth: .infinity, alignment: .leading)
                            }
                            .padding(14)
                            .background(Color(UIColor.secondarySystemGroupedBackground))
                            .cornerRadius(12)
                        }
                    }
                }
                .padding(20)
            }
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button(languageManager.localize("btn_close")) {
                        selectedTopic = nil
                    }
                }
            }
        }
    }
    
    // MARK: - Data Loader
    private func loadEncyclopediaData() {
        if let topicsURL = Bundle.main.url(forResource: "topics", withExtension: "json") ??
            Bundle.main.url(forResource: "topics", withExtension: "json", subdirectory: "encyclopedia") {
            if let data = try? Data(contentsOf: topicsURL),
               let decoded = try? JSONDecoder().decode([EncyclopediaTopic].self, from: data) {
                self.topics = decoded
            }
        }
        
        if let glossaryURL = Bundle.main.url(forResource: "encyclopedia_glossary", withExtension: "json") ??
            Bundle.main.url(forResource: "encyclopedia_glossary", withExtension: "json", subdirectory: "encyclopedia") {
            if let data = try? Data(contentsOf: glossaryURL),
               let decoded = try? JSONDecoder().decode([GlossaryTerm].self, from: data) {
                self.glossary = decoded
            }
        }
    }
}
