package com.cfox.wmem;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
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
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import java.util.ArrayList;

public class MainActivity extends ListActivity {

//TODO: export-import,text,xml
//TODO: settings intervals, number repeats, number variants
//TODO:onPause,onResume, ondestroy in other activities
    SimpleCursorAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
      //  Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        QuizData.context=getApplicationContext();

        final CursorCallBacks callbacks=new CursorCallBacks();

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

        final android.app.LoaderManager lm = getLoaderManager();
        lm.initLoader(CursorCallBacks.LOADER_ID, null, callbacks);

        findViewById(R.id.buttonNew).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                                    callbacks.addTable(str);
                                lm.restartLoader(CursorCallBacks.LOADER_ID, null, callbacks);
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            }
                        })
                        .show();
            }
        });
        findViewById(R.id.buttonAdd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                                      callbacks.delTable(qzname);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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

        public void addTable(String name) {

            SQLiteDatabase db = QuizData.getQuizData().getWritableDatabase();
            String sql = "CREATE TABLE " + name + " ("
                    + "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "word text unique not null, " +
                    "trans text not null, " +
                    "session integer default 0, " +
                    "datetime integer default 0);";
            db.execSQL(sql);
            updateWmemQuizes(db);
        }

        public void delTable(String name) {

            SQLiteDatabase db = QuizData.getQuizData().getWritableDatabase();
            String sql = "DROP TABLE " + name + ";";
            db.execSQL(sql);
            updateWmemQuizes(db);
            //TODO instead of update, add to wmemQuizes
        }

        private void updateWmemQuizes(SQLiteDatabase db){
           String delRecs = "DELETE FROM wmem_quizes;";
            db.execSQL(delRecs);
            String showTables1 = "SELECT name FROM sqlite_master WHERE type='table' "
                    + " and name not like 'android%' and name not like 'sqlite%' and name not like 'wmem%'" +
                    " ORDER BY name;";
            String update_wmem_table = "INSERT OR IGNORE INTO wmem_quizes(name) " + showTables1;
            db.execSQL(update_wmem_table);
        }
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
