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
package com.mindquarry.desktop.client.dialog;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.HelpEvent;
import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Shell;

/**
 * Base class for all dialogs.
 * 
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public abstract class DialogBase extends TitleAreaDialog {
    public DialogBase(Shell shell) {
        super(shell);
        
        shell.addHelpListener(new HelpListener() {
            public void helpRequested(HelpEvent e) {
                Program.launch(getHelpURL());
            }
        });
        // TODO uncomment this once detailed help is available
//        setHelpAvailable(true);
//        setDialogHelpAvailable(true);
        
        setShellStyle(SWT.RESIZE);
        setBlockOnOpen(true);
    }
    
    protected void setValid() {
        setErrorMessage(null);
        getButton(IDialogConstants.OK_ID).setEnabled(true);
    }

    protected void setInvalid(String message) {
        setErrorMessage(message);
        getButton(IDialogConstants.OK_ID).setEnabled(false);
    }
    
    protected abstract String getHelpURL();
}
