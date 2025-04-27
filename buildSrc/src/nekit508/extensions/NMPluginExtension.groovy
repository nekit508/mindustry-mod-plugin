package nekit508.extensions

import nekit508.NMPlugin
import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.gradle.util.internal.ConfigureUtil

abstract class NMPluginExtension<T> {
    ObjectFactory factory
    NMPlugin nmp

    private boolean configureConf = false, settingsConf = false

    NMPluginExtension(NMPlugin plugin) {
        nmp = plugin
        nmp.extensions.add this
    }

    /** Generally shouldn't be accessed manually */
    final List<Runnable> configureActions = new ArrayList<>()
    /** Generally shouldn't be accessed manually */
    final List<Runnable> settingsActions = new ArrayList<>()

    void checkSettings() {
        if (!settingsConf)
            throw new IllegalCallerException("Settings cannot be adjusted outside settings closure")
    }

    void checkConfigure() {
        if (!configureConf)
            throw new IllegalCallerException("Configuration methods cannot be executed outside configure closure")
    }

    void settingsI(Closure cl) {
        settingsActions.add () -> ConfigureUtil.configureSelf cl, this
    }

    void configureI(Closure cl) {
        println "add " + configureActions.modCount
        configureActions.add () -> ConfigureUtil.configureSelf cl, this
    }

    /** Stores settings adjustment action. */
    abstract <T> T configure(Closure closure)

    /** Stores configuration action. */
    abstract <T> T settings(Closure closure)

    void apply() {
        factory = nmp.project.objects
    }

    /** Internal method, not for manual use */
    void afterConfigure() {
        settingsConf = true
        settingsActions*.run()
        settingsConf = false

        configureConf = true
        for (final def action in configureActions) {
            println "iter " + configureActions.modCount
            action.run()
        }
        //configureActions*.run()
        configureConf = false
    }
}