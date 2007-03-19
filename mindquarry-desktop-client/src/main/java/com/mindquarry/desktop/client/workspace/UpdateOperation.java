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

import java.io.File;
import java.util.HashMap;
import java.util.List;

import org.tigris.subversion.javahl.ClientException;

import com.mindquarry.desktop.client.MindClient;
import com.mindquarry.desktop.client.workspace.widgets.SynchronizeWidget;
import com.mindquarry.desktop.preferences.profile.Profile;
import com.mindquarry.desktop.workspace.SVNHelper;

/**
 * Updates the local working copy of the workspaces.
 * 
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class UpdateOperation extends SvnOperation {
    private HashMap<String, String> workspaces;
    
    public UpdateOperation(final MindClient client,
            List<SynchronizeWidget> synAreas, HashMap<String, String> workspaces) {
        super(client, synAreas);
        this.workspaces = workspaces;
    }

    public void run() {
        resetProgress();
        updateWorkspaces();
        resetProgress();
    }

    private void updateWorkspaces() {
        Profile profile = Profile.getSelectedProfile(client
                .getPreferenceStore());

        // get directory for workspaces
        File wsHome = new File(profile.getWorkspaceFolder());

        // check if teamspace dir already exists, if not create it
        if (!wsHome.exists()) {
            wsHome.mkdirs();
        }
        // init progress steps for progress dialog
        int wsCount = workspaces.size();
        int wsNbr = 0;

        setProgressSteps(wsCount);

        // loop workspaces
        for (String id : workspaces.keySet()) {
            setMessage(Messages.getString("UpdateOperation.12") //$NON-NLS-1$
                    + " (" //$NON-NLS-1$
                    + ++wsNbr + " of " //$NON-NLS-1$
                    + wsCount + ")"); //$NON-NLS-1$

            // create directory for the new workspace
            File wsDir = new File(wsHome.getAbsolutePath() + "/" + id); //$NON-NLS-1$
            SVNHelper svnHelper = new JavaSVNHelper(workspaces.get(id), wsDir
                    .getAbsolutePath(), profile.getLogin(), profile
                    .getPassword());
            try {
                // TODO pipe local changes to commit operation
                svnHelper.getLocalChanges();
                
                svnHelper.update();
            } catch (ClientException e) {
                MindClient.showErrorMessage("Could not update workspace " + id);
                e.printStackTrace();
            }
            updateProgress();
        }
    }
}
