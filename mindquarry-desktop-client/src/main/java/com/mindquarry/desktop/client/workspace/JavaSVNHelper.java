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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.tigris.subversion.javahl.NotifyAction;
import org.tigris.subversion.javahl.NotifyInformation;
import org.tigris.subversion.javahl.Status;

import com.mindquarry.desktop.client.Messages;
import com.mindquarry.desktop.client.MindClient;
import com.mindquarry.desktop.client.workspace.dialog.ConflictDialog;
import com.mindquarry.desktop.workspace.SVNHelper;

/**
 * Specilization of the {@link SVNHelper} for Java clients.
 * 
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class JavaSVNHelper extends SVNHelper {

    private Log log = LogFactory.getLog(this.getClass());

    private String commitMessage = ""; //$NON-NLS-1$

    private String commitInfo;

    private int conflictResolveMethod;

    public JavaSVNHelper(String repositoryURL, String localPath,
            String username, String password) {
        super(repositoryURL, localPath, username, password);
    }

    public void onNotify(NotifyInformation info) {
        switch (info.getAction()) {
        case NotifyAction.commit_added:
            log.info("SVN notify: added " + info.getPath()); //$NON-NLS-1$
            break;

        case NotifyAction.commit_deleted:
            log.info("SVN notify: deleted " + info.getPath()); //$NON-NLS-1$
            break;

        case NotifyAction.commit_modified:
            log.info("SVN notify: modified " + info.getPath()); //$NON-NLS-1$
            break;
        }
        log.info("SVN notify: modified " + info.getPath()); //$NON-NLS-1$
    }

    protected int resolveConflict(final Status status) {
        Display.getDefault().syncExec(new Runnable() {
            public void run() {
                // retrieve revision number
                long revision = status.getReposLastCmtRevisionNumber();
                if (revision == -1) {
                    revision = status.getRevisionNumber();
                }
                // show conflict dialog
                ConflictDialog dlg = new ConflictDialog(new Shell(), revision);
                if (dlg.open() == Window.OK) {
                    conflictResolveMethod = dlg.getResolveMethod();
                }
            }
        });
        return conflictResolveMethod;
    }

    public void setCommitInfo(String commitInfo) {
        this.commitInfo = commitInfo;
    }

    protected String getCommitMessage() {
        // retrieve (asynchronously) commit message
        final InputDialog dlg = new InputDialog(MindClient.getShell(), Messages
                .getString("com.mindquarry.desktop.client.83"), commitInfo, //$NON-NLS-1$
                Messages.getString("com.mindquarry.desktop.client.84"), null); //$NON-NLS-1$

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
