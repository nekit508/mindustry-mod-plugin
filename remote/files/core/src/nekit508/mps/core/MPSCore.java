package nekit508.mps.core;

import mindustry.mod.Mod;
import nekit508.mps.ui.MPSUI;

public class MPSCore extends Mod {
    @Override
    public void init() {
        new MPSUI();

        MPSVars.ui.build();
    }
}
