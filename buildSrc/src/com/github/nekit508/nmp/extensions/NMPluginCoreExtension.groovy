package com.github.nekit508.nmp.extensions


import com.github.nekit508.nmp.extensions.components.Compiled
import com.github.nekit508.nmp.extensions.components.NMPluginExtension
import com.github.nekit508.nmp.tasks.TasksQueue
import com.github.nekit508.nmp.tasks.core.*
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.provider.Property
import org.gradle.api.publish.maven.MavenPublication

import javax.inject.Inject 

abstract class NMPluginCoreExtension extends NMPluginExtension implements Compiled {
    Property<String> mindustryVersion, arcVersion, modName, modVersion, modGroup, mindustryWorkingDirectory, mindustryDataDirectory
    Property<Boolean> generateModInfo, mindustryCopyModInDataDir

    Property<String> mavenPublishPluginName, javaLibraryPluginName

    protected boolean _publishable
    protected String _group

    @Inject
    NMPluginCoreExtension(String name, Project project, boolean publishable, String group) {
        super(name, project)

        _publishable = publishable
        _group = group
    }

    @Override
    void apply() {
        super.apply()

        _Sourced()
        _Compiled()

        mindustryVersion = objectFactory.property String
        arcVersion = objectFactory.property String
        modName = objectFactory.property String
        modVersion = objectFactory.property String
        modGroup = objectFactory.property String
        mindustryWorkingDirectory = objectFactory.property String
        mindustryDataDirectory = objectFactory.property String

        generateModInfo = objectFactory.property Boolean
        mindustryCopyModInDataDir = objectFactory.property Boolean

        mavenPublishPluginName = objectFactory.property String
        javaLibraryPluginName = objectFactory.property String

        nmp.setting {
            generateModInfo.set true
            mindustryVersion.set "v146"
            arcVersion.set mindustryVersion

            modName.set project.name
            modGroup.set project.group.toString()

            mavenPublishPluginName.set "maven-publish"
            javaLibraryPluginName.set "java-library"

            mindustryWorkingDirectory.set nmp.nmpSettings?.mindustry?.workingDirectory
            mindustryDataDirectory.set nmp.nmpSettings?.mindustry?.dataDirectory ?: mindustryWorkingDirectory.getOrNull()
            mindustryCopyModInDataDir.set nmp.nmpSettings?.mindustry?.copyModInDataDir ?: true
        }

        nmp.configuration {
            project.allprojects.each { Project project ->
                project.configurations.configureEach { configuration ->
                    // force Arc version.
                    configuration.resolutionStrategy.eachDependency { dep ->
                        if(dep.requested.group == "com.github.Anuken.Arc" ){
                            arcVersion.finalizeValue()
                            dep.useVersion arcVersion.get()
                        }
                    }
                }
            }

            project.dependencies { DependencyHandler handler ->
                mindustryVersion.finalizeValue()
                arcVersion.finalizeValue()

                handler.add "compileOnly", nmp.mindustryDependency(mindustryVersion.get())
                handler.add "compileOnly", nmp.arcDependency(arcVersion.get())
            }
        }

        nmp.initialisation {
            project.tasks.create "nmpBuild", BuildTask, this
            project.tasks.create "nmpDex", DexTask, this

            project.tasks.create "nmpBuildRelease", BuildReleaseTask, this
            project.tasks.create "nmpCopyBuildRelease", CopyBuildReleaseTask, this
            project.tasks.create "nmpGenerateModInfo", GenerateModInfoTask, this

            project.tasks.create "nmpFetchServer", FetchServerTask, this
            project.tasks.create "nmpRunServer", RunServerTask, this
            project.tasks.create "nmpCopyBuildReleaseRunServer", TasksQueue, "nmp", new Task[]{
                    project.tasks.nmpCopyBuildRelease,
                    project.tasks.nmpRunServer
            }

            project.tasks.create "nmpFetchMindustry", FetchMindustryTask, this
            project.tasks.create "nmpRunMindustry", RunMindustryTask, this
            project.tasks.create "nmpCopyBuildReleaseRunMindustry", TasksQueue, "nmp", new Task[]{
                    project.tasks.nmpCopyBuildRelease,
                    project.tasks.nmpRunMindustry
            }

            //project.tasks.create "nmpBundlesAutoGen", BundlesAutoGen, this
        }

        if (_publishable) {
            if (_group == null)
                new GradleException("group must be specified when publishable = true.")
            nmp.configureProjectDataForJitpackBuilding _group

            nmp.initialisation {
                project.tasks.create "nmpBuildSources", BuildSourcesTask, this
                project.tasks.create "nmpBuildLibrary", BuildLibraryTask, this
            }

            nmp.configuration() {
                project.with {
                    nmp.requirePlugin project, mavenPublishPluginName.get()
                    nmp.requirePlugin project, javaLibraryPluginName.get()

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
