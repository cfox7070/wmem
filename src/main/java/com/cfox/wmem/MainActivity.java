package com.cfox.wmem;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.cfox.fdlg.FileChooser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import static com.cfox.wmem.QuizData.getQuizData;
import static com.cfox.wmem.QuizData.unescapeString;

public class MainActivity extends AppCompatActivity {

    //todo: text to speach, fonts, colors, translation

    //Todo: localisation

//TODO:onPause,onResume, ondestroy in other activities

    private ArrayAdapter<String> tablesadapter;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //  Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        //Toolbar will now take on default actionbar characteristics
        setSupportActionBar (toolbar);

        QuizData.initQuizdata(this);

      //  supportActionBar.Title = "Hello from Appcompat Toolbar";
        tablesadapter=new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_single_choice,
                new ArrayList<String>());

        updateTables();

        listView = (ListView) findViewById(R.id.list);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setAdapter(tablesadapter);
        listView.setOnItemClickListener(new ListView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> p1, View p2, int p3, long p4)
            {
                MainActivity.this.invalidateOptionsMenu();
            }
        });

/*
        findViewById(R.id.buttonNew).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newQuiz();
            }
        });
        findViewById(R.id.buttonAdd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addWords();
            }
        });
        findViewById(R.id.buttonQuiz).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos=listView.getCheckedItemPosition();
                if(pos>-1) {
                    final String qzname = tablesadapter.getItem(pos);
                    if(qzname!=null && !qzname.equals("")) {
                        Intent i = new Intent(MainActivity.this, LearnActivity.class);
                        i.putExtra(AddWordsActivity.KEY_QUIZNAME, qzname);
                        startActivity(i);
                    }
                }

            }
        });

        findViewById(R.id.buttonDel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos=listView.getCheckedItemPosition();
                if(pos>-1) {
                    final String qzname = tablesadapter.getItem(pos);
                    if(qzname!=null && !qzname.equals("")) {
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("wmem")
                                .setMessage("delete "+qzname+"?")
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        getQuizData().delTable(qzname);
                                        updateTables();
                                    }
                                })
                                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                    }
                                })
                                .show();
                    }
                }

            }
        });*/

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onRestoreInstanceState(savedInstanceState, persistentState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    public boolean onPrepareOptionsMenu(Menu menu){
        if(listView.getCheckedItemPosition()==ListView.INVALID_POSITION){
            menu.setGroupEnabled(R.id.group_q,false);
        }else{
            menu.setGroupEnabled(R.id.group_q,true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch(id){
            case R.id.action_new:
                newQuiz();
                return true;
//            case R.id.action_psettings:
//                return true;
        }
        int pos=listView.getCheckedItemPosition();
        String quizname=null;
        if(pos>-1) {
            quizname = tablesadapter.getItem(pos);
            if(quizname==null || quizname.equals("")) {
                return true;
            }
        }
        switch(id){
            case R.id.action_add:
                addWords(quizname);
                return true;
            case R.id.action_start:
                learnWords(quizname);
                return true;
            case R.id.action_exportw:
                exportWords(quizname);
                return true;
            case R.id.action_importw:
                importWords(quizname);
                return true;
            case R.id.action_del:
                delTable(quizname);
                return true;
            case R.id.action_qsettings:
                editQSettings(quizname);
                return true;
        }
        return true;
    }

    private void exportDB(){
        new FileChooser(this)
                .setFileListener(new FileChooser.FileSelectedListener() {
                    @Override
                    public void fileSelected(File file) {
                        FileOutputStream os=null;
                        FileInputStream is=null;
                        try {
                            if(file==null) return;
                            os=new FileOutputStream(file);
                            String dbPath=MainActivity.this.getDatabasePath("quizes.db").getPath();
                            is=new FileInputStream(dbPath);
                            byte[] buf = new byte[1024];
                            int len;
                            while ((len = is.read(buf)) > 0) {
                                os.write(buf, 0, len);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                if (is != null)
                                    is.close();
                                if (os != null)
                                    os.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                    }
                })
                .setMode(FileChooser.Mode.create)
                .showDialog();
    }

    private void exportWords(final String quizname){
        new FileChooser(this)
                .setFileListener(new FileChooser.FileSelectedListener() {
                    @Override
                    public void fileSelected(File file) {
                        BufferedWriter bw=null;
                         try {
                            if(file==null) return;
                             bw=new BufferedWriter(new FileWriter(file));
                             Cursor cur=getQuizData().getWords(quizname);
                             String sout;int count=0;
                             for(cur.moveToFirst();!cur.isAfterLast();cur.moveToNext()){
                                 sout=unescapeString(cur.getString(0))+" / "+unescapeString(cur.getString(1))+"\n";
                                 bw.write(sout);
                                 count++;
                             }
                             Toast toast=Toast.makeText(MainActivity.this,count+" words exported",Toast.LENGTH_SHORT);
                             toast.show();

                         }  catch (IOException e) {
                             e.printStackTrace();
                         } finally {
                             try {
                                 if (bw != null)
                                     bw.close();
                             } catch (IOException e) {
                                 e.printStackTrace();
                             }
                         }
                    }
                })
                .setMode(FileChooser.Mode.create)
                .showDialog();
    }

    private void importWords(final String quizname){
        new FileChooser(this)
                .setFileListener(new FileChooser.FileSelectedListener() {
                    @Override
                    public void fileSelected(File file) {
                        BufferedReader br=null;
                        try {
                            if(file==null) return;
                            br=new BufferedReader(new FileReader(file));
                            int count=0;
                            for(String line=br.readLine();line!=null;line=br.readLine()){
                                line=line.trim();
                                if(line.isEmpty())
                                    continue;
                                String[] wt=line.split("/");
                                if(wt.length!=2)
                                    throw new RuntimeException("bad file");
                                wt[0]=wt[0].trim();
                                wt[1]=wt[1].trim();
                                getQuizData().addWord(quizname,wt[0],wt[1]);
                                count++;
                                 }
                            Toast toast=Toast.makeText(MainActivity.this,count+" words added",Toast.LENGTH_SHORT);
                            toast.show();
                        }  catch (IOException e) {
                            e.printStackTrace();
                        } catch(RuntimeException e){
                            if(e.getMessage().equals("bad file")){
                                Toast toast=Toast.makeText(MainActivity.this,e.getMessage(),Toast.LENGTH_SHORT);
                                toast.show();
                            }else{
                                throw e;
                            }
                        }finally {
                            try {
                                if (br != null)
                                    br.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                })
                .setMode(FileChooser.Mode.open)
                .showDialog();
    }

    private void addWords(final String quizname){
                Intent i = new Intent(MainActivity.this, AddWordsActivity.class);
                i.putExtra(AddWordsActivity.KEY_QUIZNAME, quizname);
                startActivity(i);
    }

    private void learnWords(final String quizname){
        Intent i = new Intent(MainActivity.this, LearnActivity.class);
        i.putExtra(AddWordsActivity.KEY_QUIZNAME, quizname);
        startActivity(i);
    }

    private void delTable(final String quizname){
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("wmem")
                .setMessage("delete "+quizname+"?")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        getQuizData().delTable(quizname);
                        getQuizData().delQuizSettings(quizname);
                        updateTables();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                })
                .show();
    }

    private void editQSettings(String quizname){
        final Dialog dialog = new Dialog(this,R.style.DialogTheme);
        dialog.setContentView(R.layout.quiz_settings_view);
        dialog.findViewById(R.id.scancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.findViewById(R.id.sok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                processUpdate(dialog,false);
            }
        });
        EditText txname=(EditText) dialog.findViewById(R.id.s_qname);
        txname.setText(quizname);
        txname.setEnabled(false);
        Cursor cur=getQuizData().getQuizSettings(quizname);
        if(cur.moveToFirst()){
            ((EditText) dialog.findViewById(R.id.s_numvars)).setText(cur.getString(0));
            ((EditText) dialog.findViewById(R.id.s_dirtrans)).setText(cur.getString(1));
            ((EditText) dialog.findViewById(R.id.s_revtrans)).setText(cur.getString(2));
            ((EditText)dialog.findViewById(R.id.s_intses)).setText(cur.getString(3));
        }
        cur.close();
        dialog.show();
    }

    private void newQuiz(){
        final Dialog dialog = new Dialog(this,R.style.DialogTheme);
        dialog.setContentView(R.layout.quiz_settings_view);
        dialog.findViewById(R.id.scancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.findViewById(R.id.sok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                processUpdate(dialog,true);
            }
        });
        dialog.show();
    }

    private void processUpdate(Dialog dialog,boolean create){
        String qname=((EditText)dialog.findViewById(R.id.s_qname)).getText().toString();
        if(create) {
            if (getQuizData().tableExists(qname)) {
                Toast.makeText(MainActivity.this, "table exists", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        int numVar,nDir,nRev;String vars;
        numVar = Integer.parseInt(((EditText) dialog.findViewById(R.id.s_numvars)).getText().toString());
        nDir = Integer.parseInt(((EditText) dialog.findViewById(R.id.s_dirtrans)).getText().toString());
        nRev = Integer.parseInt(((EditText) dialog.findViewById(R.id.s_revtrans)).getText().toString());
        nDir=nDir<0?0:nDir;
        nRev=nRev<0?0:nRev;
        vars=((EditText)dialog.findViewById(R.id.s_intses)).getText().toString();
        if(checkVars(numVar,nDir,nRev,vars)) {
            if(create) {
                getQuizData().addTable(qname);
                getQuizData().addQuizSettings(qname, numVar, nDir, nRev, vars);
            }else{
                getQuizData().updateQuizSettings(qname, numVar, nDir, nRev, vars);
            }
            updateTables();
            dialog.dismiss();
        }
    }

    private boolean checkVars(int numVar,int nDir, int nRev, String vars){
           if(numVar<2){
                Toast.makeText(MainActivity.this,"Must be more variants",Toast.LENGTH_SHORT).show();
                return false;
            }
            nDir=nDir<0?0:nDir;
            nRev=nRev<0?0:nRev;
            if(nDir==0 && nRev==0){
                Toast.makeText(MainActivity.this,"Must be more repeats",Toast.LENGTH_SHORT).show();
                return false;
            }
        try{
           String[]ss=vars.split(" ");
            for(int i=0;i<ss.length;i++){
                ss[i]=ss[i].trim();
                if(ss[i].isEmpty())
                    continue;
                int ic=ss[i].indexOf(",");
                if(ic>-1)
                    ss[i]=ss[i].substring(0,ic);
                if(ss[i].isEmpty())
                    continue;

                @SuppressWarnings("unused") double d = Float.parseFloat(ss[i]);
            }
        }catch(NumberFormatException e){
            Toast.makeText(MainActivity.this,"Wrong format of intervals",Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void updateTables(){
        Cursor cur=getQuizData().getWordTables();
        tablesadapter.clear();
        for(cur.moveToFirst();!cur.isAfterLast();cur.moveToNext()){
            tablesadapter.add(unescapeString(cur.getString(0)));
        }
        tablesadapter.notifyDataSetChanged();
    }

 }
