# NMPluginCoreExtension

Main extension, that must be applied to the logical root of your mod.\
Contains reference to [NMPPlugin](../NMPPlugin.md) in nmp field.

That extension contains following methods:
- [genericModInit](#genericmodinit)
- [initGenericTasks](#initGenericTasks):
- [initModTasks](#initModTasks)
- [initLibraryTasks](#initLibraryTasks)
- [configureCompileTask](#configureCompileTask)
- [setupJabel](#setupJabel)
- [modBaseDependencies](#modBaseDependencies)
- [configureMavenPublishing](#configureMavenPublishing)

## Properties info:

---

### mindustryVersion

type: String\
default value: v146

---

### modName

type: String\
default value: _unset_

---

### modVersion

type: String\
default value: _unset_

---

### modGroup

type: String\
default value: _unset_

---

### jabelVersion

type: String\
default value: 1.0.1-1

---

### generateModInfo

type: Boolean\
default value: true

---

### sourceCompatibility

type: JavaVersion\
default value: 20

---

### srcDirs

type: File[]\
default value: \[projectRoot/src]

---

### resDirs

type: File[]\
default value: \[projectRoot/res]

---

### genDir

type: File\
default value: projectRoot/gen

---

### mavenPublishPluginName

type: String\
default value: maven-publish 

---

### javaLibraryPluginName

type: String\
default value: java-library

---

## Methods info:

---

### genericModInit

Arguments:
1) isLibrary: Boolean = false
2) group: String = null (must be specified if isLibrary set to true)

Executes:
- [configureCompileTask](#configureCompileTask)
- [setupJabel](#setupJabel)
- [modBaseDependencies](#modBaseDependencies)
- [initGenericTasks](#initGenericTasks)
- [initModTasks](#initModTasks)
- if isLibrary set to true:
  - nmp.[configureProjectDataForJitpackBuilding]()(group)
  - [initLibraryTasks](#initLibraryTasks)
  - [configureMavenPublishing](#configureMavenPublishing)

---

### initGenericTasks

Registers following tasks:
- [nmpBuild](../tasks/core/BuildTask.md)
- [nmpDex](../tasks/core/DexTask.md)

---

### initModTasks

Registers following tasks:
- [nmpBuildRelease]()
- [nmpCopyBuildRelease]()
- [nmpGenerateModInfo]()
- [nmpFetchMindustry]()
- [nmpRunMindustry]()
- [nmpCopyBuildReleaseRunMindustry]()

---

### initLibraryTasks

Registers following tasks:
- [nmpBuildSources]()
- [nmpBuildLibrary]()

--- 

### configureCompileTask

Configures compileJava task to compile mod:
- sets utf-8 encoding
- sets generated files output directory to [genDir](#gendir)
- and before compilation:
  - removes the preview option
  - deletes the directory with generated files
- configures source sets:
  - adds [srcDirs](#srcdirs) to main.java.srcDirs
  - adds [resDirs](#resdirs) to main.resources.srcDirs

---

### setupJabel

Configures project for jabel using:
- compileJava task configuration:
  - sets task's sourceCompatibility to [sourceCompatibility](#sourcecompatibility)
  - adds options:
    - `--release 8`
    - `--enable-preview`
    - `-Xlint:-options`
- adds dependencies:
  - `com.pkware.jabel:jabel-javac-plugin` of version [jabelVersion](#jabelversion) as annotationProcessor and compileOnly

---

### modBaseDependencies

Adds dependencies of mindustry and arc of version [mindustryVersion](#mindustryversion) as compileOnly

---

### configureMavenPublishing

Requires plugins:
- [mavenPublishPluginName](#mavenpublishpluginname)
- [javaLibraryPluginName](#javalibrarypluginname)

Configures project for publishing on jitpack platform:
- components configuration:
  - adds sources and javaDoc jars to java components
- publishing configuration:
  - creates library publication with java components
- tasks configuration:
  - jar:
    - make depends on [nmpBuildLibrary]()
    - sets duplications resolution strategy to exclude
    - adds [nmpBuildLibrary]()'s archiveFile file tree as input
  - sourcesJar:
    - make depends on [nmpBuildSources]()
    - sets duplications resolution strategy to exclude
    - adds [nmpBuildSources]()'s archiveFile file tree as input

---
