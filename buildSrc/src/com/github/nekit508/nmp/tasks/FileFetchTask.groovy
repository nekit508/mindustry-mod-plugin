package com.github.nekit508.nmp.tasks


import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputFile
import org.gradle.internal.hash.HashCode
import org.gradle.internal.hash.HashFunction
import org.gradle.internal.hash.Hashing

import javax.inject.Inject;

/** outputFile fields must be set by subclasses. */
abstract class FileFetchTask extends FetchTask {
    /** must be set by subclasses. */
    @OutputFile
    final Property<File> outputFile

    @Internal
    final Property<HashFunction> checksumAlgorithm

    @Inject
    FileFetchTask() {
        var factory = project.objects

        outputFile = factory.property File
        checksumAlgorithm = factory.property HashFunction

        configure {
            checksumAlgorithm.set Hashing.sha256()
        }
    }

    @Override
    BufferedOutputStream resolveOutput() {
        outputFile.get().newOutputStream()
    }
}
