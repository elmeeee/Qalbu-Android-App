import SwiftUI

struct FloatingTabBar: View {
    @Binding var selectedTab: RootTabView.Tab
    let avatarUrl: URL?

    var body: some View {
        HStack(spacing: 0) {
            tabItem(tab: .today, icon: "sun.max", selectedIcon: "sun.max.fill", title: "Today")
            Spacer()
            tabItem(tab: .journey, icon: "book", selectedIcon: "book.fill", title: "Quran")
            Spacer()
            tabItem(tab: .tools, icon: "square.grid.2x2", selectedIcon: "square.grid.2x2.fill", title: "Tools")
            Spacer()
            tabItem(tab: .reflect, icon: "pencil.line", selectedIcon: "pencil", title: "Reflect")
            Spacer()
            accountTabItem()
        }
        .padding(.horizontal, 24)
        .padding(.vertical, 12)
        .frame(height: AndroidTokens.Metrics.floatingNavBarHeight)
        .background {
            AndroidTokens.Shapes.navigationBarShape
                .fill(AndroidTokens.Colors.pureWhite.opacity(0.94))
                .overlay(
                    AndroidTokens.Shapes.navigationBarShape
                        .strokeBorder(
                            LinearGradient(
                                colors: [
                                    Color.white.opacity(0.9),
                                    AndroidTokens.Colors.teal.opacity(0.12)
                                ],
                                startPoint: .top,
                                endPoint: .bottom
                            ),
                            lineWidth: 0.5
                        )
                )
                .shadow(color: AndroidTokens.Colors.deepEmerald.opacity(0.12), radius: 16, x: 0, y: 8)
        }
        .padding(.horizontal, 18)
        .padding(.bottom, AndroidTokens.Metrics.floatingNavBarOuterVerticalPadding)
    }

    @ViewBuilder
    private func tabItem(tab: RootTabView.Tab, icon: String, selectedIcon: String, title: String) -> some View {
        let isSelected = selectedTab == tab
        
        Button(action: {
            withAnimation(.spring(response: 0.3, dampingFraction: 0.7)) {
                selectedTab = tab
            }
        }) {
            VStack(spacing: 4) {
                Image(systemName: isSelected ? selectedIcon : icon)
                    .font(.system(size: 21))
                    .foregroundColor(isSelected ? AndroidTokens.Colors.deepEmerald : AndroidTokens.Colors.slate500)
                
                if isSelected {
                    Text(title)
                        .font(.system(size: 10, weight: .semibold))
                        .foregroundColor(AndroidTokens.Colors.deepEmerald)
                }
            }
            .padding(.horizontal, isSelected ? 10 : 8)
            .padding(.vertical, 6)
            .background(
                Capsule()
                    .fill(isSelected ? AndroidTokens.Colors.teal.opacity(0.12) : Color.clear)
            )
        }
        .buttonStyle(.plain)
    }

    @ViewBuilder
    private func accountTabItem() -> some View {
        let isSelected = selectedTab == .account
        
        Button(action: {
            withAnimation(.spring(response: 0.3, dampingFraction: 0.7)) {
                selectedTab = .account
            }
        }) {
            VStack(spacing: 4) {
                if let url = avatarUrl {
                    AsyncImage(url: url) { phase in
                        if let image = phase.image {
                            image
                                .resizable()
                                .scaledToFill()
                        } else {
                            Image(systemName: "person.crop.circle")
                                .resizable()
                        }
                    }
                    .frame(width: 21, height: 21)
                    .clipShape(Circle())
                    .overlay(
                        Circle()
                            .strokeBorder(isSelected ? AndroidTokens.Colors.deepEmerald : Color.clear, lineWidth: 1)
                    )
                } else {
                    Image(systemName: isSelected ? "person.crop.circle.fill" : "person.crop.circle")
                        .font(.system(size: 21))
                        .foregroundColor(isSelected ? AndroidTokens.Colors.deepEmerald : AndroidTokens.Colors.slate500)
                }
                
                if isSelected {
                    Text("Profile")
                        .font(.system(size: 10, weight: .semibold))
                        .foregroundColor(AndroidTokens.Colors.deepEmerald)
                }
            }
            .padding(.horizontal, isSelected ? 10 : 8)
            .padding(.vertical, 6)
            .background(
                Capsule()
                    .fill(isSelected ? AndroidTokens.Colors.teal.opacity(0.12) : Color.clear)
            )
        }
        .buttonStyle(.plain)
    }
}
