package nekit508.mps.ui;

import mindustry.Vars;
import mindustry.gen.Icon;
import mindustry.ui.fragments.MenuFragment;
import nekit508.mps.core.MPSVars;
import nekit508.mps.ui.dialogs.ShareConnectDialog;

public class MPSUI {
    public ShareConnectDialog shareConnectDialog;

    public MPSUI() {
        MPSVars.ui = this;
        shareConnectDialog = new ShareConnectDialog();
    }

    public void build() {
        Vars.ui.menufrag.addButton(new MenuFragment.MenuButton("Share progress", Icon.export, () -> {
            shareConnectDialog.show();
            shareConnectDialog.rebuild(0);
        }));
    }
}
