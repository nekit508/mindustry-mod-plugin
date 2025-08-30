package com.github.nekit508.extensions

import com.github.nekit508.NMPlugin
import com.github.nekit508.tasks.core.*
import org.gradle.api.GradleException
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.compile.JavaCompile

class NMPluginCoreExtension extends NMPluginExtension {
    Property<String> mindustryVersion, modName, modVersion, modGroup, jabelVersion
    Property<Boolean> generateModInfo
    Property<JavaVersion> sourceCompatibility
    ListProperty<File> srcDirs, resDirs
    Property<File> genDir

    Property<String> mavenPublishPluginName, javaLibraryPluginName

    NMPluginCoreExtension(String name, Project project, NMPlugin plugin) {
        super(name, project, plugin)
    }

    @Override
    NMPluginCoreExtension settings(@DelegatesTo(NMPluginCoreExtension) Closure closure) {
        settingsI closure
        return this
    }

    @Override
    NMPluginCoreExtension configure(@DelegatesTo(NMPluginCoreExtension) Closure closure) {
        configureI closure
        return this
    }

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

        settings {
            genDir.set attachedProject.file("gen")
            resDirs.add attachedProject.file("res")
            srcDirs.add attachedProject.file("src")
            sourceCompatibility.set JavaVersion.VERSION_20
            generateModInfo.set true
            jabelVersion.set "1.0.1-1"
            mindustryVersion.set "v146"

            mavenPublishPluginName.set "maven-publish"
            javaLibraryPluginName.set "java-library"
        }
    }

    // TODO remove this boolean after some minor releases
    private boolean initedGenericTasks = false;
    void initGenericTasks() {
        if (checkConfigure(this::initGenericTasks)) return

        attachedProject.tasks.register "nmpBuild", BuildTask, this
        attachedProject.tasks.register "nmpDex", DexTask, this

        initedGenericTasks = true
    }

    void initModTasks() {
        if (checkConfigure(this::initModTasks)) return

        attachedProject.tasks.register "nmpBuildRelease", BuildReleaseTask, this
        attachedProject.tasks.register "nmpCopyBuildRelease", CopyBuildReleaseTask, this
        attachedProject.tasks.register "nmpGenerateModInfo", GenerateModInfoTask, this
    }

    @Deprecated(forRemoval = true)
    void initTasks() {
        if (checkConfigure(this::initTasks)) return

        if (!initedGenericTasks)
            initGenericTasks()

        initModTasks()

        attachedProject.logger.error("Legacy tasks initialisation will be removed in feature. Instead execute initGenericTasks() and initModTasks().")
    }

    void initLibraryTasks() {
        if (checkConfigure this::initLibraryTasks) return

        attachedProject.tasks.register "nmpBuildSources", BuildSourcesTask, this
        attachedProject.tasks.register "nmpBuildLibrary", BuildLibraryTask, this
    }

    void configureCompileTask() {
        if (checkConfigure this::configureCompileTask) return

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

    void setupJabel() {
        if (checkConfigure this::setupJabel) return

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

    void modBaseDependencies() {
        if (checkConfigure this::modBaseDependencies) return

        attachedProject.dependencies { DependencyHandler handler ->
            handler.add "compileOnly", nmp.mindustryDependency(mindustryVersion.get())
            handler.add "compileOnly", nmp.arcDependency(mindustryVersion.get())
        }
    }

    void configureMavenPublishing() {
        if (checkConfigure this::configureMavenPublishing) return

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

            tasks.jar.from tasks.nmpBuildLibrary
            tasks.jar.dependsOn tasks.nmpBuildLibrary

            tasks.sourcesJar.from tasks.nmpBuildSources
            tasks.sourcesJar.dependsOn tasks.nmpBuildSources
        }
    }

    void genericModInit(boolean isLibrary = false, String group = null) {
        if (checkConfigure () -> genericModInit(isLibrary, group)) return

        configureCompileTask()
        setupJabel()
        modBaseDependencies()

        initGenericTasks()
        initModTasks()

        if (isLibrary) {
            if (group == null)
                new GradleException("group must be specified with isLibrary = true.")
            initLibraryTasks()
            nmp.configureProjectDataForJitpackBuilding group
            configureMavenPublishing()
            arcAndMindustryAsApi()
        }
    }

    void arcAndMindustryAsApi() {
        if (checkConfigure this::arcAndMindustryAsApi) return

        attachedProject.dependencies { DependencyHandler handler ->
            handler.add "api", nmp.mindustryDependency(mindustryVersion.get())
            handler.add "api", nmp.arcDependency(mindustryVersion.get())
        }
    }

    @Deprecated(forRemoval = true)
    void genericInit() {
        if (checkConfigure this::genericInit) return

        genericModInit(false)
        attachedProject.logger.error("genericInit() will be removed in feature. Use genericModInit() instead.")
    }
}
