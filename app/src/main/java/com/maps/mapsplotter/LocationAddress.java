package com.maps.mapsplotter;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.List;
import java.util.Locale;
/**
 * Created by vksingh on 15/02/15.
 */
public class LocationAddress {
    private static final String TAG = "LocationAddress";

    public  void getAddressFromLocation(final double latitude, final double longitude,
                                              final Context context, final Handler handler) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                String result = null;
                try {
                    List<Address> addressList = geocoder.getFromLocation(
                            latitude, longitude, 1);
                    if (addressList != null && addressList.size() > 0) {

                        Address address = addressList.get(0);
                        System.out.println("My address " +address);
                        StringBuilder sb = new StringBuilder();
//                        for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                        System.out.println(address.getLocality());
                        if(address!=null) {
                            String shortAddress[] = address.getAddressLine(0).split(",");
                            sb.append(shortAddress[0]);
                            if (shortAddress[0].length() < 20) {
                                if(shortAddress.length>1) {
                                    sb.append(",");
                                    sb.append(shortAddress[1]);
                                }
                            }
                        }
                        result = sb.toString();

                    }
                } catch (Exception e) {
                    Log.e(TAG, "Unable connect to Geocoder", e);
                }
                finally {
                    Message message = Message.obtain();
                    message.setTarget(handler);
                    if (result != null) {
                        message.what = 1;
                        Bundle bundle = new Bundle();
//                        result = "Latitude: " + latitude + " Longitude: " + longitude +
//                                "\n\nAddress:\n" + result;
                        bundle.putString("address", result);
                        message.setData(bundle);
                    } else {
                        message.what = 1;
                        Bundle bundle = new Bundle();
                        result = "Address not available !";
                        //result =  "\n Unable to get address for this lat-long.";
                        bundle.putString("address", result);
                        message.setData(bundle);
                    }
                    message.sendToTarget();
                }
            }
        };
        thread.start();
    }
}