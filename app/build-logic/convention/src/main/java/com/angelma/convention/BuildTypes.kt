package com.angelma.convention

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.BuildType
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import java.util.Properties

internal fun Project.configureBuildTypes(
    commonExtension: CommonExtension,
    extensionType: ExtensionType
) {
    commonExtension.run {
        buildFeatures.buildConfig = true
    }
    val props = Properties()
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { props.load(it) }
    val url = props.getProperty("BASE_URL", "\"http://10.0.2.2:8080\"")
    when (extensionType) {
        ExtensionType.APPLICATION -> {
            extensions.configure<ApplicationExtension> {
                buildTypes {
                    debug {
                        configureDebugType(url)
                    }
                    release {
                        configureReleaseType(url)
                        optimization.enable = true
                    }
                }
            }
        }

        ExtensionType.LIBRARY -> {
            extensions.configure<LibraryExtension> {
                buildTypes {
                    debug {
                        configureDebugType(url)
                    }
                    release {
                        configureReleaseType(url)
                    }
                }
            }
        }
    }
}

private fun BuildType.configureDebugType(url: String) {
    buildConfigField("String", "BASE_URL", url)
}

private fun BuildType.configureReleaseType(url: String) {
    buildConfigField("String", "BASE_URL", url)
}