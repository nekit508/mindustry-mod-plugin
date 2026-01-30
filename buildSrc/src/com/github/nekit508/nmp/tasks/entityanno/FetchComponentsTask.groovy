package com.github.nekit508.nmp.tasks.entityanno

import com.github.nekit508.nmp.lib.Utils
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
    final Property<String> fetchedCompsPackage
    @Input
    final Property<String> fetchCompsVersion

    @Inject
    FetchComponentsTask(NMPluginEntityAnnoExtension ext) {
        group = "nmpea"
        this.ext = ext

        var factory = project.objects

        fetchedCompsDir = factory.directoryProperty()
        fetchCompsVersion = factory.property String
        fetchedCompsPackage = factory.property String

        configure {
            fetchCompsVersion.set ext.core.mindustryVersion

            fetchedCompsDir.set ext.fetchedCompsDir
            fetchedCompsPackage.set ext.fetchedCompsPackage
        }
    }

    @TaskAction
    void fetch() {
        if (ext.nmp().isOnline()) {
            var data = (Utils.readJson "https://api.github.com/repos/Anuken/Mindustry/contents/core/src/mindustry/entities/comp?ref=${fetchCompsVersion.get()}") as Iterable<?>
            var dir = fetchedCompsDir.get().asFile
            project.delete { DeleteSpec srec ->
                srec.delete project.fileTree(dir).files
            }
            var packagee = fetchedCompsPackage.get()
            var packageDir = new File(dir, packagee.replaceAll("\\.", "/"))
            packageDir.mkdirs()

            logger.lifecycle "Fetching components"
            data.each { Map<String, ?> fileInfo ->
                var file = new File(packageDir, fileInfo.name as String)
                var url = fileInfo.download_url as String
                logger.lifecycle "Fetching $url into $file.absolutePath."

                var text = Utils.readString url
                file.write text
                        .replace("mindustry.entities.comp", packagee)
                        .replace("mindustry.annotations.Annotations.*", "ent.anno.Annotations.*")
                        .replaceAll("@Component\\((base = true|.)+\\)\n*", "@EntityComponent(base = true, vanilla = true)\n")
                        .replaceAll("@Component\n*", "@EntityComponent(vanilla = true)\n")
                        .replaceAll("@BaseComponent\n*", "@EntityBaseComponent\n")
                        .replaceAll("@CallSuper\n*", "")
                        .replaceAll("@Final\n*", "")
                        .replaceAll("@EntityDef\\(*.*\\)*\n*", "")
            }
            logger.lifecycle "Fetched components"
        } else {
            logger.warn "warning: Working in offline mode, components files may be incomplete, which can cause compilation errors!"
            state.setDidWork false
        }
    }
}
