package com.example.myweather;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SQLiteHelper extends SQLiteOpenHelper {
    public SQLiteHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public void onCreate(SQLiteDatabase database) {
        String createTableQuery = "CREATE TABLE histories ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "province TEXT, "
                + "city TEXT, "
                + "time TEXT, "
                + "temperature TEXT, "
                + "weather TEXT, "
                + "humidity TEXT, "
                + "winddirection TEXT)";
        database.execSQL(createTableQuery);
        createTableQuery = "CREATE TABLE favorites ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "province TEXT, "
                + "city TEXT, "
                + "time TEXT, "
                + "temperature TEXT, "
                + "weather TEXT, "
                + "humidity TEXT, "
                + "winddirection TEXT)";
        database.execSQL(createTableQuery);
    }

    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        String dropTableQuery = "DROP TABLE IF EXISTS favorites";
        database.execSQL(dropTableQuery);
        onCreate(database);
    }

    public boolean insert(String table,String province, String city, String time, String temperature, String weather, String humidity, String winddirection) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("province", province);
        values.put("city", city);
        values.put("time", time);
        values.put("temperature", temperature);
        values.put("weather", weather);
        values.put("humidity", humidity);
        values.put("winddirection", winddirection);
        long insertRows;
        if (table.equals("favorites"))
            insertRows = database.insert("favorites", null, values);
        else
            insertRows = database.insert("histories", null, values);
        values.clear();
//        database.close();
        if (insertRows >= 0)
            return true;
        return false;
    }

    public boolean delete(String table, String city) {
        SQLiteDatabase database = this.getWritableDatabase();
        int deleteRows;
        if (table.equals("favorites"))
            deleteRows = database.delete("favorites", "city LIKE ?", new String[]{city + "%"});
        else
            deleteRows = database.delete("histories", "city LIKE ?", new String[]{city + "%"});
//        database.close();
        if (deleteRows > 0)
            return true;
        return false;
    }

    public boolean update(String table, String id, String province, String city, String time, String temperature, String weather, String humidity, String winddirection) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("province", province);
        values.put("city", city);
        values.put("time", time);
        values.put("temperature", temperature);
        values.put("weather", weather);
        values.put("humidity", humidity);
        values.put("winddirection", winddirection);
        int updateRows;
        if (table.equals("favorites"))
            updateRows = database.update("favorites", values, "id = ?", new String[]{id});
        else
            updateRows = database.update("histories", values, "id = ?", new String[]{id});
        values.clear();
//        database.close();
        if (updateRows > 0)
            return true;
        return false;
    }

    public Map<String, String> getSingleData(String table, String _city) {
        SQLiteDatabase database = this.getWritableDatabase();
        String selectQuery;
        if (table.equals("favorites"))
            selectQuery = "SELECT * FROM favorites WHERE city LIKE ?";
        else
            selectQuery = "SELECT * FROM histories WHERE city LIKE ?";
        Cursor cursor = database.rawQuery(selectQuery, new String[]{_city + "%"});
        Map<String, String> map = new HashMap<>();
        if (cursor.moveToFirst()) {
            String id = cursor.getString(0);
            String province = cursor.getString(1);
            String city = cursor.getString(2);
            String time = cursor.getString(3);
            String temperature = cursor.getString(4);
            String weather = cursor.getString(5);
            String humidity = cursor.getString(6);
            String winddirection = cursor.getString(7);
            map.put("id", id);
            map.put("province", province);
            map.put("city", city);
            map.put("time", time);
            map.put("temperature", temperature);
            map.put("weather", weather);
            map.put("humidity", humidity);
            map.put("winddirection", winddirection);
        }
        cursor.close();
//        database.close();
        return map;
    }

    public List<Map<String, String>> getAllData(String table) {
        SQLiteDatabase database = this.getReadableDatabase();
        String selectQuery;
        if (table.equals("favorites"))
            selectQuery = "SELECT * FROM favorites";
        else
            selectQuery = "SELECT * FROM histories";
        Cursor cursor = database.rawQuery(selectQuery, null);
        List<Map<String, String>> list = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                Map<String, String> map = new HashMap<>();
                String id = cursor.getString(0);
                String province = cursor.getString(1);
                String city = cursor.getString(2);
                String time = cursor.getString(3);
                String temperature = cursor.getString(4);
                String weather = cursor.getString(5);
                String humidity = cursor.getString(6);
                String winddirection = cursor.getString(7);
                map.put("id", id);
                map.put("province", province);
                map.put("city", city);
                map.put("time", time);
                map.put("temperature", temperature);
                map.put("weather", weather);
                map.put("humidity", humidity);
                map.put("winddirection", winddirection);
                list.add(map);
            } while (cursor.moveToNext());
        }
        cursor.close();
//        database.close();
        return list;
    }
}
