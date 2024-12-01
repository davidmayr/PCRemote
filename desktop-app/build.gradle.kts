import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

group = "com.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

dependencies {
    // Note, if you develop a library, you should use compose.desktop.common.
    // compose.desktop.currentOs should be used in launcher-sourceSet
    // (in a separate module for demo project and in testMain).
    // With compose.desktop.common you will also lose @Preview functionality
    implementation(compose.desktop.currentOs)
    implementation("io.javalin:javalin-bundle:6.3.0")
    implementation("io.nayuki:qrcodegen:1.8.0")
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("org.apache.logging.log4j:log4j-core:2.23.1")
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:2.23.1")

}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            windows {
                shortcut = true
                menu = true
                iconFile = file("src/main/resources/icon.ico")
            }

            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "S Pen PC Remote"
            packageVersion = "1.0.0"
        }
    }
}
