package com.github.nekit508.nmp.tasks

import com.github.nekit508.nmp.NMPlugin
import com.github.nekit508.nmp.lib.Utils
import com.github.nekit508.nmp.extensions.NMPluginCoreExtension
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.internal.hash.Hashing

import javax.inject.Inject
import java.util.function.Consumer

class FetchTask extends DefaultTask {
    @Internal
    NMPlugin nmp

    @OutputDirectory
    final DirectoryProperty outputDir
    @OutputFile
    final RegularFileProperty outputFile

    @Input
    final Property<String> repo
    @Input
    final Property<String> version
    @Input
    final Property<String> fileName
    @Input
    final Property<String> extension
    @Input
    final Property<String> repoFileName
    @Input
    final Property<String> outputFileName

    @Inject
    FetchTask() {
        var factory = project.getObjects()

        version = factory.property String
        outputDir = factory.directoryProperty()
        outputFile = factory.fileProperty()
        fileName = factory.property String
        extension = factory.property String
        outputFileName = factory.property String
        repoFileName = factory.property String
        repo = factory.property String

        configure {
            repoFileName.set project.provider {
                nmp.finalizeProperty fileName, extension
                "${fileName.get()}.${extension.get()}"
            }
            outputFileName.set project.provider {
                nmp.finalizeProperty fileName, version, extension
                "${fileName.get()}-${version.get()}.${extension.get()}"
            }
            outputFile.set project.provider {
                nmp.finalizeProperty outputFileName, outputDir
                (outputDir.get() as Directory).file(outputFileName.get() as String)
            }
        }
    }

    @TaskAction
    void fetch() {
        nmp.finalizeProperty version, repoFileName, repo, outputFile

        var targetVersion = version.get()
        var targetRepo = repo.get()
        var targetRepoFileName = repoFileName.get()
        var targetOutputFile = outputFile.get()

        if (nmp.isOnline()) {
            logger.lifecycle("Fetching release info.")
            var tag = Utils.readJson("https://api.github.com/repos/$targetRepo/releases/tags/$targetVersion") as Map<String, ?>
            if (tag.containsKey("status") && tag["status"] == 404)
                throw new GradleException("Release $targetVersion does not exists.")

            var releaseInfo = tag["assets"].find { it["name"] == "$targetRepoFileName" } as Map<String, ?>
            if (releaseInfo == null)
                throw new GradleException("$targetRepoFileName wasn't founded in release ${targetVersion}.")

            if (targetOutputFile.exists()) {
                logger.lifecycle("Computing digest.")
                var localDigest = Hashing.sha256().hashStream(targetOutputFile.newInputStream()).toString()
                if (releaseInfo.containsKey("digest")) {
                    var remoteDigest = releaseInfo["digest"] as String
                    remoteDigest = remoteDigest.substring(remoteDigest.indexOf(':') + 1)

                    logger.lifecycle("Comparing remote and local digests.")
                    if (remoteDigest == localDigest) {
                        logger.lifecycle("Identical - abort fetching.")
                        state.setDidWork false
                        return
                    } else
                        logger.lifecycle("Different - fetching.")
                } else
                    logger.lifecycle("File exists, but remote release has no digest - abort fetching. (delete ${targetOutputFile.absolutePath}, if you need to re-download it)")
            }

            logger.lifecycle("Fetching ${targetRepoFileName} into ${targetOutputFile.absolutePath}.")
            Utils.readFile releaseInfo["browser_download_url"] as String, targetOutputFile, 4096, new Consumer<Long>() {
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
        } else {
            if (!targetOutputFile.exists())
                state.addFailure(new TaskExecutionException(this, new Exception("Unable to fetch files in offline mode.")))
            else logger.warn "warning: File exists, but working in offline mode - using it without integrity check."
        }
    }
}
