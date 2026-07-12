import SwiftUI

enum AndroidTokens {
    struct Colors {
        // Primary variables (default Emerald)
        static let deepEmerald = Color(hex: 0xFF0F4C3A)
        static let teal = Color(hex: 0xFF14806A)
        static let tealDark = Color(hex: 0xFF106352)
        static let emeraldRich = Color(hex: 0xFF165C4B)
        static let emeraldNight = Color(hex: 0xFF09291F)
        static let forestDark = Color(hex: 0xFF0C382A)
        static let forestDeeper = Color(hex: 0xFF062118)
        static let readerMoss = Color(hex: 0xFF0E4030)
        static let readerForest = Color(hex: 0xFF08261C)
        static let sageTint = Color(hex: 0xFFE8F2ED)
        static let mintWash = Color(hex: 0xFFF2F9F6)

        // Layout variables
        static let offWhite = Color(hex: 0xFFF9F9F8)
        static let screenBackground = Color(hex: 0xFFF8FAFC)
        static let pureWhite = Color(hex: 0xFFFFFFFF)
        static let softGrey = Color(hex: 0xFFE5E7EB)
        static let lightGrey = Color(hex: 0xFFF3F4F6)
        static let slate500 = Color(hex: 0xFF64748B)
        static let slate700 = Color(hex: 0xFF334155)
        static let slate800 = Color(hex: 0xFF1E293B)
        static let slate900 = Color(hex: 0xFF0F172A)
        static let prayerCream = Color(hex: 0xFFFFF7ED)
        static let prayerCreamWarm = Color(hex: 0xFFFEF3C7)
        static let prayerMint = Color(hex: 0xFFF0FDFA)
        static let sageMist = Color(hex: 0xFFF1F5F2)
        static let panelGrey = Color(hex: 0xFFE8EBEF)
        static let panelGreyAlt = Color(hex: 0xFFEEF2EE)
        static let indigoAccent = Color(hex: 0xFF4F46E5)
        static let blueLink = Color(hex: 0xFF2563EB)

        // Constants
        static let gold = Color(hex: 0xFFB45309)
        static let goldBright = Color(hex: 0xFFD4A017)
        static let goldDeep = Color(hex: 0xFFD97706)
        static let amberWash = Color(hex: 0xFFFFFBEB)
        static let indigoDeep = Color(hex: 0xFF312E81)
        static let danger = Color(hex: 0xFFEF4444)
    }

    struct Metrics {
        static let floatingNavBarHeight: CGFloat = 72
        static let floatingNavBarOuterVerticalPadding: CGFloat = 16
        static let floatingAudioBarHeight: CGFloat = 68
        static let floatingAudioBarBottomGap: CGFloat = 8
    }

    struct Shapes {
        static let navigationBarShape = RoundedRectangle(cornerRadius: 32, style: .continuous)
    }
}

extension Color {
    init(hex: UInt, alpha: Double = 1) {
        self.init(
            .sRGB,
            red: Double((hex >> 16) & 0xff) / 255,
            green: Double((hex >> 08) & 0xff) / 255,
            blue: Double((hex >> 00) & 0xff) / 255,
            opacity: alpha
        )
    }
}
