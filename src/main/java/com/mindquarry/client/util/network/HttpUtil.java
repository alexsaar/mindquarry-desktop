/**
 * Copyright (C) 2005 MindQuarry GmbH, All Rights Reserved
 */
package com.mindquarry.client.util.network;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PutMethod;

import com.mindquarry.client.util.widgets.MessageDialogUtil;

/**
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class HttpUtil {
    public static InputStream getContentAsXML(String login, String pwd,
            String address) throws HttpException, IOException {
        HttpClient httpClient = new HttpClient();
        httpClient.getState().setCredentials(
                new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT,
                        AuthScope.ANY_REALM),
                new UsernamePasswordCredentials(login, pwd));

        GetMethod get = new GetMethod(address);
        get.setDoAuthentication(true);
        get.addRequestHeader("accept", "text/xml"); //$NON-NLS-1$ //$NON-NLS-2$
        httpClient.executeMethod(get);

        InputStream result = null;
        if (get.getStatusCode() == 200) {
        	result = get.getResponseBodyAsStream();
        } else if (get.getStatusCode() == 401) {
            MessageDialogUtil
                    .displaySyncErrorMsg(Messages.getString("HttpUtil.0")); //$NON-NLS-1$
        } else {
            MessageDialogUtil
                    .displaySyncErrorMsg(Messages.getString("HttpUtil.1") //$NON-NLS-1$
                            + get.getStatusCode());
        }
        //get.releaseConnection();
        return result;
    }

    public static void putAsXML(String login, String pwd, String address,
            byte[] content) throws HttpException, IOException {
        HttpClient httpClient = new HttpClient();
        httpClient.getState().setCredentials(
                new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT,
                        AuthScope.ANY_REALM),
                new UsernamePasswordCredentials(login, pwd));

        PutMethod put = new PutMethod(address);
        put.setDoAuthentication(true);
        put.setRequestEntity(new ByteArrayRequestEntity(content));

        httpClient.executeMethod(put);

        if (put.getStatusCode() == 401) {
            MessageDialogUtil
                    .displaySyncErrorMsg(Messages.getString("HttpUtil.0")); //$NON-NLS-1$
        } else if (put.getStatusCode() != 200) {
            MessageDialogUtil
                    .displaySyncErrorMsg(Messages.getString("HttpUtil.1") //$NON-NLS-1$
                            + put.getStatusCode());
        }
        put.releaseConnection();
    }
}
