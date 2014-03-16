package com.skrumaz.app.data.WebService;

import android.content.Context;
import android.util.Log;

import com.skrumaz.app.classes.Workspace;
import com.skrumaz.app.data.Preferences;
import com.skrumaz.app.data.Store.Workspaces;

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
 * Created by Paito Anderson on 2014-03-15.
 */
public class GetWorkspaces {

    private List<Workspace> workspaces = new ArrayList<Workspace>();

    public List<Workspace> FetchItems(Context context) {

        // Setup HTTP Request
        DefaultHttpClient httpClient = new DefaultHttpClient();

        HttpGet get = new HttpGet("https://rally1.rallydev.com/slm/webservice/v2.0/workspace?fetch=ObjectID&pagesize=100");

        Log.d("GetWorkspaces", "https://rally1.rallydev.com/slm/webservice/v2.0/workspace?fetch=ObjectID&pagesize=100&pretty=true");

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
                JSONArray workspaceArray = new JSONObject(responseStrBuilder.toString()).getJSONObject("QueryResult").getJSONArray("Results");

                // Iterate though Defects
                for (int i = 0; i < workspaceArray.length(); i++) {

                    Workspace workspace = new Workspace();
                    workspace.setOid(workspaceArray.getJSONObject(i).getLong("ObjectID"));
                    workspace.setName(workspaceArray.getJSONObject(i).getString("_refObjectName"));

                    // Add iteration to list
                    workspaces.add(workspace);
                }

                // Store items in Database
                Workspaces db = new Workspaces(context);
                db.storeWorkspaces(workspaces);
                db.close();

            } else {
                //((IterationList)context).SetError(false, statusLine.getReasonPhrase());
                //Log.d("GetWorkspaces", "Workspace Error: " + statusLine.getReasonPhrase());
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return workspaces;
    }
}
