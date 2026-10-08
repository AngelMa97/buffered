import com.android.build.api.dsl.LibraryExtension
import com.angelma.convention.ExtensionType
import com.angelma.convention.configureBuildTypes
import com.angelma.convention.configureKotlinAndroid
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.kotlin

class AndroidLibraryConventionPlugin: Plugin<Project> {
    override fun apply(project: Project) {
        project.run {
            pluginManager.run {
                apply("com.android.library")
            }
            extensions.configure<LibraryExtension> {
                configureKotlinAndroid(this)

                defaultConfig {
                    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                }

                configureBuildTypes(
                    commonExtension = this,
                    extensionType = ExtensionType.LIBRARY
                )

                dependencies {
                    "testImplementation"(kotlin("test"))
                }
            }
        }
    }
}