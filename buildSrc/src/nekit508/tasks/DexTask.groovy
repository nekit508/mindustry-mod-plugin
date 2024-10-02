package nekit508.tasks

import nekit508.NMPlugin
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile

import javax.inject.Inject

class DexTask extends DefaultTask {
    @OutputFile
    final RegularFileProperty dexFile
    @Input
    Provider<String> sdkRoot
    @Input
    Provider<Boolean> buildAndroid

    @Inject
    DexTask(NMPlugin ext) {
        ObjectFactory objectFactory = getProject().getObjects()

        dexFile = objectFactory.fileProperty()
        dexFile.set project.layout.buildDirectory.file("libs/tmp/dex.jar")

        buildAndroid = objectFactory.property(Boolean.class)
        var use = project.extensions.local?.build?.useAndroid;
        buildAndroid.set use != null ? use : true

        sdkRoot = objectFactory.property(String.class)
        sdkRoot.set project.extensions.local?.build?.sdkRoot ?: System.getenv("ANDROID_HOME") ?: System.getenv("ANDROID_SDK_ROOT")

        dependsOn project.tasks.nmpBuild

        doLast {
            println "build android: " + buildAndroid.get()
            if (buildAndroid.get()) {
                var sdkRoot = new File(sdkRoot.getOrElse(""))
                if (!sdkRoot.exists()) throw new GradleException("No Android SDK found.")

                var platformRoot = new File(sdkRoot, "platforms").listFiles().find { File file -> new File(file, "android.jar").exists() }

                String d8Name = System.getenv("OS") == "Windows_NT" ? "d8.bat" : "d8"
                var buildToolsRoot = new File(sdkRoot, "build-tools").listFiles().find { File file -> new File(file, d8Name).exists() }

                if (!platformRoot || !buildToolsRoot)
                    throw new GradleException("" +
                            (!platformRoot ? "No android.jar found. Ensure that you have an Android platform installed." : "") +
                            (!buildToolsRoot ? "No $d8Name found. Ensure that you have an Android build tools installed." : ""))

                var dependencies = ""
                (project.configurations.compileClasspath.asList()
                        + project.configurations.runtimeClasspath.asList()
                        + new File(platformRoot, "android.jar")).each { path -> dependencies += "--classpath $path.absolutePath " }

                ("$buildToolsRoot/$d8Name $dependencies ${project.tasks.nmpBuild.archiveFile.get()}" +
                        " --min-api 14 --output ${dexFile.get()}").execute(null, project.projectDir).waitForProcessOutput(System.out, System.err)
            }
        }
    }
}
