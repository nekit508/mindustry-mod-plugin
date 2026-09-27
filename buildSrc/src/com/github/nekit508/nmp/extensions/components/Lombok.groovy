package com.github.nekit508.nmp.extensions.components

import com.github.nekit508.nmp.extensions.NMPluginExtension
import com.github.nekit508.nmp.tasks.GenerateLombokConfigTask
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.provider.Property

@Component
interface Lombok extends Default {
    Property<String> getLombokVersion()
    Property<Boolean> getUseLombok()

    @SuppressWarnings('unused')
    default void _Lombok() {
        nmp.initialisation {
            project.tasks.create "nmpGenerateLombokConfig", GenerateLombokConfigTask, (this as NMPluginExtension)
        }

        nmp.setting {
            lombokVersion.set "1.18.48"
            useLombok.set false
        }

        nmp.configuration {
            nmp.finalizeProperty useLombok, lombokVersion

            project.tasks.compileJava.dependsOn project.tasks.nmpGenerateLombokConfig
            project.tasks.nmpGenerateLombokConfig.setOnlyIf { useLombok.get() }

            if (useLombok.get()) {
                project.dependencies { DependencyHandler handler ->
                    handler.add "compileOnly", "org.projectlombok:lombok:${lombokVersion.get()}"
                    handler.add "annotationProcessor", "org.projectlombok:lombok:${lombokVersion.get()}"
                }
            }
        }
    }
}