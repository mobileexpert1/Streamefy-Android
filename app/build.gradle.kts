plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
//  alias(libs.plugins.crashlyticsFirebase)
//    id("com.google.firebase.crashlytics")
//    id("com.google.gms.google-services")
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
//        versionCode = 28
//        versionName = "1.0.28"


        // viyafilms for amazon
//        versionCode = 2
//        versionName = "1.0.2"

       // viyafilms for play store
    //    versionCode = 2
    //    versionName = "1.0.2"

        //  new flavours
//        // Royalreelz for amazon
//        versionCode = 1
//        versionName = "1.0.0"
        // Royalreelz for playstore
    //    versionCode = 4
    //    versionName = "1.0.3"


//        // Shutterdown for amazon `
//        versionCode = 1
//        versionName = "1.0.0"
//        // Shutterdown for playstore
//        versionCode = 4
//        versionName = "1.0.3"
//
        // Epic for amazon
//        versionCode = 1
//        versionName = "1.0.0"
        // Epic for playstore
//        versionCode = 7
//        versionName = "1.0.6"

        // Prakash for amazon
//        versionCode = 1
//        versionName = "1.0.0"
//        // Prakash for playstore
//        versionCode = 2
//        versionName = "1.0.1"


        // jodiclickers for amazon
    //    versionCode = 2
    //    versionName = "1.0.2"
        // jodiclickers for playstore
   //     versionCode = 4
    //    versionName = "1.0.3"


        // Ivory for amazon
     //   versionCode = 1
     //   versionName = "1.0.0"
        // Ivory for playstore
        //       versionCode = 1
        //       versionName = "1.0.0"


        // Cine love for amazon
        //   versionCode = 1
        //   versionName = "1.0.0"
        // Cine love for playstore
        //       versionCode = 1
        //       versionName = "1.0.0"


        // Vidrow for amazon
           versionCode = 1
           versionName = "1.0.0"
        // Vidrow for playstore
        //       versionCode = 1
         //      versionName = "1.0.0"

        // Magicmotion for amazon
     //       versionCode = 2
      //      versionName = "1.0.1"
        // Magicmotion for playstore
        //      versionCode = 1
         //     versionName = "1.0.0"

        // Robin Saini for amazon
         //      versionCode = 2
         //     versionName = "1.0.1"
        // Robin Saini for playstore
       //      versionCode = 2
       //      versionName = "1.0.1"

        // dream teller for amazon
       //       versionCode = 2
       //       versionName = "1.0.1"
        // dream teller for playstore
       //        versionCode = 1
       //        versionName = "1.0.0"

        // camlition for amazon
        //       versionCode = 1
        //       versionName = "1.0.0"
        // camlition for playstore
//        versionCode = 1
//        versionName = "1.0.0"

        // reflection for amazon
            //   versionCode = 2
             //  versionName = "1.0.1"
        // reflection for playstore
      //  versionCode = 1
      //  versionName = "1.0.0"

        // shutterbug for amazon
        //       versionCode = 2
         //      versionName = "1.0.1"
        // shutterbug for playstore
     //   versionCode = 3
     //   versionName = "1.0.0"

        // The movieing moments for amazon
         //      versionCode = 2
         //      versionName = "1.0.1"
        // The movieing moments for playstore
//        versionCode = 1
//        versionName = "1.0.0"

        // fhotofocus for amazon
        //        versionCode = 2
        //        versionName = "1.0.1"
        // fhotofocus for playstore
