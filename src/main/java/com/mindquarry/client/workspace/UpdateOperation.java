/*
 * Copyright (C) 2006-2007 MindQuarry GmbH, All Rights Reserved
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
package com.mindquarry.client.workspace;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.eclipse.swt.widgets.Widget;
import org.tigris.subversion.javahl.ClientException;
import org.tigris.subversion.javahl.Revision;

import com.mindquarry.client.MindClient;
import com.mindquarry.client.util.network.HttpUtil;
import com.mindquarry.client.util.widgets.MessageDialogUtil;
import com.mindquarry.client.workspace.xml.TeamListTransformer;
import com.mindquarry.client.workspace.xml.TeamspaceTransformer;

/**
 * Updates the local working copy of the workspaces.
 * 
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class UpdateOperation extends SvnOperation {
    public UpdateOperation(final MindClient client,
            List<Widget> progressBars) {
        super(client, progressBars);
    }

    /**
     * @see java.lang.Runnable#run()
     */
    public void run() {
        HashMap<String, String> teamspaces = new HashMap<String, String>();
        if (!getTeamspaceList(teamspaces)) {
            return;
        }
        updateWorkspaces(teamspaces);
    }

    private boolean getTeamspaceList(HashMap<String, String> teamspaces) {
        InputStream content = null;
        try {
            content = HttpUtil.getContentAsXML(client.getProfileList()
                    .selectedProfile().getLogin(), client.getProfileList()
                    .selectedProfile().getPassword(), client.getProfileList()
                    .selectedProfile().getEndpoint()
                    + "/teams"); //$NON-NLS-1$
        } catch (Exception e) {
            MessageDialogUtil.displaySyncErrorMsg(Messages
                    .getString("UpdateOperation.2")); //$NON-NLS-1$
            return false;
        }
        // check if some contant was received
        if (content == null) {
            return false;
        }
        // parse teamspace list
        SAXReader reader = new SAXReader();
        Document doc;
        try {
            doc = reader.read(content);
        } catch (DocumentException e) {
            MessageDialogUtil.displaySyncErrorMsg(Messages
                    .getString("UpdateOperation.4")); //$NON-NLS-1$
            return false;
        }
        // create a transformer for teamspace list
        TeamListTransformer listTrans = new TeamListTransformer();
        listTrans.execute(doc);

        // init progress steps for progress dialog
        int tsCount = listTrans.getTeamspaces().size();
        setProgressSteps((tsCount * 3) + 1);
        updateProgress();

        // loop teamspace descriptions
        for (String tsID : listTrans.getTeamspaces()) {
            content = null;
            try {
                content = HttpUtil.getContentAsXML(client.getProfileList()
                        .selectedProfile().getLogin(), client.getProfileList()
                        .selectedProfile().getPassword(), client
                        .getProfileList().selectedProfile().getEndpoint()
                        + "/teams/team/" + tsID + "/"); //$NON-NLS-1$ //$NON-NLS-2$
            } catch (Exception e) {
                MessageDialogUtil.displaySyncErrorMsg(Messages
                        .getString("UpdateOperation.6") //$NON-NLS-1$
                        + tsID + Messages.getString("UpdateOperation.7")); //$NON-NLS-1$
                return false;
            }
            // parse teamspace description
            try {
                doc = reader.read(content);
            } catch (DocumentException e) {
                e.printStackTrace();
                return false;
            }
            // create a transformer for teamspace description
            TeamspaceTransformer tsTrans = new TeamspaceTransformer();
            tsTrans.execute(doc);

            teamspaces.put(tsID, tsTrans.getWorkspace());
            updateProgress();
        }
        return true;
    }

    private void updateWorkspaces(HashMap<String, String> workspaces) {
        // init SVN client API types
        svnClient
                .username(client.getProfileList().selectedProfile().getLogin());
        svnClient.password(client.getProfileList().selectedProfile()
                .getPassword());

        // get directory for workspaces
        File teamspacesDir = new File(client.getProfileList().selectedProfile()
                .getLocation());

        // check if teamspace dir already exists, if not create it
        if (!teamspacesDir.exists()) {
            teamspacesDir.mkdirs();
        }
        // loop existing workspace directories
        for (String id : teamspacesDir.list()) {
            // loop received workspace items
            if (workspaces.containsKey(id)) {
                // remove entry from workspace list
                workspaces.remove(id);

                // update workspace
                updateWorkspace(new File(teamspacesDir.getAbsolutePath()
                        + "/" + id), id); //$NON-NLS-1$
            }
            updateProgress();
        }
        // add additional workspace directories
        for (String id : workspaces.keySet()) {
            // create directory for the new workspace
            File newWorkspaceDir = new File(teamspacesDir.getAbsolutePath()
                    + "/" + id); //$NON-NLS-1$
            newWorkspaceDir.mkdir();
            checkoutWorkspace(workspaces.get(id), newWorkspaceDir, id);
            updateProgress();
        }
    }

    private void checkoutWorkspace(String url, File dir, String id) {
        try {
            svnClient.checkout(url, dir.getAbsolutePath(), Revision.HEAD, true);
        } catch (ClientException e) {
            MessageDialogUtil.displaySyncErrorMsg(Messages
                    .getString("UpdateOperation.11") //$NON-NLS-1$
                    + " '" //$NON-NLS-1$
                    + id + "' (" //$NON-NLS-1$
                    + e.getMessage() + ")"); //$NON-NLS-1$
        }
    }

    private void updateWorkspace(File dir, String id) {
        try {
            svnClient.update(dir.getAbsolutePath(), Revision.HEAD, true);
        } catch (ClientException e) {
            MessageDialogUtil.displaySyncErrorMsg(Messages
                    .getString("UpdateOperation.11") //$NON-NLS-1$
                    + " '" //$NON-NLS-1$
                    + id + "' (" //$NON-NLS-1$
                    + e.getMessage() + ")"); //$NON-NLS-1$
        }
    }
}
