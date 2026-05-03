package com.example.sarah_app.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sarah_app.ui.theme.AccentOrange
import com.example.sarah_app.ui.theme.BackgroundCream
import com.example.sarah_app.ui.theme.BrownText
import com.example.sarah_app.ui.theme.PrimaryOrange
import com.example.sarah_app.ui.theme.SurfaceLight

@Composable
fun ChatScreen(
    viewModel: ChatViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundCream)
            .imePadding()
            .navigationBarsPadding(),
    ) {
        if (uiState.messages.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Hi, what's up?",
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = BrownText,
                    modifier = Modifier
                        .offset(x = 0.dp, y = 50.dp)
                )
                Spacer(modifier = Modifier.height(36.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = 50.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    AccentOrange.copy(alpha = 0.7f),
                                    Color.Transparent
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = "file:///android_asset/panda/sarah.gif",
                        contentDescription = "Sarah",
                        modifier = Modifier
                            .size(240.dp)
                            .offset(x = 15.dp, y = 0.dp)
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        // Lista wiadomości
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(uiState.messages) { message ->
                MessageBubble(message)
            }

            // "Sarah pisze..."
            if (uiState.isLoading) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp))
                                .background(SurfaceLight)
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = PrimaryOrange,
                                strokeWidth = 2.dp,
                            )
                        }
                    }
                }
            }
        }

        // Pole do wpisywania
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceLight)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = uiState.inputText,
                onValueChange = viewModel::onInputChanged,
                placeholder = {
                    Text(
                        text = "What do you want to talk about today?",
                        fontStyle = FontStyle.Italic,
                        color = BrownText.copy(alpha = 0.5f),
                        fontSize = 15.sp,
                    )
                },
                textStyle = TextStyle(fontSize = 15.sp, color = BrownText),
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryOrange,
                    unfocusedBorderColor = BrownText.copy(alpha = 0.2f),
                    focusedContainerColor = BackgroundCream,
                    unfocusedContainerColor = BackgroundCream,
                ),
                modifier = Modifier.weight(1f),
                maxLines = 4,
            )
            IconButton(
                onClick = viewModel::sendMessage,
                enabled = uiState.inputText.isNotBlank() && !uiState.isLoading,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Wyślij",
                    tint = if (uiState.inputText.isNotBlank() && !uiState.isLoading)
                        PrimaryOrange else BrownText.copy(alpha = 0.3f),
                )
            }
        }
    }

    // Dialog błędu
    uiState.errorMessage?.let { error ->
        AlertDialog(
            onDismissRequest = viewModel::dismissError,
            title = { Text("Błąd połączenia") },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = viewModel::dismissError) { Text("OK") }
            },
        )
    }
}

@Composable
private fun MessageBubble(message: ChatMessage) {
    val isUser = message.role == "user"
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(
                    if (isUser)
                        RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp)
                    else
                        RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)
                )
                .background(if (isUser) PrimaryOrange else SurfaceLight)
                .padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
            Text(
                text = message.content,
                color = if (isUser) Color.White else BrownText,
                fontSize = 15.sp,
                lineHeight = 22.sp,
            )
        }
    }
}
