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
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.os.OperatingSystem
import org.gradle.process.JavaExecSpec

import javax.inject.Inject
import java.util.jar.JarFile

class RunMindustry extends DefaultTask {
    @Internal
    NMPluginCoreExtension ext

    @InputFile
    final Property<File> mindustryJar

    @Input
    final Property<File> dataDirectory

    @Input
    final Property<File> workingDirectory

    @Input
    final ListProperty<String> arguments

    @Inject
    RunMindustry(NMPluginCoreExtension ext) {
        group = "nmp"
        this.ext = ext

        var factory = project.objects

        mindustryJar = factory.property File
        dataDirectory = factory.property File
        workingDirectory = factory.property File
        arguments = factory.listProperty String

        configure {
            mindustryJar.set project.tasks.nmpFetchMindustry.outputFile
            workingDirectory.set project.file("mindustry-dir.local")
            dataDirectory.set workingDirectory
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

        workingDirectory.finalizeValue()
        var dir = workingDirectory.get()
        dir.mkdirs()

        dataDirectory.finalizeValue()
        dataDirectory.get().mkdirs()

        project.javaexec { JavaExecSpec spec ->
            spec.classpath(file)
            spec.workingDir(dir)

            var os = OperatingSystem.current();
            if (os.isWindows())
                spec.environment.put("APPDATA", dataDirectory.get())
            else if (os.isLinux())
                spec.environment.put("XDG_DATA_HOME", dataDirectory.get())

            spec.mainClass.set(mainClass)
        }
    }
}