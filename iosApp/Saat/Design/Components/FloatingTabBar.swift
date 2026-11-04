import SwiftUI

struct FloatingTabBar: View {
    @Binding var selectedTab: RootTabView.Tab

    private let tabs: [(RootTabView.Tab, String, String, String)] = [
        (.today, "ic_home_off", "ic_home_on", "Beranda"),
        (.journey, "ic_quran_off", "ic_quran_on", "Al-Qur'an"),
        (.tools, "ic_spritual_off", "ic_spritual_on", "Ibadah"),
        (.account, "ic_setting_off", "ic_setting_on", "Lainnya")
    ]

    var body: some View {
        HStack(spacing: 0) {
            ForEach(tabs, id: \.0) { tab, unselectedIcon, selectedIcon, label in
                let isSelected = selectedTab == tab
                Button(action: {
                    UIImpactFeedbackGenerator(style: .light).impactOccurred()
                    withAnimation(.spring(response: 0.35, dampingFraction: 0.76)) {
                        selectedTab = tab
                    }
                }) {
                    VStack(spacing: 2) {
                        Image(isSelected ? selectedIcon : unselectedIcon)
                            .renderingMode(isSelected ? .template : .original)
                            .resizable()
                            .scaledToFit()
                            .frame(width: 22, height: 22)
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
