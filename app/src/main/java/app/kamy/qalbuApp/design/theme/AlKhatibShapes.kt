package app.kamy.qalbuApp.design.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/** Material 3 shape scale tuned for Al-Khatib cards, sheets, and navigation. */
val AlKhatibShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

/** Floating navigation bar capsule. */
val NavigationBarShape = RoundedCornerShape(percent = 50)
