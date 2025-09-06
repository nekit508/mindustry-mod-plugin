# BuildReleaseTask (Jar)

dependsOn:
- project.tasks.nmpDex
- project.tasks.nmpBuild

### Action
Combines jars from project.tasks.nmpDex and project.tasks.nmpBuild tasks into one jar.