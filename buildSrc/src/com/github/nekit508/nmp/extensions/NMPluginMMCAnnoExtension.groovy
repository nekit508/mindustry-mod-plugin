package com.github.nekit508.nmp.extensions

import com.github.nekit508.nmp.NMPlugin
import com.github.nekit508.nmp.lib.Utils
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.compile.JavaCompile

// TODO Pull request settings via compiler arguments
class NMPluginMMCAnnoExtension extends NMPluginExtension {
    RegularFileProperty modInfoPath
    DirectoryProperty rootDirectory, genRes, rawRes, revisionsPath
    ListProperty<String> modules
    Property<String> mmcVersion, classPrefix;

    final NMPluginCoreExtension ext

    NMPluginMMCAnnoExtension(String name, Project project, NMPlugin plugin, NMPluginCoreExtension ext) {
        super(name, project, plugin)
        this.ext = ext
    }

    @Override
    void apply() {
        super.apply()

        mmcVersion = factory.property String
        modules = factory.listProperty String

        rootDirectory = factory.directoryProperty()
        genRes = factory.directoryProperty()
        rawRes = factory.directoryProperty()
        modInfoPath = factory.fileProperty()
        revisionsPath = factory.directoryProperty()

        classPrefix = factory.property String

        nmp.setting {
            rootDirectory.set attachedProject.projectDir

            genRes.set attachedProject.layout.projectDirectory.file("genRes").asFile
            rawRes.set attachedProject.layout.projectDirectory.file("rawRes").asFile
            modInfoPath.set(attachedProject.provider { attachedProject.tasks.named("nmpGenerateModInfo").get().outputFile.get() })
            revisionsPath.set attachedProject.layout.projectDirectory.file("mmcRevisions").asFile
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
            attachedProject.repositories {
                maven { url "https://raw.githubusercontent.com/Zelaux/Repo/master/repository" }
            }
        }
    }

    void setupDependencies() {
        nmp.configuration {
            var version = mmcVersion.get()

            attachedProject.dependencies { DependencyHandler handler ->
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
            attachedProject.tasks.named("compileJava").configure { task ->
                task.doFirst {
                    attachedProject.delete genRes.get().asFileTree.files

                    var rootPath = rootDirectory.get().asFile.absolutePath
                    Utils.annotationProcessorArgs attachedProject.tasks.named("compileJava") as TaskProvider<JavaCompile>,
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
            attachedProject.sourceSets.main.resources.srcDirs += genRes
        }
    }

    void basicModules() {
        nmp.setting {
            modules.addAll "load", "remote", "logic", "assets", "struct", "serialize"
        }
    }
}
