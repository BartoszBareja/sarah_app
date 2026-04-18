package com.example.sarah_app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sarah_app.ui.theme.AccentOrange
import com.example.sarah_app.ui.theme.BackgroundCream
import com.example.sarah_app.ui.theme.BrownText
import com.example.sarah_app.ui.theme.CardBrown
import com.example.sarah_app.ui.theme.PrimaryOrange
import com.example.sarah_app.ui.theme.ProfileOrange
import com.example.sarah_app.ui.theme.SurfaceLight

@Composable
fun HomeScreen(
    onNavigateToBreathing: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToRateUs: () -> Unit,
    onNavigateToAbout: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundCream)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            HomeCard(
                title = "Breathing\nExercises",
                background = PrimaryOrange,
                textColor = Color.White,
                modifier = Modifier
                    .weight(1f)
                    .height(140.dp),
                onClick = onNavigateToBreathing,
            )
            HomeCard(
                title = "Profile",
                background = ProfileOrange,
                textColor = Color.White,
                modifier = Modifier
                    .weight(1f)
                    .height(140.dp),
                onClick = onNavigateToProfile,
            )
        }

        ChatCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            onClick = onNavigateToChat,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            HomeCard(
                title = "Rate Us",
                background = AccentOrange,
                textColor = Color.White,
                modifier = Modifier
                    .weight(1f)
                    .height(140.dp),
                onClick = onNavigateToRateUs,
            )
            HomeCard(
                title = "About\nSarah",
                background = CardBrown,
                textColor = Color.White,
                modifier = Modifier
                    .weight(1f)
                    .height(140.dp),
                onClick = onNavigateToAbout,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun HomeCard(
    title: String,
    background: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(background)
            .clickable { onClick() }
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = title,
            color = textColor,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            lineHeight = 24.sp,
        )
    }
}

@Composable
private fun ChatCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceLight)
            .clickable { onClick() }
            .padding(16.dp),
    ) {
        Text(
            text = "Chat with Sarah",
            color = BrownText,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            fontFamily = FontFamily.Serif,
            modifier = Modifier.align(Alignment.CenterStart),
        )
        // Red panda face placeholder
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(AccentOrange.copy(alpha = 0.3f))
                .align(Alignment.CenterEnd),
            contentAlignment = Alignment.Center,
        ) {
            Text("🐼", fontSize = 32.sp)
        }
    }
}
