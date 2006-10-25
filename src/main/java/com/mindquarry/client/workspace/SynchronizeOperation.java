/**
 * Copyright (C) 2005 MindQuarry GmbH, All Rights Reserved
 */
package com.mindquarry.client.workspace;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Set;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;
import org.tmatesoft.svn.core.wc.SVNWCUtil;
import org.xml.sax.InputSource;

import com.mindquarry.client.MindClient;
import com.mindquarry.client.util.RegUtil;
import com.mindquarry.client.xml.TeamlistContentHandler;

/**
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class SynchronizeOperation implements IRunnableWithProgress {
    private final MindClient client;

    public SynchronizeOperation(MindClient client) {
        this.client = client;
    }

    /**
     * @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
     */
    public void run(IProgressMonitor monitor) throws InvocationTargetException,
            InterruptedException {
        monitor.beginTask("Synchronizing workspaces ...",
                IProgressMonitor.UNKNOWN);

        HashMap<String, String> workspaces = new HashMap<String, String>();
        getTeamspaceList(workspaces, monitor);

        if (monitor.isCanceled()) {
            return;
        }
        updateWorkspaces(workspaces, monitor);
        monitor.done();
    }

    private void getTeamspaceList(HashMap<String, String> workspaces,
            IProgressMonitor monitor) {
        HttpClient httpClient = new HttpClient();
        httpClient.getState().setCredentials(
                new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT,
                        AuthScope.ANY_REALM),
                new UsernamePasswordCredentials(client.getOptions()
                        .getProperty(MindClient.LOGIN_KEY), client.getOptions()
                        .getProperty(MindClient.PASSWORD_KEY)));

        GetMethod get = new GetMethod(client.getOptions().getProperty(
                MindClient.ENDPOINT_KEY)
                + "/teamspace/teamlist.xml"); //$NON-NLS-1$
        get.setDoAuthentication(true);

        try {
            monitor.setTaskName("Retrieving teamspace list ...");

            httpClient.executeMethod(get);
            String teamlistXML = get.getResponseBodyAsString();
            InputStream is = new ByteArrayInputStream(teamlistXML.getBytes());
            get.releaseConnection();

            if (monitor.isCanceled()) {
                return;
            }

            // parse teamspace list
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            parser.parse(new InputSource(is), new TeamlistContentHandler(
                    workspaces));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateWorkspaces(HashMap<String, String> workspaces,
            IProgressMonitor monitor) {
        monitor.setTaskName("Synchronizing workspaces ...");

        // init SVN types
        ISVNAuthenticationManager authManager = SVNWCUtil
                .createDefaultAuthenticationManager(client.getOptions()
                        .getProperty(MindClient.LOGIN_KEY), client.getOptions()
                        .getProperty(MindClient.PASSWORD_KEY));
        SVNUpdateClient svnClient = new SVNUpdateClient(authManager, SVNWCUtil
                .createDefaultOptions(true));

        // get directory for workspaces
        File workspacesDir = new File(RegUtil.getMyDocumentsFolder());
        if (!workspacesDir.exists()) {
            workspacesDir.mkdir();
        }
        // loop existing workspace directories
        for (String wsID : workspacesDir.list()) {
            if (monitor.isCanceled()) {
                return;
            }
            // loop received workspace items
            if (workspaces.containsKey(wsID)) {
                // remove entry from workspace list
                workspaces.remove(wsID);

                // TODO check if folder is under version control

                // update workspace
                monitor.setTaskName("Synchronizing workspace " + wsID + " ..."); //$NON-NLS-1$
                updateWorkspace(svnClient, new File(workspacesDir
                        .getAbsolutePath()
                        + "/" + wsID)); //$NON-NLS-1$
            }
        }
        // add additional workspace directories
        for (String wsID : workspaces.keySet()) {
            if (monitor.isCanceled()) {
                return;
            }
            // create directory for the new workspace
            File newWorkspaceDir = new File(workspacesDir.getAbsolutePath()
                    + "/" + wsID); //$NON-NLS-1$
            SVNURL url = null;
            try {
                url = SVNURL.parseURIEncoded(workspaces.get(wsID));
            } catch (SVNException e) {
                MessageDialog.openError(MindClient.getShell(),
                        "Synchronization Error",
                        "Malformed workspace location.");
                continue;
            }
            newWorkspaceDir.mkdir();
            monitor.setTaskName("Synchronizing workspace " + wsID + " ..."); //$NON-NLS-1$
            checkoutWorkspace(svnClient, url, newWorkspaceDir);
        }
    }

    private void checkoutWorkspace(SVNUpdateClient svnClient, SVNURL url,
            File dir) {
        try {
            svnClient.doCheckout(url, dir, SVNRevision.HEAD, SVNRevision.HEAD,
                    true);
        } catch (SVNException e) {
            e.printStackTrace();
        }
    }

    private void updateWorkspace(SVNUpdateClient svnClient, File dir) {
        try {
            svnClient.doUpdate(dir, SVNRevision.HEAD, true);
        } catch (SVNException e) {
            e.printStackTrace();
        }
    }
}
