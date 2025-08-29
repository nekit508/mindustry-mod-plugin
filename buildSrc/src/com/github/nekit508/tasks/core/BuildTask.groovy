package com.github.nekit508.tasks.core

import com.github.nekit508.extensions.NMPluginCoreExtension
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.Internal
import org.gradle.jvm.tasks.Jar

import javax.inject.Inject

class BuildTask extends Jar {
    @Internal
    NMPluginCoreExtension ext

    @Inject
    BuildTask(NMPluginCoreExtension ext) {
        group = "nmp"
        this.ext = ext

        configure {
            (archiveFile as RegularFileProperty).set project.layout.buildDirectory.file("libs/tmp/classes.jar")

            dependsOn project.tasks.classes

            setDuplicatesStrategy DuplicatesStrategy.EXCLUDE

            from {
                project.configurations.runtimeClasspath.collect {
                    it.isDirectory() ? it : project.zipTree(it)
                }
            }

            from project.sourceSets.main.output
        }
    }
}
