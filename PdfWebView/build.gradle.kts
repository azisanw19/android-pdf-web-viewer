plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
}

android {
    namespace = "com.canwar.pdfwebview"
    compileSdk = 34

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")

    /* SDP android */
    implementation("com.intuit.sdp:sdp-android:1.1.0")
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                pom {
                    name.set("Pdf Web View")
                    description.set("Pdf Web View with pdf.js, turn.js,")
                    url.set("https://github.com/azisanw19/android-pdf-web-viewer")

                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("https://github.com/azisanw19/android-pdf-web-viewer/blob/main/LICENSE")
                        }
                    }
                    developers {
                        developer {
                            id.set("azisanw19")
                            name.set("Aziz Anwar")
                            email.set("azisanw19@gmail.com")
                        }
                    }
                }

                from(components.findByName("release"))

                groupId = "com.github.azisanw19"
                artifactId = "pdfwebview"
                version = "0.0.1-alpha02"

            }
        }
    }
}