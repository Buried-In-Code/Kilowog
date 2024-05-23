plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "Kilowog"

include("kilowog")
include("comicvine")
include("metron")
