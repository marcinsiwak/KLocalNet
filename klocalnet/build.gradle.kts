import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.serialization)
    id("io.github.ttypic.swiftklib") version "0.6.4"
    alias(libs.plugins.vanniktech.mavenPublish)
    id("signing")
}

group = "io.github.marcinsiwak"
version = "1.0.0"

kotlin {
    jvm()
    androidLibrary {
        namespace = "io.github.marcinsiwak.klocalnet"
        compileSdk = 36
        minSdk = 24

        withJava() // enable java compilation support
        withHostTestBuilder {}.configure {}
        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }

        compilations.configureEach {
            compilerOptions.configure {
                jvmTarget.set(
                    JvmTarget.JVM_17
                )
            }
        }
    }
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "lib_connection"
        }
    }
    jvmToolchain(17)

    sourceSets {
        sourceSets {
            androidMain.dependencies {
                implementation(libs.ktor.android)
                implementation(libs.ktor.cio)
                implementation(libs.ktor.server.core)
                implementation(libs.ktor.server.cio)
                implementation(libs.ktor.server.websocket)
                implementation(libs.ktor.server.contentNegotiation)
                implementation(libs.androidx.annotation.jvm)
            }
            commonMain.dependencies {
                implementation(libs.koin.core)
                implementation(libs.ktor.core)
                implementation(libs.ktor.contentNegation)
                implementation(libs.ktor.websockets)
                implementation(libs.ktor.logger)
                implementation(libs.ktor.serialization)
                implementation(libs.kotlinx.serialization)
            }
            iosMain.dependencies {
                implementation(libs.ktor.ios)
            }
        }
    }

    swiftPMDependencies {
        localPackage(
            path = projectDir.resolve("TelegraphWrapper"),
            products = listOf(product("TelegraphObjCWrapper", platforms = setOf(iOS())))
        )
        localPackage(
            path = projectDir.resolve("NetworkWrapper"),
            products = listOf(product("NetworkWrapper", platforms = setOf(iOS())))
        )
    }
}

buildscript {
    dependencies.constraints {
        "classpath"("org.jetbrains.kotlin:kotlin-gradle-plugin:2.2.21-titan-211!!")
    }
}

mavenPublishing {
    publishToMavenCentral()

    signAllPublications()

    coordinates(group.toString(), "klocalnet", version.toString())

    pom {
        name = "KLocalNet library"
        description = "Local network communication library for Kotlin Multiplatform projects"
        inceptionYear = "2026"
        url = "https://github.com/marcinsiwak/KLocalNet/"
        licenses {
            license {
                name = "The Apache License, Version 2.0"
                url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                distribution = "https://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        }
        developers {
            developer {
                id = "marcinsiwak"
                name = "Kotlin Developer"
                url = "https://github.com/marcinsiwak/"
            }
        }
        scm {
            url = "https://github.com/marcinsiwak/KLocalNet/"
            connection = "scm:git:git://github.com/marcinsiwak/KLocalNet.git"
            developerConnection = "scm:git:ssh://git@github.com/marcinsiwak/KLocalNet.git"
        }
    }
}
