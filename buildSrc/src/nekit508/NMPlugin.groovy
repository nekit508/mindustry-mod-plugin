package nekit508

import groovy.json.JsonSlurper
import nekit508.tasks.BuildReleaseTask
import nekit508.tasks.BuildTask
import nekit508.tasks.CopyBuildReleaseTask
import nekit508.tasks.DexTask
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile

class NMPlugin implements Plugin<Project> {
    public Project project

    void parseSettings() {
        var localFile = project.file("settings/local.json")
        var globalFile = project.file("settings/global.json")
        project.extensions.add "local", localFile.exists() ? new JsonSlurper().parse(localFile) : null
        project.extensions.add "global", globalFile.exists() ? new JsonSlurper().parse(globalFile) : null
    }

    void configureCompileTask() {
        project.tasks.compileJava { JavaCompile task ->
            task.targetCompatibility = JavaVersion.VERSION_1_8
            task.sourceCompatibility = JavaVersion.VERSION_20

            task.options.encoding = "UTF-8"
            task.options.generatedSourceOutputDirectory.set project.file("gen")
            task.options.compilerArgs += [ "-Xlint:none", "--release", "8"]

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
        project.getTasks().register "nmpBuild", BuildTask.class, this
        project.getTasks().register "nmpDex", DexTask.class, this
        project.getTasks().register "nmpBuildRelease", BuildReleaseTask.class, this
        project.getTasks().register "nmpCopyBuildRelease", CopyBuildReleaseTask.class, this
    }

    @Override
    void apply(Project target) {
        project = target

        target.extensions.add "mmp", this
    }
}
