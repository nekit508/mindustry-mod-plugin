package nekit508.tools

import nekit508.main.NMPlugin
import nekit508.tools.tasks.RunToolsTask
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.provider.Property
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.util.Configurable
import org.gradle.util.internal.ConfigureUtil

class NMPToolsPlugin implements Plugin<Project> {
    NMPToolsPluginSettings settings

    Project project
    NMPlugin nmp

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
        project.tasks.register "nmptRunTools", RunToolsTask, this
    }

    void genericInit() {
        configureCompileTask()
        setupJabel()
        initTasks()
    }

    @Override
    void apply(Project target) {
        project = target

        project.extensions.nmpt = this
        nmp = Objects.requireNonNull project.parent.extensions.nmp, "NMPPlugin must be applied to parent project"

        settings = new NMPToolsPluginSettings()
    }

    class NMPToolsPluginSettings implements Configurable<NMPToolsPluginSettings> {
        Property<String> jabelVersion
        Property<JavaVersion> sourceCompatibility

        NMPToolsPluginSettings() {
            var factory = project.getObjects()

            jabelVersion = factory.property String
            jabelVersion.set nmp.settings.jabelVersion

            sourceCompatibility = factory.property JavaVersion
            sourceCompatibility.set nmp.settings.sourceCompatibility
        }

        @Override
        NMPToolsPluginSettings configure(@DelegatesTo(NMPToolsPluginSettings) Closure cl) {
            return ConfigureUtil.configureSelf(cl, this)
        }
    }
}
