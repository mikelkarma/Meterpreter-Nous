package com.capture;

import android.util.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class TorThread extends Thread {
    private static boolean torAlreadyRunning = false;

    @Override
    public void run() {
        
        synchronized (TorThread.class) {
            if (torAlreadyRunning) {
                Log.d("TOR", "O processo libTor.so já está em execução. Thread bloqueada.");
                return; 
            }
            torAlreadyRunning = true; 
        }

        try {
            
            String Tor = Shell.exec("./data/data/com.capture/tor/tor/libTor.so");
            Log.d("TOR", "[EXECUTANDO] " + Tor);
        } finally {
            
            synchronized (TorThread.class) {
                torAlreadyRunning = false;
            }
        }
    }
}

