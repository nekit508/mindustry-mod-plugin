package com.github.nekit508.nmp.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.Task

import javax.inject.Inject

class TasksQueue extends DefaultTask {
    @Inject
    TasksQueue(String group, Task... tasks) {
        this.group = group

        for (var i = 0; i < tasks.length; i++) {
            dependsOn tasks[i]

            if (i > 0)
                tasks[i].mustRunAfter tasks[i-1]
        }
    }
}
