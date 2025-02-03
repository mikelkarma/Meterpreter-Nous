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
            serviceIntent = new Intent(this, RealService.class);
            startService(serviceIntent);
	
        } else {
            serviceIntent = RealService.serviceIntent;
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
