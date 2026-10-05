package com.angelma.convention

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.BuildType
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

internal fun Project.configureBuildTypes(
    commonExtension: CommonExtension,
    extensionType: ExtensionType
) {
    commonExtension.run {
        buildFeatures.buildConfig = true
    }
    when (extensionType) {
        ExtensionType.APPLICATION -> {
            extensions.configure<ApplicationExtension> {
                buildTypes {
                    debug {
                        configureDebugType()
                    }
                    release {
                        configureReleaseType()
                        optimization.enable = true
                    }
                }
            }
        }
        ExtensionType.LIBRARY -> {
            extensions.configure<LibraryExtension> {
                buildTypes {
                    debug {
                        configureDebugType()
                    }
                    release {
                        configureReleaseType()
                    }
                }
            }
        }
    }
}

private fun BuildType.configureDebugType() {
    buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:8080/\"")
}

private fun BuildType.configureReleaseType() {
    buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:8080/\"")
}