package core;

import arc.util.Log;
import com.github.nekit508.mod.annotations.AnnotationProcessor;

@SuppressWarnings("unused")
@AnnotationProcessor
public class Mod extends mindustry.mod.Mod {
    @Override
    public void loadContent() {
        var content = "Hello!";
        Log.info(content);
    }
}
