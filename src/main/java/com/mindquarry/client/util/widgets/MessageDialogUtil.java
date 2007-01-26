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
