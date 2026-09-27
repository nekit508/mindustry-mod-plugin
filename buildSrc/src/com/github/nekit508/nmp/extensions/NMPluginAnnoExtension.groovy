package com.github.nekit508.nmp.extensions


import com.github.nekit508.nmp.extensions.packs.ProjectRoot
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyHandler
import com.github.nekit508.nmp.tasks.anno.*

import javax.inject.Inject

abstract class NMPluginAnnoExtension extends NMPluginExtension implements ProjectRoot {
    final NMPluginCoreExtension core

    @Inject
    NMPluginAnnoExtension(String name, Project project, NMPluginCoreExtension core) {
        super(name, project)
        this.core = core
    }

    @Override
    void apply() {
        super.apply()

        nmp.configuration {
            core.project.dependencies { DependencyHandler handler ->
                handler.add "compileOnly", project
                handler.add "annotationProcessor", project
            }
        }

        nmp.initialisation {
            project.tasks.create "nmpaGenerateProcessorsFile", GenerateProcessorsFileTask, this
        }
    }
}
