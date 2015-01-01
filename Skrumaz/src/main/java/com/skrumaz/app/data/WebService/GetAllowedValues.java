package com.skrumaz.app.data.WebService;

import android.content.Context;
import android.util.Log;

import com.skrumaz.app.classes.AllowedValue;
import com.skrumaz.app.data.Preferences;
import com.skrumaz.app.utils.ClientInfo;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Paito Anderson on 2/3/2014.
 */
public class GetAllowedValues {

    private List<AllowedValue> allowedValues = new ArrayList<>();

    public List<AllowedValue> FetchItems(Context context, Long fieldId) {

        // Setup HTTP Request
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpGet get = new HttpGet("https://rally1.rallydev.com/slm/webservice/v2.0/AttributeDefinition/" + fieldId + "/AllowedValues");
        Log.d("GetAllowedValues", "https://rally1.rallydev.com/slm/webservice/v2.0/AttributeDefinition/" + fieldId + "/AllowedValues?pretty=true");

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

                // Get array of Form Attributes for this Definition for this Project
                JSONArray allowedValueArray = new JSONObject(responseStrBuilder.toString()).getJSONObject("QueryResult").getJSONArray("Results");

                // Iterate though Form Attributes
                for (int i = 0; i < allowedValueArray.length(); i++) {
                    AllowedValue allowedValue = new AllowedValue();
                    if (!allowedValueArray.getJSONObject(i).getString("ObjectID").equals("null"))
                    {
                        allowedValue.setOid(allowedValueArray.getJSONObject(i).getLong("ObjectID"));
                    }
                    allowedValue.setName(allowedValueArray.getJSONObject(i).getString("StringValue"));

                    if (allowedValue.getName().length() > 0)
                    {
                        allowedValues.add(allowedValue);
                    }
                }
            }

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        return allowedValues;
    }
}
