import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    @OptIn(ExperimentalWasmDsl::class)
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
        }
    }
}
