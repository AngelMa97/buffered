package com.angelma.convention

import org.gradle.api.Project
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.project

fun DependencyHandlerScope.addUiLayerDependencies(project: Project) {
    "implementation"(project(":core:presentation"))
    "implementation"(project(":core:design-system"))

    "implementation"(platform(project.libs.findLibrary("koin.bom").get()))
    "implementation"(project.libs.findLibrary("koin.androidx.compose").get())
    "implementation"(project.libs.findLibrary("androidx.navigation.compose").get())
    "implementation"(project.libs.findBundle("compose").get())
    "debugImplementation"(project.libs.findBundle("compose.debug").get())
    "androidTestImplementation"(project.libs.findLibrary("androidx.compose.ui.test.junit4").get())
}