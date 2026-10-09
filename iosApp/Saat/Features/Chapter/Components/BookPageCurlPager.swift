//
//  BookPageCurlPager.swift
//  Saat
//
//  Created by Sufiandy Elmy on 09/10/2026.
//  Copyright © 2026 Elmee. All rights reserved.
//

import SwiftUI

/// A high-performance 3D Book Page Curl Pager modeled after physical Quran pages, Apple Books, and Fizzo Novel.
/// Provides realistic 3D spine rotation, traveling paper highlight sheens, curl valley shading, and cast drop shadows.
struct BookPageCurlPager<Content: View>: View {
    let pageCount: Int
    @Binding var currentPage: Int
    let onPageChanged: ((Int) -> Void)?
    @ViewBuilder let content: (Int) -> Content

    @State private var dragOffset: CGFloat = 0
    @State private var isDragging: Bool = false
    @State private var turnDirection: TurnDirection = .none

    private enum TurnDirection {
        case none
        case forward   // turning to next page (curling from right to left)
        case backward  // turning to previous page (curling from left to right)
    }

    init(
        pageCount: Int,
        currentPage: Binding<Int>,
        onPageChanged: ((Int) -> Void)? = nil,
        @ViewBuilder content: @escaping (Int) -> Content
    ) {
        self.pageCount = pageCount
        self._currentPage = currentPage
        self.onPageChanged = onPageChanged
        self.content = content
    }

    var body: some View {
        GeometryReader { geometry in
            let width = geometry.size.width
            let height = geometry.size.height

            ZStack {
                // Background paper tone
                Color(hex: 0xFFFC_FBF7)
                    .ignoresSafeArea()

                // Render layers based on turn state
                if isDragging || dragOffset != 0 {
                    let progress = min(1.0, max(0.0, abs(dragOffset) / max(1.0, width)))

                    if turnDirection == .forward && currentPage < pageCount - 1 {
                        // 1. Next Page underneath (Stationary Revealed Layer)
                        ZStack {
                            content(currentPage + 1)
                                .frame(width: width, height: height)

                            // Cast shadow from folding top page
                            LinearGradient(
                                colors: [
                                    Color.black.opacity(0.32 * Double(1.0 - progress)),
                                    Color.black.opacity(0.08 * Double(1.0 - progress)),
                                    Color.clear
                                ],
                                startPoint: .leading,
                                endPoint: .trailing
                            )
                            .frame(width: width * 0.35)
                            .frame(maxWidth: .infinity, alignment: .leading)
                        }
                        .zIndex(1)

                        // 2. Current Page turning over (3D folding layer around left spine)
                        turningPageLayer(
                            pageIndex: currentPage,
                            progress: progress,
                            width: width,
                            height: height,
                            isForward: true
                        )
                        .zIndex(2)

                    } else if turnDirection == .backward && currentPage > 0 {
                        // 1. Current Page underneath
                        content(currentPage)
                            .frame(width: width, height: height)
                            .zIndex(1)

                        // 2. Previous Page curling in from left
                        turningPageLayer(
                            pageIndex: currentPage - 1,
                            progress: 1.0 - progress,
                            width: width,
                            height: height,
                            isForward: false
                        )
                        .zIndex(2)

                    } else {
                        // Edge bounce
                        content(currentPage)
                            .frame(width: width, height: height)
                            .offset(x: dragOffset * 0.25)
                            .zIndex(1)
                    }

                } else {
                    // Resting state: Only render active page
                    content(currentPage)
                        .frame(width: width, height: height)
                        .zIndex(1)
                }
            }
            .contentShape(Rectangle())
            .gesture(
                DragGesture(minimumDistance: 12)
                    .onChanged { value in
                        isDragging = true
                        let translation = value.translation.width
                        dragOffset = translation

                        if translation < 0 {
                            turnDirection = .forward
                        } else if translation > 0 {
                            turnDirection = .backward
                        } else {
                            turnDirection = .none
                        }
                    }
                    .onEnded { value in
                        isDragging = false
                        let translation = value.translation.width
                        let velocity = value.predictedEndTranslation.width - translation
                        let threshold = width * 0.22

                        if translation < -threshold || velocity < -200 {
                            if currentPage < pageCount - 1 {
                                // Turn to next page
                                withAnimation(.spring(response: 0.36, dampingFraction: 0.88)) {
                                    dragOffset = -width
                                }
                                DispatchQueue.main.asyncAfter(deadline: .now() + 0.36) {
                                    currentPage += 1
                                    dragOffset = 0
                                    turnDirection = .none
                                    onPageChanged?(currentPage)
                                }
                                return
                            }
                        } else if translation > threshold || velocity > 200 {
                            if currentPage > 0 {
                                // Turn to previous page
                                withAnimation(.spring(response: 0.36, dampingFraction: 0.88)) {
                                    dragOffset = width
                                }
                                DispatchQueue.main.asyncAfter(deadline: .now() + 0.36) {
                                    currentPage -= 1
                                    dragOffset = 0
                                    turnDirection = .none
                                    onPageChanged?(currentPage)
                                }
                                return
                            }
                        }

                        // Snap back
                        withAnimation(.spring(response: 0.32, dampingFraction: 0.85)) {
                            dragOffset = 0
                            turnDirection = .none
                        }
                    }
            )
        }
    }

