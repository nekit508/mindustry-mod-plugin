package nekit508.tasks

import nekit508.NMPlugin
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile

import javax.inject.Inject

class DexTask extends DefaultTask {
    @OutputFile
    @Optional
    final RegularFileProperty dexFile
    @Input
    @Optional
    final Property<String> sdkRoot
    @Input
    final Property<Boolean> buildAndroid

    @Inject
    DexTask(NMPlugin ext) {
        group = "nmp"

        ObjectFactory objectFactory = getProject().getObjects()

        logger.debug("$name: DexTask $name configuration")

        dexFile = objectFactory.fileProperty()
        dexFile.set project.layout.buildDirectory.file("libs/tmp/dex.jar")
        logger.debug("$name: dexFile: ${dexFile.getOrNull()?.asFile?.absolutePath}")

        buildAndroid = objectFactory.property(Boolean.class)
        var use = ext.local?.build?.useAndroid
        buildAndroid.set use != null ? use : true
        logger.debug("$name: buildAndroid: ${buildAndroid.get()}")

        sdkRoot = objectFactory.property(String)
        var p = ext.local?.build?.sdkRoot ?: System.getenv("ANDROID_HOME") ?: System.getenv("ANDROID_SDK_ROOT") ?: null
        if (p)
            sdkRoot.set p
        else
            sdkRoot.unset()
        logger.debug("$name: sdkRoot: ${sdkRoot.getOrNull()}")

        dependsOn project.tasks.nmpBuild

        onlyIf {
            buildAndroid.get()
        }

        doLast {
            logger.debug("$name: d8 start preparing")

            var sdkRootPath = sdkRoot.getOrNull()
            var sdkRoot = sdkRootPath != null ? project.file(sdkRootPath) : null
            if (!sdkRoot || !sdkRoot.exists()) throw new GradleException("No Android SDK found. SDK root if set to $sdkRootPath")
            logger.debug("$name: sdkRoot: $sdkRoot.absolutePath")

            var platformRoot = new File(sdkRoot, "platforms").listFiles().find { File file -> new File(file, "android.jar").exists() }
            logger.debug("$name: platformRoot: $platformRoot.absolutePath")

            String d8Name = System.getenv("OS") == "Windows_NT" ? "d8.bat" : "d8"
            logger.debug("$name: d8Name: $d8Name")
            var buildToolsRoot = new File(sdkRoot, "build-tools").listFiles().find { File file -> new File(file, d8Name).exists() }
            logger.debug("$name: buildToolsRoot: $buildToolsRoot.absolutePath")

            if (!platformRoot || !buildToolsRoot)
                throw new GradleException("" +
                        (!platformRoot ? "No android.jar found. Ensure that you have an Android platform installed." : "") +
                        (!buildToolsRoot ? "No $d8Name found. Ensure that you have an Android build tools installed." : ""))

            var dependencies = ""
            (project.configurations.compileClasspath.asList()
                    + project.configurations.runtimeClasspath.asList()
                    + new File(platformRoot, "android.jar")).each { path -> dependencies += "--classpath $path.absolutePath " }

            logger.debug("$name: dependencies: $dependencies")

            logger.debug("$name: d8 start")
            ("$buildToolsRoot/$d8Name $dependencies ${project.tasks.nmpBuild.archiveFile.get()}" +
                    " --min-api 14 --output ${dexFile.get()}").execute(null, project.projectDir).waitForProcessOutput(System.out, System.err)
            logger.debug("$name: d8 end")
        }
    }
}
