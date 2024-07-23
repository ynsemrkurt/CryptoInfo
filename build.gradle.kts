buildscript {
    repositories {
        google()
    }
    dependencies {
        classpath(libs.navigation.safe.args.gradle.plugin)
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
}