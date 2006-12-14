/**
 * Copyright (C) 2005 MindQuarry GmbH, All Rights Reserved
 */
package com.mindquarry.client.workspace;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import com.mindquarry.client.MindClient;

/**
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class WorkspaceShareListener implements Listener {
    private final MindClient client;

    private final Button button;

    public WorkspaceShareListener(final MindClient client, Button button) {
        this.client = client;
        this.button = button;
    }

    /**
     * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
     */
    public void handleEvent(Event event) {
        button.setEnabled(false);
        
        try {
            // need to sync workspaces first (for merging, up-to-date working
            // copies and so on)
            SynchronizeOperation syncOp = new SynchronizeOperation(client);
            new ProgressMonitorDialog(MindClient.getShell()).run(true, true,
                    syncOp);
        } catch (Exception e) {
            MessageDialog.openError(MindClient.getShell(),
                    Messages.getString("WorkspaceShareListener.0"), //$NON-NLS-1$
                    Messages.getString("WorkspaceShareListener.1")); //$NON-NLS-1$
        }
        try {
            // share workspace changes
            ShareOperation shareOp = new ShareOperation(client);
            new ProgressMonitorDialog(MindClient.getShell()).run(true, true,
                    shareOp);
        } catch (Exception e) {
            MessageDialog.openError(MindClient.getShell(), Messages.getString("WorkspaceShareListener.2"), //$NON-NLS-1$
                    Messages.getString("WorkspaceShareListener.3")); //$NON-NLS-1$
        }
        button.setEnabled(true);
    }
}
