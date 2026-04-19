package com.example.sarah_app.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sarah_app.data.db.ChatSessionEntity
import com.example.sarah_app.ui.theme.BackgroundCream
import com.example.sarah_app.ui.theme.BrownText
import com.example.sarah_app.ui.theme.SurfaceLight
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatHistoryScreen(viewModel: ChatHistoryViewModel = hiltViewModel()) {
    val sessions by viewModel.sessions.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundCream),
    ) {
        if (sessions.isEmpty()) {
            Text(
                text = "No conversations yet",
                fontFamily = FontFamily.Serif,
                fontSize = 18.sp,
                color = BrownText.copy(alpha = 0.5f),
                modifier = Modifier.align(Alignment.Center),
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(sessions, key = { it.id }) { session ->
                    SessionCard(session)
                }
            }
        }
    }
}

@Composable
private fun SessionCard(session: ChatSessionEntity) {
    val dateStr = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        .format(Date(session.createdAt))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = session.preview,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = BrownText,
                maxLines = 2,
            )
            Text(
                text = dateStr,
                fontSize = 12.sp,
                color = BrownText.copy(alpha = 0.55f),
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}
