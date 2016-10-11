package com.maps.mapsplotter;

/**
 * Created by vksingh on 23/12/14.
 */

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.concurrent.ExecutionException;

/**
 * Location sample.
 *
 * Demonstrates use of the Location API to retrieve the last known location for a device.
 * This sample uses Google Play services (GoogleApiClient) but does not need to authenticate a user.
 * See https://github.com/googlesamples/android-google-accounts/tree/master/QuickStart if you are
 * also using APIs that need authentication.
 */
public class MainActivity extends FragmentActivity implements GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    Handler mHandler=new Handler();
    String friendNumber;
    String originalFriendNum;
    boolean isPaused=false;
    Marker marker;
    String myNumber;
    MarkerOptions markerOptions;
    String address;
    LatLng latLng;
    long timeStamp;
    String contactName;
    AdView mAdView;
    boolean stopHandler=false;


    Runnable updateMarker = new Runnable() {

        @Override
        public void run() {
            if(stopHandler)
            {
                mHandler.removeCallbacks(updateMarker);
                mHandler.removeCallbacksAndMessages(null);
                return;
            }
                try {
                    setUpMap();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
               mHandler.postDelayed(this, 10000);
            }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity);
        if (!Utils.isInternetConnected(getApplicationContext())) {
            new AlertDialogManager().showAlertDialog(MainActivity.this,
                    "Internet Connection Error",
                    "Please connect to working Internet connection", false);
            // stop executing code by return
            return;
        }


        Button refresh = (Button) findViewById(R.id.Refresh);
        TextView textView=(TextView) findViewById(R.id.textView);
        Intent i = getIntent();
        friendNumber = i.getStringExtra("friendNumber");
        originalFriendNum=i.getStringExtra("originalNumber");
        System.out.println("friend Number : "+friendNumber);
        System.out.println("Original Number--------------------- : "+originalFriendNum);
        myNumber = i.getStringExtra("myNumber");

        ImageView callImage=(ImageView)findViewById(R.id.call_image);
        callImage.setImageResource(R.drawable.ic_call_white);

        callImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + originalFriendNum));
                startActivity(callIntent);
            }
        });


        contactName=Utils.getNameFromContact(getApplicationContext(),friendNumber);
        if(contactName!=null)
        {
            textView.setText("Tracking : "+contactName);
        }
        else
        {
            textView.setText("Tracking : "+friendNumber);
        }
        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);



        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                try {
//                    setUpMap();
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
                Pair pair = new Pair();
                pair.setContext(getApplicationContext());
                pair.setMyNumber(Utils.myNumber(getApplicationContext()));
                pair.setFriendOneNum(friendNumber);
                pair.setFriendTwoNum("shareCurrentLocation");
                new SendNotificationAsynTask().execute(pair);
                if(contactName!=null)
                {
                    Toast.makeText(getApplicationContext(),"Share current location request sent to " + contactName +  " !", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(getApplicationContext(),"Share current location request sent to " + friendNumber +" !", Toast.LENGTH_SHORT).show();
                }

            }

        });
        try {
            setUpMapIfNeeded();
            mHandler.postDelayed(updateMarker, 10000);
            final InterstitialAd mInterstitialAd = new InterstitialAd(getApplicationContext());
            mInterstitialAd.setAdUnitId("ca-app-pub-3947201996699849/9394878411");
            AdRequest adRequestInter = new AdRequest.Builder().build();
            mInterstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    mInterstitialAd.show();
                }
            });
            mInterstitialAd.loadAd(adRequestInter);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
       System.out.println("inside onpause");
       isPaused=true;
       mHandler.removeCallbacks(updateMarker);

       super.onPause();
       // finish();
    }

    @Override
    protected  void onResume()
    {
        super.onResume();
        if(isPaused) {
            mHandler.postDelayed(updateMarker, 10000);
            System.out.println("inside resume");
            isPaused=false;
        }
    }

