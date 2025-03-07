import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    wasmJs {
        moduleName = "FlightDataRecorderSharedModel"
        browser {
            commonWebpackConfig {
            }
        }
        binaries.executable()
    }

    jvm()

    sourceSets {
        commonMain.dependencies {
            api(libs.kotlin.serialization)
            implementation(libs.kotlin.datetime)
        }
    }
}
