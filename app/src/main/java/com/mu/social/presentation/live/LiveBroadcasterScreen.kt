package com.mu.social.presentation.live

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mu.social.live.AgoraManager
import com.mu.social.presentation.live.components.LiveChatComponent
import com.mu.social.presentation.navigation.Screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest

@Composable
fun LiveBroadcasterScreen(
    navController: NavController,
    streamId: String,
    viewModel: LiveStreamViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val currentStream by viewModel.currentStream.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val isMuted by viewModel.isMuted
    var isBeautyEnabled by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.all { it }
        if (granted) {
            viewModel.joinAgoraChannel(context, streamId, true)
        } else {
            navController.popBackStack()
        }
    }

    LaunchedEffect(streamId) {
        viewModel.joinStream(streamId)
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            )
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.endStream(streamId)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Camera Preview
        AndroidView(
            factory = { ctx ->
                FrameLayout(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    viewModel.agoraManager.setupLocalVideo(this)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

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
                
                Text(
                    text = "${currentStream?.viewersCount ?: 0} Viewers",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape).padding(horizontal = 12.dp, vertical = 4.dp)
                )

                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "End Stream", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = currentStream?.title ?: "",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            // Revenue Stats
            Row(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Star, contentDescription = null, tint = Color.Yellow, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Earnings: ${currentStream?.likesCount ?: 0}", // Reusing likesCount or add giftCount
                    color = Color.White,
                    fontSize = 14.sp
                )
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
                        onClick = { viewModel.toggleMute() },
                        modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = "Mute",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    IconButton(
                        onClick = { viewModel.agoraManager.switchCamera() },
                        modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(Icons.Default.SwitchCamera, contentDescription = "Switch Camera", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    IconButton(
                        onClick = { 
                            isBeautyEnabled = !isBeautyEnabled
                            viewModel.agoraManager.toggleBeautyFilter(isBeautyEnabled)
                        },
                        modifier = Modifier.background(
                            if (isBeautyEnabled) MaterialTheme.colorScheme.primary else Color.Black.copy(alpha = 0.5f),
                            CircleShape
                        )
                    ) {
                        Icon(Icons.Default.Face, contentDescription = "Beauty Filter", tint = Color.White)
                    }
                }
            }
        }
    }
}
