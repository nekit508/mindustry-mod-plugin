package nekit508.extensions

import nekit508.NMPlugin
import nekit508.tasks.core.BuildReleaseTask
import nekit508.tasks.core.BuildTask
import nekit508.tasks.core.CopyBuildReleaseTask
import nekit508.tasks.core.DexTask
import nekit508.tasks.core.GenerateModInfoTask
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.compile.JavaCompile

class NMPluginCoreExtension extends NMPluginExtension {
    Property<String> mindustryVersion, modName, modVersion, modGroup, jabelVersion
    Property<Boolean> generateModInfo
    Property<JavaVersion> sourceCompatibility
    ListProperty<File> srcDirs, resDirs
    Property<File> genDir

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

        settings {
            genDir.set attachedProject.file("gen")
            resDirs.add attachedProject.file("res")
            srcDirs.add attachedProject.file("src")
            sourceCompatibility.set JavaVersion.VERSION_20
            generateModInfo.set false
            jabelVersion.set "1.0.0"
            mindustryVersion.set "v146"
        }
    }

    void initTasks() {
        attachedProject.tasks.register "nmpBuild", BuildTask, this
        attachedProject.tasks.register "nmpDex", DexTask, this
        attachedProject.tasks.register "nmpBuildRelease", BuildReleaseTask, this
        attachedProject.tasks.register "nmpCopyBuildRelease", CopyBuildReleaseTask, this
        attachedProject.tasks.register "nmpGenerateModInfo", GenerateModInfoTask, this
    }

    void configureCompileTask() {
        if (checkConfigure(this::configureCompileTask)) return

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
        if (checkConfigure(this::setupJabel)) return

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
        }
    }

    void modBaseDependencies() {
        if (checkConfigure(this::modBaseDependencies)) return

        attachedProject.dependencies { DependencyHandler handler ->
            handler.add "compileOnly", nmp.mindustryDependency(mindustryVersion.get())
            handler.add "compileOnly", nmp.arcDependency(mindustryVersion.get())
        }
    }

    void genericInit() {
        configureCompileTask()
        setupJabel()
        modBaseDependencies()
        initTasks()
    }
}
