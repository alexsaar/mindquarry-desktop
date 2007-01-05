/**
 * Copyright (C) 2005 MindQuarry GmbH, All Rights Reserved
 */
package com.mindquarry.client.util.widgets;

import org.eclipse.jface.dialogs.MessageDialog;

import com.mindquarry.client.MindClient;

/**
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class MessageDialogUtil {
    /**
     * Show (synchronously) error dialog
     * 
     * @param msg the message to be displayed
     */
    public static void displaySyncErrorMsg(final String msg) {
    	System.out.println("Trying to write an error message");
        MindClient.getShell().getDisplay().syncExec(new Runnable() {
            /**
             * @see java.lang.Runnable#run()
             */
            public void run() {
                MessageDialog.openError(MindClient.getShell(), Messages
                        .getString("MessageDialogUtil.0"), msg); //$NON-NLS-1$
            }
        });
    }
}
