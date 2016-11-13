package com.cfox.wmem;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;

public class AddWordsActivity extends Activity  {

    public static final String KEY_QUIZNAME="com.cfox.wmem.quizname";
    String quizname;
    // UI references.
    private EditText mWordView;
    private EditText mTrans;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_words);

        quizname=getIntent().getStringExtra(KEY_QUIZNAME);
        final TextView caption=(TextView) findViewById(R.id.quizname);
        String scap=quizname+"("+QuizData.getQuizData().getWordCount(quizname)+")";
        caption.setText(scap);
        mWordView = (EditText) findViewById(R.id.word);
        mTrans=(EditText)findViewById(R.id.trans);
        ((Button) findViewById(R.id.add)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String sw=mWordView.getText().toString();
                String st=mTrans.getText().toString();
                if(sw.equals("")){
                    mWordView.requestFocus();
                    return;
                }
                if(st.equals("")){
                    mTrans.requestFocus();
                    return;
                }
                int count=QuizData.getQuizData().addWord(quizname,sw,st);
                String scap=quizname+"("+count+")";
                caption.setText(scap);
                mWordView.setText("");
                mTrans.setText("");
                mWordView.requestFocus();
                View view = AddWordsActivity.this.getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(view,0);
                }
            }
        });
        ((Button) findViewById(R.id.clear)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mWordView.setText("");
                mTrans.setText("");
            }
        });
    }
}

