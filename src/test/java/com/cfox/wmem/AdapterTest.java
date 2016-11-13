package com.cfox.wmem;

/**
 * Created by mrr on 11/6/16.
 */
import android.content.Context;
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
import static org.junit.Assert.*;
import static org.robolectric.shadows.ShadowLog.setupLogging;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class AdapterTest {

    QuizData dbHelper;
    Context context;

    @Before
    public void setup() {
        context=RuntimeEnvironment.application.getApplicationContext();
        QuizData.context=context;
        dbHelper = QuizData.getQuizData();
        dbHelper.addTable("first");
        dbHelper.addQuizSettings("first",3,2,1,"8, 48, 168, 236");
        dbHelper.addWord("first","w1","t1");
        dbHelper.addWord("first","w2","t2");
        dbHelper.addWord("first","w3","t3");
        dbHelper.addWord("first","w4","t4");
        dbHelper.addWord("first","w5","t5");
        dbHelper.addWord("first","w6","t6");
    }

    @Test
    public void testAdapter() {
       RevLWAdapter adapter=new RevLWAdapter(context,"first");
        assertThat(adapter,is(notNullValue()));
    }

    @After
    public void tearDown() {

    }
}
