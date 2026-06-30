package com.mu.social.presentation.live

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mu.social.presentation.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateLiveScreen(
    navController: NavController,
    viewModel: LiveStreamViewModel = hiltViewModel()
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Entertainment") }
    var isPublic by remember { mutableStateOf(true) }
    val isLoading by viewModel.isLoading

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Go Live") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Stream Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Basic Category Selection (could be a dropdown in production)
            Text("Category", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FilterChip(
                    selected = category == "Entertainment",
                    onClick = { category = "Entertainment" },
                    label = { Text("Entertainment") }
                )
                FilterChip(
                    selected = category == "Gaming",
                    onClick = { category = "Gaming" },
                    label = { Text("Gaming") }
                )
                FilterChip(
                    selected = category == "Education",
                    onClick = { category = "Education" },
                    label = { Text("Education") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(checked = isPublic, onCheckedChange = { isPublic = it })
                Text("Public Stream")
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    viewModel.startLiveStream(title, category, isPublic) { streamId ->
                        navController.navigate(Screen.LiveBroadcaster.createRoute(streamId)) {
                            popUpTo(Screen.CreateLive.route) { inclusive = true }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = title.isNotBlank() && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("Start Live Stream")
                }
            }
        }
    }
}
