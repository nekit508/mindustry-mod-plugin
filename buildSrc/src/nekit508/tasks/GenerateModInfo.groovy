package nekit508.tasks

import groovy.json.JsonBuilder
import nekit508.NMPlugin
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile

import javax.inject.Inject

class GenerateModInfo extends DefaultTask {
    @OutputFile
    final RegularFileProperty outputFile // by default set by plugin

    @Input
    final Property<String> modName, // by default set by plugin
            modMinGameVersion, // by default set by plugin
            modVersion,  // by default set by plugin
            modMain

    @Input
    @Optional
    final Property<String> modDisplayName,
            modAuthor,
            modDescription,
            modSubtitle,
            modRepo

    @Optional
    @Input
    final ListProperty<String> modDependencies,
            modSoftDependencies

    @Optional
    @Input
    final Property<Boolean> modJava,  // by default set by plugin
            modPregenerated,
            modHidden,
            modKeepOutlines

    @Optional
    @Input
    MapProperty<String, ?> modMiscData

    @Inject
    GenerateModInfo(NMPlugin ext) {
        group = "nmp"

        project.tasks.nmpBuildRelease.dependsOn this

        var factory = getProject().getObjects()

        outputFile = factory.fileProperty()

        modName = factory.property String
        modDisplayName = factory.property String
        modMinGameVersion = factory.property String
        modAuthor = factory.property String
        modDescription = factory.property String
        modSubtitle = factory.property String
        modVersion = factory.property String
        modMain = factory.property String
        modRepo = factory.property String

        modDependencies = factory.listProperty String
        modSoftDependencies = factory.listProperty String

        modJava = factory.property Boolean
        modPregenerated = factory.property Boolean
        modHidden = factory.property Boolean
        modKeepOutlines = factory.property Boolean

        modMiscData = factory.mapProperty(String, Object)

        modJava.set true
        outputFile.set getProject().file("mod.json")

        modName.set Objects.requireNonNull(ext.modName, "nmp.modName must be set")
        modVersion.set Objects.requireNonNull(ext.modVersion, "nmp.modVersion must be set")
        modMinGameVersion.set Objects.requireNonNull(ext.mindutsryVersion.substring(1), "nmp.mindutsryVersion must be set")

        if (ext.generateModInfo)
            project.tasks.nmpBuildRelease.from outputFile

        onlyIf {
            ext.generateModInfo
        }

        doLast {
            JsonBuilder builder = new JsonBuilder()

            var misc = modMiscData.get()
            var all = new LinkedHashMap<String, Object>()

            misc.forEach { k, v ->
                all.put(k, v)
            }

            all.put("main", modMain.get())
            all.put("name", modName.get())
            all.put("version", modVersion.get())
            all.put("minGameVersion", modMinGameVersion.get())

            if (modDisplayName.isPresent()) all.put("displayName", modDisplayName.get())
            if (modAuthor.isPresent()) all.put("author", modAuthor.get())
            if (modDescription.isPresent()) all.put("description", modDescription.get())
            if (modSubtitle.isPresent()) all.put("subtitle", modSubtitle.get())
            if (!modDependencies.get().isEmpty()) all.put("dependencies", modDependencies.get())
            if (!modSoftDependencies.get().isEmpty()) all.put("softDependencies", modSoftDependencies.get())
            if (modJava.isPresent()) all.put("java", modJava.get())
            if (modPregenerated.isPresent()) all.put("pregenerated", modPregenerated.get())
            if (modHidden.isPresent()) all.put("hidden", modHidden.get())
            if (modKeepOutlines.isPresent()) all.put("keepOutlines", modKeepOutlines.get())

            builder.call(all)

            try (var writer = new FileWriter(outputFile.get().asFile)) {
                writer.write(builder.toPrettyString())
                writer.close()
            }
        }
    }
}
