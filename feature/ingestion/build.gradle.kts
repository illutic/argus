plugins {
    id("argus.kotlin.library")
}

dependencies {
    api(projects.domain)
    implementation(libs.bundles.ktor.server)
    implementation(libs.koin.ktor)
}
