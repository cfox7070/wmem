package com.cfox.wmem;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;

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

        String sql="CREATE TABLE IF NOT EXISTS quizsettings ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                " qname TEXT UNIQUE NOT NULL," +
                " numvar INTEGER NOT NULL DEFAULT 5," +
                " dirreps INTEGER DEFAULT 2," +
                " revreps INTEGER DEFAULT 1," +
                " intervals TEXT NOT NULL DEFAULT \' 10 20 30 \');" ;
        db.execSQL(sql);
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(newVersion==4){
            String sql = "DROP TABLE IF EXISTS wmem_quizes;" ;
            db.execSQL(sql);
            sql="CREATE TABLE IF NOT EXISTS quizsettings ("
                    + "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        " qname TEXT UNIQUE NOT NULL," +
                        " numvar INTEGER NOT NULL DEFAULT 5," +
                        " dirreps INTEGER DEFAULT 2," +
                        " revreps INTEGER DEFAULT 1," +
                    " intervals TEXT NOT NULL DEFAULT \' 10 20 30 \');" ;
            db.execSQL(sql);
        }
//TODO: in version 4 set fields for settings
    }

    /**
     *
     * @param name
     * @return id of table or -1
     */
    public int findTable(String name){
        return 0;
    }

    public void addTable(String name){
        SQLiteDatabase db = getWritableDatabase();
        String sql = "CREATE TABLE IF NOT EXISTS " + name + " ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "word text unique not null, " +
                "trans text not null, " +
                "session integer default 0, " +
                "datetime integer default 0);";
            db.execSQL(sql);
     }

    public Cursor getWordTables(){
        String showTables1 = "SELECT name FROM sqlite_master WHERE type='table' "
                + " and name not like 'android%' and name not like 'sqlite%' and name not like 'wmem%'" +
                " ORDER BY name;";
        return  getReadableDatabase().rawQuery(showTables1, null);
    }

    public void delTable(String name) {
        SQLiteDatabase db = getWritableDatabase();
        String sql = "DROP TABLE IF EXISTS " + name + ";";
        db.execSQL(sql);
    }

    public void addQuizSettings(String name,int nvar,int dreps,int rreps,String intervals) {
        if(name==null || intervals==null)
            return;
        SQLiteDatabase db = getWritableDatabase();
         String winsert="INSERT OR IGNORE INTO quizsettings (qname,numvar,dirreps,revreps,intervals) VALUES (?, ?, ?, ?,?);";
        db.execSQL(winsert,new String[]{name,
                                        String.valueOf(nvar),
                                        String.valueOf(dreps),
                                        String.valueOf(rreps),
                                        intervals});
    }


    public void updateQuizSettings(String name,int nvar,int dreps,int rreps,String intervals) {
        if (name == null || intervals == null)
            return;
        String upd = "UPDATE quizsettings SET (numvar="+nvar+
                                            ",dirreps="+dreps+
                                            ",revreps="+rreps+
                                            ",intervals='"+intervals+
                                            "') WHERE name="+name+";";
        getWritableDatabase().execSQL(upd);
    }

    public Cursor getQuizSettings(String name) {
        if(nullOrEmpty(name))
            return null;
        SQLiteDatabase db = getReadableDatabase();
        String sql =  "SELECT numvar,dirreps,revreps,intervals FROM quizsettings WHERE name="+name+";";
        Cursor cur=db.rawQuery(sql,null);
        return cur;
     }

    public int addWord(String table,String word,String trans){
        if(nullOrEmpty(table) ||nullOrEmpty(word) ||nullOrEmpty(trans))
            return getWordCount(table);
        SQLiteDatabase db=getWritableDatabase();
        word= DatabaseUtils.sqlEscapeString(word);
        trans=DatabaseUtils.sqlEscapeString(trans);
        String winsert="INSERT OR IGNORE INTO "+table+" (word,trans,session,datetime) VALUES (?, ?, 0, 0);";
        db.execSQL(winsert,new String[]{word,trans});
        return getWordCount(table);
    }

    public int getWordCount(String table){
        if(nullOrEmpty(table))
            return -1;
        final String count="SELECT COUNT(*) FROM ";
        int c=0;
        SQLiteDatabase db=getReadableDatabase();
        String cnt=count+table+";";
        Cursor cur=db.rawQuery(cnt, null);
        boolean r=cur.moveToFirst();
        if(r)
            c=cur.getInt(0);
        cur.close();
        return c;
    }
    public int getWordCount(String table,int ses){
        if(nullOrEmpty(table))
            return -1;
        final String count="SELECT COUNT(*) FROM "+table+" WHERE (session = ?);";
        int c=0;
        SQLiteDatabase db=getReadableDatabase();
        Cursor cur=db.rawQuery(count, new String[]{String.valueOf(ses)});
        boolean r=cur.moveToFirst();
        if(r)
            c=cur.getInt(0);
        cur.close();
        return c;
    }

    public Cursor getWords(String table,String conditions, int vars){
        if(nullOrEmpty(table))
            return null;
        String getWords = "SELECT _id, word, trans, session, datetime FROM " + table +
                " WHERE ("+conditions+") ORDER BY session DESC LIMIT "+vars+";";
        Cursor cursor = getReadableDatabase().rawQuery(getWords,null);
        return cursor;
        }

    public void updateWord(String table,String word,int ses,long time){
        if (nullOrEmpty(table)|| nullOrEmpty(word))
            return;
        String upd = "UPDATE "+table+" SET (session="+ses+",datetime="+time+") WHERE word="+DatabaseUtils.sqlEscapeString(word)+";";
        getWritableDatabase().execSQL(upd);
    }

    private boolean nullOrEmpty(String str){
        return str==null || str.equals("");
    }
}
