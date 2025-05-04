package nekit508.extensions

import nekit508.NMPlugin
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.TaskContainer
import org.gradle.util.internal.ConfigureUtil

abstract class NMPluginExtension {
    ObjectFactory factory
    NMPlugin nmp
    Project attachedProject

    /**
     * If `true`, than method that must be executed in action/settings closure was called outside it,
     * will be stored as action and will not throw an exception.
     */
    boolean holdOverInappropriateMethods = true

    /** Generally shouldn't be accessed manually */
    private final List<Runnable> configureActions = new LinkedList<>()
    /** Generally shouldn't be accessed manually */
    private final Queue<Runnable> settingsActions = new LinkedList<>()

    NMPluginExtension(String name, Project project, NMPlugin plugin) {
        nmp = plugin
        attachedProject = project
        nmp.extensions.add this

        if (project.extensions.findByName(name) != null)
            throw new IllegalArgumentException("Extension with same name already registered.")
        project.extensions.add name, this

        apply()
    }

    boolean checkSettings(Closure closure) {
        if (!nmp.settingsConf) {
            if (holdOverInappropriateMethods)
                settingsActions.add () -> closure()
            else
                throw new IllegalCallerException("Settings cannot be adjusted outside settings closure")
            return true
        }
        return false
    }

    boolean checkConfigure(Closure closure) {
        if (!nmp.configureConf) {
            if (holdOverInappropriateMethods) {
                configureActions.add () -> closure()
                println "Held inappropriate method in extension $this"
                return true
            } else
                throw new IllegalCallerException("Configuration methods cannot be executed outside configure closure")

        }
        return false
    }

    /**
     * This method should be used by {@link nekit508.extensions.NMPluginExtension#settings(groovy.lang.Closure)}
     * implementation to add settings closure in query.
     */
    void settingsI(Closure cl) {
        settingsActions.add () -> ConfigureUtil.configureSelf cl, this
    }

    /**
     * Stores settings adjustment action. Must use {@link nekit508.extensions.NMPluginExtension#settingsI(groovy.lang.Closure)}
     * to add settings closure in query.
     */
    abstract <T> T settings(Closure closure)

    /**
     * This method should be used by {@link nekit508.extensions.NMPluginExtension#configure(groovy.lang.Closure)}
     * implementation to add configure closure in query.
     */
    void configureI(Closure cl) {
        configureActions.add () -> ConfigureUtil.configureSelf cl, this
    }

    /**
     * Stores settings adjustment action. Must use {@link nekit508.extensions.NMPluginExtension#configureI(groovy.lang.Closure)}
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

    void clearActions() {
        configureActions.clear()
        settingsActions.clear()
    }

    Object prop(String name) {
        attachedProject.properties[name]
    }
}