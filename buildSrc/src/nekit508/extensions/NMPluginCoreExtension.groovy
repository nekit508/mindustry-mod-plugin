package nekit508.extensions

import nekit508.NMPlugin
import org.gradle.api.JavaVersion
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.provider.Property
import org.gradle.api.tasks.compile.JavaCompile

class NMPluginCoreExtension extends NMPluginExtension {
    Property<String> mindustryVersion, modName, modVersion, modGroup, jabelVersion
    Property<Boolean> generateModInfo
    Property<JavaVersion> sourceCompatibility

    NMPluginCoreExtension(NMPlugin plugin) {
        super(plugin)
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

    void configureCompileTask() {
        checkConfigure()

        configureActions.add () -> {
            nmp.project.tasks.compileJava { JavaCompile task ->
                task.options.encoding = "UTF-8"
                task.options.generatedSourceOutputDirectory.set nmp.project.file("gen")

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
    }

    void setupJabel() {
        checkConfigure()

        configureActions.add () -> {
            nmp.project.tasks.compileJava { JavaCompile task ->
                task.sourceCompatibility = sourceCompatibility.get().majorVersion

                task.options.compilerArgs = [
                        "--release", "8",
                        "--enable-preview",
                        "-Xlint:-options"
                ]

                task.doFirst {
                    nmp.project.delete task.options.generatedSourceOutputDirectory.get().asFile.listFiles()

                    task.options.compilerArgs = task.options.compilerArgs.findAll {
                        it != "--enable-preview"
                    }
                }
            }

            nmp.project.dependencies { DependencyHandler handler ->
                handler.add "annotationProcessor", "com.github.bsideup.jabel:jabel-javac-plugin:${jabelVersion.get()}"
            }
        }
    }

    void modBaseDependencies() {
        checkConfigure()

        nmp.project.dependencies { DependencyHandler handler ->
            handler.add "compileOnly", nmp.mindustryDependency(mindustryVersion.get())
            handler.add "compileOnly", nmp.arcDependency(mindustryVersion.get())
        }
    }
}
