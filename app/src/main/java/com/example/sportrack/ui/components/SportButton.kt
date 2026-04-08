package com.example.sportrack.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.sportrack.ui.theme.SportGreen

/**
 * Стандартизована кнопка для інтерфейсу Sportrack.
 * Має вбудований ефект об'єму (тіні) та підтримує стан активності (увімкнена/вимкнена).
 *
 * @param text Текст, який буде відображатись на кнопці (автоматично переводиться у верхній регістр).
 * @param onClick Дія (колбек), яка виконується при натисканні на кнопку.
 * @param color Основний колір кнопки (за замовчуванням [SportGreen]).
 * @param enabled Визначає, чи активна кнопка (якщо false - кнопка стає сірою і не клікабельною).
 * @param modifier Модифікатор для налаштування розміщення та відступів компонента.
 */
@Composable
fun SportButton(
    text: String,
    onClick: () -> Unit,
    color: Color = SportGreen,
    enabled: Boolean = true,
    modifier: Modifier
) {
    val mainColor = if (enabled) color else Color.LightGray
    val shadowColor = if (enabled) color.copy(alpha = 0.7f).compositeOver(Color.Black) else Color.Gray

    Box(
        modifier = modifier
            .height(56.dp)
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier.fillMaxSize().background(shadowColor, RoundedCornerShape(16.dp))
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 4.dp)
                .background(mainColor, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text.uppercase(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}