plugins {
    alias(libs.plugins.buffered.client.jvm.library)
}

dependencies {
    implementation(projects.core.domain)
}
