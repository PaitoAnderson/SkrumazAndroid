package com.skrumaz.app.data.WebService;

import android.content.Context;
import android.util.Log;

import com.skrumaz.app.MainActivity;
import com.skrumaz.app.classes.Project;
import com.skrumaz.app.classes.Workspace;
import com.skrumaz.app.data.Preferences;
import com.skrumaz.app.data.Store.Projects;
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
 * Created by Paito Anderson on 12/2/2013.
 */
public class GetProjects {

    private List<Project> projects = new ArrayList<>();
    private Workspace workspace = new Workspace();

    public List<Project> FetchItems(Context context) {

        // Set Calling Fragment
        ((MainActivity) context).mFragmentAttached = MainActivity.Fragments.PROJECTS;

        // Get workspaceId to use
        Long workspaceId = Preferences.getWorkspaceId(context, false);

        // Is workspaceId available? If not, get current.
        if (workspaceId > 0) {

            // Set IterationId to use
            workspace.setOid(workspaceId);

            // Get Iterations
            GetWorkspaceProjects(context);

            // Store items in Database
            Projects db = new Projects(context);
            db.storeProjects(projects, workspace);
            db.close();

        } else {

            // Update Main View with status
            ((MainActivity)context).SetProgress("Getting Current Workspace...");

            // Setup HTTP Request
            DefaultHttpClient httpClient = new DefaultHttpClient();

            HttpGet get = new HttpGet("https://rally1.rallydev.com/slm/webservice/v2.0/workspace?fetch=ObjectID");

                 Log.d("GetProjects", "https://rally1.rallydev.com/slm/webservice/v2.0/workspace?fetch=ObjectID&pretty=true");

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
                    JSONObject projectArray = new JSONObject(responseStrBuilder.toString()).getJSONObject("QueryResult").getJSONArray("Results").getJSONObject(0);

                    workspace.setOid(projectArray.getLong("ObjectID"));
                    workspace.setName(projectArray.getString("_refObjectName"));

                    // Get Projects
                    GetWorkspaceProjects(context);

                    // Store items in Database
                    Projects db = new Projects(context);
                    db.storeProjects(projects, workspace);
                    db.close();

                    // Set Workspace to use
                    Preferences.setWorkspaceId(context, workspace.getOid());

                } else {
                    ((MainActivity)context).SetError(statusLine.getReasonPhrase());
                    Log.d("GetProjects", "Workspace Error: " + statusLine.getReasonPhrase());
                }

            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }

        return projects;
    }

    // Get Projects from WebService
    private void GetWorkspaceProjects(Context context) {

        // Update Main View with status
        ((MainActivity) context).SetProgress("Getting Projects...");

        int startIndex = 1;
        int pageSize = 200;
        int currentIndex = pageSize;
        boolean continueRequests = true;

        // Loop until we have all the Projects
        while (continueRequests) {

            // Set to not loop
            continueRequests = false;

            // Setup HTTP Request
            DefaultHttpClient httpClient = new DefaultHttpClient();

            HttpGet get = new HttpGet("https://rally1.rallydev.com/slm/webservice/v2.0/workspace/" + workspace.getOid() + "/Projects?fetch=Name,ObjectID,State&start=" + startIndex + "&pagesize=" + pageSize + "&order=Name");
                 Log.d("GetProjects", "https://rally1.rallydev.com/slm/webservice/v2.0/workspace/" + workspace.getOid() + "/Projects?fetch=Name,ObjectID,State&start=" + startIndex + "&pagesize=" + pageSize + "&order=Name&pretty=true");

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

                    // Get total number of projects
                    int totalResultCount = new JSONObject(responseStrBuilder.toString()).getJSONObject("QueryResult").getInt("TotalResultCount");
                    if (totalResultCount > currentIndex) {
                        startIndex = startIndex + pageSize;
                        currentIndex = currentIndex + pageSize;
                        continueRequests = true;
                    }

                    // Get array of Projects for this Workspace
                    JSONArray projectArray = new JSONObject(responseStrBuilder.toString()).getJSONObject("QueryResult").getJSONArray("Results");

                    // Iterate though Projects
                    for (int i = 0; i < projectArray.length(); i++) {
                        Project project = new Project();
                        project.setOid(projectArray.getJSONObject(i).getLong("ObjectID"));
                        project.setName(projectArray.getJSONObject(i).getString("Name"));

                        // Add iteration to list
                        projects.add(project);
                    }

                } else {
                    ((MainActivity) context).SetError(statusLine.getReasonPhrase());
                    Log.d("GetProjects", "Project Error: " + statusLine.getReasonPhrase());
                }

            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
