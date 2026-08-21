plugins {
    id("argus.ktor.app")
}

application {
    mainClass.set("com.argus.ApplicationKt")
}

dependencies {
    implementation(projects.domain)
    implementation(projects.feature.ingestion)
    implementation(projects.feature.enrichment)
    implementation(projects.feature.analysis)
    implementation(projects.feature.alert)
    implementation(libs.bundles.exposed)
    implementation(libs.sqlite.jdbc)
    implementation(libs.bundles.ktor.client)

    testImplementation(projects.testFixtures)
}
