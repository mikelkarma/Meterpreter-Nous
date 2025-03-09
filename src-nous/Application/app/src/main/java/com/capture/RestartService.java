package com.capture;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;



public class RestartService extends Service {
    public RestartService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    
	public int onStartCommand(Intent intent, int flags, int startId) {
		Notification.Builder builder;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			NotificationChannel channel = new NotificationChannel("default", "기본 채널", NotificationManager.IMPORTANCE_NONE);
			NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			manager.createNotificationChannel(channel);

			builder = new Notification.Builder(this, "default")
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(null)
                .setContentText(null)
                .setContentIntent(createPendingIntent())
                .setChannelId("default");
		} else {
			builder = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(null)
                .setContentText(null)
                .setContentIntent(createPendingIntent());
		}

		Notification notification = builder.build();
		startForeground(9, notification);
		Intent in = new Intent(this, RealService.class);
		startForegroundService(in);
		return START_NOT_STICKY;
	}

	private PendingIntent createPendingIntent() {
		Intent notificationIntent = new Intent(this, MainActivity.class);
		return PendingIntent.getActivity(this, 0, notificationIntent, 0);
	}
	

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

}
