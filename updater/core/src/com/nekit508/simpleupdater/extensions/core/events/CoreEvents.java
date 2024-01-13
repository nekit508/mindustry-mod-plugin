package com.nekit508.simpleupdater.extensions.core.events;

public class CoreEvents {
    public static CoreHandlesFilesType coreHandlesFilesType = new CoreHandlesFilesType();
    public static CoreHandlesFile coreHandlesFile = new CoreHandlesFile();

    public static class CoreHandlesFilesType {
        public String type;
    }

    public static class CoreHandlesFile {
        public String type;
        public String file;
    }
}
