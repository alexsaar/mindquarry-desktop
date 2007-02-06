/*
 * Copyright (C) 2006-2007 MindQuarry GmbH, All Rights Reserved
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
package com.mindquarry.client.workspace;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Widget;

import com.mindquarry.client.MindClient;

/**
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class WorkspaceSynchronizeListener implements Listener {
    private final MindClient client;

    private final Widget widget;

    public WorkspaceSynchronizeListener(final MindClient client, Widget widget) {
        this.client = client;
        this.widget = widget;
    }
    
    private static void setEnabled(Widget widget, boolean enable) {
        if (widget instanceof Control) {
            ((Control) widget).setEnabled(enable);
        } else if (widget instanceof MenuItem) {
            ((MenuItem) widget).setEnabled(enable);
        }        
    }

    /**
     * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
     */
    public void handleEvent(Event event) {
        if (client.getProfileList().selectedProfile() == null) {
            MessageDialog.openError(MindClient.getShell(), Messages
                    .getString("WorkspaceSynchronizeListener.2"), //$NON-NLS-1$
                    Messages.getString("WorkspaceSynchronizeListener.3")); //$NON-NLS-1$
            return;
        }
        
        setEnabled(widget, false);

        try {
            // need to sync workspaces first (for merging, up-to-date working
            // copies and so on)
            UpdateOperation syncOp = new UpdateOperation(client);
            new ProgressMonitorDialog(MindClient.getShell()).run(true, true,
                    syncOp);
        } catch (Exception e) {
            MessageDialog.openError(MindClient.getShell(), Messages
                    .getString("WorkspaceSynchronizeListener.0"), //$NON-NLS-1$
                    Messages.getString("WorkspaceSynchronizeListener.1")); //$NON-NLS-1$
        }
        try {
            // share workspace changes
            PublishOperation shareOp = new PublishOperation(client);
            new ProgressMonitorDialog(MindClient.getShell()).run(true, true,
                    shareOp);
        } catch (Exception e) {
            MessageDialog.openError(MindClient.getShell(), Messages
                    .getString("WorkspaceSynchronizeListener.0"), //$NON-NLS-1$
                    Messages.getString("WorkspaceSynchronizeListener.1")); //$NON-NLS-1$
        }
        
        setEnabled(widget, true);
    }
}
