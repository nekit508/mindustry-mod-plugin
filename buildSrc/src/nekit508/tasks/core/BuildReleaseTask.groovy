package nekit508.tasks.core

import nekit508.extensions.NMPluginCoreExtension
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.Internal
import org.gradle.jvm.tasks.Jar

import javax.inject.Inject

class BuildReleaseTask extends Jar {
    @Internal
    NMPluginCoreExtension ext

    @Inject
    BuildReleaseTask(NMPluginCoreExtension ext) {
        group = "nmp"
        this.ext = ext

        configure {
            dependsOn project.tasks.nmpDex
            dependsOn project.tasks.nmpBuild

            (archiveFile as RegularFileProperty).set project.layout.buildDirectory.file("libs/$project.group-$project.name-${project.version}.jar")

            from {
                project.tasks.nmpDex.buildAndroid.get() ? [project.zipTree(project.tasks.nmpBuild.archiveFile.get()), project.zipTree(project.tasks.nmpDex.dexFile.get())] :
                        [project.zipTree(project.tasks.nmpBuild.archiveFile.get())]
            }
        }
    }
}
