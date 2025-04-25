package nekit508.main.tasks

import nekit508.main.NMPlugin
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.Internal
import org.gradle.jvm.tasks.Jar

import javax.inject.Inject

class BuildTask extends Jar {
    @Internal
    NMPlugin ext

    @Inject
    BuildTask(NMPlugin ext) {
        group = "nmp"
        this.ext = ext

        (archiveFile as RegularFileProperty).set project.layout.buildDirectory.file("libs/tmp/classes.jar")

        dependsOn project.tasks.classes

        setDuplicatesStrategy DuplicatesStrategy.EXCLUDE

        from {
            project.configurations.runtimeClasspath.collect {
                it.isDirectory() ? it : project.zipTree(it)
            }
        }
        from project.file(project.tasks.compileJava.destinationDirectory.get())
        from project.file(project.tasks.processResources.destinationDir)
    }
}
