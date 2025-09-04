package com.github.nekit508.nmp.tasks.entityanno

import com.github.nekit508.nmp.extensions.NMPluginEntityAnnoExtension
import org.gradle.api.DefaultTask
import org.gradle.api.file.DeleteSpec
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

class ProcessComponentsTask extends DefaultTask {
    @Internal
    NMPluginEntityAnnoExtension ext

    @OutputDirectory
    final DirectoryProperty fetchedCompsDir
    @InputDirectory
    final DirectoryProperty fetchedCompsDirSource

    @Input
    final Property<String> fetchedCompsPackage

    @Inject
    ProcessComponentsTask(NMPluginEntityAnnoExtension ext) {
        group = "nmpea"
        this.ext = ext

        var factory = project.objects

        fetchedCompsDir = factory.directoryProperty()
        fetchedCompsPackage = factory.property String
        fetchedCompsDirSource = factory.directoryProperty()

        configure {
            fetchedCompsPackage.set ext.fetchedCompsPackage
            fetchedCompsDirSource.set project.tasks.nmpeaFetchComps.fetchedCompsDir
            fetchedCompsDir.set ext.fetchedCompsDir
        }

        dependsOn project.tasks.nmpeaFetchComps
    }

    @TaskAction
    void process() {
        var packagee = fetchedCompsPackage.get()
        var l = fetchedCompsDirSource.get().asFile.absolutePath.length()
        var destDirFile = fetchedCompsDir.get().asFile
        project.delete { DeleteSpec srec ->
            srec.delete project.fileTree(destDirFile).files
        }

        logger.lifecycle "Processing components"
        fetchedCompsDirSource.get().asFileTree.files.each { File src ->
            var subPath = src.absolutePath
            subPath = subPath.substring l, subPath.length()
            var dest = new File(destDirFile, subPath)

            logger.lifecycle "Processing $src.absolutePath into $dest.absolutePath"

            dest.write(
                    src.text
                            .replace("mindustry.entities.comp", packagee)
                            .replace("mindustry.annotations.Annotations.*", "ent.anno.Annotations.*")
                            .replaceAll("@Component\\((base = true|.)+\\)\n*", "@EntityComponent(base = true, vanilla = true)\n")
                            .replaceAll("@Component\n*", "@EntityComponent(vanilla = true)\n")
                            .replaceAll("@BaseComponent\n*", "@EntityBaseComponent\n")
                            .replaceAll("@CallSuper\n*", "")
                            .replaceAll("@Final\n*", "")
                            .replaceAll("@EntityDef\\(*.*\\)*\n*", "")
            )
        }
        logger.lifecycle "Processed components"
    }
}
