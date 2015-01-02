package com.skrumaz.app.utils;

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;

/**
 * Created by Paito Anderson on 2014-03-19.
 */
public class ClientInfo {

    private static final String appVersionName = "1.1.2";

    public static HttpGet addHttpGetHeaders(HttpGet httpGet) {
        httpGet.setHeader("X-RallyIntegrationName", "Skrumaz");
        httpGet.setHeader("X-RallyIntegrationVendor", "Paito Anderson");
        httpGet.setHeader("X-RallyIntegrationVersion", appVersionName);
        httpGet.setHeader("X-RallyIntegrationOS", System.getProperty("os.name") + " " + System.getProperty("os.version"));
        return httpGet;
    }

    public static HttpPut addHttpPutHeaders(HttpPut httpPut) {
        httpPut.setHeader("X-RallyIntegrationName", "Skrumaz");
        httpPut.setHeader("X-RallyIntegrationVendor", "Paito Anderson");
        httpPut.setHeader("X-RallyIntegrationVersion", appVersionName);
        httpPut.setHeader("X-RallyIntegrationOS", System.getProperty("os.name") + " " + System.getProperty("os.version"));
        return httpPut;
    }

    public static HttpDelete addHttpDeleteHeaders(HttpDelete httpDelete) {
        httpDelete.setHeader("X-RallyIntegrationName", "Skrumaz");
        httpDelete.setHeader("X-RallyIntegrationVendor", "Paito Anderson");
        httpDelete.setHeader("X-RallyIntegrationVersion", appVersionName);
        httpDelete.setHeader("X-RallyIntegrationOS", System.getProperty("os.name") + " " + System.getProperty("os.version"));
        return httpDelete;
    }
}