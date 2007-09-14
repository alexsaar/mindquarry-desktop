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

package com.mindquarry.desktop.client.dialog.workspace;

import java.io.File;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.mindquarry.desktop.client.Messages;
import com.mindquarry.desktop.client.dialog.DialogBase;
import com.mindquarry.desktop.client.widget.workspace.ChangeSet;
import com.mindquarry.desktop.client.widget.workspace.ModificationDescription;

/**
 * Ask the user for a commit message.
 * 
 * @author <a href="naber(at)mindquarry(dot)com">Daniel Naber</a>
 */
public class CommitDialog extends DialogBase {

    private String commitMessage = "";

    private ChangeSet changeSet;

    public CommitDialog(Shell shell, ChangeSet changeSet) {
        super(shell);
        this.changeSet = changeSet;
    }

    protected Control createContents(Composite parent) {
        Control contents = super.createContents(parent);
        String teamName = changeSet.getTeam().getName();
        String title = Messages.getString("Commit message for team '{0}'",  //$NON-NLS-1$ 
                teamName); 
        setTitle(title);
        getShell().setText(title);
        setMessage(Messages.getString("Please describe the changes you have " +  //$NON-NLS-1$
        		"made to the following files in team '{0}'", teamName),  //$NON-NLS-1$
        		IMessageProvider.INFORMATION);
        getShell().setSize(600, 350);
        getShell().redraw();
        return contents;
    }

    protected Control createDialogArea(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(1, false));
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

        // build the separator line
        Label titleBarSeparator = new Label(composite, SWT.HORIZONTAL
                | SWT.SEPARATOR);
        titleBarSeparator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label label = new Label(composite, SWT.READ_ONLY);
        label.setText(Messages.getString("Modified files:"));

        GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        gridData.heightHint = 50;
        gridData.widthHint = 360;

        // the text field for all the file names:
        // the list might get long, so we need a scrollable text field here:
        final Text fileField = new Text(composite, SWT.MULTI | SWT.BORDER | 
                SWT.V_SCROLL | SWT.WRAP | SWT.READ_ONLY);

        StringBuilder sb = new StringBuilder();
        for (File file : changeSet.getChanges().keySet()) {
            ModificationDescription desc = ModificationDescription.
                getLocalDescription(changeSet.getChanges().get(file));
            sb.append(file.getAbsolutePath() +
                    " (" + desc.getShortDescription() + ")\n");
        }
        fileField.setLayoutData(gridData);
        fileField.setText(sb.toString());

        label = new Label(composite, SWT.READ_ONLY);
        label.setText(Messages.getString("Your message:"));

        // the main text field for the commit message:
        final Text textField = new Text(composite, SWT.MULTI | SWT.BORDER | 
                SWT.V_SCROLL | SWT.WRAP);
        textField.addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent evt) {
            }
            public void keyReleased(KeyEvent evt) {
                setCommitMessage(textField.getText());
            }
        });
        textField.setFocus();
        textField.setLayoutData(gridData);
        
        Label barSeparator = new Label(composite, SWT.HORIZONTAL
                | SWT.SEPARATOR);
        barSeparator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        composite = new Composite(composite, SWT.NONE);
        composite.setLayout(new GridLayout(1, false));
        ((GridLayout) composite.getLayout()).marginWidth = 17;

        return composite;
    }
    
    public String getCommitMessage() {
        return commitMessage;
    }
    
    void setCommitMessage(String commitMessage) {
        this.commitMessage = commitMessage;
    }
    
    @Override
    protected String getHelpURL() {
        // TODO Auto-generated method stub
        return null;
    }

}
