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

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.tigris.subversion.javahl.Notify2;
import org.tigris.subversion.javahl.NotifyInformation;
import org.tigris.subversion.javahl.Status;
import org.tigris.subversion.javahl.StatusKind;

import com.mindquarry.desktop.client.Messages;
import com.mindquarry.desktop.client.MindClient;
import com.mindquarry.desktop.client.action.ActionBase;
import com.mindquarry.desktop.client.dialog.workspace.CommitDialog;
import com.mindquarry.desktop.client.widget.workspace.ChangeSet;
import com.mindquarry.desktop.client.widget.workspace.ChangeSets;
import com.mindquarry.desktop.client.widget.workspace.WorkspaceBrowserWidget;
import com.mindquarry.desktop.model.team.Team;
import com.mindquarry.desktop.preferences.profile.Profile;
import com.mindquarry.desktop.workspace.SVNSynchronizer;

/**
 * Trigger workspace synchronization, i.e. SVN update + commit.
 * 
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class SynchronizeWorkspacesAction extends ActionBase {
    public static final String ID = SynchronizeWorkspacesAction.class
            .getSimpleName();

    private WorkspaceBrowserWidget workspaceWidget;

    private static final Image IMAGE = new Image(
            Display.getCurrent(),
            SynchronizeWorkspacesAction.class
                    .getResourceAsStream("/com/mindquarry/icons/" + ICON_SIZE + "/actions/synchronize-vertical.png")); //$NON-NLS-1$

    protected static final String SYNC_WORKSPACE_MESSAGE =
        Messages.getString("Synchronizing workspaces"); //$NON-NLS-1$
    protected static final String SYNC_WORKSPACE_NOTE = Messages.getString(
            "Please do not modify, copy, or move files " + //$NON-NLS-1$
            "in your workspace during the synchronization."); //$NON-NLS-1$

    protected static final String SYNC_WORKSPACE_NOTE2 = Messages.getString(
            "Currently working on: ");  //$NON-NLS-1$

    private static final String REFRESHING_MESSAGE = Messages
            .getString("Refreshing workspaces changes");

    private Thread updateThread;

    public SynchronizeWorkspacesAction(MindClient client) {
        super(client);

        setId(ID);
        setActionDefinitionId(ID);

        setText(Messages.getString("Synchronize")); //$NON-NLS-1$
        setToolTipText(Messages
                .getString("Synchronize workspaces with your desktop.")); //$NON-NLS-1$
        setAccelerator(SWT.CTRL + +SWT.SHIFT + 'S');
        setImageDescriptor(ImageDescriptor.createFromImage(IMAGE));
    }

    public void run() {
        updateThread = new Thread(new SyncThread(), "workspace-synchronise");
        updateThread.setDaemon(true);
        updateThread.start();
    }

    public String getGroup() {
        return ActionBase.WORKSPACE_ACTION_GROUP;
    }

    public boolean isToolbarAction() {
        return true;
    }

    public void setWorkspaceWidget(WorkspaceBrowserWidget workspaceWidget) {
        this.workspaceWidget = workspaceWidget;
    }
    
    public void stop() {
        if (updateThread != null && updateThread.isAlive()) {
            log.debug("Killing synchronize thread");
            // TODO: use non-deprecated way to stop threads: interrupt(); 
            updateThread.stop();
            workspaceWidget.showErrorMessage(Messages.getString("Synchronisation stopped."));
            client.stopAction(REFRESHING_MESSAGE);
            client.stopAction(SYNC_WORKSPACE_MESSAGE);
            client.enableActions(true, ActionBase.WORKSPACE_ACTION_GROUP);
            client.enableActions(false, ActionBase.STOP_ACTION_GROUP);
        }
    }

    class NotifyListener implements Notify2 {

        public void onNotify(NotifyInformation info) {
            workspaceWidget.setUpdateMessage(SYNC_WORKSPACE_NOTE2 +
                    "\n" + info.getPath()); //$NON-NLS-1$
        }
        
    }

    class SyncThread implements Runnable {

        public void run() {
            boolean cancelled = false;
            client.enableActions(false, ActionBase.WORKSPACE_ACTION_GROUP);
            client.enableActions(true, ActionBase.STOP_ACTION_GROUP);

            workspaceWidget.showRefreshMessage(
                    Messages.getString("Refreshing workspaces changes") + " ..."); //$NON-NLS-1$ //$NON-NLS-2

            client.startAction(REFRESHING_MESSAGE); //$NON-NLS-1$
            boolean refreshNeeded = workspaceWidget.refreshNeeded(true);
            client.stopAction(REFRESHING_MESSAGE); //$NON-NLS-1$

            if (refreshNeeded) {
                Display.getDefault().syncExec(new Runnable() {
                    public void run() {
                        MessageBox messageBox = new MessageBox(client
                                .getShell(), SWT.ICON_INFORMATION | SWT.OK);
                        messageBox
                                .setMessage(Messages
                                        .getString("The list of changes is not up to date. It will be updated now. " //$NON-NLS-1$
                                                + "\n" //$NON-NLS-1$
                                                + "Please check the list of changes and press synchronize again.")); //$NON-NLS-1$
                        messageBox.open();
                    }
                });
                workspaceWidget.showEmptyMessage(workspaceWidget.isRefreshListEmpty());
            } else {
                workspaceWidget.showRefreshMessage(SYNC_WORKSPACE_MESSAGE
                        + " ...\n" + SYNC_WORKSPACE_NOTE); //$NON-NLS-1$
                client.startAction(SYNC_WORKSPACE_MESSAGE);

                // retrieve selected profile
                PreferenceStore store = client.getPreferenceStore();
                Profile selected = Profile.getSelectedProfile(store);
                if (selected == null) {
                    log.debug("No profile selected."); //$NON-NLS-1$
                    return;
                }

                // retrieve selected teams
                final List<Team> teams = new ArrayList<Team>();
                Display.getDefault().syncExec(new Runnable() {
                    public void run() {
                        teams.addAll(client.getSelectedTeams());
                    }
                });
                try {
                    // ask the user for the message(s) and remember it, this
                    // way he can let the sync run in the background:
                    final Map<Team,String> commitMessages = new HashMap<Team, String>();
                    ChangeSets changeSets = workspaceWidget.getChangeSets();
                    for (final ChangeSet changeSet : changeSets.getList()) {
                        // TODO: clean up, this is only needed because the list
                        // returned by getLocalChanges() does actually also contain
                        // remote changes:
                        if (!hasLocalChanges(changeSet)) {
                            continue;
                        }
                        Display.getDefault().syncExec(new Runnable() {
                            public void run() {
                                CommitDialog dlg = new CommitDialog(client.getShell(), changeSet);
                                int result = dlg.open();
                                if (result == IDialogConstants.OK_ID) {
                                    commitMessages.put(changeSet.getTeam(), dlg.getCommitMessage());
                                } else {
                                    throw new SynchronizeCancelException();
                                }
                            }
                        });
                    }
                    // now actually update and commit for each team:
                    for (Team team : teams) {
                        long startTime = System.currentTimeMillis();
                        SVNSynchronizer sc = new SVNSynchronizer(team
                                .getWorkspaceURL(), selected
                                .getWorkspaceFolder()
                                + "/" + team.getName(), selected.getLogin(), //$NON-NLS-1$
                                selected.getPassword(),
                                new InteractiveConflictHandler(client
                                        .getShell()));
                        sc.setNotifyListener(new NotifyListener());
                        sc.setCommitMessageHandler(new CommitMessageHandler(commitMessages.get(team)));
                        sc.synchronizeOrCheckout();
                        log.debug("synchronizeOrCheckout for team '" + team + "' took " +
                                (System.currentTimeMillis()-startTime) + "ms (incl. user interaction if any)");
                    }
                } catch (SynchronizeCancelException e) {
                    log.info("synchronization cancelled (1)"); //$NON-NLS-1$
                    cancelled = true;
                } catch (final Exception e) {
                    if (e.getCause() != null && e.getCause().getCause() != null &&
                            e.getCause().getCause().getClass() == SynchronizeCancelException.class) {
                        // cancel clicked in commit message dialog , don't show error:
                        log.info("synchronization cancelled (2)"); //$NON-NLS-1$
                        cancelled = true;
                    } else if (e.getCause() != null
                            && e.getCause().getClass() == SynchronizeCancelException.class) {
                        // cancel clicked in content conflict dialog, don't show error:
                        log.info("synchronization cancelled (3)"); //$NON-NLS-1$
                        cancelled = true;
                    } else {
                        log.error(e.toString(), e);
                        cancelled = true;
                        Display.getDefault().syncExec(new Runnable() {
                            public void run() {
                                MessageBox messageBox = new MessageBox(client
                                        .getShell(), SWT.ICON_ERROR | SWT.OK);
                                messageBox.setMessage(Messages
                                        .getString("An error occured during synchronization: ") + //$NON-NLS-1$
                                        e.getMessage());
                                messageBox.open();
                            }
                        });                        
                    }
                }
                client.stopAction(SYNC_WORKSPACE_MESSAGE);

                if (cancelled) {
                    // show list of file changes
                    workspaceWidget.showEmptyMessage(false);
                } else {
                    // show "sucessfully synchronized"
                    workspaceWidget.showEmptyMessage(Messages.getString(
                            "Synchronized successfully at ") //$NON-NLS-1$
                            + DateFormat.getTimeInstance().format(new Date()) + "."); //$NON-NLS-1$
                }
            }
            client.enableActions(true, ActionBase.WORKSPACE_ACTION_GROUP);
            client.enableActions(false, ActionBase.STOP_ACTION_GROUP);
        }

        private boolean hasLocalChanges(ChangeSet changeSet) {
            for (File file : changeSet.getChanges().keySet()) {
                Status status = changeSet.getChanges().get(file).getStatus();
                // TODO: better check for local changes
                // Note: At this point, when dirs/files are added, they are
                // 'unversioned', but they will be added later (which is a local
                // change).
                if (status.getTextStatus() != StatusKind.none &&
                        status.getTextStatus() != StatusKind.normal) {
                    return true;
                }
            }
            return false;
        }
    }
}
