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
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.tigris.subversion.javahl.ClientException;

import com.mindquarry.desktop.client.Messages;
import com.mindquarry.desktop.workspace.SVNHelper;
import com.mindquarry.desktop.workspace.conflict.AddConflict;

/**
 * Dialog for resolving "add" conflicts.
 * 
 * @author dnaber
 */
public class AddConflictDialog extends AbstractConflictDialog {

    private String newName;
    private Text newNameField;
    private AddConflict conflict;
    
    public AddConflictDialog(AddConflict conflict, Shell shell) {
        super(shell, SVNHelper.CONFLICT_RENAME_AND_RETRY);
        this.conflict = conflict;
    }

    protected void showFileInformation(Composite composite) {
        Label name = new Label(composite, SWT.READ_ONLY);
        name.setText(Messages.getString("Local Filename(s)") + ": " + conflict.getLocalAdded());
        Label name2 = new Label(composite, SWT.READ_ONLY);
        name2.setText(Messages.getString("Remote Filename(s)") + ": " + conflict.getRemoteAdded());
    }

    @Override
    protected void createLowerDialogArea(Composite composite) {
        Composite subComposite = new Composite(composite, SWT.NONE);
        subComposite.setLayout(new GridLayout(2, false));
        Button button1 = makeRadioButton(subComposite,
                Messages.getString("Rename file and upload it under the new name"),  //$NON-NLS-1$
                SVNHelper.CONFLICT_RENAME_AND_RETRY);
        button1.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                newNameField.setEnabled(true);
            }
        });
                
        newNameField = new Text(subComposite, SWT.BORDER | SWT.SINGLE);
        // TODO: make field wider
        String oldName = FilenameUtils.getName(conflict.getStatus().getPath());
        String nameSuggestion = getNameSuggestion(oldName);
        newNameChanged(nameSuggestion);
        newNameField.setText(nameSuggestion);
        newName = nameSuggestion;
        newNameField.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent arg0) {
                newNameField.selectAll();
            }
            public void focusLost(FocusEvent arg0) {
            }
        });
        newNameField.addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent arg0) {
            }
            public void keyReleased(KeyEvent arg0) {
                newNameChanged(newNameField.getText());
            }
        });
        Button button2 = makeRadioButton(subComposite,
                Messages.getString("Overwrite the file on the server with your local version"),  //$NON-NLS-1$
                SVNHelper.CONFLICT_OVERRIDE_FROM_WC);
        button2.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                newNameField.setEnabled(false);
                okButton.setEnabled(true);
            }
        });
    }

    private void newNameChanged(String name) {
        try {
            if (conflict.isRenamePossible(name)) {
                newName = newNameField.getText();
                if (okButton != null) {
                    okButton.setEnabled(true);
                }
                System.err.println(name + " accepted");
            } else {
                // TODO: show error
                if (okButton != null) {
                    okButton.setEnabled(false);
                }
                System.err.println(name + " not accepted");
            }
        } catch (ClientException e) {
            throw new RuntimeException(e.toString(), e);
        }
    }
    
    private String getNameSuggestion(String existingName) {
        int pos = existingName.lastIndexOf('.');
        if (pos == -1) {
            return existingName + "_1";
        } else {
            return existingName.substring(0, pos) + "_1" + existingName.substring(pos);
        }
    }

    public String getNewName() {
        return newName;
    }
    
    @Override
    protected String getMessage() {
        return Messages.getString("One of the local files you want to upload to " +
        		"the server already exists on the server. This case can occur if " +
        		"you created a file or renamed an existing file and someone created " +
        		"the file in between (by creating it or renaming an existing file).");
    }

}
