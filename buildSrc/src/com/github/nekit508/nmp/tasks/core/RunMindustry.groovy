package com.github.nekit508.nmp.tasks.core

import com.github.nekit508.nmp.extensions.NMPluginCoreExtension
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.Directory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.os.OperatingSystem
import org.gradle.process.JavaExecSpec

import javax.inject.Inject
import java.util.jar.JarFile

class RunMindustry extends DefaultTask {
    @Internal
    NMPluginCoreExtension ext

    @Input
    final Property<File> mindustryJar

    @Input
    @Optional
    final Property<String> dataDirectory

    @Input
    @Optional
    final Property<String> workingDirectory

    @Input
    final ListProperty<String> arguments

    @Input
    final Property<Boolean> copyModInDataDir

    @Inject
    RunMindustry(NMPluginCoreExtension ext) {
        group = "nmp"
        this.ext = ext

        var factory = project.objects

        mindustryJar = factory.property File
        dataDirectory = factory.property String
        workingDirectory = factory.property String
        arguments = factory.listProperty String
        copyModInDataDir = factory.property Boolean

        configure {
            mindustryJar.set project.tasks.nmpFetchMindustry.outputFile

            workingDirectory.set ext.mindustryWorkingDirectory
            dataDirectory.set ext.mindustryDataDirectory
            copyModInDataDir.set ext.mindustryCopyModInDataDir
        }

        ext.nmp().configuration {
            dataDirectory.finalizeValue()
            copyModInDataDir.finalizeValue()

            logger.lifecycle("Patching nmpCopyBuildRelease for copy mod in data dir.")

            if (copyModInDataDir.get() && dataDirectory.isPresent())
                (project.tasks.nmpCopyBuildRelease.copyPaths as ListProperty<File>).add new File(project.file(dataDirectory.get()), "Mindustry/mods")
        }

        outputs.upToDateWhen { false }
    }

    @TaskAction
    void run() {
        mindustryJar.finalizeValue()
        var file = mindustryJar.get()

        String mainClass
        if (file.exists()) {
            var jar = new JarFile(file)
            def manifest = jar.manifest
            mainClass = manifest.mainAttributes["Main-Class"] as String
            jar.close()
        } else
            throw new GradleException("mindustryJar does not exists!")

        boolean overrideWDir = false, overrideDDir = false
        File wDir, dDir

        workingDirectory.finalizeValue()
        dataDirectory.finalizeValue()

        if (workingDirectory.isPresent()) {
            overrideWDir = true
            wDir = project.file(workingDirectory.get())
            wDir.mkdirs()
        }

        if (dataDirectory.isPresent()) {
            overrideDDir = true
            dDir = project.file(dataDirectory.get())
            dDir.mkdirs()
        }

        logger.lifecycle("Running mindustry in ${overrideWDir ? wDir.absolutePath : "default path"} with data from ${overrideDDir ? dDir.absolutePath : "default path"}")

        project.javaexec { JavaExecSpec spec ->
            spec.classpath(file)

            if (overrideWDir)
                spec.workingDir(wDir)

            if (overrideDDir) {
                var os = OperatingSystem.current()
                if (os.isWindows())
                    spec.environment.put("APPDATA", dDir.absolutePath)
                else if (os.isLinux())
                    spec.environment.put("XDG_DATA_HOME", dDir.absolutePath)
            }

            spec.mainClass.set(mainClass)
        }
    }
}