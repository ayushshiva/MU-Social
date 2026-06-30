# MU Social ProGuard Rules

# Project specific rules
-keep class com.mu.social.domain.model.** { *; }
-keep class com.mu.social.presentation.** { *; }
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    public <init>(...);
}

# Hilt rules
-keep class dagger.hilt.** { *; }
-keep class com.google.dagger.** { *; }

# Firebase rules
-keep class com.google.firebase.** { *; }
-keepnames class com.google.firebase.** { *; }

# Gemini AI rules
-keep class com.google.ai.client.generativeai.** { *; }

# OkHttp/Retrofit
-keepattributes Signature, InnerClasses, EnclosingMethod
-keep class retrofit2.** { *; }
-keep class okhttp3.** { *; }
-dontwarn retrofit2.**
-dontwarn okhttp3.**

# Compose
-keep class androidx.compose.** { *; }

# Protect Repository layer
-keep class com.mu.social.data.repository.** { *; }

# Obfuscation settings
-repackageclasses 'com.mu.social.a'
-allowaccessmodification
-overloadaggressively
