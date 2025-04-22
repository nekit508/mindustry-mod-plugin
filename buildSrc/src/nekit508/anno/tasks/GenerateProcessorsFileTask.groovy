package nekit508.anno.tasks

import groovy.io.FileType
import nekit508.anno.NMPAnnoPlugin
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.work.NormalizeLineEndings

import javax.inject.Inject

class GenerateProcessorsFileTask extends DefaultTask {
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
    GenerateProcessorsFileTask(NMPAnnoPlugin ext) {
        group = "nmpa"

        var factory = project.getObjects()

        triggerString = factory.property String
        sources = factory.fileCollection()

        outputFile = new File(temporaryDir, "/META-INF/services/javax.annotation.processing.Processor")

        triggerString.set "// anno processor class"

        addSource project.sourceSets.main.java.srcDirs

        project.tasks.processResources.dependsOn this
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
