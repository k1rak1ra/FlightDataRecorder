import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    id("maven-publish")
    alias(libs.plugins.kover)
    alias(libs.plugins.dokka)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.room)
    alias(libs.plugins.ksp)
}

group = "net.k1ra.flight_data_recorder"
version = System.getenv("releaseName") ?: "999999.999999.999999"


kotlin {
    android {
        namespace = "net.k1ra.flight_data_recorder"
        compileSdk {
            //noinspection WrongGradleMethod
            val (major, minor) = libs.versions.android.compileSdk.get().split(".").map { it.toInt() } + listOf(0)
            version = release(major) { minorApiLevel = minor }
        }
        minSdk = libs.versions.android.minSdk.get().toInt()

        androidResources.enable = true

        withHostTest {
            enableCoverage = true
        }
        withDeviceTest {
            enableCoverage = true
        }
    }

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

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

            implementation(libs.androidx.room3.runtime)

            implementation(libs.uuid)

            implementation(libs.korlibs.crypto)

            implementation(libs.oshi.core)

            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.ui)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }

        androidMain.dependencies {
            implementation(libs.androidx.room3.sqlite)

            implementation(libs.androidx.crypto)
        }

        iosMain.dependencies {
            implementation(libs.androidx.room3.sqlite)
        }

        jvmMain.dependencies {
            implementation(libs.androidx.room3.sqlite)
        }

        wasmJsMain.dependencies {
            implementation(libs.kotlinx.browser)

            implementation(libs.androidx.room3.sqlite.web)
            implementation(libs.androidx.sqlite.opfs)
        }
    }
}

dependencies {
    add("kspAndroid", libs.androidx.room3.compiler)
    add("kspIosSimulatorArm64", libs.androidx.room3.compiler)
    add("kspIosArm64", libs.androidx.room3.compiler)
    add("kspJvm", libs.androidx.room3.compiler)
    add("kspWasmJs", libs.androidx.room3.compiler)
}

room3 {
    schemaDirectory("$projectDir/schemas")
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
        from(dokkaGeneratePublicationHtml)
        dependsOn(dokkaGeneratePublicationHtml)
        archiveClassifier.set("javadoc")
    }
}