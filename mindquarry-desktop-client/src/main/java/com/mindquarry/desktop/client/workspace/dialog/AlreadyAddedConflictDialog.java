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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.tigris.subversion.javahl.Status;

import com.mindquarry.desktop.client.Messages;
import com.mindquarry.desktop.workspace.SVNHelper;

/**
 * Dialog for resolving working copy conflicts.
 * 
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class AlreadyAddedConflictDialog extends AbstractConflictDialog {

    private String newName;
    private Text newNameField;
    private String name;
    
    public AlreadyAddedConflictDialog(Shell shell, Status remoteStatus, String name) {
        super(shell, remoteStatus, SVNHelper.CONFLICT_RENAME_AND_RETRY);
        this.name = name;
    }

    @Override
    protected void createLowerDialogArea(Composite composite) {
        Composite subComposite = new Composite(composite, SWT.NONE);
        subComposite.setLayout(new GridLayout(2, false));
        Button button1 = makeRadioButton(subComposite, Messages.getString(AlreadyAddedConflictDialog.class, "1"),  //$NON-NLS-1$
                SVNHelper.CONFLICT_RENAME_AND_RETRY);
        button1.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                newNameField.setEnabled(true);
            }
        });
        newNameField = new Text(subComposite, SWT.BORDER | SWT.SINGLE);
        // TODO: make field wider
        String nameSuggestion = getNameSuggestion(name);
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
                newName = newNameField.getText();
            }
        });
        Button button2 = makeRadioButton(subComposite, Messages.getString(AlreadyAddedConflictDialog.class, "2"),  //$NON-NLS-1$
                SVNHelper.CONFLICT_OVERRIDE_FROM_WC);
        button2.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                newNameField.setEnabled(false);
            }
        });
    }

    private String getNameSuggestion(String name) {
        int pos = name.lastIndexOf('.');
        if (pos == -1) {
            // TODO: how to avoid suggestion a name that already exists?
            return name + "_1";
        } else {
            return name.substring(0, pos) + "_1" + name.substring(pos);
        }
    }

    protected String getNewName() {
        return newName;
    }
    
    @Override
    protected String getMessage() {
        return Messages.getString(AlreadyAddedConflictDialog.class, "0");
    }

}
