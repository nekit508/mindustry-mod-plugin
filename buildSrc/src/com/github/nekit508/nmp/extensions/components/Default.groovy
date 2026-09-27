package com.github.nekit508.nmp.extensions.components

import com.github.nekit508.nmp.NMPlugin
import org.gradle.api.Project
import org.gradle.api.internal.file.FileFactory
import org.gradle.api.internal.file.FileResolver
import org.gradle.api.internal.provider.PropertyFactory
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.TaskContainer
import org.gradle.util.internal.ConfigureUtil

import javax.inject.Inject

@Component
interface Default {
    @Inject
    FileFactory getFileFactory();
    @Inject
    FileResolver getFileResolver();
    @Inject
    ProviderFactory getProviderFactory();
    @Inject
    ObjectFactory getObjectFactory();
    @Inject
    PropertyFactory getPropertyFactory();
    @Inject
    Project getProject();

    default Map<String, ?> getSettings() {
        return nmp.nmpSettings
    }

    default NMPlugin getNmp() {
        return Objects.requireNonNull(project.extensions.nmp as NMPlugin)
    }

    default void configureTasks(@DelegatesTo(TaskContainer) Closure closure) {
        nmp.configuration {
            ConfigureUtil.configureSelf closure, project.tasks
        }
    }

    default String prop(String name) {
        return project."$name"
    }
}