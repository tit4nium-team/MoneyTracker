plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.compose)
    alias(libs.plugins.kotlinCompose)
    alias(libs.plugins.kotlinxserialization)
    id("com.google.gms.google-services")
}

kotlin {
    targetHierarchy.default()

    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "11"
            }
        }
    }
    
    jvmToolchain(11)
    
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        val androidMain by getting {
            dependencies {
                implementation(compose.preview)
                implementation(libs.androidx.activity.compose)
                implementation(platform("com.google.firebase:firebase-bom:32.7.4"))
                implementation("com.google.firebase:firebase-auth-ktx")
                implementation("com.google.firebase:firebase-firestore-ktx")
                implementation("com.google.firebase:firebase-analytics-ktx")
                implementation("androidx.work:work-runtime-ktx:2.9.0")
                implementation("androidx.datastore:datastore-preferences:1.0.0")
            }
        }
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)
                implementation(libs.androidx.lifecycle.viewmodel)
                implementation(libs.androidx.lifecycle.runtimeCompose)
                implementation(projects.shared)
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0") // Or the latest version
                // Add any common dependencies here
                implementation(libs.generativeai)
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
                implementation("com.google.ai.client.generativeai:generativeai:0.1.1")
            }
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
        }
        val androidUnitTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
                implementation("junit:junit:4.13.2")
                implementation("org.robolectric:robolectric:4.10.3")
                implementation("org.mockito:mockito-core:5.4.0")
                implementation("org.mockito:mockito-junit-jupiter:5.4.0")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
                implementation("androidx.datastore:datastore-preferences-core:1.0.0")
            }
        }
        val iosTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
            }
        }
    }
}

android {
    namespace = "com.example.moneytracker"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "br.com.tit4nium.moneytracker"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        getByName("debug") {
            // Add Firebase configuration for debug
            buildConfigField("String", "FIREBASE_PROJECT_ID", "\"moneytracker-5508d\"") // Replace with your project ID
            buildConfigField("String", "FIREBASE_APP_ID", "\"1:534125893092:android:c6a74d7202b54b34fa23f1\"") // Replace with your app ID
            buildConfigField("String", "FIREBASE_API_KEY", "\"AIzaSyBL1j9F1_ctko5TgFMh8bAbGiTe2t6B_H0\"") // Replace with your API key
            buildConfigField("String", "GEMINI_API_KEY", "\"${System.getenv("AIzaSyCekWzR9iJ5ND4soW-HHFcF69o7jezFCOs") ?: ""}\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    sourceSets {
        getByName("test") {
            java.srcDirs("src/test/java", "src/test/kotlin")
        }
        getByName("androidTest") {
            java.srcDirs("src/androidTest/java", "src/androidTest/kotlin")
        }
        create("androidUnitTest") {
            java.srcDirs("src/androidUnitTest/java", "src/androidUnitTest/kotlin")
            // resources.srcDirs("src/androidUnitTest/res") // Uncomment if resources are needed
        }
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}

