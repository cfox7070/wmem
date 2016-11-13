package com.cfox.wmem;

import android.app.ListActivity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Random;

public class LearnActivity extends ListActivity {
//    private static final long millis_in_hour = 3600000;
//    private static final long millis_in_day = millis_in_hour * 24;
//    private static final long millis_in_week = millis_in_day * 7;

    private String quizname;

//    private long[] intervals = {0,millis_in_hour * 8, millis_in_day * 2, millis_in_week, millis_in_week * 2};

    //
//    private LearnedWord[] workArray = {new LearnedWord(0, "voy", "я иду", 0),
//            new LearnedWord(1, "vas", "ты идешь", 0),
//            new LearnedWord(2, "va", "он идет", 0),
//            new LearnedWord(3, "vamos", "мы идем", 0),
//            new LearnedWord(4, "vais", "вы идете", 0),
//            new LearnedWord(5, "van", "они идут", 0),
//    };
    private RevLWAdapter mAdapter;
    private TextView question;
    private TextView caption;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learn);
        quizname=getIntent().getStringExtra(AddWordsActivity.KEY_QUIZNAME);
        caption=((TextView)findViewById(R.id.quizname));
        mAdapter=new RevLWAdapter(this,quizname);
        ListView lv=getListView();
        lv.setAdapter(mAdapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                question.setText(mAdapter.testPos(position));
                setCaption();
            }
        });
        mAdapter.updateList();
        question=(TextView)findViewById(R.id.question);
        question.setText(mAdapter.getWord());
        setCaption();
    }


    private void setCaption(){
        String strCap=quizname+" - total: ";
        QuizData qd=QuizData.getQuizData();
        strCap+=qd.getWordCount(quizname)+"-";
        int nSes=mAdapter.getNSes();
        int i=1;
        for(;i<=nSes-1;i++){
            strCap+=i+": "+qd.getWordCount(quizname,i)+"-";
       }
        strCap+="passed: "+qd.getWordCount(quizname,i);
        caption.setText(strCap);
    }

}


