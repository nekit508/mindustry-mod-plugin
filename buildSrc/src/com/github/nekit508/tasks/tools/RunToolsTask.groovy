package com.github.nekit508.tasks.tools

import com.github.nekit508.extensions.NMPluginToolsExtension
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.JavaExec

import javax.inject.Inject

class RunToolsTask extends JavaExec {
    @Internal
    NMPluginToolsExtension ext

    @Inject
    RunToolsTask(NMPluginToolsExtension ext) {
        group = "nmpt"
        this.ext = ext

        configure {
            classpath = project.sourceSets.main.runtimeClasspath
            workingDir = project.parent.projectDir
        }
    }
}
