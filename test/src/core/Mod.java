package core;

import com.github.nekit508.mod.annotations.AnnotationProcessor;
import core.gen.Newc;
import ent.anno.Annotations;
import lombok.Getter;
import lombok.Setter;
import mindustry.gen.Builderc;
import mindustry.gen.Unitc;
import mindustry.type.UnitType;

@SuppressWarnings("unused")
@AnnotationProcessor
public class Mod extends mindustry.mod.Mod {
    public static @Annotations.EntityDef({Unitc.class, Builderc.class, Newc.class}) UnitType type;

    static {
        var t = new TestLombokGenerated();
        var tTest = t.test(1).test();
    }

    @Getter
    @Setter
    private static class TestLombokGenerated {
        int test;
    }
}
