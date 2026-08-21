pluginManagement {
    includeBuild("build-logic")
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

rootProject.name = "argus"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(":domain")
include(":feature:ingestion")
include(":feature:enrichment")
include(":feature:analysis")
include(":feature:alert")
include(":app")
include(":test-fixtures")
