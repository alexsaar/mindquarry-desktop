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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.tigris.subversion.javahl.ClientException;
import org.tigris.subversion.javahl.StatusKind;

import com.mindquarry.desktop.client.Messages;
import com.mindquarry.desktop.workspace.conflict.ReplaceConflict;

/**
 * Dialog for resolving replace conflicts.
 * 
 * @author <a href="mailto:victor(dot)saar(at)mindquarry(dot)com">Victor Saar</a>
 */
public class ReplaceConflictDialog extends AbstractConflictDialog {

    private String newName;
    private Text newNameField;
    private ReplaceConflict conflict;
    private ReplaceConflict.Action resolveMethod;

    private static final ReplaceConflict.Action DEFAULT_RESOLUTION = ReplaceConflict.Action.RENAME;

    public ReplaceConflictDialog(ReplaceConflict conflict, Shell shell) {
        super(shell);
        this.conflict = conflict;
        resolveMethod = DEFAULT_RESOLUTION;
    }

    protected void showFileInformation(Composite composite) {
        Label name = new Label(composite, SWT.READ_ONLY);
        name.setText(Messages.getString("Filename(s)") + ": "
                + conflict.getStatus().getPath());
    }

    @Override
    protected String getMessage() {
        if (conflict.getStatus().getTextStatus() == StatusKind.replaced
                && conflict.getStatus().getRepositoryTextStatus() == StatusKind.replaced) {
            return Messages
                    .getString("A locally replaced file or directory (svn delete + svn add) "
                            + "was also replaced by someone else on the server.");
        } else if (conflict.getStatus().getTextStatus() == StatusKind.replaced) {
            return Messages
                    .getString("A locally replaced file or directory (svn delete + svn add) "
                            + "was modified by someone else on the server.");
        } else {
            return Messages
                    .getString("A locally modified file was replaced (svn delete + svn add) "
                            + "by someone else on the server.");
        }
    }

    @Override
    protected void createLowerDialogArea(Composite composite) {
        Composite subComposite = new Composite(composite, SWT.NONE);
        subComposite.setLayout(new GridLayout(2, false));
        Button button1 = makeRadioButton(subComposite, Messages
                .getString("Rename file and upload it under the new name"), //$NON-NLS-1$
                ReplaceConflict.Action.RENAME);
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
        // TODO: avoid suggesting a name that exists
        if (pos == -1) {
            return existingName + "_1";
        } else {
            return existingName.substring(0, pos) + "_1"
                    + existingName.substring(pos);
        }
    }

    public String getNewName() {
        return newName;
    }

    protected Button makeRadioButton(Composite composite, String text,
            final ReplaceConflict.Action action) {
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

    public ReplaceConflict.Action getResolveMethod() {
        return resolveMethod;
    }

}
