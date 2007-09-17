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

import org.apache.commons.io.FilenameUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.mindquarry.desktop.client.Messages;
import com.mindquarry.desktop.workspace.conflict.AddConflict;
import com.mindquarry.desktop.workspace.conflict.AddConflict.Action;

/**
 * Dialog for resolving add/modification conflicts.
 * 
 * @author dnaber
 */
public class AddConflictDialog extends RenamingConflictDialog {

    private Action resolveMethod;

    protected Text newNameField;

    private static final Action DEFAULT_RESOLUTION = Action.RENAME;

    public AddConflictDialog(AddConflict conflict, Shell shell) {
        super(conflict, shell);
        resolveMethod = DEFAULT_RESOLUTION;
    }

    protected void showFileInformation(Composite composite) {
        Label name = new Label(composite, SWT.READ_ONLY);
        name.setText(Messages.getString("Filename(s)") + ": "
                + conflict.getStatus().getPath());
    }

    @Override
    protected String getMessage() {
        return Messages.getString("This case can occur if you created a file " +
                "or renamed an existing file and someone else created a file " +
                "with the same name.");
    }
    
    @Override
    protected String getTitle() {
        return Messages.getString(
                "The file you are trying to synchronize already exists on the server"); //$NON-NLS-1$
    }

    @Override
    protected void createLowerDialogArea(Composite composite) {
        Composite subComposite = new Composite(composite, SWT.NONE);
        subComposite.setLayout(new GridLayout(2, false));
        Button button1 = makeRadioButton(subComposite, Messages
                .getString("Rename file and upload it using a new name:"), //$NON-NLS-1$
                Action.RENAME);
        button1.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                newNameField.setEnabled(true);
            }
        });

        newNameField = createNewNameField(subComposite, FilenameUtils.getName(conflict.getStatus().getPath()));        
        newNameField.setFocus();

        // no replace available because it is a dangerous, unrecoverable action
        /*
         * Button button2 = makeRadioButton(subComposite,
         * //Messages.getString("Overwrite the file on the server with your
         * local version"), //$NON-NLS-1$ //Action.REPLACE);
         * Messages.getString("Overwrite your local file with the one from the
         * server"), //$NON-NLS-1$ Action.REPLACE);
         * button2.addListener(SWT.Selection, new Listener() { public void
         * handleEvent(Event event) { newNameField.setEnabled(false);
         * okButton.setEnabled(true); } });
         */
    }

    protected String getHelpURL() {
        // TODO fix help URL
        return "http://www.mindquarry.com/";
    }

    protected Button makeRadioButton(Composite composite, String text,
            final Action action) {
        Button button = new Button(composite, SWT.RADIO);
        button.setText(text);
        if (action == DEFAULT_RESOLUTION) {
            button.setSelection(true);
        }
        button.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
        button.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                resolveMethod = action;
            }
        });
        return button;
    }

    public Action getResolveMethod() {
        return resolveMethod;
    }

}
