package com.angelma.convention

import org.gradle.api.Project
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.project

fun DependencyHandlerScope.addUiLayerDependencies(project: Project) {
//    "implementation"(project(":core:presentation:ui"))
//    "implementation"(project(":core:presentation:designsystem"))

    // Un BOM solo fija versiones si entra como platform(); dentro de un bundle no aplica.
    "implementation"(platform(project.libs.findLibrary("koin.bom").get()))
    "implementation"(project.libs.findLibrary("koin.androidx.compose").get())
    "implementation"(project.libs.findBundle("compose").get())
    "debugImplementation"(project.libs.findBundle("compose.debug").get())
    "androidTestImplementation"(project.libs.findLibrary("androidx.compose.ui.test.junit4").get())
}