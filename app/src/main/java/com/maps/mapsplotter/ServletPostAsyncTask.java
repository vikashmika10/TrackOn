package com.maps.mapsplotter;

import android.content.Context;
import android.os.AsyncTask;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by vksingh on 26/12/14.
 */
class ServletPostAsyncTask extends AsyncTask<Pair, Void, String> {
    private Context context;
    HttpClient httpClient = new DefaultHttpClient();
    String latitude;



    @Override
    protected String doInBackground(Pair... params) {
        context = params[0].getContext();
        latitude=params[0].getLatitude();
        String longitude=params[0].getLongitude();


        try {
          if(latitude=="Get") {
              List<NameValuePair> getData = new ArrayList<NameValuePair>(1);
              getData.add(new BasicNameValuePair("friendNumber", params[0].getFriendNumber()));
              getData.add(new BasicNameValuePair("myNumber", params[0].getMyNumber()));

              String paramString = URLEncodedUtils.format(getData, "utf-8");

              System.out.println("http://10.0.2.2.appspot.com/hello?"+paramString);
              HttpGet httpGet = new HttpGet("http://maps-plotter-2014.appspot.com/hello?"+paramString);


           HttpResponse response = null;
           response = httpClient.execute(httpGet);

           return  EntityUtils.toString(response.getEntity());
       }

    else {
           HttpPost httpPost = new HttpPost("http://maps-plotter-2014.appspot.com/hello"); // 10.0.2.2 is http://10.0.2.2.appspot.com/hello's IP address in Android emulator

              System.out.println("inside Post call to server");
           // Add name data to request
           List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
           nameValuePairs.add(new BasicNameValuePair("name",params[0].getMyNumber()));
           nameValuePairs.add(new BasicNameValuePair("friendOneNum",params[0].getFriendOneNum()));
           nameValuePairs.add(new BasicNameValuePair("friendTwoNum",params[0].getFriendTwoNum()));
           nameValuePairs.add(new BasicNameValuePair("friendThreeNum",params[0].getFriendThreeNum()));
           nameValuePairs.add(new BasicNameValuePair("latitude", latitude));
           nameValuePairs.add(new BasicNameValuePair("longitude", longitude));
           httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

           // Execute HTTP Post Request
           HttpResponse response = httpClient.execute(httpPost);
           if (response.getStatusLine().getStatusCode() == 200) {
               return EntityUtils.toString(response.getEntity());
           }

           return "Error: " + response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase();
       }
        } catch (ClientProtocolException e) {
            return e.getMessage();
        } catch (IOException e) {
            return e.getMessage();
        }
    }

   @Override
    protected void onPostExecute(String result) {



    }
}