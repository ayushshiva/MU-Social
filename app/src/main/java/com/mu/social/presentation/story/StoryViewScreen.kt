package com.mu.social.presentation.story

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.mu.social.domain.model.StoryType
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoryViewScreen(
    navController: NavController,
    userId: String,
    viewModel: StoryViewModel = hiltViewModel()
) {
    val state = viewModel.state.value
    val userStories = remember(state.stories) { state.stories.filter { it.userId == userId } }
    var currentStoryIndex by remember { mutableStateOf(0) }
    var replyText by remember { mutableStateOf("") }
    var showViewerList by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (userStories.isEmpty()) {
        LaunchedEffect(Unit) {
            navController.popBackStack()
        }
        return
    }

    val currentStory = userStories[currentStoryIndex]
    val isOwner = currentStory.userId == state.currentUserId
    val isLiked = currentStory.likes.contains(state.currentUserId)

    LaunchedEffect(currentStoryIndex) {
        viewModel.viewStory(currentStory.storyId)
        delay(5000) // 5 seconds per story
        if (!showViewerList) {
            if (currentStoryIndex < userStories.size - 1) {
                currentStoryIndex++
            } else {
                navController.popBackStack()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        when (currentStory.storyType) {
            StoryType.IMAGE -> {
                AsyncImage(
                    model = currentStory.mediaUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
            StoryType.TEXT -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = currentStory.text,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        modifier = Modifier.padding(32.dp)
                    )
                }
            }
            StoryType.VIDEO -> {
                Text("Video Story not implemented yet", color = Color.White, modifier = Modifier.align(Alignment.Center))
            }
        }

        // Progress indicators
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 8.dp, end = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            userStories.forEachIndexed { index, _ ->
                LinearProgressIndicator(
                    progress = if (index < currentStoryIndex) 1f else if (index == currentStoryIndex) 0.5f else 0f,
                    modifier = Modifier.weight(1f),
                    color = Color.White,
                    trackColor = Color.Gray.copy(alpha = 0.5f)
                )
            }
        }

        // User info and close button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp, start = 16.dp, end = 16.dp)
                .align(Alignment.TopStart),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = currentStory.userProfilePicture,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp).clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = currentStory.username, color = Color.White, fontWeight = FontWeight.Bold)
            }
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }
        }

        // Bottom interactions
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            if (isOwner) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.fetchViewerDetails(currentStory.viewers)
                            showViewerList = true
                        },
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Visibility, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "${currentStory.viewers.size} views", color = Color.White, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isOwner) {
                    TextField(
                        value = replyText,
                        onValueChange = { replyText = it },
                        placeholder = { Text("Reply...", color = Color.LightGray) },
                        modifier = Modifier.weight(1f),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color.White,
                            focusedIndicatorColor = Color.White,
                            unfocusedIndicatorColor = Color.Gray
                        )
                    )
                    IconButton(onClick = {
                        if (replyText.isNotBlank()) {
                            viewModel.sendReply(currentStory.storyId, currentStory.userId, replyText)
                            replyText = ""
                        }
                    }) {
                        Icon(Icons.Default.Send, contentDescription = "Send Reply", tint = Color.White)
                    }
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(onClick = { viewModel.likeStory(currentStory.storyId) }) {
                        Icon(
                            imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (isLiked) Color.Red else Color.White
                        )
                    }
                    if (currentStory.likes.isNotEmpty()) {
                        Text(text = "${currentStory.likes.size}", color = Color.White, fontSize = 12.sp)
                    }
                }
            }
        }
    }

    if (showViewerList) {
        ModalBottomSheet(
            onDismissRequest = { showViewerList = false },
            sheetState = sheetState
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text(text = "Viewers", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn {
                    items(state.viewerDetails) { viewer ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = viewer.profilePictureUrl,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp).clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = viewer.username, fontWeight = FontWeight.SemiBold)
                                Text(text = viewer.userId, fontSize = 10.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
}
