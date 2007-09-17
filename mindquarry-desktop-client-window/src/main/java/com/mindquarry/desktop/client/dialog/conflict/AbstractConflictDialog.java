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
package com.mindquarry.desktop.client.dialog.conflict;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.mindquarry.desktop.client.Messages;
import com.mindquarry.desktop.client.dialog.DialogBase;

/**
 * Abstract dialog for resolving several kinds of working copy conflicts.
 * 
 * @author dnaber
 */
public abstract class AbstractConflictDialog extends DialogBase {

    protected Button okButton;
    protected Button cancelButton;

    // abstract methods
    
    abstract protected String getMessage();

    abstract protected void createLowerDialogArea(Composite composite);
    
    abstract protected void showFileInformation(Composite composite);

    // public methods
    
    public AbstractConflictDialog(Shell shell) {
        super(shell);
        setBlockOnOpen(true);
        setShellStyle(SWT.CLOSE | SWT.MIN | SWT.MAX | SWT.RESIZE);
    }

    // other methods

    protected Control createContents(Composite parent) {
        Control contents = super.createContents(parent);
        setTitle(getTitle());
        setMessage(getMessage(), IMessageProvider.INFORMATION);
        getShell().setText(Messages.getString("Resolving conflicts")); //$NON-NLS-1$
        getShell().setSize(600, getHeightHint());
        getShell().redraw();
        return contents;
    }

    protected Control createDialogArea(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(1, false));
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

        // build the separator line
        Label titleBarSeparator = new Label(composite, SWT.HORIZONTAL
                | SWT.SEPARATOR);
        titleBarSeparator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        // Show affected file information:
        showFileInformation(composite);

        Label barSeparator = new Label(composite, SWT.HORIZONTAL
                | SWT.SEPARATOR);
        barSeparator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        createLowerDialogArea(composite);
        
        composite = new Composite(composite, SWT.NONE);
        composite.setLayout(new GridLayout(1, false));
        ((GridLayout) composite.getLayout()).marginWidth = 17;

        return composite;
    }
        
    protected void createButtonsForButtonBar(Composite parent) {  
        okButton = createButton(parent, IDialogConstants.OK_ID,
                Messages.getString("OK"), true);  
        cancelButton = createButton(parent, IDialogConstants.CANCEL_ID,  
            Messages.getString("Cancel Synchronization"), false);  
    }
    
    protected void makeLabel(Composite composite, String text) {
        Label name = new Label(composite, SWT.READ_ONLY);
        name.setText(text);
    }

    protected String getTitle() {
        return Messages.getString("Resolve conflicts"); //$NON-NLS-1$
    }
    
    /**
     * Returns a hint as to the optimal height of the dialog.
     * TODO: Make it dynamic, i.e. infer height from the components in the UI.
     */
    protected int getHeightHint() {
        return 350;
    }
}
