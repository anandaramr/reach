import java.io.FileInputStream
import java.util.Properties

val isReleaseBuild = gradle.startParameter.taskNames.any { it.contains("Release", ignoreCase = true) }
val keystorePropsFile = rootProject.file("keystore.properties")
val keystoreProps = Properties()

if (isReleaseBuild) {
    if (keystorePropsFile.exists()) {
        keystoreProps.load(FileInputStream(keystorePropsFile))
    } else {
        keystoreProps["storeFile"] = System.getenv("KEYSTORE_PATH")
        keystoreProps["storePassword"] = System.getenv("KEYSTORE_PASSWORD")
        keystoreProps["keyAlias"] = System.getenv("KEY_ALIAS")
        keystoreProps["keyPassword"] = System.getenv("KEY_PASSWORD")
    }
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

    id("com.google.devtools.ksp") version "2.2.20-2.0.3"
    id("com.google.dagger.hilt.android") version "2.57.2"
    id("com.google.protobuf") version "0.9.5"
}

android {
    namespace = "com.project.reach"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.project.reach"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (isReleaseBuild) {
            create("release") {
                storeFile = file(keystoreProps["storeFile"] as String)
                storePassword = keystoreProps["storePassword"] as String
                keyAlias = keystoreProps["keyAlias"] as String
                keyPassword = keystoreProps["keyPassword"] as String
            }
        }
    }

    splits {
        abi {
            isEnable = true
            reset()
            include("armeabi-v7a", "arm64-v8a")
            isUniversalApk = true
        }
        density {
            isEnable = false
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            if (isReleaseBuild) signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs += "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    //noinspection UseTomlInstead

    implementation("androidx.navigation:navigation-compose:2.9.6")

    implementation("androidx.room:room-runtime:2.8.3")
    implementation("androidx.room:room-ktx:2.8.3")
    ksp("androidx.room:room-compiler:2.8.3")

    implementation("com.google.dagger:hilt-android:2.57.2")
    implementation("androidx.hilt:hilt-navigation-compose:1.3.0")
    ksp("com.google.dagger:hilt-android-compiler:2.57.2")
    implementation("androidx.compose.material:material-icons-extended:1.7.8")

    implementation("androidx.paging:paging-compose:3.3.6")
    implementation("androidx.room:room-paging:2.8.3")

    implementation("com.google.protobuf:protobuf-javalite:4.33.0")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.25.3"
    }
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                create("java") {
                    option("lite")
                }
            }
        }
    }
}

// resolves task conflict due to proto
tasks.configureEach {
    if (name == "kspDebugKotlin") {
        dependsOn("generateDebugProto")
    }
    if (name == "kspReleaseKotlin") {
        dependsOn("generateReleaseProto")
    }
    if (name == "kspDebugAndroidTestKotlin") {
        dependsOn("generateDebugAndroidTestProto")
    }
    if (name == "kspDebugUnitTestKotlin") {
        dependsOn("generateDebugUnitTestProto")
    }
}