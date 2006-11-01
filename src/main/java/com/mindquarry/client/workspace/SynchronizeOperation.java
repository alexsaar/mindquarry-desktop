/**
 * Copyright (C) 2005 MindQuarry GmbH, All Rights Reserved
 */
package com.mindquarry.client.workspace;

import java.io.File;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.springframework.beans.factory.BeanFactory;
import org.tigris.subversion.javahl.ClientException;
import org.tigris.subversion.javahl.Revision;
import org.tigris.subversion.javahl.SVNClientInterface;

import com.mindquarry.client.MindClient;
import com.mindquarry.client.util.HomeUtil;
import com.mindquarry.client.util.HttpUtil;
import com.mindquarry.client.util.MessageDialogUtil;
import com.mindquarry.client.util.OperatingSystem;
import com.mindquarry.client.xml.TeamListTransformer;
import com.mindquarry.client.xml.TeamspaceTransformer;

/**
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class SynchronizeOperation implements IRunnableWithProgress {
    private final MindClient client;

    private final SVNClientInterface svnClient;

    public SynchronizeOperation(final MindClient client) {
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

        HashMap<String, String> teamspaces = new HashMap<String, String>();
        if (!getTeamspaceList(teamspaces, monitor)) {
            return;
        }
        // check cancel state
        if (monitor.isCanceled()) {
            return;
        }
        updateWorkspaces(teamspaces, monitor);
        monitor.done();
    }

    private boolean getTeamspaceList(HashMap<String, String> teamspaces,
            IProgressMonitor monitor) {
        monitor.setTaskName("Retrieving teamspace list ...");

        String content = null;
        try {
            content = HttpUtil.getContentAsXML(client.getOptions().getProperty(
                    MindClient.LOGIN_KEY), client.getOptions().getProperty(
                    MindClient.PASSWORD_KEY), client.getOptions().getProperty(
                    MindClient.ENDPOINT_KEY)
                    + "/teamspace"); //$NON-NLS-1$
        } catch (Exception e) {
            MessageDialogUtil
                    .displaySyncErrorMsg("Could not retrieve list of teamspaces due to unexpected connection errors.");
            return false;
        }
        // check if some contant was received
        if (content == null) {
            return false;
        }
        // check if the operation was canceled
        if (monitor.isCanceled()) {
            return true;
        }
        // parse teamspace list
        monitor.setTaskName("Checking teamspace list ...");
        SAXReader reader = new SAXReader();
        Document doc;
        try {
            doc = reader.read(new StringReader(content));
        } catch (DocumentException e) {
            MessageDialogUtil
                    .displaySyncErrorMsg("An error occured while reading list of teamspaces.");
            return false;
        }
        // create a transformer for teamspace list
        TeamListTransformer listTrans = new TeamListTransformer();
        listTrans.execute(doc);

        // loop teamspace descriptions
        for (String tsID : listTrans.getTeamspaces()) {
            monitor.setTaskName("Retrieving description for teamspace '" + tsID
                    + "'...");

            content = null;
            try {
                content = HttpUtil.getContentAsXML(client.getOptions()
                        .getProperty(MindClient.LOGIN_KEY), client.getOptions()
                        .getProperty(MindClient.PASSWORD_KEY), client
                        .getOptions().getProperty(MindClient.ENDPOINT_KEY)
                        + "/teamspace/" + tsID); //$NON-NLS-1$
            } catch (Exception e) {
                MessageDialogUtil
                        .displaySyncErrorMsg("Could not retrieve teamspace '"
                                + tsID
                                + "' due to unexpected connection errors.");
                return false;
            }
            // parse teamspace description
            monitor.setTaskName("Checking description for teamspace '" + tsID
                    + "'...");
            try {
                doc = reader.read(new StringReader(content));
            } catch (DocumentException e) {
                e.printStackTrace();
                return false;
            }
            // create a transformer for teamspace description
            TeamspaceTransformer tsTrans = new TeamspaceTransformer();
            tsTrans.execute(doc);

            teamspaces.put(tsID, tsTrans.getWorkspace());
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
        for (String id : teamspacesDir.list()) {
            if (monitor.isCanceled()) {
                return;
            }
            // loop received workspace items
            if (workspaces.containsKey(id)) {
                // remove entry from workspace list
                workspaces.remove(id);

                // TODO check if folder is under version control

                // update workspace
                monitor.setTaskName("Synchronizing workspace " + id + " ..."); //$NON-NLS-2$
                updateWorkspace(new File(teamspacesDir.getAbsolutePath()
                        + "/" + id), id); //$NON-NLS-1$
            }
        }
        // add additional workspace directories
        for (String id : workspaces.keySet()) {
            if (monitor.isCanceled()) {
                return;
            }
            // create directory for the new workspace
            File newWorkspaceDir = new File(teamspacesDir.getAbsolutePath()
                    + "/" + id); //$NON-NLS-1$
            newWorkspaceDir.mkdir();

            monitor.setTaskName("Synchronizing workspace " + id + " ..."); //$NON-NLS-2$
            checkoutWorkspace(workspaces.get(id), newWorkspaceDir, id);
        }
    }

    private void checkoutWorkspace(String url, File dir, String id) {
        try {
            svnClient.checkout(url, dir.getAbsolutePath(), Revision.HEAD, true);
        } catch (ClientException e) {
            MessageDialogUtil
                    .displaySyncErrorMsg("Could not synchronize workspace "
                            + id);
        }
    }

    private void updateWorkspace(File dir, String id) {
        try {
            svnClient.update(dir.getAbsolutePath(), Revision.HEAD, true);
        } catch (ClientException e) {
            MessageDialogUtil
                    .displaySyncErrorMsg("Could not synchronize workspace "
                            + id);
        }
    }
}
