package nekit508.main.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.Task

import javax.inject.Inject

class DelegatorTask extends DefaultTask {
    @Inject
    DelegatorTask(Task task) {
        group = "nmp"

        dependsOn task
    }
}
