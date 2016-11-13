package com.cfox.wmem;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.cfox.fdlg.FileChooser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends ListActivity {

    //TODO: change buttons with img and menu
//TODO: export-import,text,xml
//TODO: settings intervals, number repeats, number variants
//TODO:onPause,onResume, ondestroy in other activities
    SimpleCursorAdapter mAdapter;
    private CursorCallBacks callbacks;
    android.app.LoaderManager lm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //  Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);




        QuizData.context=getApplicationContext();

        callbacks=new CursorCallBacks();

        mAdapter =
                new SimpleCursorAdapter(
                        this,                // Current context
                        android.R.layout.simple_list_item_single_choice,  // Layout for a single row
                        null,                // No Cursor yet
                        new String[]{"name"},        // Cursor columns to use
                        new int[]{android.R.id.text1},           // Layout fields to use
                        0                    // No flags
                );

        ListView listView = getListView();
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new ListView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> p1, View p2, int p3, long p4)
            {
                MainActivity.this.invalidateOptionsMenu();
            }
        });

        lm = getLoaderManager();
        lm.initLoader(CursorCallBacks.LOADER_ID, null, callbacks);

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
                int pos=MainActivity.this.getListView().getCheckedItemPosition();
                if(pos>-1) {
                    final String qzname = (String)((Cursor) mAdapter.getItem(pos)).getString(1);
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
                int pos=MainActivity.this.getListView().getCheckedItemPosition();
                if(pos>-1) {
                    final String qzname = (String)((Cursor) mAdapter.getItem(pos)).getString(1);
                    if(qzname!=null && !qzname.equals("")) {
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("wmem")
                                .setMessage("delete "+qzname+"?")
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        QuizData.getQuizData().delTable(qzname);
                                        lm.restartLoader(CursorCallBacks.LOADER_ID, null, callbacks);
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
        });

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
        if(this.getListView().getCheckedItemPosition()==ListView.INVALID_POSITION){
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
            case R.id.action_psettings:
                return true;
        }
        int pos=MainActivity.this.getListView()
                .getCheckedItemPosition();
        String quizname=null;
        if(pos>-1) {
            quizname = (String)((Cursor) mAdapter.getItem(pos)).getString(1);
            if(quizname==null && quizname.equals("")) {
                return true;
            }
        }
        switch(id){
            case R.id.action_add:
                addWords();
                return true;
            case R.id.action_start:
                return true;
            case R.id.action_exportw:
                exportDB();
                return true;
            case R.id.action_importw:
                importWords(quizname);
                return true;
            case R.id.action_qsettings:
                return true;
        }
        return true;
    }

    private void exportDB(){
        File outfile=null;
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
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            if(is!=null)
                                is.close();
                            if(os!=null)
                                os.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }


                    }
                })
                .setMode(FileChooser.Mode.create)
                .showDialog();
    }

    class CursorCallBacks implements LoaderManager.LoaderCallbacks<Cursor> {

        private static final int LOADER_ID = 1;

        public CursorCallBacks() {
            //           quizes=new QuizData(MainActivity.this);
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            MyCursorLoader loader = new MyCursorLoader(MainActivity.this);
            return loader;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
            mAdapter.changeCursor(cursor);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            mAdapter.changeCursor(null);
        }



        private void updateWmemQuizes(SQLiteDatabase db) {
            String delRecs = "DELETE FROM wmem_quizes;";
            db.execSQL(delRecs);
            String showTables1 = "SELECT name FROM sqlite_master WHERE type='table' "
                    + " and name not like 'android%' and name not like 'sqlite%' and name not like 'wmem%'" +
                    " ORDER BY name;";
            String update_wmem_table = "INSERT OR IGNORE INTO wmem_quizes(name) " + showTables1;
            db.execSQL(update_wmem_table);
        }
    }

    private void importWords(String quizname){
        String sw="w";
        String st="t";

        SQLiteDatabase db=QuizData.getQuizData().getWritableDatabase();
        sw= DatabaseUtils.sqlEscapeString(sw);
        st=DatabaseUtils.sqlEscapeString(st);
        String winsert="INSERT OR IGNORE INTO "+quizname+" (word,trans,session,datetime) VALUES (?, ?, 0, 0);";
        db.execSQL(winsert,new String[]{sw,st});
    }

    private void addWords(){
        int pos=MainActivity.this.getListView().getCheckedItemPosition();
        if(pos>-1) {
            final String qzname = (String)((Cursor) mAdapter.getItem(pos)).getString(1);
            if(qzname!=null && !qzname.equals("")) {
                Intent i = new Intent(MainActivity.this, AddWordsActivity.class);
                i.putExtra(AddWordsActivity.KEY_QUIZNAME, qzname);
                startActivity(i);
            }
        }

    }

    private void newQuiz(){
        final EditText txtName = new EditText(MainActivity.this);
        // Set the default text to a link of the Queen
        txtName.setHint("quiz name");
        txtName.setSingleLine();

        new AlertDialog.Builder(MainActivity.this)
                .setTitle("quiz name")
                .setMessage("enter quiz name")
                .setView(txtName)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String str = txtName.getText().toString();
                        if (str != null && !str.equals(""))
                            QuizData.getQuizData().addTable(str);
                        lm.restartLoader(CursorCallBacks.LOADER_ID, null, callbacks);
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                })
                .show();
    }


    static class MyCursorLoader extends CursorLoader {

        public MyCursorLoader(Context context) {
            super(context);
            //           this.quizes = quizes;
        }

        @Override
        public Cursor loadInBackground() {
            return getTables();
        }


        private Cursor getTables() {
            SQLiteDatabase db = QuizData.getQuizData().getReadableDatabase();
            String get_tables="Select * from wmem_quizes";
            Cursor cursor=db.rawQuery(get_tables,null);
            return cursor;
        }
    }
 }
