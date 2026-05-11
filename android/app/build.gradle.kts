import java.util.Properties
import java.io.FileInputStream

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("com.google.gms.google-services")
    id("dev.flutter.flutter-gradle-plugin")
}

val keystoreProperties = Properties()
val keysFolder = File(System.getProperty("user.home"), "Documents/AndroidKeys/Einkaufsliste")
// BEHOBEN 1: Diese Zeile hat gefehlt, damit Gradle weiß, wonach es sucht
val keystorePropertiesFile = File(keysFolder, "key.properties")

if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

android {
    namespace = "com.example.einkaufsliste"
    compileSdk = flutter.compileSdkVersion
    ndkVersion = "27.0.12077973"

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }

    defaultConfig {
        applicationId = "com.example.einkaufsliste"
        minSdk = 23
        targetSdk = flutter.targetSdkVersion
        versionCode = flutter.versionCode
        versionName = flutter.versionName
    }

    signingConfigs {
        create("release") {
            keyAlias = keystoreProperties["keyAlias"] as String?
            keyPassword = keystoreProperties["keyPassword"] as String?
            storePassword = keystoreProperties["storePassword"] as String?

            // BEHOBEN 2: Holt die Keystore-Datei jetzt direkt aus deinem Dokumente-Ordner
            val storeFileName = keystoreProperties["storeFile"] as String?
            storeFile = storeFileName?.let { File(keysFolder, it) }
        }
    }

    buildTypes {
        getByName("release") {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false
            isShrinkResources = false
        }
    }
}

dependencies {
    implementation("androidx.core:core-google-shortcuts:1.1.0")
}

flutter {
    source = "../.."
}

// DER RETTER: Dieser Block erzwingt, dass die fehlerhafte Deep-Link-Aufgabe übersprungen wird
tasks.whenTaskAdded {
    if (name.contains("extractDeepLinks")) {
        enabled = false
    }
}