package com.nekit508.simpleupdater;

import arc.files.Fi;
import arc.func.Func;
import arc.struct.Seq;
import arc.util.Http;
import arc.util.Log;
import arc.util.serialization.JsonReader;
import arc.util.serialization.JsonValue;
import com.nekit508.simpleupdater.config.Config;
import com.nekit508.simpleupdater.config.Files;
import com.nekit508.simpleupdater.extensions.core.Core;
import com.nekit508.simpleupdater.extensions.Extension;
import com.nekit508.simpleupdater.extensions.ExtensionType;

import java.io.InputStream;


/** This class provides tools and variables needed to work with remote and local files. **/
public class SimpleUpdater {
    private static String repo, branch;

    /** Global json reader. **/
    public static JsonReader jsonReader = new JsonReader();
    /** Path to the root where the files are actually located. **/
    public static String remoteRootPath;
    /** Local directory, where program should save synchronised files. **/
    public static String localRootPath;

    /** Remote data structure version. **/
    public static float version;

    /** remote-info.json root JsonValue **/
    public static JsonValue remoteInfo;
    /** extension/list.json root JsonValue **/
    public static JsonValue extensionsList;

    /** Directory where files are stored. **/
    public static String simpleUpdaterFiles = clampPath(".updater");

    /** Returns stream to file in current root **/
    public static <T> T getRemoteFile(Files file, Func<InputStream, T> func) {
        return getRemoteFile(remoteRootPath, file.val(), func);
    }

    /** Returns stream to file in current root **/
    public static <T> T getRemoteFile(Files file, Func<InputStream, T> func, Object... format) {
        return getRemoteFile(remoteRootPath, file.val(format), func);
    }

    /** Returns stream to file in current root **/
    public static <T> T getRemoteFile(String file, Func<InputStream, T> func) {
        return getRemoteFile(remoteRootPath, file, func);
    }

    /** Returns stream to file in branch in repo **/
    public static <T> T getRemoteFile(String repo, String branch, String file, Func<InputStream, T> func) {
        return getRemoteFile(repo + "/" + branch, file, func);
    }

    /** Returns stream to file in specified root **/
    public static <T> T getRemoteFile(String rootPath, String file, Func<InputStream, T> func) {
        Object[] out = new Object[1];
        Http.get("https://raw.githubusercontent.com/%s/%s".formatted(rootPath, file)).block(r -> {
            out[0] = func.get(r.getResultAsStream());
        });
        return (T) out[0];
    }

    public static Fi getLocalFi(String file) {
        return new Fi(localRootPath + file);
    }

    public static JsonValue getRemoteJson(String file) {
        return getRemoteFile(file, s -> parse(s));
    }

    public static JsonValue getRemoteJson(Files file) {
        return getRemoteFile(file.val(), s -> parse(s));
    }

    public static JsonValue getRemoteJson(Files file, Object format) {
        return getRemoteFile(file.val(format), s -> parse(s));
    }

    public static JsonValue parse(InputStream stream) {
        return jsonReader.parse(stream);
    }

    public static String clampPath(String path) {
        path = path.replaceAll("\\|/", "/");
        if (path.charAt(path.length()-1) != '/')
            path += "/";
        return path;
    }

    public static void main(String[] args) {
        for (String arg : args) {
            if (arg.startsWith("-R"))
                repo = arg.substring(2);
            else if (arg.startsWith("-B"))
                branch = arg.substring(2);
            else if (arg.startsWith("-debug"))
                Log.level = Log.LogLevel.debug;
            else if (arg.startsWith("-D"))
                localRootPath = clampPath(arg.substring(2));
        }
        if (repo == null) {
            Log.err("The program arguments must contain -R{remoteRepoName}");
            return;
        }
        if (branch == null) {
            Log.err("The program arguments must contain -B{remoteBranch}");
            return;
        }
        if (localRootPath == null)
            localRootPath = new Fi("local").absolutePath();

        load();
    }

    public static void load() {
        // read remote info
        remoteInfo = getRemoteFile(repo, branch, Files.remoteInfoFile.val(), s -> parse(s));
        version = Config.remoteInfoVersion.get(remoteInfo).asFloat(); // idk what to do with this (-_-(
        remoteRootPath = Config.remoteInfoRoot.get(remoteInfo).asString();

        // extensions
        extensionsList = getRemoteFile(Files.extensionsList, s -> parse(s));
        String[] extensionsDirs = Config.extensionsListList.get(extensionsList).asStringArray();

        // create extensions list
        Seq<Extension> extensions = new Seq<>();

        for (String extensionDir : extensionsDirs) {
            Extension extension = new Extension(extensionDir);

            extension.parse(getRemoteFile(Files.extensionInfo, s -> parse(s), extensionDir));
            extensions.add(extension);
        }

        // create extensions queue
        Seq<Extension> loadingQueue = new Seq<>();

        for (Extension extension : extensions) {
            if (!loadingQueue.contains(extension))
                loadingQueue.add(extension);

            for (String dependency : extension.dependencies) {
                Extension dep = extensions.find(e -> e.name.equals(dependency));

                if (dep == null)
                    Log.err("Missing dependency @ for extension @.", dependency, extension.name);

                if (!loadingQueue.contains(dep))
                    loadingQueue.insert(loadingQueue.indexOf(extension), dep);
            }
        }

        loadingQueue.add(new Extension("$no-root"){{
            mainClass = new Core();
            type = ExtensionType.java;
        }});

        Log.debug("loading queue: @", loadingQueue);

        // init extensions
        for (Extension extension : loadingQueue)
            extension.init();

        // load extensions
        for (Extension extension : loadingQueue)
            extension.load();

        // start extensions
        for (Extension extension : loadingQueue)
            extension.start();
    }
}
