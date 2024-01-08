package com.nekit508.extensions;

import arc.util.serialization.JsonValue;
import com.nekit508.SimpleUpdater;
import com.nekit508.config.Config;
import com.nekit508.config.Files;
import org.luaj.vm2.Globals;
import org.luaj.vm2.Lua;
import org.luaj.vm2.LuaClosure;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.ast.Chunk;
import org.luaj.vm2.lib.jse.JsePlatform;

import java.io.InputStreamReader;

public class Extension {
    public static Globals globals = JsePlatform.standardGlobals();

    public JsonValue info;

    public String name;
    public String[] dependencies;
    public ExtensionType type;
    public String main;
    public String extensionRoot;

    public LuaValue mainScript;
    public ExtensionMain mainClass;

    public Extension(String root) {
        extensionRoot = root;
    }

    public void parse(JsonValue value) {
        info = value;

        name = Config.extensionsDirExtensionInfoName.get(value).asString();
        dependencies = Config.extensionsDirExtensionInfoDependencies.get(value).asStringArray();
        main = Config.extensionsDirExtensionInfoMain.get(value).asString();

        String t = Config.extensionsDirExtensionInfoType.get(value).asString();
        if (t.equals("lua"))
            type = ExtensionType.lua;
        else if (t.equals("java"))
            type = ExtensionType.java;
    }

    public void init() {
        if (type == ExtensionType.lua) {
            mainScript= globals.load(new InputStreamReader(
                    SimpleUpdater.getRemoteFile(Files.extensionMain, extensionRoot, main)), main);

            LuaValue init = mainScript.method("init");
            init.call();
        } else if (type == ExtensionType.java) {

        }
    }

    public void load() {
        if (type == ExtensionType.lua) {
            mainScript.method("load").call();
        } else if (type == ExtensionType.java) {

        }
    }

    public void start() {
        if (type == ExtensionType.lua) {
            mainScript.method("start").call();
        } else if (type == ExtensionType.java) {

        }
    }

    public static abstract class ExtensionMain {
        public void init() {}
        public void load() {}
        public void start() {}
    }
}
