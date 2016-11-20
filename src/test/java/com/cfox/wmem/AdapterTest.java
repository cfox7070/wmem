package com.cfox.wmem;

/**
 * Created by mrr on 11/6/16.
 */
import android.content.Context;
import android.database.Cursor;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.*;

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
        dbHelper.addQuizSettings("first",3,1,1,"8, 48, 168    236");
        dbHelper.addWord("first","w1","t1");
        dbHelper.addWord("first","w2","t2");
        dbHelper.addWord("first","w3","t3");
        dbHelper.addWord("first","w4","t4");
        dbHelper.addWord("first","w5","t5");
        dbHelper.addWord("first","w6","t6");
        dbHelper.addWord("first","w7","t7");
        dbHelper.addWord("first","w8","t8");
    }

    @Test
    public void testAdapter() {
        assertThat(dbHelper.getWordCount("first"),is(8));
       RevLWAdapter adapter=new RevLWAdapter(context,"first");
        assertThat(adapter,is(notNullValue()));
        String w="",w1="";int ind=0;
        for(int i=0;i<9;i++) {
            w=adapter.getWord();
            System.out.println(w);
            ind = adapter.getCurInd();
            assertThat(w1=adapter.testPos(ind),is(not(equalTo(w))));
            w1=w;
        }
        Cursor cur=dbHelper.getReadableDatabase().rawQuery("Select * from first;",null);
        for(cur.moveToFirst();!cur.isAfterLast();cur.moveToNext()){

            System.out.println(cur.getInt(0)+" "+cur.getString(1)+" "+cur.getString(2)+" "+cur.getInt(3)+" "+cur.getLong(4));
        }
        System.out.println();
        cur.close();
        w=adapter.getWord();
        ind = adapter.getCurInd();
        cur=dbHelper.getReadableDatabase().rawQuery("Select * from first;",null);
        for(cur.moveToFirst();!cur.isAfterLast();cur.moveToNext()){

            System.out.println(cur.getInt(0)+" "+cur.getString(1)+" "+cur.getString(2)+" "+cur.getInt(3)+" "+cur.getLong(4));
        }
        System.out.println();
        cur.close();
        //    assertThat(w1=adapter.testPos(ind+1),is(equalTo(w or )));
        for(int i=0;i<19;i++) {
            w=adapter.getWord();
            ind = adapter.getCurInd();
            assertThat(w1=adapter.testPos(ind),is(not(equalTo(w))));
            w1=w;
        }
        cur=dbHelper.getReadableDatabase().rawQuery("Select * from first;",null);
        for(cur.moveToFirst();!cur.isAfterLast();cur.moveToNext()){

            System.out.println(cur.getInt(0)+" "+cur.getString(1)+" "+cur.getString(2)+" "+cur.getInt(3)+" "+cur.getLong(4));
        }
        cur.close();
    }

    @After
    public void tearDown() {

    }
}
