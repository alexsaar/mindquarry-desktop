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

package com.mindquarry.desktop.client.action.workspace;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.widgets.Shell;
import org.tigris.subversion.javahl.CommitItem;
import org.tigris.subversion.javahl.CommitMessage;

import com.mindquarry.desktop.client.Messages;
import com.mindquarry.desktop.model.team.Team;

public class CommitMessageHandler implements CommitMessage {

    private String commitMessage = "";

    private Team team;

    private Shell shell;

    public CommitMessageHandler(Shell shell, Team team) {
        this.team = team;
        this.shell = shell;
    }

    public String getLogMessage(CommitItem[] elementsToBeCommited) {
        // TODO: show all elements to be commited
    	// TODO: show multiline dialog
        final InputDialog dlg = new InputDialog(shell,
                Messages.getString("Commit message"),
                // TODO: add argument to i18n class:
                Messages.getString("Please describe your changes for team ") + team + ":",
                "",     // the default commit message
                null);

        shell.getDisplay().syncExec(new Runnable() {
            public void run() {
                dlg.setBlockOnOpen(true);
                if (dlg.open() == Dialog.OK) {
                    setCommitMessage(dlg.getValue());
                } else {
                    //setCommitMessage(null);
                    throw new SynchronizeCancelException();
                }
            }
        });
        return commitMessage;
    }

    private void setCommitMessage(String message) {
        commitMessage = message;
    }
    
}
