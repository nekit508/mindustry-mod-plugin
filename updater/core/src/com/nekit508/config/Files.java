package com.nekit508.config;

import arc.util.Strings;

public enum Files {
    remoteInfoFile("remote-info.json"),

    extensionMain("extensions/@/@"),

    extensionsList("extensions/list.json"),
    extensionInfo("extensions/@/extension-info.json");

    public final String file;

    Files(String file) {
        this.file = file;
    }

    public String val() {
        return file;
    }

    public String val(Object... format) {
        return Strings.format(file, format);
    }
}
