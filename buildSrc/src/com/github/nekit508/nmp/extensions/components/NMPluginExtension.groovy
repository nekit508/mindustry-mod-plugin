package com.github.nekit508.nmp.extensions.components


import org.gradle.api.Project

abstract class NMPluginExtension {
    NMPluginExtension(String name, Project project) {
        if (project.extensions.findByName(name) != null)
            throw new IllegalArgumentException("Extension with same name already registered.")
        project.extensions.add name, this

        apply()
    }

    void apply() {

    }
}