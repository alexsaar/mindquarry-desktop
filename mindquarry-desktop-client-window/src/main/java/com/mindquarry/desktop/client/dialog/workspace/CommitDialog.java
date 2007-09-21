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

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.mindquarry.desktop.client.Messages;
import com.mindquarry.desktop.client.dialog.DialogBase;
import com.mindquarry.desktop.client.widget.workspace.ChangeSet;
import com.mindquarry.desktop.client.widget.workspace.ModificationDescription;
import com.mindquarry.desktop.client.widget.workspace.ChangeTree;
import com.mindquarry.desktop.client.widget.workspace.ChangeTree.ChangeTreeNode;
import com.mindquarry.desktop.client.widget.workspace.ChangeTree.TreeNode;
import com.mindquarry.desktop.client.widget.workspace.ChangeTree.TreeNodeVisitor;
import com.mindquarry.desktop.util.FileHelper;

/**
 * Ask the user for a commit message.
 * 
 * @author <a href="naber(at)mindquarry(dot)com">Daniel Naber</a>
 */
public class CommitDialog extends DialogBase {

    private String commitMessage = "";

//    private ChangeSet changeSet;
    private String teamName;
    private ChangeTree changeTree;
    private File workspaceRoot;
    private File teamDir;

    public CommitDialog(Shell shell, String teamName, ChangeTree changeTree, File workspaceRoot) {
        super(shell);
//        this.changeSet = changeSet;
        this.teamName = teamName;
        this.changeTree = changeTree;
        this.workspaceRoot = workspaceRoot;
        this.teamDir = new File(workspaceRoot, teamName);
        setShellStyle(SWT.CLOSE | SWT.MIN | SWT.MAX | SWT.RESIZE);
    }

    protected Control createContents(Composite parent) {
        Control contents = super.createContents(parent);
//        String teamName = changeSet.getTeam().getName();
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
        label.setText(Messages.getString("Modified files in '{0}':", teamDir.getAbsolutePath()));

        GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        gridData.heightHint = 50;
        gridData.widthHint = 360;

        // the text field for all the file names:
        // the list might get long, so we need a scrollable text field here:
        final Text fileField = new Text(composite, SWT.MULTI | SWT.BORDER | 
                SWT.V_SCROLL | SWT.WRAP | SWT.READ_ONLY);

        final StringBuilder sb = new StringBuilder();
        TreeNode node = changeTree.getRoot();
        node.visit(new TreeNodeVisitor() {
            public void visit(TreeNode node) {
                if (node instanceof ChangeTreeNode) {
                    ChangeTreeNode changeNode = (ChangeTreeNode) node;
                    ModificationDescription desc = ModificationDescription
                            .getDescription(changeNode.getChange());
                    String shortDesc = desc.getShortDescription();
                    // TODO: this currently happens because the changes
                    // also contains remote changes -- needs cleanup:
                    if (shortDesc == null || shortDesc.equals("")) {
                        return;
                    }
                    
                    if (sb.length() > 0)
                        sb.append("\n");
                    
                    // shorten the displayed path (remove path to team dir)
                    String pathName = changeNode.getFile().getAbsolutePath();
                    if (teamDir.equals(changeNode.getFile())) {
                        return; // ignore team workspace
                    }
                    if (FileHelper.isParent(teamDir, changeNode.getFile())) {
                        int length = teamDir.getAbsolutePath().length();
                        pathName = pathName.substring(length);
                        if(pathName.charAt(0) == '\\')
                            pathName = pathName.substring(1);
                    }
                    
                    sb.append(pathName+" ("+desc.getShortDescription()+")");
                }
            }
        });
        
        fileField.setLayoutData(gridData);
        fileField.setText(sb.toString());

        label = new Label(composite, SWT.READ_ONLY);
        label.setText(Messages.getString("Your message:"));

        // the main text field for the commit message:
        final Text textField = new Text(composite, SWT.MULTI | SWT.BORDER | 
                SWT.V_SCROLL | SWT.WRAP);
        textField.addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent evt) {
              // support Ctrl+Return => OK 
              if (evt.keyCode == SWT.CR && evt.stateMask==SWT.CTRL) {
                getButton(IDialogConstants.OK_ID).notifyListeners(SWT.Selection, new Event());
              }
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
