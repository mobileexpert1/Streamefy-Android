// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        mavenCentral()
        jcenter() // Optional
        maven { url = uri("https://jitpack.io") }
//        maven {
//            url = uri("https://artifacts.bitmovin.com/artifactory/public-releases")
//        }
    }
    dependencies {
        // Add your classpath dependencies here
        classpath("com.android.tools.build:gradle:8.0.0")
        classpath("com.google.gms:google-services:4.3.15")

    }
}

plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.jetbrainsKotlinAndroid) apply false
}