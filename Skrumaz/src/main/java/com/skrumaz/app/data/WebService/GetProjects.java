package com.skrumaz.app.data.WebService;

import android.content.Context;
import android.util.Log;

import com.skrumaz.app.classes.Project;
import com.skrumaz.app.data.Preferences;
import com.skrumaz.app.data.Store.Projects;

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
 * Created by Paito Anderson on 12/2/2013.
 */
public class GetProjects {

    private List<Project> projects = new ArrayList<Project>();

    public List<Project> FetchItems(Context context) {

        // Setup HTTP Request
        DefaultHttpClient httpClient = new DefaultHttpClient();

        HttpGet get = new HttpGet("https://rally1.rallydev.com/slm/webservice/v2.0/project");

        Log.d("GetProjects", "https://rally1.rallydev.com/slm/webservice/v2.0/project?pretty=true");

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
                JSONArray projectArray = new JSONObject(responseStrBuilder.toString()).getJSONObject("QueryResult").getJSONArray("Results");

                // Iterate though Defects
                for (int i = 0; i < projectArray.length(); i++) {

                    String[] projectUrl = projectArray.getJSONObject(i).getString("_ref").split("/");

                    Project project = new Project();
                    project.setOid(Long.parseLong(projectUrl[projectUrl.length-1]));
                    project.setName(projectArray.getJSONObject(i).getString("_refObjectName"));

                    // Add iteration to list
                    projects.add(project);
                }

                // Store items in Database
                Projects db = new Projects(context);
                db.storeProjects(projects);
                db.close();

            } else {
                //((IterationList)context).SetError(false, statusLine.getReasonPhrase());
                //Log.d("GetProjects", "Project Error: " + statusLine.getReasonPhrase());
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return projects;
    }
}
