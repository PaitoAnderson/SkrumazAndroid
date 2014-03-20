package com.skrumaz.app.data.WebService;

import android.content.Context;
import android.util.Log;

import com.skrumaz.app.classes.CreateAuthorization;
import com.skrumaz.app.classes.CreateResult;
import com.skrumaz.app.data.Preferences;
import com.skrumaz.app.utils.ClientInfo;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;

/**
 * Created by Paito Anderson on 2/7/2014.
 */
public class PutDomainObject {

    CreateResult createResult = new CreateResult();
    String objectType;

    public CreateResult PutObject(Context context,  CreateAuthorization createAuthorization, String objectType, JSONObject createObject) {

        this.objectType = objectType;

        // Setup HTTP Request
        HttpClient httpClient = createAuthorization.getHttpClient();
        HttpPut put = new HttpPut("https://rally1.rallydev.com/slm/webservice/v2.0/" + objectType + "/create?key=" + createAuthorization.getSecurityToken());
        Log.d("PutDomainObject", "https://rally1.rallydev.com/slm/webservice/v2.0/" + objectType + "/create?key=" + createAuthorization.getSecurityToken());

        // Setup HTTP Headers / Authorization
        put.setHeader("Accept", "application/json");
        put = ClientInfo.addHttpPutHeaders(put);
        put.setHeader("Authorization", Preferences.getCredentials(context));

        try {
            StringEntity inputEntity;
            inputEntity = new StringEntity(createObject.toString());
            put.setEntity(inputEntity);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        try {

            // Make HTTP Request
            HttpResponse response = httpClient.execute(put);
            StatusLine statusLine = response.getStatusLine();
            if (statusLine.getStatusCode() == HttpURLConnection.HTTP_OK) {

                // Parse JSON Response
                BufferedReader streamReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
                StringBuilder responseStrBuilder = new StringBuilder();
                String inputStr;
                while ((inputStr = streamReader.readLine()) != null) {
                    responseStrBuilder.append(inputStr);
                }

                //Log.i("PutDomainObject", responseStrBuilder.toString());

                // Get array of Errors from this Request
                JSONObject jsonResponse = new JSONObject(responseStrBuilder.toString()).getJSONObject("CreateResult");

                for (int i = 0; i < jsonResponse.getJSONArray("Errors").length(); i++) {
                    createResult.addMessage(stripMessage(jsonResponse.getJSONArray("Errors").getString(i)));
                }

                if (jsonResponse.getJSONArray("Errors").length() == 0) {
                    if (!jsonResponse.isNull("Object")) {
                        if (!jsonResponse.getJSONObject("Object").isNull("FormattedID")) {
                            createResult.addMessage("Created " + jsonResponse.getJSONObject("Object").getString("FormattedID") + "!");
                        } else {
                            createResult.addMessage("Created Successfully!");
                        }
                        createResult.setSuccess(true);

                    }
                }

                // Error Response
                //{
                //    "CreateResult":{
                //        "_rallyAPIMajor":"2",
                //                "_rallyAPIMinor":"0",
                //                "Errors":[
                //        "Validation error: Card.WorkProductName contained invalid input. Search 'HTML Whitelist' at help.rallydev.com for details.",
                //                "Validation error: HierarchicalRequirement.Name contained invalid input. Search 'HTML Whitelist' at help.rallydev.com for details.",
                //                "Validation error: HierarchicalRequirement.MICS Category should not be null"
                //        ],
                //        "Warnings":[]
                //    }
                //}
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return createResult;
    }

    private String stripMessage(String message)
    {
        // Remove Validation error
        message = message.replace("Validation error: ", "");

        // Remove ObjectType
        message = message.replace(objectType + ".", "");

        // Robot to Human Talk
        message = message.replace("should not be null", "is a required field.");
        message = message.replace(" Search 'HTML Whitelist' at help.rallydev.com for details.", "");

        return message;
    }
}
