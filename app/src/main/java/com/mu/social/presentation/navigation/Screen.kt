package com.mu.social.presentation.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object SignUp : Screen("signup")
    object Home : Screen("home")
    object Search : Screen("search")
    object Reels : Screen("reels")
    object Notifications : Screen("notifications")
    object Profile : Screen("profile/{userId}") {
        fun createRoute(userId: String) = "profile/$userId"
    }
    object ChatList : Screen("chat_list")
    object ChatDetail : Screen("chat_detail/{chatId}") {
        fun createRoute(chatId: String) = "chat_detail/$chatId"
    }
    object CreatePost : Screen("create_post")
    object Comments : Screen("comments/{postId}") {
        fun createRoute(postId: String) = "comments/$postId"
    }
    object EditProfile : Screen("edit_profile")
    object Settings : Screen("settings")
    object StoryView : Screen("story_view/{userId}") {
        fun createRoute(userId: String) = "story_view/$userId"
    }
    object CreateStory : Screen("create_story")
    object Dashboard : Screen("dashboard")
    object Moderation : Screen("moderation")
    
    // Live Streaming
    object LiveFeed : Screen("live_feed")
    object CreateLive : Screen("create_live")
    object LiveBroadcaster : Screen("live_broadcaster/{streamId}") {
        fun createRoute(streamId: String) = "live_broadcaster/$streamId"
    }
    object LiveViewer : Screen("live_viewer/{streamId}") {
        fun createRoute(streamId: String) = "live_viewer/$streamId"
    }
    object Wallet : Screen("wallet")
}
