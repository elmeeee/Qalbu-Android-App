import SwiftUI

enum SaatTokens {
    struct Colors {
        // Primary variables (default Emerald)
        static let deepEmerald = Color(hex: 0xFF0F_4C3A)
        static let teal = Color(hex: 0xFF14_806A)
        static let tealDark = Color(hex: 0xFF10_6352)
        static let emeraldRich = Color(hex: 0xFF16_5C4B)
        static let emeraldNight = Color(hex: 0xFF09_291F)
        static let forestDark = Color(hex: 0xFF0C_382A)
        static let forestDeeper = Color(hex: 0xFF06_2118)
        static let readerMoss = Color(hex: 0xFF0E_4030)
        static let readerForest = Color(hex: 0xFF08_261C)
        static let sageTint = Color(hex: 0xFFE8_F2ED)
        static let mintWash = Color(hex: 0xFFF2_F9F6)

        // Layout variables
        static let offWhite = Color(hex: 0xFFF9_F9F8)
        static let screenBackground = Color(hex: 0xFFF8_FAFC)
        static let pureWhite = Color(hex: 0xFFFF_FFFF)
        static let softGrey = Color(hex: 0xFFE5_E7EB)
        static let lightGrey = Color(hex: 0xFFF3_F4F6)
        static let slate500 = Color(hex: 0xFF64_748B)
        static let slate600 = Color(hex: 0xFF47_5569)
        static let slate700 = Color(hex: 0xFF33_4155)
        static let slate800 = Color(hex: 0xFF1E_293B)
        static let slate900 = Color(hex: 0xFF0F_172A)
        static let prayerCream = Color(hex: 0xFFFF_F7ED)
        static let prayerCreamWarm = Color(hex: 0xFFFE_F3C7)
        static let prayerMint = Color(hex: 0xFFF0_FDFA)
        static let sageMist = Color(hex: 0xFFF1_F5F2)
        static let panelGrey = Color(hex: 0xFFE8_EBEF)
        static let panelGreyAlt = Color(hex: 0xFFEE_F2EE)
        static let indigoAccent = Color(hex: 0xFF4F_46E5)
        static let blueLink = Color(hex: 0xFF25_63EB)

        // Constants
        static let gold = Color(hex: 0xFFB4_5309)
        static let goldBright = Color(hex: 0xFFD4_A017)
        static let goldDeep = Color(hex: 0xFFD9_7706)
        static let amberWash = Color(hex: 0xFFFF_FBEB)
        static let indigoDeep = Color(hex: 0xFF31_2E81)
        static let danger = Color(hex: 0xFFEF_4444)
    }

    struct Metrics {
        static let floatingNavBarHeight: CGFloat = 72
        static let floatingNavBarOuterVerticalPadding: CGFloat = 16
        static let floatingAudioBarHeight: CGFloat = 68
        static let floatingAudioBarBottomGap: CGFloat = 8
    }

    struct Spacing {
        static let xs: CGFloat = 4
        static let sm: CGFloat = 8
        static let md: CGFloat = 16
        static let lg: CGFloat = 24
        static let xl: CGFloat = 32
        static let screenHorizontal: CGFloat = 16
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
