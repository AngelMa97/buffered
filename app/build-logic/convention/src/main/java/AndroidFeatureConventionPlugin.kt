import com.angelma.convention.addUiLayerDependencies
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class AndroidFeatureConventionPlugin: Plugin<Project> {
    override fun apply(project: Project) {
        project.run {
            pluginManager.run {
                apply("bufferedclient.android.library.compose")
            }

            dependencies {
                addUiLayerDependencies(project)
            }
        }
    }
}