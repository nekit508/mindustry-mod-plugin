package nekit508.tasks

import nekit508.NMPlugin
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.file.RegularFileProperty
import org.gradle.jvm.tasks.Jar

class BuildTask extends Jar {
    BuildTask(NMPlugin ext) {
        (archiveFile as RegularFileProperty).convention { project.file("build/libs/classes.jar") }

        setDuplicatesStrategy DuplicatesStrategy.EXCLUDE
        from {
            project.configurations.runtimeClasspath.collect {
                it.isDirectory() ? it : project.zipTree(it)
            }
        }
    }
}
