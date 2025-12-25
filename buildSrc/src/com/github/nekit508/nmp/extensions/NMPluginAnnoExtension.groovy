package com.github.nekit508.nmp.extensions

import com.github.nekit508.nmp.NMPlugin
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.compile.JavaCompile
import com.github.nekit508.nmp.tasks.anno.*

class NMPluginAnnoExtension extends NMPluginExtension {
    Property<String> jabelVersion
    Property<JavaVersion> sourceCompatibility
    ListProperty<File> srcDirs, resDirs
    Property<File> genDir

    final NMPluginCoreExtension core

    NMPluginAnnoExtension(String name, Project project, NMPlugin plugin, NMPluginCoreExtension core) {
        super(name, project, plugin)
        this.core = core

        genericInit()
    }

    /*@Override
    NMPluginAnnoExtension settings(Closure closure) {
        settingsI closure
        return this
    }

    @Override
    NMPluginAnnoExtension configure(Closure closure) {
        configureI closure
        return this
    }*/

    void genericInit() {
        nmp.configuration {
            Common.configureBuildTasks attachedProject, attachedProject.tasks.compileJava as JavaCompile, genDir

            srcDirs.finalizeValue()
            resDirs.finalizeValue()
            attachedProject.sourceSets.main.java.srcDirs += srcDirs.get()
            attachedProject.sourceSets.main.resources.srcDirs += resDirs.get()

            Common.setupJabel attachedProject, sourceCompatibility, jabelVersion

            core.attachedProject.dependencies { DependencyHandler handler ->
                handler.add "compileOnly", attachedProject
                handler.add "annotationProcessor", attachedProject
            }
        }

        nmp.initialisation {
            attachedProject.tasks.register "nmpaGenerateProcessorsFile", GenerateProcessorsFileTask, this
        }
    }

    @Override
    void apply() {
        super.apply()

        jabelVersion = factory.property String
        sourceCompatibility = factory.property JavaVersion

        srcDirs = factory.listProperty File
        resDirs = factory.listProperty File
        genDir = factory.property File

        nmp.setting {
            genDir.set attachedProject.file("gen")
            resDirs.add attachedProject.file("res")
            srcDirs.add attachedProject.file("src")

            sourceCompatibility.set core.sourceCompatibility
            jabelVersion.set core.jabelVersion
        }
    }
}
