package com.github.nekit508.nmp

import com.github.nekit508.nmp.extensions.Common
import com.github.nekit508.nmp.extensions.NMPluginAnnoExtension
import com.github.nekit508.nmp.extensions.NMPluginCoreExtension
import com.github.nekit508.nmp.extensions.NMPluginEntityAnnoExtension
import com.github.nekit508.nmp.extensions.NMPluginExtension
import com.github.nekit508.nmp.extensions.NMPluginMMCAnnoExtension
import com.github.nekit508.nmp.extensions.NMPluginToolsExtension
import com.github.nekit508.nmp.lib.ScheduledActionsList
import groovy.json.JsonSlurper

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
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

    final List<NMPluginExtension> extensions = new LinkedList<>()

    Project project

    Map<String, Object> local = new LinkedHashMap<>()

    protected ScheduledActionsList initialisations, settings, configurations

    @Override
    void apply(Project target) {
        project = target

        project.allprojects.each { it.extensions.nmp = this }
        parseSettings()

        project.allprojects.each {
            it.afterEvaluate {
                evaluatedProjects.add it

                if (evaluatedProjects == this.project.allprojects)
                    afterEvaluate()
            }
        }

        offlineMode = project.objects.property Boolean.class
        autoOfflineMode = project.objects.property Boolean.class
        autoOfflineModeTimeout = project.objects.property Integer.class

        offlineMode.set((local?.offlineMode ?: false) as Boolean)
        autoOfflineMode.set((local?.autoOfflineMode ?: local?.offlineMode == null) as Boolean)
        autoOfflineModeTimeout.set((local?.autoOfflineModeTimeout ?: 5000) as Integer)

        initialisations = new ScheduledActionsList()
        settings = new ScheduledActionsList()
        configurations = new ScheduledActionsList()

        configuration { // do it right after settings
            autoOfflineMode.finalizeValue()
            if (autoOfflineMode.get()) {
                project.logger.lifecycle "Automatically proving internet connection."

                try {
                    InetAddress google = InetAddress.getByName("google.com"), github = InetAddress.getByName("github.com")
                    autoOfflineModeTimeout.finalizeValue()
                    int timeout = autoOfflineModeTimeout.get()

                    if (google.isReachable(timeout) || github.isReachable(timeout))
                        offlineMode.set false
                    else offlineMode.set true
                } catch (UnknownHostException ignored) {
                    offlineMode.set true
                } catch (IOException ignored) {
                    offlineMode.set true
                }

                offlineMode.finalizeValue()
            }
            project.logger.lifecycle "Working in ${isOffline() ? "offline" : "online"}."
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
        var localFile = project.file("settings/local.json")

        if (localFile.exists())
            local += new JsonSlurper().parse(localFile)
    }

    void afterEvaluate() {
        initialisation().schedule()
        setting().schedule()
        configuration().schedule()
    }

    static String mindustryDependency(String version, String module = "core") {
        return Common.mindustryDependency(version, module)
    }

    static String arcDependency(String version, String module = "arc-core") {
        return Common.arcDependency(version, module)
    }

    static String dependency(String dep, String module, String version) {
        return Common.dependency(dep, module, version)
    }

    NMPluginCoreExtension core(Project project, String name, boolean publishable = false, String group = null) { new NMPluginCoreExtension(name, project, this, publishable, group) }

    NMPluginAnnoExtension anno(Project project, String name, NMPluginCoreExtension core) { new NMPluginAnnoExtension(name, project, this, core) }

    NMPluginToolsExtension tools(Project project, String name, NMPluginCoreExtension core) { new NMPluginToolsExtension(name, project, this, core) }

    NMPluginEntityAnnoExtension entityAnno(Project project, String name, NMPluginCoreExtension core, boolean excludeComponents = true) { new NMPluginEntityAnnoExtension(name, project, this, core, excludeComponents) }

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

    void requirePlugin(Project project, String pluginId) {
        try {
            project.plugins.getPlugin(pluginId)
        } catch (UnknownPluginException ignored) {
            throw new GradleException("Required plugin $pluginId was not founded in project $project")
        }
    }
}
