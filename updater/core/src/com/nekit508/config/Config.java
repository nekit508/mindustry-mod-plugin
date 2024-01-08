package com.nekit508.config;

import arc.util.Strings;
import arc.util.serialization.JsonValue;

public enum Config {
    remoteInfoRoot("root"),
    remoteInfoVersion("version"),

    extensionsListList("list"),

    extensionsDirExtensionInfoType("type"),
    extensionsDirExtensionInfoMain("main"),
    extensionsDirExtensionInfoDependencies("dependencies"),
    extensionsDirExtensionInfoName("name");

    public final String path;

    Config(String path) {
        this.path = path;
    }

    public JsonValue get(JsonValue value) {
        String[] strs = path.split("[/\\\\]");

        for (String str : strs) {
            value = value.get(str);
        }

        return value;
    }

    public JsonValue get(JsonValue value, Object... format) {
        String[] strs = Strings.format(path, format).split("[/\\\\]");

        for (String str : strs) {
            value = value.get(str);
        }

        return value;
    }
}
