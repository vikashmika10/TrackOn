package com.maps.mapsplotter;

import android.content.Context;
import android.os.AsyncTask;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by vksingh on 19/02/15.
 */
public class SendNotificationAsynTask extends AsyncTask<Pair, Void, String> {
    private Context context;

    @Override
    protected String doInBackground(Pair... params) {
        context = params[0].getContext();
        HttpClient httpClient = new DefaultHttpClient();

        HttpPost httpPost = new HttpPost("http://maps-plotter-2014.appspot.com/hello"); // 10.0.2.2 is http://10.0.2.2.appspot.com/hello's IP address in Android emulator

      //  System.out.println("inside Post call to server for notification");
        // Add name data to request
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
        nameValuePairs.add(new BasicNameValuePair("name", params[0].getMyNumber()));
        nameValuePairs.add(new BasicNameValuePair("friendOneNum",params[0].getFriendOneNum()));
        nameValuePairs.add(new BasicNameValuePair("friendTwoNum",params[0].getFriendTwoNum()));
        nameValuePairs.add(new BasicNameValuePair("friendThreeNum",params[0].getFriendThreeNum()));
        nameValuePairs.add(new BasicNameValuePair("notificationFlag", params[0].getFriendTwoNum()));
        if(params[0].getFriendNumber()!=null)
        {
        nameValuePairs.add(new BasicNameValuePair("updateFriendList",params[0].getMyNumber()));
        }

        try {
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        // Execute HTTP Post Request
        HttpResponse response = null;
        try {
            response = httpClient.execute(httpPost);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(response!=null)
        {
        if (response.getStatusLine().getStatusCode() == 200) {
            try {
                return EntityUtils.toString(response.getEntity());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "Error: " + response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase();
        }
        return "errror";
    }

      @Override
      protected void onPostExecute(String result){
        }

} //end of class
