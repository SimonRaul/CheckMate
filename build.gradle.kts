// Top-level build file. Kotlin is compiled by AGP's built-in Kotlin support (AGP 9+),
// so the kotlin-android plugin is intentionally not applied.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
}
