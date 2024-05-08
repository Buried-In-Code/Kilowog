import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
    kotlin("jvm") version "1.9.24"
    kotlin("plugin.serialization") version "1.9.22"
    application
    id("com.github.ben-manes.versions") version "0.51.0"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.0"
}

group = "github.comiccorps"
version = "0.2.1"

println("Kilowog v$version")
println("Kotlin v${KotlinVersion.CURRENT}")
println("Java v${System.getProperty("java.version")}")
println("Arch: ${System.getProperty("os.arch")}")

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation("com.github.junrar", "junrar", "7.5.5")
    implementation("com.sksamuel.hoplite", "hoplite-core", "2.7.5")
    implementation("org.jetbrains.kotlinx", "kotlinx-datetime", "0.5.0")
    implementation("org.jetbrains.kotlinx", "kotlinx-serialization-json", "1.6.3")
    runtimeOnly("org.xerial", "sqlite-jdbc", "3.45.1.0")

    // XmlUtil
    val xmlutilVersion = "0.86.3"
    implementation("io.github.pdvrieze.xmlutil", "core-jvm", xmlutilVersion)
    implementation("io.github.pdvrieze.xmlutil", "serialization-jvm", xmlutilVersion)

    // Log4j2
    implementation("org.apache.logging.log4j", "log4j-api-kotlin", "1.4.0")
    runtimeOnly("org.apache.logging.log4j", "log4j-slf4j2-impl", "2.23.0")
}

kotlin {
    jvmToolchain(17)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

application {
    mainClass.set("github.comiccorps.kilowog.AppKt")
    applicationName = "Kilowog"
}

configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
    version.set("1.2.1")
}

tasks {
    val run by existing(JavaExec::class)
    run.configure {
        standardInput = System.`in`
    }
}

fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return isStable.not()
}

tasks.withType<DependencyUpdatesTask> {
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
