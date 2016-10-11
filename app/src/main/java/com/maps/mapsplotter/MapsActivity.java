package com.maps.mapsplotter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.maps.database.DatabaseHandler;

import java.util.Calendar;
import java.util.HashMap;


public class MapsActivity extends FragmentActivity implements
        LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnMarkerClickListener {

    private static final long INTERVAL = 10000;
    private static final long FASTEST_INTERVAL = 5000;
    private static final long ONE_MIN = 1000 * 60;
    private static final long REFRESH_TIME = 100;
    private static final float MINIMUM_ACCURACY = 50.0f;
    public Location location;
    AlertDialogManager alert = new AlertDialogManager();
    boolean animationOver = false;
    String number;
    boolean shareMyLocation = false;
    AlertDialog.Builder alertDialog;
    HashMap<String, String> friendNumMap;
    ToggleButton start;
    Button GetServerData;
    Button friendList;
    ToggleButton traffic;
    boolean locationServiceDisabled = false;
    private LocationRequest locationRequest;
    private GoogleApiClient googleApiClient;
    LocationManager locationManager;
    private FusedLocationProviderApi fusedLocationProviderApi = LocationServices.FusedLocationApi;
    private GoogleMap mMap;
    private Marker marker;
    String address;
    MarkerOptions markerOptions;
    long timeOnLoad;
    private InterstitialAd adView;  // The ad
    private Handler mHandler;       // Handler to display the ad on the UI thread
    private Runnable displayAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getApplicationContext().sendBroadcast(new Intent("com.google.android.intent.action.GTALK_HEARTBEAT"));
        getApplicationContext().sendBroadcast(new Intent("com.google.android.intent.action.MCS_HEARTBEAT"));

//        //show error dialog if GoolglePlayServices not available
//        if (!isGooglePlayServicesAvailable()) {
//            finish();
//        }
        // Check if location service is enabled or not
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean isLSEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (!isLSEnabled) {
            showSettingsAlert();
            locationServiceDisabled = true;
            alertDialog.show();
        }
        Calendar calendar=Calendar.getInstance();
        timeOnLoad=calendar.getTimeInMillis();

        // Check if Internet present
        if (!Utils.isInternetConnected(getApplicationContext())) {
            alert.showAlertDialog(MapsActivity.this,
                    "Internet Connection Error",
                    "Please connect to working Internet connection", false);
            // stop executing code by return
            return;
        }
        setContentView(R.layout.activity_maps);
        GetServerData = (Button) findViewById(R.id.GetServerData);
        friendList = (Button) findViewById(R.id.friendList);

        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();

        showInterstitialAd();
        friendList.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                friendList.setBackgroundColor(R.id.action_mode_close_button);
                friendList.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        friendList.setBackgroundColor(getApplicationContext().getResources().getColor(R.color.text_msg_input));
                    }
                }, 1000);

                Intent intent = new Intent(getApplicationContext(), FriendList.class);
                startActivity(intent);
            }
        });

        //Toogle button for traffic
        traffic = (ToggleButton) findViewById(R.id.toggleButton);
        traffic.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    traffic.setBackgroundColor(R.id.action_mode_close_button);
                    if(mMap!=null) {
                        mMap.setTrafficEnabled(true);
                    }
                } else {
                    traffic.setBackgroundColor(getApplicationContext().getResources().getColor(R.color.text_msg_input));
                    if(mMap!=null) {
                        mMap.setTrafficEnabled(false);
                    }
                }
            }
        });

        // Toogle Button for start
        start = (ToggleButton) findViewById(R.id.startStop);
        start.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (!Utils.isInternetConnected(getApplicationContext())) {
                        alert.showAlertDialog(MapsActivity.this,
                                "Internet Connection Error",
                                "Please connect to working Internet connection", false);
                        // stop executing code by return
                        return;
                    }
                    try {
                        DatabaseHandler db = new DatabaseHandler(getApplicationContext());
                        if (db.getContactsCount() == 0) {

                            AlertDialog.Builder
                                    alertDialog = new AlertDialog.Builder(MapsActivity.this);

                            alertDialog.setTitle("Friend List Empty !");

                            alertDialog.setMessage("Add maximum 3 friend's with whom you want to share your location ");

                            //On Pressing Setting button
                            alertDialog.setPositiveButton("Add Friend", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    Intent intent = new Intent(getApplicationContext(), FriendList.class);
                                    startActivity(intent);
                                }
                            });
                            //On pressing cancel button
                            alertDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });

                            alertDialog.show();
                            buttonView.setChecked(false);

                        }
                        else if (!locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                                showSettingsAlert();
                                locationServiceDisabled = true;
                                alertDialog.show();
                            }
                        else {
                            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                                start.setBackgroundColor(R.id.action_mode_close_button);
                                showSettingsAlert();
                                alertDialog.setTitle("Tips : Location Accuracy");
                                alertDialog.setMessage("For real time tracking experience change location service to High Accuracy/GPS mode");
                                alertDialog.show();
                            }
                            googleApiClient.disconnect();
                            shareMyLocation = true;
                            googleApiClient.connect();
                            start.setBackgroundColor(R.id.action_mode_close_button);
                            Toast.makeText(getApplicationContext(),
                                    "Your Location Sharing With Friend's Started", Toast.LENGTH_SHORT).show();
                           friendNumMap= Utils.readFriendNumberFromDB(getApplicationContext());
                            Pair pair = new Pair();
                            pair.setContext(getApplicationContext());
                            pair.setMyNumber(number);
                            if (friendNumMap.containsKey("1")) {
                                pair.setFriendOneNum(friendNumMap.get("1"));
                            }
                            else
                            {
                                pair.setFriendOneNum("1111111");
                            }
                            if (friendNumMap.containsKey("2")) {
                                pair.setFriendTwoNum(friendNumMap.get("2"));
                            }
                            else
                            {
                                pair.setFriendTwoNum("222222");
                            }
                            if (friendNumMap.containsKey("3")) {
                                pair.setFriendThreeNum(friendNumMap.get("3"));
                            }
                            else
                            {
                                pair.setFriendThreeNum("3333333");
                            }
                            new SendNotificationAsynTask().execute(pair);
                            System.out.println("Sent Notification to friend's");
                        }
                        }catch(Exception e){
                            e.printStackTrace();
                        }

                } else {
                    start.setBackgroundColor(getApplicationContext().getResources().getColor(R.color.text_msg_input));
                    googleApiClient.disconnect();
                    Toast.makeText(getApplicationContext(),
                            "Your Location Sharing With Friend's Stopped", Toast.LENGTH_SHORT).show();

                }
            }
        });

        //Onclick listner for Track Friend Button
        GetServerData.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                GetServerData.setBackgroundColor(R.id.action_mode_close_button);
                GetServerData.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        GetServerData.setBackgroundColor(getApplicationContext().getResources().getColor(R.color.text_msg_input));
                    }
                }, 1000);

                Intent intent = new Intent(getApplicationContext(), Display.class);
                intent.putExtra("myNumber", number);
                startActivity(intent);
            }
        });

        // Fused APi client builder and location request object
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        locationRequest.setInterval(INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);
        locationRequest.setSmallestDisplacement(0);
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();


        if (googleApiClient != null) {
            googleApiClient.connect();
        }

        // Getting the user number after map loads
        number = Utils.myNumber(getApplicationContext());
        Toast.makeText(getApplicationContext(),
                number, Toast.LENGTH_SHORT).show();
    }

   private void showInterstitialAd()
   {
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
   }

    private boolean isGooglePlayServicesAvailable() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == status) {
            return true;
        } else {
            GooglePlayServicesUtil.getErrorDialog(status, this, 0).show();
            return false;
        }
    }

    @Override
    public void onBackPressed() {
        showInterstitialAd();
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (googleApiClient != null) {
            googleApiClient.connect();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (googleApiClient != null && googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }

    // Setting up the map if already not plotted
    private void setUpMapIfNeeded() {
        if (mMap == null) {
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setZoomControlsEnabled(false);
            mMap.setBuildingsEnabled(true);
            mMap.getUiSettings().setMapToolbarEnabled(true);
            setUpMap();
        }

    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     */
    private void setUpMap() {

        if (null != location) {
            double lat = location.getLatitude();
            double lon = location.getLongitude();


            AppLocationService appLocationService = new AppLocationService(
                    MapsActivity.this);
            Location location = appLocationService
                    .getLocation(LocationManager.NETWORK_PROVIDER);
            if(location!=null) {
                LocationAddress locationAddress = new LocationAddress();
                locationAddress.getAddressFromLocation(location.getLatitude(), location.getLongitude(), getApplicationContext(), new GeocoderHandler());
            }
            LatLng latLng = new LatLng(lat, lon);
            CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(latLng, 15);
           if(mMap!=null) {
            mMap.setOnMarkerClickListener(this);
            mMap.animateCamera(yourLocation);
           }
            mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
                @Override
                public void onCameraChange(CameraPosition cameraPosition) {
                    if(mMap!=null) {
                        if (mMap.getCameraPosition().zoom > 17 && mMap.getMapType() != 4) {
                            mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                        } else if (mMap.getCameraPosition().zoom <= 17 && mMap.getMapType() != 1) {
                            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        }
                    }
                }
            });

        }
    }

    @Override
    public void onConnected(Bundle dataBundle) {
        if (locationServiceDisabled) {
            fusedLocationProviderApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        } else {
            Location currentLocation = fusedLocationProviderApi.getLastLocation(googleApiClient);
            if (currentLocation != null) {   //&& currentLocation.getTime() > REFRESH_TIME
                location = currentLocation;
               // new ServletPostAsyncTask().execute(getPairObject(location));
                setUpMapIfNeeded();

            }
            if (shareMyLocation) {
                fusedLocationProviderApi.requestLocationUpdates(googleApiClient, locationRequest, this);
                Pair pair = new Pair();
                pair.setContext(this);
                pair.setMyNumber(number);
                new ServletPostAsyncTask().execute(pair);

            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location newlocation) {
        //if (null == this.location ) { //|| location.getAccuracy() < this.location.getAccuracy()

        if (locationServiceDisabled) {
            location = newlocation;
            setUpMapIfNeeded();
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, MapsActivity.this);
            locationServiceDisabled = false;

        } else {

            Calendar calendar=Calendar.getInstance();
            timeOnLoad=calendar.getTimeInMillis();
            location = newlocation;
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            System.out.println(latLng);
            if (animationOver) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, mMap.getCameraPosition().zoom));
            }
            marker.setPosition(latLng);
            new ServletPostAsyncTask().execute(getPairObject(location));
            System.out.println("updated location coordinates");
            animationOver = true;
        }

    }


