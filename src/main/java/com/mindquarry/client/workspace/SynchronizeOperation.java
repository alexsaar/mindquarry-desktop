/**
 * Copyright (C) 2005 MindQuarry GmbH, All Rights Reserved
 */
package com.mindquarry.client.workspace;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.springframework.beans.factory.BeanFactory;
import org.tigris.subversion.javahl.ClientException;
import org.tigris.subversion.javahl.Revision;
import org.tigris.subversion.javahl.SVNClientInterface;
import org.xml.sax.InputSource;

import com.mindquarry.client.MindClient;
import com.mindquarry.client.util.HomeUtil;
import com.mindquarry.client.util.OperatingSystem;
import com.mindquarry.client.xml.TeamlistContentHandler;

/**
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class SynchronizeOperation implements IRunnableWithProgress {
    private final MindClient client;

    private final SVNClientInterface svnClient;

    public SynchronizeOperation(MindClient client) {
        this.client = client;

        // get SVN client interface component
        BeanFactory factory = client.getFactory();
        svnClient = (SVNClientInterface) factory
                .getBean(SVNClientInterface.class.getName());
    }

    /**
     * @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
     */
    public void run(IProgressMonitor monitor) throws InvocationTargetException,
            InterruptedException {
        monitor.beginTask("Synchronizing workspaces ...",
                IProgressMonitor.UNKNOWN);

        HashMap<String, String> workspaces = new HashMap<String, String>();
        if (!getTeamspaceList(workspaces, monitor)) {
            // show (asynchronously) error dialog
            MindClient.getShell().getDisplay().syncExec(new Runnable() {
                /**
                 * @see java.lang.Runnable#run()
                 */
                public void run() {
                    MessageDialog.openError(MindClient.getShell(), "Error",
                            "Could not retrieve list of teamspaces.");
                }
            });
            return;
        }
        // check cancel state
        if (monitor.isCanceled()) {
            return;
        }
        updateWorkspaces(workspaces, monitor);
        monitor.done();
    }

    private boolean getTeamspaceList(HashMap<String, String> workspaces,
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

            if (get.getStatusCode() != 200) {
                return false;
            }
            String teamlistXML = get.getResponseBodyAsString();
            InputStream is = new ByteArrayInputStream(teamlistXML.getBytes());
            get.releaseConnection();

            if (monitor.isCanceled()) {
                return true;
            }
            // parse teamspace list
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            parser.parse(new InputSource(is), new TeamlistContentHandler(
                    workspaces));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void updateWorkspaces(HashMap<String, String> workspaces,
            IProgressMonitor monitor) {
        monitor.setTaskName("Synchronizing workspaces ...");

        // init SVN types
        svnClient.username(client.getOptions()
                .getProperty(MindClient.LOGIN_KEY));
        svnClient.password(client.getOptions().getProperty(
                MindClient.PASSWORD_KEY));

        // get directory for workspaces
        File teamspacesDir;
        if (client.getOS() == OperatingSystem.WINDOWS) {
            teamspacesDir = new File(HomeUtil.getTeamspaceFolderWindows());
        } else {
            teamspacesDir = new File(HomeUtil.getTeamspaceFolder());
        }
        // check if teamspace dir already exists, if not create it
        if (!teamspacesDir.exists()) {
            teamspacesDir.mkdirs();
        }
        // loop existing workspace directories
        for (String tsID : teamspacesDir.list()) {
            if (monitor.isCanceled()) {
                return;
            }
            // loop received workspace items
            if (workspaces.containsKey(tsID)) {
                // remove entry from workspace list
                workspaces.remove(tsID);

                // TODO check if folder is under version control

                // update workspace
                monitor.setTaskName("Synchronizing workspace " + tsID + " ..."); //$NON-NLS-2$
                updateWorkspace(new File(teamspacesDir.getAbsolutePath()
                        + "/" + tsID)); //$NON-NLS-1$
            }
        }
        // add additional workspace directories
        for (String wsID : workspaces.keySet()) {
            if (monitor.isCanceled()) {
                return;
            }
            // create directory for the new workspace
            File newWorkspaceDir = new File(teamspacesDir.getAbsolutePath()
                    + "/" + wsID); //$NON-NLS-1$
            newWorkspaceDir.mkdir();

            monitor.setTaskName("Synchronizing workspace " + wsID + " ..."); //$NON-NLS-2$
            checkoutWorkspace(workspaces.get(wsID), newWorkspaceDir);
        }
    }

    private void checkoutWorkspace(String url, File dir) {
        try {
            svnClient.checkout(url, dir.getAbsolutePath(), Revision.HEAD, true);
        } catch (ClientException e) {
            e.printStackTrace();
        }
    }

    private void updateWorkspace(File dir) {
        try {
            svnClient.update(dir.getAbsolutePath(), Revision.HEAD, true);
        } catch (ClientException e) {
            e.printStackTrace();
        }
    }
}
