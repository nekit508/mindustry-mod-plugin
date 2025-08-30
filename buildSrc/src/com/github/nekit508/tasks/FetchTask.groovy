package com.github.nekit508.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

abstract class FetchTask extends DefaultTask {
    @Internal
    final Property<Integer> bufferSize

    FetchTask() {
        ObjectFactory objectFactory = getProject().getObjects()

        bufferSize = objectFactory.property Integer

        configure {
            bufferSize.set 8192
        }
    }

    @TaskAction
    void fetch() {
        try (var reader = resolveInput(); var writer = resolveOutput()) {
            var buffer = new byte[bufferSize.get()]
            int size
            while ((size = reader.read(buffer)) != -1)
                writer.write(buffer, 0, size)
        } catch (IOException e) {
            throw new GradleException("Fetching error.", e)
        }
    }

    abstract BufferedInputStream resolveInput();

    abstract BufferedOutputStream resolveOutput();
}
