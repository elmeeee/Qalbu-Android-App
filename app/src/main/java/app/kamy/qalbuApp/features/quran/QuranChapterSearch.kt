package app.kamy.qalbuApp.features.quran

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.kamy.qalbuApp.R
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.kamy.qalbuApp.design.theme.AlKhatibColors
import app.kamy.qalbuApp.design.theme.AlKhatibSpacing
import app.kamy.qalbuApp.domain.model.QuranChapter
import java.text.Normalizer

fun String.normalizedSearchQuery(): String = trim()

fun List<QuranChapter>.filteredBySearch(query: String): List<QuranChapter> =
    searchChapters(query)

fun List<QuranChapter>.searchChapters(query: String): List<QuranChapter> {
    val q = query.normalizedSearchQuery()
    if (q.isEmpty()) return this

    val normalized = normalizeLatin(q)
    val revelationFilter = revelationFilterFor(normalized)

    val results = mapNotNull { chapter ->
        scoreChapter(chapter, normalized, revelationFilter)?.let { score ->
            chapter to score
        }
    }

    if (results.isEmpty()) return emptyList()

    return results
        .sortedWith(
            compareByDescending<Pair<QuranChapter, Int>> { it.second }
                .thenBy { it.first.id }
        )
        .map { it.first }
}

private fun normalizeLatin(text: String): String =
    Normalizer.normalize(text, Normalizer.Form.NFD)
        .replace(Regex("\\p{Mn}+"), "")
        .lowercase()
        .replace(Regex("[''`´]"), "")
        .replace(Regex("[\\-–—]"), " ")
        .replace(Regex("\\s+"), " ")
        .trim()

private fun stripArticle(text: String): String =
    text.removePrefix("al ").removePrefix("ar ").removePrefix("as ").removePrefix("at ").trim()

private val REVELATION_FILTERS = mapOf(
    "makkah" to true,
    "mecca" to true,
    "mekah" to true,
    "mekkah" to true,
    "madinah" to false,
    "madina" to false,
    "medina" to false
)

private fun revelationFilterFor(normalizedQuery: String): Boolean? =
    REVELATION_FILTERS[normalizedQuery]

