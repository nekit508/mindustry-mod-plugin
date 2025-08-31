package com.github.nekit508.nmp.extensions

import com.github.nekit508.nmp.NMPlugin
import com.github.nekit508.nmp.Utils
import com.github.nekit508.nmp.tasks.core.BuildTask
import com.github.nekit508.nmp.tasks.entityanno.FetchComponentsTask
import com.github.nekit508.nmp.tasks.entityanno.ProcessComponentsTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileTreeElement
import org.gradle.api.provider.Property
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.compile.JavaCompile

class NMPluginEntityAnnoExtension extends NMPluginExtension {
    //Property<String> kotlinKaptPluginName

    Property<String> genPackage, fetchedCompsPackage, modCompsPackage
    DirectoryProperty revisionsDir
    DirectoryProperty fetchedCompsDir

    Property<String> entityAnnoVersion

    final NMPluginCoreExtension core

    NMPluginEntityAnnoExtension(String name, Project project, NMPlugin plugin, NMPluginCoreExtension core) {
        super(name, project, plugin)
        this.core = core

        if (core.attachedProject != attachedProject)
            throw new GradleException("Entity anno extension must be applied to the same project as core extension.")
    }

    @Override
    NMPluginEntityAnnoExtension settings(Closure closure) {
        settingsI closure
        return this
    }

    @Override
    NMPluginEntityAnnoExtension configure(Closure closure) {
        configureI closure
        return this
    }

    @Override
    void apply() {
        super.apply()

        //kotlinKaptPluginName = factory.property String

        genPackage = factory.property String
        revisionsDir = factory.directoryProperty()
        fetchedCompsPackage = factory.property String
        entityAnnoVersion = factory.property String
        fetchedCompsDir = factory.directoryProperty()
        modCompsPackage = factory.property String

        settings {
            //kotlinKaptPluginName.set "kotlin-kapt"

            fetchedCompsPackage.set attachedProject.provider { "${genPackage.get()}.comps.fetched" }
            revisionsDir.set attachedProject.layout.projectDirectory.dir("revisions")
            fetchedCompsDir.set attachedProject.layout.projectDirectory.dir("fetchedComps")
            modCompsPackage.set attachedProject.provider { "${genPackage.get()}.comps" }
        }
    }

    void genericInit(boolean excludeFetchedComponents = true) {
        if (checkConfigure this::genericInit) return

        initTasks()
        configureFetchedComps()
        addEntityAnnoRepo()
        setupDependencies()
        configureAnnotationProcessor()

        if (excludeFetchedComponents)
            excludeCompsFromBuild()
    }

    void initTasks() {
        if (checkConfigure this::initTasks) return

        attachedProject.tasks.register "nmpeaFetchComps", FetchComponentsTask, this
        attachedProject.tasks.register "nmpeaProcessComps", ProcessComponentsTask, this

        attachedProject.tasks.compileJava.dependsOn attachedProject.tasks.nmpeaFetchComps
        attachedProject.tasks.compileJava.dependsOn attachedProject.tasks.nmpeaProcessComps
    }

    void addEntityAnnoRepo() {
        attachedProject.repositories {
            maven { url "https://raw.githubusercontent.com/GglLfr/EntityAnnoMaven/main" }
        }
    }

    void setupDependencies() {
        attachedProject.dependencies { handler ->
            handler.compileOnly "com.github.GglLfr.EntityAnno:entity:${entityAnnoVersion.get()}"
            handler.annotationProcessor "com.github.GglLfr.EntityAnno:entity:${entityAnnoVersion.get()}" // TODO use kapt
        }
    }

    void configureFetchedComps() {
        if (checkConfigure this::configureFetchedComps) return

        attachedProject.sourceSets.main.java.srcDirs += fetchedCompsDir
    }

    void excludeCompsFromBuild() {
        if (checkConfigure this::excludeCompsFromBuild) return

        attachedProject.tasks.nmpBuild.configure { BuildTask task ->
            task.exclude { FileTreeElement elem ->
                var compsPackages = [fetchedCompsPackage.get(), modCompsPackage.get()]*.replaceAll("\\.", "/")*.replaceAll("[/\\\\]", "/")
                return compsPackages.any { String packagee -> elem.path.replaceAll("[/\\\\]", "/").startsWith(packagee) }
            }
        }
    }

    void configureAnnotationProcessor() {
        //nmp.requirePlugin attachedProject, kotlinKaptPluginName.get()

        attachedProject.tasks.named("compileJava").configure {
            doFirst {
                Utils.annotationProcessorArgs attachedProject.tasks.named("compileJava") as TaskProvider<JavaCompile>,
                        [
                                "modName": core.modName.get(),
                                "genPackage": genPackage.get(),
                                "fetchPackage": fetchedCompsPackage.get(),
                                "revisionDir": revisionsDir.get().asFile.absolutePath
                        ]
            }
        }
    }
}
