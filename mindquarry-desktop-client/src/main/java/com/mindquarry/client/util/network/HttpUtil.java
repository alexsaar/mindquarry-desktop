/*
 * Copyright (C) 2006-2007 Mindquarry GmbH, All Rights Reserved
 * 
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 */
package com.mindquarry.client.util.network;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PutMethod;

import com.mindquarry.client.MindClient;

/**
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class HttpUtil {
    public static InputStream getContentAsXML(String login, String pwd,
            String address) throws HttpException, IOException {
        HttpClient client = createHttpClient(login, pwd, address);
        GetMethod get = createAndExecuteGetMethod(address, client);

        InputStream result = null;
        if (get.getStatusCode() == 200) {
            result = get.getResponseBodyAsStream();
        } else if (get.getStatusCode() == 401) {
            MindClient.showErrorMessage(Messages.getString("HttpUtil.0")); //$NON-NLS-1$
        } else {
            MindClient.showErrorMessage(Messages.getString("HttpUtil.1") //$NON-NLS-1$
                    + get.getStatusCode());
        }
        return result;
    }

    public static String getContentAsString(String login, String pwd,
            String address) throws HttpException, IOException {
        HttpClient client = createHttpClient(login, pwd, address);
        GetMethod get = createAndExecuteGetMethod(address, client);

        String result = null;
        if (get.getStatusCode() == 200) {
            result = get.getResponseBodyAsString();
        } else if (get.getStatusCode() == 401) {
            MindClient.showErrorMessage(Messages.getString("HttpUtil.0")); //$NON-NLS-1$
        } else {
            MindClient.showErrorMessage(Messages.getString("HttpUtil.1") //$NON-NLS-1$
                    + get.getStatusCode());
        }
        return result;
    }

    public static void putAsXML(String login, String pwd, String address,
            byte[] content) throws HttpException, IOException {
        HttpClient client = createHttpClient(login, pwd, address);

        PutMethod put = new PutMethod(address);
        put.setDoAuthentication(true);
        put.addRequestHeader("accept", "text/xml"); //$NON-NLS-1$ //$NON-NLS-2$
        put.setRequestEntity(new ByteArrayRequestEntity(content));

        client.executeMethod(put);

        if (put.getStatusCode() == 401) {
            MindClient.showErrorMessage(Messages.getString("HttpUtil.0")); //$NON-NLS-1$
        } else if (put.getStatusCode() == 302) {
            // we received a redirect to the URL of the putted document, so
            // everthign seems right and we have nothing to do
        } else if (put.getStatusCode() != 200) {
            MindClient.showErrorMessage(Messages.getString("HttpUtil.1") //$NON-NLS-1$
                    + put.getStatusCode());
        }
        put.releaseConnection();
    }

    private static HttpClient createHttpClient(String login, String pwd,
            String address) throws MalformedURLException {
        URL url = new URL(address);

        HttpClient client = new HttpClient();
        client.getParams().setAuthenticationPreemptive(true);
        client.getState()
                .setCredentials(
                        new AuthScope(url.getHost(), url.getPort(),
                                AuthScope.ANY_REALM),
                        new UsernamePasswordCredentials(login, pwd));
        return client;
    }

    private static GetMethod createAndExecuteGetMethod(String address,
            HttpClient client) throws IOException, HttpException {
        GetMethod get = new GetMethod(address);
        get.setDoAuthentication(true);
        get.addRequestHeader("accept", "text/xml"); //$NON-NLS-1$ //$NON-NLS-2$
        client.executeMethod(get);
        return get;
    }
}
