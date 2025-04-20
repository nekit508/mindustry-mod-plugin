package nekit508

import groovy.json.JsonSlurper
import nekit508.tasks.BuildReleaseTask
import nekit508.tasks.BuildTask
import nekit508.tasks.CopyBuildReleaseTask
import nekit508.tasks.DelegatorTask
import nekit508.tasks.DexTask
import nekit508.tasks.GenerateModInfo
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.tasks.compile.JavaCompile

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target
import java.lang.reflect.Field

class NMPlugin implements Plugin<Project> {
    Project project

    @DataProp
    String mindutsryVersion = "v146", modName, modVersion, modGroup, jabelVersion = "1.0.0"
    @DataProp
    boolean generateModInfo = false
    @DataProp
    JavaVersion sourceCompatibility = JavaVersion.VERSION_20

    Map<String, Object> local = new LinkedHashMap<>()

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
            task.sourceCompatibility = sourceCompatibility.majorVersion

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
            handler.add "annotationProcessor", "com.github.bsideup.jabel:jabel-javac-plugin:$jabelVersion"
        }
    }

    void initTasks() {
        project.tasks.register "nmpBuild", BuildTask, this
        project.tasks.register "nmpDex", DexTask, this
        project.tasks.register "nmpBuildRelease", BuildReleaseTask, this
        project.tasks.register "nmpCopyBuildRelease", CopyBuildReleaseTask, this
        project.tasks.register "nmpGenerateModInfo", GenerateModInfo, this
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

    String mindustryDependency(String module = "core") {
        return dependency("com.github.Anuken.Mindustry", module, mindutsryVersion)
    }

    String arcDependency(String module = "arc-core") {
        return dependency("com.github.Anuken.Arc", module, mindutsryVersion)
    }

    @SuppressWarnings('GrMethodMayBeStatic')
    String dependency(String dep, String module, String version) {
        return "$dep:$module:$version"
    }

    void setProps(Map<String, Object> data) {
        for (Field field : getClass().getDeclaredFields()) {
            var anno = field.getAnnotation(DataProp)

            if (anno == null) continue

            var key = anno.key()
            if (key == "") key = field.name

            if (data.containsKey(key)) {
                field.setAccessible true
                field.set this, data[key]
                field.setAccessible false
            }
        }
    }

    void genericInit(boolean createLegacyTasks = false) {
        project.group = modGroup ?: project.group
        project.version = modVersion ?: project.version

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

        target.extensions.nmp = this
    }

    /** For easier in-closure configure. */
    Object prop(Object property) {
        project.properties.get(property)
    }
}

/** Internal annotation. */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@interface DataProp {
    String key() default ""
}