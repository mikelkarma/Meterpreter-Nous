package com.capture;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.media.projection.MediaProjectionManager;
import android.media.projection.MediaProjection;
import android.hardware.display.VirtualDisplay;
import android.media.ImageReader;
import android.media.Image;
import android.graphics.Bitmap;
import android.os.Environment;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.net.Uri;
import android.widget.Toast;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ScreenCaptureService extends Service {
    private static final int REQUEST_SCREEN_CAPTURE = 1001;
    private MediaProjectionManager projectionManager;
    private MediaProjection mediaProjection;
    private VirtualDisplay virtualDisplay;
    private ImageReader imageReader;
    private ScheduledExecutorService scheduler;
    private String imagePath;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        
        int resultCode = intent.getIntExtra("resultCode", Activity.RESULT_CANCELED);
        Intent data = intent.getParcelableExtra("data");

        
        if (resultCode == Activity.RESULT_OK && data != null) {
            
            projectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
            mediaProjection = projectionManager.getMediaProjection(resultCode, data);
            startScreenCapture(resultCode, data);
        } else {
            stopSelf();
        }

        return START_STICKY;
    }

    private void startScreenCapture(int resultCode, Intent data) {
        
        WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getRealMetrics(metrics);
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        int density = metrics.densityDpi;

       
        imageReader = ImageReader.newInstance(width, height, 0x1, 2);

        
        virtualDisplay = mediaProjection.createVirtualDisplay(
			"ScreenCapture",
			width, height, density,
			DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
			imageReader.getSurface(), null, null
        );

        
        String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/.nous/";
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        imagePath = dirPath + "screen.nous";

        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(new Runnable() {
				@Override
				public void run() {
					captureFrame();
				}
			}, 0, 100, TimeUnit.MILLISECONDS);
    }

    private void captureFrame() {
        Image image = imageReader.acquireLatestImage();
        if (image != null) {
            Bitmap bitmap = imageToBitmap(image);
            saveImage(bitmap);
            image.close();
        }
    }

    private Bitmap imageToBitmap(Image image) {
        int width = image.getWidth();
        int height = image.getHeight();

        Image.Plane plane = image.getPlanes()[0];
        ByteBuffer buffer = plane.getBuffer();
        int pixelStride = plane.getPixelStride();
        int rowStride = plane.getRowStride();
        int rowPadding = rowStride - pixelStride * width;

        Bitmap bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(buffer);

        return Bitmap.createBitmap(bitmap, 0, 0, width, height);
    }

    private void saveImage(Bitmap bitmap) {
        try {
            File file = new File(imagePath);
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopScreenCapture();
    }

    private void stopScreenCapture() {
        if (scheduler != null) {
            scheduler.shutdown();
        }
        if (mediaProjection != null) {
            mediaProjection.stop();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
