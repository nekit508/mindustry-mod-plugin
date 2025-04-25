package nekit508.main.tasks

import nekit508.main.NMPlugin
import org.gradle.api.DefaultTask
import org.gradle.api.Task
import org.gradle.api.tasks.Internal

import javax.inject.Inject

class DelegatorTask extends DefaultTask {
    @Internal
    NMPlugin ext

    @Inject
    DelegatorTask(Task task) {
        group = "nmp"
        this.ext = ext

        dependsOn task
    }
}
