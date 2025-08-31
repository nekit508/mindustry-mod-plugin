package com.github.nekit508.nmp.tasks.entityanno

import com.github.nekit508.nmp.Utils
import com.github.nekit508.nmp.extensions.NMPluginEntityAnnoExtension
import org.gradle.api.DefaultTask
import org.gradle.api.file.DeleteSpec
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

class FetchComponentsTask extends DefaultTask {
    @Internal
    NMPluginEntityAnnoExtension ext

    @OutputDirectory
    final DirectoryProperty fetchedCompsDir

    @Input
    final Property<String> fetchCompsVersion

    @Inject
    FetchComponentsTask(NMPluginEntityAnnoExtension ext) {
        group = "nmpea"
        this.ext = ext

        var factory = project.objects

        fetchedCompsDir = factory.directoryProperty()
        fetchCompsVersion = factory.property String

        configure {
            fetchCompsVersion.set ext.core.mindustryVersion

            fetchedCompsDir.set temporaryDir
        }
    }

    @TaskAction
    void fetch() {
        var data = (Utils.readJson "https://api.github.com/repos/Anuken/MIndustry/contents/core/src/mindustry/entities/comp?ref=${fetchCompsVersion.get()}") as Iterable<?>
        var dir = fetchedCompsDir.get().asFile
        project.delete { DeleteSpec srec ->
            srec.delete project.fileTree(dir).files
        }

        var files = new ArrayList<File>()

        logger.lifecycle "Fetching components"
        data.each { Map<String, ?> fileInfo ->
            var file = new File(dir, fileInfo.name as String)
            var url = fileInfo.download_url as String

            logger.lifecycle "Downloading $url into $file.absolutePath"

            Utils.readFile url, file
            files.add file
        }
        logger.lifecycle "Fetched components"
    }
}
