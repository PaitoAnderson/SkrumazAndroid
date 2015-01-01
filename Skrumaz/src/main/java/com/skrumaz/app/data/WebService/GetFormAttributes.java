package com.skrumaz.app.data.WebService;

import android.content.Context;
import android.util.Log;

import com.skrumaz.app.classes.AttributeDefinition;
import com.skrumaz.app.data.Preferences;
import com.skrumaz.app.utils.AttributeTypeLookup;
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
import java.util.Collections;
import java.util.List;

/**
 * Created by Paito Anderson on 1/26/2014.
 */
public class GetFormAttributes {

    List<AttributeDefinition> attributeDefinitions = new ArrayList<>();

    public List<AttributeDefinition> FetchItems(Context context, Long typeDefinition) {

        // Setup HTTP Request
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpGet get = new HttpGet("https://rally1.rallydev.com/slm/webservice/v2.0/TypeDefinition/" + typeDefinition + "/Attributes?fetch=ObjectID,AllowedValues,AttributeType,Constrained,Custom,ElementName,Hidden,MaxLength,Name,ReadOnly,Required&pagesize=200");
        Log.d("GetTypeDefinitions", "https://rally1.rallydev.com/slm/webservice/v2.0/TypeDefinition/" + typeDefinition + "/Attributes?fetch=ObjectID,AllowedValues,AttributeType,Constrained,Custom,ElementName,Hidden,MaxLength,Name,ReadOnly,Required&pagesize=200&pretty=true");

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
                JSONArray formAttributeArray = new JSONObject(responseStrBuilder.toString()).getJSONObject("QueryResult").getJSONArray("Results");

                // Iterate though Form Attributes
                for (int i = 0; i < formAttributeArray.length(); i++) {
                    AttributeDefinition attributeDefinition = new AttributeDefinition();
                    attributeDefinition.setObjectUUID(formAttributeArray.getJSONObject(i).getString("_refObjectUUID"));
                    attributeDefinition.setObjectId(formAttributeArray.getJSONObject(i).getLong("ObjectID"));
                    attributeDefinition.setAttributeType(AttributeTypeLookup.stringToAttributeType(formAttributeArray.getJSONObject(i).getString("AttributeType")));
                    attributeDefinition.setConstrained(formAttributeArray.getJSONObject(i).getBoolean("Constrained"));
                    attributeDefinition.setCustom(formAttributeArray.getJSONObject(i).getBoolean("Custom"));
                    attributeDefinition.setHidden(formAttributeArray.getJSONObject(i).getBoolean("Hidden"));
                    attributeDefinition.setName(formAttributeArray.getJSONObject(i).getString("Name"));
                    attributeDefinition.setRequired(formAttributeArray.getJSONObject(i).getBoolean("Required"));
                    attributeDefinition.setReadOnly(formAttributeArray.getJSONObject(i).getBoolean("ReadOnly"));
                    attributeDefinition.setElementName(formAttributeArray.getJSONObject(i).getString("ElementName"));
                    attributeDefinition.setMaxLength(formAttributeArray.getJSONObject(i).getInt("MaxLength"));

                    // Don't add hidden ones
                    if (!attributeDefinition.getHidden()) {
                        if (!attributeDefinition.getReadOnly()) {
                            if (!skipFormAttribute(attributeDefinition.getElementName() )) {
                                attributeDefinitions.add(attributeDefinition);
                            }
                        }
                    }
                }
            }

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        // Order Form Attributes
        attributeDefinitions = orderFormAttributes(attributeDefinitions);


        return attributeDefinitions;
    }

    private Boolean skipFormAttribute(String name) {

        if (name.equalsIgnoreCase("Parent")) {
            return true;
        } else if (name.equalsIgnoreCase("DisplayColor")) {
            return true;
        } else if (name.equalsIgnoreCase("Successors")) {
            return true;
        } else if (name.equalsIgnoreCase("Predecessors")) {
            return true;
        } else if (name.equalsIgnoreCase("Tags")) {
            return true;
        } else if (name.equalsIgnoreCase("Workspace")) {
            return true;
        } else if (name.equalsIgnoreCase("Test Case Result")) {
            return true;
        } else if (name.equalsIgnoreCase("Test Case")) {
            return true;
        } else if (name.equalsIgnoreCase("Requirement")) {
            return true;
        }
        return false;
    }

    private List<AttributeDefinition> orderFormAttributes(List<AttributeDefinition> attributeDefinitions)
    {
        // How items should be re-ordered
        List<String> orderNames = new ArrayList<>();

        /**
         * GENERAL
         */
        orderNames.add("Name");
        orderNames.add("Theme");
        orderNames.add("Start Date");
        orderNames.add("End Date");
        //orderNames.add("Tags");
        orderNames.add("Description");
        //orderNames.add("Attachments");
        orderNames.add("Owner");
        orderNames.add("Project");
        orderNames.add("State");
        orderNames.add("Planned Velocity");
        orderNames.add("Notes");

        /**
         * DEFECT
         */
        orderNames.add("Priority");
        orderNames.add("Submitted By");
        orderNames.add("Found In Build");
        orderNames.add("Fixed In Build");
        orderNames.add("Verified In Build");
        orderNames.add("Target Build");
        orderNames.add("Target Date");
        orderNames.add("Resolution");
        orderNames.add("Release Note");
        //orderNames.add("User Story");
        //orderNames.add("Test Case");
        orderNames.add("Environment");
        orderNames.add("Severity");
        orderNames.add("Affects Doc");

        /**
         * HIERARCHY
         */
        //orderNames.add("Parent");

        /**
         * SCHEDULE
         */
        orderNames.add("Schedule State");
        orderNames.add("Ready");
        orderNames.add("Release");
        orderNames.add("Plan Estimate");
        orderNames.add("Blocked");
        orderNames.add("Blocked Reason");
        orderNames.add("Iteration");

        // Current position in re-ordering process
        int toIndex = 0;

        for (String orderName : orderNames)
        {
            for (AttributeDefinition attributeDefinition : attributeDefinitions) {
                if (attributeDefinition.getName().equalsIgnoreCase(orderName))
                {
                    int fromIndex = attributeDefinitions.indexOf(attributeDefinition);
                    Collections.swap(attributeDefinitions, fromIndex, toIndex);
                    toIndex++;
                }
            }
        }

        return attributeDefinitions;
    }
}
