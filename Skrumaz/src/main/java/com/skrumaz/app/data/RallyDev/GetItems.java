package com.skrumaz.app.data.RallyDev;

import android.content.Context;
import android.util.Log;

import com.skrumaz.app.MainActivity;
import com.skrumaz.app.classes.Artifact;
import com.skrumaz.app.classes.Iteration;
import com.skrumaz.app.classes.Service;
import com.skrumaz.app.classes.Task;
import com.skrumaz.app.data.Preferences;
import com.skrumaz.app.data.Store;
import com.skrumaz.app.utils.StatusLookup;

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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Paito Anderson on 10/19/2013.
 */
public class GetItems {

    List<Artifact> artifacts = new ArrayList<Artifact>();
    private Iteration iteration = new Iteration();

    public List<Artifact> FetchItems(Context context) {

        ((MainActivity)context).SetProgress("Getting Current Iteration...");

        // Setup HTTP Request
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpGet get = new HttpGet("https://rally1.rallydev.com/slm/webservice/v2.0/iteration:current");

        Log.d("GetItems", "https://rally1.rallydev.com/slm/webservice/v2.0/iteration:current");

        // Setup HTTP Headers / Authorization
        get.setHeader("Accept", "application/json");
        get.setHeader("Authorization", Preferences.getCredentials(context, Service.RALLY_DEV));
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
                JSONObject jsonIteration = new JSONObject(responseStrBuilder.toString());

                // Get Iteration Name and Reference
                iteration.setOid(jsonIteration.getJSONObject("Iteration").getString("ObjectID").toString());
                iteration.setName(jsonIteration.getJSONObject("Iteration").getString("Name").toString());

                Log.i("GetItems", "Iteration: " + iteration.getName());

                // Get US/DE's
                GetUserStories(context);
                GetDefects(context);

                // Store items in Database
                Store db = new Store(context);
                db.storeArtifacts(artifacts);
                db.close();

                // Set data expiry date
                Preferences.setDataExpiry(context);

            } else {
                ((MainActivity)context).SetError(false, statusLine.getReasonPhrase());
                Log.e("GetItems", "GI Error: " + statusLine.getReasonPhrase());
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return artifacts;
    }

