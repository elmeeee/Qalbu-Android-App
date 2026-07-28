package app.kamy.saatApp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import app.kamy.saatApp.ui.navigation.RootTab

@Composable
fun FloatingTabBar(
    selectedRoute: String?,
    onTabSelected: (RootTab) -> Unit,
    modifier: Modifier = Modifier,
    tabs: List<RootTab> = RootTab.mainTabs
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .shadow(
                elevation = 10.dp,
                shape = CircleShape,
                clip = false,
                ambientColor = Color.Black.copy(alpha = 0.08f),
                spotColor = Color.Black.copy(alpha = 0.06f)
            )
            .clip(CircleShape),
        shape = CircleShape,
        color = Color(0xC8F4F7F5),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.75f)),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEach { tab ->
                val selected = selectedRoute == tab.route
                FloatingTabItem(
                    tab = tab,
                    selected = selected,
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
    onClick: () -> Unit
) {
    val activeGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF085E43),
            Color(0xFF15AA7C)
        )
    )

    val itemShape = CircleShape

    Row(
        modifier = Modifier
            .clip(itemShape)
            .then(
                if (selected) {
                    Modifier.background(brush = activeGradient, shape = itemShape)
                } else {
                    Modifier.background(color = Color.Transparent, shape = itemShape)
                }
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, radius = 28.dp),
                role = Role.Tab,
                onClick = onClick
            )
            .animateContentSize(animationSpec = spring(stiffness = 800f))
            .padding(
                horizontal = if (selected) 20.dp else 16.dp,
                vertical = 12.dp
            ),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(
                id = if (selected) tab.selectedIconRes else tab.unselectedIconRes
            ),
            contentDescription = stringResource(tab.labelRes),
            tint = Color.Unspecified,
            modifier = Modifier.size(24.dp)
        )

        AnimatedVisibility(
            visible = selected,
            enter = fadeIn(animationSpec = spring(stiffness = 800f)),
            exit = fadeOut(animationSpec = spring(stiffness = 800f))
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(tab.labelRes),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
