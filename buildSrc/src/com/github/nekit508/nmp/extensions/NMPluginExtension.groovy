package com.github.nekit508.nmp.extensions

import com.github.nekit508.nmp.NMPlugin
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.FirstParam
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.TaskContainer
import org.gradle.util.internal.ConfigureUtil

abstract class NMPluginExtension {
    ObjectFactory factory
    protected final NMPlugin nmp
    protected final Project attachedProject

    NMPluginExtension(String name, Project project, NMPlugin plugin) {
        nmp = plugin
        attachedProject = project
        nmp.extensions.add this

        if (project.extensions.findByName(name) != null)
            throw new IllegalArgumentException("Extension with same name already registered.")
        project.extensions.add name, this

        apply()
    }

    NMPlugin nmp() {
        nmp
    }

    Project attachedProject() {
        attachedProject
    }

    @Deprecated(forRemoval = true)
    protected void settingsI(Closure cl) {
        ConfigureUtil.configureSelf cl, this
    }

    @Deprecated(forRemoval = true)
    abstract <T> T settings(Closure closure)

    @Deprecated(forRemoval = true)
    protected void configureI(Closure cl) {
        ConfigureUtil.configureSelf cl, this
    }

    @Deprecated(forRemoval = true)
    abstract <T> T configure(Closure closure)

    void configureTasks(@DelegatesTo(TaskContainer) Closure closure) {
        nmp.configuration { ConfigureUtil.configureSelf closure, attachedProject.tasks }
    }

    /** There you can initialize all your properties. */
    void apply() {
        factory = nmp.project.objects
    }

    Object prop(String name) {
        attachedProject.properties[name]
    }
}