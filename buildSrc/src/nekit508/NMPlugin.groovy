package nekit508

import groovy.json.JsonSlurper
import nekit508.tasks.BuildReleaseTask
import nekit508.tasks.BuildTask
import nekit508.tasks.CopyBuildReleaseTask
import nekit508.tasks.DelegatorTask
import nekit508.tasks.DexTask
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.tasks.compile.JavaCompile

class NMPlugin implements Plugin<Project> {
    Project project

    String mindutsryVersion = "v146"

    void parseSettings() {
        var localFile = project.file("settings/local.json")
        project.extensions.add "local", localFile.exists() ? new JsonSlurper().parse(localFile) : new HashMap<String, Object>()
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

            task.doFirst {
                project.delete task.options.generatedSourceOutputDirectory.get().asFile.listFiles()
            }
        }
    }

    void initTasks() {
        project.tasks.register "nmpBuild", BuildTask.class, this
        project.tasks.register "nmpDex", DexTask.class, this
        project.tasks.register "nmpBuildRelease", BuildReleaseTask.class, this
        project.tasks.register "nmpCopyBuildRelease", CopyBuildReleaseTask.class, this
    }

    /** Add tasks with old names. */
    void enableLegacy() {
        project.tasks.register "copyBuildRelease", DelegatorTask.class, project.tasks.nmpCopyBuildRelease
        project.tasks.register "buildRelease", DelegatorTask.class, project.tasks.nmpBuildRelease
    }

    void modBaseDependencies() {
        project.dependencies { DependencyHandler handler ->
            handler.add("compileOnly", mindustryDependency())
            handler.add("compileOnly", arcDependency())
        }
    }

    String mindustryDependency(String module = "core") {
        return dependency("com.github.Anuken.Mindustry", module, mindutsryVersion)
    }

    String arcDependency(String module = "arc-core") {
        return dependency("com.github.Anuken.Arc", module, mindutsryVersion)
    }

    String dependency(String dep, String module, String version) {
        return "$dep:$module:$version"
    }

    void genericInit(String mindustryVersion, boolean createLegacyTasks = false) {
        this.mindutsryVersion = mindustryVersion

        parseSettings()
        configureCompileTask()
        initTasks()
        if (createLegacyTasks) enableLegacy()
        modBaseDependencies()
    }

    @Override
    void apply(Project target) {
        project = target

        target.extensions.nmp = this
    }
}
