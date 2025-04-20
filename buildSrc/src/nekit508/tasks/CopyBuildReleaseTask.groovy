package nekit508.tasks

import nekit508.NMPlugin
import org.gradle.api.DefaultTask
import org.gradle.api.file.CopySpec
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Input

import javax.inject.Inject

class CopyBuildReleaseTask extends DefaultTask {
    @Input
    ListProperty<File> copyPaths

    @Inject
    CopyBuildReleaseTask(NMPlugin ext) {
        group = "nmp"

        ObjectFactory objectFactory = getProject().getObjects()
        dependsOn project.tasks.nmpBuildRelease

        copyPaths = objectFactory.listProperty(File.class)
        copyPaths.set ext.local?.copy?.collect {String path -> new File(path)} ?: []

        doLast {
            copyPaths.get().each { p ->
                project.copy { CopySpec spec ->
                    spec.from project.tasks.nmpBuildRelease.archiveFile.get()
                    spec.into p
                }
            }
        }
    }
}
