package nekit508.mps.net;

import arc.Core;
import arc.files.Fi;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Strings;
import arc.util.Threads;
import arc.util.io.Writes;

import java.io.DataOutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import static mindustry.Vars.*;

public class Transceiver {
    public ServerSocket serverSocket;

    public String state = "disabled";

    public transient double length = 1, processed = 0;
    public transient float progress = 0f;

    public boolean[] forTrans;

    Thread thread;

    public Transceiver() {
        this(new boolean[]{true, true, true, true, true});
    }

    public Transceiver(boolean[] ft) {
        forTrans = ft;
        try {
            serverSocket = new ServerSocket(63369);
            Log.info(serverSocket.getInetAddress().getHostAddress());
            thread = Threads.daemon(() -> {
                try {
                    state = "waiting for connection";
                    Socket socket = serverSocket.accept();
                    transmitData(new Writes(new DataOutputStream(socket.getOutputStream())));
                    socket.close();
                    serverSocket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getState() {
        return serverSocket.isClosed() ? "disabled" : state;
    }

    public void transmitData(Writes writes) {
        try {
            Seq<Fi> files = new Seq<>();
            if (forTrans[0]) files.add(Core.settings.getSettingsFile());
            if (forTrans[1]) files.addAll(customMapDirectory.list());
            if (forTrans[2]) files.addAll(saveDirectory.list());
            if (forTrans[3]) files.addAll(modDirectory.list());
            if (forTrans[4]) files.addAll(schematicDirectory.list());
            String base = Core.settings.getDataDirectory().path();

            length = 0;
            for (Fi file : files) {
                length += file.length();
            }

            writes.d(length);
            writes.i(files.size);

            processed = 0;
            for (Fi file : files) {
                byte[] bytes = file.readBytes();
                String filePath = file.path().substring(base.length());

                state = Strings.format("Transmitting file @ (@ bytes)", filePath, bytes.length);

                writes.str(filePath);
                writes.i(bytes.length);
                for (byte b : bytes) {
                    writes.b(b);
                    processed++;
                    progress = (float) (processed / length);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendCampaign() {

    }

    public void close() {
        try {
            thread.interrupt();
            serverSocket.close();
        } catch (Exception e) {

        }
    }
}
