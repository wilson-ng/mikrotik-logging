package org.personal.cctv;

import com.github.shyiko.dotenv.DotEnv;
import me.legrange.mikrotik.ApiConnection;
import me.legrange.mikrotik.MikrotikApiException;

import java.applet.Applet;
import java.applet.AudioClip;
import java.util.List;
import java.util.Map;

public class Application {

    public static void main (String... args) throws Exception {
        System.out.println("Eyes on Eyes");
        Map<String, String> dotEnv = DotEnv.load();

        int lastPacket = 0;
        boolean isLogin = false;
        while (!isLogin) {
            System.out.println("Initializing Connection...");
            ApiConnection apiConnection = null;
            try {
                apiConnection = ApiConnection.connect(dotEnv.get("MIKROTIK_HOST"));
                apiConnection.login(dotEnv.get("MIKROTIK_USERNAME"), dotEnv.get("MIKROTIK_PASSWORD"));
                apiConnection.setTimeout(5000);
            } catch (MikrotikApiException mikrotikException) {
                System.out.println("Rerty Connection in 30s");
                Thread.sleep(30000);
                continue;
            }

            boolean isConnected = apiConnection.isConnected();
            System.out.println(isConnected ? "Connected" : "Disconnected");
            while (isConnected) {
                int packets = 0;

                try {
                    List<Map<String, String>> rs = apiConnection.execute("/ip/firewall/mangle/print where log-prefix=Monitor-on");
                    for (Map<String, String> r : rs) {
                        packets = Integer.parseInt(r.get("packets"));
                        if (0 == lastPacket) {
                            lastPacket = packets;
                        }
                        break;
                    }

                    if ((packets - lastPacket) > 10) {
                        System.out.println("Warning");

                        AudioClip ac = Applet.newAudioClip(Application.class.getResource("bell.wav"));
                        ac.loop();
                        Thread.sleep(5000);
                        ac.stop();
                    }

                    lastPacket = packets;
                    Thread.sleep(5000);
                } catch (MikrotikApiException mikrotikException) {
                    System.out.println("Disconnected");
                    isConnected = false;
                }
            }

            isLogin = false;
        }
    }
}
