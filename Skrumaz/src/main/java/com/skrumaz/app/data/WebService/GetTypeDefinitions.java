package com.skrumaz.app.data.WebService;

import android.content.Context;
import android.util.Log;

import com.skrumaz.app.classes.TypeDefinition;
import com.skrumaz.app.data.Preferences;
import com.skrumaz.app.data.Store.TypeDefinitions;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Paito Anderson on 1/26/2014.
 */
public class GetTypeDefinitions {

    public void FetchItems(Context context) {

        List<TypeDefinition> typeDefinitions = new ArrayList<TypeDefinition>();

        // Setup HTTP Request
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpGet get = new HttpGet("https://rally1.rallydev.com/slm/webservice/v2.0/typedefinition?fetch=ObjectID,ElementName&pagesize=200");
        Log.d("GetTypeDefinitions", "https://rally1.rallydev.com/slm/webservice/v2.0/typedefinition?fetch=ObjectID,ElementName&pagesize=200&pretty=true");

        // Setup HTTP Headers / Authorization
        get.setHeader("Accept", "application/json");
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

                // Get array of User Stories in Iteration for this user
                JSONArray typeDefsArray = new JSONObject(responseStrBuilder.toString()).getJSONObject("QueryResult").getJSONArray("Results");

                // Iterate though User Stories
                for (int i = 0; i < typeDefsArray.length(); i++) {
                    TypeDefinition typeDefinition = new TypeDefinition();
                    typeDefinition.setOid(typeDefsArray.getJSONObject(i).getLong("ObjectID"));
                    typeDefinition.setElementName(typeDefsArray.getJSONObject(i).getString("ElementName"));
                    typeDefinitions.add(typeDefinition);
                }

                // Store items in Database
                TypeDefinitions db = new TypeDefinitions(context);
                db.storeDefinitions(typeDefinitions);
                db.close();
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