//        versionCode = 1
//        versionName = "1.0.0"


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

        create("viyafilms") {
            storeFile = file("D:\\KEY_STORE_CREDETIALS\\Streamefy_info\\streamefy.jks")
            storePassword = "android"
            keyAlias = "android"
            keyPassword = "android"
        }

        create("royalreelz") {
            storeFile = file("D:\\KEY_STORE_CREDETIALS\\Streamefy_info\\streamefy.jks")
            storePassword = "android"
            keyAlias = "android"
            keyPassword = "android"
        }
        create("shutterdown") {
            storeFile = file("D:\\KEY_STORE_CREDETIALS\\Streamefy_info\\streamefy.jks")
            storePassword = "android"
            keyAlias = "android"
            keyPassword = "android"
        }
        create("epic") {
            storeFile = file("D:\\KEY_STORE_CREDETIALS\\Streamefy_info\\streamefy.jks")
            storePassword = "android"
            keyAlias = "android"
            keyPassword = "android"
        }
        create("prakash") {
            storeFile = file("D:\\KEY_STORE_CREDETIALS\\Streamefy_info\\streamefy.jks")
            storePassword = "android"
            keyAlias = "android"
            keyPassword = "android"
        }
        create("jodiclickers") {
            storeFile = file("D:\\KEY_STORE_CREDETIALS\\Streamefy_info\\streamefy.jks")
            storePassword = "android"
            keyAlias = "android"
            keyPassword = "android"
        }
        create("vidrow") {
            storeFile = file("D:\\KEY_STORE_CREDETIALS\\Streamefy_info\\streamefy.jks")
            storePassword = "android"
            keyAlias = "android"
            keyPassword = "android"
        }
        create("ivory") {
            storeFile = file("D:\\KEY_STORE_CREDETIALS\\Streamefy_info\\streamefy.jks")
            storePassword = "android"
            keyAlias = "android"
            keyPassword = "android"
        }
        create("cinelove") {
            storeFile = file("D:\\KEY_STORE_CREDETIALS\\Streamefy_info\\streamefy.jks")
            storePassword = "android"
            keyAlias = "android"
            keyPassword = "android"
        }
        create("magicmotion") {
            storeFile = file("D:\\KEY_STORE_CREDETIALS\\Streamefy_info\\streamefy.jks")
            storePassword = "android"
            keyAlias = "android"
            keyPassword = "android"
        }
        create("robinsaini") {
            storeFile = file("D:\\KEY_STORE_CREDETIALS\\Streamefy_info\\streamefy.jks")
            storePassword = "android"
            keyAlias = "android"
            keyPassword = "android"
        }
        create("dreamteller") {
            storeFile = file("D:\\KEY_STORE_CREDETIALS\\Streamefy_info\\streamefy.jks")
            storePassword = "android"
            keyAlias = "android"
            keyPassword = "android"
        }
        create("camlition") {
            storeFile = file("D:\\KEY_STORE_CREDETIALS\\Streamefy_info\\streamefy.jks")
            storePassword = "android"
            keyAlias = "android"
            keyPassword = "android"
        }
        create("reflection") {
            storeFile = file("D:\\KEY_STORE_CREDETIALS\\Streamefy_info\\streamefy.jks")
            storePassword = "android"
            keyAlias = "android"
            keyPassword = "android"
        }
        create("moving_moments") {
            storeFile = file("D:\\KEY_STORE_CREDETIALS\\Streamefy_info\\streamefy.jks")
            storePassword = "android"
            keyAlias = "android"
            keyPassword = "android"
        }
        create("shutterbug") {
            storeFile = file("D:\\KEY_STORE_CREDETIALS\\Streamefy_info\\streamefy.jks")
            storePassword = "android"
            keyAlias = "android"
            keyPassword = "android"
        }
        create("fhotofocus") {
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
         //   buildConfigField("String", "Admin_email", "\"APP-84@Streamefy.com\"")
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
        create("viyafilms") {
            applicationId = "com.tech.viyafilms"
            versionNameSuffix = "-viyafilms"
            signingConfig = signingConfigs.getByName("viyafilms")
            buildConfigField("String", "Admin_email", "\"APP-182@streamefy.com\"")
            buildConfigField("String", "Password", "\"StreamefyTVCRED-182\"")
        }
        create("royalreelz") {
            applicationId = "com.tech.royalreelz"
            versionNameSuffix = "-royalreelz"
            signingConfig = signingConfigs.getByName("royalreelz")
            buildConfigField("String", "Admin_email", "\"APP-256@streamefy.com\"")
            buildConfigField("String", "Password", "\"StreamefyTVCRED-256\"")
        }
        create("shutterdown") {
            applicationId = "com.tech.shutterdown"
            versionNameSuffix = "-shutterdown"
            signingConfig = signingConfigs.getByName("shutterdown")
            buildConfigField("String", "Admin_email", "\"APP-230@Streamefy.com\"")
            buildConfigField("String", "Password", "\"StreamefyTVCRED-230\"")
        }
        create("epic") {
            applicationId = "com.tech.epic"
            versionNameSuffix = "-epic"
            signingConfig = signingConfigs.getByName("epic")
            buildConfigField("String", "Admin_email", "\"APP-200@Streamefy.com\"")
            buildConfigField("String", "Password", "\"StreamefyTVCRED-200\"")
        }
        create("prakash") {
            applicationId = "com.tech.prakash"
            versionNameSuffix = "-prakash"
            signingConfig = signingConfigs.getByName("prakash")
            buildConfigField("String", "Admin_email", "\"APP-234@Streamefy.com\"")
            buildConfigField("String", "Password", "\"StreamefyTVCRED-234\"")
        }
        create("jodiclickers") {
            applicationId = "com.tech.jodiclickers"
            versionNameSuffix = "-jodiclickers"
            signingConfig = signingConfigs.getByName("jodiclickers")
            buildConfigField("String", "Admin_email", "\"APP-274@Streamefy.com\"")
            buildConfigField("String", "Password", "\"StreamefyTVCRED-274\"")
        }
        create("vidrow") {
            applicationId = "com.tech.vidrow"
            versionNameSuffix = "-vidrow"
            signingConfig = signingConfigs.getByName("vidrow")
            buildConfigField("String", "Admin_email", "\"APP-294@Streamefy.com\"")
            buildConfigField("String", "Password", "\"StreamefyTVCRED-294\"")
        }
        create("ivory") {
            applicationId = "com.tech.ivory"
            versionNameSuffix = "-ivory"
            signingConfig = signingConfigs.getByName("ivory")
            buildConfigField("String", "Admin_email", "\"APP-214@Streamefy.com\"")
            buildConfigField("String", "Password", "\"StreamefyTVCRED-214\"")
        }
        create("cinelove") {
            applicationId = "com.tech.cinelove"
            versionNameSuffix = "-cinelove"
            signingConfig = signingConfigs.getByName("cinelove")
            buildConfigField("String", "Admin_email", "\"APP-232@Streamefy.com\"")
            buildConfigField("String", "Password", "\"StreamefyTVCRED-232\"")
        }
        create("magicmotion") {
            applicationId = "com.tech.magicmotion"
            versionNameSuffix = "-magicmotion"
            signingConfig = signingConfigs.getByName("magicmotion")
            buildConfigField("String", "Admin_email", "\"APP-280@Streamefy.com\"")
            buildConfigField("String", "Password", "\"StreamefyTVCRED-280\"")
        }
//        create("dreamteller") {
//            applicationId = "com.tech.dreamteller"
//            versionNameSuffix = "-dreamteller"
//            signingConfig = signingConfigs.getByName("dreamteller")
//            buildConfigField("String", "Admin_email", "\"APP-278@Streamefy.com\"")
//            buildConfigField("String", "Password", "\"StreamefyTVCRED-278\"")
//        }
        create("robinsaini") {
            applicationId = "com.tech.robinsainiphotography"
            versionNameSuffix = "-robinsaini"
            signingConfig = signingConfigs.getByName("robinsaini")
            buildConfigField("String", "Admin_email", "\"APP-474@Streamefy.com\"")
            buildConfigField("String", "Password", "\"StreamefyTVCRED-474\"")
        }
        create("dreamteller") {
            applicationId = "com.tech.dreamtellerstudio"
            versionNameSuffix = "-dreamteller"
            signingConfig = signingConfigs.getByName("dreamteller")
            buildConfigField("String", "Admin_email", "\"APP-278@Streamefy.com\"")
            buildConfigField("String", "Password", "\"StreamefyTVCRED-278\"")
        }
        create("camlition") {
            applicationId = "com.tech.camlition"
            versionNameSuffix = "-camlition"
            signingConfig = signingConfigs.getByName("camlition")
            buildConfigField("String", "Admin_email", "\"APP-522@Streamefy.com\"")
            buildConfigField("String", "Password", "\"StreamefyTVCRED-522\"")
        }
        create("reflection") {
            applicationId = "com.tech.reflection"
            versionNameSuffix = "-reflection"
            signingConfig = signingConfigs.getByName("reflection")
            buildConfigField("String", "Admin_email", "\"APP-470@Streamefy.com\"")
            buildConfigField("String", "Password", "\"StreamefyTVCRED-470\"")
        }
        create("moving_moments") {
            applicationId = "com.tech.moving_moments"
            versionNameSuffix = "-moving_moments"
            signingConfig = signingConfigs.getByName("moving_moments")
            buildConfigField("String", "Admin_email", "\"APP-524@Streamefy.com\"")
            buildConfigField("String", "Password", "\"StreamefyTVCRED-524\"")
        }
        create("shutterbug") {
            applicationId = "com.tech.shutterbug"
            versionNameSuffix = "-shutterbug"
            signingConfig = signingConfigs.getByName("shutterbug")
            buildConfigField("String", "Admin_email", "\"APP-532@Streamefy.com\"")
            buildConfigField("String", "Password", "\"StreamefyTVCRED-532\"")
        }
        create("fhotofocus") {
            applicationId = "com.tech.fhotofocus"
            versionNameSuffix = "-fhotofocus"
            signingConfig = signingConfigs.getByName("fhotofocus")
            buildConfigField("String", "Admin_email", "\"APP-586@Streamefy.com\"")
            buildConfigField("String", "Password", "\"StreamefyTVCRED-586\"")
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
//    implementation (platform("com.google.firebase:firebase-bom:33.4.0"))
//    implementation ("com.google.firebase:firebase-crashlytics-ktx")
//    implementation("com.google.firebase:firebase-analytics")
    implementation("com.hbb20:ccp:2.7.3")
    implementation("com.googlecode.libphonenumber:libphonenumber:8.12.40")
    implementation("com.google.android.play:app-update:2.1.0")
    implementation("com.google.android.play:app-update-ktx:2.1.0")
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}