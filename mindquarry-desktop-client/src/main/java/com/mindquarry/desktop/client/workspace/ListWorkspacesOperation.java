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
package com.mindquarry.desktop.client.workspace;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

import com.mindquarry.desktop.client.MindClient;
import com.mindquarry.desktop.client.teamspace.TeamspaceUtilities;
import com.mindquarry.desktop.client.util.network.HttpUtilities;
import com.mindquarry.desktop.client.workspace.widgets.SynchronizeWidget;
import com.mindquarry.desktop.client.workspace.xml.TeamspaceTransformer;
import com.mindquarry.desktop.preferences.profile.Profile;

/**
 * Updates the local working copy of the workspaces.
 * 
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class ListWorkspacesOperation extends SvnOperation {
    private HashMap<String, String> workspaces;

    public ListWorkspacesOperation(final MindClient client,
            List<SynchronizeWidget> synAreas) {
        super(client, synAreas);
    }

    public void run() {
        resetProgress();
        workspaces = new HashMap<String, String>();
        if (!getTeamspaceList(workspaces)) {
            resetProgress();
            return;
        }
        resetProgress();
    }

    private boolean getTeamspaceList(HashMap<String, String> teamspaces) {
        setMessage(Messages.getString("ListWorkspacesOperation.0")); //$NON-NLS-1$
        Profile selectedProfile = Profile.getSelectedProfile(client
                .getPreferenceStore());

        List<String> teamspaceList;
        try {
            teamspaceList = TeamspaceUtilities
                    .getTeamspaceNamesForProfile(selectedProfile);
        } catch (Exception e) {
            MindClient
                    .showErrorMessage(Messages.getString("ListWorkspacesOperation.3")); //$NON-NLS-1$
            return false;
        }
        // init progress steps for progress dialog
        int tsCount = teamspaceList.size();
        int tsNbr = 0;
        setProgressSteps(tsCount + 1);
        updateProgress();

        // loop teamspace descriptions
        for (String tsID : teamspaceList) {
            setMessage(Messages.getString("ListWorkspacesOperation.4") //$NON-NLS-1$
                    + " (" //$NON-NLS-1$
                    + ++tsNbr + " of " //$NON-NLS-1$
                    + tsCount + ")..."); //$NON-NLS-1$

            InputStream content = null;
            try {
                content = HttpUtilities.getContentAsXML(selectedProfile
                        .getLogin(), selectedProfile.getPassword(),
                        selectedProfile.getServerURL()
                                + "/teams/team/" + tsID + "/"); //$NON-NLS-1$ //$NON-NLS-2$
            } catch (Exception e) {
                MindClient.showErrorMessage(Messages
                        .getString("ListWorkspacesOperation.1") //$NON-NLS-1$
                        + tsID
                        + Messages.getString("ListWorkspacesOperation.2")); //$NON-NLS-1$
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

    public HashMap<String, String> getWorkspaces() {
        return workspaces;
    }
}
