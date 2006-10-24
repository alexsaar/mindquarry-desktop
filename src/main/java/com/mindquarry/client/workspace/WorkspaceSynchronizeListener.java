/**
 * Copyright (C) 2005 MindQuarry GmbH, All Rights Reserved
 */
package com.mindquarry.client.workspace;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Set;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.xml.sax.InputSource;

import com.mindquarry.client.util.RegUtil;
import com.mindquarry.client.xml.TeamlistContentHandler;

/**
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class WorkspaceSynchronizeListener implements Listener {
    /**
     * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
     */
    public void handleEvent(Event event) {
        HashMap<String, String> workspaces = new HashMap<String, String>();

        getTeamspaceList(workspaces);
        updateWorkspaces(workspaces);
    }

    private void updateWorkspaces(HashMap<String, String> workspaces) {
        // get directory for workspaces
        File workspacesDir = new File(RegUtil.getMyDocumentsFolder());
        if (!workspacesDir.exists()) {
            workspacesDir.mkdir();
        }
        // loop existing workspace directories
        for (String workspace : workspacesDir.list()) {
            // loop received workspace items
            if (workspaces.containsKey(workspace)) {
                // TODO update workspace

                // remove entry from workspace list
                workspaces.remove(workspace);
            }
        }
        // add additional workspace directories
        Set<String> keys = workspaces.keySet();
        for (String key : keys) {
            File newWorkspaceDir = new File(workspacesDir.getAbsolutePath()
                    + "/" + key);
            if (newWorkspaceDir.exists()) {
                // TODO not sure what to do here, maybe we need to overwrite
                // these dirs
                continue;
            } else {
                newWorkspaceDir.mkdir();
            }
        }
    }

    private void getTeamspaceList(HashMap<String, String> workspaces) {
        HttpClient client = new HttpClient();
        client.getState().setCredentials(
                new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT,
                        AuthScope.ANY_REALM),
                new UsernamePasswordCredentials("alexs", "alexs"));
        GetMethod get = new GetMethod(
                "http://172.16.5.142:8888/teamspace/teamlist.xml");
        get.setDoAuthentication(true);

        try {
            client.executeMethod(get);
            String teamlistXML = get.getResponseBodyAsString();
            InputStream is = new ByteArrayInputStream(teamlistXML.getBytes());
            get.releaseConnection();

            // parse teamspace list
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            parser.parse(new InputSource(is), new TeamlistContentHandler(
                    workspaces));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
