import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.sqlDelight)
    id("maven-publish")
    alias(libs.plugins.kover)
    alias(libs.plugins.dokka)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

group = "net.k1ra.flight_data_recorder"
version = System.getenv("releaseName") ?: "999999.999999.999999"

sqldelight {
    databases {
        create("FlightDataRecorderDatabase") {
            packageName.set("net.k1ra.flight_data_recorder.database")
            generateAsync.set(true)
        }
    }
    linkSqlite = true
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "11"
            }
        }
    }

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
    
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        binaries.executable()
        browser {
            val rootDirPath = project.rootDir.path
            val projectDirPath = project.projectDir.path
            commonWebpackConfig {
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static = (static ?: mutableListOf()).apply {
                        // Serve sources to debug inside browser
                        add(rootDirPath)
                        add(projectDirPath)
                    }
                }
            }
        }
    }
    
    jvm()
    
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlin.datetime)
            implementation(libs.kotlin.coroutines)

            implementation(libs.k1ra.network)
            implementation(libs.k1ra.sharedpref)

            implementation(libs.bundles.sqldelight.common)

            implementation(libs.uuid)

            implementation(libs.korlibs.crypto)

            implementation(libs.oshi.core)

            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.ui)
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

        wasmJsMain.dependencies {
            implementation(libs.kotlinx.browser)
            implementation(libs.sqldelight.driver.web)
            implementation(npm("@cashapp/sqldelight-sqljs-worker", libs.versions.sqlJsWorker.get()))
            implementation(npm("sql.js", libs.versions.sqlJs.get()))
            implementation(devNpm("copy-webpack-plugin", libs.versions.webPackPlugin.get()))
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