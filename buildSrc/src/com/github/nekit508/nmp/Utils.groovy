package com.github.nekit508.nmp

import groovy.json.JsonSlurper

class Utils {
    static var json = new JsonSlurper()

    static String readString(String uri) {
        var bytes = new ByteArrayOutputStream()
        new URI(uri).toURL().withInputStream {
            bytes << it
        }
        return new String(bytes.toByteArray(), 0, bytes.size())
    }

    static Object readJson(String uri) {
        return json.parse(new URI(uri).toURL())
    }

    static void readFile(String uri, File file) {
        new URI(uri).toURL().withInputStream { input ->
            file.withOutputStream { output ->
                output << input
            }
        }
    }
}
