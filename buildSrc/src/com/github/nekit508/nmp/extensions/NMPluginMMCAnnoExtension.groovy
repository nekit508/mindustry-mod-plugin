package com.github.nekit508.nmp.extensions

import com.github.nekit508.nmp.NMPlugin
import com.github.nekit508.nmp.lib.Utils
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.compile.JavaCompile

// TODO Pull request settings via compiler arguments
abstract class NMPluginMMCAnnoExtension extends NMPluginExtension {
    RegularFileProperty modInfoPath
    DirectoryProperty rootDirectory, genRes, rawRes, revisionsPath
    ListProperty<String> modules
    Property<String> mmcVersion, classPrefix

    final NMPluginCoreExtension ext

    NMPluginMMCAnnoExtension(String name, Project project, NMPluginCoreExtension ext) {
        super(name, project)
        this.ext = ext
    }

    @Override
    void apply() {
        super.apply()

        mmcVersion = objectFactory.property String
        modules = objectFactory.listProperty String

        rootDirectory = objectFactory.directoryProperty()
        genRes = objectFactory.directoryProperty()
        rawRes = objectFactory.directoryProperty()
        modInfoPath = objectFactory.fileProperty()
        revisionsPath = objectFactory.directoryProperty()

        classPrefix = objectFactory.property String

        nmp.setting {
            rootDirectory.set project.projectDir

            genRes.set project.layout.projectDirectory.file("genRes").asFile
            rawRes.set project.layout.projectDirectory.file("rawRes").asFile
            modInfoPath.set(project.provider { project.tasks.named("nmpGenerateModInfo").get().outputFile.get() } as Provider<? extends RegularFile>)
            revisionsPath.set project.layout.projectDirectory.file("mmcRevisions").asFile
        }
    }

    void genericInit() {
        basicModules()
        addMMCRepo()
        setupDependencies()
        setupCompileJava()
        setupSourceSets()
    }

    void addMMCRepo() {
        nmp.configuration {
            project.repositories {
                maven { url "https://raw.githubusercontent.com/Zelaux/Repo/master/repository" }
            }
        }
    }

    void setupDependencies() {
        nmp.configuration {
            var version = mmcVersion.get()

            project.dependencies { DependencyHandler handler ->
                this.modules.get().each { module ->
                    var moduleDependency = "com.github.Zelaux.MindustryModCore:annotations-$module:$version"
                    handler.compileOnly moduleDependency
                    handler.annotationProcessor moduleDependency
                }
            }
        }
    }

    void setupCompileJava() {
        nmp.configuration {
            project.tasks.named("compileJava").configure { task ->
                task.doFirst {
                    project.delete genRes.get().asFileTree.files

                    var rootPath = rootDirectory.get().asFile.absolutePath
                    Utils.annotationProcessorArgs project.tasks.named("compileJava") as TaskProvider<JavaCompile>,
                            [
                                    "rootDirectory": rootPath,
                                    "assetsPath"   : Utils.subpath(rootPath, genRes.get().asFile.absolutePath),
                                    "assetsRawPath": Utils.subpath(rootPath, rawRes.get().asFile.absolutePath),
                                    "rootPackage"  : Utils.subpath(rootPath, rootDirectory.get().asFile.absolutePath),
                                    "modInfoPath"  : Utils.subpath(rootPath, modInfoPath.get().asFile.absolutePath),
                                    "revisionsPath": Utils.subpath(rootPath, revisionsPath.get().asFile.absolutePath),
                                    "classPrefix"  : classPrefix.get()
                            ]
                }
            }
        }
    }

    void setupSourceSets() {
        nmp.configuration {
            project.sourceSets.main.resources.srcDirs += genRes
        }
    }

    void basicModules() {
        nmp.setting {
            modules.addAll "load", "remote", "logic", "assets", "struct", "serialize"
        }
    }
}
