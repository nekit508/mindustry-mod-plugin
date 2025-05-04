package tools;

import arc.files.Fi;
import arc.util.Log;

public class Tools {
    public static void main(String[] args) {
        var fi = new Fi("");

        Log.info(fi.findAll());
    }
}
