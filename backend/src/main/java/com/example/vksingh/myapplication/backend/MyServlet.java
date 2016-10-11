/*
   For step-by-step instructions on connecting your Android application to this backend module,
   see "App Engine Java Servlet Module" template documentation at
   https://github.com/GoogleCloudPlatform/gradle-appengine-templates/tree/master/HelloWorld
*/

package com.example.vksingh.myapplication.backend;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Sender;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class MyServlet extends HttpServlet {

    private static final String GOOGLE_SERVER_KEY = "AIzaSyCXAvBwgYDM_rOUjEsiV9ivEL5mkC21SVk";

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException{

            String getData=req.getParameter("friendNumber");
            String myNumber=req.getParameter("myNumber");
            DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();

           Key key= KeyFactory.createKey("User", Long.valueOf(getData));
           Entity e2= null;
           try {
            e2 = datastoreService.get(key);

            JSONObject object=null;
            if(e2!=null )
            {
                String f1= String.valueOf(e2.getProperties().get("Friend1"));
                String f2=String.valueOf(e2.getProperties().get("Friend2"));
                String f3= String.valueOf(e2.getProperties().get("Friend3"));

             if(myNumber.equals(f1) || myNumber.equals(f2) || myNumber.equals(f3)) {
                 object = new JSONObject();
                 object.put("lat", e2.getProperties().get("latitude"));
                 object.put("lon", e2.getProperties().get("longitude"));
                 object.put("time",e2.getProperties().get("LastUpdated"));
                 resp.getWriter().write(object.toString());
             }
             else
             {
                 resp.getWriter().write("access denied");
             }
            }
        } catch (EntityNotFoundException e) {
            resp.getWriter().write("data doesn't exists");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        Entity entity=null;
        DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();
        if(req.getParameter("regId")!=null)
        {
            String number=req.getParameter("myNumber");
            String regId=req.getParameter("regId");
            entity=new Entity("RegistrationId",Long.valueOf(number));
            entity.setProperty("regId",regId);
            Calendar calendar=Calendar.getInstance();
            entity.setProperty("LastUpdated",calendar.getTimeInMillis());
            datastoreService.put(entity);
        }
        else if(req.getParameter("notificationFlag")!=null)
        {
            String number = req.getParameter("name");
            String first = req.getParameter("friendOneNum");
            String second = req.getParameter("friendTwoNum");
            String third = req.getParameter("friendThreeNum");
            if(req.getParameter("updateFriendList")!=null)
            {
                try {
                    entity = datastoreService.get(KeyFactory.createKey("User", Long.valueOf(number)));
                } catch (EntityNotFoundException e) {
                    e.printStackTrace();
                }
                if(entity!=null)
                {
                  setValues(entity,first,second,third);
                  datastoreService.put(entity);
                }

            }
           else {

                if (first != null) {
                    if (req.getParameter("notificationFlag").equalsIgnoreCase("accessRequest")) {
                        sendNotification(first, number, "accessRequest");
                    }
                    else if(req.getParameter("notificationFlag").equalsIgnoreCase("shareCurrentLocation"))
                    {
                        sendNotification(first, number, "shareCurrentLocation");
                    }
                    else {
                        sendNotification(first, number, "message");
                    }
                }
                if (!req.getParameter("notificationFlag").equalsIgnoreCase("accessRequest")) {
                    if (second != null) {
                        sendNotification(second, number, "message");
                    }
                    if (third != null) {
                        sendNotification(third, number, "message");
                    }
                }
            }
        }
        else {
            String latitude = req.getParameter("latitude");
            String longitude = req.getParameter("longitude");
            String number = req.getParameter("name");
            String first = req.getParameter("friendOneNum");
            String second = req.getParameter("friendTwoNum");
            String third = req.getParameter("friendThreeNum");

            entity = new Entity("User", Long.valueOf(number));
            entity.setProperty("latitude", latitude);
            entity.setProperty("longitude", longitude);
            setValues(entity, first, second, third);
            Calendar calendar=Calendar.getInstance();
            entity.setProperty("LastUpdated",calendar.getTimeInMillis());
            datastoreService.put(entity);
        }
    }

    public void sendNotification(String member,String senderNumber,String messageKey) throws IOException {
        Key key= KeyFactory.createKey("RegistrationId", Long.valueOf(member));
        DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();
        Entity e2 = null;
        try {
            e2 = datastoreService.get(key);
        } catch (EntityNotFoundException e) {
            e.printStackTrace();
        }
        if(e2!=null) {
            Sender sender = new Sender(GOOGLE_SERVER_KEY);
            Message message = new Message.Builder().addData(messageKey, senderNumber).build();
            sender.send(message, e2.getProperties().get("regId").toString(), 1);
        }
    }

    public void setValues(Entity entity,String first,String second,String third)
    {
        if (first == null) {
            entity.setProperty("Friend1", 1234L);
        } else {
            entity.setProperty("Friend1", Long.valueOf(first));
        }
        if (second == null) {
            entity.setProperty("Friend2", 1234L);
        } else {
            entity.setProperty("Friend2", Long.valueOf(second));
        }
        if (third == null) {
            entity.setProperty("Friend3", 12345L);
        } else {
            entity.setProperty("Friend3", Long.valueOf(third));
        }
    }
}
