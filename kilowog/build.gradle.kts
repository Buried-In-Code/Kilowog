plugins {
    application
}

dependencies {
    implementation(project(":metron"))
    implementation(libs.commons.compress)
    implementation(libs.hoplite.core)
    implementation(libs.junrar)
    implementation(libs.xmlutil.core)
    implementation(libs.xmlutil.serialization)
}

application {
    mainClass = "github.comiccorps.kilowog.AppKt"
    applicationName = "Kilowog"
}

tasks {
    val run by existing(JavaExec::class)
    run.configure {
        standardInput = System.`in`
    }
}
