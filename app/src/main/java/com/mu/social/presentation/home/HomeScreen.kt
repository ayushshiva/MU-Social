package com.mu.social.presentation.home

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.mu.social.domain.model.Report
import com.mu.social.domain.model.ReportReason
import com.mu.social.domain.model.ReportType
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.mu.social.presentation.components.PostItem
import com.mu.social.presentation.navigation.Screen
import com.mu.social.presentation.story.StoryItem
import com.mu.social.presentation.story.StoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel(),
    storyViewModel: StoryViewModel = hiltViewModel()
) {
    val state = viewModel.state.value
    val storyState = storyViewModel.state.value
    var showReportDialog by remember { mutableStateOf<String?>(null) }

    if (showReportDialog != null) {
        ReportDialog(
            onDismiss = { showReportDialog = null },
            onReport = { reason, desc ->
                showReportDialog?.let { postId ->
                    viewModel.reportPost(postId, reason, desc)
                }
                showReportDialog = null
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "MU Social",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Notifications.route) }) {
                        BadgedBox(
                            badge = {
                                if (state.unreadCount > 0) {
                                    Badge { Text(state.unreadCount.toString()) }
                                }
                            }
                        ) {
                            Icon(Icons.Outlined.Notifications, contentDescription = "Notifications")
                        }
                    }
IconButton(onClick = { navController.navigate(Screen.ChatList.route) }) {
                        Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = "Messages")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(Screen.CreatePost.route) }) {
                Icon(Icons.Default.Add, contentDescription = "Create Post")
            }
        },
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            
            if (state.error.isNotEmpty()) {
                Text(
                    text = state.error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center).padding(16.dp)
                )
            }

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        item {
                            AddStoryItem(onClick = { navController.navigate(Screen.CreateStory.route) })
                        }
                        items(storyState.stories.distinctBy { it.userId }) { story ->
                            StoryItem(
                                story = story,
                                onClick = { navController.navigate(Screen.StoryView.createRoute(story.userId)) }
                            )
                        }
                    }
                    Divider()
                }
                items(state.posts) { post ->
                    androidx.compose.runtime.LaunchedEffect(post.postId) {
                        viewModel.trackPostView(post.postId)
                    }
                    PostItem(
                        post = post,
                        onLikeClick = { viewModel.likePost(post.postId) },
                        onCommentClick = { /* TODO: Implement navigation to comments */ },
                        onProfileClick = { navController.navigate(Screen.Profile.createRoute(post.userId)) },
                        onReportClick = { showReportDialog = post.postId }
                    )
                }
            }
        }
    }
}

@Composable
fun AddStoryItem(onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .padding(8.dp)
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(70.dp)
                .border(
                    width = 2.dp,
                    color = Color.LightGray,
                    shape = CircleShape
                )
                .padding(3.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Add Story",
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Your Story",
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}

@Composable
fun ReportDialog(
    onDismiss: () -> Unit,
    onReport: (ReportReason, String) -> Unit
) {
    var description by remember { mutableStateOf("") }
    var selectedReason by remember { mutableStateOf(ReportReason.OTHER) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Report Content") },
        text = {
            Column {
                Text("Why are you reporting this?", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                ReportReason.values().forEach { reason ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedReason = reason }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = selectedReason == reason, onClick = { selectedReason = reason })
                        Text(reason.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() })
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Additional details (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onReport(selectedReason, description) }) {
                Text("Report")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        NavigationItem("Home", Screen.Home.route, Icons.Default.Home),
        NavigationItem("Search", Screen.Search.route, Icons.Default.Search),
        NavigationItem("Reels", Screen.Reels.route, Icons.Default.PlayCircle),
        NavigationItem("Notifications", Screen.Notifications.route, Icons.Outlined.Notifications),
        NavigationItem("Profile", Screen.Profile.createRoute("current_user"), Icons.Default.Person)
    )
    
    NavigationBar {
        val currentRoute = navController.currentDestination?.route
        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(Screen.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title) }
            )
        }
    }
}

data class NavigationItem(val title: String, val route: String, val icon: ImageVector)
