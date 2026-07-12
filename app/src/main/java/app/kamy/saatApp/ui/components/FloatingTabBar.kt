package app.kamy.saatApp.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kamy.saatApp.design.theme.SaatColors
import app.kamy.saatApp.design.theme.NavigationBarShape
import app.kamy.saatApp.ui.layout.FloatingNavBarMetrics
import app.kamy.saatApp.ui.navigation.RootTab
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.shape.CircleShape

@Composable
fun FloatingTabBar(
    selectedRoute: String?,
    onTabSelected: (RootTab) -> Unit,
    modifier: Modifier = Modifier,
    avatarUrl: String? = null,
    tabs: List<RootTab> = RootTab.mainTabs
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(
                horizontal = 18.dp,
                vertical = FloatingNavBarMetrics.outerVerticalPadding
            )
            .shadow(
                elevation = 16.dp,
                shape = NavigationBarShape,
                clip = false,
                ambientColor = SaatColors.DeepEmerald.copy(alpha = 0.12f),
                spotColor = SaatColors.DeepEmerald.copy(alpha = 0.08f)
            )
            .clip(NavigationBarShape)
            .border(
                width = 0.5.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = 0.9f),
                        SaatColors.Teal.copy(alpha = 0.12f)
                    )
                ),
                shape = NavigationBarShape
            ),
        shape = NavigationBarShape,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(FloatingNavBarMetrics.barHeight)
                .padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEach { tab ->
                val selected = selectedRoute == tab.route
                FloatingTabItem(
                    tab = tab,
                    selected = selected,
                    avatarUrl = if (tab == RootTab.Account) avatarUrl else null,
                    onClick = { onTabSelected(tab) }
                )
            }
        }
    }
}

@Composable
private fun FloatingTabItem(
    tab: RootTab,
    selected: Boolean,
    avatarUrl: String?,
    onClick: () -> Unit
) {
    val iconTint by animateColorAsState(
        targetValue = if (selected) SaatColors.DeepEmerald else SaatColors.Slate500,
        animationSpec = spring(stiffness = 500f),
        label = "tab_icon"
    )
    val labelTint by animateColorAsState(
        targetValue = if (selected) SaatColors.DeepEmerald else Color.Transparent,
        animationSpec = spring(stiffness = 500f),
        label = "tab_label"
    )
    val pillColor = if (selected) SaatColors.Teal.copy(alpha = 0.12f) else Color.Transparent

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(pillColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, radius = 28.dp),
                role = Role.Tab,
                onClick = onClick
            )
            .padding(horizontal = if (selected) 10.dp else 8.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (avatarUrl != null) {
            AsyncImage(
                model = avatarUrl,
                contentDescription = stringResource(tab.labelRes),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(21.dp)
                    .clip(CircleShape)
                    .border(
                        width = 1.dp,
                        color = if (selected) SaatColors.DeepEmerald else Color.Transparent,
                        shape = CircleShape
                    )
            )
        } else {
            Icon(
                imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                contentDescription = stringResource(tab.labelRes),
                tint = iconTint,
                modifier = Modifier.size(21.dp)
            )
        }
        if (selected) {
            Text(
                text = stringResource(tab.labelRes),
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = labelTint,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}
