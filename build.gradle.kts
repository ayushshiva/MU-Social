// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.13.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
    id("com.google.dagger.hilt.android") version "2.50" apply false
    id("com.google.gms.google-services") version "4.4.0" apply false
    id("com.google.devtools.ksp") version "1.9.22-1.0.17" apply false
    id("io.gitlab.arturbosch.detekt") version "1.23.5"
}

// Global Detekt configuration using simpler syntax for compatibility
tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    // Basic config without using unresolved root project references in top-level KTS
}

tasks.register("detektAll") {
    group = "verification"
}
