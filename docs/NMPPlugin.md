# NMPlugin

NMPlugin must be applied at root project, but only application will not affect your project somehow.
All logic is provided by extensions, that can be created by plugin instance. To access it, use `nmp` of project to which you have applied plugin.

These are all functions that can create extensions:
- [NMPluginCoreExtension](extensions/NMPluginCoreExtension.md) - `NMPluginCoreExtension core(Project project, String name)`
- [NMPluginAnnoExtension]() - `NMPluginAnnoExtension anno(Project project, String name, NMPluginCoreExtension core)`
- [NMPluginToolsExtension]() - `NMPluginToolsExtension tools(Project project, String name, NMPluginCoreExtension core)`
- [NMPluginEntityAnnoExtension]() - `NMPluginEntityAnnoExtension entityAnno(Project project, String name, NMPluginCoreExtension core)`
- [NMPluginMMCAnnoExtension]() - `NMPluginMMCAnnoExtension mmcAnno(Project project, String name, NMPluginCoreExtension core)`