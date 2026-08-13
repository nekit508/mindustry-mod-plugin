package com.github.nekit508.nmp.lib

import com.github.nekit508.nmp.extensions.NMPluginCoreExtension
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.process.JavaExecSpec

import javax.inject.Inject
import java.util.jar.JarFile

class RunJarTask extends DefaultTask {
    @Internal
    NMPluginCoreExtension ext

    @InputFile
    final RegularFileProperty jar

    @Input
    @Optional
    final Property<String> workingDirectory

    @Input
    final ListProperty<String> arguments

    @Input
    final MapProperty<String, String> env

    @Inject
    RunJarTask(NMPluginCoreExtension ext) {
        this.ext = ext

        var factory = project.objects

        jar = factory.fileProperty()
        workingDirectory = factory.property String
        arguments = factory.listProperty String
        env = factory.mapProperty String.class, String.class

        configure {
            workingDirectory.set ext.mindustryWorkingDirectory
        }

        outputs.upToDateWhen { false }
    }

    @TaskAction
    void run() {
        ext.nmp.finalizeProperty jar, workingDirectory
        env.finalizeValue()
        var file = jar.get().asFile

        String mainClass
        if (file.exists()) {
            var jar = new JarFile(file)
            def manifest = jar.manifest
            mainClass = manifest.mainAttributes["Main-Class"] as String
            jar.close()
        } else
            throw new GradleException("jar file does not exists!")

        project.javaexec { JavaExecSpec spec ->
            spec.classpath(file)

            if (workingDirectory.isPresent()) {
                var wDir = project.file(workingDirectory.get())
                wDir.mkdirs()
                spec.setWorkingDir wDir
            }

            spec.environment.putAll env.get()

            spec.mainClass.set(mainClass)
        }
    }
}
