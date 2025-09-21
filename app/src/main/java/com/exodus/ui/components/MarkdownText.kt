package com.exodus.ui.components

import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.material3.Text
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit

@Composable
fun MarkdownText(
    text: String,
    color: Color = MaterialTheme.colorScheme.onSurface,
    fontSize: TextUnit = MaterialTheme.typography.bodyMedium.fontSize,
    lineHeight: TextUnit = MaterialTheme.typography.bodyMedium.lineHeight
) {
    val annotatedString = buildAnnotatedString {
        var currentIndex = 0
        val boldRegex = Regex("""\*\*(.*?)\*\*""")
        val matches = boldRegex.findAll(text)
        
        for (match in matches) {
            // Add text before the bold text
            if (match.range.first > currentIndex) {
                append(text.substring(currentIndex, match.range.first))
            }
            
            // Add bold text
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                append(match.groupValues[1])
            }
            
            currentIndex = match.range.last + 1
        }
        
        // Add remaining text after the last bold text
        if (currentIndex < text.length) {
            append(text.substring(currentIndex))
        }
    }
    
    SelectionContainer {
        Text(
            text = annotatedString,
            color = color,
            fontSize = fontSize,
            lineHeight = lineHeight,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}