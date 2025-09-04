package com.github.nekit508.nmp.tasks.core

import com.github.nekit508.nmp.extensions.NMPluginCoreExtension
import groovy.json.JsonBuilder
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.Pair

import javax.inject.Inject

class GenerateModInfoTask extends DefaultTask {
    @Internal
    NMPluginCoreExtension ext

    @OutputFile
    final RegularFileProperty outputFile // by default set by plugin

    @Input
    final Property<String> modName, // by default set by plugin
                           modVersion,  // by default set by plugin
                           modMain

    @Input
    final Property<Integer> modMinGameVersion // by default set by plugin;

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

    protected Map<String, Pair<Provider<?>, Boolean>> jsonProperties

    @Inject
    GenerateModInfoTask(NMPluginCoreExtension ext) {
        group = "nmp"
        this.ext = ext

        project.tasks.nmpBuild.dependsOn this

        var factory = getProject().getObjects()

        outputFile = factory.fileProperty()

        jsonProperties = new HashMap<>()

        modName = adjProp("name", factory.property (String), true)
        modDisplayName = adjProp("displayName", factory.property (String))
        modMinGameVersion = adjProp("minGameVersion", factory.property (Integer), true)
        modAuthor = adjProp("author", factory.property (String))
        modDescription = adjProp("description", factory.property (String))
        modSubtitle = adjProp("subtitle", factory.property (String))
        modVersion = adjProp("version", factory.property (String), true)
        modMain = adjProp("main", factory.property (String), true)
        modRepo = adjProp("repo", factory.property (String))

        modDependencies = adjProp("dependencies", factory.listProperty (String))
        modSoftDependencies = adjProp("softDependencies", factory.listProperty (String))

        modJava = adjProp("java", factory.property (Boolean))
        modPregenerated = adjProp("pregenerated", factory.property (Boolean))
        modHidden = adjProp("hidden", factory.property (Boolean))
        modKeepOutlines = adjProp("keepOutlines", factory.property (Boolean))

        modMiscData = factory.mapProperty(String, Object)

        configure {
            modJava.set true
            outputFile.set getProject().file("mod.json")

            modName.set ext.modName
            modVersion.set ext.modVersion
            modMinGameVersion.set project.provider { ext.mindustryVersion.get().substring(1) as Integer }

            if (ext.generateModInfo.get())
                project.tasks.nmpBuild.from outputFile
            setEnabled ext.generateModInfo.get()
        }
    }

    void set(Map<String, ?> settings) {
        var keys = settings.keySet()
        for (final var key in keys) {
            var value = settings[key]

            if (!jsonProperties.containsKey(key)) {
                modMiscData.put(key, value)
            } else {
                var prop = jsonProperties[key].left()
                if (prop instanceof ListProperty<?>) {
                    if (value instanceof Provider<Iterable<?>>)
                        prop.set value
                    else {
                        if (value instanceof Iterable<?>)
                            prop.addAll value
                        else
                            prop.add value
                    }
                } else if (prop instanceof Property<?>) {
                    prop.set value
                } else if (prop instanceof MapProperty<?, ?>)
                    throw new IllegalArgumentException("Map property " + key + " cannot be set.")
                else
                    throw new IllegalArgumentException("Property " + key + " has unknown type.")
            }
        }
    }

    Object getAt(String name) {
        jsonProperties.containsKey(name) ? jsonProperties[name].left() : modMiscData[name]
    }

    void putAt(String name, var value) {
        if (jsonProperties.containsKey(name)) {
            var prop = jsonProperties[name].left()

            if (prop instanceof Property<?>)
                prop.set value
            else
                throw new ClassCastException("If you want to adjust list or map property, do it directly by getting it and using it's methods.")
        } else {
            modMiscData[name] = value
        }
    }

    protected <T extends Provider<?>> T adjProp(String name, T property, boolean required = false) {
        jsonProperties[name] = Pair.of(property, required)
        return property
    }

    @TaskAction
    void generate() {
        var all = new LinkedHashMap<String, Object>()

        var misc = modMiscData.get()
        misc.forEach { k, v ->
            all[k] = v instanceof Provider<?> ? (v.isPresent() ? v.get() : "null") : v
        }

        var keys = jsonProperties.keySet()
        for (final var key in keys) {
            var pair = jsonProperties[key]
            if (pair.left().isPresent() || pair.right())
                all.put(key, pair.left().get())
        }

        JsonBuilder builder = new JsonBuilder()
        builder.call(all)
        try (var writer = new FileWriter(outputFile.get().asFile)) {
            writer.write(builder.toPrettyString())
            writer.close()
        }
    }
}
