package com.maps.mapsplotter;

import android.content.Context;

/**
 * Created by vksingh on 26/12/14.
 */
public class Pair {
    Context context;
    String latitude;
    String longitude;
    String myNumber;
    String friendOneNum;
    String friendTwoNum;
    String friendThreeNum;
    String friendNumber;

    public String getFriendOneNum() {
        return friendOneNum;
    }

    public void setFriendOneNum(String friendOneNum) {
        this.friendOneNum = friendOneNum;
    }

    public String getFriendTwoNum() {
        return friendTwoNum;
    }

    public void setFriendTwoNum(String friendTwoNum) {
        this.friendTwoNum = friendTwoNum;
    }

    public String getFriendThreeNum() {
        return friendThreeNum;
    }

    public void setFriendThreeNum(String friendThreeNum) {
        this.friendThreeNum = friendThreeNum;
    }

    public String getMyNumber() {
        return myNumber;
    }

    public void setMyNumber(String myNumber) {
        this.myNumber = myNumber;
    }

    public String getFriendNumber() {
        return friendNumber;
    }

    public void setFriendNumber(String friendNumber) {
        this.friendNumber = friendNumber;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }
}
