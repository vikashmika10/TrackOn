package com.maps.mapsplotter;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.maps.database.DatabaseHandler;

/**
 * Created by vksingh on 03/02/15.
 */
public class FriendList extends TabActivity {
   public static TabHost tabHost;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tab);

        Resources ressources = getResources();

        tabHost = getTabHost();
        tabHost.getTabWidget().setDividerDrawable(null);
        tabHost.getTabWidget().setStripEnabled(false);
        tabHost.getTabWidget().setRightStripDrawable(android.R.color.transparent);
        tabHost.getTabWidget().setLeftStripDrawable(android.R.color.transparent);
       // tabHost.getTabWidget().set

        if (!Utils.isInternetConnected(getApplicationContext())) {
            new AlertDialogManager().showAlertDialog(FriendList.this,
                    "Internet Connection Error",
                    "Please connect to working Internet connection", false);
            // stop executing code by return
            return;
        }


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

        Intent intentApple = new Intent().setClass(this, MyFriendListActivity.class);
        TabHost.TabSpec tabSpecApple = tabHost
                .newTabSpec("My Friend List")
                .setIndicator("My List", null)
                .setContent(intentApple);
        // Android tab
        Intent intentAndroid = new Intent().setClass(this, MenuActivity.class);
        TabHost.TabSpec tabSpecAndroid = tabHost
                .newTabSpec("Add Friend")
                .setIndicator("Add Friend", null)
                .setContent(intentAndroid);

        tabHost.addTab(tabSpecApple);
        tabHost.addTab(tabSpecAndroid);

        //set Windows tab as default (zero based)
        tabHost.setCurrentTab(0);

        DatabaseHandler db = new DatabaseHandler(this);
        if(db.getContactsCount()==0)
        {
            Toast.makeText(getApplicationContext(),
                                  "Your Friend List Is Empty ! ", Toast.LENGTH_SHORT).show();
        }


    }

}