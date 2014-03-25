package com.skrumaz.app.data.WebService;

import android.content.Context;
import android.util.Log;

import com.skrumaz.app.data.Preferences;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Paito Anderson on 2014-03-24.
 */
public class GetCookie {

    public DefaultHttpClient FetchHttpClient(Context context) {

        DefaultHttpClient defaultHttpClient = new DefaultHttpClient();
        HttpPost post = new HttpPost("https://rally1.rallydev.com/slm/j_spring_security_check");
                  Log.d("GetCookie", "https://rally1.rallydev.com/slm/j_spring_security_check");

        post.setHeader("Accept", "text/html");

        try {
            // Set Form Username / Password
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("j_username", Preferences.getUsername(context)));
            params.add(new BasicNameValuePair("j_password", Preferences.getPassword(context)));
            post.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));

            // Call Login Page
            defaultHttpClient.execute(post);

            // Get rid of content
            post.getEntity().consumeContent();

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return defaultHttpClient;
    }
}
