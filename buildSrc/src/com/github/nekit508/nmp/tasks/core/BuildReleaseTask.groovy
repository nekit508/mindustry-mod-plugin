package com.github.nekit508.nmp.tasks.core

import com.github.nekit508.nmp.extensions.NMPluginCoreExtension
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.gradle.jvm.tasks.Jar

import javax.inject.Inject

class BuildReleaseTask extends Jar {
    @Internal
    NMPluginCoreExtension ext

    @Inject
    BuildReleaseTask(NMPluginCoreExtension ext) {
        group = "nmp"
        this.ext = ext

        configure {
            dependsOn project.tasks.nmpDex
            dependsOn project.tasks.nmpBuild

            duplicatesStrategy = DuplicatesStrategy.EXCLUDE

            (archiveFile as RegularFileProperty).set project.layout.buildDirectory.file("libs/$project.group-$project.name-${project.version}.jar")

            if (project.tasks.nmpDex.buildAndroid.get())
                from project.zipTree(project.tasks.nmpDex.dexFile.get())

            from project.zipTree(project.tasks.nmpBuild.archiveFile.get())
        }
    }
}
