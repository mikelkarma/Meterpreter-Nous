package com.capture;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import android.util.Log;

public class Shell {

    public static String exec(String command) {
        StringBuilder output = new StringBuilder();

        try {
            
            String[] envp = {"HOME=/data/data/com.capture/"};

           
            Process process = Runtime.getRuntime().exec(command, envp);

            // Ler a saída do comando
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            BufferedReader stderr = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String errLine;
            while ((errLine = stderr.readLine()) != null) {
                output.append(errLine).append("\n");
            }

            
            int exitCode = process.waitFor();

            
            output.append("\nExit code: ").append(exitCode);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            output.append("Error executing command: ").append(e.getMessage());
            Log.d("TOR", "[TOR] Error executing Tor");
        }

        return output.toString();
    }
}

