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

import java.io.File;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.tigris.subversion.javahl.ClientException;
import org.tmatesoft.svn.core.SVNErrorCode;
import org.tmatesoft.svn.core.SVNErrorMessage;
import org.tmatesoft.svn.core.internal.wc.SVNErrorManager;
import org.tmatesoft.svn.core.internal.wc.SVNFileType;

import com.mindquarry.desktop.client.Messages;
import com.mindquarry.desktop.workspace.conflict.RenamingConflict;

/**
 * Base class provising functionality related to renaming a file.
 * 
 * @author <a href="mailto:christian(dot)christian(at)mindquarry.com">Christian
 *         Richardt</a>
 * @author <a href="mailto:naber(at)mindquarry(dot)com">Daniel Naber</a>
 * 
 */
public abstract class RenamingConflictDialog extends AbstractConflictDialog {

    protected String newName;
    protected RenamingConflict conflict;

    public RenamingConflictDialog(RenamingConflict conflict, Shell shell) {
        super(shell);
        this.conflict = conflict;
    }

    protected void newNameChanged(String name) {
        try {
            if (conflict.isRenamePossible(name)) {
                setErrorMessage(null);
                newName = name;
                if (okButton != null) {
                    okButton.setEnabled(true);
                }
            } else {
                setErrorMessage(Messages.getString(
                        "The filename '{1}' is not available, please choose a different one."));
                if (okButton != null) {
                    okButton.setEnabled(false);
                }
            }
        } catch (ClientException e) {
            throw new RuntimeException(e.toString(), e);
        }
    }

    protected String getUniqueNameSuggestion(String existingName, String comment) {
        String namePrefix; // e.g. "filename ("
        String nameInfix;  // e.g. "local" or ""
        String nameSuffix; // e.g. ").ext"
        
        if(comment == null || comment.length() == 0) {
            comment = "";
            nameInfix = "1";
        } else {
            nameInfix = comment;
        }
        
        int pos = existingName.lastIndexOf('.');
        if (pos == -1) { // no extension
            namePrefix = existingName + " (";
            nameSuffix = ")";
        } else {
            namePrefix = existingName.substring(0, pos) + " (";
            nameSuffix = ")" + existingName.substring(pos);
        }
        
        try {
            String newFilename = namePrefix + nameInfix + nameSuffix;
            for (int i = 2; i < 99999; i++) {
                if (conflict.isRenamePossible(newFilename)) {
                    return newFilename;
                }
                nameInfix = comment + i;
                newFilename = namePrefix + nameInfix + nameSuffix;
            }
        }
        catch(Exception e) {
            throw new RuntimeException(e);
        }

        throw new RuntimeException("Failed to find a unique filename based on '"+existingName+"'");
    }

    public String getNewName() {
        return newName;
    }

    protected Text createNewNameField(Composite subComposite, String oldName) {
        return createNewNameField(subComposite, oldName, "");
    }

    protected Text createNewNameField(Composite subComposite, String oldName, String comment) {
        final Text textField = new Text(subComposite, SWT.BORDER | SWT.SINGLE);
        // TODO: make field wider
        String nameSuggestion = getUniqueNameSuggestion(oldName, comment);
        newNameChanged(nameSuggestion);
        textField.setText(nameSuggestion);
        newName = nameSuggestion;
        textField.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent arg0) {
                textField.selectAll();
            }
    
            public void focusLost(FocusEvent arg0) {
            }
        });
        textField.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent arg0) {
                newNameChanged(textField.getText());
            }
        });
        return textField;
    }

}