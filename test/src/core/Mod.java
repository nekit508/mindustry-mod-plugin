package core;

import arc.util.Log;
import com.github.nekit508.mod.annotations.AnnotationProcessor;
import core.gen.Newc;
import ent.anno.Annotations;
import mindustry.gen.Builderc;
import mindustry.gen.Unitc;
import mindustry.type.UnitType;

@SuppressWarnings("unused")
@AnnotationProcessor
public class Mod extends mindustry.mod.Mod {
    public static @Annotations.EntityDef({Unitc.class, Builderc.class, Newc.class}) UnitType type;

    @Override
    public void loadContent() {
        Log.info("Loaded @.", getClass().getCanonicalName());
    }
}
