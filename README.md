[![](https://jitpack.io/v/nekit508/mindustry-mod-plugin.svg)](https://jitpack.io/#nekit508/mindustry-mod-plugin)

---
# Tasks info

`nmpBuild` - build desktop jar

`nmpDex` - build dex jar (will be skipped if `local.build.useAndroind` == true)

`nmpBuildRelease` - build combined jar (desktop and android (if `local.build.useAndroind` == true))

`nmpCopyBuildRelease` - build combined jar (desktop and android (if `local.build.useAndroind` == true)) and copy it in `local.copy`

---
# Minimal setup
### build.gradle
```groovy
plugins{
    id "java"
    id "com.github.nekit508.mindustry-mod-plugin" version "$nmpVersion" apply true
}

project.repositories {
    mavenCentral()
    mavenLocal()
    maven { url "https://raw.githubusercontent.com/Zelaux/MindustryRepo/master/repository" }
    maven { url "https://www.jitpack.io" }
}

group = "your.group.name"
version = "0.0.0"

// optional
sourceSets.main.java.srcDirs = ["src", "gen"]
sourceSets.main.resources.srcDirs = ["res"]

nmp.genericInit(mindustryVersion)
```

### settings.gradle
```groovy
pluginManagement {
    repositories{
        gradlePluginPortal()
        maven{url 'https://jitpack.io'}
    }
}

rootProject.name = "your-project-name"
```

### settings/local.json
```json
{
  "build": {
    "useAndroid": true,
    "sdkRoot": "path/to/androindSdk/root"
  },
  "copy": [
    "jar/copy/destination/path"
  ]
}
```
`build.useAndroid` - whether .dex file be built

`build.sdkRoot` - androidSDK root path (example `D:/soft/android-sdk`)

`copy` - paths where .jar file will be copied


### gradle.properties
```ini
mindustryVersion = v146
arcVersion = v146
# current plugin version
nmpVersion = v0.1.1

org.gradle.parallel = true

org.gradle.jvmargs = \
-Dfile.encoding=UTF-8 \
--add-opens=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED \
--add-opens=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED \
--add-opens=jdk.compiler/com.sun.tools.javac.model=ALL-UNNAMED \
--add-opens=jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED \
--add-opens=jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED \
--add-opens=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED \
--add-opens=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED \
--add-opens=jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED \
--add-opens=jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED \
--add-opens=jdk.compiler/com.sun.tools.javac.jvm=ALL-UNNAMED \
--add-opens=jdk.compiler/com.sun.tools.javac.comp=ALL-UNNAMED \
--add-opens=java.base/sun.reflect.annotation=ALL-UNNAMED

# uncomment if u want to use kotlin
kapt.include.compile.classpath = false
kotlin.stdlib.default.dependency = false
```
