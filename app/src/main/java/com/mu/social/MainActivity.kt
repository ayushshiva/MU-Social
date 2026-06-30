package com.mu.social

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.mu.social.presentation.navigation.NavGraph
import com.mu.social.presentation.navigation.Screen
import com.mu.social.ui.theme.MUSocialTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MUSocialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavGraph(navController = navController)
                    
                    // Handle Deep Link from Notification
                    LaunchedEffect(intent) {
                        handleIntent(intent, navController)
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    private fun handleIntent(intent: Intent?, navController: androidx.navigation.NavHostController) {
        val type = intent?.getStringExtra("type")
        val contentId = intent?.getStringExtra("contentId")
        
        if (type != null && contentId != null) {
            when (type) {
                "FOLLOW" -> navController.navigate(Screen.Profile.createRoute(contentId))
                "LIKE", "COMMENT" -> navController.navigate(Screen.Comments.createRoute(contentId))
"MESSAGE" -> {
                    val chatId = intent.getStringExtra("chatId") ?: contentId
                    if (!chatId.isNullOrBlank()) {
                        navController.navigate(Screen.ChatDetail.createRoute(chatId))
                    }
                }
            }
        }
    }
}

@androidx.compose.runtime.Composable
fun LaunchedEffect(intent: Intent?, block: suspend () -> Unit) {
    androidx.compose.runtime.LaunchedEffect(intent) {
        block()
    }
}
