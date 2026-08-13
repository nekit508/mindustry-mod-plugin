package com.github.nekit508.nmp.tasks.core

import com.github.nekit508.nmp.extensions.NMPluginCoreExtension
import com.github.nekit508.nmp.lib.RunJarTask
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input

import javax.inject.Inject

class RunServerTask extends RunJarTask {
    @Input
    final Property<Boolean> copyModInDataDir

    @Inject
    RunServerTask(NMPluginCoreExtension ext) {
        super(ext)
        group = "nmp"

        var factory = project.objects

        copyModInDataDir = factory.property Boolean

        configure {
            jar.set project.tasks.nmpFetchServer.outputFile

            workingDirectory.set ext.mindustryWorkingDirectory
            copyModInDataDir.set ext.mindustryCopyModInDataDir
        }

        ext.nmp.configuration {
            copyModInDataDir.finalizeValue()

            if (!workingDirectory.isPresent())
                workingDirectory.set("mindustry-server-dir.local")

            workingDirectory.finalizeValue()

            var wDir = project.file workingDirectory.get()

            if (copyModInDataDir.get()) {
                var file = new File(wDir, "config/mods")
                logger.lifecycle("[$name] Patching nmpCopyBuildRelease for copy mod in $file.")
                (project.tasks.nmpCopyBuildRelease.copyPaths as ListProperty<File>).add project.provider({ file })
            }
        }

        outputs.upToDateWhen { false }
    }
}