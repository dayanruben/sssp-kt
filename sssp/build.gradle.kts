import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.vanniktech.mavenPublish)
}

group = "com.dayanruben"
version = "0.1.0"

kotlin {
    jvm()
    androidTarget {
        publishLibraryVariants("release")
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    linuxX64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                //put your multiplatform dependencies here
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
    }
}

android {
    namespace = "com.dayanruben.sssp"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

mavenPublishing {
    publishToMavenCentral()

    signAllPublications()

    coordinates(group.toString(), "sssp", version.toString())

    pom {
        name = "SSSP"
        description = "A KMP library to find shortest path in a graph (SSSP)."
        inceptionYear = "2025"
        url = "https://github.com/dayanruben/sssp-kt/"
        licenses {
            license {
                name = "BSD-3-Clause"
                url = "https://opensource.org/license/bsd-3-clause"
                distribution = "repo"
            }
        }
        developers {
            developer {
                id = "dayanruben"
                name = "Dayan Ruben"
                url = "https://github.com/dayanruben"
            }
        }
        scm {
            url = "https://github.com/dayanruben/sssp-kt"
            connection = "scm:git:git://github.com/dayanruben/sssp-kt.git"
            developerConnection = "scm:git:ssh://git@github.com/dayanruben/sssp-kt.git"
        }
    }
}
