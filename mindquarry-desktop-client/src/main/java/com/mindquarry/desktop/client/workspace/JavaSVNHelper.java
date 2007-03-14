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

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.InputDialog;
import org.tigris.subversion.javahl.NotifyInformation;
import org.tigris.subversion.javahl.Status;

import com.mindquarry.desktop.client.MindClient;
import com.mindquarry.desktop.workspace.SVNHelper;

/**
 * Specilization of the {@link SVNHelper} for Java clients.
 * 
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class JavaSVNHelper extends SVNHelper {
    private String commitMessage = ""; //$NON-NLS-1$
    
    private String commitInfo;

    public JavaSVNHelper(String repositoryURL, String localPath,
            String username, String password) {
        super(repositoryURL, localPath, username, password);
    }

    public void onNotify(NotifyInformation info) {
        System.out.println("java notify: " + info.getPath()); //$NON-NLS-1$
    }

    protected int resolveConflict(Status status) {
        return CONFLICT_OVERRIDE_FROM_WC;
    }

    public void setCommitInfo(String commitInfo) {
        this.commitInfo = commitInfo;
    }

    protected String getCommitMessage() {
        // retrieve (asynchronously) commit message
        final InputDialog dlg = new InputDialog(MindClient.getShell(), Messages
                .getString("JavaSVNHelper.1"), //$NON-NLS-1$
                commitInfo, Messages.getString("JavaSVNHelper.3"), null); //$NON-NLS-1$

        MindClient.getShell().getDisplay().syncExec(new Runnable() {
            public void run() {
                dlg.setBlockOnOpen(true);
                if (dlg.open() == Dialog.OK) {
                    setCommitMessage(dlg.getValue());
                }
            }
        });
        return commitMessage;
    }
    
    private void setCommitMessage(String message) {
        commitMessage = message;
    }
}
