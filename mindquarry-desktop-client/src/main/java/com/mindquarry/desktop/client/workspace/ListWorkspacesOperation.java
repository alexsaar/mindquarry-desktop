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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.mindquarry.desktop.client.MindClient;
import com.mindquarry.desktop.model.team.Team;
import com.mindquarry.desktop.model.team.TeamList;
import com.mindquarry.desktop.preferences.profile.Profile;

/**
 * Updates the local working copy of the workspaces.
 * 
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class ListWorkspacesOperation extends SvnOperation {
    private Log log;

    private HashMap workspaces;

    public ListWorkspacesOperation(final MindClient client, List synAreas) {
        super(client, synAreas);
        log = LogFactory.getLog(ListWorkspacesOperation.class);
    }

    public void run() {
        resetProgress();
        workspaces = new HashMap();
        if (!getTeamspaceList(workspaces)) {
            resetProgress();
            return;
        }
        resetProgress();
    }

    private boolean getTeamspaceList(HashMap teamspaces) {
        setMessage("Retrieving teamspace list...");
        log.info("Retrieving teamspace list for workspace synchronization..."); //$NON-NLS-1$

        Profile profile = Profile.getSelectedProfile(client
                .getPreferenceStore());

        TeamList teamList = null;
        try {
            teamList = new TeamList(profile.getServerURL() + "/teams", //$NON-NLS-1$
                    profile.getLogin(), profile.getPassword());
        } catch (Exception e) {
            client
                    .showMessage("Error",
                            "Could not retrieve list of tasks for workspace synchronization.");
            log.error("Could not retrieve list of tasks " //$NON-NLS-1$
                    + "for workspace synchronization.", e); //$NON-NLS-1$
            return false;
        }
        List teams = teamList.getTeams();

        // loop teamspace descriptions
        Iterator tIt = teams.iterator();
        while (tIt.hasNext()) {
            Team team = (Team) tIt.next();
            workspaces.put(team.getName(), team.getWorkspaceURL());
            log.info("Added workspace from " + team.getWorkspaceURL()); //$NON-NLS-1$
        }
        return true;
    }

    public HashMap getWorkspaces() {
        return workspaces;
    }
}
