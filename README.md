[![](https://jitpack.io/v/nekit508/mindustry-mod-plugin.svg)](https://jitpack.io/#nekit508/mindustry-mod-plugin)

# Mindustry mod plugin

Plugin for building mindustry mods.

[My mod template based on this plugin](https://github.com/nekit508/mmp-template).

---
## Tasks info

`nmpBuild` - build desktop jar

`nmpDex` - build dex jar (will be skipped if `local.build.useAndroind` == true)

`nmpBuildRelease` - build combined jar (desktop and android (if `local.build.useAndroind` == true))

`nmpCopyBuildRelease` - build combined jar (desktop and android (if `local.build.useAndroind` == true)) and copy it in `local.copy`

`nmpGenerateModInfo` - generate `mod.json` file

---

## Local settings

`build.useAndroid` - whether .dex file be built (If you do not know what it means, set this parameter `false`)

`build.sdkRoot` - androidSDK root path (example D:/soft/android-sdk)

`copy` - list of paths where .jar file will be copied

## Project settings

Task `nmpGenerateModInfo` supports all mod metadata fields and also allows you to add your own by storing it in `modMiscData`.

Plugin's main class (that can be referenced from `build.gradle` by `project.nmp`) also allows you to manually set up the following parameters:
- `mindutsryVersion` - mindustry and arc version that will be used as dependencies (default `v146`)
- `modName` - name of mod, affects output .jar name and `mod.json`
- `modVersion` - name of mod, affects output .jar name, `mod.json` and `project.version`
- `modGroup` - group of mod, affects output .jar name and `project.group`
- `jabelVersion` - jabel version that will be used to compile mod (default `1.0.0`)
- `generateModInfo` - wether `mod.json` be genarated (default `false`)
- `sourceCompatibility` - source bytecode version (allows newer features) (default 20th java vesion)

All these parameters can be set by the dictionary in the `nmp.setProps(Map<String, Object>)` method.

--- Get project prepared

After settings up paraments, you can finally prepare your project for modding by using `nmp.genericInit()` method after parameters adjustment code.

This method will configure compilation settings, set up Jabel, create tasks and add midustry and arc dependencies.
