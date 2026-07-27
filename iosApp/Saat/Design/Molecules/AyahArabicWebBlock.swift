//
//  AyahArabicWebBlock.swift
//  Sāat
//
//  Created by Elmee on 25/04/2026.
//  Copyright © 2026 Elmee. All rights reserved.
//

import SwiftUI

struct AyahArabicWebBlock: View {
    static let placeholderHeight: CGFloat = 72

    let payload: RandomAyahPayload
    var style: HTMLContentStyle = .verseCard
    var fontScale: Double = 1.0
    var showTajweed: Bool = true
    var measuredHeight: Binding<CGFloat>?
    var includeTranslationInAccessibility: Bool = false
    var onTajweedTap: ((TajweedType) -> Void)? = nil

    @State private var webHeight: CGFloat = placeholderHeight

    var body: some View {
        HTMLContentWebView(
            htmlFragment: htmlFragment,
            style: style,
            rendersTajweedHTML: showTajweed,
            fontScale: fontScale,
            contentHeight: $webHeight,
            onTajweedTap: showTajweed ? onTajweedTap : nil
        )
        .frame(maxWidth: .infinity, alignment: .top)
        .frame(height: webHeight)
        .accessibilityElement(children: .ignore)
        .accessibilityLabel(payload.spokenAccessibilitySummary(includeTranslation: includeTranslationInAccessibility))
        .accessibilityAddTraits(.isStaticText)
        .allowsHitTesting(showTajweed && onTajweedTap != nil)
        .animation(nil, value: webHeight)
        .id(stableId)
        .onChangeWithFallback(of: reloadKey) { _ in
            webHeight = Self.placeholderHeight
            measuredHeight?.wrappedValue = Self.placeholderHeight
        }
        .onChangeWithFallback(of: webHeight) { height in
            measuredHeight?.wrappedValue = height
        }
    }

    private var htmlFragment: String {
        if showTajweed {
            return payload.tajweedWebHTMLFragment()
        } else {
            return payload.plainArabicWebHTMLFragment()
        }
    }

    private var stableId: String {
        let base: String
        if let key = payload.verseKey {
            base = key
        } else if let id = payload.id {
            base = "id-\(id)"
        } else {
            base = "ayah"
        }
        return "\(base)-tajweed-\(showTajweed)-\(style)-\(fontScale)"
    }

    private var reloadKey: String {
        "\(payload.verseKey ?? "")-\(showTajweed)-\(fontScale)"
    }
}
