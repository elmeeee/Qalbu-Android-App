package app.kamy.saatApp.ui.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kamy.saatApp.design.theme.AlKhatibColors

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
    if (normalized.looksLikeHtml()) {
        ReaderHtmlView(
            htmlBody = normalized,
            wrapHtml = ::wrapTransliterationHtml,
            modifier = contentModifier
        )
    } else {
        Text(
            text = normalized.decodeHtmlEntities(),
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
