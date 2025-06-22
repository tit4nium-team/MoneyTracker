import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.compose)
    alias(libs.plugins.kotlinCompose)
    alias(libs.plugins.kotlinxserialization)
    // id("com.google.gms.google-services") // Removido/Comentado - Verificar se é usado por outros serviços Firebase
}

kotlin {
    targetHierarchy.default()

    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }
    
    jvmToolchain(17)
    
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
            // export(libs.generativeai) // REMOVED
        }
    }

    sourceSets {
        val androidMain by getting {
            dependencies {
                implementation(compose.preview)
                implementation(libs.androidx.activity.compose)
                // Remover Firebase BoM e Vertex AI
                // implementation(platform(libs.firebase.bom)) // REMOVIDO
                // implementation(libs.firebase.vertexai)      // REMOVIDO

                // Adicionar SDK base do Google AI para Android
                implementation(libs.generativeai) // Para com.google.ai.client.generativeai
                implementation(libs.ktor.client.cio) // Ktor client engine for Android/JVM

                // Outras dependências Firebase que você já usa (MANTER SE NECESSÁRIO)
                // Se estas também não forem necessárias sem o Firebase VertexAI, podem ser removidas.
                // Por enquanto, vou assumir que podem ser necessárias para outros fins.
                implementation("com.google.firebase:firebase-analytics-ktx")
                implementation("androidx.work:work-runtime-ktx:2.9.0")

                // Dependências dev.gitlive:firebase-* são para KMP Firebase, podem permanecer se você as usa.
                // Elas não são para o Gemini SDK que estamos configurando.
            }
        }
        val commonMain by getting {
            dependencies {
                // Dependências dev.gitlive:firebase-* são para KMP Firebase.
                // Se você usa Auth/Firestore via KMP (dev.gitlive), mantenha-as.
                // Elas não conflitam diretamente com o SDK Gemini que será usado no androidMain.
                implementation("dev.gitlive:firebase-auth:1.11.1")
                implementation("dev.gitlive:firebase-firestore:1.11.1")
                implementation("dev.gitlive:firebase-common:1.11.1") // Adicionada para consistência se as outras gitlive são usadas

                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)
                implementation(libs.androidx.lifecycle.viewmodel)
                implementation(libs.androidx.lifecycle.runtimeCompose)
                implementation(projects.shared)
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")

                // Ktor dependencies for commonMain (core, content-negotiation, json-serialization)
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.serialization.kotlinx.json)
            }
        }
        val iosMain by getting {
            dependencies {
                // Ktor client engine for iOS
                implementation(libs.ktor.client.darwin)
            }
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
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

    // Load local.properties
    val localProperties = Properties()
    val localPropertiesFile = rootProject.file("local.properties") // Access file from root project

    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { input ->
            localProperties.load(input)
        }
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
            // Ensure API keys are handled securely for release builds, e.g., not hardcoded
            // buildConfigField("String", "GEMINI_API_KEY", "\"YOUR_RELEASE_GEMINI_API_KEY\"") // Example, use a secure way
        }
        getByName("debug") {
            buildConfigField("String", "FIREBASE_PROJECT_ID", "\"${localProperties.getProperty("FIREBASE_PROJECT_ID") ?: ""}\"")
            buildConfigField("String", "FIREBASE_APP_ID", "\"${localProperties.getProperty("FIREBASE_APP_ID") ?: ""}\"")
            buildConfigField("String", "FIREBASE_API_KEY", "\"${localProperties.getProperty("FIREBASE_API_KEY") ?: ""}\"")
            buildConfigField("String", "GEMINI_API_KEY", "\"${localProperties.getProperty("GEMINI_API_KEY") ?: ""}\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

// Removed duplicate dependency block, commonMain should handle generativeai
// dependencies {
//     debugImplementation(compose.uiTooling)
//     implementation("com.google.ai.client.generativeai:generativeai:0.1.1") // Already in commonMain
// }

