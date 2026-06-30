package com.mu.social.presentation.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.mu.social.domain.model.Post
import com.mu.social.presentation.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state = viewModel.state.value
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = state.user?.username ?: "Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            state.user?.let { user ->
                Column(modifier = Modifier.fillMaxSize()) {
                    // Profile Header
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AsyncImage(
                            model = user.profilePictureUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = user.fullName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Text(
                            text = "@${user.username}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                        if (user.bio.isNotEmpty()) {
                            Text(
                                text = user.bio,
                                modifier = Modifier.padding(top = 8.dp),
                                fontSize = 14.sp
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Stats
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            ProfileStatItem(label = "Posts", value = state.posts.size.toString())
                            ProfileStatItem(label = "Followers", value = user.followersCount.toString())
                            ProfileStatItem(label = "Following", value = user.followingCount.toString())
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Action Button
                        if (state.isCurrentUser) {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Button(
                                    onClick = { /* Navigate to Edit Profile */ },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Edit Profile")
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                OutlinedButton(
                                    onClick = { navController.navigate(Screen.Wallet.route) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Wallet 🪙")
                                }
                            }
                        } else {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Button(
                                    onClick = {
                                        if (state.isFollowing) viewModel.unfollowUser() else viewModel.followUser()
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = if (state.isFollowing) {
                                        ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                                    } else {
                                        ButtonDefaults.buttonColors()
                                    }
                                ) {
                                    Text(if (state.isFollowing) "Unfollow" else "Follow")
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        val profileUserId = user.userId
                                        scope.launch {
                                            val result = viewModel.startChat(profileUserId)
                                            if (result is com.mu.social.utils.Resource.Success) {
                                                navController.navigate(Screen.ChatDetail.createRoute(result.data ?: ""))
                                            }
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Message")
                                }

                            }

                        }

                    }

                    Divider()

                    // Posts Grid
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(state.posts) { post ->
                            AsyncImage(
                                model = post.mediaUrls.firstOrNull(),
                                contentDescription = null,
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .padding(1.dp),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }

            if (state.error.isNotEmpty()) {
                Text(
                    text = state.error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
fun ProfileStatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(text = label, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
