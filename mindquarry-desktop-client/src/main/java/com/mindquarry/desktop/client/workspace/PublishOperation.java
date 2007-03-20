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
import java.util.List;

import org.tigris.subversion.javahl.ClientException;
import org.tigris.subversion.javahl.Status;

import com.mindquarry.desktop.client.MindClient;
import com.mindquarry.desktop.client.workspace.widgets.SynchronizeWidget;
import com.mindquarry.desktop.preferences.profile.Profile;

/**
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class PublishOperation extends SvnOperation {
    private HashMap<String, String> workspaces;

    public PublishOperation(final MindClient client,
            List<SynchronizeWidget> synAreas, HashMap<String, String> workspaces) {
        super(client, synAreas);
        this.workspaces = workspaces;
    }

    public void run() {
        resetProgress();
        setMessage(Messages.getString("PublishOperation.0")); //$NON-NLS-1$

        Profile profile = Profile.getSelectedProfile(client
                .getPreferenceStore());

        // get directory for workspaces
        File wsHome = new File(profile.getWorkspaceFolder());

        // init progress steps for progress dialog
        final int wsCount = workspaces.keySet().size();
        setProgressSteps(wsCount);
        int wsNbr = 0;

        for (String id : workspaces.keySet()) {
            setMessage(Messages.getString("PublishOperation.4") + " (" //$NON-NLS-1$//$NON-NLS-2$
                    + ++wsNbr + " of " //$NON-NLS-1$
                    + wsCount + ")"); //$NON-NLS-1$

            File wsDir = new File(wsHome.getAbsolutePath() + "/" //$NON-NLS-1$ 
                    + id);
            JavaSVNHelper svnHelper = new JavaSVNHelper(workspaces.get(id),
                    wsDir.getAbsolutePath(), profile.getLogin(), profile
                            .getPassword());
            try {
                Status[] changes = svnHelper.getLocalChanges();
                List<String> changedPaths = new ArrayList<String>();

                StringBuffer commitInfo = new StringBuffer();
                commitInfo.append(Messages.getString("PublishOperation.2") //$NON-NLS-1$
                        + id + ":\n\n"); //$NON-NLS-1$
                commitInfo
                        .append(getStatiDescription(changes, wsDir.getPath()));

                for (Status change : changes) {
                    changedPaths.add(change.getPath());
                }
                if (changedPaths.size() > 0) {
                    svnHelper.setCommitInfo(commitInfo.toString());
                    svnHelper.commit(changedPaths.toArray(new String[0]));
                }
            } catch (ClientException e) {
                MindClient
                        .showErrorMessage("Could not publish workspace changes "
                                + id);
                e.printStackTrace();
            }
            updateProgress();
        }
        resetProgress();
    }
}
