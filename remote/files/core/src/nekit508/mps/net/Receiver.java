package nekit508.mps.net;

import arc.Core;
import arc.files.Fi;
import arc.util.Log;
import arc.util.Strings;
import arc.util.Threads;
import arc.util.io.Reads;

import java.io.DataInputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import static arc.Core.settings;

public class Receiver {
    public Socket clientSocket;

    public String state = "disabled";

    public transient double length = 1, processed = 0;
    public transient float progress = 0f;

    Thread thread;

    public Receiver() {
        try {
            clientSocket = new Socket();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void connect(String host, int port) {
        try {
            thread = Threads.daemon(() -> {
                try {
                    clientSocket.connect(new InetSocketAddress(host, port));
                    receiveData(new Reads(new DataInputStream(clientSocket.getInputStream())));
                    clientSocket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void receiveData(Reads reads) {
        length = reads.d();
        int count = reads.i();

        for (int i = 0; i < count; i++) {
            String filePath = reads.str();
            int bytesC = reads.i();
            byte[] bytes = new byte[bytesC];

            Fi file = Core.settings.getDataDirectory().child(filePath);
            state = Strings.format("Receiving file @ (@ bytes)", filePath, bytesC);

            for (int j = 0; j < bytesC; j++) {
                bytes[j] = reads.b();
                processed++;
                progress = (float) (processed / length);
            }

            file.writeBytes(bytes);
        }

        settings.clear();
        settings.load();
    }

    public String getState() {
        return clientSocket.isClosed() ? "disabled" : state;
    }

    public void close() {
        try {
            thread.interrupt();
            clientSocket.close();
        } catch (Exception e) {

        }
    }
}
