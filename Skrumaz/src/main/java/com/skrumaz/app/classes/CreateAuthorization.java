package com.skrumaz.app.classes;

import org.apache.http.client.HttpClient;

/**
 * Created by Paito Anderson on 2/7/2014.
 */
public class CreateAuthorization {
    private String SecurityToken;
    private HttpClient httpClient;

    public String getSecurityToken() {
        return SecurityToken;
    }

    public void setSecurityToken(String securityToken) {
        SecurityToken = securityToken;
    }

    public HttpClient getHttpClient() {
        return httpClient;
    }

    public void setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }
}
