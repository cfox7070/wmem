package com.cfox.wmem;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.Locale;
import java.util.Set;

import static com.cfox.wmem.QuizData.getQuizData;
import static java.lang.Thread.sleep;


public class LearnActivity extends AppCompatActivity {
//    private static final long millis_in_hour = 3600000;
//    private static final long millis_in_day = millis_in_hour * 24;
//    private static final long millis_in_week = millis_in_day * 7;
    private final String KEY_SPEAK="lspeak";

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
    class TtsInitListener implements TextToSpeech.OnInitListener{
        @Override
        public void onInit(int i) {
            Locale lang=mAdapter.getLang();
            String tx=mAdapter.getWord();
            if(TextToSpeech.LANG_AVAILABLE==tts.isLanguageAvailable(lang)){
                speak.setEnabled(true);
                speak.setChecked(getPreferences(MODE_PRIVATE).getBoolean(KEY_SPEAK,true));
                tts.setLanguage(lang);
                question.setText(tx);
                //noinspection deprecation
                tts.speak(tx, TextToSpeech.QUEUE_FLUSH, null);
            }else{
                speak.setEnabled(false);
                speak.setChecked(false);
                question.setText(tx);
            }
        }
    }

    private RevLWAdapter mAdapter;
    private TextView question;
    private TextView caption;
    private ToggleButton speak;
    private TextToSpeech tts;
    private TtsInitListener ttsInitListener;

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
                String text=mAdapter.testPos(position);
                question.setText(text);
                if(speak.isChecked()){
                    if(mAdapter.maySpeak()) {
                        //noinspection deprecation
                        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
                    }
                }
                setCaption();
            }
        });
        question=(TextView)findViewById(R.id.question);

        question.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(speak.isChecked()){
                    if(mAdapter.maySpeak()) {
                        String text=question.getText().toString();
                        //noinspection deprecation
                        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
                    }
                }
            }
        });
        speak=(ToggleButton) findViewById(R.id.speak);
        speak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean b=speak.isChecked();
                getPreferences(MODE_PRIVATE).edit().putBoolean(KEY_SPEAK,b).commit();//todo: kill tts???
            }
        });
        speak.setEnabled(true);
        speak.setChecked(getPreferences(MODE_PRIVATE).getBoolean(KEY_SPEAK,true));
        if(speak.isChecked()) {
            ttsInitListener = new TtsInitListener();
            tts = new TextToSpeech(getApplicationContext(), ttsInitListener);
        }else{
            question.setText(mAdapter.getWord());
        }
        setCaption();
    }

    private void prepareTTS(){
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
    protected void onPause() {
        super.onPause();
        tts.shutdown();
        tts=null;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(tts!=null) tts.shutdown();
        tts=null;
        QuizData.closeQuizData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(tts==null){
            tts=new TextToSpeech(getApplicationContext(),ttsInitListener);
        };
        QuizData.initQuizdata(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        QuizData.closeQuizData();
    }

}


