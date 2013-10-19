package com.skrumaz.app.data.PivotalTracker;

import android.content.Context;
import android.util.Log;

import com.skrumaz.app.MainActivity;
import com.skrumaz.app.classes.Artifact;
import com.skrumaz.app.classes.Service;
import com.skrumaz.app.classes.Task;
import com.skrumaz.app.data.Preferences;
import com.skrumaz.app.data.Store;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Paito Anderson on 10/19/2013.
 */
public class GetItems {

    List<Artifact> artifacts = new ArrayList<Artifact>();

    public List<Artifact> FetchItems(Context context) {

        // Setup HTTP Request
        DefaultHttpClient httpClient = new DefaultHttpClient();

        if (Preferences.showAllOwners(context)) {
            // TODO: Implement This
        }

        HttpGet get = new HttpGet("https://www.pivotaltracker.com/services/v5/my/work?fields=tasks,current_state,name,updated_at");

                Log.d("GetItems", "https://www.pivotaltracker.com/services/v5/my/work?fields=tasks,current_state,name,updated_at");

        // Setup HTTP Headers / Authorization
        get.setHeader("Accept", "application/json");
        get.setHeader("X-TrackerToken", Preferences.getCredentials(context, Service.PIVOTAL_TRACKER));
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
                JSONArray myWork = new JSONArray(responseStrBuilder.toString());

                for (int i = 0; i < myWork.length(); i++) {

                    Artifact artifact = new Artifact(myWork.getJSONObject(i).getString("name"));
                    artifact.setFormattedID(myWork.getJSONObject(i).getString("id"));
                    artifact.setLastUpdate(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").parse(myWork.getJSONObject(i).getString("updated_at")));
                    artifact.setBlocked(false); // Support coming end of year
                    artifact.setStatus(com.skrumaz.app.classes.Status.DEFINED); //TODO: FIX THIS!
                    artifact.setRank(Integer.toString(i)); // Downloaded by Rank

                    //Log.d("MainActivity", "Set: " + artifact.getName() + " Rank: " + Integer.toString(i));

                    // Iterate though Tasks for User Story
                    JSONArray tasks = new JSONArray(myWork.getJSONObject(i).getString("tasks"));
                    for (int t = 0; t < tasks.length(); t++)
                    {
                        Task task = new Task(tasks.getJSONObject(t).getString("description"));
                        task.setFormattedID(tasks.getJSONObject(t).getString("id"));

                        // Add Tasks as children
                        artifact.addTask(task);
                    }

                    artifacts.add(artifact);
                }

                // Store artifacts in Database
                Store db = new Store(context);
                db.storeArtifacts(artifacts);
                db.close();

                // Set data expiry date
                Preferences.setDataExpiry(context);

            } else {
                ((MainActivity)context).SetError(false, statusLine.getReasonPhrase());
                Log.e("GetItems", "Error: " + statusLine.getReasonPhrase());
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return artifacts;
    }
}
