package com.github.nekit508.tasks.core

import com.github.nekit508.extensions.NMPluginCoreExtension
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.Internal
import org.gradle.jvm.tasks.Jar

import javax.inject.Inject

class BuildSourcesTask extends Jar {
    @Internal
    NMPluginCoreExtension ext

    @Inject
    BuildSourcesTask(NMPluginCoreExtension ext) {
        group = "nmp"
        this.ext = ext

        configure {
            (archiveFile as RegularFileProperty).set project.layout.buildDirectory.file("libs/tmp/sources.jar")

            from project.sourceSets.main.allSource

            logger.lifecycle( project.tasks.sourcesJar as String)
            project.tasks.sourcesJar.dependsOn this
            project.tasks.sourcesJar.from this
        }
    }
}