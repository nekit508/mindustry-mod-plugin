package com.github.nekit508.nmp

import com.github.nekit508.nmp.extensions.NMPluginAnnoExtension
import com.github.nekit508.nmp.extensions.NMPluginCoreExtension
import com.github.nekit508.nmp.extensions.NMPluginEntityAnnoExtension
import com.github.nekit508.nmp.extensions.components.NMPluginExtension
import com.github.nekit508.nmp.extensions.NMPluginMMCAnnoExtension
import com.github.nekit508.nmp.extensions.NMPluginToolsExtension
import com.github.nekit508.nmp.lib.ScheduledActionsList
import groovy.json.JsonSlurper
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.RegularFile
import org.gradle.api.plugins.UnknownPluginException
import org.gradle.api.provider.Property

/**
 * Initialisation - properties and tasks creation <br>
 * Settings - properties configuration <br>
 * Configuration - tasks configuration <br>
 */
class NMPlugin implements Plugin<Project> {
    final Set<Project> evaluatedProjects = new LinkedHashSet<>()
    protected Property<Boolean> offlineMode
    protected Property<Boolean> autoOfflineMode
    protected Property<Integer> autoOfflineModeTimeout

    Property<RegularFile> localSettingsFile, defaultSettingsFile

    Project project

    Map<String, Object> nmpSettings = new LinkedHashMap<>()

    protected ScheduledActionsList initialisations, settings, configurations

    @Override
    void apply(Project target) {
        project = target

        localSettingsFile = project.objects.fileProperty()
        localSettingsFile.set project.file("settings/settings.local.json")
        localSettingsFile.finalizeValue()

        defaultSettingsFile = project.objects.fileProperty()
        defaultSettingsFile.set project.file("settings/settings.json")

        project.allprojects.each { it.extensions.nmp = this }
        parseSettings()

        project.allprojects.each { Project it ->
            it.afterEvaluate {
                evaluatedProjects.add it

                if (evaluatedProjects == this.project.allprojects)
                    afterEvaluate()
            }
        }

        offlineMode = project.objects.property Boolean.class
        autoOfflineMode = project.objects.property Boolean.class
        autoOfflineModeTimeout = project.objects.property Integer.class

        if (nmpSettings?.offlineMode != null && nmpSettings?.autoOfflineMode)
            project.logger.warn "warning: probably misconfiguration: offlineMode is set when autoOfflineMode is true"

        if (nmpSettings?.autoOfflineMode != true && nmpSettings?.autoOfflineModeTimeout != null)
            project.logger.warn "warning: probably misconfiguration: autoOfflineModeTimeout is set when autoOfflineMode is false"

        initialisations = new ScheduledActionsList()
        settings = new ScheduledActionsList()
        configurations = new ScheduledActionsList()

        configuration { // do it right after settings
            offlineMode.set((nmpSettings?.offlineMode ?: false) as Boolean)
            autoOfflineMode.set((nmpSettings?.autoOfflineMode ?: nmpSettings?.offlineMode == null) as Boolean)
            autoOfflineModeTimeout.set((nmpSettings?.autoOfflineModeTimeout ?: 5000) as Integer)

            autoOfflineMode.finalizeValue()
            if (autoOfflineMode.get()) {
                project.logger.lifecycle "Automatically proving internet connection."

                var addresses = ["google.com", "github.com"]
                autoOfflineModeTimeout.finalizeValue()
                int timeout = autoOfflineModeTimeout.get()

                offlineMode.set !addresses.any {
                    try {
                        return InetAddress.getByName(it).isReachable(timeout)
                    } catch (UnknownHostException ignored) {
                        offlineMode.set true
                    } catch (IOException ignored) {
                        offlineMode.set true
                    }
                }

                offlineMode.finalizeValue()
            }
            project.logger.lifecycle "Working in ${isOffline() ? "offline" : "online"}."
        }
    }

    void finalizeProperty(Property<?>... props) {
        props.each {
            project.logger.debug("finalizing $it")
            it.finalizeValue()
        }
    }

    boolean isOffline() {
        return offlineMode.get()
    }

    boolean isOnline() {
        return !offlineMode.get()
    }

    Property<Boolean> autoOfflineMode() {
        return autoOfflineMode
    }

    Property<Boolean> offlineMode() {
        return offlineMode
    }

    ScheduledActionsList initialisation() {
        initialisations
    }

    ScheduledActionsList setting() {
        settings
    }

    ScheduledActionsList configuration() {
        configurations
    }

    ScheduledActionsList initialisation(Closure closure) {
        initialisations + closure
    }

    ScheduledActionsList setting(Closure closure) {
        settings + closure
    }

    ScheduledActionsList configuration(Closure closure) {
        configurations + closure
    }

