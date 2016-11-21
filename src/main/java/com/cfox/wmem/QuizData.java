package com.cfox.wmem;

/**
 * Created by mrr on 11/10/15.
 */

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static android.database.DatabaseUtils.sqlEscapeString;
import static com.cfox.wmem.DataContract.qs;
import static com.cfox.wmem.DataContract.w;

@SuppressWarnings("WeakerAccess")
class DataContract{


    public static class qs{ //quiz settiongs
        public static final String tablename="quizsettings";
        public static final String id="_id";
        public static final String qname="qname";
        public static final String numvar="numvar";
        public static final String dirreps="dirreps";
        public static final String revreps="revreps";
        public static final String intervals="intervals";
        //todo: there in next version:
//        public static final String langword="langword";
//        public static final String langtrans="langtrans";
    }

    public static class w{ //words
        public static final String id="_id";
         public static final String word="word";
        public static final String trans="trans";
        public static final String session="session";
        public static final String datetime="datetime";
   }

}

@SuppressWarnings("WeakerAccess")
public class QuizData extends SQLiteOpenHelper {

    private static QuizData mqdata=null;

    public static void initQuizdata(Context context){
        if(mqdata==null)
            mqdata=new QuizData(context.getApplicationContext());
    }

    public static QuizData getQuizData(){
        if(mqdata==null)
            throw new RuntimeException("QuizData must be initialized");
        return mqdata;
    }

    private static final String DATABASE_NAME = "quizes.db";
    private static final int DATABASE_VERSION = 5;

    public static String unescapeString(String arg){
        arg=arg.replace("''","'");
        if(arg.charAt(0)=='\'') arg=arg.substring(1);
        if(arg.charAt(arg.length()-1)=='\'') arg=arg.substring(0,arg.length()-1);
        return arg;
    }

    private QuizData(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        
        String sql="CREATE TABLE IF NOT EXISTS "+qs.tablename+" ("
                + qs.id+" INTEGER PRIMARY KEY AUTOINCREMENT, "
                + qs.qname+" TEXT UNIQUE NOT NULL," +
                qs.numvar+" INTEGER NOT NULL DEFAULT 5," +
                qs.dirreps+" INTEGER DEFAULT 2," +
                qs.revreps+" INTEGER DEFAULT 1," +
                qs.intervals+" TEXT NOT NULL DEFAULT \' 10 20 30 \');" ;
        db.execSQL(sql);
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(newVersion==4){
            String sql = "DROP TABLE IF EXISTS wmem_quizes;" ;
            db.execSQL(sql);
            sql="CREATE TABLE IF NOT EXISTS "+qs.tablename+" ("
                    + qs.id+" INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + qs.qname+" TEXT UNIQUE NOT NULL," +
                    qs.numvar+" INTEGER NOT NULL DEFAULT 5," +
                    qs.dirreps+" INTEGER DEFAULT 2," +
                    qs.revreps+" INTEGER DEFAULT 1," +
                    qs.intervals+" TEXT NOT NULL DEFAULT \' 10 20 30 \');" ;
            db.execSQL(sql);
            String showTables1 = "SELECT name FROM sqlite_master WHERE type='table' "
                    + " and name not like 'android%' and name not like 'sqlite%' and name not like 'wmem%'and name not like 'quizsettings'" +
                    " ORDER BY name;";
            Cursor cur=db.rawQuery(showTables1,null);
            //long mh=3600000;
            String intrvls=(8)+" "+(24 * 2)+" "+(24*7)+" "+(24*7*2);
            for(cur.moveToFirst();!cur.isAfterLast();cur.moveToNext()){
                String tname=cur.getString(0);
                addQuizSettings(db,tname,5,2,1,intrvls);
            }
            cur.close();
        }else if(newVersion==5){
            String showTables1 = "SELECT name FROM sqlite_master WHERE type='table' "
                    + " and name not like 'android%' and name not like 'sqlite%' and name not like 'wmem%'and name not like 'quizsettings'" +
                    " ORDER BY name;";
            Cursor cur=db.rawQuery(showTables1,null);
            String intrvls=(8)+" "+(24 * 2)+" "+(24*7)+" "+(24*7*2);
            for(cur.moveToFirst();!cur.isAfterLast();cur.moveToNext()){
                String tname=cur.getString(0);
                tname=unescapeString(tname);
                updateQuizSettings(db,tname,5,2,1,intrvls);
            }
            cur.close();
        }
    }

