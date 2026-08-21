plugins {
    id("argus.kotlin.library")
}

dependencies {
    api(projects.domain)
    api(projects.feature.ingestion)
    api(projects.feature.enrichment)
    api(projects.feature.analysis)
    api(projects.feature.alert)
    implementation(libs.kotlinx.coroutines.core)
}
