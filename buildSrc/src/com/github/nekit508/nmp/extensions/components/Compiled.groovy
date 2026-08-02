package com.github.nekit508.nmp.extensions.components

import org.gradle.api.JavaVersion
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.compile.JavaCompile

interface Compiled extends Sourced, Default {
    DirectoryProperty getGen();
    Property<JavaVersion> getTargetCompatibility();
    Property<JavaVersion> getSourceCompatibility();
    Property<Boolean> getUseJabel();
    Property<String> getJabelVersion();
    Property<String> getJabelRepo();

    default void _Compiled() {
        nmp.setting {
            gen.set fileFactory.dir(
                    project.file(settings?.compilation?.gen ?: "gen")
            )

            targetCompatibility.set settings?.compilation?.target ?: JavaVersion.VERSION_17
            sourceCompatibility.set settings?.compilation?.source ?: JavaVersion.VERSION_17

            useJabel.set Boolean.parseBoolean(settings?.compilation?.useJabel ?: "false")
            jabelVersion.set settings?.compilation?.jabelVersion ?: "1.0.1-1"
            jabelRepo.set settings?.compilation?.jabelRepo ?: "com.pkware.jabel:jabel-javac-plugin"
        }

        nmp.configuration {
            project.tasks.withType(JavaCompile).configureEach { JavaCompile task ->
                task.options.encoding = "UTF-8"

                task.options.generatedSourceOutputDirectory.set gen

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

                task.doFirst { i ->
                    task.sourceCompatibility = this.sourceCompatibility.get().getMajorVersion()
                    task.targetCompatibility = this.targetCompatibility.get().getMajorVersion()
                    // TODO is javac so stupid, that I need to delete generated stuff at every compilation?
                    this.project.delete task.options.generatedSourceOutputDirectory.get().asFile.listFiles()

                    // TODO do we really need ts?
                    task.options.compilerArgs = task.options.compilerArgs.findAll {
                        it != "--enable-preview"
                    }
                }
            }

            if (useJabel.get()) {
                if (targetCompatibility.get() != JavaVersion.VERSION_1_8) {
                    project.logger.warn("warning: targetCompatibility is set to ${targetCompatibility.get()}, but jabel is enabled, targetCompatibility will be forced to 8th version.")
                    targetCompatibility.set JavaVersion.VERSION_1_8
                }

                project.tasks.compileJava.configure { JavaCompile task ->
                    task.options.compilerArgs = [
                            "--release", "8",
                            "-Xlint:-options"
                    ]
                }

                getProject().dependencies { DependencyHandler handler ->
                    handler.add "annotationProcessor", "${jabelRepo.get()}:${jabelVersion.get()}"
                    handler.add "compileOnly", "${jabelRepo.get()}:${jabelVersion.get()}"
                }
            }
        }
    }
}