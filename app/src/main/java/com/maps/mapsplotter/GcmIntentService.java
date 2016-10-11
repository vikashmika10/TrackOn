package com.maps.mapsplotter;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by vksingh on 17/02/15.
 */
public class GcmIntentService extends IntentService {

    Random random=new Random();
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;
    public static final String TAG = "GCMNotificationIntentService";


    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (extras != null && !extras.isEmpty()) {  // has effect of unparcelling Bundle
            // Since we're not using two way messaging, this is all we really to check for
            if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                Logger.getLogger("GCM_RECEIVED").log(Level.INFO, extras.toString());
                //showToast(extras.getString("message"));
                String message=extras.getString("message");

                String accessRequest=extras.getString("accessRequest");
                String shareCurrentLocation=extras.getString("shareCurrentLocation");

                if(accessRequest!=null)
                {
                    sendNotification(accessRequest," wants to see your location !");
                }
                else if(message!=null)
                {
                    sendNotification(message," sharing location with you. Tap to track !");
                }
                else if(shareCurrentLocation!=null)
                {
                    sendNotification(shareCurrentLocation," requesting you to share your current location !");
                }
            }
        }
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    protected void showToast(final String message) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void sendNotification(String msg,String appendingString) {
        Log.d(TAG, "Preparing to send notification...: " + msg);
        mNotificationManager = (NotificationManager) this
                .getSystemService(Context.NOTIFICATION_SERVICE);
        int NOTIFICATION_ID = random.nextInt(100);
        Intent intent=null;
        if(appendingString.contains("wants to see your location"))
        {
           intent = new Intent(this, FriendList.class);
        }
        else if(appendingString.contains("requesting you to share your current location"))
        {
            intent = new Intent(this, MapsActivity.class);
        }
        else {
            intent = new Intent(this, MainActivity.class);
            intent.putExtra("friendNumber", msg);
            intent.putExtra("myNumber", Utils.myNumber(getApplicationContext()));
        }
        PendingIntent contentIntent = PendingIntent.getActivity(this, NOTIFICATION_ID,
               intent, PendingIntent.FLAG_UPDATE_CURRENT);
        String contactName=Utils.getNameFromContact(getApplicationContext(),msg);
        String displayText=null;
        if(contactName!=null)
        {
         displayText= contactName+ appendingString;
        }
        else
        {
            displayText= msg + appendingString;
        }
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                this).setSmallIcon(R.drawable.launcher)
                .setContentTitle("TrackOn Notification")
                .setStyle(new NotificationCompat.BigTextStyle().bigText(displayText))
                .setContentText(displayText);

        mBuilder.setContentIntent(contentIntent);
        mBuilder.setDefaults(Notification.DEFAULT_SOUND);
        mBuilder.setAutoCancel(true);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());

        Log.d(TAG, "Notification sent successfully.");
    }
}
