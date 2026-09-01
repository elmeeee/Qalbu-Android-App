package app.kamy.saatApp.features.tools

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import coil.compose.AsyncImage
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kamy.saatApp.R
import app.kamy.saatApp.ui.layout.floatingNavBottomPadding
import app.kamy.saatApp.ui.layout.tabContentStatusBarInset

private data class SpiritualToolItem(
    @DrawableRes val iconRes: Int,
    @StringRes val titleRes: Int,
    @StringRes val descRes: Int,
    val route: String
)

private val SpiritualCardColor = Color(0xFFF8F4E9)
private val SpiritualTextColor = Color(0xFF124C31)

@Composable
fun SpiritualToolsScreen(
    onOpenTool: (String) -> Unit
) {
    val tools = remember {
        listOf(
            SpiritualToolItem(
                R.drawable.ic_qibla_3d,
                R.string.tool_qibla_title,
                R.string.tool_qibla_desc,
                "qibla"
            ),
            SpiritualToolItem(
                R.drawable.ic_radio_3d,
                R.string.tool_radio_title,
                R.string.tool_radio_desc,
                "radio"
            ),
            SpiritualToolItem(
                R.drawable.ic_doazikir_3d,
                R.string.tool_dua_dhikr_title,
                R.string.tool_dua_dhikr_desc,
                "doa-zikir"
            ),
            SpiritualToolItem(
                R.drawable.ic_asmaulhusna_3d,
                R.string.tool_asmaul_husna_title,
                R.string.tool_asmaul_husna_desc,
                "asmaul-husna"
            ),
            SpiritualToolItem(
                R.drawable.ic_faraidh_3d,
                R.string.tool_faraidh_title,
                R.string.tool_faraidh_desc,
                "faraidh"
            ),
            SpiritualToolItem(
                R.drawable.ic_zakat_3d,
                R.string.tool_zakah_title,
                R.string.tool_zakah_desc,
                "zakat"
            ),
            SpiritualToolItem(
                R.drawable.ic_tasbih_3d,
                R.string.tool_tasbih_title,
                R.string.tool_tasbih_desc,
                "dhikr"
            ),
            SpiritualToolItem(
                R.drawable.ic_manzil_3d,
                R.string.tool_manzil_title,
                R.string.tool_manzil_desc,
                "manzil"
            ),
            SpiritualToolItem(
                R.drawable.ic_hajj_umrah_3d,
                R.string.tool_hajj_umrah_title,
                R.string.tool_hajj_umrah_desc,
                "hajj-umrah"
            ),
            SpiritualToolItem(
                R.drawable.ic_jamak_3d,
                R.string.tool_jamak_guide_title,
                R.string.tool_jamak_guide_desc,
                "jamak-qashar"
            ),
            SpiritualToolItem(
                R.drawable.ic_sunnah_3d,
                R.string.tool_sunnah_practices_title,
                R.string.tool_sunnah_practices_desc,
                "sunnah-prayer"
            ),
            SpiritualToolItem(
                R.drawable.ic_jenazah_3d,
                R.string.tool_janazah_guide_title,
                R.string.tool_janazah_guide_desc,
                "jenazah"
            ),
            SpiritualToolItem(
                R.drawable.ic_encyclopedia_3d,
                R.string.tool_encyclopedia_title,
                R.string.tool_encyclopedia_desc,
                "encyclopedia"
            ),
            SpiritualToolItem(
                R.drawable.ic_fidyah_3d,
                R.string.tool_fidyah_tracker_title,
                R.string.tool_fidyah_tracker_desc,
                "fidyah"
            )
        )
    }

    val gridState = rememberLazyGridState()


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAF7F2))
    ) {
        // Full width Parallax Header Background Image (Edge to Edge, 0.45x Parallax Speed)
        AsyncImage(
            model = R.drawable.bg_worship_header,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp)
                .graphicsLayer {
                    val offset = if (gridState.firstVisibleItemIndex == 0) {
                        gridState.firstVisibleItemScrollOffset.toFloat()
                    } else {
                        1000f
                    }
                    translationY = -offset * 0.45f
                }
        )

        // Scrollable Grid Content
        LazyVerticalGrid(
            state = gridState,
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 175.dp,
                bottom = floatingNavBottomPadding() + 24.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(tools, key = { it.route }) { tool ->
                SpiritualToolGridCard(
                    tool = tool,
                    onClick = { onOpenTool(tool.route) }
                )
            }
        }

        // Pinned Header Bar with Parallax Backdrop Glass Effect & Crisp Text Layer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
        ) {
            // 1. Parallax Glass Backdrop Layer (Translates with subtle parallax motion & fades on scroll)
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer {
                        val offset = if (gridState.firstVisibleItemIndex == 0) {
                            gridState.firstVisibleItemScrollOffset.toFloat()
                        } else {
                            1000f
                        }
                        val progress = (offset / 120f).coerceIn(0f, 1f)
                        translationY = (-offset * 0.15f).coerceIn(-40f, 0f)
                        alpha = progress
                    }
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFFAF7F2).copy(alpha = 0.95f),
                                Color(0xFFFAF7F2).copy(alpha = 0.85f)
                            )
                        )
                    )
                    .drawWithContent {
                        drawContent()
                        // Glass bottom hairline border
                        drawLine(
                            color = Color(0xFF124C31).copy(alpha = 0.08f),
                            start = Offset(0f, size.height),
                            end = Offset(size.width, size.height),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
            )

            // 2. Crisp, Sharp Header Text Layer (Unblurred)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .tabContentStatusBarInset()
                    .padding(top = 16.dp, bottom = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.worship_header_title),
                    style = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SpiritualTextColor
                    ),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.worship_header_subtitle),
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Light,
                        color = SpiritualTextColor
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun SpiritualToolGridCard(
    tool: SpiritualToolItem,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val cardShape = RoundedCornerShape(16.dp)

    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(156f / 169f),
        shape = cardShape,
        color = SpiritualCardColor,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, Color(0xFFE4DDD0))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AsyncImage(
                model = tool.iconRes,
                contentDescription = null,
                modifier = Modifier
                    .size(width = 88.dp, height = 94.dp)
                    .padding(bottom = 4.dp),
                contentScale = ContentScale.Fit
            )

            Text(
                text = stringResource(tool.titleRes),
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SpiritualTextColor,
                    textAlign = TextAlign.Center
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = stringResource(tool.descRes),
                style = TextStyle(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Light,
                    color = SpiritualTextColor,
                    textAlign = TextAlign.Center,
                    lineHeight = 13.sp
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}