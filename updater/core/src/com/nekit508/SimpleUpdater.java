package com.nekit508;

import arc.func.Func;
import arc.struct.Queue;
import arc.struct.Seq;
import arc.util.Http;
import arc.util.Log;
import arc.util.serialization.JsonReader;
import arc.util.serialization.JsonValue;
import com.nekit508.config.Config;
import com.nekit508.config.Files;
import com.nekit508.extensions.Extension;

import java.io.InputStream;


/** This class provides tools and variables needed to work with remote and local files. **/
public class SimpleUpdater {
    private static String repo, branch;

    /** Global json reader. **/
    public static JsonReader jsonReader = new JsonReader();
    /** Path to the root where the files are actually located. **/
    public static String rootPath;

    /** Remote data structure version. **/
    public static float version;

    /** remote-info.json root JsonValue **/
    public static JsonValue remoteInfo;
    /** extension/list.json root JsonValue **/
    public static JsonValue extensionsList;

    /** Returns stream to file in current root **/
    public static <T> T getRemoteFile(Files file, Func<InputStream, T> func) {
        return getRemoteFile(rootPath, file.val(), func);
    }

    /** Returns stream to file in current root **/
    public static <T> T getRemoteFile(Files file, Func<InputStream, T> func, Object... format) {
        return getRemoteFile(rootPath, file.val(format), func);
    }

    /** Returns stream to file in current root **/
    public static <T> T getRemoteFile(String file, Func<InputStream, T> func) {
        return getRemoteFile(rootPath, file, func);
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

    public static JsonValue parse(InputStream stream) {
        return jsonReader.parse(stream);
    }

    public static void main(String[] args) {
        for (String arg : args) {
            if (arg.startsWith("-R"))
                repo = arg.substring(2);
            else if (arg.startsWith("-B"))
                branch = arg.substring(2);
            else if (arg.startsWith("-debug"))
                Log.level = Log.LogLevel.debug;
        }
        if (repo == null) {
            Log.err("The program arguments must contain -R{remoteRepoName}");
            return;
        }
        if (branch == null) {
            Log.err("The program arguments must contain -B{remoteBranch}");
            return;
        }

        load();
    }

    public static void load() {
        // read remote info
        remoteInfo = getRemoteFile(repo, branch, Files.remoteInfoFile.val(), s -> parse(s));
        version = Config.remoteInfoVersion.get(remoteInfo).asFloat(); // idk what to do with this (-_-(
        rootPath = Config.remoteInfoRoot.get(remoteInfo).asString();

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
