import SwiftUI

enum SaatTokens {
    struct Colors {
        // Primary vertical gradient: #085E43 to #15AA7C
        static let deepEmerald = Color(hex: 0xFF08_5E43)
        static let teal = Color(hex: 0xFF15_AA7C)
        static let tealDark = Color(hex: 0xFF08_5E43)
        static let emeraldRich = Color(hex: 0xFF08_5E43)
        static let emeraldNight = Color(hex: 0xFF04_291D)
        static let forestDark = Color(hex: 0xFF08_5E43)
        static let forestDeeper = Color(hex: 0xFF04_291D)
        static let readerMoss = Color(hex: 0xFF08_5E43)
        static let readerForest = Color(hex: 0xFF04_291D)
        static let sageTint = Color(hex: 0xFFE6_F3EE)
        static let mintWash = Color(hex: 0xFFF0_F8F5)

        // Primary Vertical Gradient Brush equivalent
        static let primaryGradient = LinearGradient(
            colors: [Color(hex: 0xFF08_5E43), Color(hex: 0xFF15_AA7C)],
            startPoint: .top,
            endPoint: .bottom
        )

        // Layout variables
        static let offWhite = Color(hex: 0xFFF9_F4EC)
        static let screenBackground = Color(hex: 0xFFF9_F4EC)
        static let homeBg = Color(hex: 0xFFF9_F4EC)
        static let lastReadBg = Color(hex: 0xFFFF_FFFF)
        static let journeyCardBg = Color(hex: 0xFFFA_F6F0)
        static let pureWhite = Color(hex: 0xFFFF_FFFF)
        static let softGrey = Color(hex: 0xFFE5_E7EB)
        static let lightGrey = Color(hex: 0xFFF3_F4F6)
        static let slate400 = Color(hex: 0xFF94_A3B8)
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
        static let indigoAccent = Color(hex: 0xFF08_5E43)
        static let blueLink = Color(hex: 0xFF15_AA7C)

        // Constants & Accents
        static let gold = Color(hex: 0xFFB4_5309)
        static let goldBright = Color(hex: 0xFFD4_A017)
        static let goldDeep = Color(hex: 0xFFD9_7706)
        static let amberWash = Color(hex: 0xFFFF_FBEB)
        static let indigoDeep = Color(hex: 0xFF31_2E81)
        static let homeDarkGreen = Color(hex: 0xFF17_6345)
        static let danger = Color(hex: 0xFFEF_4444)
    }

    struct Metrics {
        static let floatingNavBarHeight: CGFloat = 72
        static let floatingNavBarOuterVerticalPadding: CGFloat = 16
        static let floatingAudioBarHeight: CGFloat = 68
        static let floatingAudioBarBottomGap: CGFloat = 8
        static let cardCornerRadius: CGFloat = 20
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
        static let cardShape = RoundedRectangle(cornerRadius: 20, style: .continuous)
        static let chipShape = RoundedRectangle(cornerRadius: 12, style: .continuous)
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
