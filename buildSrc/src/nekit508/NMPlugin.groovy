package nekit508

import nekit508.extensions.NMPluginCoreExtension
import nekit508.extensions.NMPluginExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class NMPlugin implements Plugin<Project> {
    List<NMPluginExtension> extensions = new LinkedList<>()

    NMPluginCoreExtension core = new NMPluginCoreExtension(this)

    Project project

    @Override
    void apply(Project target) {
        project = target
        project.extensions.nmp = this

        extensions*.apply()
    }

    void afterConfigure() {
        extensions*.afterConfigure()
    }

    String mindustryDependency(String version, String module = "core") {
        return dependency("com.github.Anuken.Mindustry", module, version)
    }

    String arcDependency(String version, String module = "arc-core") {
        return dependency("com.github.Anuken.Arc", module, version)
    }

    @SuppressWarnings("GrMethodMayBeStatic")
    String dependency(String dep, String module, String version) {
        return "$dep:$module:$version"
    }
}
