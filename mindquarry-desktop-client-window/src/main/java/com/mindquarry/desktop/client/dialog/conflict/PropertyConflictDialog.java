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
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
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
import com.mindquarry.desktop.workspace.conflict.PropertyConflict;

/**
 * Dialog for resolving replace conflicts.
 * 
 * @author <a href="mailto:victor(dot)saar(at)mindquarry(dot)com">Victor Saar</a>
 */
public class PropertyConflictDialog extends AbstractConflictDialog {
    private PropertyConflict conflict;
    private PropertyConflict.Action resolveMethod;
    
    private Text newPropText;
    private String newValue = new String("");

    private static final PropertyConflict.Action DEFAULT_RESOLUTION = PropertyConflict.Action.USE_LOCAL_VALUE;
    
    public PropertyConflictDialog(PropertyConflict conflict, Shell shell) {
        super(shell);
        this.conflict = conflict;
        resolveMethod = DEFAULT_RESOLUTION;
    }

    protected void showFileInformation(Composite composite) {
        Label name = new Label(composite, SWT.READ_ONLY);
        name.setText(Messages.getString("Property") + ": " + conflict.getLocalProperty().getName());
    }

    @Override
    protected String getMessage() {
        return Messages.getString("Somebody else modified the property you are trying to synchronize." +
                "Please select the version that should be treated as the current version.");
    }

    @Override
    protected void createLowerDialogArea(Composite composite) {
        Composite subComposite = new Composite(composite, SWT.NONE);
        subComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        subComposite.setLayout(new GridLayout(2, false));

        GridData gridData = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
        gridData.minimumHeight = 35;

        Button button1 = makeRadioButton(subComposite,
                Messages.getString("Use your local version of the property"),  //$NON-NLS-1$
                PropertyConflict.Action.USE_LOCAL_VALUE);
        button1.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
        
        Text localPropText = new Text(subComposite, SWT.BORDER | SWT.MULTI | SWT.READ_ONLY | SWT.V_SCROLL);
        localPropText.setLayoutData(gridData);
        localPropText.setText(conflict.getLocalProperty().getValue());
        
        Button button2 = makeRadioButton(subComposite,
                Messages.getString("Use the property from the server"),  //$NON-NLS-1$
                PropertyConflict.Action.USE_REMOTE_VALUE);
        button2.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
        
        Text remotePropText = new Text(subComposite, SWT.BORDER | SWT.MULTI | SWT.READ_ONLY | SWT.V_SCROLL);
        remotePropText.setLayoutData(gridData);
        remotePropText.setText(conflict.getRemoteProperty().getValue());
        
        Button button3 = makeRadioButton(subComposite,
                Messages.getString("Specify a new value for this property"),  //$NON-NLS-1$
                PropertyConflict.Action.USE_NEW_VALUE);
        button3.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
        
        newPropText = new Text(subComposite, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
        newPropText.setLayoutData(gridData);
        newPropText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                newValue = newPropText.getText();
            }});
    }
    
    public String getNewValue() {
        return newValue;
    }

    protected Button makeRadioButton(Composite composite, String text, final PropertyConflict.Action action) {
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

    public PropertyConflict.Action getResolveMethod() {
        return resolveMethod;
    }
}
