package app.kamy.qalbuApp.ui.common

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import app.kamy.qalbuApp.design.theme.AlKhatibColors

@Composable
fun TransliterationView(
    text: String,
    useHtml: Boolean,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Center
) {
    if (useHtml && text.looksLikeHtml()) {
        ReaderHtmlView(
            htmlBody = text,
            wrapHtml = ::wrapTransliterationHtml,
            modifier = modifier
        )
    } else {
        Text(
            text = text.replace("\r\n", "\n"),
            style = MaterialTheme.typography.bodyMedium.copy(
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.3f
            ),
            color = AlKhatibColors.Slate500,
            textAlign = textAlign,
            modifier = modifier
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
        }
        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
            font-size: 15px;
            line-height: 1.55;
            text-align: center;
            direction: ltr;
            padding: 2px 0 8px;
            word-wrap: break-word;
        }
        u { text-decoration: underline; }
        b, strong { color: #334155; font-weight: 600; }
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
