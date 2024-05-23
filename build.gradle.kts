import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlinx.serialization) apply false
    alias(libs.plugins.versions)
    alias(libs.plugins.ktlint)
}

allprojects {
    group = "github.buriedincode"
    version = "0.2.0"

    repositories {
        mavenCentral()
        mavenLocal()
    }

    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        version.set("1.2.1")
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.serialization")

    dependencies {
        implementation(rootProject.libs.kotlinx.datetime)
        implementation(rootProject.libs.kotlinx.serialization.json)
        implementation(rootProject.libs.log4j2.api.kotlin)
        runtimeOnly(rootProject.libs.log4j2.slf4j2.impl)
        runtimeOnly(rootProject.libs.sqlite.jdbc)
    }

    kotlin {
        jvmToolchain(17)
    }

    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(17)
        }
    }
}

fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return isStable.not()
}

tasks.withType<com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask> {
    gradleReleaseChannel = "current"
    resolutionStrategy {
        componentSelection {
            all {
                if (isNonStable(candidate.version) && !isNonStable(currentVersion)) {
                    reject("Release candidate")
                }
            }
        }
    }
}
