package com.maps.mapsplotter;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by vksingh on 26/01/15.
 */
public class FirstActivity extends Activity {

    private Button btnJoin;
    private EditText txtName;
    private static final String PREF_NAME = "MapsPlotter";
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    private static final String IS_LOGIN = "IsLoggedIn";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pref = getApplicationContext().getSharedPreferences(PREF_NAME, 0);
        editor = pref.edit();

     ConnectionDetector cd = new ConnectionDetector(getApplicationContext());
        // Check if Internet present
        if (!cd.isConnectingToInternet()) {
            new AlertDialogManager().showAlertDialog(FirstActivity.this,
                    "Internet Connection Error",
                    "Please connect to working Internet connection", false);
        }

        if(pref.getBoolean(IS_LOGIN,false))
        {
            Intent intent = new Intent(FirstActivity.this,
                    MapsActivity.class);
            intent.putExtra("name", pref.getString("number",null));
            startActivity(intent);

        }
        setContentView(R.layout.enter_phone_number);

        btnJoin = (Button) findViewById(R.id.btnJoin);
        txtName = (EditText) findViewById(R.id.name);
        btnJoin.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (txtName.getText().toString().trim().length() > 0) {
                    String number = txtName.getText().toString().trim();

                    if(!TextUtils.isDigitsOnly(number) || number.length()!=10)
                    {

                        Toast.makeText(getApplicationContext(),
                                "Please enter valid number", Toast.LENGTH_LONG).show();
                        return;
                    }
                    new GcmRegistrationAsyncTask(getApplicationContext()).execute(number);
                    Pair pair = new Pair();
                    pair.setContext(getApplicationContext());
                    pair.setMyNumber(number);
                    pair.setFriendOneNum("1111111");
                    pair.setFriendTwoNum("222222");
                    pair.setFriendThreeNum("3333333");
                    Location location=new AppLocationService(getApplicationContext()).getLocation(LocationManager.NETWORK_PROVIDER);
                    if(location!=null)
                    {
                        pair.setLatitude(String.valueOf(location.getLatitude()));
                        pair.setLongitude(String.valueOf(location.getLongitude()));
                    }
                    else {
                        pair.setLatitude("0.0");
                        pair.setLongitude("0.0");
                    }
                    new ServletPostAsyncTask().execute(pair);
                    editor.putBoolean(IS_LOGIN,true);
                    editor.putString("number",number);
                    editor.commit();

                    Intent intent = new Intent(FirstActivity.this,
                            MapsActivity.class);

                    startActivity(intent);

                } else {
                    Toast.makeText(getApplicationContext(),
                            "Please enter your number", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}
