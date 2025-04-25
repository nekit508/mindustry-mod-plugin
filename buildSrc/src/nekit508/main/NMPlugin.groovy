package nekit508.main

import groovy.json.JsonSlurper
import nekit508.anno.NMPAnnoPlugin
import nekit508.main.tasks.BuildReleaseTask
import nekit508.main.tasks.BuildTask
import nekit508.main.tasks.CopyBuildReleaseTask
import nekit508.main.tasks.DelegatorTask
import nekit508.main.tasks.DexTask
import nekit508.main.tasks.GenerateModInfoTask
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.provider.Property
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.util.Configurable
import org.gradle.util.internal.ConfigureUtil

import javax.annotation.Nullable

class NMPlugin implements Plugin<Project> {
    NMPluginSettings settings

    Project project

    Map<String, Object> local = new LinkedHashMap<>()

    @Nullable NMPAnnoPlugin nmpa

    void parseSettings() {
        var localFile = project.file("settings/local.json")

        if (localFile.exists())
            local += new JsonSlurper().parse(localFile)
    }

    void configureCompileTask() {
        project.tasks.compileJava { JavaCompile task ->
            task.options.encoding = "UTF-8"
            task.options.generatedSourceOutputDirectory.set project.file("gen")

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


        }
    }

    void setupJabel() {
        project.tasks.compileJava { JavaCompile task ->
            task.sourceCompatibility = settings.sourceCompatibility.get().majorVersion

            task.options.compilerArgs = [
                    "--release", "8",
                    "--enable-preview",
                    "-Xlint:-options"
            ]

            task.doFirst {
                project.delete task.options.generatedSourceOutputDirectory.get().asFile.listFiles()

                task.options.compilerArgs = task.options.compilerArgs.findAll {
                    it != "--enable-preview"
                }
            }
        }

        project.dependencies { DependencyHandler handler ->
            handler.add "annotationProcessor", "com.github.bsideup.jabel:jabel-javac-plugin:${settings.jabelVersion.get()}"
        }
    }

    void initTasks() {
        project.tasks.register "nmpBuild", BuildTask, this
        project.tasks.register "nmpDex", DexTask, this
        project.tasks.register "nmpBuildRelease", BuildReleaseTask, this
        project.tasks.register "nmpCopyBuildRelease", CopyBuildReleaseTask, this
        project.tasks.register "nmpGenerateModInfo", GenerateModInfoTask, this
    }

    /** Add tasks with old names. */
    void enableLegacy() {
        project.tasks.register "copyBuildRelease", DelegatorTask, project.tasks.nmpCopyBuildRelease
        project.tasks.register "buildRelease", DelegatorTask, project.tasks.nmpBuildRelease
    }

    void modBaseDependencies() {
        project.dependencies { DependencyHandler handler ->
            handler.add "compileOnly", mindustryDependency()
            handler.add "compileOnly", arcDependency()
        }
    }

    void setupProjectAsAnnoProject(Project project) {
        project.apply {
            plugin NMPAnnoPlugin
        }

        nmpa = project.extensions.nmpa
        nmpa.genericInit()

        this.project.dependencies { DependencyHandler handler ->
            handler.add "compileOnly", project
            handler.add "annotationProcessor", project
        }
    }

    String mindustryDependency(String module = "core") {
        return dependency("com.github.Anuken.Mindustry", module, settings.mindustryVersion.get())
    }

    String arcDependency(String module = "arc-core") {
        return dependency("com.github.Anuken.Arc", module, settings.mindustryVersion.get())
    }

    @SuppressWarnings("GrMethodMayBeStatic")
    String dependency(String dep, String module, String version) {
        return "$dep:$module:$version"
    }

    void genericInit(boolean createLegacyTasks = false) {
        project.group = settings.modGroup.getOrElse project.group
        project.version = settings.modVersion.getOrElse project.version

        parseSettings()
        configureCompileTask()
        setupJabel()
        initTasks()
        if (createLegacyTasks) enableLegacy()
        modBaseDependencies()
    }

    @Override
    void apply(Project target) {
        project = target

        project.extensions.nmp = this

        settings = new NMPluginSettings()
    }

    /** For easier in-closure configure. */
    <T> T prop(Object property) {
        return (T) project.properties.get(property)
    }

    class NMPluginSettings implements Configurable<NMPluginSettings> {
        Property<String> mindustryVersion, modName, modVersion, modGroup, jabelVersion
        Property<Boolean> generateModInfo
        Property<JavaVersion> sourceCompatibility

        NMPluginSettings() {
            var factory = project.getObjects()

            mindustryVersion = factory.property String
            mindustryVersion.set "v146"
            modName = factory.property String
            modVersion = factory.property String
            modGroup = factory.property String
            jabelVersion = factory.property String
            jabelVersion.set "1.0.0"

            generateModInfo = factory.property Boolean
            generateModInfo.set false

            sourceCompatibility = factory.property JavaVersion
            sourceCompatibility.set JavaVersion.VERSION_20
        }

        @Override
        NMPluginSettings configure(Closure cl) {
            return ConfigureUtil.configureSelf(cl, this)
        }
    }
}