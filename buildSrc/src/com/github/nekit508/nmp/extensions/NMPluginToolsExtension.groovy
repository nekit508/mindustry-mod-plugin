package com.github.nekit508.nmp.extensions


import com.github.nekit508.nmp.extensions.packs.ProjectRoot
import com.github.nekit508.nmp.tasks.tools.RunToolsTask
import org.gradle.api.Project

import javax.inject.Inject

abstract class NMPluginToolsExtension extends NMPluginExtension implements ProjectRoot {
    final NMPluginCoreExtension core

    @Inject
    NMPluginToolsExtension(String name, Project project, NMPluginCoreExtension core) {
        super(name, project)
        this.core = core
    }

    @Override
    void apply() {
        super.apply()

        nmp.setting {
            sourceCompatibility.set core.sourceCompatibility
            targetCompatibility.set core.targetCompatibility

            useJabel.set core.useJabel
            jabelVersion.set core.jabelVersion
            jabelRepo.set core.jabelVersion
        }

        nmp.initialisation {
            project.tasks.create "nmptRunTools", RunToolsTask, this
        }
    }
}
