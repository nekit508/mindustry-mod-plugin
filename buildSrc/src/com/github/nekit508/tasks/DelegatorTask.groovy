package com.github.nekit508.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.Task

import javax.inject.Inject

class DelegatorTask extends DefaultTask {
    @Inject
    DelegatorTask(Task... tasks) {
        tasks.each { dependsOn it }
    }
}
