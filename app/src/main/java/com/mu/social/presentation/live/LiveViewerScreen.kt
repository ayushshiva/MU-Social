package com.mu.social.presentation.live

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mu.social.live.AgoraManager
import com.mu.social.presentation.live.components.GiftBottomSheet
import com.mu.social.presentation.live.components.LiveChatComponent

@Composable
fun LiveViewerScreen(
    navController: NavController,
    streamId: String,
    viewModel: LiveStreamViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val currentStream by viewModel.currentStream.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val remoteUid by viewModel.agoraManager.remoteUid.collectAsState()
    var showGiftSheet by remember { mutableStateOf(false) }

    LaunchedEffect(streamId) {
        viewModel.joinStream(streamId)
        viewModel.joinAgoraChannel(context, streamId, false)
    }

    if (showGiftSheet) {
        GiftBottomSheet(
            onGiftSelected = { gift ->
                viewModel.sendGift(streamId, gift)
            },
            onRecharge = {
                navController.navigate(com.mu.social.presentation.navigation.Screen.Wallet.route)
            },
            onDismiss = { showGiftSheet = false }
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.agoraManager.leaveChannel()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Remote Video (Broadcaster)
        if (remoteUid != null) {
            val uid = remoteUid
            AndroidView(
                factory = { ctx ->
                    FrameLayout(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        if (uid != null) {
                            viewModel.agoraManager.setupRemoteVideo(this, uid)
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color.White)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Connecting to stream...", color = Color.White)
                }
            }
        }

        // Overlay UI
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Color.Red,
                        shape = CircleShape
                    ) {
                        Text(
                            text = "LIVE",
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${currentStream?.viewersCount ?: 0}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }

                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Exit", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Chat & Actions
            Row(modifier = Modifier.fillMaxWidth().height(300.dp)) {
                LiveChatComponent(
                    messages = chatMessages,
                    onSendMessage = { viewModel.sendChatMessage(streamId, it) },
                    modifier = Modifier.weight(1f)
                )

                Column(
                    modifier = Modifier.padding(start = 8.dp),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    IconButton(
                        onClick = { showGiftSheet = true },
                        modifier = Modifier.background(Color.Yellow.copy(alpha = 0.7f), CircleShape)
                    ) {
                        Text("🎁", fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    IconButton(
                        onClick = { viewModel.sendLike(streamId) },
                        modifier = Modifier.background(Color.Red.copy(alpha = 0.7f), CircleShape)
                    ) {
                        Icon(Icons.Default.Favorite, contentDescription = "Like", tint = Color.White)
                    }
                }
            }
        }
    }
}
