package com.github.nekit508.nmp.tasks.core

import com.github.nekit508.nmp.extensions.NMPluginCoreExtension
import com.github.nekit508.nmp.lib.RunJarTask
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.internal.os.OperatingSystem

import javax.inject.Inject

class RunMindustryTask extends RunJarTask {
    @Input
    @Optional
    final Property<String> dataDirectory

    @Input
    final Property<Boolean> copyModInDataDir

    @Inject
    RunMindustryTask(NMPluginCoreExtension ext) {
        super(ext)
        group = "nmp"

        var factory = project.objects

        dataDirectory = factory.property String
        copyModInDataDir = factory.property Boolean

        configure {
            jar.set project.tasks.nmpFetchMindustry.outputFile

            workingDirectory.set ext.mindustryWorkingDirectory
            dataDirectory.set ext.mindustryDataDirectory
            copyModInDataDir.set ext.mindustryCopyModInDataDir
        }

        ext.nmp.configuration {
            dataDirectory.finalizeValue()
            copyModInDataDir.finalizeValue()

            if (dataDirectory.isPresent()) {
                var dDir = project.file(dataDirectory.get())

                dDir.mkdirs()

                var os = OperatingSystem.current()
                if (os.isWindows())
                    env["APPDATA"] = dDir.absolutePath
                else if (os.isLinux())
                    env["XDG_DATA_HOME"] = dDir.absolutePath

                if (copyModInDataDir.get()) {
                    var file = new File(dDir, "Mindustry/mods")
                    logger.lifecycle("[$name] Patching nmpCopyBuildRelease for copy mod in $file.absolutePath.")
                    (project.tasks.nmpCopyBuildRelease.copyPaths as ListProperty<File>).add project.provider({ file })
                }
            }
        }

        outputs.upToDateWhen { false }
    }
}