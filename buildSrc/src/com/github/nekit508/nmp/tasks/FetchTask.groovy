package com.github.nekit508.nmp.tasks

import com.github.nekit508.nmp.NMPlugin
import com.github.nekit508.nmp.lib.FetchSpec
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*

import javax.inject.Inject

/** Actually it is FetchGithubReleaseTask */
class FetchTask extends DefaultTask {
    @Internal
    NMPlugin nmp

    @OutputFile
    final RegularFileProperty outputFile

    @Input
    final Property<String> repoFileName
    @Input
    final Property<String> repo
    @Input
    final Property<String> version

    @Internal
    final DirectoryProperty outputDir

    @Internal
    final Property<String> fileName
    @Internal
    final Property<String> extension
    @Internal
    final Property<String> outputFileName

    @Internal
    FetchSpec spec

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

        spec = factory.newInstance(FetchSpec, project as Project, logger)

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

        spec.setup outputFile.get(), repo.get(), version.get(), repoFileName.get()
        try {
            state.didWork = spec.fetch()
        } catch (Exception e) {
            state.addFailure new TaskExecutionException(this, e)
        }
    }
}
