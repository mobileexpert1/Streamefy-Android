plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
}

android {
    namespace = "com.streamefy"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.streamefy"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        vectorDrawables {
            useSupportLibrary = true
        }

    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures{
        viewBinding =true
        dataBinding=true
        buildConfig = true
        compose = true
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.leanback)
    implementation(libs.androidx.appcompat)

    implementation(libs.glide)
    implementation(libs.viewmodel)
    implementation(libs.livedata)
    implementation(libs.annotation)
    implementation(libs.databindingcommon)
    implementation(libs.databindingruntime)
    implementation(libs.activityktx)
    implementation(libs.navigationfragment)
    implementation(libs.navigationui)
    implementation(libs.sdp)
    implementation(libs.koin)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.exoplayer)
    implementation(libs.autoimageslider)
    implementation(libs.drawerlayout)
    implementation(libs.retrofit)
    implementation(libs.gson)
    implementation(libs.interceptor)
    implementation(libs.coroutines)
//    implementation(libs.bitmovin)
    implementation("com.bitmovin.player:player:3.82.0")
    implementation("commons-codec:commons-codec:1.17.1")
    implementation("org.apache.httpcomponents.core5:httpcore5:5.2.5")
    implementation("junit:junit:4.12")
    implementation ("com.github.appsfeature:otp-view:1.1")
//    implementation(libs.androidx.media3.exoplayer.hls)


    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}