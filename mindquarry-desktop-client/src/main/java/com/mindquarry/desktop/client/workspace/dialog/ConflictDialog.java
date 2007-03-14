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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * Dialog for resolving working copy conflicts.
 * 
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class ConflictDialog extends TitleAreaDialog {
    private static String LOG_MSG_PREFIX = "Log message for remote version:"
            + "\n"; //$NON-NLS-1$

    private Button localCopy;

    private Button remoteCopy;

    private Label logMsg;

    public ConflictDialog(Shell shell) {
        super(shell);
        setBlockOnOpen(true);
    }

    @Override
    protected Control createContents(Composite parent) {
        Control contents = super.createContents(parent);

        setTitle("Resolve conflicts");
        setMessage(
                "Please select the version that should be treated as actual.",
                IMessageProvider.INFORMATION);
        getShell().setText("Resolving conflicts");
        return contents;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(1, false));
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

        // Build the separator line
        Label titleBarSeparator = new Label(composite, SWT.HORIZONTAL
                | SWT.SEPARATOR);
        titleBarSeparator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        localCopy = new Button(composite, SWT.RADIO);
        localCopy.setText("Local Version");
        localCopy.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));

        remoteCopy = new Button(composite, SWT.RADIO);
        remoteCopy.setText("Remote Version (Revision 12345)");

        composite = new Composite(composite, SWT.NONE);
        composite.setLayout(new GridLayout(1, false));
        ((GridLayout) composite.getLayout()).marginWidth = 17;

        logMsg = new Label(composite, SWT.LEFT);
        logMsg.setText(LOG_MSG_PREFIX + "the remote version");

        return composite;
    }
}
