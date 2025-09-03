# NMPluginCoreExtension

Main extension, that must be applied to the logical root of your mod.

That extension can create following tasks and methods which provides them:
- [initGenericTasks]():
  - [nmpBuild]()
  - [nmpDex]()
- [initModTasks]():
  - [nmpBuildRelease]()
  - [nmpCopyBuildRelease]()
  - [nmpGenerateModInfo]()
  - [nmpFetchMindustry]()
  - [nmpRunMindustry]()
  - [nmpCopyBuildReleaseRunMindustry]()
- [initLibraryTasks]():
  - [nmpBuildSources]()
  - [nmpBuildLibrary]()
