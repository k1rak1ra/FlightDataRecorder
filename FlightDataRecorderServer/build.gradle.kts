plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    application
}

group = "net.k1ra.flight_data_recorder_server"
version = "1.0.0"
application {
    mainClass.set("net.k1ra.flight_data_recorder_server.ApplicationKt")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=${extra["development"] ?: "false"}")
}

dependencies {
    implementation(project(":FlightDataRecorder"))
    implementation(project(":WebSharedModel"))
    implementation(libs.logback)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.serialization)
    
    implementation(libs.postgres)
    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.kotlin.datetime)

    implementation(libs.argon2)

    testImplementation(libs.ktor.server.tests)
    testImplementation(libs.kotlin.test.junit)
}

tasks.register("buildWebDashboardDev") {
    group = "build"
    description = "Builds the FlightDataRecorderDemoPackage and transfers files to site"

    val commands = arrayListOf(
        "./gradlew FlightDataRecorderWebDashboard:wasmJsBrowserDevelopmentWebpack",
        "rm -rf FlightDataRecorderServer/src/main/resources/site",
        "mv FlightDataRecorderWebDashboard/build/dist/wasmJs/developmentExecutable FlightDataRecorderServer/src/main/resources/site"
    )

    doLast {
        for (command in commands) {
            val process = ProcessBuilder()
                .command(command.split(" "))
                .directory(rootProject.projectDir)
                .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                .redirectError(ProcessBuilder.Redirect.INHERIT)
                .start()
            process.waitFor()
        }
    }
}

tasks.register("buildWebDashboardDist") {
    group = "build"
    description = "Builds the FlightDataRecorderDemoPackage and transfers files to site"

    val commands = arrayListOf(
        "./gradlew FlightDataRecorderWebDashboard:wasmJsBrowserDistribution",
        "rm -rf FlightDataRecorderServer/src/main/resources/site",
        "mv FlightDataRecorderWebDashboard/build/dist/wasmJs/developmentExecutable FlightDataRecorderServer/src/main/resources/site"
    )

    doLast {
        for (command in commands) {
            val process = ProcessBuilder()
                .command(command.split(" "))
                .directory(rootProject.projectDir)
                .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                .redirectError(ProcessBuilder.Redirect.INHERIT)
                .start()
            process.waitFor()
        }
    }
}