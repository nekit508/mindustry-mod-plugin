package com.github.nekit508.tasks.core

import com.github.nekit508.extensions.NMPluginCoreExtension
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.Internal
import org.gradle.jvm.tasks.Jar

import javax.inject.Inject

class BuildLibraryTask extends Jar {
    @Internal
    NMPluginCoreExtension ext

    @Inject
    BuildLibraryTask(NMPluginCoreExtension ext) {
        group = "nmp"
        this.ext = ext

        // TODO this task now does nothing
        configure {
            //dependsOn project.tasks.nmpBuild

            (archiveFile as RegularFileProperty).set project.layout.buildDirectory.file("libs/tmp/library.jar")

            //from project.zipTree(project.tasks.nmpBuild.archiveFile.get())
        }
    }
}