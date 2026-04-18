package com.example.sarah_app.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sarah_app.ui.theme.BackgroundCream
import com.example.sarah_app.ui.theme.BrownText

@Composable
fun ChatHistoryScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundCream),
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            // Chat session items will be populated in the next phase
        }
        Text(
            text = "No conversations yet",
            fontFamily = FontFamily.Serif,
            fontSize = 18.sp,
            color = BrownText.copy(alpha = 0.5f),
            modifier = Modifier.align(Alignment.Center),
        )
    }
}
