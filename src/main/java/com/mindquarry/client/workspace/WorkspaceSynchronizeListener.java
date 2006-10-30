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
public class WorkspaceSynchronizeListener implements Listener {
    private final MindClient client;

    private final Button button;

    public WorkspaceSynchronizeListener(final MindClient client, Button button) {
        this.client = client;
        this.button = button;
    }

    /**
     * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
     */
    public void handleEvent(Event event) {
        button.setEnabled(false);

        try {
            SynchronizeOperation syncOp = new SynchronizeOperation(client);
            new ProgressMonitorDialog(MindClient.getShell()).run(true, true,
                    syncOp);
        } catch (Exception e) {
            MessageDialog.openError(MindClient.getShell(),
                    "Synchronzation Error",
                    "Error during workspaces synchronization.");
        }
        button.setEnabled(true);
    }
}