private val CHAPTER_ALIASES = mapOf(
    "fatihah" to 1,
    "fatiha" to 1,
    "alfatihah" to 1,
    "al fatihah" to 1,
    "pembukaan" to 1,
    "opening" to 1,
    "baqarah" to 2,
    "baqara" to 2,
    "bakara" to 2,
    "sapi" to 2,
    "cow" to 2,
    "ali imran" to 3,
    "imran" to 3,
    "nisa" to 4,
    "women" to 4,
    "maidah" to 5,
    "ma'idah" to 5,
    "anam" to 6,
    "a'raf" to 7,
    "araf" to 7,
    "anfal" to 8,
    "spoil" to 8,
    "taubah" to 9,
    "tawbah" to 9,
    "repentance" to 9,
    "yunus" to 10,
    "jonah" to 10,
    "hud" to 11,
    "yusuf" to 12,
    "joseph" to 12,
    "ra'd" to 13,
    "rad" to 13,
    "ibrahim" to 14,
    "abraham" to 14,
    "hijr" to 15,
    "nahl" to 16,
    "bee" to 16,
    "isra" to 17,
    "isra'" to 17,
    "kahf" to 18,
    "kahfi" to 18,
    "alkahf" to 18,
    "al kahf" to 18,
    "gua" to 18,
    "cave" to 18,
    "maryam" to 19,
    "mary" to 19,
    "ta ha" to 20,
    "taha" to 20,
    "anbiya" to 21,
    "prophets" to 21,
    "hajj" to 22,
    "haj" to 22,
    "muminun" to 23,
    "believers" to 23,
    "nur" to 24,
    "light" to 24,
    "furqan" to 25,
    "criterion" to 25,
    "shu'ara" to 26,
    "shuara" to 26,
    "poets" to 26,
    "naml" to 27,
    "ant" to 27,
    "qasas" to 28,
    "stories" to 28,
    "ankabut" to 29,
    "spider" to 29,
    "rum" to 30,
    "rome" to 30,
    "luqman" to 31,
    "sajdah" to 32,
    "prostration" to 32,
    "ahzab" to 33,
    "confederates" to 33,
    "saba" to 34,
    "sheba" to 34,
    "fatir" to 35,
    "originator" to 35,
    "yasin" to 36,
    "yaseen" to 36,
    "ya sin" to 36,
    "ya'sin" to 36,
    "saffat" to 37,
    "sad" to 38,
    "zumar" to 39,
    "crowds" to 39,
    "ghafir" to 40,
    "mumin" to 40,
    "fussilat" to 41,
    "shura" to 42,
    "consultation" to 42,
    "zukhruf" to 43,
    "ornaments" to 43,
    "dukhan" to 44,
    "smoke" to 44,
    "jathiyah" to 45,
    "crouching" to 45,
    "ahqaf" to 46,
    "muhammad" to 47,
    "fath" to 48,
    "victory" to 48,
    "hujurat" to 49,
    "rooms" to 49,
    "qaf" to 50,
    "dhariyat" to 51,
    "tur" to 52,
    "mount" to 52,
    "najm" to 53,
    "star" to 53,
    "qamar" to 54,
    "moon" to 54,
    "rahman" to 55,
    "ar rahman" to 55,
    "merciful" to 55,
    "waqiah" to 56,
    "waqi'ah" to 56,
    "waqi ah" to 56,
    "event" to 56,
    "hadid" to 57,
    "iron" to 57,
    "mujadilah" to 58,
    "hashr" to 59,
    "exile" to 59,
    "mumtahanah" to 60,
    "saff" to 61,
    "jumuah" to 62,
    "jumua" to 62,
    "friday" to 62,
    "munafiqun" to 63,
    "taghabun" to 64,
    "talaq" to 65,
    "divorce" to 65,
    "tahrim" to 66,
    "mulk" to 67,
    "dominion" to 67,
    "pen" to 68,
    "qalam" to 68,
    "haqqah" to 69,
    "reality" to 69,
    "ma'arij" to 70,
    "marij" to 70,
    "nuh" to 71,
    "noah" to 71,
    "jinn" to 72,
    "muzzammil" to 73,
    "muddaththir" to 74,
    "qiyamah" to 75,
    "resurrection" to 75,
    "insan" to 76,
    "man" to 76,
    "mursalat" to 77,
    "naba" to 78,
    "tidings" to 78,
    "naziat" to 79,
    "abasa" to 80,
    "takwir" to 81,
    "infitar" to 82,
    "mutaffifin" to 83,
    "inshiqaq" to 84,
    "buruj" to 85,
    "tarikh" to 86,
    "a'la" to 87,
    "ala" to 87,
    "most high" to 87,
    "ghashiyah" to 88,
    "fajr" to 89,
    "dawn" to 89,
    "balad" to 90,
    "city" to 90,
    "shams" to 91,
    "sun" to 91,
    "layl" to 92,
    "night" to 92,
    "duha" to 93,
    "morning" to 93,
    "sharh" to 94,
    "inshirah" to 94,
    "tin" to 95,
    "fig" to 95,
    "alaq" to 96,
    "clot" to 96,
    "qadr" to 97,
    "power" to 97,
    "bayyinah" to 98,
    "zalzalah" to 99,
    "earthquake" to 99,
    "adiyat" to 100,
    "qariah" to 101,
    "calamity" to 101,
    "takathur" to 102,
    "rivalry" to 102,
    "asr" to 103,
    "time" to 103,
    "humazah" to 104,
    "fil" to 105,
    "elephant" to 105,
    "quraysh" to 106,
    "ma'un" to 107,
    "kawthar" to 108,
    "abundance" to 108,
    "kafirun" to 109,
    "disbelievers" to 109,
    "nasr" to 110,
    "help" to 110,
    "lahab" to 111,
    "masad" to 111,
    "ikhlas" to 112,
    "ikhlash" to 112,
    "tawhid" to 112,
    "sincerity" to 112,
    "falaq" to 113,
    "daybreak" to 113,
    "nas" to 114,
    "an nas" to 114,
    "mankind" to 114,
    "people" to 114
)

private fun QuranChapter.searchFields(): List<String> {
    val latin = listOfNotNull(
        id.toString(),
        displayComplexName,
        nameSimple,
        displayTranslatedName,
        revelationLabel,
        revelationPlace
    ).map { normalizeLatin(it) }

    val stripped = latin.map { stripArticle(it) }
    return (latin + stripped).filter { it.isNotEmpty() }.distinct()
}

