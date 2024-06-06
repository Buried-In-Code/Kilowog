plugins {
    application
}

dependencies {
    implementation(project(":comicvine"))
    implementation(project(":metron"))
    implementation(libs.bundles.xmlutil)
    implementation(libs.commons.compress)
    implementation(libs.hoplite.core)
    implementation(libs.junrar)
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
