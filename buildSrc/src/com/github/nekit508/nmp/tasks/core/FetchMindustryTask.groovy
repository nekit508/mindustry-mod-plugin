package com.github.nekit508.nmp.tasks.core

import com.github.nekit508.nmp.lib.Utils
import com.github.nekit508.nmp.extensions.NMPluginCoreExtension
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*

import javax.inject.Inject
import java.util.function.Consumer

class FetchMindustryTask extends DefaultTask {
    @Internal
    NMPluginCoreExtension ext

    @OutputDirectory
    final RegularFileProperty outputDir
    @OutputFile
    final Property<File> outputFile

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
        outputFile = factory.property File
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

    @TaskAction
    void fetch() {
        logger.lifecycle("Fetching mindustry ${mindustryVersion.get()} into ${outputFile.get().absolutePath}.")
        Utils.readFile "https://github.com/Anuken/Mindustry/releases/download/${mindustryVersion.get()}/Mindustry.jar", outputFile.get(), 4096, new Consumer<Long>() {
            final bs = 4096 * 1024
            long prev = 0

            @Override
            void accept(Long count) {
                if (count - prev > bs) {
                    logger.lifecycle("Downloaded ${(long) (count / 1024 / 1024)}mB.")
                    prev = (long) ((long) (count / bs)) * bs
                }
            }
        }
        logger.lifecycle("Fetched.")
    }
}
