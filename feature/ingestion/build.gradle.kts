plugins {
    id("argus.kotlin.library")
}

dependencies {
    api(projects.domain)
    implementation(libs.bundles.ktor.server)
    implementation(libs.koin.ktor)
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(projects.testFixtures)
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.bundles.ktor.client)
    testImplementation(libs.koin.test)
    testImplementation(libs.koin.test.junit5)
}
