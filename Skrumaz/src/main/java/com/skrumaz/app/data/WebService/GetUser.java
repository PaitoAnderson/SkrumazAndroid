package com.skrumaz.app.data.WebService;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.skrumaz.app.classes.User;
import com.skrumaz.app.data.Preferences;
import com.skrumaz.app.data.Store.Users;
import com.skrumaz.app.utils.ClientInfo;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

/**
 * Created by Paito Anderson on 2014-03-22.
 */
public class GetUser {

    public User FetchUser(Context context) {
        User user = null;

        // Setup HTTP Request
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpGet get = new HttpGet("https://rally1.rallydev.com/slm/webservice/v2.0/user?fetch=ObjectID,DisplayName,EmailAddress");
                 Log.d("GetUser", "https://rally1.rallydev.com/slm/webservice/v2.0/user?fetch=ObjectID,DisplayName,EmailAddress&pretty=true");

        // Setup HTTP Headers / Authorization
        get.setHeader("Accept", "application/json");
        get = ClientInfo.addHttpGetHeaders(get);
        get.setHeader("Authorization", Preferences.getCredentials(context));
        try {

            // Make HTTP Request
            HttpResponse response = httpClient.execute(get);
            StatusLine statusLine = response.getStatusLine();
            if (statusLine.getStatusCode() == HttpURLConnection.HTTP_OK) {

                // Parse JSON Response
                BufferedReader streamReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
                StringBuilder responseStrBuilder = new StringBuilder();
                String inputStr;
                while ((inputStr = streamReader.readLine()) != null) {
                    responseStrBuilder.append(inputStr);
                }

                // Get Object for this user
                JSONObject userObject = new JSONObject(responseStrBuilder.toString()).getJSONObject("User");
                user = new User(userObject.getString("DisplayName"));
                user.setOid(userObject.getLong("ObjectID"));
                user.setEmail(userObject.getString("EmailAddress"));
                user.setPhotoBitmap(FetchUserProfile(context, user.getOid()));

                // Store user in Database
                Users db = new Users(context);
                db.storeUser(user);
                db.close();
            }

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        return user;
    }

    private Bitmap FetchUserProfile(Context context, Long userId)
    {
        Bitmap profileImage = null;

        // Setup HTTP Request
        GetCookie getCookie = new GetCookie();
        DefaultHttpClient httpClient = getCookie.FetchHttpClient(context);
        HttpGet get = new HttpGet("https://rally1.rallydev.com/slm/profile/viewThumbnailImage.sp?tSize=150&uid=" + userId);
                 Log.d("GetUser", "https://rally1.rallydev.com/slm/profile/viewThumbnailImage.sp?tSize=150&uid=" + userId);

        // Setup HTTP Headers / Authorization
        get.setHeader("Accept", "application/json");
        get = ClientInfo.addHttpGetHeaders(get);
        get.setHeader("Authorization", Preferences.getCredentials(context));
        try {
            // Make HTTP Request
            HttpResponse response = httpClient.execute(get);
            StatusLine statusLine = response.getStatusLine();
            if (statusLine.getStatusCode() == HttpURLConnection.HTTP_OK) {
                BufferedHttpEntity bufferedHttpEntity = new BufferedHttpEntity(response.getEntity());
                InputStream imageStream = bufferedHttpEntity.getContent();
                profileImage = BitmapFactory.decodeStream(imageStream);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return profileImage;
    }
}
