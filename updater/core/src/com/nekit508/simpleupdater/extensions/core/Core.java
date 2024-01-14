package com.nekit508.simpleupdater.extensions.core;

import arc.files.Fi;
import arc.func.Cons2;
import arc.func.Func;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.serialization.JsonValue;
import com.nekit508.simpleupdater.SimpleUpdater;
import com.nekit508.simpleupdater.events.Events;
import com.nekit508.simpleupdater.extensions.Extension;
import com.nekit508.simpleupdater.extensions.core.events.CoreEvents;

import java.io.InputStream;
import java.util.Properties;

public class Core extends Extension.ExtensionMain {
    public JsonValue files;
    public String remoteFilesRoot;

    /**
     * Files key-value map, where key is file type, but value is file path. <br>
     * File types:
     * <li>static - this file overrides on update</li>
     * <li>local - creates if not exists</li>
     * <li>map-config - configuration file is a key-value table; key-value pairs can be in any order </li>
     * <li>one-time - this files creates only on init</li>
     **/
    public ObjectMap<String, Seq<String>> filesMap = new ObjectMap<>();

    ObjectMap<String, Cons2<String, String>> handlers = new ObjectMap<>();

    public Core() {
        handlers.put("static", (type, file) -> {
            writeFileFromRemote(createFi(file), file);
        });

        handlers.put("local", (type, file) -> {
            Fi fi = createFi(file);
            if (!fi.exists())
                writeFileFromRemote(fi, file);
        });

        handlers.put("map-config", (type, file) -> {
            try {
                Properties properties = new Properties();
                getRemoteFile(file, s -> {
                    try {
                        properties.load(s);
                    } catch (Exception e) {
                        Log.err(e);
                    }
                    return 0;
                });

                Fi fi = createFi(file);

                if (fi.exists())
                    properties.load(fi.reader());

                StringBuilder str = new StringBuilder();
                properties.forEach((key, value) -> {
                    str.append(key.toString() + " = " + value.toString() + "\n");
                });
                fi.writeString(str.toString());
            } catch (Exception e) {
                Log.err(e);
            }
        });

        handlers.put("one-time", (type, file) -> {
            Fi fi = createFi(file);
            if (!fi.exists())
                writeFileFromRemote(fi, file);
        });
    }

    void writeFileFromRemote(Fi fi, String file) {
        getRemoteFile(file, s -> {
            fi.write(s, false);
            return 0;
        });
    }

    @Override
    public void init() {
        Events.on(CoreEvents.coreHandlesFile, e -> Log.info("processing @ @", e.type, e.file));
    }

    @Override
    public void load() {
        files = SimpleUpdater.getRemoteJson("files.json");

        remoteFilesRoot = SimpleUpdater.clampPath(files.getString("files-root", "files"));

        Log.info(new Seq<>(files.get("map-config").asStringArray()));

        // create files types map
        loadFileType("static");
        loadFileType("local");
        loadFileType("map-config");
        loadFileType("one-time");

        Log.info(filesMap);
    }

    @Override
    public void start() {
        // process every file from every group

        handleType("static");
        handleType("local");
        handleType("map-config");
        handleType("one-time");
    }

    void handleType(String type) {
        if (!filesMap.containsKey(type)) return;

        CoreEvents.coreHandlesFile.type = type;
        CoreEvents.coreHandlesFilesType.type = type;
        Events.fire(CoreEvents.coreHandlesFilesType);

        filesMap.get(type).each(file -> {
            handleFile(type, file);
        });
    }

    void handleFile(String type, String file) {
        CoreEvents.coreHandlesFile.file = file;
        Events.fire(CoreEvents.coreHandlesFile);

        handlers.get(type).get(type, file);
    }

    <T> T getRemoteFile(String file, Func<InputStream, T> func) {
        return SimpleUpdater.getRemoteFile(remoteFilesRoot + file, func);
    }

    Fi createFi(String file) {
        return SimpleUpdater.getLocalFi(file);
    }

    Fi createLocalStoreFi(String file) {
        return SimpleUpdater.getLocalFi(SimpleUpdater.simpleUpdaterFiles + file);
    }

    void loadFileType(String type) {
        putFile(type, files.get(type).asStringArray());
    }

    void putFile(String type, String... files) {
        Seq<String> seq = filesMap.get(type);

        if (seq == null) {
            seq = new Seq<>(files.length);
            filesMap.put(type, seq);
        }

        seq.addAll(files);
    }
}
