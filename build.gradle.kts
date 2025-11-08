plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    // ✅ 패키지명은 항상 소문자
    namespace = "com.example.kioskapp"
    compileSdk = 34

    defaultConfig {
        // ✅ 실제 앱 ID (Play 스토어에 표시되는 고유 식별자)
        applicationId = "com.example.kioskapp"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        vectorDrawables.useSupportLibrary = true
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

    // ⚙️ Kotlin과 Java 타깃을 동일하게 맞춤
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.fragment:fragment-ktx:1.8.2")
}

// ✅ Gradle JVM Toolchain (Java 17 고정)
kotlin {
    jvmToolchain(17)
}
