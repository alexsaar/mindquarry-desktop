/**
 * Copyright (C) 2005 MindQuarry GmbH, All Rights Reserved
 */
package com.mindquarry.client.util;

import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;

/**
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class HttpUtil {
    public static String getContent(String login, String pwd, String address)
            throws HttpException, IOException {
        HttpClient httpClient = new HttpClient();
        httpClient.getState().setCredentials(
                new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT,
                        AuthScope.ANY_REALM),
                new UsernamePasswordCredentials(login, pwd));

        GetMethod get = new GetMethod(address);
        get.setDoAuthentication(true);
        httpClient.executeMethod(get);

        if (get.getStatusCode() == 200) {
            String content = get.getResponseBodyAsString();
            get.releaseConnection();
            return content;
        } else if (get.getStatusCode() == 401) {
            MessageDialogUtil
                    .displaySyncErrorMsg("Authorization has been refused. Please check your login ID and password!");
            return null;
        } else {
            MessageDialogUtil
                    .displaySyncErrorMsg("Unknown connection Error. Status Code "
                            + get.getStatusCode());
            return null;
        }
    }
}
