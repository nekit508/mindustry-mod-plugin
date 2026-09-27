package com.github.nekit508.nmp.extensions

import com.github.nekit508.nmp.extensions.components.Component
import com.github.nekit508.nmp.extensions.components.Default
import org.gradle.api.Project

import java.lang.annotation.Annotation

abstract class NMPluginExtension implements Default {
    NMPluginExtension(String name, Project project) {
        if (project.extensions.findByName(name) != null)
            throw new IllegalArgumentException("Extension with same name already registered.")
        project.extensions.add name, this

        apply()
    }

    void apply() {
        initComponents()
    }

    void initComponents() {
        var processed = new LinkedHashSet()

        def init
        init = { Class<?> clazz ->
            if (clazz in processed)
                return
            processed.add clazz

            if (clazz.superclass != null) init clazz.superclass
            clazz.interfaces.each { init it }

            if (clazz.annotations.any { Annotation anno -> anno.annotationType() == Component }) {
                var name = "_$clazz.simpleName"
                clazz.declaredMethods.find { it.name == name && it.parameterCount == 0 }?.invoke(this)
            }
        }

        init this.class
    }
}