    public void GetUserStories(Context context) {

        ((MainActivity)context).SetProgress("Getting User Stories...");

        // Setup HTTP Request
        DefaultHttpClient httpClient = new DefaultHttpClient();
        String whereQuery = "((Iteration.Oid%20=%20%22" + iteration.getOid() + "%22)%20and%20(Owner.Name%20=%20%22" + Preferences.getUsername(context) + "%22))";

        if (Preferences.showAllOwners(context)) {
            whereQuery = "(Iteration.Oid%20=%20%22" + iteration.getOid() + "%22)";
        }

        HttpGet get = new HttpGet("https://rally1.rallydev.com/slm/webservice/v2.0/hierarchicalrequirement?query=" + whereQuery + "&pagesize=100&fetch=Tasks:summary[FormattedID;Name],Rank,FormattedID,Blocked,ScheduleState,LastUpdateDate");

                 Log.d("GetItems","https://rally1.rallydev.com/slm/webservice/v2.0/hierarchicalrequirement?query=" + whereQuery + "&pagesize=100&fetch=Tasks:summary[FormattedID;Name],Rank,FormattedID,Blocked,ScheduleState,LastUpdateDate&pretty=true");

        // Setup HTTP Headers / Authorization
        get.setHeader("Accept", "application/json");
        get.setHeader("Authorization", Preferences.getCredentials(context, Service.RALLY_DEV));
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
                JSONArray userStoriesArray = new JSONObject(responseStrBuilder.toString()).getJSONObject("QueryResult").getJSONArray("Results");

                // Iterate though User Stories
                for (int i = 0; i < userStoriesArray.length(); i++) {

                    // Create an expandable list item for each user story
                    Artifact userStory = new Artifact(userStoriesArray.getJSONObject(i).getString("_refObjectName"));
                    userStory.setRank(userStoriesArray.getJSONObject(i).getString("DragAndDropRank"));
                    userStory.setFormattedID(userStoriesArray.getJSONObject(i).getString("FormattedID"));
                    userStory.setBlocked(userStoriesArray.getJSONObject(i).getBoolean("Blocked"));
                    userStory.setStatus(StatusLookup.stringToStatus(userStoriesArray.getJSONObject(i).getString("ScheduleState")));
                    userStory.setLastUpdate(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").parse(userStoriesArray.getJSONObject(i).getString("LastUpdateDate")));

                    // Iterate though Tasks for User Story
                    for (int j = 0; j < userStoriesArray.getJSONObject(i).getJSONObject("Summary").getJSONObject("Tasks").getInt("Count"); j++)
                    {
                        Task task = new Task(userStoriesArray.getJSONObject(i).getJSONObject("Summary").getJSONObject("Tasks").getJSONObject("Name").names().getString(j));
                        task.setFormattedID(userStoriesArray.getJSONObject(i).getJSONObject("Summary").getJSONObject("Tasks").getJSONObject("FormattedID").names().getString(j));

                        // Add Tasks as children
                        userStory.addTask(task);
                    }

                    artifacts.add(userStory);
                }

            } else {
                ((MainActivity)context).SetError(false, statusLine.getReasonPhrase());
                Log.d("GetItems", "US Error: " + statusLine.getReasonPhrase());
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void GetDefects(Context context) {

        ((MainActivity)context).SetProgress("Getting Defects...");

        // Setup HTTP Request
        DefaultHttpClient httpClient = new DefaultHttpClient();
        String whereQuery = "((Iteration.Oid%20=%20%22" + iteration.getOid() + "%22)%20and%20(Owner.Name%20=%20%22" + Preferences.getUsername(context) + "%22))";

        if (Preferences.showAllOwners(context)) {
            whereQuery = "(Iteration.Oid%20=%20%22" + iteration.getOid() + "%22)";
        }

        HttpGet get = new HttpGet("https://rally1.rallydev.com/slm/webservice/v2.0/defects?query=" + whereQuery + "&pagesize=100&fetch=Tasks:summary[FormattedID;Name],Rank,FormattedID,Blocked,ScheduleState,LastUpdateDate");

        Log.d("MainActivity","https://rally1.rallydev.com/slm/webservice/v2.0/defects?query=" + whereQuery + "&pagesize=100&fetch=Tasks:summary[FormattedID;Name],Rank,FormattedID,Blocked,ScheduleState,LastUpdateDate&pretty=true");

        // Setup HTTP Headers / Authorization
        get.setHeader("Accept", "application/json");
        get.setHeader("Authorization", Preferences.getCredentials(context, Service.RALLY_DEV));
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
                JSONArray defectsArray = new JSONObject(responseStrBuilder.toString()).getJSONObject("QueryResult").getJSONArray("Results");

                // Iterate though Defects
                for (int i = 0; i < defectsArray.length(); i++) {

                    // Create an expandable list item for each defect
                    Artifact defect = new Artifact(defectsArray.getJSONObject(i).getString("_refObjectName"));
                    defect.setRank(defectsArray.getJSONObject(i).getString("DragAndDropRank"));
                    defect.setFormattedID(defectsArray.getJSONObject(i).getString("FormattedID"));
                    defect.setBlocked(defectsArray.getJSONObject(i).getBoolean("Blocked"));
                    defect.setStatus(StatusLookup.stringToStatus(defectsArray.getJSONObject(i).getString("ScheduleState")));
                    defect.setLastUpdate(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").parse(defectsArray.getJSONObject(i).getString("LastUpdateDate")));

                    // Iterate though Tasks for Defect
                    for (int j = 0; j < defectsArray.getJSONObject(i).getJSONObject("Summary").getJSONObject("Tasks").getInt("Count"); j++)
                    {
                        Task task = new Task(defectsArray.getJSONObject(i).getJSONObject("Summary").getJSONObject("Tasks").getJSONObject("Name").names().getString(j));
                        task.setFormattedID(defectsArray.getJSONObject(i).getJSONObject("Summary").getJSONObject("Tasks").getJSONObject("FormattedID").names().getString(j));

                        // Add Tasks as children
                        defect.addTask(task);
                    }

                    // Add defect with Tasks to List
                    artifacts.add(defect);
                }

            } else {
                ((MainActivity)context).SetError(false, statusLine.getReasonPhrase());
                Log.d("GetItems", "DE Error: " + statusLine.getReasonPhrase());
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
