plugins {
    alias(libs.plugins.buffered.client.android.library)
}

android {
    namespace = "com.angelma.core.data"
}

dependencies {
    implementation(libs.bundles.ktor)

    implementation(projects.core.domain)
}