
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.sqlDelight)
    id("maven-publish")
    alias(libs.plugins.kover)
    alias(libs.plugins.dokka)
}

group = "net.k1ra.flight_data_recorder"
version = System.getenv("releaseName") ?: "999999.999999.999999"

sqldelight {
    databases {
        create("FlightDataRecorderDatabase") {
            packageName.set("net.k1ra.flight_data_recorder.database")
        }
    }
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "11"
            }
        }
    }

    targets.all {
        compilations.all {
            compilerOptions.configure {
                freeCompilerArgs.add("-Xexpect-actual-classes")
            }
        }
    }
    
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    
    jvm()
    
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlin.datetime)
            implementation(libs.kotlin.coroutines)
            implementation(libs.network)
            implementation(libs.bundles.sqldelight.common)
            implementation(libs.uuid)
            implementation(libs.korlibs.crypto)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }

        androidMain.dependencies {
            implementation(libs.sqldelight.driver.android)
            implementation(libs.sqldelight.driver.jdbc)
            implementation(libs.androidx.crypto)
        }

        iosMain.dependencies {
            implementation(libs.sqldelight.driver.ios)
        }

        jvmMain.dependencies {
            implementation(libs.sqldelight.driver.jdbc)
        }
    }
}

android {
    namespace = "net.k1ra.flight_data_recorder"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}

publishing {
    repositories {
        maven {
            name = "k1ra-nexus"
            url = uri("https://k1ra.net/nexus/repository/public/")

            credentials(PasswordCredentials::class) {
                username = System.getenv("NEXUS_USERNAME") ?: "anonymous"
                password = System.getenv("NEXUS_PASSWORD") ?: ""
            }
        }
    }
}

tasks{
    register<Jar>("dokkaJar") {
        from(dokkaHtml)
        dependsOn(dokkaHtml)
        archiveClassifier.set("javadoc")
    }
}