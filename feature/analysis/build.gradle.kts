plugins {
    id("argus.kotlin.library")
}

dependencies {
    api(projects.domain)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.bundles.ktor.client)
    implementation(libs.koin.ktor)
    implementation(libs.slf4j.api)
}
