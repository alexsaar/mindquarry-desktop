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

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import com.mindquarry.desktop.client.Messages;
import com.mindquarry.desktop.client.MindClient;
import com.mindquarry.desktop.client.action.ActionBase;
import com.mindquarry.desktop.client.action.workspace.SynchronizeWorkspacesAction.SyncThread;
import com.mindquarry.desktop.client.widget.team.TeamlistWidget;
import com.mindquarry.desktop.client.widget.workspace.WorkspaceBrowserWidget;
import com.mindquarry.desktop.workspace.exception.CancelException;

/**
 * Update list of SVN changes.
 * 
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class UpdateWorkspacesAction extends ActionBase {
    public static final String ID = UpdateWorkspacesAction.class
            .getSimpleName();

    private WorkspaceBrowserWidget workspaceWidget;

    private TeamlistWidget teamList;
    
    private Thread updateThread;

    private static final Image IMAGE = new Image(
            Display.getCurrent(),
            UpdateWorkspacesAction.class
                    .getResourceAsStream("/org/tango-project/tango-icon-theme/" + ICON_SIZE + "/actions/view-refresh.png")); //$NON-NLS-1$ //$NON-NLS-2$

    public UpdateWorkspacesAction(MindClient client) {
        super(client);

        setId(ID);
        setActionDefinitionId(ID);

        setText(Messages.getString("Refresh")); //$NON-NLS-1$
        setToolTipText(Messages.getString("Refresh the list of file changes")); //$NON-NLS-1$
        setAccelerator(SWT.CTRL + +SWT.SHIFT + 'U');
        setImageDescriptor(ImageDescriptor.createFromImage(IMAGE));
    }

    private static final String REFRESH_MESSAGE = Messages
            .getString("Refreshing workspaces changes"); //$NON-NLS-1$

    public void run() {
        try {
            teamList.refresh();
        } catch (CancelException e) {
            // TODO: better exception handling
            log.warn("Refreshing team list before updating workspaces cancelled.", e); //$NON-NLS-1$
            return;
        }
        updateThread = new Thread(new UpdateThread(), "workspace-changes-update");
        updateThread.setDaemon(true);
        updateThread.start();
    }
    
    class UpdateThread implements Runnable {
        public void run() {
            client.enableActions(false, ActionBase.WORKSPACE_ACTION_GROUP);
            client.enableActions(true, ActionBase.STOP_ACTION_GROUP);
            client.startAction(REFRESH_MESSAGE);

            workspaceWidget.showRefreshMessage(REFRESH_MESSAGE + " ..."); //$NON-NLS-1$
            workspaceWidget.refresh();
            if (workspaceWidget.hasCheckout()) {
                workspaceWidget.showEmptyMessage( 
                        workspaceWidget.isRefreshListEmpty());
            } else {
                workspaceWidget.showErrorMessage(
                        Messages.getString("You have not synchronized yet.\n" + //$NON-NLS-1$
                                "Click the 'Synchronize' button to " + //$NON-NLS-1$
                                "download files from the server.")); //$NON-NLS-1$
            }

            client.stopAction(REFRESH_MESSAGE);
            client.enableActions(true, ActionBase.WORKSPACE_ACTION_GROUP);
            client.enableActions(false, ActionBase.STOP_ACTION_GROUP);
        }
    }

    public String getGroup() {
        return ActionBase.WORKSPACE_ACTION_GROUP;
    }

    public boolean isToolbarAction() {
        return true;
    }

    public void setTeamList(TeamlistWidget teamList) {
        this.teamList = teamList;
    }

    public void setWorkspaceWidget(WorkspaceBrowserWidget workspaceWidget) {
        this.workspaceWidget = workspaceWidget;
    }
    
    public void stop() {
        if (updateThread != null && updateThread.isAlive()) {
            log.debug("Killing synchronize thread");
            // TODO: use non-deprecated way to stop threads: interrupt(); 
            updateThread.stop();
            workspaceWidget.showErrorMessage(Messages.getString("Refresh stopped."));
            client.stopAction(REFRESH_MESSAGE);
            client.enableActions(true, ActionBase.WORKSPACE_ACTION_GROUP);
            client.enableActions(false, ActionBase.STOP_ACTION_GROUP);
        }
    }
}
