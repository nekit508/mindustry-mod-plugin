package com.github.nekit508.nmp.extensions


import com.github.nekit508.nmp.extensions.components.Default
import com.github.nekit508.nmp.extensions.components.NMPluginExtension
import com.github.nekit508.nmp.lib.Utils
import com.github.nekit508.nmp.tasks.core.BuildTask
import com.github.nekit508.nmp.tasks.entityanno.FetchComponentsTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileTreeElement
import org.gradle.api.provider.Property
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.compile.JavaCompile

abstract class NMPluginEntityAnnoExtension extends NMPluginExtension implements Default {
    // TODO why is kapt here?
    //Property<String> kotlinKaptPluginName

    Property<String> genPackage, fetchedCompsPackage, modCompsPackage
    DirectoryProperty revisionsDir
    DirectoryProperty fetchedCompsDir

    Property<String> entityAnnoVersion

    final NMPluginCoreExtension core

    NMPluginEntityAnnoExtension(String name, Project project, NMPluginCoreExtension core, boolean excludeComponents) {
        super(name, project)
        this.core = core

        if (core.project != project)
            throw new GradleException("Entity anno extension must be applied to the same project as core extension.")

        genericInit(excludeComponents)
    }

    @Override
    void apply() {
        super.apply()

        //kotlinKaptPluginName = objectFactory.property String

        genPackage = objectFactory.property String
        revisionsDir = objectFactory.directoryProperty()
        fetchedCompsPackage = objectFactory.property String
        entityAnnoVersion = objectFactory.property String
        fetchedCompsDir = objectFactory.directoryProperty()
        modCompsPackage = objectFactory.property String

        nmp.setting {
            //kotlinKaptPluginName.set "kotlin-kapt"

            fetchedCompsPackage.set project.provider { "${genPackage.get()}.comps.fetched" }
            revisionsDir.set project.layout.projectDirectory.dir("revisions")
            fetchedCompsDir.set project.layout.projectDirectory.dir("fetchedComps")
            modCompsPackage.set project.provider { "${genPackage.get()}.comps" }
        }
    }

    void genericInit(boolean excludeComponents) {
        nmp.initialisation {
            project.tasks.register "nmpeaFetchComps", FetchComponentsTask, this
        }

        nmp.configuration {
            project.tasks.compileJava.dependsOn project.tasks.nmpeaFetchComps

            project.repositories {
                maven { url "https://raw.githubusercontent.com/GglLfr/EntityAnnoMaven/main" }
            }

            project.dependencies { handler ->
                entityAnnoVersion.finalizeValue()
                handler.compileOnly "com.github.GglLfr.EntityAnno:entity:${entityAnnoVersion.get()}"
                handler.annotationProcessor "com.github.GglLfr.EntityAnno:entity:${entityAnnoVersion.get()}" // TODO use kapt
            }

            project.sourceSets.main.java.srcDirs += fetchedCompsDir
            if (excludeComponents)
                project.tasks.nmpBuild.configure { BuildTask task ->
                    task.exclude { FileTreeElement elem ->
                        fetchedCompsPackage.finalizeValue()
                        modCompsPackage.finalizeValue()
                        var compsPackages = [fetchedCompsPackage.get(), modCompsPackage.get()]*.replaceAll("\\.", "/")*.replaceAll("[/\\\\]", "/")
                        return compsPackages.any { String packagee -> elem.path.replaceAll("[/\\\\]", "/").startsWith(packagee) }
                    }
                }

            //nmp.requirePlugin project, kotlinKaptPluginName.get() // TODO use kapt

            project.tasks.named("compileJava").configure {
                doFirst {
                    Utils.annotationProcessorArgs project.tasks.named("compileJava") as TaskProvider<JavaCompile>,
                            [
                                    "modName"     : core.modName.get(),
                                    "genPackage"  : genPackage.get(),
                                    "fetchPackage": fetchedCompsPackage.get(),
                                    "revisionDir" : revisionsDir.get().asFile.absolutePath
                            ]
                }
            }
        }
    }
}
