package com.github.nekit508.extensions

import com.github.nekit508.NMPlugin
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.util.internal.ConfigureUtil

abstract class NMPluginExtension {
    ObjectFactory factory
    protected final NMPlugin nmp
    protected final Project attachedProject

    /**
     * If `true`, than method that must be executed in action/settings closure was called outside it,
     * will be stored as action and will not throw an exception.
     */
    protected boolean ignoreWrongMethodCalls = true

    /** Generally shouldn't be accessed manually */
    protected final List<Runnable> configureActions = new LinkedList<>()
    /** Generally shouldn't be accessed manually */
    protected final Queue<Runnable> settingsActions = new LinkedList<>()

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

    boolean checkSettings(Closure closure) {
        if (!nmp.settingsConf) {
            if (ignoreWrongMethodCalls) {
                settingsActions.add () -> closure()
                attachedProject.logger.warn "Held inappropriate settings method in extension $this"
            } else
                throw new Exception("Settings cannot be adjusted outside settings closure")
            return true
        }
        return false
    }

    boolean checkConfigure(Closure closure) {
        if (!nmp.configureConf) {
            if (ignoreWrongMethodCalls) {
                configureActions.add () -> closure()
                attachedProject.logger.warn "Held inappropriate configuration method in extension $this"
                return true
            } else
                throw new Exception("Configuration methods cannot be executed outside configure closure")

        }
        return false
    }

    /**
     * This method should be used by {@link com.github.nekit508.extensions.NMPluginExtension#settings(groovy.lang.Closure)}
     * implementation to add settings closure in query.
     */
    protected void settingsI(Closure cl) {
        settingsActions.add () -> ConfigureUtil.configureSelf cl, this
    }

    /**
     * Stores settings adjustment action. Must use {@link com.github.nekit508.extensions.NMPluginExtension#settingsI(groovy.lang.Closure)}
     * to add settings closure in query.
     */
    abstract <T> T settings(Closure closure)

    /**
     * This method should be used by {@link com.github.nekit508.extensions.NMPluginExtension#configure(groovy.lang.Closure)}
     * implementation to add configure closure in query.
     */
    protected void configureI(Closure cl) {
        configureActions.add () -> ConfigureUtil.configureSelf cl, this
    }

    /**
     * Stores settings adjustment action. Must use {@link com.github.nekit508.extensions.NMPluginExtension#configureI(groovy.lang.Closure)}
     * to add configure closure in query.
     */
    abstract <T> T configure(Closure closure)

    void configureTasks(@DelegatesTo(TaskContainer) Closure closure) {
        if (checkConfigure(closure)) return

        ConfigureUtil.configureSelf closure, attachedProject.tasks
    }

    /** There you can initialize all your properties. */
    void apply() {
        factory = nmp.project.objects
    }

    /** Do not use manually. */
    List<Runnable> popSettingsActions() {
        var out = settingsActions.asImmutable()
        out.reverse()
        settingsActions.clear()
        out
    }

    /** Do not use manually. */
    List<Runnable> popConfigureActions() {
        var out = configureActions.asImmutable()
        configureActions.clear()
        out
    }

    /** Do not use manually. */
    void clearActions() {
        configureActions.clear()
        settingsActions.clear()
    }

    Object prop(String name) {
        attachedProject.properties[name]
    }
}