package com.github.nekit508.tasks

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.OutputFile

import javax.inject.Inject;

/** outputFile fields must be set by subclasses. */
abstract class FileFetchTask extends FetchTask {
    /** must be set by subclasses. */
    @OutputFile
    final Property<File> outputFile

    @Inject
    FileFetchTask() {
        outputFile = project.objects.property File
    }

    @Override
    BufferedOutputStream resolveOutput() {
        outputFile.get().newOutputStream()
    }
}
