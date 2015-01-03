package com.skrumaz.app.data.WebService;

import android.content.Context;
import android.util.Log;

import com.skrumaz.app.MainActivity;
import com.skrumaz.app.classes.Artifact;
import com.skrumaz.app.classes.Iteration;
import com.skrumaz.app.classes.Task;
import com.skrumaz.app.data.Preferences;
import com.skrumaz.app.data.Store.Artifacts;
import com.skrumaz.app.utils.ArtifactStatusLookup;
import com.skrumaz.app.utils.ClientInfo;
import com.skrumaz.app.utils.IterationStatusLookup;

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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Paito Anderson on 10/19/2013.
 */
public class GetArtifacts {

    private List<Artifact> artifacts = new ArrayList<>();
    private Iteration iteration = new Iteration();

    public List<Artifact> FetchItems(Context context) {

        // Set Calling Fragment
        ((MainActivity) context).mFragmentAttached = MainActivity.Fragments.TASKS;

        // Get IterationId to use
        Long IterationId = Preferences.getIterationId(context, false);

        // Is IterationId available? If not, get current.
        if (IterationId > 0) {

            // Set IterationId to use
            iteration.setOid(IterationId);

            // Get US/DE's
            GetAllArtifacts(context);

            // Store items in Database
            Artifacts db = new Artifacts(context);
            db.storeArtifacts(artifacts, iteration);
            db.close();
        } else {

            // Update Main View with status
            ((MainActivity)context).SetProgress("Getting Current Iteration...");

            // Setup HTTP Request
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpGet get = new HttpGet("https://rally1.rallydev.com/slm/webservice/v2.0/iteration:current?fetch=Workspace,Project,Iteration,ObjectID,Name,State");

                Log.d("GetArtifacts", "https://rally1.rallydev.com/slm/webservice/v2.0/iteration:current?fetch=Workspace,Project,Iteration,ObjectID,Name,State&pretty=true");

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
                    JSONObject jsonIteration = new JSONObject(responseStrBuilder.toString());

                    // Get Iteration Name and Reference
                    iteration.setOid(Long.parseLong(jsonIteration.getJSONObject("Iteration").getString("ObjectID")));
                    iteration.setName(jsonIteration.getJSONObject("Iteration").getString("Name"));
                    iteration.setIterationStatus(IterationStatusLookup.stringToIterationStatus(jsonIteration.getJSONObject("Iteration").getString("State")));

                    Log.i("GetArtifacts", "Iteration: " + iteration.getName());

                    // Get US/DE's
                    GetAllArtifacts(context);

                    // Store items in Database
                    Artifacts db = new Artifacts(context);
                    db.storeArtifacts(artifacts, iteration);
                    db.close();

                    // Set Iteration, Project, Workspace to use for current iteration
                    Preferences.setIterationId(context, iteration.getOid());
                    Preferences.setProjectId(context, jsonIteration.getJSONObject("Iteration").getJSONObject("Project").getLong("ObjectID"));
                    Preferences.setWorkspaceId(context, jsonIteration.getJSONObject("Iteration").getJSONObject("Workspace").getLong("ObjectID"));

                } else {
                    ((MainActivity) context).SetError(statusLine.getReasonPhrase());
                    Log.e("GetArtifacts", "GI Error: " + statusLine.getReasonPhrase());
                }

            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }

        return artifacts;
    }

    public void GetAllArtifacts(Context context) {

        ((MainActivity)context).SetProgress("Getting Artifacts...");

        // Setup HTTP Request
        DefaultHttpClient httpClient = new DefaultHttpClient();
        String whereQuery = "(Iteration.Oid%20=%20%22" + iteration.getOid() + "%22)";

        // Get Backlog Items
        if (iteration.getOid() == Long.MAX_VALUE) {
            whereQuery = "(Iteration.Name%20=%20%22%22)";
        }

        if (!Preferences.showAllOwners(context)) {
            whereQuery = "(" + whereQuery + "%20and%20(Owner.Name%20=%20%22" + Preferences.getUsername(context) + "%22))";
        }

        HttpGet get = new HttpGet("https://rally1.rallydev.com/slm/webservice/v2.0/artifact?query=" + whereQuery + "&pagesize=100&fetch=Tasks:summary[FormattedID;Name],Rank,FormattedID,Blocked,ScheduleState,LastUpdateDate,Owner,Description&types=hierarchicalrequirement,defect");

        Log.d("GetArtifacts","https://rally1.rallydev.com/slm/webservice/v2.0/artifact?query=" + whereQuery + "&pagesize=100&fetch=Tasks:summary[FormattedID;Name],Rank,FormattedID,Blocked,ScheduleState,LastUpdateDate,Owner,Description&types=hierarchicalrequirement,defect&pretty=true");

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

                // Get array of User Stories in Iteration for this user
                JSONArray artifactArray = new JSONObject(responseStrBuilder.toString()).getJSONObject("QueryResult").getJSONArray("Results");

                // Iterate though User Stories
                for (int i = 0; i < artifactArray.length(); i++) {

                    // Create an expandable list item for each user story
                    Artifact artifact = new Artifact(artifactArray.getJSONObject(i).getString("_refObjectName"));
                    artifact.setRank(artifactArray.getJSONObject(i).getString("DragAndDropRank"));
                    artifact.setFormattedID(artifactArray.getJSONObject(i).getString("FormattedID"));
                    artifact.setBlocked(artifactArray.getJSONObject(i).getBoolean("Blocked"));
                    artifact.setStatus(ArtifactStatusLookup.stringToStatus(artifactArray.getJSONObject(i).getString("ScheduleState")));
                    artifact.setLastUpdate(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").parse(artifactArray.getJSONObject(i).getString("LastUpdateDate")));
                    artifact.setDescription(artifactArray.getJSONObject(i).getString("Description"));

                    // Iterate though Tasks for User Story
                    for (int j = 0; j < artifactArray.getJSONObject(i).getJSONObject("Summary").getJSONObject("Tasks").getInt("Count"); j++)
                    {
                        Task task = new Task(artifactArray.getJSONObject(i).getJSONObject("Summary").getJSONObject("Tasks").getJSONObject("Name").names().getString(j));
                        task.setFormattedID(artifactArray.getJSONObject(i).getJSONObject("Summary").getJSONObject("Tasks").getJSONObject("FormattedID").names().getString(j));

                        // Add Tasks as children
                        artifact.addTask(task);
                    }

                    // Update story with Owner name
                    try {
                        artifact.setOwnerName(artifactArray.getJSONObject(i).getJSONObject("Owner").getString("_refObjectName"));
                    } catch(JSONException ex) {
                        artifact.setOwnerName("No Owner");
                    }

                    artifacts.add(artifact);
                }

            } else {
                ((MainActivity)context).SetError(statusLine.getReasonPhrase());
                Log.d("GetArtifacts", "Artifact Error: " + statusLine.getReasonPhrase());
            }

        } catch (IOException | JSONException | ParseException e) {
            e.printStackTrace();
        }
    }
}
