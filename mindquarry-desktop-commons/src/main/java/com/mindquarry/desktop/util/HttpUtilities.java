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
package com.mindquarry.desktop.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.preference.PreferenceStore;

import com.mindquarry.desktop.Messages;
import com.mindquarry.desktop.preferences.pages.ProxySettingsPage;

/**
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class HttpUtilities {
    private static Log log = LogFactory.getLog(HttpUtilities.class);

    private static final String AUTH_REFUSED = Messages
            .getString("Authorization has been refused. Please check your login name and password.");
    private static final String CONNECTION_ERROR = Messages
            .getString("Unknown connection error. Status code ");

    private static final int SOCKET_TIMEOUT = 30 * 1000; // milliseconds
    private static final int CONNECTION_TIMEOUT = 60 * 1000; // milliseconds

    private static PreferenceStore store;

    public static void setStore(PreferenceStore store) {
        HttpUtilities.store = store;
    }

    public static InputStream getContentAsXML(String login, String pwd,
            String address) throws NotAuthorizedException,
            MalformedURLException {
        try {
            HttpClient client = createHttpClient(login, pwd, address);
            GetMethod get = createAndExecuteGetMethod(address, client);

            InputStream result = null;
            if (get.getStatusCode() == 200) {
                result = get.getResponseBodyAsStream();
            } else if (get.getStatusCode() == 401) {
                throw new NotAuthorizedException(AUTH_REFUSED, address, login,
                        pwd);
            } else {
                throw new HttpException(Messages
                        .getString("Unknown connection error. Status code ") //$NON-NLS-1$
                        + get.getStatusCode());
            }
            return result;
        } catch (NotAuthorizedException e) {
            throw e;
        } catch (MalformedURLException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e.toString(), e);
        }
    }

    public static String putAsXML(String login, String pwd, String address,
            byte[] content) throws NotAuthorizedException {
        try {
            HttpClient client = createHttpClient(login, pwd, address);
            PutMethod put = new PutMethod(address);
            put.setDoAuthentication(true);
            put.addRequestHeader("accept", "text/xml"); //$NON-NLS-1$ //$NON-NLS-2$
            put.setRequestEntity(new ByteArrayRequestEntity(content,
                    "text/xml; charset=utf-8"));

            log.info("Executing HTTP PUT on " + address); //$NON-NLS-1$
            client.executeMethod(put);
            log.info("Finished HTTP PUT with status code: "//$NON-NLS-1$
                    + put.getStatusCode());

            String id = null;
            if (put.getStatusCode() == 401) {
                throw new NotAuthorizedException(AUTH_REFUSED, address, login,
                        pwd);
            } else if (put.getStatusCode() == 302) {
                Header locationHeader = put.getResponseHeader("location");
                if (locationHeader != null) {
                    // we received a redirect to the URL of the putted document,
                    // so
                    // everything seems right and we just use the new ID:
                    id = locationHeader.getValue();
                } else {
                    throw new RuntimeException(CONNECTION_ERROR
                            + put.getStatusCode());
                }
            } else {
                throw new RuntimeException(CONNECTION_ERROR
                        + put.getStatusCode());
            }
            put.releaseConnection();
            return id;
        } catch (NotAuthorizedException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e.toString(), e);
        }
    }

    public enum CheckResult {
        AUTH_REFUSED, NOT_AVAILABLE, OK
    }

    public static CheckResult checkServerExistence(String login, String pwd,
            String address) throws MalformedURLException {
        try {
            if (!address.endsWith("/")) //$NON-NLS-1$
                address += "/"; //$NON-NLS-1$

            HttpClient client = createHttpClient(login, pwd, address);
            GetMethod get = createAndExecuteGetMethod(address, client);

            if (200 == get.getStatusCode()) {
                return CheckResult.OK;
            } else if (401 == get.getStatusCode()) {
                return CheckResult.AUTH_REFUSED;
            } else {
                return CheckResult.NOT_AVAILABLE;
            }
        } catch (UnknownHostException uhe) {
            return CheckResult.NOT_AVAILABLE;
        } catch (MalformedURLException murle) {
            throw murle;
        } catch (Exception e) {
            log.info(e.toString());
            return CheckResult.NOT_AVAILABLE;
        }
    }

    private static HttpClient createHttpClient(String login, String pwd,
            String address) throws MalformedURLException {
        URL url = new URL(address);

        HttpClient client = new HttpClient();
        client.getParams().setSoTimeout(SOCKET_TIMEOUT);
        client.getParams().setParameter("http.connection.timeout",
                CONNECTION_TIMEOUT);
        client.getParams().setAuthenticationPreemptive(true);
        client.getState()
                .setCredentials(
                        new AuthScope(url.getHost(), url.getPort(),
                                AuthScope.ANY_REALM),
                        new UsernamePasswordCredentials(login, pwd));

        // apply proxy settings if necessary
        if ((store != null)
                && (store.getBoolean(ProxySettingsPage.PREF_PROXY_ENABLED))) {
            URL proxyUrl = new URL(store
                    .getString(ProxySettingsPage.PREF_PROXY_URL));
            String proxyLogin = store
                    .getString(ProxySettingsPage.PREF_PROXY_LOGIN);
            String proxyPwd = store
                    .getString(ProxySettingsPage.PREF_PROXY_PASSWORD);

            client.getHostConfiguration().setProxy(proxyUrl.getHost(),
                    proxyUrl.getPort());
            client.getState().setProxyCredentials(
                    new AuthScope(proxyUrl.getHost(), proxyUrl.getPort()),
                    new UsernamePasswordCredentials(proxyLogin, proxyPwd));
        }
        return client;
    }

    private static GetMethod createAndExecuteGetMethod(String address,
            HttpClient client) throws IOException, HttpException {
        GetMethod get = new GetMethod(address);
        get.setDoAuthentication(true);
        get.addRequestHeader("accept", "text/xml"); //$NON-NLS-1$ //$NON-NLS-2$

        log.info("Executing HTTP GET on " + address); //$NON-NLS-1$
        client.executeMethod(get);
        log.info("Finished HTTP GET with status code: "//$NON-NLS-1$
                + get.getStatusCode());
        return get;
    }
}
