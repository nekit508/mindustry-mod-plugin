package com.github.nekit508.nmp.lib

import groovy.json.JsonSlurper
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.compile.JavaCompile

import java.util.function.Consumer

class Utils {
    static var json = new JsonSlurper()

    static String readString(String uri, int maxTries = 3, String message = "Connection error") {
        int tryNum = 0
        while (true) {
            try {
                tryNum++
                var bytes = new ByteArrayOutputStream()
                new URI(uri).toURL().withInputStream {
                    bytes << it
                }
                return new String(bytes.toByteArray(), 0, bytes.size())
            } catch (SocketException e) {
                if (tryNum == maxTries)
                    throw new GradleException(message, e)
            }
        }
    }

    static Object readJson(String uri) {
        return json.parse(new URI(uri).toURL())
    }

    static boolean readFile(String uri, File file, int blockSize = 4096, Consumer<Long> progressHandler = (_ -> {})) {
        boolean ok = false
        new URI(uri).toURL().withInputStream { input ->
            file.withOutputStream { output ->
                byte[] buf = new byte[blockSize]
                long totalCount = 0
                for (int count; (count = input.read(buf)) != -1;) {
                    output.write(buf, 0, count)
                    totalCount += count
                    progressHandler.accept(totalCount)
                }
                output.flush()
                ok = true
            }
        }
        ok
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
