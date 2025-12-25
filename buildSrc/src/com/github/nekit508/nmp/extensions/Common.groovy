package com.github.nekit508.nmp.extensions

import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.provider.Property
import org.gradle.api.tasks.compile.JavaCompile

class Common {
    static void configureBuildTasks(Project project, JavaCompile task, Property<File> gen) {
        task.options.encoding = "UTF-8"

        gen.finalizeValue()
        task.options.generatedSourceOutputDirectory.set gen.get()

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
            project.delete task.options.generatedSourceOutputDirectory.get().asFile.listFiles()

            task.options.compilerArgs = task.options.compilerArgs.findAll {
                it != "--enable-preview"
            }
        }

        project.tasks.processResources.setDuplicatesStrategy DuplicatesStrategy.EXCLUDE
    }

    static void setupJabel(Project project, Property<JavaVersion> sourceCompatibility, Property<String> jabelVersion) {
        project.tasks.compileJava { JavaCompile task ->
            sourceCompatibility.finalizeValue()
            task.sourceCompatibility = sourceCompatibility.get().majorVersion

            task.options.compilerArgs = [
                    "--release", "8",
                    "--enable-preview",
                    "-Xlint:-options"
            ]
        }

        project.dependencies { DependencyHandler handler ->
            jabelVersion.disallowChanges()
            handler.add "annotationProcessor", "com.pkware.jabel:jabel-javac-plugin:${jabelVersion.get()}"
            handler.add "compileOnly", "com.pkware.jabel:jabel-javac-plugin:${jabelVersion.get()}"
        }
    }

    static String mindustryDependency(String version, String module = "core") {
        return dependency("com.github.Anuken.Mindustry", module, version)
    }

    static String arcDependency(String version, String module = "arc-core") {
        return dependency("com.github.Anuken.Arc", module, version)
    }

    static String dependency(String dep, String module, String version) {
        return "$dep:$module:$version"
    }
}
