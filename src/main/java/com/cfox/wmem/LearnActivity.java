package com.cfox.wmem;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import static com.cfox.wmem.QuizData.getQuizData;


public class LearnActivity extends AppCompatActivity {
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
        QuizData.initQuizdata(this);
        quizname=getIntent().getStringExtra(AddWordsActivity.KEY_QUIZNAME);
        caption=((TextView)findViewById(R.id.quizname));
        mAdapter=new RevLWAdapter(this,quizname);
        ListView lv = (ListView) findViewById(R.id.list);
        lv.setAdapter(mAdapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                question.setText(mAdapter.testPos(position));
                setCaption();
            }
        });
        question=(TextView)findViewById(R.id.question);
        question.setText(mAdapter.getWord());
        setCaption();
    }


    private void setCaption(){
        QuizData qd=QuizData.getQuizData(this);
        String strCap=quizname+" - "+getString(R.string.total)+":"+qd.getWordCount(quizname)+" - ";
        int nSes=mAdapter.getNSes();
        int i=0;
        for(;i<=nSes;i++){
            strCap+=qd.getWordCount(quizname,i)+" : ";
       }
        strCap+=getString(R.string.passed)+": "+qd.getWordCount(quizname,i);
        caption.setText(strCap);
    }
    @Override
    protected void onStop() {
        super.onStop();
        QuizData.closeQuizData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        QuizData.initQuizdata(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        QuizData.closeQuizData();
    }

}


