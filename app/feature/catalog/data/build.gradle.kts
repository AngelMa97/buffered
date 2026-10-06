plugins {
    alias(libs.plugins.buffered.client.android.library)
    alias(libs.plugins.kotlin.serialization)  // genera el código de @Serializable en los DTOs
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

    // Tests del repositorio con MockEngine: el cliente de prueba necesita las mismas piezas de Ktor
    // que el real (content negotiation + JSON), que :core:data tiene como implementation.
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.ktor.client.mock)
    testImplementation(libs.ktor.client.content.negotiation)
    testImplementation(libs.ktor.serialization.kotlinx.json)
}
