package com.skrumaz.app.data.WebService;

import android.content.Context;
import android.util.Log;

import com.skrumaz.app.MainActivity;
import com.skrumaz.app.classes.Artifact;
import com.skrumaz.app.classes.Project;
import com.skrumaz.app.classes.Task;
import com.skrumaz.app.data.Preferences;
import com.skrumaz.app.data.Store.Artifacts;
import com.skrumaz.app.utils.ArtifactStatusLookup;
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
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by paito on 14-12-26.
 */
public class GetQueryResults {

    private static final String TAG = "GetQueryResults";

    private List<Artifact> artifacts = new ArrayList<>();
    private Project project = new Project();

    public List<Artifact> FetchItems(Context context, String query) {

        // Get ProjectId to use
        project.setOid(Preferences.getProjectId(context, false));

        // Is ProjectId available? If not, get current.
        if (project.getOid() > 0) {
            GetArtifactResults(context, query);
        } else {
            // Setup HTTP Request
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpGet get = new HttpGet("https://rally1.rallydev.com/slm/webservice/v2.0/iteration:current?fetch=Workspace,Project,ObjectID");

            Log.d("GetIterations", "https://rally1.rallydev.com/slm/webservice/v2.0/iteration:current?fetch=Workspace,Project,ObjectID&pretty=true");

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
                    project.setOid(jsonIteration.getJSONObject("Iteration").getJSONObject("Project").getLong("ObjectID"));
                    project.setName(jsonIteration.getJSONObject("Iteration").getJSONObject("Project").getString("_refObjectName"));

                    // Get Iterations
                    GetArtifactResults(context, query);

                    // Set Project, Workspace to use for current iteration
                    Preferences.setProjectId(context, project.getOid());
                    Preferences.setWorkspaceId(context, jsonIteration.getJSONObject("Iteration").getJSONObject("Workspace").getLong("ObjectID"));

                } else {
                    ((MainActivity)context).SetError(statusLine.getReasonPhrase());
                    Log.e(TAG, "Search Error: " + statusLine.getReasonPhrase());
                }

            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }

        // Store items in Database
        Artifacts db = new Artifacts(context);
        db.storeArtifact(artifacts);
        db.close();

        return artifacts;
    }

    // Get Artifacts from WebService based on Query
    private void GetArtifactResults(Context context, String query) {

        String WebServiceQuery = null;
        try {
            WebServiceQuery = "(((Description%20contains%20%22" + URLEncoder.encode(query, "UTF-8") + "%22)%20OR%20(Name%20contains%20%22" + URLEncoder.encode(query, "UTF-8") + "%22))%20OR%20(Notes%20contains%20%22" + URLEncoder.encode(query, "UTF-8") + "%22))";
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String formattedIdPattern = "(US|DE)\\d+";
        Pattern regPattern = Pattern.compile(formattedIdPattern, Pattern.CASE_INSENSITIVE);
        if (regPattern.matcher(query).find()) {
            WebServiceQuery = "(FormattedID%20=%20%22" + query.toUpperCase() + "%22)";
        }

        // Setup HTTP Request
        DefaultHttpClient httpClient = new DefaultHttpClient();

        HttpGet get = new HttpGet("https://rally1.rallydev.com/slm/webservice/v2.0/artifact?project=https://rally1.rallydev.com/slm/webservice/v2.0/project/" + project.getOid() + "&projectScopeDown=false&projectScopeUp=false&query=" + WebServiceQuery + "&fetch=Tasks:summary[FormattedID;Name],Rank,FormattedID,Blocked,ScheduleState,LastUpdateDate,Owner,Description&types=hierarchicalrequirement,defect");

          Log.d("GetQueryResults","https://rally1.rallydev.com/slm/webservice/v2.0/artifact?project=https://rally1.rallydev.com/slm/webservice/v2.0/project/" + project.getOid() + "&projectScopeDown=false&projectScopeUp=false&query=" + WebServiceQuery + "&fetch=Tasks:summary[FormattedID;Name],Rank,FormattedID,Blocked,ScheduleState,LastUpdateDate,Owner,Description&types=hierarchicalrequirement,defect&pretty=true");

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

                // Get array of Iterations for this Project
                JSONArray artifactArray = new JSONObject(responseStrBuilder.toString()).getJSONObject("QueryResult").getJSONArray("Results");

                // Iterate though Iterations
                for (int i = 0; i < artifactArray.length(); i++) {

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

                    // Update with Owner name
                    try {
                        artifact.setOwnerName(artifactArray.getJSONObject(i).getJSONObject("Owner").getString("_refObjectName"));
                    } catch(JSONException ex) {
                        artifact.setOwnerName("No Owner");
                    }

                    // Add iteration to list
                    artifacts.add(artifact);
                }

            } else {
                Log.d(TAG, "Search Error: " + statusLine.getReasonPhrase());
            }

        } catch (IOException| JSONException | ParseException e) {
            e.printStackTrace();
        }
    }
}
