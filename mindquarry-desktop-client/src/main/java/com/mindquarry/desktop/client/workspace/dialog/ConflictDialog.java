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

import com.mindquarry.desktop.client.Messages;
import com.mindquarry.desktop.workspace.SVNHelper;

/**
 * Dialog for resolving working copy conflicts.
 * 
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class ConflictDialog extends TitleAreaDialog {
    private Button localCopy;

    private Button remoteCopy;

    private long remoteRevision;

    private int resolveMethod;

    public ConflictDialog(Shell shell, long remoteRevision) {
        super(shell);
        this.remoteRevision = remoteRevision;
        resolveMethod = SVNHelper.CONFLICT_OVERRIDE_FROM_WC;
        setBlockOnOpen(true);
    }

    /**
     * @see org.eclipse.jface.dialogs.TitleAreaDialog#createContents(org.eclipse.swt.widgets.Composite)
     */
    protected Control createContents(Composite parent) {
        Control contents = super.createContents(parent);

        setTitle(Messages.getString(ConflictDialog.class, "0")); //$NON-NLS-1$
        setMessage(Messages.getString(ConflictDialog.class, "1"), //$NON-NLS-1$
                IMessageProvider.INFORMATION);
        getShell().setText(Messages.getString(ConflictDialog.class, "2")); //$NON-NLS-1$
        return contents;
    }

    /**
     * @see org.eclipse.jface.dialogs.TitleAreaDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    protected Control createDialogArea(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(1, false));
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

        // build the separator line
        Label titleBarSeparator = new Label(composite, SWT.HORIZONTAL
                | SWT.SEPARATOR);
        titleBarSeparator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        localCopy = new Button(composite, SWT.RADIO);
        localCopy.setText(Messages.getString(ConflictDialog.class, "3")); //$NON-NLS-1$
        localCopy.setSelection(true);
        localCopy.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
        localCopy.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                resolveMethod = SVNHelper.CONFLICT_OVERRIDE_FROM_WC;
            }
        });
        remoteCopy = new Button(composite, SWT.RADIO);
        remoteCopy.setText(Messages.getString(ConflictDialog.class, "4") //$NON-NLS-1$
                + " (Revision " //$NON-NLS-1$
                + remoteRevision + ")"); //$NON-NLS-1$
        remoteCopy.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
        remoteCopy.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                resolveMethod = SVNHelper.CONFLICT_RESET_FROM_SERVER;
            }
        });
        composite = new Composite(composite, SWT.NONE);
        composite.setLayout(new GridLayout(1, false));
        ((GridLayout) composite.getLayout()).marginWidth = 17;

        return composite;
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
