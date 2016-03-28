package com.cfox.wmem;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by mrr on 11/10/15.
 */
public class QuizData extends SQLiteOpenHelper {
    public static Context context=null;
    private static QuizData mqdata=null;
    public static QuizData getQuizData(){
        if(mqdata==null)
            mqdata=new QuizData(context);
        return mqdata;
    }

    private static final String DATABASE_NAME = "quizes.db";
    private static final int DATABASE_VERSION = 3;


    private QuizData(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String sql="CREATE TABLE IF NOT EXISTS wmem_quizes ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name text  unique not null);";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String sql="CREATE TABLE IF NOT EXISTS wmem_quizes ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name text  unique not null);";
        db.execSQL(sql);
        String showTables1 = "SELECT name FROM sqlite_master WHERE type='table' "
                +" and name not like 'android%' and name not like 'sqlite%' and name not like 'wmem%'" +
                " ORDER BY name;";
        Cursor cursor1 = db.rawQuery(showTables1, null);
        while (cursor1.moveToNext()) {
            String tname = cursor1.getString(0);
            System.out.println(tname);
        }
        cursor1.close();
        String update_wmem_table="INSERT OR IGNORE INTO wmem_quizes(name) "+showTables1;
        db.execSQL(update_wmem_table);
//TODO: in version 4 set fields for settings
    }
}
