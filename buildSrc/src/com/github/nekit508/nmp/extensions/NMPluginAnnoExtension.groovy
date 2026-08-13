package com.github.nekit508.nmp.extensions

import com.github.nekit508.nmp.extensions.components.Compiled
import com.github.nekit508.nmp.extensions.components.NMPluginExtension
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyHandler
import com.github.nekit508.nmp.tasks.anno.*

import javax.inject.Inject

abstract class NMPluginAnnoExtension extends NMPluginExtension implements Compiled {
    final NMPluginCoreExtension core

    @Inject
    NMPluginAnnoExtension(String name, Project project, NMPluginCoreExtension core) {
        super(name, project)
        this.core = core
    }

    @Override
    void apply() {
        super.apply()

        _Sourced()
        _Compiled()

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
