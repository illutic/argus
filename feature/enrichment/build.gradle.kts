plugins {
    id("argus.kotlin.library")
}

dependencies {
    api(projects.domain)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.bundles.ktor.client)
    implementation(libs.koin.ktor)
    implementation(libs.slf4j.api)

    testImplementation(projects.testFixtures)
    testImplementation(libs.koin.test)
    testImplementation(libs.koin.test.junit5)
}
