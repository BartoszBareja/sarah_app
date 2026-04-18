package com.example.sarah_app.ui.about

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sarah_app.ui.theme.BackgroundCream
import com.example.sarah_app.ui.theme.BrownText
import com.example.sarah_app.ui.theme.SurfaceLight

@Composable
fun AboutSarahScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundCream)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Team photo placeholder
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceLight),
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Sarah is a student project with a goal to help elderly people feel less alone through emotional support, breathing exercises, and a friendly companion — a red panda who is always there to listen.\n\n" +
                "Built with care by a team of students who believe technology should be warm, accessible, and human.\n",
            fontFamily = FontFamily.Default,
            fontSize = 16.sp,
            lineHeight = 26.sp,
            color = BrownText,
        )


        Text(
            buildAnnotatedString {
                append("Visit our site at: \n")
                withLink(
                    LinkAnnotation.Url(
                        "https://konar.pwr.edu.pl/project/sarah",
                        TextLinkStyles(style = SpanStyle(color = BrownText)),
                    )
                ) {
                    append("https://konar.pwr.edu.pl/project/sarah")
                }
            }
        )
    }
}
