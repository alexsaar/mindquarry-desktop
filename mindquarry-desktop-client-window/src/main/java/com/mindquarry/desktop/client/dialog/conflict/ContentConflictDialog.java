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
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import com.mindquarry.desktop.client.Messages;
import com.mindquarry.desktop.workspace.conflict.ContentConflict;

/**
 * Dialog for resolving replace conflicts.
 * 
 * @author <a href="mailto:victor(dot)saar(at)mindquarry(dot)com">Victor Saar</a>
 */
public class ContentConflictDialog extends AbstractConflictDialog {

    private ContentConflict conflict;
    private ContentConflict.Action resolveMethod;

    private static final ContentConflict.Action DEFAULT_RESOLUTION = ContentConflict.Action.USE_LOCAL;
    
    public ContentConflictDialog(ContentConflict conflict, Shell shell) {
        super(shell);
        this.conflict = conflict;
        resolveMethod = DEFAULT_RESOLUTION;
    }

    protected void showFileInformation(Composite composite) {
        Label name = new Label(composite, SWT.READ_ONLY);
        name.setText(Messages.getString("Filename(s)") + ": " + conflict.getStatus().getPath());
    }

    @Override
    protected String getMessage() {
        return Messages.getString("Somebody else modified the file your are trying to synchronize. " +
                "Please select the version that should be treated as the current version.");
    }

    @Override
    protected void createLowerDialogArea(Composite composite) {
        Composite subComposite = new Composite(composite, SWT.NONE);
        subComposite.setLayout(new RowLayout(SWT.VERTICAL));
        Button button1 = makeRadioButton(subComposite,
                Messages.getString("Use your local version of the file"),  //$NON-NLS-1$
                ContentConflict.Action.USE_LOCAL);
        button1.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                okButton.setEnabled(true);
            }
        });
                
        Button button2 = makeRadioButton(subComposite,
                Messages.getString("Use the file from the server"),  //$NON-NLS-1$
                ContentConflict.Action.USE_REMOTE);
        button2.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                okButton.setEnabled(true);
            }
        });

//        Button button3 = makeRadioButton(subComposite,
//                Messages.getString("Merge your changes with the file from the server"),  //$NON-NLS-1$
//                ContentConflict.Action.USE_REMOTE);
//        button3.addListener(SWT.Selection, new Listener() {
//            public void handleEvent(Event event) {
//                okButton.setEnabled(true);
//            }
//        });
    }

    protected Button makeRadioButton(Composite composite, String text, final ContentConflict.Action action) {
        Button button = new Button(composite, SWT.RADIO);
        button.setText(text);
        if (action == DEFAULT_RESOLUTION) {
            button.setSelection(true);
        }
        button.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                resolveMethod = action;
            }
        });
        return button;
    }

    public ContentConflict.Action getResolveMethod() {
        return resolveMethod;
    }

}