//    public Pair getPair()
//    {
//
//    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public void showSettingsAlert() {
        alertDialog = new AlertDialog.Builder(this);

        //Setting Dialog Title
        alertDialog.setTitle(R.string.GPSAlertDialogTitle);

        //Setting Dialog Message
        alertDialog.setMessage(R.string.GPSAlertDialogMessage);

        //On Pressing Setting button
        alertDialog.setPositiveButton(R.string.settings, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });

        //On pressing cancel button
        alertDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // alertDialog.show();
    }


    public int getLocationMode(Context context) throws Settings.SettingNotFoundException {
        return Settings.Secure.getInt(getContentResolver(), Settings.Secure.LOCATION_MODE);

    }

    public Pair getPairObject(Location location)
    {
        Pair pair = new Pair();
        pair.setContext(this);
        pair.setMyNumber(number);
        friendNumMap=Utils.readFriendNumberFromDB(getApplicationContext());
        if (friendNumMap.containsKey("1")) {
            pair.setFriendOneNum(friendNumMap.get("1").toString());
        }
        else
        {
            pair.setFriendOneNum("1111111");
        }
        if (friendNumMap.containsKey("2")) {
            pair.setFriendTwoNum(friendNumMap.get("2").toString());
        }
        else
        {
            pair.setFriendTwoNum("222222");
        }
        if (friendNumMap.containsKey("3")) {
            pair.setFriendThreeNum(friendNumMap.get("3").toString());
        }
        else
        {
            pair.setFriendThreeNum("3333333");
        }
        pair.setLatitude(String.valueOf(location.getLatitude()));
        pair.setLongitude(String.valueOf(location.getLongitude()));
        return pair;
    }



    @Override
    public boolean onMarkerClick(Marker marker) {
        String msg=Utils.getLastUpdatedTime(timeOnLoad);
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
                    System.out.println("i am here dude watch me out !");
                    break;
                default:
                    result = null;
            }
            address=result;
//            AQuery androidQuery = new AQuery(getApplicationContext());
//
//            androidQuery.ajax("https://www.facebook.com/profile.php?id=729063561", Bitmap.class, 0, new AjaxCallback<Bitmap>() {
//                @Override
//                public void callback(String url, Bitmap object, AjaxStatus status) {
//                    super.callback(url, object, status);
//                    marker= mMap.addMarker(new MarkerOptions().title("yourtitle").icon(BitmapDescriptorFactory.fromBitmap(object)).position(new LatLng(location.getLatitude(), location.getLongitude())));
//                }
//            });
            markerOptions=new MarkerOptions();
            markerOptions.title(result)
                         .position(new LatLng(location.getLatitude(), location.getLongitude()));

            marker = mMap.addMarker(markerOptions);
            onMarkerClick(marker);
            Log.d("Address", result);
        }
    }



}