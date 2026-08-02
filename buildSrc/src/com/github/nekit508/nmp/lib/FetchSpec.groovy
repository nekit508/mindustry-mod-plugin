package com.github.nekit508.nmp.lib

import com.github.nekit508.nmp.NMPlugin
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.file.RegularFile
import org.gradle.api.logging.Logger
import org.gradle.internal.hash.Hashing

import javax.inject.Inject
import java.util.function.Consumer

// TODO write FetchSpecFactory and obtain it via gradle object factory in NMPlugin, and then create specs in place by this factory
/** Actually it is FetchGithubReleaseSpec */
abstract class FetchSpec {
    RegularFile outputFile
    String repo
    String version
    String repoFileName

    Project project
    NMPlugin nmp
    Logger logger

    @Inject
    FetchSpec(Project project, Logger logger) {
        this.project = project
        nmp = Objects.requireNonNull(project.extensions.nmp as NMPlugin)
        this.logger = logger
    }

    void setup(RegularFile outputFile, String repo, String version, String repoFileName) {
        this.outputFile = outputFile
        this.repo = repo
        this.version = version
        this.repoFileName = repoFileName
    }

    boolean fetch() {
        var targetVersion = Objects.requireNonNull(version)
        var targetRepo = Objects.requireNonNull(repo)
        var targetRepoFileName = Objects.requireNonNull(repoFileName)
        var targetOutputFile = Objects.requireNonNull(outputFile?.asFile)

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
                        return false
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

            return true
        } else {
            if (!targetOutputFile.exists())
                throw new Exception("Unable to fetch files in offline mode.")

            logger.warn "warning: File exists, but working in offline mode - using it without integrity check."
            return false
        }
    }
}
