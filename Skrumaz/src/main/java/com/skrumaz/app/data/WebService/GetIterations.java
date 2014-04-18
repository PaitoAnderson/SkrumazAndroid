package com.skrumaz.app.data.WebService;

import android.content.Context;
import android.util.Log;

import com.skrumaz.app.MainActivity;
import com.skrumaz.app.classes.Iteration;
import com.skrumaz.app.classes.Project;
import com.skrumaz.app.data.Preferences;
import com.skrumaz.app.data.Store.Iterations;
import com.skrumaz.app.utils.ClientInfo;
import com.skrumaz.app.utils.IterationStatusLookup;

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
 * Created by Paito Anderson on 11/28/2013.
 */
public class GetIterations {

    private List<Iteration> iterations = new ArrayList<Iteration>();
    private Project project = new Project();

    public List<Iteration> FetchItems(Context context) {

        // Set Calling Fragment
        ((MainActivity) context).mFragmentAttached = MainActivity.Fragments.ITERATIONS;

        // Get ProjectId to use
        Long ProjectId = Preferences.getProjectId(context, false);

        // Is ProjectId available? If not, get current.
        if (ProjectId > 0) {

            // Set IterationId to use
            project.setOid(ProjectId);

            // Get Iterations
            GetProjectIterations(context);

            // Store items in Database
            Iterations db = new Iterations(context);
            db.storeIterations(iterations, project);
            db.close();
        } else {

            // Update Main View with status
            ((MainActivity)context).SetProgress("Getting Current Project...");

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
                    GetProjectIterations(context);

                    // Store items in Database
                    Iterations db = new Iterations(context);
                    db.storeIterations(iterations, project);
                    db.close();

                    // Set Project, Workspace to use for current iteration
                    Preferences.setProjectId(context, project.getOid());
                    Preferences.setWorkspaceId(context, jsonIteration.getJSONObject("Iteration").getJSONObject("Workspace").getLong("ObjectID"));

                } else {
                    ((MainActivity)context).SetError(statusLine.getReasonPhrase());
                    Log.e("GetIterations", "Project Error: " + statusLine.getReasonPhrase());
                }

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return iterations;
    }

    // Get Iterations from WebService
    private void GetProjectIterations(Context context) {

        // Update Main View with status
        ((MainActivity)context).SetProgress("Getting Iterations...");

        // Add Backlog Link
        //Iteration backlog = new Iteration();
        //backlog.setName("Backlog");
        //backlog.setOid(Long.parseLong("0"));
        //backlog.setIterationStatus(IterationStatus.RD_PLANNED);
        //iterations.add(backlog);

        // Setup HTTP Request
        DefaultHttpClient httpClient = new DefaultHttpClient();

        HttpGet get = new HttpGet("https://rally1.rallydev.com/slm/webservice/v2.0/project/" + project.getOid() + "/Iterations?fetch=ObjectID,Name,State&order=ObjectID%20desc");

        Log.d("GetIterations","https://rally1.rallydev.com/slm/webservice/v2.0/project/" + project.getOid() + "/Iterations?fetch=ObjectID,Name,State&order=ObjectID%20desc&pretty=true");

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
                JSONArray iterationArray = new JSONObject(responseStrBuilder.toString()).getJSONObject("QueryResult").getJSONArray("Results");

                // Iterate though Iterations
                for (int i = 0; i < iterationArray.length(); i++) {

                    Iteration iteration = new Iteration();
                    iteration.setOid(iterationArray.getJSONObject(i).getLong("ObjectID"));
                    iteration.setName(iterationArray.getJSONObject(i).getString("Name"));
                    iteration.setIterationStatus(IterationStatusLookup.stringToIterationStatus(iterationArray.getJSONObject(i).getString("State")));

                    // Add iteration to list
                    iterations.add(iteration);
                }

            } else {
                ((MainActivity)context).SetError(statusLine.getReasonPhrase());
                Log.d("GetIterations", "Iteration Error: " + statusLine.getReasonPhrase());
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
