package com.github.nekit508.nmp.tasks.core

import com.github.nekit508.nmp.extensions.NMPluginCoreExtension
import org.gradle.api.DefaultTask
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

class RunMindustry extends DefaultTask {
    @Internal
    NMPluginCoreExtension ext

    @InputFile
    final Property<File> mindustryFile

    @Input
    final ListProperty<String> arguments

    @Input
    final Property<String> javaHome

    @Inject
    RunMindustry(NMPluginCoreExtension ext) {
        group = "nmp"
        this.ext = ext

        var factory = project.objects

        mindustryFile = factory.property File
        arguments = factory.listProperty String
        javaHome = factory.property String

        configure {
            mindustryFile.set project.tasks.nmpFetchMindustry.outputFile
            javaHome.set(System.getProperty "java.home")
        }

        outputs.upToDateWhen { false }
    }

    @TaskAction
    void run() {
        var executable = "${javaHome.get()}/bin/java.exe"
        ("$executable -jar ${mindustryFile.get().absolutePath} ${String.join(" ", arguments.get())}").execute().waitForProcessOutput System.out, System.err
    }
}