    @ViewBuilder
    private func turningPageLayer(
        pageIndex: Int,
        progress: CGFloat,
        width: CGFloat,
        height: CGFloat,
        isForward: Bool
    ) -> some View {
        let p = max(0.0, min(1.0, progress))
        let angle = Double(-180.0 * p)
        let sinP = sin(Double(p * .pi))

        ZStack {
            if p <= 0.5 {
                // Front face of the page
                content(pageIndex)
                    .frame(width: width, height: height)

                // 1. Spine Crease Shadow
                let spineShadowWidth = width * 0.16
                let spineAlpha = 0.30 * sinP
                if spineAlpha > 0.01 {
                    LinearGradient(
                        colors: [Color.black.opacity(spineAlpha), Color.clear],
                        startPoint: .leading,
                        endPoint: .trailing
                    )
                    .frame(width: spineShadowWidth, height: height)
                    .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .leading)
                }

                // 2. Traveling Dynamic Paper Curl Sheen / Highlight
                let curlCenter = width * (1.0 - p * 0.75)
                let curlHalfWidth = width * 0.14
                let curlHighlightAlpha = 0.28 * sinP
                if curlHighlightAlpha > 0.01 {
                    LinearGradient(
                        colors: [
                            Color.clear,
                            Color.white.opacity(curlHighlightAlpha),
                            Color.clear
                        ],
                        startPoint: .leading,
                        endPoint: .trailing
                    )
                    .frame(width: curlHalfWidth * 2, height: height)
                    .position(x: curlCenter, y: height / 2.0)
                }

                // 3. Valley Shadow (just behind curl highlight)
                let valleyCenter = max(0, curlCenter - curlHalfWidth * 0.75)
                let valleyAlpha = 0.18 * sinP
                if valleyAlpha > 0.01 {
                    LinearGradient(
                        colors: [
                            Color.clear,
                            Color.black.opacity(valleyAlpha),
                            Color.clear
                        ],
                        startPoint: .leading,
                        endPoint: .trailing
                    )
                    .frame(width: curlHalfWidth * 1.5, height: height)
                    .position(x: valleyCenter, y: height / 2.0)
                }

            } else {
                // Back face of the page (reverse side paper)
                ZStack {
                    Color(hex: 0xFFF7_F3EB)
                        .frame(width: width, height: height)

                    // Subtle paper fibers/translucency tint
                    LinearGradient(
                        colors: [
                            Color.black.opacity(0.12),
                            Color.black.opacity(0.04),
                            Color.clear
                        ],
                        startPoint: .trailing,
                        endPoint: .leading
                    )
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                }
                .rotation3DEffect(.degrees(180), axis: (x: 0, y: 1, z: 0))
            }
        }
        .rotation3DEffect(
            .degrees(angle),
            axis: (x: 0, y: 1, z: 0),
            anchor: .leading,
            anchorZ: 0,
            perspective: 0.35
        )
        .shadow(
            color: Color.black.opacity(0.20 * sinP),
            radius: 12,
            x: -8 * sinP,
            y: 4
        )
    }
}
