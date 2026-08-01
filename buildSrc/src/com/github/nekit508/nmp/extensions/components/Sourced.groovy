package com.github.nekit508.nmp.extensions.components

import org.gradle.api.file.Directory
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.provider.ListProperty
import org.gradle.language.jvm.tasks.ProcessResources

interface Sourced extends Default {
    ListProperty<Directory> getSrc();
    ListProperty<Directory> getRes();

    default void _Sourced() {
        nmp.setting {
            src.add fileFactory.dir(project.file("src"))
            res.add fileFactory.dir(project.file("res"))
        }

        nmp.configuration {
            project.sourceSets.main.java.srcDirs += src.get()
            project.sourceSets.main.resources.srcDirs += res.get()

            project.tasks.processResources.configure { ProcessResources task ->
                task.setDuplicatesStrategy DuplicatesStrategy.EXCLUDE
            }
        }
    }
}