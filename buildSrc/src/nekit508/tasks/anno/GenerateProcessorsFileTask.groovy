package nekit508.tasks.anno

import groovy.io.FileType
import nekit508.extensions.NMPluginAnnoExtension
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.work.NormalizeLineEndings

import javax.inject.Inject

class GenerateProcessorsFileTask extends DefaultTask {
    @Internal
    NMPluginAnnoExtension ext

    @OutputFile
    final File outputFile

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

        outputFile = new File(temporaryDir, "/META-INF/services/javax.annotation.processing.Processor")

        configure {
            triggerString.set "// anno processor class"

            addSource project.sourceSets.main.java.srcDirs

            project.tasks.processResources.dependsOn this
        }
    }

    @TaskAction
    void generate() {
        List<String> files = []

        sources.each { dir ->
            dir.eachFileRecurse(FileType.FILES, { file ->
                if (file.getText().find(triggerString.get())) {
                    var relativeFileName = dir.relativePath(file).replaceAll("[/\\\\]", ".")
                    files += relativeFileName.substring(0, relativeFileName.lastIndexOf('.'))
                }
            })
        }

        String text = ""
        files.each {file -> text += file + '\n'}
        outputFile.setText(text)

        project.copy {
            from temporaryDir
            into project.tasks.processResources.destinationDir
        }
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
