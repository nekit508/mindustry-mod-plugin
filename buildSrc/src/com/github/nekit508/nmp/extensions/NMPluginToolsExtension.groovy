package com.github.nekit508.nmp.extensions

import com.github.nekit508.nmp.extensions.components.Compiled
import com.github.nekit508.nmp.extensions.components.NMPluginExtension
import org.gradle.api.Project
import com.github.nekit508.nmp.tasks.tools.*

import javax.inject.Inject

abstract class NMPluginToolsExtension extends NMPluginExtension implements Compiled {
    final NMPluginCoreExtension core

    @Inject
    NMPluginToolsExtension(String name, Project project, NMPluginCoreExtension core) {
        super(name, project)
        this.core = core
    }

    @Override
    void apply() {
        super.apply()

        _Sourced()
        _Compiled()

        nmp.setting {
            sourceCompatibility.set core.sourceCompatibility
            targetCompatibility.set core.targetCompatibility

            useJabel.set core.useJabel
            jabelVersion.set core.jabelVersion
            jabelRepo.set core.jabelVersion
        }

        nmp.initialisation {
            project.tasks.register "nmptRunTools", RunToolsTask, this
        }
    }
}
