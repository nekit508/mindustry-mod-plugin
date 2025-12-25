package com.github.nekit508.nmp.tasks.core

import com.github.nekit508.nmp.lib.Utils
import com.github.nekit508.nmp.extensions.NMPluginCoreExtension
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.internal.hash.Hashing

import javax.inject.Inject
import java.security.MessageDigest
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

        outputs.upToDateWhen { false }
    }

    @TaskAction
    void fetch() {
        var mindustryVersion = mindustryVersion.get()
        var jar = outputFile.get()

        logger.lifecycle("Fetching release info.")
        var tag = Utils.readJson("https://api.github.com/repos/Anuken/Mindustry/releases/tags/${mindustryVersion}") as Map<String, ?>
        if (tag.containsKey("status") && tag["status"] == 404)
            throw new GradleException("Release ${mindustryVersion} does not exists.")

        var release_info = tag["assets"].find { it["name"] == "Mindustry.jar" }
        if (release_info == null)
             throw new GradleException("Mindustry.jar wasn't founded in release ${mindustryVersion}.")

        if (jar.exists()) {
            logger.lifecycle("Computing Mindustry.jar digest.")
            var local_digest = Hashing.sha256().hashStream(jar.newInputStream()).toString()
            var remote_digest = release_info["digest"] as String
            remote_digest = remote_digest.substring(remote_digest.indexOf(':')+1)

            logger.lifecycle("Comparing remote and local digests.")
            if (remote_digest == local_digest) {
                logger.lifecycle("Identical - abort fetching.")
                state.setDidWork false
                return
            } else
                logger.lifecycle("Different - fetching mindustry.")
        }

        logger.lifecycle("Fetching mindustry release ${mindustryVersion} into ${jar.absolutePath}.")
        Utils.readFile release_info["browser_download_url"] as String, jar, 4096, new Consumer<Long>() {
            long prev_time = System.currentTimeMillis()
            long prev_count
            final bs = 4096 * 1024
            long prev = 0

            @Override
            void accept(Long count) {
                if (count - prev > bs) {
                    long time = System.currentTimeMillis()

                    logger.lifecycle("Downloaded ${(long) (count / 1024 / 1024)} mB. (avg ${((count - prev_count) / 1024D) / ((System.currentTimeMillis() - prev_time) / 1000D)} kBs/sec)")
                    prev = (long) ((long) (count / bs)) * bs

                    prev_time = time
                    prev_count = count
                }
            }
        }
        logger.lifecycle("Fetched.")
    }
}
