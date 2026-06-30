package com.mu.social.presentation.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mu.social.domain.model.ModerationStatus
import com.mu.social.domain.model.Report
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModerationScreen(
    navController: NavController,
    viewModel: ModerationViewModel = hiltViewModel()
) {
    val state = viewModel.state.value

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Moderation") },
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

            if (state.reports.isEmpty() && !state.isLoading) {
                Text(
                    text = "No pending reports",
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.Gray
                )
            }

            LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)) {
                items(state.reports) { report ->
                    ReportItem(
                        report = report,
                        onAction = { status, notes -> 
                            viewModel.resolveReport(report.reportId, status, notes)
                        },
                        onBanUser = {
                            viewModel.takeAction(report.reportedEntityId, "BAN", "Violating community standards")
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun ReportItem(
    report: Report,
    onAction: (ModerationStatus, String) -> Unit,
    onBanUser: () -> Unit
) {
    var notes by remember { mutableStateOf("") }
    var showActionDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = report.type.name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text(
                    text = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(report.timestamp)),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            
            Text(text = "Reason: ${report.reason.name}", fontWeight = FontWeight.Medium, modifier = Modifier.padding(vertical = 4.dp))
            Text(text = report.description, fontSize = 14.sp)
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Moderator Notes") },
                modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { onAction(ModerationStatus.DISMISSED, notes) }) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Dismiss")
                }
                Button(
                    onClick = { showActionDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Gavel, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Take Action")
                }
            }
        }
    }

    if (showActionDialog) {
        AlertDialog(
            onDismissRequest = { showActionDialog = false },
            title = { Text("Moderation Action") },
            text = { Text("Choose an action for this report. This will be applied to the reported entity/user.") },
            confirmButton = {
                Button(onClick = { 
                    onBanUser()
                    onAction(ModerationStatus.ACTION_TAKEN, notes)
                    showActionDialog = false 
                }) {
                    Text("Ban User")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    onAction(ModerationStatus.REVIEWED, notes)
                    showActionDialog = false 
                }) {
                    Text("Mark as Reviewed")
                }
            }
        )
    }
}
