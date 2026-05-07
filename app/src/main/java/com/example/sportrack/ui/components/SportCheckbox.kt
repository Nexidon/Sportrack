package com.example.sportrack.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.sportrack.ui.theme.SportGreen

@Composable
fun SportCheckbox(
    checked: Boolean,
    color: Color = SportGreen,
    modifier: Modifier,
    onCheckedChange: (Boolean) -> Unit
)
{
    val backgroundColor by animateColorAsState(
        targetValue = if (checked) color else Color.White,
        label = "bgColor"
    )

    //Цвет рамки
    val borderColor = if (checked) Color.Transparent else Color(0xFFE5E5E5)

    Box(modifier = modifier
        .size(32.dp)
        .clip(RoundedCornerShape(8.dp))
        .clickable{onCheckedChange(!checked)}
        .background(backgroundColor)
        .border(
            width = if (checked) 0.dp else 3.dp,
            color = borderColor,
            shape = RoundedCornerShape(8.dp)
        ),
        contentAlignment = Alignment.Center
    )
    {
        if (checked) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color.White, // Белая галочка на зеленом
                modifier = Modifier.size(20.dp) // Чуть меньше самого квадрата
            )
        }
    }


}