[![](https://jitpack.io/v/nekit508/mindustry-mod-plugin.svg)](https://jitpack.io/#nekit508/mindustry-mod-plugin)

# Mindustry mod plugin

Plugin for building mindustry mods.

[My mod template based on this plugin](https://github.com/nekit508/mmp-template).

---
## Tasks info

### Main project:

`nmpBuild` - build desktop jar

`nmpDex` - build dex jar (will be skipped if `local.build.useAndroind`)

`nmpBuildRelease` - build combined jar (desktop and android (if `local.build.useAndroind`))

`nmpCopyBuildRelease` - build combined jar (desktop and android (if `local.build.useAndroind`)) and copy it in `local.copy`

`nmpGenerateModInfo` - generate `mod.json` file (only if `nmp.generateModInfo`)

### Annotations subproject:

`nmpaGenerateProcessorsFile` - generate `javax.annotation.processing.Processor` file

---
## Local settings

`build.useAndroid` - whether .dex file be built (If you do not know what it means, set this parameter `false`)

`build.sdkRoot` - androidSDK root path (example D:/soft/android-sdk)

`copy` - list of paths where .jar file will be copied

---
## Project settings

### Main settings

Plugin's main class (that can be referenced from main project by `project.nmp`) allows you to manually set up the following parameters:
- `mindutsryVersion` - mindustry and arc version that will be used as dependencies (default `v146`)
- `modName` - name of mod, affects output .jar name and `mod.json`
- `modVersion` - name of mod, affects output .jar name, `mod.json` and `project.version`
- `modGroup` - group of mod, affects output .jar name and `project.group`
- `jabelVersion` - jabel version that will be used to compile mod (default `1.0.0`)
- `generateModInfo` - whether `mod.json` be generated (default `false`)
- `sourceCompatibility` - source bytecode version (allows newer features) (default 20th java vesion)

All these parameters can be set by the dictionary in the `nmp.setProps(Map<String, Object>)` method.

### Anno settings

Plugin's anno class (that can be referenced from anno subproject by `project.nmpa`) now doesn't contain any parameters.

### Mod info generator

Task `tasks.nmpGenerateModInfo` supports all mod metadata fields and also allows you to add your own by storing it in `tasks.nmpGenerateModInfo.modMiscData`.

--- 
## Get project prepared

After setting up parameters, you can finally prepare your project for modding by using `nmp.genericInit()` method after parameters adjustment code.
This method will configure compilation settings, set up Jabel, create tasks and add midustry and arc dependencies.

Also, you can add annotation project by yourself and then configure it with `project.nmp.setupProjectAsAnnoProject(Project)`.
This method will create task for auto generate processors list and add annotations project as dependency to your main project.
