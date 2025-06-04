import org.gradle.kotlin.dsl.implementation

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21"
}

android {
    namespace = "dev.coffee.examapp"
    compileSdk = 35

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    defaultConfig {
        applicationId = "dev.coffee.examapp"
        minSdk = 34
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    // 基础依赖
    implementation ("androidx.core:core-ktx:1.16.0")
    implementation ("androidx.appcompat:appcompat:1.7.0")
    implementation ("com.google.android.material:material:1.12.0")

    // Compose依赖
    implementation ("androidx.compose.ui:ui:1.8.2")
    implementation ("androidx.compose.compiler:compiler:1.5.15")
    implementation ("androidx.compose.material:material:1.8.2")
    implementation ("androidx.compose.material:material-icons-extended:1.7.8")
    implementation ("androidx.compose.material3:material3:1.3.2")
    implementation ("androidx.compose.material3:material3-window-size-class:1.3.2")
    implementation ("androidx.compose.ui:ui-tooling-preview:1.8.2")
    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.9.0")
    implementation ("androidx.activity:activity-compose:1.10.1")

    // 导航组件
    implementation ("androidx.navigation:navigation-compose:2.9.0")

    // 网络请求
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.squareup.okhttp3:logging-interceptor:4.10.0")

    // 图片加载
    implementation ("io.coil-kt:coil-compose:2.4.0")

    // 分页
    implementation ("androidx.paging:paging-compose:3.3.6")
    implementation("androidx.compose.foundation:foundation-android:1.7.4")

    // 测试依赖
    testImplementation ("junit:junit:4.13.2")
    androidTestImplementation ("androidx.test.ext:junit:1.2.1")
    androidTestImplementation ("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation ("androidx.compose.ui:ui-test-junit4:1.8.2")
    debugImplementation ("androidx.compose.ui:ui-tooling:1.8.2")
    debugImplementation ("androidx.compose.ui:ui-test-manifest:1.8.2")
}