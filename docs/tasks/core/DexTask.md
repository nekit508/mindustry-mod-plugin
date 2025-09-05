# Name (SuperClass)

dependsOn nmpBuild

## Properties

---

### dexFile (optional)

type: RegularFile\
default value: projectBuildDirectory/libs/tmp/dex.jar

Required if [buildAndroid](#buildandroid) is set to true.

---

### sdkRoot (optional)

type: String\
default value: parsed from local.build.sdkRoot or `ANDROID_HOME` and `ANDROID_SDK_ROOT` env variables

Required if [buildAndroid](#buildandroid) is set to true.

---

### buildAndroid

type: Boolean\
default value: local.build.useAndroid or true

---

### Action

Executes only if [buildAndroid](#buildandroid) is set to true.\
Up to date when nmpBuild is up to date.\
Builds [dex file](#dexfile-optional) with d8 from nmpBuild.archiveFile as input and compileClasspath, runtimeClasspath and android as classpath.\
d8 executable is being searched for in the newest version of android tools in [sdkRoot](#sdkroot-optional).
