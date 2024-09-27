package nekit508.tasks

import nekit508.NMPlugin
import org.gradle.api.file.RegularFileProperty
import org.gradle.jvm.tasks.Jar

class BuildReleaseTask extends Jar {
    BuildReleaseTask(NMPlugin ext) {
        dependsOn project.tasks.nmpDex
        dependsOn project.tasks.nmpBuild

        (archiveFile as RegularFileProperty).convention { project.file("build/libs/" + project.getName() + "-" + project.getVersion() + ".jar") }

        from {
            project.tasks.nmpDex.buildAndroid.get() ? [project.zipTree(project.tasks.nmpBuild.archiveFile.get()), project.zipTree(project.tasks.nmpDex.dexFile.get())] :
                    [project.zipTree(project.tasks.nmpBuild.archiveFile.get())]
        }
    }
}
