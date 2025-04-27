package nekit508.tools.tasks

import nekit508.tools.NMPToolsPlugin
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.JavaExec

import javax.inject.Inject

class RunToolsTask extends JavaExec {
    @Internal
    NMPToolsPlugin ext

    @Inject
    RunToolsTask(NMPToolsPlugin ext) {
        group = "nmpt"
        this.ext = ext

        mainClass.set "tools.Tools"
        classpath = project.sourceSets.main.runtimeClasspath

        workingDir = project.parent.projectDir
    }
}
