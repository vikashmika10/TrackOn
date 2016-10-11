package com.maps.mapsplotter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

/**
 * Created by vksingh on 29/01/15.
 */



public class Display extends Activity  {
    private ListView mListView;
    private SampleAdapter mAdapter;
    EditText inputSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        this.mListView = (ListView) findViewById(R.id.lv_sample_list);
        this.mAdapter = new SampleAdapter(this);
        this.mListView.setAdapter(mAdapter);
        inputSearch = (EditText) findViewById(R.id.searchbox);
        inputSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                // When user changed the Text
                mAdapter.getFilter().filter(cs);
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                          int arg3) {
            }

            @Override
            public void afterTextChanged(Editable arg0) {
            }
        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

                StringBuilder checkedcontacts = new StringBuilder();
                if (mAdapter.name1.size() != 0) {
                    try {
                        checkedcontacts.append(mAdapter.name1.get(position).toString());

                        Toast.makeText(Display.this, checkedcontacts, Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.putExtra("originalNumber",mAdapter.phno1.get(position));
                        intent.putExtra("friendNumber", Utils.removeSpecialCharFromString(mAdapter.phno1.get(position)));
                        intent.putExtra("myNumber", Utils.myNumber(getApplicationContext()));
                        startActivity(intent);
                        InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                    } catch (Exception e) {
                        Toast.makeText(Display.this, "Please select a valid contact", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(Display.this, "Please select a valid contact", Toast.LENGTH_SHORT).show();
                }

            }


        });
    }

}
