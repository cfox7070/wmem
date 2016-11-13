package com.cfox.wmem;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.Random;

/**
 * Created by mrr on 11/12/16.
 */



public class RevLWAdapter extends ArrayAdapter<String> {

    private static final long millis_in_hour = 3600000;

    private final Random rnd = new Random(System.currentTimeMillis());

    private final Context context;
    private final String qname;

    private final int numVariants ;
    private final int numRepeatsD;
    private final int numRepeatsR;
    private final int numSes;
    private final long[] ses;
    private  final LearnedWord[] values;
    private final String conditions;
    private boolean rev=false;

    private int curInd=0;


    public RevLWAdapter(Context context, String qname) {
        super(context, -1, (String[]) null);//// TODO: 11/13/16 string dummy ???
        this.context = context;
        this.qname = qname;
        Cursor cur=QuizData.getQuizData().getQuizSettings(qname);
        cur.moveToFirst();
        numVariants=cur.getInt(0);
        numRepeatsD=cur.getInt(1);
        numRepeatsR=cur.getInt(2);
        String s=cur.getString(3);
        cur.close();
        String[] ss=s.split(" ");
        numSes=ss.length;
        ses=new long[numSes];
        setSessions(ss);
        values=new LearnedWord[numVariants];
        conditions=setConditions();
        this.addAll(new String[numVariants]);
     }

    private void setSessions(String[] ss) {
        for(int i=0;i<ss.length;i++){
            double d=Float.parseFloat(ss[i]);
            d*=millis_in_hour;
            long l=Math.round(d);
            ses[i]=l;
        }
    }

    private String setConditions(){
        String s="";
        long curdate=System.currentTimeMillis();
        long dt;
        int i=0;
        s+="(session = "+i+" AND datetime < "+curdate+")";
        for(i=1;i<numSes+1;i++){
            dt=curdate-ses[i-1];
            s+=" OR (session = "+i+" AND datetime < "+dt+")";
        }
        return s;
    }

    public void updateList(){
         Cursor cur=QuizData.getQuizData().getWords(qname,conditions,numVariants);
         cur.moveToFirst();
         for(int i=0;i<values.length;i++){
            values[i].id=cur.getInt(0);
             values[i].word=cur.getString(1);
             values[i].trans=cur.getString(2);
             values[i].ses=cur.getInt(3);
             values[i].pDir=0;
             values[i].pRev=0;
             values[i].rev=false;
             values[i].faled=false;
             cur.moveToNext();
         }
        cur.close();
    }

    private void shuffle(){

        for (int i = values.length - 1; i > 0; i--)
        {
            int index = rnd.nextInt(i + 1);
            // Simple swap
            LearnedWord a = values[index];
            values[index] = values[i];
            values[i] = a;
        }
    }

    public String testPos(int pos) {
        LearnedWord lv=values[curInd];
        if(pos!=curInd) {
             lv.pDir=0;
            lv.pRev=0;
            lv.rev=false;
            lv.faled=true;
            return getWord(curInd);
        }else {
            if(!rev) {
               lv.pDir++;
                if(lv.pDir>=numRepeatsD)
                    lv.rev=true;
            }else {
                lv.pRev++;
                if(lv.pRev>=numRepeatsR) {
                    if(lv.faled)
                      QuizData.getQuizData().updateWord(qname,lv.word,0,0);
                    else
                        QuizData.getQuizData().updateWord(qname,lv.word,lv.ses+1,System.currentTimeMillis()+ses[lv.ses]);
                        updateList();
                }
            }
            shuffle();
            return getWord();
        }
    }

    public String getWord() {
        return getWord(rnd.nextInt(numVariants));
    }

    private String getWord(int ind) {
        curInd=ind;
        LearnedWord lv=values[curInd];
        rev=lv.rev;
        if (rev)
            return unescapeString(lv.trans);
        else
            return unescapeString(lv.word);
    }

    public int getNSes() {
        return numSes;
    }

    static class ViewHolder {
        public TextView qtext;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView=convertView;
        ViewHolder viewHolder=null;
        if(rowView==null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.quiz_text, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.qtext = (TextView) rowView.findViewById(R.id.answer);
             rowView.setTag(viewHolder);
        }
        viewHolder=(ViewHolder) rowView.getTag();
        if(values[position]!=null) {
            if (rev)
                viewHolder.qtext.setText(values[position].word);
            else
                viewHolder.qtext.setText(values[position].trans);
        } else{
            viewHolder.qtext.setText("");
        }
        return rowView;
    }

//    private void updateWorkArray() {
//       SQLiteDatabase db = QuizData.getQuizData().getWritableDatabase();
//        int nSes=intervals.length-1;
//        int arrPos=0;
//        ////
//        ////
//        ////
//        String getWords = "SELECT _id, word, trans, session FROM " + quizname + " WHERE (session = ? AND datetime < ?);";
//        loops:
//        for(;nSes>-1;nSes--) {
//            Cursor cursor = db.rawQuery(getWords, new String[]{String.valueOf(nSes), String.valueOf(curdate - intervals[nSes])});
//            while (cursor.moveToNext()) {
//                LearnedWord lw =new LearnedWord(cursor.getInt(0),unescapeString(cursor.getString(1)),
//                        unescapeString(cursor.getString(2)),cursor.getInt(3));
//                tempArray[arrPos]=lw;
//                for(int i=0;i<workArray.length;i++){
//                    if(workArray[i]!=null && workArray[i].id==tempArray[arrPos].id) {
//                        tempArray[arrPos].pStr=workArray[i].pStr;
//                        tempArray[arrPos].pRev=workArray[i].pRev;
//                        workArray[i]=null;
//                    }
//                }
//                arrPos++;
//                if(arrPos==tempArray.length){
//                    cursor.close();
//                    break loops;
//                }
//            }
//            cursor.close();
//        }
//        LearnedWord[] tarr=workArray;
//        workArray=tempArray;
//        tempArray=tarr;
//        setCaption();
//    }

    private String unescapeString(String arg){
        arg=arg.replace("''","'");
        if(arg.charAt(0)=='\'') arg=arg.substring(1);
        if(arg.charAt(arg.length()-1)=='\'') arg=arg.substring(0,arg.length()-1);
        return arg;
    }

    private static class LearnedWord {
        int id;
        String word;
        String trans;
        int pDir=0;
        int pRev=0;
        int ses=0;
        boolean rev=false;
        boolean faled=false;
        }
}