//    @Override
//    public void onBackPressed() {
//
//           //this is the last activity on stack
//            Intent intent = new Intent(this,MapsActivity.class);
//            startActivity(intent);
//        finish();
//    }
    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     */
    private void setUpMapIfNeeded() throws JSONException {
    if(mMap==null) {
    // Try to obtain the map from the SupportMapFragment.
        mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
            .getMap();
        if(mMap != null)
        {


        mMap.setMyLocationEnabled(true);
        mMap.setTrafficEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.setOnMarkerClickListener(this);
        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                if (mMap.getCameraPosition().zoom > 17 && mMap.getMapType() != 4) {
                    mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                } else if (mMap.getCameraPosition().zoom <= 17 && mMap.getMapType() != 1) {
                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                }
            }
        });
        setUpMap();
        }
    }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() throws JSONException {

        Pair pair=new Pair();
        pair.setContext(this);
        pair.setFriendNumber(friendNumber);
        pair.setMyNumber(myNumber);

        pair.setLatitude("Get");
        String str= null;
        try {
            str = new ServletPostAsyncTask().execute(pair).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        System.out.println(str);
        if(str.contains("data doesn't exists"))
        {
            stopHandler=true;
            AlertDialog.Builder
                    alertDialog = new AlertDialog.Builder(this);
            alertDialog.setTitle("Invite Friend");
            if(contactName!=null) {
                alertDialog.setMessage(contactName + " is not using TrackOn. Invite your friend using whats app to join TrackOn to see his/her location");
            }
            else
            {
                alertDialog.setMessage(friendNumber + " is not using TrackOn. Invite your friend using whats app to join TrackOn to see his/her location");
            }

            //On Pressing Setting button
            alertDialog.setPositiveButton("Invite", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    PackageManager pm=getPackageManager();
                    try {
                        Intent waIntent = new Intent(Intent.ACTION_SEND);
                        waIntent.setType("text/plain");
                        String text = "Download TrackOn to track friends and family location in real time. https://play.google.com/store/apps/details?id=com.maps.mapsplotter Install Now !";

                        PackageInfo info=pm.getPackageInfo("com.whatsapp", PackageManager.GET_META_DATA);
                        //Check if package exists or not. If not then code
                        //in catch block will be called
                        waIntent.setPackage("com.whatsapp");

                        waIntent.putExtra(Intent.EXTRA_TEXT, text);
                        startActivity(Intent.createChooser(waIntent, "Share with"));
                        finish();

                    } catch (Exception e) {
                        Intent smsIntent = new Intent(Intent.ACTION_VIEW);
                    smsIntent.setType("vnd.android-dir/mms-sms");
                    smsIntent.putExtra("address", "0"+friendNumber);
                    smsIntent.putExtra("sms_body","Download TrackOn to track friends and family location in real time. https://play.google.com/store/apps/details?id=com.maps.mapsplotter Install Now !");
                    startActivity(smsIntent);
                    finish();
                    }
                }
            });
            //On pressing cancel button
            alertDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    finish();
                }
            });

            alertDialog.show();
        }
        else if(str.contains("access denied"))
        {
            stopHandler=true;
            AlertDialog.Builder
            alertDialog = new AlertDialog.Builder(this);
            alertDialog.setTitle("Location Access Denied");
            if(contactName!=null) {
                alertDialog.setMessage("Send access request to " + contactName + " using TrackOn");
            }
            else
            {
                alertDialog.setMessage("Send access request to " + friendNumber + " using TrackOn");
            }
            //On Pressing Setting button
            alertDialog.setPositiveButton("Send", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    Pair pair = new Pair();
                    pair.setContext(getApplicationContext());
                    pair.setMyNumber(Utils.myNumber(getApplicationContext()));
                    pair.setFriendOneNum(friendNumber);
                    pair.setFriendTwoNum("accessRequest");
                    new SendNotificationAsynTask().execute(pair);
                    Toast.makeText(getApplicationContext(),"Access request sent !", Toast.LENGTH_SHORT).show();
                }
            });
            //On pressing cancel button
            alertDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    finish();
                }
            });

            alertDialog.show();
           // Toast.makeText(getApplicationContext(),"Location Access Denied ! Send access request to your friend using TrackOn", Toast.LENGTH_LONG).show();

        }
        else {
            JSONTokener tokener = new JSONTokener(str);
            JSONObject object = new JSONObject(tokener);

            double lat = Double.valueOf(object.getString("lat"));
            double lon = Double.valueOf(object.getString("lon"));
            timeStamp=Long.valueOf(object.getString("time"));
            LocationAddress locationAddress = new LocationAddress();
            locationAddress.getAddressFromLocation(lat, lon,getApplicationContext(), new GeocoderHandler());
             latLng = new LatLng(lat, lon);
//            if (marker == null) {
//                MarkerOptions markerOptions = new MarkerOptions()
//                        .title(address)
//                        .position(latLng);
//                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
//                marker = mMap.addMarker(markerOptions);
//            } else {
//                mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(lat, lon)));
//                marker.setPosition(new LatLng(lat, lon));
//            }


        }
    }
    @Override
    public boolean onMarkerClick(Marker marker) {
        String msg=Utils.getLastUpdatedTime(timeStamp);
        marker.setSnippet(msg);
        marker.showInfoWindow();
        return true;
    }


    private class GeocoderHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            String result;
            switch (message.what) {
                case 1:
                    Bundle bundle = message.getData();
                    result = bundle.getString("address");
                    break;
                default:
                    result = null;
            }
            address=result;
            if (marker == null) {
            MarkerOptions markerOptions = new MarkerOptions()
                    .title(address)
                    //.icon((BitmapDescriptorFactory.fromBitmap(Utils.getPhoto(getApplicationContext(), friendNumber))))
                    .position(latLng);
                if(mMap!=null) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                    marker = mMap.addMarker(markerOptions);
                    onMarkerClick(marker);
                    marker.showInfoWindow();
                }
        } else {
                if(mMap!=null) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    onMarkerClick(marker);
                    marker.setPosition(latLng);
                }
        }
        Log.d("address ",result);
        }
    }

}