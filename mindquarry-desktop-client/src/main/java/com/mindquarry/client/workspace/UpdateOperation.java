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
package com.mindquarry.client.workspace;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.tigris.subversion.javahl.ClientException;
import org.tigris.subversion.javahl.Revision;

import com.mindquarry.client.MindClient;
import com.mindquarry.client.teamspace.TeamspaceUtilities;
import com.mindquarry.client.util.network.HttpUtilities;
import com.mindquarry.client.workspace.widgets.SynchronizeWidget;
import com.mindquarry.client.workspace.xml.TeamspaceTransformer;
import com.mindquarry.desktop.preferences.profile.Profile;

/**
 * Updates the local working copy of the workspaces.
 * 
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class UpdateOperation extends SvnOperation {
    public UpdateOperation(final MindClient client,
            List<SynchronizeWidget> synAreas) {
        super(client, synAreas);
    }

    public void run() {
        resetProgress();
        HashMap<String, String> teamspaces = new HashMap<String, String>();
        if (!getTeamspaceList(teamspaces)) {
            resetProgress();
            return;
        }
        resetProgress();
        updateWorkspaces(teamspaces);
        resetProgress();
    }

    private boolean getTeamspaceList(HashMap<String, String> teamspaces) {
        setMessage(Messages.getString("UpdateOperation.1")); //$NON-NLS-1$
        Profile selectedProfile = Profile.getSelectedProfile(client
                .getPreferenceStore());

        List<String> teamspaceList;
        try {
            teamspaceList = TeamspaceUtilities.getTeamspaceNamesForProfile(selectedProfile);
        } catch (Exception e) {
            MindClient
                    .showErrorMessage(Messages.getString("UpdateOperation.2")); //$NON-NLS-1$
            return false;
        }
        // init progress steps for progress dialog
        int tsCount = teamspaceList.size();
        int tsNbr = 0;
        setProgressSteps(tsCount + 1);
        updateProgress();

        // loop teamspace descriptions
        for (String tsID : teamspaceList) {
            setMessage(Messages.getString("UpdateOperation.0") + " (" + ++tsNbr + " of " //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
                    + tsCount + ")..."); //$NON-NLS-1$

            InputStream content = null;
            try {
                content = HttpUtilities.getContentAsXML(selectedProfile.getLogin(),
                        selectedProfile.getPassword(), selectedProfile
                                .getServerURL()
                                + "/teams/team/" + tsID + "/"); //$NON-NLS-1$ //$NON-NLS-2$
            } catch (Exception e) {
                MindClient.showErrorMessage(Messages
                        .getString("UpdateOperation.6") //$NON-NLS-1$
                        + tsID + Messages.getString("UpdateOperation.7")); //$NON-NLS-1$
                return false;
            }
            // parse teamspace description
            Document doc;
            try {
                SAXReader reader = new SAXReader();
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
        Profile selectedProfile = Profile.getSelectedProfile(client
                .getPreferenceStore());

        // init SVN client API types
        svnClient.username(selectedProfile.getLogin());
        svnClient.password(selectedProfile.getPassword());

        // get directory for workspaces
        File teamspacesDir = new File(selectedProfile.getWorkspaceFolder());

        // check if teamspace dir already exists, if not create it
        if (!teamspacesDir.exists()) {
            teamspacesDir.mkdirs();
        }
        // init progress steps for progress dialog
        int tsCount = workspaces.size();
        int tsNbr = 0;

        setProgressSteps(tsCount);

        // loop existing workspace directories
        for (String id : teamspacesDir.list()) {
            // loop received workspace items
            if (workspaces.containsKey(id)) {
                // remove entry from workspace list
                workspaces.remove(id);

                setMessage(Messages.getString("UpdateOperation.12") + " (" + ++tsNbr + " of " //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
                        + tsCount + ")"); //$NON-NLS-1$

                // update workspace
                updateWorkspace(new File(teamspacesDir.getAbsolutePath()
                        + "/" + id), id); //$NON-NLS-1$
            }
            updateProgress();
        }
        // add additional workspace directories
        for (String id : workspaces.keySet()) {
            setMessage(Messages.getString("UpdateOperation.12") + " (" + ++tsNbr + " of " //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
                    + tsCount + ")"); //$NON-NLS-1$

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
            MindClient.showErrorMessage(Messages
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
            MindClient.showErrorMessage(Messages
                    .getString("UpdateOperation.11") //$NON-NLS-1$
                    + " '" //$NON-NLS-1$
                    + id + "' (" //$NON-NLS-1$
                    + e.getMessage() + ")"); //$NON-NLS-1$
        }
    }
}
