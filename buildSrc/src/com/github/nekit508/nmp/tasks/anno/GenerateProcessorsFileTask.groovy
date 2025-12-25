package com.github.nekit508.nmp.tasks.anno

import com.github.nekit508.nmp.extensions.NMPluginAnnoExtension
import groovy.io.FileType
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.language.jvm.tasks.ProcessResources
import org.gradle.work.NormalizeLineEndings

import javax.inject.Inject

class GenerateProcessorsFileTask extends DefaultTask {
    @Internal
    NMPluginAnnoExtension ext

    @OutputDirectory
    final DirectoryProperty outputDirectory

    @Input
    final Property<String> triggerString

    @SkipWhenEmpty
    @IgnoreEmptyDirectories
    @NormalizeLineEndings
    @PathSensitive(PathSensitivity.RELATIVE)
    @InputFiles
    final ConfigurableFileCollection sources

    @Inject
    GenerateProcessorsFileTask(NMPluginAnnoExtension ext) {
        group = "nmpa"
        this.ext = ext

        var factory = project.getObjects()

        triggerString = factory.property String
        sources = factory.fileCollection()

        outputDirectory = factory.directoryProperty()

        configure {
            outputDirectory.set new File(temporaryDir, "")
            triggerString.set "// anno processor class"

            addSource project.sourceSets.main.java.srcDirs

            (project.tasks.processResources as ProcessResources).from(outputs)
        }
    }

    @TaskAction
    void generate() {
        String text = ""

        sources.each { dir ->
            if (dir.exists())
                dir.eachFileRecurse(FileType.FILES, { file ->
                    if (file.getText().find(triggerString.get())) {
                        var relativeFileName = dir.relativePath(file).replaceAll("[/\\\\]", ".")
                        text += relativeFileName.substring(0, relativeFileName.lastIndexOf('.')) + '\n'
                    }
                })
        }

        var dir = new File(outputDirectory.get().asFile,"META-INF/services")
        dir.mkdirs()
        new File(dir, "javax.annotation.processing.Processor").setText(text)
    }

    void addSource(Iterable<Object> files) {
        files.each { Object file ->
            addSource file
        }
    }

    void addSource(Object... files) {
        files.each { Object file ->
            addSource file
        }
    }

    void addSource(Object file) {
        sources.from file
    }
}
