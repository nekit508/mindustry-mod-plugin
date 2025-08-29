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
        
        configure {
            dependsOn project.tasks.nmpDex
            dependsOn project.tasks.nmpBuild

            (archiveFile as RegularFileProperty).set project.layout.buildDirectory.file("libs/tmp/library.jar")

            from {
                project.tasks.nmpDex.buildAndroid.get() ? [project.zipTree(project.tasks.nmpBuild.archiveFile.get()), project.zipTree(project.tasks.nmpDex.dexFile.get())] :
                        [project.zipTree(project.tasks.nmpBuild.archiveFile.get())]
            }

            project.tasks.jar.from this
            project.tasks.jar.dependsOn this
        }
    }
}