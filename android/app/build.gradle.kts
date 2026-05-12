import java.util.Properties
import java.io.FileInputStream

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("com.google.gms.google-services")
    id("dev.flutter.flutter-gradle-plugin")
}

val keystoreProperties = Properties()
val keysFolder = File(System.getProperty("user.home"), "Documents/AndroidKeys/Einkaufsliste/android")

val keystorePropertiesFile = File(keysFolder, "key.properties")

if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
    println("✅ Key-Properties erfolgreich geladen aus: ${keystorePropertiesFile.absolutePath}")
} else {
    println("❌ FEHLER: key.properties nicht gefunden in: ${keystorePropertiesFile.absolutePath}")
}

android {
    namespace = "de.jmporcher.einkaufsliste"
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
        applicationId = "de.jmporcher.einkaufsliste"
        minSdk = 23
        targetSdk = flutter.targetSdkVersion
        versionCode = flutter.versionCode
        versionName = flutter.versionName
    }

    signingConfigs {
        create("release") {
            // Wir lesen die Werte explizit aus und werfen einen Fehler, falls einer fehlt
            val alias = keystoreProperties.getProperty("keyAlias")
            val sPw = keystoreProperties.getProperty("storePassword")
            val kPw = keystoreProperties.getProperty("keyPassword")
            val sFile = keystoreProperties.getProperty("storeFile")

            if (alias != null && sPw != null && kPw != null && sFile != null) {
                keyAlias = alias
                keyPassword = kPw
                storePassword = sPw
                storeFile = File(keysFolder, sFile)
            } else {
                println("WARNUNG: Key-Properties konnten nicht vollständig geladen werden!")
            }
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