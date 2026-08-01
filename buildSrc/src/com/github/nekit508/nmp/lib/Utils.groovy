package com.github.nekit508.nmp.lib

import groovy.json.JsonSlurper
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.compile.JavaCompile

import java.util.function.Consumer

class Utils {
    static var json = new JsonSlurper()

    static String readString(String uri) {
        return new URI(uri).toURL().text
    }

    static Object readJson(String uri) {
        return json.parse(new URI(uri).toURL())
    }

    static void readFile(String uri, File file, int blockSize = 4096, Consumer<Long> progressHandler = (_ -> {})) {
        readFile(new URI(uri).toURL().newInputStream(), file.newOutputStream(), blockSize, progressHandler)
    }

    static void readFile(InputStream input, OutputStream output, int blockSize = 4096, Consumer<Long> progressHandler = (_ -> {})) {
        byte[] buf = new byte[blockSize]
        long totalCount = 0
        for (int count; (count = input.read(buf)) != -1;) {
            output.write(buf, 0, count)
            totalCount += count
            progressHandler.accept(totalCount)
        }
        output.flush()
    }

    static void annotationProcessorArgs(TaskProvider<JavaCompile> provider, Map<String, String> args) {
        provider.configure { task ->
            args.each { key, value ->
                task.options.compilerArgs.add "-A$key=$value"
            }
        }
    }

    static String subpath(String root, String child) {
        child.substring(root.length(), child.length())
    }
}
