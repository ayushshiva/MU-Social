package com.mu.social.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mu.social.presentation.auth.login.LoginScreen
import com.mu.social.presentation.auth.signup.SignUpScreen
import com.mu.social.presentation.home.HomeScreen
import com.mu.social.presentation.profile.ProfileScreen
import com.mu.social.presentation.chat.ChatListScreen
import com.mu.social.presentation.chat.ChatDetailScreen
import com.mu.social.presentation.post.CreatePostScreen
import com.mu.social.presentation.search.SearchScreen
import com.mu.social.presentation.reels.ReelsScreen
import com.mu.social.presentation.comments.CommentsScreen
import com.mu.social.presentation.profile.edit_profile.EditProfileScreen
import com.mu.social.presentation.story.StoryViewScreen
import com.mu.social.presentation.story.CreateStoryScreen
import com.mu.social.presentation.notification.NotificationScreen
import com.mu.social.presentation.dashboard.CreatorDashboardScreen
import com.mu.social.presentation.admin.ModerationScreen
import com.mu.social.presentation.live.CreateLiveScreen
import com.mu.social.presentation.live.LiveBroadcasterScreen
import com.mu.social.presentation.live.LiveFeedScreen
import com.mu.social.presentation.live.LiveViewerScreen
import com.mu.social.presentation.live.WalletScreen

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Login.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // ... (existing routes)
        composable(Screen.Login.route) { LoginScreen(navController) }
        composable(Screen.SignUp.route) { SignUpScreen(navController) }
        composable(Screen.Home.route) { HomeScreen(navController) }
        composable(Screen.Search.route) { SearchScreen(navController) }
        composable(Screen.Reels.route) { ReelsScreen(navController) }
        composable(Screen.CreatePost.route) { CreatePostScreen(navController) }
        composable(Screen.ChatList.route) { ChatListScreen(navController) }
        composable(
            route = Screen.Profile.route,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { ProfileScreen(navController) }
        composable(
            route = Screen.ChatDetail.route,
            arguments = listOf(navArgument("chatId") { type = NavType.StringType })
        ) { ChatDetailScreen(navController) }
        composable(
            route = Screen.Comments.route,
            arguments = listOf(navArgument("postId") { type = NavType.StringType })
        ) { CommentsScreen(navController) }
        composable(Screen.EditProfile.route) {
            android.util.Log.d("EDIT_PROFILE", "NavGraph reached")
            EditProfileScreen(navController)
        }
        composable(Screen.CreateStory.route) { CreateStoryScreen(navController) }
        composable(Screen.Notifications.route) { NotificationScreen(navController) }
        composable(Screen.Dashboard.route) { CreatorDashboardScreen(navController) }
        composable(Screen.Moderation.route) { ModerationScreen(navController) }
        composable(
            route = Screen.StoryView.route,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            StoryViewScreen(navController, userId)
        }

        // Live Streaming
        composable(Screen.LiveFeed.route) {
            LiveFeedScreen(navController)
        }
        composable(Screen.CreateLive.route) {
            CreateLiveScreen(navController)
        }
        composable(
            route = Screen.LiveBroadcaster.route,
            arguments = listOf(navArgument("streamId") { type = NavType.StringType })
        ) { backStackEntry ->
            val streamId = backStackEntry.arguments?.getString("streamId") ?: ""
            LiveBroadcasterScreen(navController, streamId)
        }
        composable(
            route = Screen.LiveViewer.route,
            arguments = listOf(navArgument("streamId") { type = NavType.StringType })
        ) { backStackEntry ->
            val streamId = backStackEntry.arguments?.getString("streamId") ?: ""
            LiveViewerScreen(navController, streamId)
        }
        composable(Screen.Wallet.route) {
            WalletScreen(onBack = { navController.popBackStack() })
        }
    }
}
