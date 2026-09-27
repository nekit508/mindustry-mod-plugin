package com.github.nekit508.nmp.tasks;

import com.github.nekit508.nmp.extensions.NMPluginExtension;
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;

class GenerateLombokConfigTask extends DefaultTask {
    @Internal
    final NMPluginExtension ext

    @OutputFile
    final RegularFileProperty output

    @Input
    final MapProperty<String, String> properties

    @Inject
    GenerateLombokConfigTask(NMPluginExtension ext) {
        this.ext = ext
        group = "nmp"

        var factory = project.objects

        output = factory.fileProperty()
        properties = factory.mapProperty String, String

        configure {
            output.set project.file("lombok.config")
        }
    }

    void lombok(String key, String value) {
        properties[key] = value
    }

    void lombok(Map<String, String> props) {
        properties.putAll props
    }

    @TaskAction
    void generate() {
        output.finalizeValue()
        properties.finalizeValue()

        output.get().getAsFile().withWriter { BufferedWriter writer ->
            writer.writeLine("config.stopBubbling = true")
            properties.get().each { writer.writeLine("$it.key = $it.value") }
        }
    }
}
