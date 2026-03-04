package com.example.sportrack.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SportTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            color = Color(0xFF4B4B4B),
            fontWeight = FontWeight.Bold
        ),
        keyboardOptions = keyboardOptions,
        decorationBox = { innerTextField ->
            // Рисуем коробку для ввода
            Box(
                modifier = modifier
                    .height(56.dp)
                    .fillMaxWidth()
                    .border(2.dp, Color(0xFFE5E5E5), RoundedCornerShape(16.dp)) // Жирная серая рамка
                    .background(Color(0xFFFAFAFA), RoundedCornerShape(16.dp))   // Светло-серый фон
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                innerTextField() // Сюда встанет курсор и текст
            }
        }
    )
}