package com.anthonynahas.autocallrecorder.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.anthonynahas.autocallrecorder.R;

import dagger.android.AndroidInjection;

public class SearchActivity extends AppCompatActivity{
    Button mBtnBack;
    Button mBtnClose;
    EditText mEditSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        mBtnBack = (Button)findViewById(R.id.action_search_back);
        mBtnClose = (Button)findViewById(R.id.action_search_clean);
        mEditSearch = (EditText)findViewById(R.id.action_search_text);

        mBtnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mBtnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mEditSearch.setText(null);
            }
        });

        mEditSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(mEditSearch.getText().equals("")){
                    mBtnClose.setVisibility(View.GONE);
                } else {
                    mBtnClose.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }


//    @Override
//    public boolean onCreateOptionsMenu(Menu menu){
//        super.onCreateOptionsMenu(menu);
//        getMenuInflater().inflate(R.menu.search_menu , menu);
//        return true;
//    }
}
