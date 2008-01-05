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
import org.tigris.subversion.javahl.NodeKind;

import com.mindquarry.desktop.client.I18N;
import com.mindquarry.desktop.workspace.conflict.ObstructedConflict;

/**
 * Dialog for resolving replace conflicts.
 * 
 * @author <a href="mailto:victor(dot)saar(at)mindquarry(dot)com">Victor Saar</a>
 */
public class ObstructedConflictDialog extends RenamingConflictDialog {

    private ObstructedConflict.Action resolveMethod;

    protected Text newNameField;

    private static final ObstructedConflict.Action DEFAULT_RESOLUTION = ObstructedConflict.Action.RENAME;

    public ObstructedConflictDialog(ObstructedConflict conflict, Shell shell) {
        super(conflict, shell);
        resolveMethod = DEFAULT_RESOLUTION;
    }

    protected void showFileInformation(Composite composite) {
        Label name = new Label(composite, SWT.READ_ONLY);
        name.setText(I18N.getString("Filename(s)") + ": "
                + conflict.getStatus().getPath());
    }

    @Override
    protected String getMessage() {
        if (conflict.getStatus().getNodeKind() == NodeKind.dir) {
            return I18N
                    .getString("One of the local directories was deleted and replaced with a file of the same name. "
                            + "This structural change must be resolved before synchronization.");
        } else {
            return I18N
                    .getString("One of the local files was deleted and replaced with a directory of the same name. "
                            + "This structural change must be resolved before synchronization.");
        }
    }

    @Override
    protected void createLowerDialogArea(Composite composite) {
        Composite subComposite = new Composite(composite, SWT.NONE);
        subComposite.setLayout(new GridLayout(2, false));
        Button button1 = makeRadioButton(subComposite, I18N
                .getString("Rename file and upload it using a new name:"), //$NON-NLS-1$
                ObstructedConflict.Action.RENAME);
        button1.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                newNameField.setEnabled(true);
            }
        });

        newNameField = createNewNameField(subComposite, FilenameUtils.getName(conflict.getStatus().getPath()));
        newNameField.setFocus();

        Button button2 = makeRadioButton(
                subComposite,
                I18N
                        .getString("Remove obstructing file and download original from server"), //$NON-NLS-1$
                ObstructedConflict.Action.REVERT);
        button2.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                newNameField.setEnabled(false);
                okButton.setEnabled(true);
            }
        });
    }

    protected String getHelpURL() {
        // TODO fix help URL
        return "http://www.mindquarry.com/";
    }

    protected Button makeRadioButton(Composite composite, String text,
            final ObstructedConflict.Action action) {
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

    public ObstructedConflict.Action getResolveMethod() {
        return resolveMethod;
    }

}