    void parseSettings() {
        var parser = new JsonSlurper();

        defaultSettingsFile.finalizeValue()
        localSettingsFile.finalizeValue()

        var localFile = localSettingsFile.get().asFile
        var defaultFile = defaultSettingsFile.get().asFile

        if (defaultFile.exists())
            addMap nmpSettings, parser.parse(defaultFile) as Map<String, Object>

        if (localFile.exists())
            addMap nmpSettings, parser.parse(localFile) as Map<String, Object>
        else {
            var fallback = project.file("settings/local.json")
            if (fallback.exists() ) {
                addMap nmpSettings, parser.parse(fallback) as Map<String, Object>
                project.logger.warn "warning: Using fallback $fallback.absolutePath. Use $localFile.absolutePath instead."
            }
        }
    }

    <T> void addMap(Map<String, T> a, Map<String, T> b) {
        b.forEach { k, v ->
            if (a.containsKey(k)) {
                var av = a[k]

                if (av instanceof Map && v instanceof Map) {
                    addMap(av, v)
                } else if (av instanceof List && v instanceof List) {
                    av.addAll(v as Iterable)
                } else {
                    a[k] = v
                }
            } else
                a[k] = v
        }
    }

    void afterEvaluate() {
        project.logger.lifecycle "[NMPlugin] Scheduling post evaluation actions (${setting().size}:${initialisation().size}:${configuration().size})"

        project.logger.lifecycle "[NMPlugin] Settings (${setting().size})"
        setting().schedule()

        project.logger.lifecycle "[NMPlugin] Initalization (${initialisation().size})"
        initialisation().schedule()

        project.logger.lifecycle "[NMPlugin] Configuaration (${configuration().size})"
        configuration().schedule()

        project.logger.lifecycle "[NMPlugin] Post evaluation actions are scheduled (${setting().size}:${initialisation().size}:${configuration().size})"
    }

    @SuppressWarnings('GrMethodMayBeStatic')
    String mindustryDependency(String version, String module = "core") {
        return dependency("com.github.Anuken.Mindustry", module, version)
    }

    @SuppressWarnings('GrMethodMayBeStatic')
    String arcDependency(String version, String module = "arc-core") {
        return dependency("com.github.Anuken.Arc", module, version)
    }

    @SuppressWarnings('GrMethodMayBeStatic')
    String dependency(String dep, String module, String version) {
        return "$dep:$module:$version"
    }

    @SuppressWarnings('GrMethodMayBeStatic')
    NMPluginCoreExtension core(Project project, String name, boolean publishable = false, String group = "") {
        //new NMPluginCoreExtension(name, project, this, publishable, group)
        project.objects.newInstance(NMPluginCoreExtension, name, project, publishable, group)
    }

    @SuppressWarnings('GrMethodMayBeStatic')
    NMPluginAnnoExtension anno(Project project, String name, NMPluginCoreExtension core) {
        //new NMPluginAnnoExtension(name, project, this, core)
        project.objects.newInstance(NMPluginAnnoExtension, name, project, core)
    }

    @SuppressWarnings('GrMethodMayBeStatic')
    NMPluginToolsExtension tools(Project project, String name, NMPluginCoreExtension core) {
        //new NMPluginToolsExtension(name, project, this, core)
        project.objects.newInstance(NMPluginToolsExtension, name, project, core)
    }

    @SuppressWarnings('GrMethodMayBeStatic')
    NMPluginEntityAnnoExtension entityAnno(Project project, String name, NMPluginCoreExtension core, boolean excludeComponents = true) {
        //new NMPluginEntityAnnoExtension(name, project, this, core, excludeComponents)
        project.objects.newInstance(NMPluginEntityAnnoExtension, name, project, core, excludeComponents)
    }

    NMPluginMMCAnnoExtension mmcAnno(Project project, String name, NMPluginCoreExtension core) { new NMPluginMMCAnnoExtension(name, project, this, core) }

    void configureProjectDataForJitpackBuilding(String group) {
        project.allprojects { Project p ->
            var path = p.path
            var forcedGroup = path.length() == 1 ? group : ("$p.parent.group.$p.parent.name")
            var isJitpackBuild = Boolean.parseBoolean(System.getenv("JITPACK") ?: "false")

            if (isJitpackBuild) {
                p.version = System.getenv("VERSION")
                p.group = forcedGroup

                println """jitpack build info $p\n    version: $p.version\n    group: $p.group"""
            }
        }
    }

    @SuppressWarnings('GrMethodMayBeStatic')
    void requirePlugin(Project project, String pluginId) {
        try {
            project.plugins.getPlugin(pluginId)
        } catch (UnknownPluginException ignored) {
            throw new GradleException("Required plugin $pluginId was not founded in project $project")
        }
    }
}
