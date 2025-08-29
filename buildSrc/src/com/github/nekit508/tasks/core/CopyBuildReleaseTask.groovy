package com.github.nekit508.tasks.core

import com.github.nekit508.extensions.NMPluginCoreExtension
import org.gradle.api.DefaultTask
import org.gradle.api.file.CopySpec
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectories
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.OutputFiles
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.enterprise.test.FileProperty

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
        copyPaths = objectFactory.listProperty(File.class)

        configure {
            dependsOn project.tasks.nmpBuildRelease

            input.set project.tasks.nmpBuildRelease.archiveFile
            copyPaths.addAll ext.nmp.local?.copy?.collect {String path -> new File(path)} ?: []
        }
    }

    @TaskAction
    void copy() {
        copyPaths.get().each { p ->
            project.copy { CopySpec spec ->
                spec.from input
                spec.into p
            }
        }
    }
}