    public void addTable(String name){
        String sname=sqlEscapeString(name);
        SQLiteDatabase db = getWritableDatabase();
        String sql = "CREATE TABLE IF NOT EXISTS " + sname + " ("
                + w.id+" INTEGER PRIMARY KEY AUTOINCREMENT, " +
                w.word+" text unique not null, " +
                w.trans+" text not null, " +
                w.session+" integer default 0, " +
                w.datetime+" integer default 0);";
            db.execSQL(sql);
     }

    public Cursor getWordTables(){
        String showTables1 = "SELECT name FROM sqlite_master WHERE type='table' "
                + " and name not like 'android%' and name not like 'sqlite%' and name not like 'wmem%'and name not like 'quizsettings'" +
                " ORDER BY name;";
        return  getReadableDatabase().rawQuery(showTables1, null);
    }

    public boolean tableExists(String tname){
        String sname=DatabaseUtils.sqlEscapeString(tname);
        String showTables1 = "SELECT name FROM sqlite_master WHERE type='table' "
                + " and name=" +sname+";";
        Cursor cur=getReadableDatabase().rawQuery(showTables1, null);
        boolean res=cur.getCount()!=0;
        cur.close();
        return res;
    }

    public void delTable(String name) {
        name=sqlEscapeString(name);
        SQLiteDatabase db = getWritableDatabase();
        String sql = "DROP TABLE IF EXISTS " + name + ";";
        db.execSQL(sql);
    }

    private void addQuizSettings(SQLiteDatabase db,String name,int nvar,int dreps,int rreps,String intervals) {
        if(name==null || intervals==null)
            return;
        String winsert="INSERT OR IGNORE INTO quizsettings (qname,numvar,dirreps,revreps,intervals) VALUES (?, ?, ?, ?,?);";
        db.execSQL(winsert,new String[]{sqlEscapeString(name),
                String.valueOf(nvar),
                String.valueOf(dreps),
                String.valueOf(rreps),
                sqlEscapeString(intervals)});
    }
    public void addQuizSettings(String name,int nvar,int dreps,int rreps,String intervals) {
        if(name==null || intervals==null)
            return;
        SQLiteDatabase db = getWritableDatabase();
        addQuizSettings(db,name,nvar,dreps,rreps,intervals);
    }

    public void delQuizSettings(String name) {
        if (name == null)
            return;
        name=sqlEscapeString(name);
         String upd = "DELETE FROM quizsettings WHERE qname="+name+";";
        getWritableDatabase().execSQL(upd);
    }

    private void updateQuizSettings(SQLiteDatabase db,String name,int nvar,int dreps,int rreps,String intervals) {
        if (name == null || intervals == null)
            return;
        name=sqlEscapeString(name);
        intervals=sqlEscapeString(intervals);
        String upd = "UPDATE quizsettings SET numvar="+nvar+
                                            ",dirreps="+dreps+
                                            ",revreps="+rreps+
                                            ",intervals="+intervals+
                                            " WHERE qname="+name+";";
        db.execSQL(upd);
    }
    public void updateQuizSettings(String name,int nvar,int dreps,int rreps,String intervals) {
        updateQuizSettings(getWritableDatabase(),name,nvar,dreps,rreps,intervals);
    }

    public Cursor getQuizSettings(String name) {
        if(nullOrEmpty(name))
            return null;
        name=sqlEscapeString(name);
        SQLiteDatabase db = getReadableDatabase();
        String sql =  "SELECT numvar,dirreps,revreps,intervals FROM quizsettings WHERE qname="+name+";";
        return db.rawQuery(sql,null);
     }

    public int addWord(String table,String word,String trans){
        if(nullOrEmpty(table) ||nullOrEmpty(word) ||nullOrEmpty(trans))
            return getWordCount(table);
        table=sqlEscapeString(table);
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
        table=sqlEscapeString(table);
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
        table=sqlEscapeString(table);
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
        table=sqlEscapeString(table);
        String getWords = "SELECT _id, word, trans, session, datetime FROM " + table +
                " WHERE ("+conditions+") ORDER BY session DESC LIMIT "+vars+";";
        return getReadableDatabase().rawQuery(getWords,null);
        }

    public Cursor getWords(String table){
        if(nullOrEmpty(table))
            return null;
        table=sqlEscapeString(table);
        String getWords = "SELECT word, trans FROM " + table +";";
        return getReadableDatabase().rawQuery(getWords,null);
    }

    public void updateWord(String table,int id,int ses,long time){
        table=sqlEscapeString(table);
        String upd = "UPDATE "+table+" SET session="+ses+",datetime="+time+" WHERE _id="+id+";";
        getWritableDatabase().execSQL(upd);
    }

    private boolean nullOrEmpty(String str){
        return str==null || str.equals("");
    }
}
