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
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import com.mindquarry.desktop.client.Messages;
import com.mindquarry.desktop.client.MindClient;
import com.mindquarry.desktop.client.action.ActionBase;
import com.mindquarry.desktop.client.action.task.SynchronizeTasksAction;

/**
 * Stops all running actions (synchronize workspace, workspace refresh, task refresh).
 * 
 * @author dnaber
 */
public class StopAction extends ActionBase {
    public static final String ID = StopAction.class
            .getSimpleName();

    private static final Image IMAGE = new Image(
            Display.getCurrent(),
            StopAction.class
                    .getResourceAsStream("/org/tango-project/tango-icon-theme/" + ICON_SIZE + "/actions/process-stop.png")); //$NON-NLS-1$ //$NON-NLS-2$

    public StopAction(MindClient client) {
        super(client);

        setId(ID);
        setActionDefinitionId(ID);

        setText(Messages.getString("Stop")); //$NON-NLS-1$
        setToolTipText(Messages.getString("Stop the currently running actions")); //$NON-NLS-1$
        //setAccelerator(SWT.CTRL + SWT.SHIFT + 'S');
        setImageDescriptor(ImageDescriptor.createFromImage(IMAGE));
    }

    public void run() {
        SynchronizeWorkspacesAction syncAction = (SynchronizeWorkspacesAction) 
            client.getAction(SynchronizeWorkspacesAction.class.getName());
        syncAction.stop();
        UpdateWorkspacesAction updateAction = (UpdateWorkspacesAction) 
            client.getAction(UpdateWorkspacesAction.class.getName());
        updateAction.stop();
        SynchronizeTasksAction syncTasksAction = (SynchronizeTasksAction) 
            client.getAction(SynchronizeTasksAction.class.getName());
        syncTasksAction.stop();
    }

    public boolean isEnabledByDefault() {
        return false;
    }

    public String getGroup() {
        return ActionBase.STOP_ACTION_GROUP;
    }

    public boolean isToolbarAction() {
        return true;
    }

}
