package app.kamy.saatApp.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kamy.saatApp.design.theme.SaatColors
import app.kamy.saatApp.ui.navigation.RootTab

@Composable
fun FloatingTabBar(
    selectedRoute: String?,
    onTabSelected: (RootTab) -> Unit,
    modifier: Modifier = Modifier,
    tabs: List<RootTab> = RootTab.mainTabs
) {
    val selectedIndex = remember(selectedRoute, tabs) {
        tabs.indexOfFirst { it.route == selectedRoute }.coerceAtLeast(0)
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 10.dp)
            .shadow(
                elevation = 16.dp,
                shape = CircleShape,
                ambientColor = Color.Black.copy(alpha = 0.10f),
                spotColor = Color.Black.copy(alpha = 0.08f)
            ),
        shape = CircleShape,
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFECE7DE)),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 6.dp, vertical = 6.dp)
        ) {
            val totalWidthDp = maxWidth
            val tabWidthDp = totalWidthDp / tabs.size

            // 1. Super Smooth Sliding Liquid Active Pill (Netflix style)
            val animatedPillOffset by animateDpAsState(
                targetValue = tabWidthDp * selectedIndex,
                animationSpec = spring(dampingRatio = 0.76f, stiffness = 380f),
                label = "pill_slide"
            )

            Box(
                modifier = Modifier
                    .offset(x = animatedPillOffset)
                    .width(tabWidthDp)
                    .height(52.dp)
                    .padding(horizontal = 2.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(SaatColors.DeepEmerald.copy(alpha = 0.10f))
                    .border(
                        1.dp,
                        SaatColors.DeepEmerald.copy(alpha = 0.20f),
                        RoundedCornerShape(26.dp)
                    )
            )

            // 2. Tab Items Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                tabs.forEach { tab ->
                    val isSelected = selectedRoute == tab.route
                    NetflixTabItem(
                        tab = tab,
                        isSelected = isSelected,
                        modifier = Modifier.width(tabWidthDp),
                        onClick = { onTabSelected(tab) }
                    )
                }
            }
        }
    }
}

@Composable
private fun NetflixTabItem(
    tab: RootTab,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.92f
            isSelected -> 1.04f
            else -> 1.0f
        },
        animationSpec = spring(stiffness = 800f),
        label = "item_scale"
    )

    val activeContentColor = SaatColors.DeepEmerald

    Column(
        modifier = modifier
            .height(52.dp)
            .scale(scale)
            .clip(RoundedCornerShape(26.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true, color = SaatColors.DeepEmerald.copy(alpha = 0.12f)),
                role = Role.Tab,
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(
                id = if (isSelected) tab.selectedIconRes else tab.unselectedIconRes
            ),
            contentDescription = stringResource(tab.labelRes),
            tint = if (isSelected) activeContentColor else Color.Unspecified,
            modifier = Modifier.size(22.dp)
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = stringResource(tab.labelRes),
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) activeContentColor else SaatColors.Slate500,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
