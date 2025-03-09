package com.capture;

import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import com.capture.DBHelper;
import android.app.Notification;

public class NotificationListener extends NotificationListenerService {

    private static final String TAG = "NotificationListener";
    private static final String FILE_NAME = "notification_log.txt";
    private DBHelper dbHelper;
    private SimpleDateFormat dateFormat;

    @Override
    public void onCreate() {
        super.onCreate();
        dbHelper = new DBHelper(this);
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);
        String appName = getAppName(sbn.getPackageName());
        CharSequence notificationText = sbn.getNotification().extras.getCharSequence(Notification.EXTRA_TEXT);
        if (notificationText != null) {
            saveNotificationToLogFile(appName, notificationText.toString());
            saveNotificationToDatabase(appName, notificationText.toString());
        } else {
            Log.e(TAG, "Notification text is null");
        }
    }

    private String getAppName(String packageName) {
        PackageManager packageManager = getPackageManager();
        try {
            return (String) packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA));
        } catch (PackageManager.NameNotFoundException e) {
            return packageName;
        }
    }

    private void saveNotificationToLogFile(String appName, String notificationText) {
        String logMessage = getCurrentDateTime() + " - " + appName + ": " + notificationText + "\n";
        File logFile = new File(getLogFilePath());
        try {
            FileWriter writer = new FileWriter(logFile, true);
            writer.append(logMessage);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            Log.e(TAG, "Error writing to log file: " + e.getMessage());
        }
    }

    private String getLogFilePath() {
        File dir = new File(getFilesDir(), "logs");
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                Log.e(TAG, "Failed to create directory: " + dir.getAbsolutePath());
            }
        }
        return new File(dir, FILE_NAME).getAbsolutePath();
    }

    private void saveNotificationToDatabase(String appName, String notificationText) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("app_name", appName);
        values.put("notification_text", notificationText);
        values.put("timestamp", getCurrentDateTime());
        long result = db.insert("notification_table", null, values);
        if (result == -1) {
            Log.e(TAG, "Error inserting notification into database");
        }
        db.close();
    }

    private String getCurrentDateTime() {
        return dateFormat.format(new Date());
    }
}

