package com.example.sportrack.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.sportrack.ui.theme.SportGreen
import com.example.sportrack.ui.theme.SportGreenDark

@Composable
fun SportCard(
    title: String,
    subtitle: String? = null,
    isActive: Boolean = false, // Если день выбран или группа назначена
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Цвета: Если активно - Зеленый, если нет - Белый (но с толстой рамкой)
    val mainColor = if (isActive) SportGreen else Color.White
    val shadowColor = if (isActive) SportGreenDark else Color(0xFFE5E5E5) // Серый для тени обычных карт
    val textColor = if (isActive) Color.White else Color(0xFF4B4B4B) // Темно-серый текст на белом
    val subTextColor = if (isActive) Color(0xFFE0F2F1) else SportGreen // Зеленый подтекст на белом

    // Контейнер с "тенью" (нижний слой)
    Box(
        modifier = modifier
            .height(100.dp) // Высота карточки
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)) // Скругление
            .background(shadowColor) // Цвет "дна" (тени)
            .clickable(onClick = onClick)
    ) {
        // Лицевая часть (сдвинута вверх, чтобы открыть "дно")
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(bottom = 6.dp) // <--- ВОТ ОН, 3D ЭФФЕКТ (отступ снизу)
                .background(mainColor, shape = RoundedCornerShape(16.dp))
                // Если карта белая - добавим ей рамку
                .then(
                    if (!isActive) Modifier.border(2.dp, Color(0xFFE5E5E5), RoundedCornerShape(16.dp))
                    else Modifier
                )
                .padding(12.dp), // Внутренний отступ текста
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = textColor
            )

            if (subtitle != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = subTextColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}