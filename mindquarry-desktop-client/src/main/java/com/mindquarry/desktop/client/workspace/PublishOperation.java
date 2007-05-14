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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.tigris.subversion.javahl.Status;

import com.mindquarry.desktop.client.MindClient;
import com.mindquarry.desktop.preferences.profile.Profile;

/**
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class PublishOperation extends SvnOperation {
    private HashMap workspaces;

    public PublishOperation(final MindClient client, List synAreas,
            HashMap workspaces) {
        super(client, synAreas);
        this.workspaces = workspaces;
    }

    public void run() {
        resetProgress();
        setMessage("Checking workspace changes...");

        Profile profile = Profile.getSelectedProfile(client
                .getPreferenceStore());

        // get directory for workspaces
        File wsHome = new File(profile.getWorkspaceFolder());

        // init progress steps for progress dialog
        final int wsCount = workspaces.keySet().size();
        setProgressSteps(wsCount);
        int wsNbr = 0;

        Iterator idIt = workspaces.keySet().iterator();
        while (idIt.hasNext()) {
            String id = (String) idIt.next();
            setMessage("Publishing workspace" + " (" //$NON-NLS-2$
                    + ++wsNbr + " of " //$NON-NLS-1$
                    + wsCount + ")"); //$NON-NLS-1$

            File wsDir = new File(wsHome.getAbsolutePath() + "/" //$NON-NLS-1$ 
                    + id);
            JavaSVNHelper svnHelper = new JavaSVNHelper((String) workspaces
                    .get(id), wsDir.getAbsolutePath(), profile.getLogin(),
                    profile.getPassword());
            try {
                Status[] changes = svnHelper.getLocalChanges();
                List changedPaths = new ArrayList();

                StringBuffer commitInfo = new StringBuffer();
                commitInfo
                        .append("Please provide a short description of the changes you have made to workspace"
                                + id + ":\n\n"); //$NON-NLS-1$
                commitInfo
                        .append(getStatiDescription(changes, wsDir.getPath()));

                for (int i = 0; i < changes.length; i++) {
                    Status change = changes[i];
                    changedPaths.add(change.getPath());
                }
                if (changedPaths.size() > 0) {
                    svnHelper.setCommitInfo(commitInfo.toString());
                    svnHelper.commit((String[]) changedPaths
                            .toArray(new String[0]));
                }
            } catch (Exception e) {
                client.showMessage("Error",
                        "Could not publish workspace changes " + id);
                log.error("Could not publish workspace changes " //$NON-NLS-1$
                        + id, e);
            }
            updateProgress();
        }
        resetProgress();
    }
}
