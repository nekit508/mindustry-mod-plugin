package com.nekit508;

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
    public static InputStream getRemoteFile(Files file) {
        return getRemoteFile(rootPath, file.val());
    }

    /** Returns stream to file in current root **/
    public static InputStream getRemoteFile(Files file, Object... format) {
        return getRemoteFile(rootPath, file.val(format));
    }

    /** Returns stream to file in current root **/
    public static InputStream getRemoteFile(String file) {
        return getRemoteFile(rootPath, file);
    }

    /** Returns stream to file in branch in repo **/
    public static InputStream getRemoteFile(String repo, String branch, String file) {
        return getRemoteFile(repo + "/" + branch, file);
    }

    /** Returns stream to file in specified root **/
    public static InputStream getRemoteFile(String rootPath, String file) {
        InputStream[] out = new InputStream[1];
        Http.get("https://raw.githubusercontent.com/%s/%s".formatted(rootPath, file)).block(r -> {
            out[0] = r.getResultAsStream();
        });
        return out[0];
    }

    public static JsonValue parse(InputStream stream) {
        return jsonReader.parse(stream);
    }

    public static void main(String[] args) {
        for (String arg : args) {
            if (arg.startsWith("-R"))
                repo = arg.substring(2);
            if (arg.startsWith("-B"))
                repo = arg.substring(2);
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
        remoteInfo = parse(getRemoteFile(repo, branch, ""));
        version = Config.remoteInfoVersion.get(remoteInfo).asFloat(); // idk what to do with this (-_-(
        rootPath = Config.remoteInfoRoot.get(remoteInfo).asString();

        // extensions
        extensionsList = parse(getRemoteFile(Files.remoteInfoFile));
        String[] extensionsDirs = Config.extensionsListList.get(extensionsList).asStringArray();

        // create extensions list
        Seq<Extension> extensions = new Seq<>();

        for (String extensionDir : extensionsDirs) {
            Extension extension = new Extension(extensionDir);

            extension.parse(parse(getRemoteFile(Files.extensionInfo, extensionDir)));
        }

        // handle dependencies and create loading queue
        Seq<Extension> loadingQueue = new Seq<>();

        for (Extension extension : extensions) {
            loadingQueue.add(extension);

            for (String dependency : extension.dependencies) {
                Extension dep = extensions.find(e -> e.name.equals(dependency));
                if (dep == null)
                    Log.err("Missing dependency @ for extension @.", dependency, extension.name);
            }
        }
    }
}
