package com.example.sportrack.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.sportrack.ui.theme.SportGreen

@Composable
fun SportProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = SportGreen
)
{
Box(
    modifier = modifier
        .height(20.dp)
        .fillMaxWidth()
        .background(Color(0xFFE5E5E5), RoundedCornerShape(50))
)
{
Box(
    modifier = Modifier
        .fillMaxWidth(progress)
        .fillMaxHeight()
        .background(color, RoundedCornerShape(50))
)
    Box(
        modifier = Modifier
            .fillMaxWidth(progress)
            .height(6.dp)
            .padding(horizontal = 4.dp, vertical = 2.dp)
            .background(Color.White.copy(alpha = 0.3f), RoundedCornerShape(50))
    )

}
}