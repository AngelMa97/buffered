plugins {
    alias(libs.plugins.buffered.client.android.library)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.angelma.feature.catalog.data"
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)

    implementation(projects.core.data)
    implementation(projects.feature.catalog.domain)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.ktor.client.mock)
    testImplementation(libs.ktor.client.content.negotiation)
    testImplementation(libs.ktor.serialization.kotlinx.json)
}
