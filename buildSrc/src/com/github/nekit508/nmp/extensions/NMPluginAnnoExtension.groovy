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
    }

    @Override
    NMPluginAnnoExtension settings(Closure closure) {
        settingsI closure
        return this
    }

    @Override
    NMPluginAnnoExtension configure(Closure closure) {
        configureI closure
        return this
    }

    void configureCompileTask() {
        nmp.configuration() + {

            attachedProject.tasks.compileJava { JavaCompile task ->
                task.options.encoding = "UTF-8"
                task.options.generatedSourceOutputDirectory.set genDir.get()

                task.options.forkOptions.jvmArgs += [
                        "--add-opens=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED",
                        "--add-opens=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED",
                        "--add-opens=jdk.compiler/com.sun.tools.javac.model=ALL-UNNAMED",
                        "--add-opens=jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED",
                        "--add-opens=jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED",
                        "--add-opens=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED",
                        "--add-opens=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED",
                        "--add-opens=jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED",
                        "--add-opens=jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED",
                        "--add-opens=jdk.compiler/com.sun.tools.javac.jvm=ALL-UNNAMED",
                        "--add-opens=jdk.compiler/com.sun.tools.javac.comp=ALL-UNNAMED",
                        "--add-opens=java.base/sun.reflect.annotation=ALL-UNNAMED"
                ]

                task.doFirst {
                    attachedProject.delete task.options.generatedSourceOutputDirectory.get().asFile.listFiles()

                    task.options.compilerArgs = task.options.compilerArgs.findAll {
                        it != "--enable-preview"
                    }
                }
            }

            attachedProject.sourceSets.main.java.srcDirs += srcDirs.get()
            attachedProject.sourceSets.main.resources.srcDirs += resDirs.get()
        }
    }

    void setupJabel() {
        nmp.configuration() + {
            attachedProject.tasks.compileJava { JavaCompile task ->
                task.sourceCompatibility = this.sourceCompatibility.get().majorVersion

                task.options.compilerArgs = [
                        "--release", "8",
                        "--enable-preview",
                        "-Xlint:-options"
                ]
            }

            attachedProject.dependencies { DependencyHandler handler ->
                handler.add "annotationProcessor", "com.pkware.jabel:jabel-javac-plugin:${jabelVersion.get()}"
                handler.add "compileOnly", "com.pkware.jabel:jabel-javac-plugin:${jabelVersion.get()}"
            }
        }
    }

    void setupDependencies() {
        nmp.configuration() + {
            core.attachedProject.dependencies { DependencyHandler handler ->
                handler.add "compileOnly", attachedProject
                handler.add "annotationProcessor", attachedProject
            }
        }
    }

    void initTasks() {
        nmp.configuration() + {
            attachedProject.tasks.register "nmpaGenerateProcessorsFile", GenerateProcessorsFileTask, this
        }
    }

    void genericInit() {
        configureCompileTask()
        setupJabel()
        setupDependencies()
        initTasks()
    }

    @Override
    void apply() {
        super.apply()

        jabelVersion = factory.property String
        sourceCompatibility = factory.property JavaVersion

        srcDirs = factory.listProperty File
        resDirs = factory.listProperty File
        genDir = factory.property File

        settings {
            genDir.set attachedProject.file("gen")
            resDirs.add attachedProject.file("res")
            srcDirs.add attachedProject.file("src")

            sourceCompatibility.set core.sourceCompatibility
            jabelVersion.set core.jabelVersion
        }
    }
}
