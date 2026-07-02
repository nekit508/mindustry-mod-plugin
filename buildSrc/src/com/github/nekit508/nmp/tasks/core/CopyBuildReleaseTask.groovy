package com.github.nekit508.nmp.tasks.core

import com.github.nekit508.nmp.extensions.NMPluginCoreExtension
import org.gradle.api.DefaultTask
import org.gradle.api.file.CopySpec
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectories
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

class CopyBuildReleaseTask extends DefaultTask {
    @Internal
    NMPluginCoreExtension ext

    @OutputDirectories
    final ListProperty<File> copyPaths

    @InputFile
    final RegularFileProperty input

    @Inject
    CopyBuildReleaseTask(NMPluginCoreExtension ext) {
        group = "nmp"
        this.ext = ext

        ObjectFactory objectFactory = getProject().getObjects()

        input = objectFactory.fileProperty()
        copyPaths = objectFactory.listProperty File

        configure {
            dependsOn project.tasks.nmpBuildRelease

            input.set project.tasks.nmpBuildRelease.archiveFile

            List<File> paths = ext.nmp.nmpSettings?.copy?.collect { String path -> project.file path } ?: []
            copyPaths.addAll paths
        }
    }

    @TaskAction
    void copy() {
        copyPaths.finalizeValue()
        copyPaths.get().each { p ->
            project.copy { CopySpec spec ->
                spec.from input
                spec.into p
            }
        }
    }
}
