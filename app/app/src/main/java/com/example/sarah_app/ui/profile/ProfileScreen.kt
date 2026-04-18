package com.example.sarah_app.ui.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.sarah_app.data.db.UserProfileEntity
import com.example.sarah_app.ui.theme.AccentOrange
import com.example.sarah_app.ui.theme.BackgroundCream
import com.example.sarah_app.ui.theme.BrownText
import com.example.sarah_app.ui.theme.SurfaceLight

@Composable
fun ProfileScreen(viewModel: ProfileViewModel = hiltViewModel()) {
    val profile by viewModel.profile.collectAsState()
    val current = profile ?: UserProfileEntity()

    var showNameDialog by remember { mutableStateOf(false) }
    var showAgeDialog by remember { mutableStateOf(false) }
    var showSexMenu by remember { mutableStateOf(false) }
    var showPronounsMenu by remember { mutableStateOf(false) }

    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) viewModel.save(current.copy(photoUri = uri.toString()))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundCream)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .size(112.dp)
                .clip(CircleShape)
                .background(SurfaceLight)
                .clickable {
                    photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
            contentAlignment = Alignment.Center,
        ) {
            if (current.photoUri != null) {
                AsyncImage(
                    model = current.photoUri,
                    contentDescription = "Profile photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                )
            } else {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = BrownText.copy(alpha = 0.5f),
                    modifier = Modifier.size(56.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = current.name.ifEmpty { "Tap to set name" },
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Normal,
            fontSize = 20.sp,
            color = BrownText,
            modifier = Modifier.clickable { showNameDialog = true },
        )

        Spacer(modifier = Modifier.height(24.dp))

        ProfileCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("Mood:", fontWeight = FontWeight.Bold, color = BrownText, fontSize = 16.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    listOf("😞" to 0, "😐" to 1, "😊" to 2).forEach { (emoji, value) ->
                        val selected = current.mood == value
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (selected) AccentOrange else Color.Transparent)
                                .clickable { viewModel.save(current.copy(mood = value)) },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(emoji, fontSize = 22.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        ProfileCard(onClick = { showAgeDialog = true }) {
            Text(
                "Age:  ${if (current.age > 0) current.age else "—"}",
                color = BrownText,
                fontSize = 16.sp,
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box {
            ProfileCard(onClick = { showSexMenu = true }) {
                Text("Sex:  ${current.sex.ifEmpty { "—" }}", color = BrownText, fontSize = 16.sp)
            }
            DropdownMenu(expanded = showSexMenu, onDismissRequest = { showSexMenu = false }) {
                listOf("Male", "Female", "Other").forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            viewModel.save(current.copy(sex = option))
                            showSexMenu = false
                        },
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box {
            ProfileCard(onClick = { showPronounsMenu = true }) {
                Text("Pronouns:  ${current.pronouns.ifEmpty { "—" }}", color = BrownText, fontSize = 16.sp)
            }
            DropdownMenu(expanded = showPronounsMenu, onDismissRequest = { showPronounsMenu = false }) {
                listOf("He/Him", "She/Her", "They/Them", "Other").forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            viewModel.save(current.copy(pronouns = option))
                            showPronounsMenu = false
                        },
                    )
                }
            }
        }
    }

    if (showNameDialog) {
        var nameInput by remember { mutableStateOf(current.name) }
        AlertDialog(
            onDismissRequest = { showNameDialog = false },
            title = { Text("Your name", fontFamily = FontFamily.Serif) },
            text = {
                TextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = SurfaceLight,
                        unfocusedContainerColor = SurfaceLight,
                    ),
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.save(current.copy(name = nameInput))
                    showNameDialog = false
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showNameDialog = false }) { Text("Cancel") }
            },
        )
    }

    if (showAgeDialog) {
        var ageInput by remember { mutableStateOf(if (current.age > 0) current.age.toString() else "") }
        AlertDialog(
            onDismissRequest = { showAgeDialog = false },
            title = { Text("Your age", fontFamily = FontFamily.Serif) },
            text = {
                TextField(
                    value = ageInput,
                    onValueChange = { ageInput = it.filter { c -> c.isDigit() } },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = SurfaceLight,
                        unfocusedContainerColor = SurfaceLight,
                    ),
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.save(current.copy(age = ageInput.toIntOrNull() ?: 0))
                    showAgeDialog = false
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showAgeDialog = false }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun ProfileCard(
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            content()
        }
    }
}
