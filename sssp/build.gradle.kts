import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKmpLibrary)
    alias(libs.plugins.vanniktech.mavenPublish)
}

group = "com.dayanruben"
version = libs.versions.sssp.get()

kotlin {
    jvm()

    android {
        namespace = "com.dayanruben.sssp"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    linuxX64()

    sourceSets {
        commonMain.dependencies {
            //put your multiplatform dependencies here
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
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
                name = "The Apache License, Version 2.0"
                url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
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
