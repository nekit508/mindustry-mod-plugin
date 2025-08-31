package com.github.nekit508.nmp

import com.github.nekit508.nmp.extensions.NMPluginAnnoExtension
import com.github.nekit508.nmp.extensions.NMPluginCoreExtension
import com.github.nekit508.nmp.extensions.NMPluginEntityAnnoExtension
import com.github.nekit508.nmp.extensions.NMPluginExtension
import com.github.nekit508.nmp.extensions.NMPluginMMCAnnoExtension
import com.github.nekit508.nmp.extensions.NMPluginToolsExtension
import groovy.json.JsonSlurper

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.UnknownPluginException

class NMPlugin implements Plugin<Project> {
    final Set<Project> evaluatedProjects = new LinkedHashSet<>()
    private boolean configured = false

    final List<NMPluginExtension> extensions = new LinkedList<>()

    Project project

    Map<String, Object> local = new LinkedHashMap<>()

    /** Do not set manually. */
    boolean configureConf = false, settingsConf = false

    @Override
    void apply(Project target) {
        project = target

        project.allprojects.each {it.extensions.nmp = this }
        parseSettings()

        project.allprojects.each {
            it.afterEvaluate {
                evaluatedProjects.add it

                if (evaluatedProjects == this.project.allprojects)
                    afterConfigure()
            }
        }
    }

    void parseSettings() {
        var localFile = project.file("settings/local.json")

        if (localFile.exists())
            local += new JsonSlurper().parse(localFile)
    }

    void afterConfigure() {
        if (configured) {
            println "afterConfigure executed not first time!"
            return
        }

        List<Runnable> settingsActions = new LinkedList<>(), configureActions = new LinkedList<>()

        while (true) {
            for (final def ext in extensions)
                settingsActions.addAll ext.popSettingsActions()

            if (settingsActions.empty)
                break

            settingsConf = true
            settingsActions*.run()
            settingsConf = false

            settingsActions.clear()
        }

        while (true) {
            for (final def ext in extensions)
                configureActions.addAll ext.popConfigureActions()

            if (configureActions.empty)
                break

            configureConf = true
            configureActions*.run()
            configureConf = false

            configureActions.clear()
        }

        configured = true
    }

    String mindustryDependency(String version, String module = "core") {
        return dependency("com.github.Anuken.Mindustry", module, version)
    }

    String arcDependency(String version, String module = "arc-core") {
        return dependency("com.github.Anuken.Arc", module, version)
    }

    @SuppressWarnings("GrMethodMayBeStatic")
    String dependency(String dep, String module, String version) {
        return "$dep:$module:$version"
    }

    NMPluginCoreExtension core(Project project, String name) { new NMPluginCoreExtension(name, project, this) }

    NMPluginAnnoExtension anno(Project project, String name, NMPluginCoreExtension core) { new NMPluginAnnoExtension(name, project, this, core) }

    NMPluginToolsExtension tools(Project project, String name, NMPluginCoreExtension core) { new NMPluginToolsExtension(name, project, this, core) }

    NMPluginEntityAnnoExtension entityAnno(Project project, String name, NMPluginCoreExtension core) { new NMPluginEntityAnnoExtension(name, project, this, core) }

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
