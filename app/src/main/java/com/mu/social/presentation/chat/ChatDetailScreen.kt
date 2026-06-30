package com.mu.social.presentation.chat

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.mu.social.domain.model.Message
import com.mu.social.domain.model.MessageType
import com.mu.social.presentation.components.VideoPlayer
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    navController: NavController,
    viewModel: ChatDetailViewModel = hiltViewModel()
) {
    val state = viewModel.state.value
    val messageText = viewModel.messageText.value
    val listState = rememberLazyListState()
    val clipboardManager = LocalClipboardManager.current

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.sendMessage(it, MessageType.IMAGE) }
    }

    if (state.error.isNotEmpty()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = "Messages") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = state.error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { navController.popBackStack() }) {
                        Text("Go Back")
                    }
                }
            }
        }
        return
    }


    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = state.partnerUser?.profilePictureUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.LightGray),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = state.partnerUser?.fullName ?: "Loading...",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (state.partnerUser?.isOnline == true) "Online" 
                                       else "Last seen ${formatLastSeen(state.partnerUser?.lastSeen)}",
                                fontSize = 12.sp,
                                color = if (state.partnerUser?.isOnline == true) Color.Green else Color.Gray
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Column {
                if (state.chat?.typingStatus?.get(state.partnerUser?.userId) == true) {
                    Text(
                        text = "${state.partnerUser?.username} is typing...",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                
                state.replyingTo?.let { replyMsg ->
                    ReplyPreview(
                        message = replyMsg,
                        onCancel = { viewModel.setReplyTo(null) }
                    )
                }

                ChatInputBar(
                    text = messageText,
                    isModerating = state.isModerating,
                    onTextChange = viewModel::onMessageChange,
                    onSend = { viewModel.moderateAndSendMessage() },
                    onMediaClick = { galleryLauncher.launch("image/*") }
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp)
            ) {
                val groupedMessages = state.messages.groupBy { 
                    SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(it.timestamp))
                }

                groupedMessages.forEach { (date, messages) ->
                    item {
                        DateHeader(date)
                    }
                    items(messages) { message ->
                        MessageBubble(
                            message = message,
                            isMine = message.senderId == viewModel.currentUserId,
                            onReply = { viewModel.setReplyTo(message) },
                            onDelete = { forEveryone -> 
                                viewModel.deleteMessage(message.messageId, forEveryone) 
                            },
                            onCopy = {
                                clipboardManager.setText(AnnotatedString(message.text))
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun DateHeader(date: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = Color.LightGray.copy(alpha = 0.3f),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = date,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                fontSize = 12.sp,
                color = Color.DarkGray
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(
    message: Message,
    isMine: Boolean,
    onReply: () -> Unit,
    onDelete: (Boolean) -> Unit,
    onCopy: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isMine) Alignment.End else Alignment.Start
    ) {
        if (message.isDeletedForEveryone) {
            DeletedBubble(isMine)
        } else {
            Surface(
                color = if (isMine) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isMine) 16.dp else 2.dp,
                    bottomEnd = if (isMine) 2.dp else 16.dp
                ),
                modifier = Modifier
                    .combinedClickable(
                        onClick = {},
                        onLongClick = { showMenu = true }
                    )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    if (message.replyToText != null) {
                        ReplyBubble(text = message.replyToText)
                    }
                    
                    when (message.messageType) {
                        MessageType.IMAGE -> {
                            AsyncImage(
                                model = message.mediaUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(200.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                        MessageType.VIDEO -> {
                            VideoPlayer(
                                videoUrl = message.mediaUrl,
                                isPlaying = false,
                                modifier = Modifier.size(200.dp)
                            )
                        }
                        else -> {
                            Text(
                                text = message.text,
                                color = if (isMine) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.align(Alignment.End),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp)),
                            fontSize = 10.sp,
                            color = if (isMine) Color.White.copy(alpha = 0.7f) else Color.Gray
                        )
                        if (isMine) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = if (message.seen) Icons.Default.DoneAll else Icons.Default.Done,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = if (message.seen) Color.Cyan else Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }

        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
            DropdownMenuItem(text = { Text("Reply") }, onClick = { onReply(); showMenu = false })
            DropdownMenuItem(text = { Text("Copy") }, onClick = { onCopy(); showMenu = false })
            DropdownMenuItem(text = { Text("Delete") }, onClick = { onDelete(true); showMenu = false })
        }
    }
}

@Composable
fun DeletedBubble(isMine: Boolean) {
    Surface(
        color = Color.Transparent,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray),
        shape = RoundedCornerShape(
            topStart = 16.dp,
            topEnd = 16.dp,
            bottomStart = if (isMine) 16.dp else 2.dp,
            bottomEnd = if (isMine) 2.dp else 16.dp
        )
    ) {
        Text(
            text = "This message was deleted",
            modifier = Modifier.padding(12.dp),
            fontSize = 14.sp,
            color = Color.Gray,
            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
        )
    }
}

@Composable
fun ReplyBubble(text: String) {
    Surface(
        color = Color.Black.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.padding(bottom = 8.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(8.dp),
            fontSize = 12.sp,
            maxLines = 2,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
    }
}

@Composable
fun ReplyPreview(message: Message, onCancel: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Replying to", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text(message.text, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
            }
            IconButton(onClick = onCancel) {
                Icon(Icons.Default.Close, contentDescription = "Cancel")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatInputBar(
    text: String,
    isModerating: Boolean,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    onMediaClick: () -> Unit
) {
    Surface(
        tonalElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .navigationBarsPadding()
                .imePadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onMediaClick, enabled = !isModerating) {
                Icon(Icons.Default.Add, contentDescription = "Add Media", tint = MaterialTheme.colorScheme.primary)
            }
            TextField(
                value = text,
                onValueChange = onTextChange,
                placeholder = { Text("Message...") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                enabled = !isModerating,
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                )
            )
            if (isModerating) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp).padding(horizontal = 8.dp))
            } else {
                IconButton(onClick = onSend) {
                    Icon(Icons.Default.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

fun formatLastSeen(timestamp: Long?): String {
    if (timestamp == null) return "Unknown"
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
