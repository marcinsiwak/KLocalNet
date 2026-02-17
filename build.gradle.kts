import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {

    alias(libs.plugins.android.kotlin.multiplatform.library) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    id("com.vanniktech.maven.publish") apply false
//
//    alias(libs.plugins.kotlinMultiplatform)
//    alias(libs.plugins.android.kotlin.multiplatform.library)
//    alias(libs.plugins.serialization)
//    id("io.github.ttypic.swiftklib") version "0.6.4"
//    id("com.vanniktech.maven.publish") version "0.36.0"
}
