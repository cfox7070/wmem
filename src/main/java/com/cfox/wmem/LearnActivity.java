package com.cfox.wmem;

import android.app.ListActivity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class LearnActivity extends ListActivity {
    private static final long millis_in_hour = 3600000;
    private static final long millis_in_day = millis_in_hour * 24;
    private static final long millis_in_week = millis_in_day * 7;

    private long curdate;
    private String quizname;

    private long[] intervals = {0,millis_in_hour * 8, millis_in_day * 2, millis_in_week, millis_in_week * 2};
    private int numVariants = 5;
    private int numRepeats=2;
    private LearnedWord[] workArray=new LearnedWord[numVariants*2];
    private LearnedWord[] tempArray=new LearnedWord[numVariants*2];
//
//    private LearnedWord[] workArray = {new LearnedWord(0, "voy", "я иду", 0),
//            new LearnedWord(1, "vas", "ты идешь", 0),
//            new LearnedWord(2, "va", "он идет", 0),
//            new LearnedWord(3, "vamos", "мы идем", 0),
//            new LearnedWord(4, "vais", "вы идете", 0),
//            new LearnedWord(5, "van", "они идут", 0),
//    };
    private RevLWAdapter mAdapter;
    private final Random rnd = new Random(System.currentTimeMillis());
    private TextView question;
    private LearnedWord curLw;
    private TextView caption;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learn);

        quizname=getIntent().getStringExtra(AddWordsActivity.KEY_QUIZNAME);


        caption=((TextView)findViewById(R.id.quizname));
        curdate=System.currentTimeMillis();
        updateWorkArray();

        mAdapter=new RevLWAdapter(this,new LearnedWord[numVariants]);
        mAdapter.updateList(workArray);
        ListView lv=getListView();
        lv.setAdapter(mAdapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int cpos = mAdapter.getCurInd();
                if (position == cpos) {
                    if (mAdapter.getRev()) {
                      //  curLw.pRev++;
                     //   if (curLw.pRev >= numRepeats) {
                            curLw.ses++;
                            curdate = System.currentTimeMillis();
                            String upd = "UPDATE " + quizname + " SET session = " + curLw.ses + ", datetime = " +
                                    curdate + " WHERE _id = " + curLw.id + ";";
                            SQLiteDatabase db = QuizData.getQuizData().getWritableDatabase();
                            db.execSQL(upd);
                            updateWorkArray();
                            //update db
                            //requery init workArray
                   //     }
                    } else {
                        curLw.pStr++;
                    }
                    shuffle(workArray);
                    LearnedWord lw;
                    mAdapter.updateList(workArray);
                    do {
                        lw = mAdapter.getLW(rnd.nextInt(numVariants));
                    }while(lw.id==curLw.id);
                    curLw = lw;
                    if (curLw.pStr < numRepeats) {
                        mAdapter.setRev(false);
                        question.setText(curLw.word);
                    } else {
                        mAdapter.setRev(true);
                        question.setText(curLw.trans);
                    }
                    mAdapter.notifyDataSetChanged();
                } else {
                    curLw.pStr = 0;
                    curLw.pRev = 0;
                }

            }
        });
        question=(TextView)findViewById(R.id.question);
        curLw=mAdapter.getLW(rnd.nextInt(numVariants));
        if(curLw!=null)
            question.setText(curLw.word);
    }

    private void updateWorkArray() {
        //TODO: remaster, use query for single word
        //TODO: make debug list of words and sessions
        SQLiteDatabase db = QuizData.getQuizData().getWritableDatabase();
        int nSes=intervals.length-1;
        int arrPos=0;
        ////
        ////
        ////
        String getWords = "SELECT _id, word, trans, session FROM " + quizname + " WHERE (session = ? AND datetime < ?);";
        loops:
        for(;nSes>-1;nSes--) {
            Cursor  cursor = db.rawQuery(getWords, new String[]{String.valueOf(nSes), String.valueOf(curdate - intervals[nSes])});
            while (cursor.moveToNext()) {
                LearnedWord lw =new LearnedWord(cursor.getInt(0),cursor.getString(1),
                                                    cursor.getString(2),cursor.getInt(3));
                tempArray[arrPos]=lw;
                for(int i=0;i<workArray.length;i++){
                    if(workArray[i]!=null && workArray[i].id==tempArray[arrPos].id) {
                        tempArray[arrPos].pStr=workArray[i].pStr;
                        tempArray[arrPos].pRev=workArray[i].pRev;
                        workArray[i]=null;
                    }
                }
                arrPos++;
                if(arrPos==tempArray.length){
                    cursor.close();
                    break loops;
                 }
           }
            cursor.close();
        }
        LearnedWord[] tarr=workArray;
        workArray=tempArray;
        tempArray=tarr;
        setCaption();
    }

    public void shuffle(LearnedWord[] ar){

        for (int i = ar.length - 1; i > 0; i--)
        {
            int index = rnd.nextInt(i + 1);
            // Simple swap
            LearnedWord a = ar[index];
            ar[index] = ar[i];
            ar[i] = a;
        }
    }

    private void setCaption(){
        String strCap=quizname+": ";
        SQLiteDatabase db = QuizData.getQuizData().getReadableDatabase();
        String cnt="SELECT COUNT(*) FROM "+quizname+";";
        int c=0;
        Cursor cur=db.rawQuery(cnt, null);
        boolean r=cur.moveToFirst();
        if(r)
            c=cur.getInt(0);
        strCap+=c+"-";
        cur.close();
        int nSes=intervals.length-1;
        cnt="SELECT COUNT(*) FROM "+quizname+" WHERE (session = ?);";
        for(int i=0;i<nSes;i++){
            if(i!=0)strCap+=":";
            c=0;
            cur=db.rawQuery(cnt,new String[]{String.valueOf(i)});
            r=cur.moveToFirst();
            if(r)
                c=cur.getInt(0);
            strCap+=c;
            cur.close();
        }
        caption.setText(strCap);
    }
}
/*

 */

class LearnedWord {
    int id;
    String word;
    String trans;
    int pStr=0;
    int pRev=0;
    int ses;

    public LearnedWord(int id, String word, String trans, int ses) {
        this.id = id;
        this.word = word;
        this.trans = trans;
        this.ses = ses;
    }

    public LearnedWord copy(){
        LearnedWord lv=new LearnedWord(id,word,trans,ses);
        lv.pStr=this.pStr;
        lv.pRev=pRev;
        return lv;
    }

}

class RevLWAdapter extends ArrayAdapter<LearnedWord>{

    private final Context context;
    private final LearnedWord[] values;
    private boolean rev=false;
    private int curInd=0;


    public RevLWAdapter(Context context, LearnedWord[] objects) {
        super(context, -1, objects);
        this.context = context;
        this.values = objects;
    }

    public void updateList(LearnedWord[] src){
         for(int i=0;i<values.length;i++){
            values[i]=src[i];
        }
    }
    public LearnedWord getLW(int ind){
        curInd=ind;
        return values[curInd];
    }
    public int getCurInd(){
        return curInd;
    }
    public void setRev(boolean rev){
        this.rev=rev;
    }
    public boolean getRev(){
        return rev;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.quiz_text, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.answer);
        if(values[position]!=null) {
            if (rev)
                textView.setText(values[position].word);
            else
                textView.setText(values[position].trans);
        } else{
            textView.setText("");
        }
        return rowView;
    }
}

