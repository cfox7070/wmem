package com.cfox.wmem;

/**
 * Created by mrr on 11/8/16.
 */
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLog;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.*;
import static org.robolectric.shadows.ShadowLog.setupLogging;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class DbTest1 {
    QuizData dbHelper;

    @Before
    public void setup() {
        QuizData.context = RuntimeEnvironment.application.getApplicationContext();
        dbHelper = QuizData.getQuizData();

    }

    @Test
    public void testAddTable() {
        dbHelper.addTable("first");
        dbHelper.addTable("second");
        dbHelper.addTable("second");
        dbHelper.addTable("third");
        Cursor cursor=dbHelper.getWordTables();
        assertThat(cursor.getCount(), is(3));
        assertThat(cursor.getColumnCount(), is(1));
        cursor.close();
        dbHelper.delTable("second");
        cursor=dbHelper.getWordTables();
        assertThat(cursor.getCount(), is(2));
        cursor.close();
        dbHelper.delTable("second");
        cursor=dbHelper.getWordTables();
        assertThat(cursor.getCount(), is(2));
        cursor.close();
        dbHelper.addQuizSettings("first",3,2,1,"8, 48, 168, 236");

        assertThat(dbHelper.getQuizSettings("first"),is(notNullValue()));

        assertThat(dbHelper.addWord("first","w1","t1"),greaterThan(-1));
        assertThat(dbHelper.addWord("first","w2","t2"),greaterThan(-1));
        assertThat(dbHelper.addWord("first","w3","t3"),greaterThan(-1));
        assertThat(dbHelper.addWord("first","w1","t1"),equalTo(3));
        assertThat(dbHelper.addWord(null,"w1","t1"),equalTo(-1));
        assertThat(dbHelper.addWord("first",null,"t1"),equalTo(3));
        assertThat(dbHelper.addWord("first","w1",null),equalTo(3));
        assertThat(dbHelper.addWord("first","w4","t4"),greaterThan(-1));
        assertThat(dbHelper.addWord("first","w5","t5"),greaterThan(-1));
        assertThat(dbHelper.addWord("first","w6","t6"),greaterThan(-1));
    }

    @Test
    public void testAddTable1() {
    }

}
