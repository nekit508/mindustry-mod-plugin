package com.github.nekit508.tasks.core

import com.github.nekit508.extensions.NMPluginCoreExtension
import com.github.nekit508.tasks.FileFetchTask
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory

import javax.inject.Inject

class FetchMindustryTask extends FileFetchTask {
    @Internal
    NMPluginCoreExtension ext

    @OutputDirectory
    final RegularFileProperty outputDir

    @Input
    final Property<String> mindustryVersion
    @Input
    final Property<String> fileName
    @Input
    final Property<String> extension

    @Inject
    FetchMindustryTask(NMPluginCoreExtension ext) {
        group = "nmp"
        this.ext = ext

        var factory = project.getObjects()

        mindustryVersion = factory.property String
        outputDir = factory.fileProperty()
        fileName = factory.property String
        extension = factory.property String

        configure {
            mindustryVersion.set ext.mindustryVersion
            outputDir.set project.layout.buildDirectory.file(ext.nmp().local?.mindustry?.downloadDir ?: "mindustry")
            fileName.set "Mindustry"
            extension.set "jar"
            outputFile.set project.provider {
                new File(outputDir.get().asFile, "${fileName.get()}-${mindustryVersion.get()}.${extension.get()}")
            }
        }
    }

    @Override
    void fetch() {
        logger.lifecycle("Fetching mindustry ${mindustryVersion.get()} into ${outputFile.get().absolutePath}.")
        super.fetch()
        logger.lifecycle("Fetched.")
    }

    @Override
    BufferedInputStream resolveInput() {
        new URI("https://github.com/Anuken/Mindustry/releases/download/${mindustryVersion.get()}/Mindustry.jar").toURL().newInputStream()
    }

    @Override
    BufferedOutputStream resolveOutput() {
        var dir
        if (outputDir.isPresent() && !(dir = outputDir.get().asFile).exists())
            dir.mkdirs()
        return super.resolveOutput()
    }
}
