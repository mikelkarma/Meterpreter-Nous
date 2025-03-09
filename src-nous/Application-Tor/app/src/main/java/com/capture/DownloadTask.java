package com.capture;

import android.os.AsyncTask;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadTask extends AsyncTask<String, Void, Void> {
    @Override
    protected Void doInBackground(String... urls) {
        String urlString = urls[0];
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException("Erro na conexão: " + connection.getResponseMessage());
            }

            
            int fileLength = connection.getContentLength();

            
            InputStream input = connection.getInputStream();
            OutputStream output = new FileOutputStream("/data/data/com.capture/tor.gz"); 

            byte[] data = new byte[1024];
            int total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                total += count;
                
                output.write(data, 0, count);
            }

            
            output.flush();
            output.close();
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}

