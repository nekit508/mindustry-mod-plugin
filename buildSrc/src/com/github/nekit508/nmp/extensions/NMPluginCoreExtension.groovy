package com.github.nekit508.nmp.extensions

import com.github.nekit508.nmp.NMPlugin
import com.github.nekit508.nmp.tasks.TasksQueue
import org.gradle.api.GradleException
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.compile.JavaCompile
import com.github.nekit508.nmp.tasks.core.*

class NMPluginCoreExtension extends NMPluginExtension {
    Property<String> mindustryVersion, modName, modVersion, modGroup, jabelVersion
    Property<Boolean> generateModInfo
    Property<JavaVersion> sourceCompatibility
    ListProperty<File> srcDirs, resDirs
    Property<File> genDir

    Property<String> mavenPublishPluginName, javaLibraryPluginName

    NMPluginCoreExtension(String name, Project project, NMPlugin plugin, boolean publishable, String group) {
        super(name, project, plugin)

        genericInit(publishable, group)
    }

    /*@Override
    NMPluginCoreExtension settings(@DelegatesTo(NMPluginCoreExtension) Closure closure) {
        settingsI closure
        return this
    }

    @Override
    NMPluginCoreExtension configure(@DelegatesTo(NMPluginCoreExtension) Closure closure) {
        configureI closure
        return this
    }*/

    @Override
    void apply() {
        super.apply()

        mindustryVersion = factory.property String
        modName = factory.property String
        modVersion = factory.property String
        modGroup = factory.property String
        jabelVersion = factory.property String

        generateModInfo = factory.property Boolean

        sourceCompatibility = factory.property JavaVersion

        srcDirs = factory.listProperty File
        resDirs = factory.listProperty File
        genDir = factory.property File

        mavenPublishPluginName = factory.property String
        javaLibraryPluginName = factory.property String

        nmp.setting {
            genDir.set attachedProject.file("gen")
            resDirs.add attachedProject.file("res")
            srcDirs.add attachedProject.file("src")
            sourceCompatibility.set JavaVersion.VERSION_20
            generateModInfo.set true
            jabelVersion.set "1.0.1-1"
            mindustryVersion.set "v146"

            modName.set attachedProject.name
            modGroup.set attachedProject.group.toString()

            mavenPublishPluginName.set "maven-publish"
            javaLibraryPluginName.set "java-library"
        }
    }

    void genericInit(boolean publishable, String group) {
        nmp.configuration {
            Common.configureBuildTasks attachedProject, attachedProject.tasks.compileJava as JavaCompile, genDir

            srcDirs.finalizeValue()
            resDirs.finalizeValue()
            attachedProject.sourceSets.main.java.srcDirs += srcDirs
            attachedProject.sourceSets.main.resources.srcDirs += resDirs

            Common.setupJabel attachedProject, sourceCompatibility, jabelVersion

            attachedProject.dependencies { DependencyHandler handler ->
                handler.add "compileOnly", Common.mindustryDependency(mindustryVersion.get())
                handler.add "compileOnly", Common.arcDependency(mindustryVersion.get())
            }

        }

        nmp.initialisation {
            attachedProject.tasks.register "nmpBuild", BuildTask, this
            attachedProject.tasks.register "nmpDex", DexTask, this

            attachedProject.tasks.register "nmpBuildRelease", BuildReleaseTask, this
            attachedProject.tasks.register "nmpCopyBuildRelease", CopyBuildReleaseTask, this
            attachedProject.tasks.register "nmpGenerateModInfo", GenerateModInfoTask, this

            attachedProject.tasks.register "nmpFetchMindustry", FetchMindustryTask, this
            attachedProject.tasks.register "nmpRunMindustry", RunMindustry, this

            attachedProject.tasks.register "nmpCopyBuildReleaseRunMindustry", TasksQueue, "nmp", new Task[]{
                    attachedProject.tasks.nmpCopyBuildRelease,
                    attachedProject.tasks.nmpRunMindustry
            }
        }

        if (publishable) {
            if (group == null)
                new GradleException("group must be specified with publishable = true.")
            nmp.configureProjectDataForJitpackBuilding group

            nmp.initialisation {
                attachedProject.tasks.register "nmpBuildSources", BuildSourcesTask, this
                attachedProject.tasks.register "nmpBuildLibrary", BuildLibraryTask, this
            }

            nmp.configuration() {
                attachedProject.with {
                    nmp.requirePlugin attachedProject, mavenPublishPluginName.get()
                    nmp.requirePlugin attachedProject, javaLibraryPluginName.get()

                    java {
                        withSourcesJar()
                        withJavadocJar()
                    }

                    publishing {
                        publications {
                            library(MavenPublication) {
                                from components.java
                            }
                        }
                    }

                    tasks.jar.dependsOn tasks.nmpBuildLibrary
                    tasks.jar.from zipTree(tasks.nmpBuildLibrary.archiveFile.get())
                    tasks.jar.setDuplicatesStrategy DuplicatesStrategy.EXCLUDE

                    tasks.sourcesJar.dependsOn tasks.nmpBuildSources
                    tasks.sourcesJar.from zipTree(tasks.nmpBuildSources.archiveFile.get())
                    tasks.sourcesJar.setDuplicatesStrategy DuplicatesStrategy.EXCLUDE
                }
            }
        }
    }
}
