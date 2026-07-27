import SwiftUI

struct FloatingAudioBar: View {
    let isPlaying: Bool
    let trackTitle: String
    let trackSubtitle: String
    let reciterName: String

    let onToggle: () -> Void
    let onDismiss: () -> Void
    let onOpenPlayback: (() -> Void)?

    var body: some View {
        HStack(spacing: 12) {
            Button(action: onToggle) {
                Image(systemName: isPlaying ? "pause.fill" : "play.fill")
                    .font(.system(size: 24))
                    .foregroundColor(.white)
            }
            .frame(width: 44, height: 44)

            VStack(alignment: .leading, spacing: 2) {
                Text(trackTitle.isEmpty ? "Playing" : trackTitle)
                    .font(.system(size: 14, weight: .semibold))
                    .foregroundColor(.white)
                    .lineLimit(1)

                let subtitle = buildSubtitle()
                if !subtitle.isEmpty {
                    Text(subtitle)
                        .font(.system(size: 12))
                        .foregroundColor(.white.opacity(0.85))
                        .lineLimit(1)
                }
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            .contentShape(Rectangle())
            .onTapGesture {
                onOpenPlayback?()
            }

            Button(action: onDismiss) {
                Image(systemName: "xmark")
                    .font(.system(size: 16, weight: .bold))
                    .foregroundColor(.white.opacity(0.9))
            }
            .frame(width: 44, height: 44)
        }
        .padding(.horizontal, 12)
        .padding(.vertical, 8)
        .background(SaatTokens.Colors.deepEmerald)
        .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
        .shadow(color: Color.black.opacity(0.15), radius: 6, x: 0, y: 3)
        .padding(.horizontal, 16)
        .frame(height: SaatTokens.Metrics.floatingAudioBarHeight)
    }

    private func buildSubtitle() -> String {
        var parts: [String] = []
        if !trackSubtitle.isEmpty {
            parts.append(trackSubtitle)
        }
        if !reciterName.isEmpty {
            parts.append(reciterName)
        }
        return parts.joined(separator: " · ")
    }
}
