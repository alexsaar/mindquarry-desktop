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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;

import com.mindquarry.desktop.client.Messages;
import com.mindquarry.desktop.client.MindClient;
import com.mindquarry.desktop.client.action.ActionBase;
import com.mindquarry.desktop.client.widget.workspace.WorkspaceBrowserWidget;
import com.mindquarry.desktop.model.team.Team;
import com.mindquarry.desktop.preferences.profile.Profile;
import com.mindquarry.desktop.workspace.SVNSynchronizer;
import com.mindquarry.desktop.workspace.exception.SynchronizeException;

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

    protected static final String SYNC_WORKSPACE_MESSAGE = Messages
        .getString("Synchronizing workspaces\n" +
        		"Please do not modify, copy, or move files\n" +
        		"in your workspace during the synchronization");

    public SynchronizeWorkspacesAction(MindClient client) {
        super(client);

        setId(ID);
        setActionDefinitionId(ID);

        setText(Messages.getString("Synchronize workspaces"));
        setToolTipText(Messages
                .getString("Synchronize workspaces with your desktop."));
        setAccelerator(SWT.CTRL + +SWT.SHIFT + 'S');
        setImageDescriptor(ImageDescriptor.createFromImage(IMAGE));
    }

    public void run() {
        Thread updateThread = new Thread(new Runnable() {
            public void run() {
                boolean cancelled = false;
                client.enableActions(false, ActionBase.WORKSPACE_ACTION_GROUP);
                workspaceWidget.updateContainer(true, Messages
                        .getString("Refreshing workspaces changes ..."), //$NON-NLS-1$
                        null, false);

                if (workspaceWidget.refreshNeeded()) {
                    client.startAction(Messages
                            .getString("Refreshing workspaces changes ..."));

                    Display.getDefault().syncExec(new Runnable() {
                        public void run() {
                            MessageBox messageBox = new MessageBox(client
                                    .getShell(), SWT.ICON_INFORMATION | SWT.OK);
                            messageBox
                                    .setMessage(Messages
                                            .getString("The list of changes is not up to date. It will be updated now. "
                                                    + "\n"
                                                    + "Please check the list of changes and press synchronize again."));
                            messageBox.open();
                        }
                    });
                    workspaceWidget.refresh();
                    client.stopAction(Messages
                            .getString("Refreshing workspaces changes ..."));
                }
                
                    workspaceWidget.updateContainer(true, SYNC_WORKSPACE_MESSAGE, //$NON-NLS-1$
                            null, false);
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
                        for (Team team : teams) {
                            SVNSynchronizer sc = new SVNSynchronizer(team
                                    .getWorkspaceURL(), selected
                                    .getWorkspaceFolder()
                                    + "/" + team.getName(), selected.getLogin(),
                                    selected.getPassword(),
                                    new InteractiveConflictHandler(client
                                            .getShell()));
                            sc.setCommitMessageHandler(new CommitMessageHandler(client.getShell(), team));
                            sc.synchronizeOrCheckout();
                        }
                        workspaceWidget.refresh();
                    } catch (SynchronizeCancelException e) {
                        log.info("synchronization cancelled (1)");
                        cancelled = true;
                    } catch (final Exception e) {
                        if (e.getCause() != null && e.getCause().getCause() != null &&
                                e.getCause().getCause().getClass() == SynchronizeCancelException.class) {
                            // cancel clicked in commit message dialog , don't show error:
                            log.info("synchronization cancelled (2)");
                            cancelled = true;
                        } else {
                            log.error(e.toString(), e);
                            cancelled = true;
                            Display.getDefault().syncExec(new Runnable() {
                                public void run() {
                                    MessageBox messageBox = new MessageBox(client
                                            .getShell(), SWT.ICON_ERROR | SWT.OK);
                                    messageBox.setMessage(Messages
                                            .getString("An error occured during synchronization: ") +
                                            e.getMessage());
                                    messageBox.open();
                                }
                            });                        
                        }
                    }
                    client.stopAction(Messages
                            .getString(SYNC_WORKSPACE_MESSAGE));

                if (cancelled) {
                    workspaceWidget.updateContainer(false, null, null, false);
                } else {
                    workspaceWidget.updateContainer(false, null, null, true);
                }
                client.enableActions(true, ActionBase.WORKSPACE_ACTION_GROUP);
            }
        }, "workspace-changes-update");
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
}
