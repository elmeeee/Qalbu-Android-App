import SwiftUI

struct FloatingTabBar: View {
    @Binding var selectedTab: RootTabView.Tab
    let avatarUrl: URL?

    private let tabs: [(RootTabView.Tab, String, String, String)] = [
        (.today, "sun.max", "sun.max.fill", "Utama"),
        (.journey, "book", "book.fill", "Al-Qur'an"),
        (.tools, "square.grid.2x2", "square.grid.2x2.fill", "Ibadah"),
        (.account, "person.circle", "person.circle.fill", "Akun")
    ]

    var body: some View {
        HStack(spacing: 0) {
            ForEach(tabs, id: \.0) { tab, icon, selectedIcon, label in
                let isSelected = selectedTab == tab
                Button(action: {
                    UIImpactFeedbackGenerator(style: .light).impactOccurred()
                    withAnimation(.spring(response: 0.35, dampingFraction: 0.76)) {
                        selectedTab = tab
                    }
                }) {
                    VStack(spacing: 2) {
                        Image(systemName: isSelected ? selectedIcon : icon)
                            .font(.system(size: 21))
                            .foregroundColor(isSelected ? SaatTokens.Colors.deepEmerald : SaatTokens.Colors.slate500)

                        Text(label)
                            .font(.system(size: 11, weight: isSelected ? .bold : .medium))
                            .foregroundColor(isSelected ? SaatTokens.Colors.deepEmerald : SaatTokens.Colors.slate500)
                            .lineLimit(1)
                    }
                    .frame(maxWidth: .infinity)
                    .frame(height: 52)
                    .background(
                        Group {
                            if isSelected {
                                RoundedRectangle(cornerRadius: 26, style: .continuous)
                                    .fill(SaatTokens.Colors.deepEmerald.opacity(0.10))
                                    .overlay(
                                        RoundedRectangle(cornerRadius: 26, style: .continuous)
                                            .stroke(SaatTokens.Colors.deepEmerald.opacity(0.20), lineWidth: 1)
                                    )
                            }
                        }
                    )
                }
                .buttonStyle(.plain)
            }
        }
        .padding(6)
        .background(
            Capsule()
                .fill(Color.white)
        )
        .overlay(
            Capsule()
                .stroke(Color(hex: 0xFFEC_E7DE), lineWidth: 1)
        )
        .shadow(color: Color.black.opacity(0.08), radius: 16, x: 0, y: 6)
        .padding(.horizontal, 24)
        .padding(.bottom, 10)
    }
}
