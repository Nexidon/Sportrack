package com.example.sportrack.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.sportrack.ui.theme.SportGreen

@Composable
fun SportCheckboxBlock(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier, // <--- Принимаем настройки снаружи
    color: Color = SportGreen
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (checked) color else Color.White,
        label = "bgColor"
    )

    // Цвет рамки самого квадратика
    val checkboxBorderColor = if (checked) Color.Transparent else Color(0xFFE5E5E5)

    // Если блок выбран, можно подсветить рамку всей карточки (опционально)
    val cardBorderColor = if (checked) color.copy(alpha = 0.5f) else Color(0xFFE5E5E5)

    Card(
        // 1. Применяем внешний modifier ТОЛЬКО сюда
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)) // Обрезаем клик по форме
            .clickable { onCheckedChange(!checked) }, // Клик на всю карточку
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        // Рамка карточки
        border = BorderStroke(2.dp, cardBorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp) // В стиле Дуолинго тени часто плоские или жесткие
    ) {
        Row(
            modifier = Modifier // <--- Тут создаем НОВЫЙ модификатор
                .fillMaxWidth()
                .padding(12.dp), // Внутренний отступ (создает высоту карточки)
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Квадратик (Чекбокс)
            Box(
                modifier = Modifier // <--- И тут НОВЫЙ
                    .size(28.dp) // Чуть аккуратнее размер
                    .clip(RoundedCornerShape(8.dp))
                    .background(backgroundColor)
                    .border(
                        width = if (checked) 0.dp else 3.dp,
                        color = checkboxBorderColor,
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (checked) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Текст
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = if (checked) color else Color(0xFF4B4B4B) // Подсвечиваем текст, если выбрано
            )
        }
    }
}