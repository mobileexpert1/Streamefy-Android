plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
//    alias(libs.plugins.crashlyticsFirebase)
    id("com.google.firebase.crashlytics")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.streamefy"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.streamefy"
        minSdk = 21
        targetSdk = 35
//      cupcake for play store
   //     versionCode = 26
   //     versionName = "1.0.26"
        // cupcake for amazon
//        versionCode = 16
//        versionName = "1.0.16"

//       streamefy for amazon
//        versionCode = 30
//        versionName = "1.0.30"
//        streameyf for play store
        versionCode = 28
        versionName = "1.0.28"
        vectorDrawables {
            useSupportLibrary = true
        }

        ndk {
            abiFilters.addAll(listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64"))
        }
    }
    flavorDimensions += "default"
    signingConfigs {
        create("streamefy") {
            storeFile = file("D:\\KEY_STORE_CREDETIALS\\Streamefy_info\\streamefy.jks")
            storePassword = "android"
            keyAlias = "android"
            keyPassword = "android"
        }

        create("cupcake") {
//            storeFile = file("D:\\work splace\\Streamefy\\cupcake_info\\cupcake.jks")
            storeFile = file("D:\\KEY_STORE_CREDETIALS\\Streamefy_info\\streamefy.jks")
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
//            buildConfigField("String", "Admin_email", "\"appsdev096@gmail.com\"")
//            buildConfigField("String", "Password", "\"Admin@123\"")
//            buildConfigField("String", "Admin_email", "\"ekamjot-kaur@cssoftsolutions.com\"")
//            buildConfigField("String", "Password", "\"Admin@123\"")
//            buildConfigField("String", "Admin_email", "\"gitikakhatri@cssoftsolutions.com\"")
//            buildConfigField("String", "Password", "\"Test@12345\"")
//            buildConfigField("String", "Admin_email", "\"bylafuwo@thetechnext.net\"")
//            buildConfigField("String", "Password", "\"Admin@123\"")
//            buildConfigField("String", "Admin_email", "\"kanishkbohra@gmail.com\"")
//            buildConfigField("String", "Password", "\"Thebest1!\"")
            buildConfigField("String", "Admin_email", "\"app-29@streamefy.com\"")
            buildConfigField("String", "Password", "\"StreamefyTVCRED-29\"")
            //DEV CREDS FOR NIKHIL ACCOUNT
        //    buildConfigField("String", "Admin_email", "\"APP-84@Streamefy.com\"")
        //    buildConfigField("String", "Password", "\"StreamefyTVCRED-84\"")
        //    buildConfigField("String", "Admin_email", "\"app-98@streamefy.com\"")
        //    buildConfigField("String", "Password", "\"StreamefyTVCRED-98\"")
        }
        create("cupcake") {
            applicationId = "com.tech.cupcake"
            versionNameSuffix = "-cupcake"
            signingConfig = signingConfigs.getByName("cupcake")
            buildConfigField("String", "Admin_email", "\"app-56@streamefy.com\"")
            buildConfigField("String", "Password", "\"StreamefyTVCRED-56\"")
//            buildConfigField("String", "Admin_email", "\"cupcakeproductions13@gmail.com\"")
//            buildConfigField("String", "Password", "\"Admin123#\"")
//            buildConfigField("String", "Admin_email", "\"cupcakeproductions13@gmail.com\"")
//            buildConfigField("String", "Password", "\"Admin123#\"")
//            buildConfigField("String", "Admin_email", "\"geetsehgal@mailinator.com\"")
//            buildConfigField("String", "Password", "\"Test@12345\"")
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
    implementation("com.google.android.play:app-update:2.1.0")
    implementation("com.google.android.play:app-update-ktx:2.1.0")
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}