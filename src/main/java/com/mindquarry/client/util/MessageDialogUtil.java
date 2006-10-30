/**
 * Copyright (C) 2005 MindQuarry GmbH, All Rights Reserved
 */
package com.mindquarry.client.util;

import org.eclipse.jface.dialogs.MessageDialog;

import com.mindquarry.client.MindClient;


/**
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class MessageDialogUtil {
    public static void displaySyncErrorMsg(final String msg) {
        // show (asynchronously) error dialog
        MindClient.getShell().getDisplay().syncExec(new Runnable() {
            /**
             * @see java.lang.Runnable#run()
             */
            public void run() {
                MessageDialog.openError(MindClient.getShell(), "Error", msg);
            }
        });
    }
}
