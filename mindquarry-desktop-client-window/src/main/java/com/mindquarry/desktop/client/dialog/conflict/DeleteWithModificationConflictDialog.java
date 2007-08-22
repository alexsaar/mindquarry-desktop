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

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import com.mindquarry.desktop.client.Messages;
import com.mindquarry.desktop.workspace.conflict.DeleteWithModificationConflict;
import com.mindquarry.desktop.workspace.conflict.DeleteWithModificationConflict.Action;

/**
 * Dialog for resolving delete/modification conflicts.
 * 
 * @author dnaber
 */
public class DeleteWithModificationConflictDialog extends AbstractConflictDialog {

    private DeleteWithModificationConflict conflict;
    
    private Action defaultResolution;

    protected Action resolveMethod;

    public DeleteWithModificationConflictDialog(DeleteWithModificationConflict conflict, Shell shell) {
        super(shell);
        // Although you can get old versions from svn, don't offer deletion
        // as default, as a common user might not be able to restore a file:
        defaultResolution = Action.REVERTDELETE;
        resolveMethod = defaultResolution;
        this.conflict = conflict;
    }

    @Override
    protected String getMessage() {
        if (conflict.isLocalDelete()) {
            return Messages.getString("You are trying to delete a file or directory " +
                "which someone else modified on the server.");
        } else {
            return Messages.getString("You are trying to upload a file to the " +
                "server which someone else has already deleted on the server.");
        }
    }

    protected void createLowerDialogArea(Composite composite) {
        if (conflict.isLocalDelete()) {
            makeLabel(composite, Messages.getString(Messages.getString("The file from the server " +
                "will be downloaded.")));
            /*makeRadioButton(composite, Messages.getString(Messages.getString("Do not delete " +
            	"the file, and download the version from the server.")),
                Action.REVERTDELETE);*/
            /*makeRadioButton(composite, Messages.getString(Messages.getString("Ignore the " +
                "modification and delete the file on the server anyway")),
                Action.DELETE);*/
        } else {
            makeLabel(composite, Messages.getString(Messages.getString("Your file will be uploaded " +
                "to the server.")));
            /*makeRadioButton(composite, Messages.getString(Messages.getString("Ignore the " +
            	"deletion on the server and upload your local file")),
                Action.REVERTDELETE);*/
            /*makeRadioButton(composite, Messages.getString(Messages.getString("Ignore your " +
                "local modification and delete your file")),
                Action.DELETE);*/
        }
    }

    @Override
    protected void showFileInformation(Composite composite) {
        Label name = new Label(composite, SWT.READ_ONLY);
        name.setText(Messages.getString("Affected file/directory:") + " " + 
                conflict.getStatus().getPath());
    }
    
    protected Button makeRadioButton(Composite composite, String text, final Action action) {
        Button button = new Button(composite, SWT.RADIO);
        button.setText(text);
        if (action == defaultResolution) {
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