private fun scoreChapter(
    chapter: QuranChapter,
    normalizedQuery: String,
    revelationFilter: Boolean?
): Int? {
    if (revelationFilter != null) {
        if (chapter.isMeccan != revelationFilter) return null
        return 320
    }

    val compactQuery = normalizedQuery.replace(" ", "")
    val digitsOnly = compactQuery.all { it.isDigit() }

    if (digitsOnly && compactQuery.isNotEmpty()) {
        compactQuery.toIntOrNull()?.let { num ->
            if (num == chapter.id) return 1000
        }
        if (chapter.id.toString().startsWith(compactQuery)) {
            return 800 - (chapter.id.toString().length - compactQuery.length) * 10
        }
        return null
    }

    CHAPTER_ALIASES[normalizedQuery]?.let { aliasId ->
        if (aliasId == chapter.id) return 980
    }

    val queryTokens = normalizedQuery.split(" ").filter { it.isNotEmpty() }
    if (queryTokens.isEmpty()) return null

    val fields = chapter.searchFields()
    val allTokensMatch = queryTokens.all { token ->
        fields.any { field -> field.contains(token) || field.startsWith(token) }
    }
    if (!allTokensMatch) return null

    val primary = normalizeLatin(chapter.displayComplexName)
    val simple = normalizeLatin(chapter.nameSimple.orEmpty())
    val translated = normalizeLatin(chapter.displayTranslatedName)

    var score = when {
        primary == normalizedQuery || simple == normalizedQuery -> 900
        primary.startsWith(normalizedQuery) || simple.startsWith(normalizedQuery) -> 850
        translated.startsWith(normalizedQuery) -> 820
        primary.contains(normalizedQuery) || simple.contains(normalizedQuery) -> 700
        translated.contains(normalizedQuery) -> 650
        queryTokens.size > 1 -> 500 + queryTokens.size * 20
        else -> 400
    }

    chapter.nameArabic?.takeIf { it.isNotBlank() }?.let { arabic ->
        if (arabic.contains(normalizedQuery)) {
            score = maxOf(score, 750)
        }
    }

    return score
}

@Composable
fun QuranChapterSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    onFocusChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit = {},
    enabled: Boolean = true,
    focusRequester: FocusRequester = remember { FocusRequester() },
    placeholder: String? = null,
) {
    val resolvedPlaceholder = placeholder ?: stringResource(R.string.search_surah_placeholder)
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val activeQuery = query.normalizedSearchQuery()

    val elevation by animateFloatAsState(
        targetValue = if (isFocused) 6f else 1f,
        animationSpec = tween(200),
        label = "searchElevation"
    )
    val borderColor by animateColorAsState(
        targetValue = when {
            !enabled -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
            isFocused -> MaterialTheme.colorScheme.tertiary
            else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        },
        animationSpec = tween(200),
        label = "searchBorder"
    )
    val fieldShape = RoundedCornerShape(16.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer { shadowElevation = elevation }
            .shadow(
                elevation = if (isFocused) 8.dp else 2.dp,
                shape = fieldShape,
                ambientColor = AlKhatibColors.DeepEmerald.copy(alpha = 0.12f),
                spotColor = AlKhatibColors.Teal.copy(alpha = 0.18f)
            )
            .clip(fieldShape)
            .background(
                if (isFocused) {
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.12f)
                        )
                    )
                } else {
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.surface,
                            AlKhatibColors.SageMist.copy(alpha = 0.65f)
                        )
                    )
                }
            )
            .border(
                width = if (isFocused) 1.5.dp else 1.dp,
                color = borderColor,
                shape = fieldShape
            )
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(AlKhatibColors.DeepEmerald, AlKhatibColors.TealDark)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(Modifier.width(10.dp))
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                enabled = enabled,
                singleLine = true,
                textStyle = TextStyle(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                    fontWeight = FontWeight.Medium
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                interactionSource = interactionSource,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = { onDismiss() }
                ),
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester)
                    .onFocusChanged { focusState ->
                        onFocusChange(focusState.isFocused)
                    },
                decorationBox = { inner ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (activeQuery.isEmpty()) {
                            Text(
                                text = resolvedPlaceholder,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                                maxLines = 1
                            )
                        }
                        inner()
                    }
                }
            )
            if (activeQuery.isNotEmpty() && enabled) {
                IconButton(
                    onClick = onClear,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(R.string.clear_search_a11y),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun QuranSearchSuggestionChips(
    onSuggestionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val suggestions = remember {
        listOf(
            "Yasin",
            "Al-Fatihah",
            "Al-Kahf",
            "Al-Mulk",
            "Makkah",
            "Madinah",
            "36",
            "67"
        )
    }
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(suggestions) { label ->
            Surface(
                onClick = { onSuggestionClick(label) },
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                tonalElevation = 0.dp
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
fun QuranSearchEmptyState(
    query: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AlKhatibSpacing.screenHorizontal, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.MenuBook,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(30.dp)
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.no_matches),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.search_empty_message, query.normalizedSearchQuery()),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
    }
}
