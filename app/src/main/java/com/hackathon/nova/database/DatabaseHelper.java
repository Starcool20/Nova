package com.hackathon.nova.database;

import android.content.Context;
import android.database.Cursor;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory;
import com.hackathon.nova.database.Data;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper {

  private static final String DATABASE_NAME = "nova_msg_history.db";
  private static final int DATABASE_VERSION = 1; // Incremented due to schema change

  private final SupportSQLiteOpenHelper helper;

  public DatabaseHelper(Context context) {
    SupportSQLiteOpenHelper.Configuration configuration =
        SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(DATABASE_NAME)
            .callback(
                new SupportSQLiteOpenHelper.Callback(DATABASE_VERSION) {
                  @Override
                  public void onCreate(SupportSQLiteDatabase db) {
                    db.execSQL(
                        "CREATE TABLE my_table (id INTEGER PRIMARY KEY, name TEXT, key TEXT)");
                  }

                  @Override
                  public void onUpgrade(SupportSQLiteDatabase db, int oldVersion, int newVersion) {
                    db.execSQL("DROP TABLE IF EXISTS my_table");
                    onCreate(db); // Recreate the table with the updated schema
                  }
                })
            .build();

    helper = new FrameworkSQLiteOpenHelperFactory().create(configuration);
  }

  private SupportSQLiteDatabase getDatabase() {
    return helper.getWritableDatabase();
  }

  public void insertData(String name, String key) {
    SupportSQLiteDatabase db = getDatabase();
    db.execSQL("INSERT INTO my_table (name, key) VALUES (?, ?)", new Object[] {name, key});
  }

  public List<Data> getAllData() {
    SupportSQLiteDatabase db = getDatabase();
    Cursor cursor = db.query("SELECT * FROM my_table");
    List<Data> dataList = new ArrayList<>();

    if (cursor.moveToFirst()) {
      do {
        String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
        String key = cursor.getString(cursor.getColumnIndexOrThrow("key"));
        dataList.add(new Data(name, key));
      } while (cursor.moveToNext());
    }
    cursor.close();
    return dataList;
  }

  public void updateData(int id, String newName, String newKey) {
    SupportSQLiteDatabase db = getDatabase();
    db.execSQL(
        "UPDATE my_table SET name = ?, key = ? WHERE id = ?", new Object[] {newName, newKey, id});
  }

  public void deleteData(int id) {
    SupportSQLiteDatabase db = getDatabase();
    db.execSQL("DELETE FROM my_table WHERE id = ?", new Object[] {id});
  }

  public void closeDatabase() {
    helper.close();
  }
}
