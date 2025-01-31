import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        moduleName = "FlightDataRecorderWebDashboard"
        browser {
            commonWebpackConfig {
                outputFileName = "FlightDataRecorderWebDashboard.js"

                export = false
            }
        }
        binaries.executable()
    }

    targets.all {
        compilations.all {
            compilerOptions.configure {
                freeCompilerArgs.add("-Xexpect-actual-classes")
            }
        }
    }
    
    sourceSets {
        
        commonMain.dependencies {
            implementation(project(":WebSharedModel"))

            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(compose.animation)

            implementation(libs.precompose.core)
            implementation(libs.precompose.viewmodel)
            implementation(libs.precompose.koin)

            implementation(libs.koin)

            implementation(libs.ktor.client.core.wasm)
            implementation(libs.ktor.client.wasm.js)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.client.serialization)
            implementation(libs.ktor.client.serialization.kotlinx)
            implementation(libs.ktor.client.content.negotiation)
        }
    }
}

compose.experimental {
    web.application {}
}