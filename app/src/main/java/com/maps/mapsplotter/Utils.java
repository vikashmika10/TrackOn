package com.maps.mapsplotter;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.ContactsContract;

import com.maps.database.Contact;
import com.maps.database.DatabaseHandler;

import java.util.Calendar;
import java.util.HashMap;

/**
 * Created by vksingh on 04/02/15.
 */
public class Utils {

    private static final String PREF_NAME = "MapsPlotter";
    static SharedPreferences pref;


    public static String removeSpecialCharFromString(String number)
    {
        StringBuilder stringBuilder=new StringBuilder();
        for(int j=0;j<number.length();j++)
        {
            if(number.charAt(j)!=' ' && number.charAt(j)!='-')
            {
                stringBuilder=stringBuilder.append(number.charAt(j));
            }
        }
        if(stringBuilder.length()>10)
        {
            number=stringBuilder.substring((stringBuilder.length()-10),stringBuilder.length());
        }
        else
        {
            number=stringBuilder.toString();
        }
        return number;
    }

    public static String myNumber(Context context)
    {
        pref = context.getSharedPreferences(PREF_NAME, 0);
        return pref.getString("number","notFound");
    }

    public static String getNameFromContact(Context context,String number)
    {
        Cursor phones=null;
        try {
            String sortOrder = ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC";
            phones = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, sortOrder);
            while (phones.moveToNext()) {
                String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String phoneNumber =removeSpecialCharFromString(phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                if (phoneNumber.contains(number)) {
                    System.out.println(name);
                    return name;
                }
            }
        }
        finally {
            if(phones!=null) {
                phones.close();
            }
        }
        return null;
    }

    public static Bitmap getPhoto(Context context,String number) {

        Cursor phones=null;
        try {
            String sortOrder = ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC";
            phones = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, sortOrder);
            while (phones.moveToNext()) {
                Bitmap  photo=null;
                String phoneNumber =removeSpecialCharFromString(phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                if (phoneNumber.contains(number)) {

                    int  imageID = phones.getInt(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_ID));
                    final byte[] ImageBytes = phones.getBlob(0);
                    if (ImageBytes != null) {
                     photo = BitmapFactory.decodeByteArray(ImageBytes, 0, ImageBytes.length);
                    }
                }
                return photo;
            }

            return BitmapFactory.decodeResource(context.getResources(),R.drawable.ic_action_person);
        }
        finally {
            if(phones!=null) {
                phones.close();
            }
        }
//        return null;
//        int idDisplayName = phoneCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
//
//        String name = phoneCursor.getString(idDisplayName);
//        int imageID = phoneCursor.getInt(phoneCursor.getColumnIndex(ContactsContract.Contacts.PHOTO_ID));
//        final Uri uri = ContentUris.withAppendedId(ContactsContract.Data.CONTENT_URI, imageID);
//        final Cursor cursor = getContentResolver().query(uri, PHOTO_BITMAP_PROJECTION, null, null, null);
//        try {
//            Bitmap photo = null;
//            if (cursor.moveToFirst()) {
//                final byte[] ImageBytes = cursor.getBlob(0);
//                if (ImageBytes != null) {
//                    photo = BitmapFactory.decodeByteArray(ImageBytes, 0, ImageBytes.length);
//                }
//            }
//
//            return photo;
//        }
//        finally {
//            cursor.close();
//        }

    }

    public static String getLastUpdatedTime(long oldTime)
    {
        Calendar c2=Calendar.getInstance();
        long currentTime=c2.getTimeInMillis();
        long diff=currentTime-oldTime;
        System.out.println("Time Diff : " +diff);
        long diffMinutes = diff / (60 * 1000) % 60;
        long diffHours = diff / (60 * 60 * 1000);
        int diffInDays = (int) ((diff) / (1000 * 60 * 60 * 24));
        if (diffInDays == 1) {
          return diffInDays +" day ago";
        }
        else if (diffInDays > 1) {
            return diffInDays +" days ago";

        }
        else if (diffHours ==1) {
            return diffHours + " hr ago";

        }
        else if (diffHours >1 && diffHours<24) {
            return diffHours + " hrs ago";

        }
        else if (diffMinutes == 1) {
            return diffMinutes +" min ago";
        }
        else if (diffMinutes >1) {
             return diffMinutes +" mins ago";
        }
        else
        {
            return "Just Now";
        }
    }

    public static String multiLetter(String text)
    {
        try {
            String arr[] = text.split(" ");
            StringBuilder sb = new StringBuilder();
            sb.append(arr[0].substring(0, 1).toUpperCase());
            if (arr.length > 1) {
                sb.append(arr[1].substring(0, 1).toUpperCase());
            }
            return sb.toString();
        }
        catch(StringIndexOutOfBoundsException e)
        {
           e.getMessage();
            return "?";
        }

    }

    public static HashMap<String, String>  readFriendNumberFromDB(Context context) {
        HashMap<String, String> friendNumMap = new HashMap<>();
        DatabaseHandler db = new DatabaseHandler(context);
        try {
            int totalFriends = db.getContactsCount();
            for (int i = 1; i <= totalFriends; i++) {
                Contact cn = db.getContact(i);
                friendNumMap.put(String.valueOf(i), Utils.removeSpecialCharFromString(cn.getPhoneNumber()));

            }
            return friendNumMap;
        } finally {
            db.close();
        }
    }

    public static Pair updateFriendListInServer(Context context)
    {
        HashMap<String,String> friendNumMap=Utils.readFriendNumberFromDB(context);
        Pair pair = new Pair();
        pair.setContext(context);
        pair.setMyNumber(Utils.myNumber(context));
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
        return pair;

    }

    public static boolean isInternetConnected(Context context)
    {
       ConnectionDetector cd = new ConnectionDetector(context);
        // Check if Internet present
        if (!cd.isConnectingToInternet()) {
            return false;
        }
        return true;
    }

}
