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
package com.mindquarry.desktop.client.workspace.dialog;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.tigris.subversion.javahl.Status;

import com.mindquarry.desktop.client.Messages;

/**
 * Abstract dialog for resolving several kinds of working copy conflicts.
 * 
 * @author dnaber
 */
public abstract class AbstractConflictDialog extends TitleAreaDialog {

    protected Status remoteStatus;
    
    protected int resolveMethod;

    abstract protected String getMessage();

    abstract protected void createLowerDialogArea(Composite composite);

    public AbstractConflictDialog(Shell shell, Status remoteStatus, int defaultResolve) {
        super(shell);
        this.remoteStatus = remoteStatus;
        setBlockOnOpen(true);
        resolveMethod = defaultResolve;
        //TODO: limit width getShell().set
        shell.getBounds().width = 300;
        //shell.setBounds(0, 0, 200, 150);
    }

    protected Control createContents(Composite parent) {
        Control contents = super.createContents(parent);
        setTitle(Messages.getString(AbstractConflictDialog.class, "0")); //$NON-NLS-1$
        setMessage(getMessage(), IMessageProvider.INFORMATION);
        getShell().setText(Messages.getString(AbstractConflictDialog.class, "1")); //$NON-NLS-1$
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
        Label name = new Label(composite, SWT.READ_ONLY);
        name.setText(Messages.getString(AbstractConflictDialog.class, "2") + ": " + remoteStatus.getPath());

        Label barSeparator = new Label(composite, SWT.HORIZONTAL
                | SWT.SEPARATOR);
        barSeparator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        createLowerDialogArea(composite);
        
        composite = new Composite(composite, SWT.NONE);
        composite.setLayout(new GridLayout(1, false));
        ((GridLayout) composite.getLayout()).marginWidth = 17;

        return composite;
    }
    
    protected void makeRadioButton(Composite composite, String text, final int resolve) {
        Button button = new Button(composite, SWT.RADIO);
        button.setText(text);
        if (resolve == resolveMethod) {
            button.setSelection(true);
        }
        button.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
        button.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                resolveMethod = resolve;
            }
        });
    }

    /**
     * Getter for resolveMethod.
     * 
     * @return the resolveMethod
     */
    public int getResolveMethod() {
        return resolveMethod;
    }
    
}
