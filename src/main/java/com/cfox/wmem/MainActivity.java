package com.cfox.wmem;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.cfox.fdlg.FileChooser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.prefs.Preferences;

import static com.cfox.wmem.QuizData.getQuizData;
import static com.cfox.wmem.QuizData.unescapeString;

public class MainActivity extends AppCompatActivity {

    //todo: text to speach, fonts, colors, spanish translation
    //todo: multythreading import,export, thead safe database helper
    //todo: adapter inherit from base adapter

    private ArrayAdapter<String> tablesadapter;
    private ListView listView;
    private int tclick=0;

//    class PopupListener implem
    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //  Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tclick++;
                if(tclick<5)
                    return;
                tclick=0;
                PopupMenu popupMenu = new PopupMenu(MainActivity.this, view);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.item_exportDB:
                                exportDB();
                                return true;
                            case R.id.item_importDB:
                                importDB();
                                return true;
                        }
                        return false;
                    }
                });
                popupMenu.inflate(R.menu.menu_export);
                popupMenu.show();

                //          Toast.makeText(MainActivity.this,"3 times",Toast.LENGTH_SHORT).show();
            }
        });


        //Toolbar will now take on default actionbar characteristics
        setSupportActionBar (toolbar);

        QuizData.initQuizdata(this);

      //  supportActionBar.Title = "Hello from Appcompat Toolbar";
        tablesadapter=new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_single_choice,
                new ArrayList<String>());

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

        loadDicExamples();

        updateTables();
    }

    private void loadDicExamples() {
        if(!getPreferences(MODE_PRIVATE).getBoolean("engru400",false)){
            try {
                InputStream is=getAssets().open("eng-w400.txt");
                getQuizData(this).addTable("eng-ru-400");
                final String intrvls=(8)+" "+(24 * 2)+" "+(24*7)+" "+(24*7*2);
                getQuizData(this).addQuizSettings("eng-ru400", 5, 2, 1, intrvls,"en");
                getQuizData(this).importWords(is,"eng-ru400");
            } catch (IOException e) {
                e.printStackTrace();
            }
            getPreferences(MODE_PRIVATE).edit().putBoolean("engru400",true).commit();
        }
        if(!getPreferences(MODE_PRIVATE).getBoolean("engesp255",false)){
            try {
                InputStream is=getAssets().open("eng-esp.txt");
                getQuizData(this).addTable("en-es-255");
                final String intrvls=(8)+" "+(24 * 2)+" "+(24*7)+" "+(24*7*2);
                getQuizData(this).addQuizSettings("en-es-255", 5, 2, 1, intrvls,"en");
                getQuizData(this).importWords(is,"en-es-255");
            } catch (IOException e) {
                e.printStackTrace();
            }
            getPreferences(MODE_PRIVATE).edit().putBoolean("engesp255",true).commit();
        }
        if(!getPreferences(MODE_PRIVATE).getBoolean("espeng255",false)){
            try {
                InputStream is=getAssets().open("esp-eng.txt");
                getQuizData(this).addTable("es-en-255");
                final String intrvls=(8)+" "+(24 * 2)+" "+(24*7)+" "+(24*7*2);
                getQuizData(this).addQuizSettings("es-en-255", 5, 2, 1, intrvls,"es");
                getQuizData(this).importWords(is,"es-en-255");
            } catch (IOException e) {
                e.printStackTrace();
            }
            getPreferences(MODE_PRIVATE).edit().putBoolean("espeng255",true).commit();
        }
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
        tclick=0;
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
                .setFileName("quizes-exp.db")
                .showDialog();
    }
    private void importDB(){
        new FileChooser(this)
                .setFileListener(new FileChooser.FileSelectedListener() {
                    @Override
                    public void fileSelected(File file) {
                        FileOutputStream os=null;
                        FileInputStream is=null;
                        try {
                            if(file==null) return;
                            is=new FileInputStream(file);
                            String dbPath=MainActivity.this.getDatabasePath("quizes.db").getPath();
                            os=new FileOutputStream(dbPath);
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
                .setMode(FileChooser.Mode.open)
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
                             Cursor cur=getQuizData(MainActivity.this).getWords(quizname);
                             String sout;int count=0;
                             for(cur.moveToFirst();!cur.isAfterLast();cur.moveToNext()){
                                 sout=unescapeString(cur.getString(0))+" / "+unescapeString(cur.getString(1))+"\n";
                                 bw.write(sout);
                                 count++;
                             }
                             Toast toast=Toast.makeText(MainActivity.this,count+" "+getString(R.string.words_exported),Toast.LENGTH_SHORT);
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
                .setFileName(quizname+"_words.txt")
                .showDialog();
    }

    private void importWords(final String quizname){
        new FileChooser(this)
                .setFileListener(new FileChooser.FileSelectedListener() {
                    @Override
                    public void fileSelected(File file) {
                        try {
                            InputStream is=new FileInputStream(file);
                            Toast.makeText(MainActivity.this,
                                    getQuizData(MainActivity.this).importWords(is,quizname)+" "+getString(R.string.words_added),
                                    Toast.LENGTH_SHORT)
                                    .show();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
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
                .setTitle(R.string.app_name)
                .setMessage(getString(R.string.delete)+" "+quizname+"?")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        getQuizData(MainActivity.this).delTable(quizname);
                        getQuizData(MainActivity.this).delQuizSettings(quizname);
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
        String slang=null;
        Cursor cur=getQuizData(this).getQuizSettings(quizname);
        if(cur.moveToFirst()){
            ((EditText) dialog.findViewById(R.id.s_numvars)).setText(cur.getString(0));
            ((EditText) dialog.findViewById(R.id.s_dirtrans)).setText(cur.getString(1));
            ((EditText) dialog.findViewById(R.id.s_revtrans)).setText(cur.getString(2));
            ((EditText)dialog.findViewById(R.id.s_intses)).setText(unescapeString(cur.getString(3)));
            slang=cur.getString(4);
        }
        cur.close();
        Spinner sp= (Spinner) dialog.findViewById(R.id.s_lang);
        setLangs(sp,slang);
        dialog.show();
    }

    int quizn=1;
    private void newQuiz(){
        final Dialog dialog = new Dialog(this,R.style.DialogTheme);
        dialog.setContentView(R.layout.quiz_settings_view);
        ((EditText)dialog.findViewById(R.id.s_qname)).setText("quiz"+quizn++);
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
        Spinner sp= (Spinner) dialog.findViewById(R.id.s_lang);
        setLangs(sp,"en");
        dialog.show();
    }

    private void setLangs(Spinner spinner,String selection){
        String[] langs = Locale.getISOLanguages();
        ArrayList<String> localcountries=new ArrayList<String>();
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, langs);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
        if(selection==null) selection="en";
        spinner.setSelection(Arrays.asList(langs).indexOf(selection));
    }

    private void processUpdate(Dialog dialog,boolean create){
        String qname=((EditText)dialog.findViewById(R.id.s_qname)).getText().toString();
        if(create) {
            if(qname.length()==0){
                Toast.makeText(this,"Enter quiz name",Toast.LENGTH_SHORT).show();
                return;
            }
            if (getQuizData(this).tableExists(qname)) {
                Toast.makeText(MainActivity.this, R.string.table_exists, Toast.LENGTH_SHORT).show();
                return;
            }
        }
        int numVar,nDir,nRev;String vars;
        String s=((EditText) dialog.findViewById(R.id.s_numvars)).getText().toString();
        numVar = Integer.parseInt(s.length()==0?"0":s);
        s=((EditText) dialog.findViewById(R.id.s_dirtrans)).getText().toString();
        nDir = Integer.parseInt(s.length()==0?"0":s);
        s=((EditText) dialog.findViewById(R.id.s_revtrans)).getText().toString();
        nRev = Integer.parseInt(s.length()==0?"0":s);
        nDir=nDir<0?0:nDir;
        nRev=nRev<0?0:nRev;
        vars=((EditText)dialog.findViewById(R.id.s_intses)).getText().toString();
        String slang=((Spinner)dialog.findViewById(R.id.s_lang)).getSelectedItem().toString();
        if(checkVars(numVar,nDir,nRev,vars)) {
            if(create) {
                getQuizData(this).addTable(qname);
                getQuizData(this).addQuizSettings(qname, numVar, nDir, nRev, vars,slang);
            }else{
                getQuizData(this).updateQuizSettings(qname, numVar, nDir, nRev, vars,slang);
            }
            updateTables();
            dialog.dismiss();
        }
    }

    private boolean checkVars(int numVar,int nDir, int nRev, String vars){
           if(numVar<2){
                Toast.makeText(MainActivity.this, R.string.more_variants,Toast.LENGTH_SHORT).show();
                return false;
            }
            nDir=nDir<0?0:nDir;
            nRev=nRev<0?0:nRev;
            if(nDir==0 && nRev==0){
                Toast.makeText(MainActivity.this, R.string.more_repeats,Toast.LENGTH_SHORT).show();
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
            Toast.makeText(MainActivity.this, R.string.wrong_intervals,Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void updateTables(){
        Cursor cur=getQuizData(this).getWordTables();
        tablesadapter.clear();
        for(cur.moveToFirst();!cur.isAfterLast();cur.moveToNext()){
            tablesadapter.add(unescapeString(cur.getString(0)));
        }
        tablesadapter.notifyDataSetChanged();
    }

 }
