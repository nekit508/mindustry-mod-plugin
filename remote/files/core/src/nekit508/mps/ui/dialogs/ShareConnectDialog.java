package nekit508.mps.ui.dialogs;

import arc.scene.ui.TextButton;
import arc.scene.ui.TextField;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectMap;
import arc.util.Strings;
import arc.util.Time;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.ui.Bar;
import mindustry.ui.dialogs.BaseDialog;
import nekit508.mps.net.Receiver;
import nekit508.mps.net.Transceiver;

public class ShareConnectDialog extends BaseDialog {
    public Table all = new Table();

    public Transceiver transceiver;
    public Receiver receiver;

    private double prev = 0;
    private String delta = "";
    private long lastPrevUpdate = 0;
    private boolean[] forTrans = new boolean[]{true, true, true, false, true};

    public int mode = Integer.MIN_VALUE;
    public ObjectMap<Integer, Runnable> modeInterfaces = ObjectMap.of(
            0, (Runnable) () -> {
                {
                    all.defaults().growX();
                    all.check("settings", b -> forTrans[0] = b).get().setChecked(forTrans[0]);
                    all.row();
                    all.check("maps", b -> forTrans[1] = b).get().setChecked(forTrans[1]);
                    all.row();
                    all.check("saves", b -> forTrans[2] = b).get().setChecked(forTrans[2]);
                    all.row();
                    all.check("mods", b -> forTrans[3] = b).get().setChecked(forTrans[3]);
                    all.row();
                    all.check("schematics", b -> forTrans[4] = b).get().setChecked(forTrans[4]);
                    /*all.row();
                    all.button("campaign", () -> rebuild(4));*/
                }

                all.row();
                all.image(Tex.whiteui, Pal.accent).fillX().height(3f).pad(4f);
                all.row();

                all.button("Receive progress", () -> rebuild(1)).size(320, 32);
                all.row();
                all.button("Transmit progress", () -> rebuild(2)).size(320, 32).fill();
            },

            1, (Runnable) () -> {
                if (transceiver != null) {
                    transceiver.close();
                    transceiver = null;
                }
                receiver = new Receiver();

                TextField ipInput = all.field("IP:port", s -> {
                }).expandX().get();
                ipInput.setValidator(s -> {
                    String[] t = s.split(":");
                    if (t.length < 2) return false;
                    String ip = t[0];
                    String port = t[1];
                    return Strings.canParseInt(port);
                });
                all.row();
                TextButton btn = all.button("Receive", () -> {
                    String[] t = ipInput.getText().split(":");
                    String ip = t[0];
                    int port = Integer.parseInt(t[1]);
                    receiver.connect(ip, port);
                    rebuild(3);
                }).get();
                btn.setDisabled(() -> !ipInput.isValid());
            },

            3, (Runnable) () -> {
                all.label(() -> receiver.getState());
                all.row();
                all.add(new Bar(() -> {
                    if (Time.millis() - lastPrevUpdate >= 1000) {
                        delta = Strings.fixed((float) ((receiver.processed - prev) / Time.delta), 1);
                        prev = receiver.processed;
                        lastPrevUpdate = Time.millis();
                    }
                    return Strings.format("@% (@/@) @@/s",
                            Strings.fixed(receiver.progress * 100, 1), (long) receiver.processed,
                            (long) receiver.length, delta, "b");
                }, () -> Pal.bar, () -> receiver.progress)).growX();
            },

            2, (Runnable) () -> {
                if (receiver != null) {
                    receiver.close();
                    receiver = null;
                }
                transceiver = new Transceiver(forTrans);

                all.label(() -> Strings.format("@:@",
                        transceiver.serverSocket.getInetAddress().getHostAddress(),
                        transceiver.serverSocket.getLocalPort()));
                all.row();
                all.label(() -> transceiver.getState());
                all.row();
                all.add(new Bar(() -> {
                    if (Time.millis() - lastPrevUpdate >= 1000) {
                        delta = Strings.fixed((float) ((transceiver.processed - prev) / Time.delta), 1);
                        prev = transceiver.processed;
                        lastPrevUpdate = Time.millis();
                    }
                    return Strings.format("@% (@/@) @@/s",
                            Strings.fixed(transceiver.progress * 100, 1), (long) transceiver.processed,
                            (long) transceiver.length, delta, "b");
                }, () -> Pal.bar, () -> transceiver.progress)).growX();
            },

            4, (Runnable) () -> {

            }
    );

    public ShareConnectDialog() {
        super("@shareconnect");

        shouldPause = true;

        addCloseButton();

        shown(() -> rebuild(mode));
        resized(() -> rebuild(mode));
        hidden(() -> {
            if (receiver != null)
                receiver.close();
            if (transceiver != null)
                transceiver.close();
        });

        all.setFillParent(true);
        all.defaults().expand().pad(5).height(32);
        cont.pane(all).scrollX(false).scrollY(true);
    }

    public void rebuild(int mode) {
        if (this.mode != mode) {
            this.mode = mode;

            all.clear();

            modeInterfaces.get(this.mode).run();
        }
    }
}
