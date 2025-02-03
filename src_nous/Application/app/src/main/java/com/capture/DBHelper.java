package com.capture;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "akina.db";
    private static final int DATABASE_VERSION = 2; // Atualize a versão do banco de dados

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createNotificationTableQuery = "CREATE TABLE notification_table (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "app_name TEXT," +
            "notification_text TEXT," +
            "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP" +
            ")";
        db.execSQL(createNotificationTableQuery);

        String createLocationTableQuery = "CREATE TABLE location_table (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "latitude REAL," +
            "longitude REAL," +
            "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP" +
            ")";
        db.execSQL(createLocationTableQuery);

        String createAudioTableQuery = "CREATE TABLE api_base64 (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "file_path TEXT," +
            "latitude REAL," +
            "longitude REAL," +
            "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP" +
            ")";
        db.execSQL(createAudioTableQuery);
		
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS notification_table");
        db.execSQL("DROP TABLE IF EXISTS location_table");
        db.execSQL("DROP TABLE IF EXISTS api_base64");
        onCreate(db);
    }
}

