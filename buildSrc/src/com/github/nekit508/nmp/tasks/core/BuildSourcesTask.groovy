package com.github.nekit508.nmp.tasks.core

import com.github.nekit508.nmp.extensions.NMPluginCoreExtension
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
            duplicatesStrategy = DuplicatesStrategy.EXCLUDE

            (archiveFile as RegularFileProperty).set project.layout.buildDirectory.file("libs/tmp/sources.jar")

            from project.sourceSets.main.allSource
        }
    }
}