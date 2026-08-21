val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

plugins {
    id("argus.kotlin.library")
    application
}

dependencies {
    add("implementation", libs.findBundle("ktor-server").get())
    add("implementation", libs.findLibrary("logback-classic").get())
    add("implementation", libs.findLibrary("logstash-logback-encoder").get())
    add("implementation", libs.findLibrary("micrometer-registry-prometheus").get())
    add("testImplementation", libs.findLibrary("ktor-server-test-host").get())
    add("testImplementation", libs.findLibrary("koin-test").get())
    add("testImplementation", libs.findLibrary("koin-test-junit5").get())
}
