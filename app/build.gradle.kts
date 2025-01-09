plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
//    alias(libs.plugins.crashlyticsFirebase)
    id("com.google.firebase.crashlytics")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.streamefy"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.streamefy"
        minSdk = 21
        targetSdk = 34
//        cupcake google play store
//        versionCode = 11
//        versionName = "1.0.11"
        // cupcake for amazon
//        versionCode = 9
//        versionName = "1.0.9"

//        fire stick streamefy
//        versionCode = 19
//        versionName = "1.0.20"
//        streameyf google play store
        versionCode = 15
        versionName = "1.0.15"
        vectorDrawables {
            useSupportLibrary = true
        }
    }
    flavorDimensions += "default"
    signingConfigs {
        create("streamefy") {
            storeFile = file("D:\\work splace\\Streamefy\\Streamefy_info\\streamefy.jks")
            storePassword = "android"
            keyAlias = "android"
            keyPassword = "android"
        }

        create("cupcake") {
//            storeFile = file("D:\\work splace\\Streamefy\\cupcake_info\\cupcake.jks")
            storeFile = file("D:\\work splace\\Streamefy\\Streamefy_info\\streamefy.jks")
            storePassword = "android"
            keyAlias = "android"
            keyPassword = "android"
        }
    }

    productFlavors {
        create("streamefy") {
            applicationId = "com.streamefy"
            versionNameSuffix = "-streamefy"
            signingConfig = signingConfigs.getByName("streamefy")
            buildConfigField("String", "Admin_email", "\"appsdev096@gmail.com\"")
            buildConfigField("String", "Password", "\"Appsdev096#\"")
        }
        create("cupcake") {
            applicationId = "com.tech.cupcake"
            versionNameSuffix = "-cupcake"
            signingConfig = signingConfigs.getByName("cupcake")
            buildConfigField("String", "Admin_email", "\"cupcakeproductions13@gmail.com\"")
            buildConfigField("String", "Password", "\"Admin123#\"")
//            for json name
//            "package_name": "com.streamefy.cupcake"
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
    implementation(libs.exoplayerCore)
//    implementation(libs.videothumbnail)
    implementation(libs.autoimageslider)
    implementation(libs.drawerlayout)
    implementation(libs.retrofit)
    implementation(libs.gson)
    implementation(libs.interceptor)
    implementation(libs.coroutines)
    implementation("commons-codec:commons-codec:1.17.1")
    implementation("org.apache.httpcomponents.core5:httpcore5:5.2.5")
    implementation("junit:junit:4.12")
    implementation ("com.github.appsfeature:otp-view:1.1")
    implementation ("com.github.mukeshsolanki.android-otpview-pinview:otpview:3.1.0")
    implementation ("com.squareup.picasso:picasso:2.8")
    implementation (platform("com.google.firebase:firebase-bom:33.4.0"))
    implementation ("com.google.firebase:firebase-crashlytics-ktx")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.hbb20:ccp:2.7.3")
    implementation("com.googlecode.libphonenumber:libphonenumber:8.12.40")

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}