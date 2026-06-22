package app.kamy.saatApp.ui.common

import android.graphics.Typeface
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.text.HtmlCompat
import app.kamy.saatApp.design.theme.AlKhatibColors

/**
 * Renders transliteration text (Latin/Roman) in a native Compose Text.
 *
 * Previously used ReaderHtmlView (WebView) when [useHtml] was true, which caused
 * the text to be clipped inside LazyColumn + animateContentSize because the WebView
 * height is measured asynchronously via JS and never propagates correctly.
 *
 * Now always renders as native Compose Text, converting HTML spans (e.g. <u> for
 * diacritics) to AnnotatedString so height is always known at first composition.
 */
@Composable
fun TransliterationView(
    text: String,
    useHtml: Boolean,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Center
) {
    val normalized = text.replace("\r\n", "\n").trim()
    if (normalized.isEmpty()) return

    val contentModifier = modifier.fillMaxWidth()

    // Always render as native Compose Text regardless of useHtml.
    // HtmlCompat.fromHtml() handles <u>, <b>, <i> tags natively without WebView.
    val annotated: AnnotatedString = remember(normalized) {
        if (normalized.looksLikeHtml()) {
            val spanned = HtmlCompat.fromHtml(normalized, HtmlCompat.FROM_HTML_MODE_COMPACT)
            buildAnnotatedString {
                append(spanned.toString())
                spanned.getSpans(0, spanned.length, UnderlineSpan::class.java).forEach { span ->
                    val start = spanned.getSpanStart(span)
                    val end = spanned.getSpanEnd(span)
                    addStyle(SpanStyle(textDecoration = TextDecoration.Underline), start, end)
                }
                spanned.getSpans(0, spanned.length, StyleSpan::class.java).forEach { span ->
                    val start = spanned.getSpanStart(span)
                    val end = spanned.getSpanEnd(span)
                    when (span.style) {
                        Typeface.BOLD -> addStyle(SpanStyle(fontStyle = FontStyle.Normal), start, end)
                        Typeface.ITALIC -> addStyle(SpanStyle(fontStyle = FontStyle.Italic), start, end)
                    }
                }
            }
        } else {
            AnnotatedString(normalized.decodeHtmlEntities())
        }
    }

    Text(
        text = annotated,
        style = MaterialTheme.typography.bodyMedium.copy(
            fontStyle = FontStyle.Italic,
            lineHeight = 24.sp
        ),
        color = AlKhatibColors.Slate500,
        textAlign = textAlign,
        softWrap = true,
        modifier = contentModifier.padding(vertical = 2.dp)
    )
}

fun wrapTransliterationHtml(body: String): String {
    val css = """
        html, body {
            margin: 0;
            padding: 0;
            background: transparent;
            color: #64748B;
            overflow: visible !important;
            height: auto !important;
            min-height: 0 !important;
        }
        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
            font-size: 15px;
            line-height: 1.65;
            text-align: center;
            direction: ltr;
            padding: 2px 4px 12px;
            word-wrap: break-word;
            overflow-wrap: break-word;
            -webkit-text-size-adjust: 100%;
        }
        u { text-decoration: underline; }
        b, strong { color: #334155; font-weight: 600; }
        p { margin: 0 0 0.5em; }
    """.trimIndent()
    return """
        <!DOCTYPE html>
        <html lang="en">
        <head>
        <meta charset="utf-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <style>$css</style>
        </head>
        <body>$body</body>
        </html>
    """.trimIndent()
}
