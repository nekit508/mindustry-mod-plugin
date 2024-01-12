package com.nekit508.extensions.core;

import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.serialization.JsonValue;
import com.nekit508.SimpleUpdater;
import com.nekit508.extensions.Extension;

public class Core extends Extension.ExtensionMain {
    public JsonValue files;
    public String filesRoot;

    /**
     * Files key-value map, where key is file type, but value is file path. <br>
     * File types:
     * <li>static - this file overrides on update</li>
     * <li>local - creates if not exists</li>
     * <li>map-config - configuration file is a key-value table; key-value pairs can be in any order </li>
     * <li>one-time - this files creates only on init</li>
     **/
    public ObjectMap<String, Seq<String>> filesMap = new ObjectMap<>();

    @Override
    public void init() {

    }

    @Override
    public void load() {
        files = SimpleUpdater.getRemoteJson("files.json");

        filesRoot = files.getString("files-root", "files") + "/";

        // create files types map
        loadFileType("static");
        loadFileType("local");
        loadFileType("map-config");
        loadFileType("one-time");
    }

    @Override
    public void start() {
        super.start();
    }



    void loadFileType(String type) {
        putFile(type, files.get("type").asStringArray());
    }

    void putFile(String type, String... files) {
        Seq<String> seq = filesMap.get(type);

        if (seq == null)
            seq = filesMap.put(type, new Seq<>(files.length));

        seq.addAll(files);
    }
}
