//
//  SkeletonView.swift
//  Sāat
//
//  Created by Elmee on 25/04/2026.
//  Copyright © 2026 Elmee. All rights reserved.
//

import SwiftUI

enum SkeletonTone {
    case light
    case muted
    case onDark
}

struct SkeletonBar: View {
    var width: CGFloat?
    var height: CGFloat = 12
    var cornerRadius: CGFloat = 8
    var tone: SkeletonTone = .light

    init(
        width: CGFloat? = nil,
        height: CGFloat = 12,
        cornerRadius: CGFloat = 8,
        tone: SkeletonTone = .light
    ) {
        self.width = width
        self.height = height
        self.cornerRadius = cornerRadius
        self.tone = tone
    }

    var body: some View {
        RoundedRectangle(cornerRadius: cornerRadius)
            .fill(baseFill)
            .frame(width: width, height: height)
            .frame(maxWidth: width == nil ? .infinity : nil)
            .skeletonShimmer(tone: tone)
    }

    private var baseFill: Color {
        switch tone {
        case .light:
            Color(.systemGray5)
        case .muted:
            Color.Token.softGrey.opacity(0.5)
        case .onDark:
            Color.white.opacity(0.06)
        }
    }
}

struct SkeletonCapsuleBar: View {
    var height: CGFloat = 12
    var tone: SkeletonTone = .light

    var body: some View {
        SkeletonBar(height: height, cornerRadius: 999, tone: tone)
    }
}

struct SkeletonCircleDot: View {
    var size: CGFloat = 44
    var tone: SkeletonTone = .light

    var body: some View {
        Circle()
            .fill(circleFill)
            .frame(width: size, height: size)
            .skeletonShimmer(tone: tone)
    }

    private var circleFill: Color {
        switch tone {
        case .light:
            Color(.systemGray5)
        case .muted:
            Color.Token.softGrey.opacity(0.4)
        case .onDark:
            Color.white.opacity(0.08)
        }
    }
}

private struct SkeletonShimmerModifier: ViewModifier {
    let tone: SkeletonTone
    @State private var offset: CGFloat = -200

    func body(content: Content) -> some View {
        content
            .overlay {
                GeometryReader { geo in
                    shimmerGradient
                        .frame(width: geo.size.width * 0.6)
                        .offset(x: offset)
                }
                .clipped()
            }
            .onAppear {
                offset = -200
                withAnimation(.linear(duration: 1.5).repeatForever(autoreverses: false)) {
                    offset = 400
                }
            }
    }

    private var shimmerGradient: LinearGradient {
        let highlight: Color = switch tone {
        case .light, .muted:
            Color.white.opacity(0.35)
        case .onDark:
            Color.white.opacity(0.08)
        }
        return LinearGradient(
            colors: [.clear, highlight, .clear],
            startPoint: .leading,
            endPoint: .trailing
        )
    }
}

extension View {
    func skeletonShimmer(tone: SkeletonTone = .light) -> some View {
        modifier(SkeletonShimmerModifier(tone: tone))
    }
}

struct LoadingSkeleton: View {
    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            SkeletonBar(width: 180, height: 28, cornerRadius: 8)
                .padding(.bottom, 4)

            ForEach(0..<4, id: \.self) { i in
                VStack(alignment: .leading, spacing: 10) {
                    SkeletonBar(width: 120, height: 14)
                    SkeletonBar(width: nil, height: 12)
                    SkeletonBar(width: i % 2 == 0 ? nil : 200, height: 12)
                }
                .padding(16)
                .flatCard()
            }

            Spacer(minLength: 0)
        }
        .padding(.horizontal)
        .padding(.top, 24)
        .padding(.bottom, 24)
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .topLeading)
        .background(Color.Token.offWhite)
    }
}

struct PrayerDashboardSkeleton: View {
    var body: some View {
        VStack(alignment: .leading, spacing: 20) {
            VStack(alignment: .leading, spacing: 10) {
                SkeletonBar(width: 140, height: 28, cornerRadius: 8, tone: .muted)
                SkeletonBar(width: 80, height: 14, cornerRadius: 6, tone: .muted)
                SkeletonBar(width: 180, height: 32, cornerRadius: 15, tone: .muted)
            }
            .padding(.horizontal, 24)
            .padding(.top, 28)

            Divider()
                .background(Color.Token.softGrey.opacity(0.4))

            HStack(spacing: 8) {
                ForEach(0..<6, id: \.self) { _ in
                    VStack(spacing: 8) {
                        SkeletonBar(width: 38, height: 12, cornerRadius: 4, tone: .muted)
                        SkeletonBar(width: 44, height: 44, cornerRadius: 8, tone: .muted)
                    }
                    .frame(maxWidth: .infinity)
                }
            }
            .padding(.horizontal, 16)
            .padding(.bottom, 20)
        }
    }
}
