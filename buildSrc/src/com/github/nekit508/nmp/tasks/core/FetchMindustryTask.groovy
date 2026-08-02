package com.github.nekit508.nmp.tasks.core

import com.github.nekit508.nmp.extensions.NMPluginCoreExtension
import com.github.nekit508.nmp.tasks.FetchTask
import org.gradle.api.tasks.Internal

import javax.inject.Inject

class FetchMindustryTask extends FetchTask {
    @Internal
    NMPluginCoreExtension ext

    @Inject
    FetchMindustryTask(NMPluginCoreExtension ext) {
        group = "nmp"
        this.ext = ext
        nmp = this.ext.nmp

        configure {
            repo.set "Anuken/Mindustry"
            version.set ext.mindustryVersion
            outputDir.set project.layout.buildDirectory.dir(ext.nmp.nmpSettings?.mindustry?.downloadDir ?: "mindustry")
            fileName.set "Mindustry"
            extension.set "jar"
        }
    }
}
