package nekit508.tasks

import nekit508.NMPlugin
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.file.RegularFileProperty
import org.gradle.jvm.tasks.Jar

import javax.inject.Inject

class BuildTask extends Jar {
    @Inject
    BuildTask(NMPlugin ext) {
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
