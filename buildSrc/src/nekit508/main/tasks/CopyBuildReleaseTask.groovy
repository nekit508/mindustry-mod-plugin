package nekit508.main.tasks

import nekit508.main.NMPlugin
import org.gradle.api.DefaultTask
import org.gradle.api.file.CopySpec
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

class CopyBuildReleaseTask extends DefaultTask {
    @Internal
    NMPlugin ext

    @Input
    ListProperty<File> copyPaths

    @Inject
    CopyBuildReleaseTask(NMPlugin ext) {
        group = "nmp"
        this.ext = ext

        ObjectFactory objectFactory = getProject().getObjects()
        dependsOn project.tasks.nmpBuildRelease

        copyPaths = objectFactory.listProperty(File.class)
        copyPaths.set ext.local?.copy?.collect {String path -> new File(path)} ?: []
    }

    @TaskAction
    void copy() {
        copyPaths.get().each { p ->
            project.copy { CopySpec spec ->
                spec.from project.tasks.nmpBuildRelease.archiveFile.get()
                spec.into p
            }
        }
    }
}
