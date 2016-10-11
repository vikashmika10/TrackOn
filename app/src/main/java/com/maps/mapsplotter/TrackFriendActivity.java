package com.maps.mapsplotter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by vksingh on 26/01/15.
 */
public class TrackFriendActivity  extends Activity {

    private Button btnJoin;
    private EditText txtName;
    private TextView title;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.enter_phone_number);

        title = (TextView) findViewById(R.id.title);
        title.setText("Enter your friend phone number");
        btnJoin = (Button) findViewById(R.id.btnJoin);
        btnJoin.setText("Track Friend");
        txtName = (EditText) findViewById(R.id.name);
        Intent i = getIntent();
        String number = i.getStringExtra("friendNumber");
        final String myNumber=i.getStringExtra("myNumber");
        txtName.setText(Utils.removeSpecialCharFromString(number));
        btnJoin.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (txtName.getText().toString().trim().length() > 0) {

                    String number = txtName.getText().toString().trim();
                    if(!TextUtils.isDigitsOnly(number) || number.length()>10)
                    {

                        Toast.makeText(getApplicationContext(),
                                "Please enter valid number", Toast.LENGTH_LONG).show();
                        return;
                    }

                        Intent intent = new Intent(TrackFriendActivity.this,
                                MainActivity.class);
                        intent.putExtra("friendName", number);
                        intent.putExtra("myNumber", myNumber);
                        startActivity(intent);
                    finish();

                } else {
                    Toast.makeText(getApplicationContext(),
                            "Please enter friend's number", Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    @Override
    public void onPause()
    {
        super.onPause();
    }
}
